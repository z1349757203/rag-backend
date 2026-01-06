package com.rag.ragbackend.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.ExtractedTextFormatter;
import org.springframework.ai.reader.TextReader;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.ai.reader.pdf.config.PdfDocumentReaderConfig;
import org.springframework.ai.transformer.ContentFormatTransformer;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;
import java.io.InputStream;
import java.util.List;

@Slf4j
public class TextExtractor {

    /** 读取 PDF → 提取文本 */
    public static List<Document> extractPdf(InputStream in) {
        try {
            // 1. 配置 PDF 读取规则（可自定义页码范围、是否提取图片等）
            PdfDocumentReaderConfig config = PdfDocumentReaderConfig.builder()
                    .withPageTopMargin(0)
                    .withPageExtractedTextFormatter(ExtractedTextFormatter.builder()
                            .withNumberOfTopTextLinesToDelete(0)
                            .build())
                    .withPagesPerDocument(1)
                    .build();

            // 2. 使用 Spring AI 封装的 PDF 读取器
            PagePdfDocumentReader pdfReader = new PagePdfDocumentReader(
                    new InputStreamResource(in),
                    config
            );

            // 3. 提取所有文本并拼接
            List<Document> documents = pdfReader.get();



            return documents;
        } catch (Exception e) {
            throw new RuntimeException("PDF解析失败", e);
        }
    }

    /** 读取 txt/md → 字符串 */
    public static List<Document> extractText(MultipartFile file) {
        try {
            InputStream inputStream = file.getInputStream();
            Resource resource = new InputStreamResource(inputStream) {
                @Override
                public String getFilename() {
                    return file.getOriginalFilename();
                }
                // 重写 exists 避免默认返回 false
                @Override
                public boolean exists() {
                    return true;
                }
            };

            TextReader reader = new TextReader(resource);
            reader.getCustomMetadata().put("source", resource.getFilename());
            List<Document> documents = reader.read();
            // 内容格式标准化（清洗乱码、统一格式）
//            ContentFormatTransformer contentFormatTransformer = new ContentFormatTransformer();
//            documents = contentFormatTransformer.apply(documents);
            // 中文文本首选 —— TokenTextSplitter（稳定）
            TokenTextSplitter splitter  = new TokenTextSplitter(800, 600, 20, 1000, true);

            List<Document> splitDocs = splitter.split(documents);
            log.info("文本文件读取完成，共 {} 页，切分后 {} 页", documents.size(), splitDocs.size());
            return splitDocs;
        } catch (Exception e) {
            throw new RuntimeException("文本文件读取失败", e);
        }
    }
}
