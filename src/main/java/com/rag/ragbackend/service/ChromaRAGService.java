package com.rag.ragbackend.service;

import com.rag.ragbackend.pojo.resp.RAGResponse;
import com.rag.ragbackend.service.impl.ChromaRAGServiceImpl;
import reactor.core.publisher.Flux;

/**
 * @Description
 * @Version 1.0.0
 * @Date 2025-12-8 22:05
 * @Author by zjh
 */
public interface ChromaRAGService {
    RAGResponse chatWithRAG(String question);

    Flux<String> streamChatWithRAG(String question);
}
