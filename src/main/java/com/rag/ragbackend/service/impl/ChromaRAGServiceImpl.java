package com.rag.ragbackend.service.impl;

import com.rag.ragbackend.manager.SseEmitterManager;
import com.rag.ragbackend.pojo.resp.RAGResponse;
import com.rag.ragbackend.pojo.resp.SseMessage;
import com.rag.ragbackend.service.ChatService;
import com.rag.ragbackend.service.ChromaRAGService;
import com.rag.ragbackend.service.EmbeddingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import java.util.List;

/**
 * @Description
 * @Version 1.0.0
 * @Date 2025-12-8 22:05
 * @Author by zjh
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ChromaRAGServiceImpl implements ChromaRAGService {

    private final ChromaServiceImpl chromaService;
    private final ChatService chatService;
    private final SseEmitterManager sseManager;


    /** 核心 RAG 工作流 */
    public RAGResponse chatWithRAG(String question) {
        List<Document> chunks = chromaService.queryVectorStore(question, 3);

        StringBuilder context = new StringBuilder();
        for (Document chunk : chunks) {
            context.append(chunk.getText()).append("\n");
        }

        String prompt = """
                基于以下资料回答问题，请勿编造信息：
                
                【知识库内容】
                %s
                
                【用户问题】
                %s
                """.formatted(context, question);

        String str = chatService.callChatRag(prompt);
        return new RAGResponse(str,null);
    }

    @Override
    public Flux<String> streamChatWithRAG(String question) {

        List<Document> chunks = chromaService.queryVectorStore(question, 3);

        StringBuilder context = new StringBuilder();
        for (Document chunk : chunks) {
            context.append(chunk.getText()).append("\n");
        }

        String prompt = """
                基于以下资料回答问题，请勿编造信息：
                
                【知识库内容】
                %s
                
                【用户问题】
                %s
                """.formatted(context, question);

        Flux<String> stringFlux = chatService.streamChatRag(prompt);
        return stringFlux;
    }

    @Override
    public void asyncGenerate(String taskId, String question) {
        log.info("开始生成, taskId: {}", taskId);
        Flux<String> stream = this.streamChatWithRAG(question);

        stream.subscribe(
                token -> sseManager.send(taskId, SseMessage.delta(token)),
                error -> {
                    sseManager.send(taskId,
                            new SseMessage("error", error.getMessage()));
                    sseManager.complete(taskId);
                },
                () -> {
                    sseManager.send(taskId, SseMessage.done());
                    sseManager.complete(taskId);
                }
        );
    }

}
