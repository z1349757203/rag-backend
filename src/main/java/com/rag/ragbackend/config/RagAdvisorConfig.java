//
//package com.rag.ragbackend.config;
//
//import org.springframework.ai.chat.prompt.PromptTemplate;
//import org.springframework.ai.ollama.OllamaChatModel;
//import org.springframework.ai.ollama.OllamaEmbeddingModel;
//import org.springframework.ai.rag.advisor.RetrievalAugmentationAdvisor;
//import org.springframework.ai.vectorstore.VectorStore;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//
///**
// * @Description RAG 顾问配置类
// * @Version 1.0.0
// * @Date 2026-1-3 23:05
// * @Author by zjh
// */
//@Configuration
//public class RagAdvisorConfig {
//
//    @Bean
//    public RetrievalAugmentationAdvisor ragAdvisor(
//            OllamaChatModel chatModel,
//            OllamaEmbeddingModel embeddingModel,
//            VectorStore vectorStore) {
//
//        // 初始化RAG增强器
//        RetrievalAugmentationAdvisor ragAdvisor = new RetrievalAugmentationAdvisor(chatModel, vectorStore);
//        // 生产配置：关闭调试、设置超时
//        ragAdvisor.setDebug(false);
//        ragAdvisor.setTimeout(5000); // RAG流程耗时更长，超时设5秒
//        return ragAdvisor;
//    }
//}