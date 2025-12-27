package com.rag.ragbackend.pojo.resp;

/**
 * @Description
 * @Version 1.0.0
 * @Date 2025-12-27 16:47
 * @Author by zjh
 */
public record SseMessage(String type, String content) {

    public static SseMessage delta(String token) {
        return new SseMessage("delta", token);
    }

    public static SseMessage done() {
        return new SseMessage("done", null);
    }
}
