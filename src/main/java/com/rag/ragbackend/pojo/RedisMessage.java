package com.rag.ragbackend.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Description
 * @Version 1.0.0
 * @Date 2026-1-2 15:43
 * @Author by zjh
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class RedisMessage {

    private String role;

    private String content;
}
