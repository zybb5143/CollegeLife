package com.shengda.service;

import com.shengda.config.LoadTestConfig;
import com.shengda.model.LoadTestReport;
import com.shengda.model.LoadTestResult;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * 抢红包压测核心服务
 * <p>
 * 主机固定为 localhost，一次只压测一个端口对应的目标服务，
 * 同时测试 Redis 版本(/api/redpacket/grab) 与 MySQL 版本(/api/mysql/redpacket/grab) 接口，
 * 通过线程池 + CountDownLatch 模拟高并发抢红包场景。
 * <p>
 * 该服务同时被 Web 接口（LoadTestController）和命令行 Runner 复用。
 */
@Service
public class LoadTestService {

    private static final Logger log = LoggerFactory.getLogger(LoadTestService.class);

    /** 主机写死为 localhost */
    private static final String HOST = "localhost";

    @Autowired
    private LoadTestConfig config;

    /**
     * 执行一次完整压测（Redis + MySQL）
     *
     * @param params 本次压测参数（端口、并发数、请求数等）
     * @return 可序列化的压测报告
     */
    public LoadTestReport runTest(LoadTestParams params) {
        String baseUrl = "http://" + HOST + ":" + params.getPort();
        RestTemplate restTemplate = buildRestTemplate();

        log.info("====================================================");
        log.info("        抢红包压测开始");
        log.info("====================================================");
        log.info("目标服务地址: {}", baseUrl);
        log.info("红包编号: {}", params.getPacketNo());
        log.info("并发线程数: {}", params.getConcurrency());
        log.info("总请求数: {}", params.getTotalRequests());
        log.info("Redis版本压测: {}", params.isEnableRedis() ? "启用" : "禁用");
        log.info("MySQL版本压测: {}", params.isEnableMysql() ? "启用" : "禁用");
        log.info("====================================================");

        LoadTestResult redisResult = null;
        LoadTestResult mysqlResult = null;

        // Redis 版本压测
        if (params.isEnableRedis()) {
            String redisUrl = baseUrl + config.getRedisGrabPath();
            log.info(">>> 开始 Redis 版本压测，目标URL: {}", redisUrl);
            warmup(restTemplate, redisUrl, "redis", params);
            redisResult = executeLoadTest(restTemplate, "Redis", redisUrl, "redis", params);
            log.info("\n{}", redisResult.getReport());
        }

        // 两个版本之间间隔2秒，避免资源竞争
        if (params.isEnableRedis() && params.isEnableMysql()) {
            log.info(">>> 等待2秒后开始MySQL版本压测...");
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        // MySQL 版本压测
        if (params.isEnableMysql()) {
            String mysqlUrl = baseUrl + config.getMysqlGrabPath();
            log.info(">>> 开始 MySQL 版本压测，目标URL: {}", mysqlUrl);
            warmup(restTemplate, mysqlUrl, "mysql", params);
            mysqlResult = executeLoadTest(restTemplate, "MySQL", mysqlUrl, "mysql", params);
            log.info("\n{}", mysqlResult.getReport());
        }

        // 组装报告
        LoadTestReport report = new LoadTestReport();
        report.setBaseUrl(baseUrl);
        report.setPacketNo(params.getPacketNo());
        if (redisResult != null) {
            report.setRedis(LoadTestReport.SingleReport.from(redisResult));
        }
        if (mysqlResult != null) {
            report.setMysql(LoadTestReport.SingleReport.from(mysqlResult));
        }
        if (redisResult != null && mysqlResult != null
                && redisResult.getTotalTimeMs() > 0 && mysqlResult.getTotalTimeMs() > 0) {
            double ratio = (double) mysqlResult.getTotalTimeMs() / redisResult.getTotalTimeMs();
            report.setSpeedRatio(Math.round(ratio * 100.0) / 100.0);
        }

        log.info("====================================================");
        log.info("        压测完成！");
        log.info("====================================================");
        return report;
    }

    /**
     * 初始化 RestTemplate，设置超时参数
     */
    private RestTemplate buildRestTemplate() {
        var factory = new org.springframework.http.client.SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(config.getConnectTimeout());
        factory.setReadTimeout(config.getReadTimeout());
        return new RestTemplate(factory);
    }

    /**
     * 预热请求，让目标服务准备好
     */
    private void warmup(RestTemplate restTemplate, String url, String storageType, LoadTestParams params) {
        int warmupRequests = config.getWarmupRequests();
        log.info("    预热中（{} 次请求）...", warmupRequests);
        for (int i = 0; i < warmupRequests; i++) {
            try {
                sendGrabRequest(restTemplate, url, "warmupUser" + i, storageType, params);
            } catch (Exception e) {
                // 预热失败不影响正式测试
            }
        }
        log.info("    预热完成");
    }

    /**
     * 执行压测核心逻辑：线程池 + CountDownLatch 实现高并发
     */
    private LoadTestResult executeLoadTest(RestTemplate restTemplate, String version, String url,
                                           String storageType, LoadTestParams params) {
        int concurrency = params.getConcurrency();
        int totalRequests = params.getTotalRequests();
        LoadTestResult result = new LoadTestResult(version, totalRequests, concurrency);

        ExecutorService executor = Executors.newFixedThreadPool(concurrency);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch endLatch = new CountDownLatch(totalRequests);

        log.info("    正在执行压测...");
        long startTime = System.currentTimeMillis();

        for (int i = 0; i < totalRequests; i++) {
            final int taskIndex = i;
            executor.submit(() -> {
                try {
                    startLatch.await();
                    String userName = params.getUserNamePrefix() + "_" + version.toLowerCase() + "_" + taskIndex;
                    long reqStart = System.currentTimeMillis();
                    boolean success = sendGrabRequest(restTemplate, url, userName, storageType, params);
                    long reqEnd = System.currentTimeMillis();
                    result.recordResponseTime(reqEnd - reqStart);
                    if (success) {
                        result.getSuccessCount().incrementAndGet();
                    } else {
                        result.getFailCount().incrementAndGet();
                    }
                } catch (Exception e) {
                    result.getErrorCount().incrementAndGet();
                } finally {
                    endLatch.countDown();
                }
            });
        }

        // 所有线程同时开始
        startLatch.countDown();

        try {
            endLatch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        result.setTotalTimeMs(System.currentTimeMillis() - startTime);

        executor.shutdown();
        try {
            if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
        }
        return result;
    }

    /**
     * 发送抢红包HTTP请求
     *
     * @return true=抢到红包，false=未抢到（业务失败）
     */
    @SuppressWarnings("unchecked")
    private boolean sendGrabRequest(RestTemplate restTemplate, String url, String userName,
                                    String storageType, LoadTestParams params) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(url)
                .queryParam("packetNo", params.getPacketNo())
                .queryParam("userName", userName);

        // Redis版本通过统一入口需要传 storageType 参数；MySQL版本使用独立路径，不需要
        if ("redis".equals(storageType)) {
            builder.queryParam("storageType", "redis");
        }

        String fullUrl = builder.toUriString();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        HttpEntity<String> entity = new HttpEntity<>(null, headers);

        ResponseEntity<Map> response = restTemplate.exchange(fullUrl, HttpMethod.POST, entity, Map.class);
        if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
            Map<String, Object> body = response.getBody();
            return Boolean.TRUE.equals(body.get("success"));
        }
        return false;
    }

    /**
     * 一次压测的入参
     */
    @Data
    public static class LoadTestParams {
        /** 目标服务端口（主机固定 localhost） */
        private int port = 10010;
        /** 红包编号 */
        private String packetNo = "TEST_PACKET_001";
        /** 并发线程数 */
        private int concurrency = 100;
        /** 总请求数 */
        private int totalRequests = 500;
        /** 用户名前缀 */
        private String userNamePrefix = "testUser";
        /** 是否压测 Redis 版本 */
        private boolean enableRedis = true;
        /** 是否压测 MySQL 版本 */
        private boolean enableMysql = true;
    }
}
