package com.rag.ragbackend.utils;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.ExtractedTextFormatter;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.ai.reader.pdf.config.PdfDocumentReaderConfig;
import org.springframework.core.io.InputStreamResource;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

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
    public static String extractText(InputStream in) {
        try {
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("文本文件读取失败", e);
        }
    }
}
