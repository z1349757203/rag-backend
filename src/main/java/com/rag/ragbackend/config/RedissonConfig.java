package com.rag.ragbackend.config;

import org.redisson.spring.data.connection.RedissonConnectionFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
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
    @Bean
    public RedissonConnectionFactory redissonConnectionFactory() {
        // Redisson会自动读取配置文件中的spring.redis或redisson.config配置
        return new RedissonConnectionFactory();
    }

    /**
     * 配置RedisTemplate，使其使用Redisson连接工厂
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedissonConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();

        // 设置核心：使用Redisson的连接工厂
        redisTemplate.setConnectionFactory(connectionFactory);

        // 配置序列化器（解决乱码问题）
        StringRedisSerializer stringSerializer = new StringRedisSerializer();
        GenericJackson2JsonRedisSerializer jsonSerializer = new GenericJackson2JsonRedisSerializer();

        // key和hashKey使用String序列化
        redisTemplate.setKeySerializer(stringSerializer);
        redisTemplate.setHashKeySerializer(stringSerializer);

        // value和hashValue使用JSON序列化
        redisTemplate.setValueSerializer(jsonSerializer);
        redisTemplate.setHashValueSerializer(jsonSerializer);

        // 初始化模板
        redisTemplate.afterPropertiesSet();

        return redisTemplate;
    }
}

