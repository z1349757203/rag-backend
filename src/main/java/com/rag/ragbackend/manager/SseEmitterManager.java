package com.rag.ragbackend.manager;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class SseEmitterManager {

    private final Map<String, SseEmitter> emitters = new ConcurrentHashMap<>();

    public SseEmitter create(String taskId) {
        SseEmitter emitter = new SseEmitter(0L); // 永不超时
        emitters.put(taskId, emitter);

        emitter.onCompletion(() -> emitters.remove(taskId));
        emitter.onTimeout(() -> emitters.remove(taskId));
        emitter.onError(e -> emitters.remove(taskId));

        return emitter;
    }

    public SseEmitter get(String taskId) {
        return emitters.get(taskId);
    }

    public void send(String taskId, Object data) {
        SseEmitter emitter = emitters.get(taskId);
        if (emitter == null) return;

        try {
            emitter.send(data);
        } catch (Exception e) {
            emitters.remove(taskId);
        }
    }

    public void complete(String taskId) {
        SseEmitter emitter = emitters.remove(taskId);
        if (emitter != null) {
            emitter.complete();
        }
    }
}
