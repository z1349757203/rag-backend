package com.rag.ragbackend.service;

import org.springframework.ai.document.Document;

import java.util.List;
import java.util.Map;

/**
 * @Description
 * @Version 1.0.0
 * @Date 2025-12-8 22:02
 * @Author by zjh
 */
public interface ChromaService {

    void addEmbedding(String id, String text, List<Double> embedding);


    List<Document> queryVectorStore(String queryText, int topK);
}
