package com.rag.ragbackend.config;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.redisson.spring.data.connection.RedissonConnectionFactory;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * @Description
 * @Version 1.0.0
 * @Date 2025-12-25 21:01
 * @Author by zjh
 */
@Configuration
public class RedissonConfig {

    /**
     * 注入Redisson连接工厂（核心：替换默认的LettuceConnectionFactory）
     */
//    @Bean
//    public RedissonConnectionFactory redissonConnectionFactory() {
//        // Redisson会自动读取配置文件中的spring.redis或redisson.config配置
//        return new RedissonConnectionFactory();
//    }

    /**
     * 配置RedisTemplate，使其使用Redisson连接工厂
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedissonConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        // 配置 ObjectMapper 保留类型信息
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.activateDefaultTyping(
                LaissezFaireSubTypeValidator.instance,
                ObjectMapper.DefaultTyping.NON_FINAL,
                JsonTypeInfo.As.PROPERTY
        );

        // 注册Message的所有实现类（可选，提升反序列化效率）
        objectMapper.registerSubtypes(
                UserMessage.class,
                AssistantMessage.class,
                SystemMessage.class
        );

        // 使用构造函数创建序列化器
        Jackson2JsonRedisSerializer<Object> serializer =
                new Jackson2JsonRedisSerializer<>(objectMapper, Object.class);

        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(serializer);
        template.afterPropertiesSet();
        return template;

        // 配置序列化器（解决乱码问题）
//        StringRedisSerializer stringSerializer = new StringRedisSerializer();
//        GenericJackson2JsonRedisSerializer jsonSerializer = new GenericJackson2JsonRedisSerializer();
//        // key和hashKey使用String序列化
//        redisTemplate.setKeySerializer(stringSerializer);
//        redisTemplate.setHashKeySerializer(stringSerializer);
//        // value和hashValue使用JSON序列化
//        redisTemplate.setValueSerializer(jsonSerializer);
//        redisTemplate.setHashValueSerializer(jsonSerializer);
//
//        // 初始化模板
//        redisTemplate.afterPropertiesSet();
//
//        return redisTemplate;
    }
}

