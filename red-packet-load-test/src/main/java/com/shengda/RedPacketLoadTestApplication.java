package com.shengda;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 抢红包压测应用启动类
 * 
 * 启动后自动执行压测任务，分别对 Redis 版本和 MySQL 版本的抢红包接口进行高并发测试，
 * 并输出性能对比报告。
 * 
 * 使用方式：
 * 1. 确保目标服务（shengda-app-10010/10011/10012）已启动
 * 2. 确保已发布好测试用的红包
 * 3. 修改 application.properties 中的压测参数
 * 4. 运行本应用
 */
@SpringBootApplication
public class RedPacketLoadTestApplication {

    public static void main(String[] args) {
        SpringApplication.run(RedPacketLoadTestApplication.class, args);
    }
}
