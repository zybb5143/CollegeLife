package com.shengda.tie;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * 帖子(Tie)独立微服务启动类（实验级）
 * 仅扫描本包 com.shengda.tie，不引入 com.example.demo，避免与单体 Bean 冲突。
 */
@SpringBootApplication
@EnableDiscoveryClient
@MapperScan("com.shengda.tie.mapper")
public class TieServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(TieServiceApplication.class, args);
    }
}
