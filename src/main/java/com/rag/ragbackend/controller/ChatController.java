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
        sseManager.create(taskId);
        String finalTaskId = taskId;
        CompletableFuture.runAsync(() -> {
            log.info("异步任务开始");
            try {
                ragService.asyncGenerate(finalTaskId, req.getMessage());
            } catch (Exception e) {
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
    @PostMapping("/getStreamMsg")
    public String getStreamMsg(@RequestBody Question req, HttpServletRequest request) {
        String clientIP = getClientIpAddress(request);
        log.info("请求来自IP: {}", clientIP);
        String userQuest = MD5Util.md5WithUUIDSalt(req.getMessage());
        redisTemplate.opsForValue().set(clientIP, userQuest, 10, TimeUnit.MINUTES);
        return userQuest;
    }

    @PostMapping(value = "/ragDb", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamChatWithRAG(@RequestBody Question req) {
        SseEmitter emitter = new SseEmitter(5 * 60 * 1000L); // 0-不超时，5 * 60 * 1000L - 5分钟超时
        Flux<String> stringFlux = ragService.streamChatWithRAG(req.message);
        stringFlux
                .doOnNext(token -> {
                    try {
                        Map<String, Object> payload = Map.of(
                                "type", "delta",
                                "content", token
                        );
                        emitter.send(payload);
                        log.info("发送 token={}", token);
                    } catch (Exception e) {
                        log.error("SSE 发送错误", e);
                        emitter.completeWithError(e);
                    }
                })
                .doOnComplete(() -> {
                    emitter.complete();
                })
                .doOnError(e -> {
                    emitter.completeWithError(e);
                }).subscribe();
        log.info("返回值={}", stringFlux);
        return emitter;
    }

    @Data
    public static class Question {
        public String message;
    }

    /**
     * 接收前端 JSON 格式 {"message": "..."}
     */
    public static class ChatRequest {
        private String message;

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
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
