package com.rag.ragbackend.utils;

import org.springframework.util.DigestUtils;

import java.util.UUID;

/**
 * @Description
 * @Version 1.0.0
 * @Date 2025-12-25 20:35
 * @Author by zjh
 */
public class MD5Util {

    /**
     * 生成UUID盐值（去除连字符，32位）
     */
    public static String generateUUIDSalt() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    /**
     * MD5加盐哈希（UUID盐值）
     */
    public static String md5WithUUIDSalt(String rawData) {
        String salt = generateUUIDSalt();
        // 原始数据 + 盐值（拼接规则前后端统一）
        String dataWithSalt = rawData + "_" + salt;
        return DigestUtils.md5DigestAsHex(dataWithSalt.getBytes());
    }

}


