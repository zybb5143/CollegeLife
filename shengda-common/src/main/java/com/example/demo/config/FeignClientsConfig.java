package com.example.demo.config;

import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Configuration;

/**
 * 启用 Feign 客户端扫描，使单体（各 app 模块）能调用独立部署的微服务。
 * 放在 shengda-common 内，三个启动类无需改动。
 */
@Configuration
@EnableFeignClients(basePackages = "com.example.demo.feign")
public class FeignClientsConfig {
}
