package com.rag.ragbackend.service;

import org.springframework.ai.document.Document;

import java.util.List;

/**
 * @Description
 * @Version 1.0.0
 * @Date 2025-12-9 21:23
 * @Author by zjh
 */
public interface DataUploadService {
    int indexDocument(String text);

    void addDocument(List<Document> documents);
}
