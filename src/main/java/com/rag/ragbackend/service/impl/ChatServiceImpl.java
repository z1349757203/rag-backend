package com.rag.ragbackend.service.impl;

import com.rag.ragbackend.manager.SessionChatHistoryManager;
import com.rag.ragbackend.service.ChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.model.StreamingChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import java.util.*;


/**
 * ChatService - 最小可用的内存 RAG 流程实现
 *
 * 流程：
 * 1. 将用户 query 通过 EmbeddingService 生成向量
 * 2. 在内存向量库中做相似度检索（cosine）
 * 3. 将 topK 检索到的片段拼接到 prompt 中，调用 OllamaChatModel 生成最终回答
 *
 * 注意：
 * - 这是演示/开发阶段用的实现，推荐在生产中替换为 Milvus / PGVector / Chroma 等向量库
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ChatServiceImpl implements ChatService {
    private final OllamaChatModel chatModel;


    @Override
    public Flux<String> streamChatRag(Message... messages) {
        try {
            Flux<String> stream = chatModel.stream(messages);
            return stream;
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    @Override
    public String callChatRag(String query) {
        return chatModel.call(query);
    }

    @Override
    public String callChatRag(List<Message> messages) {
        Prompt prompt = new Prompt(messages);
        Generation generation = chatModel.call(prompt).getResult();
        return (generation != null) ? generation.getOutput().getText() : "";
    }

    @Override
    public void CompressionChat() {

    }

}
