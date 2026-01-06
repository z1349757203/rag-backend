package com.rag.ragbackend.manager;

import com.rag.ragbackend.pojo.RedisMessage;
import jakarta.annotation.Resource;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Description
 * @Version 1.0.0
 * @Date 2026-1-1 17:43
 * @Author by zjh
 */
@Component
public class SessionChatHistoryManager {

    private final Map<String, List<Message>> sessionHistory = new ConcurrentHashMap<>();

    @Resource
    private RedisTemplate redisTemplate;


    public List<Message> getHistory(String ipStr) {
        List<Message> messages = new ArrayList<>();
        List range = redisTemplate.opsForList().range(ipStr, 0, -1);
        if (range == null) {
            return messages;
        }
        List<RedisMessage> redisMessages = range;
        for (RedisMessage redisMessage : redisMessages) {
            if (redisMessage.getRole().equals("user")) {
                messages.add(new UserMessage(redisMessage.getContent()));
            } else if (redisMessage.getRole().equals("assistant")) {
                messages.add(new AssistantMessage(redisMessage.getContent()));
            }
        }
        return messages;



//        return sessionHistory.computeIfAbsent(ipStr, k -> new ArrayList<>());
    }

    public void addHistory(String ipStr, RedisMessage message) {
//        List<Message> historyList = getHistory(ipStr);
//        historyList.add(message);
//        if (historyList.size() > 10) {
//            historyList.subList(0, historyList.size() - 10).clear();
//        }
        redisTemplate.opsForList().rightPush(ipStr, message);
        redisTemplate.opsForList().trim(ipStr, -10, -1);
    }

    public void clearHistory(String ipStr) {
        sessionHistory.remove(ipStr);
    }
}
