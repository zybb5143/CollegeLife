package com.example.demo.config;

import java.util.Arrays;
import java.util.List;

/**
 * CORS 配置常量类
 * 统一管理所有允许的跨域源
 */
public class CorsConfigConstants {

    // CORS 允许的源列表
    public static final List<String> ALLOWED_ORIGINS = Arrays.asList(
            "http://localhost:5173",
            "http://localhost:8080",
            "http://localhost:10086",
            "http://localhost:80",
            "http://localhost:3000",
            "http://127.0.0.1:5173",
            "http://127.0.0.1:8080",
            "http://127.0.0.1:10086",
            "http://127.0.0.1:80",
            "http://127.0.0.1:3000"
    );

    // 允许的HTTP方法
    public static final List<String> ALLOWED_METHODS = Arrays.asList(
            "GET", "POST", "PUT", "DELETE", "OPTIONS"
    );

    // 允许的所有Header
    public static final List<String> ALLOWED_HEADERS = Arrays.asList("*");

    // 暴露的响应头
    public static final List<String> EXPOSED_HEADERS = Arrays.asList("X-Backend-Server");

    // 预检请求缓存时间（秒）
    public static final long MAX_AGE = 3600L;

    // 是否允许携带凭证
    public static final boolean ALLOW_CREDENTIALS = true;
}
