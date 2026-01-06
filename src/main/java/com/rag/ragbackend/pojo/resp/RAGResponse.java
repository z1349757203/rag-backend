package com.rag.ragbackend.pojo.resp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @Description
 * @Version 1.0.0
 * @Date 2025-12-14 20:53
 * @Author by zjh
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RAGResponse {

    private String answer;

    private List<String> chunks;
}
