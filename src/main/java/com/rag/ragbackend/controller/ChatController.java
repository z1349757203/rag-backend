package com.rag.ragbackend.controller;

import com.rag.ragbackend.manager.SseEmitterManager;
import com.rag.ragbackend.service.ChromaRAGService;
import com.rag.ragbackend.utils.MD5Util;
import jakarta.servlet.http.HttpServletRequest;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import reactor.core.publisher.Flux;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

@Slf4j
@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
@CrossOrigin(origins = "*") // 允许跨域，前端可正常访问
public class ChatController {

    private final ChromaRAGService ragService;

    private final RedisTemplate redisTemplate;

    private final Executor bizExecutor;

    private final SseEmitterManager sseManager;

    @PostMapping("/ask")
    public String ragDb(@RequestBody Question req, HttpServletRequest request) {
        String clientIP = getClientIpAddress(request);
        log.info("请求来自IP: {}", clientIP);
        String taskId = MD5Util.md5WithUUIDSalt(req.getMessage());
        redisTemplate.opsForValue().set(taskId, req.getMessage(), 10, TimeUnit.MINUTES);
        sseManager.create(taskId);
        String finalTaskId = taskId;
        CompletableFuture.runAsync(() -> {
            log.info("异步任务开始");
            try {
                ragService.asyncGenerate(clientIP, finalTaskId, req.getMessage());
            } catch (Exception e) {
                log.error("异步任务执行异常", e);
                Thread.currentThread().interrupt();
            }
            log.info("异步任务完成");
        }, bizExecutor);
        return taskId;
    }

    /**
     * 2️ 前端用 taskId 建立 SSE 监听
     * 返回SseEmitter需要设置 MediaType.TEXT_EVENT_STREAM_VALUE
     */
    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter stream(@RequestParam String taskId) {
        return sseManager.get(taskId);
    }

    @Data
    public static class Question {
        public String message;
    }


    /**
     * 获取客户端真实IP地址
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Forwarded-For");
            if (ip != null && ip.contains(",")) {
                ip = ip.split(",")[0].trim();
            }
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }
}
