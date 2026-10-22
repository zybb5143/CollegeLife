package com.shengda.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 压测配置类
 * 通过 application.properties 配置压测参数
 */
@Data
@Component
@ConfigurationProperties(prefix = "loadtest")
public class LoadTestConfig {

    /**
     * 目标服务基础URL
     */
    private String baseUrl = "http://localhost:10010";

    /**
     * Redis版本抢红包接口路径（写死）
     */
    private String redisGrabPath = "/api/redpacket/grab";

    /**
     * MySQL版本抢红包接口路径（写死）
     */
    private String mysqlGrabPath = "/api/mysql/redpacket/grab";

    /**
     * 红包编号
     */
    private String packetNo = "TEST_PACKET_001";

    /**
     * 并发线程数（模拟多少用户同时抢）
     */
    private int concurrency = 100;

    /**
     * 总请求数（抢红包总次数）
     */
    private int totalRequests = 500;

    /**
     * 用户名前缀（会拼接序号生成不同用户）
     */
    private String userNamePrefix = "testUser";

    /**
     * 是否启用Redis版本压测
     */
    private boolean enableRedis = true;

    /**
     * 是否启用MySQL版本压测
     */
    private boolean enableMysql = true;

    /**
     * 压测前预热请求数
     */
    private int warmupRequests = 5;

    /**
     * 连接超时时间（毫秒）
     */
    private int connectTimeout = 5000;

    /**
     * 读取超时时间（毫秒）
     */
    private int readTimeout = 10000;

    /**
     * 是否在应用启动时自动执行一次压测。
     * 默认 false：改由网页手动触发，一次只测一个端口。
     */
    private boolean autoRun = false;
}
