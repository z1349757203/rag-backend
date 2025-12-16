package com.rag.ragbackend.service.impl;

import com.rag.ragbackend.service.ChromaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChromaServiceImpl implements ChromaService {

    private final VectorStore vectorStore;

    /** 插入向量 */
    @Override
    public void addEmbedding(String id, String text, List<Double> embedding) {
        log.info("添加向量 id={}, text={}, embedding={}", id, text, embedding);
        Document document = new Document(id, text, Map.of("embedding", embedding));
        vectorStore.add(List.of(document));

    }

    @Override
    public List<Document> queryVectorStore(String queryText, int topK) {
        List<Document> documents = vectorStore.similaritySearch(SearchRequest.builder()
                .query(queryText)
                .build());
        return documents;
    }
}
