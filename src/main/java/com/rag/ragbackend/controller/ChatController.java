package com.rag.ragbackend.controller;

import com.rag.ragbackend.service.ChatService;
import com.rag.ragbackend.service.ChromaRAGService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import com.rag.ragbackend.pojo.resp.RAGResponse;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import reactor.core.publisher.Flux;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
@CrossOrigin(origins = "*") // 允许跨域，前端可正常访问
public class ChatController {

    private final ChatService chatService;

    private final ChromaRAGService ragService;

//    @PostMapping("/ragDb")
//    public RAGResponse ragDb(@RequestBody Question req) {
//        RAGResponse ragResponse = ragService.chatWithRAG(req.message);
//        log.info("返回值={}", ragResponse);
//        return ragResponse;
//    }

    @PostMapping(value = "/ragDb", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamChatWithRAG(@RequestBody Question req) {
        SseEmitter emitter = new SseEmitter(0L); // 不超时

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
}
