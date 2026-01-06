package com.rag.ragbackend.service.impl;

import com.rag.ragbackend.service.ChromaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.model.transformer.KeywordMetadataEnricher;
import org.springframework.ai.model.transformer.SummaryMetadataEnricher;
import org.springframework.ai.ollama.OllamaChatModel;
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
    private final OllamaChatModel chatModel;

    @Override
    public void addDocument(List<Document> documents) {
        log.info("开始添加文档");

        KeywordMetadataEnricher keywordMetadataEnricher = new KeywordMetadataEnricher(this.chatModel, 5);
        SummaryMetadataEnricher summaryMetadataEnricher = new SummaryMetadataEnricher(chatModel,
                List.of(SummaryMetadataEnricher.SummaryType.PREVIOUS, SummaryMetadataEnricher.SummaryType.CURRENT, SummaryMetadataEnricher.SummaryType.NEXT));
        summaryMetadataEnricher.apply(documents);
        log.info("摘要添加完成");

        keywordMetadataEnricher.apply(documents);
        log.info("关键字添加完成");

        vectorStore.add(documents);
    }
    @Override
    public List<Document> queryVectorStore(String queryText, int topK) {
        List<Document> documents = vectorStore.similaritySearch(SearchRequest.builder()
                .similarityThreshold(0.65f)
                .topK(topK)
                .query(queryText)
                .build());
        return documents;
    }
}
