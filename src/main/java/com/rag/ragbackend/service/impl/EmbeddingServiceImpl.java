package com.rag.ragbackend.service.impl;

import com.rag.ragbackend.service.EmbeddingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.ollama.OllamaEmbeddingModel;
import org.springframework.stereotype.Service;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * 兼容多种返回类型的 EmbeddingService。
 * - 将 embeddingModel.embed(text) 的返回值解析为 List<Double>
 * - 打印实际返回类型，便于调试
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmbeddingServiceImpl implements EmbeddingService {

}
