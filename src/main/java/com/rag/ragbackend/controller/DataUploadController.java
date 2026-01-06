package com.rag.ragbackend.controller;

import com.rag.ragbackend.service.ChromaService;
import com.rag.ragbackend.utils.TextExtractor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/data")
@RequiredArgsConstructor
@CrossOrigin
public class DataUploadController {

    private final ChromaService chromaService;

    @PostMapping("/upload")
    public UploadResponse upload(@RequestParam("file") MultipartFile file) throws Exception {

        String filename = file.getOriginalFilename();
        if (filename == null) throw new RuntimeException("文件名为空");

        String text;
        int chunks = 0;
        if (filename.endsWith(".pdf")) {
            List<Document> documents = TextExtractor.extractPdf(file.getInputStream());
            chromaService.addDocument(documents);
            chunks = documents.size();
        } else if (filename.endsWith(".txt") || filename.endsWith(".md")) {
            List<Document> documents = TextExtractor.extractText(file);
            chromaService.addDocument(documents);
            chunks = documents.size();
        } else {
            throw new RuntimeException("仅支持 PDF / TXT / MD 文件");
        }

        return new UploadResponse("上传成功", chunks);
    }

    public record UploadResponse(String message, int chunks) {}
}
