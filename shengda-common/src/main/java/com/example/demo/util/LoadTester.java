package com.example.demo.util;

import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 简易HTTP压测工具
 */
public class LoadTester {

    private final String url;
    private final int totalRequests;
    private final int concurrentUsers;
    private final HttpClient httpClient;

    public LoadTester(String url, int totalRequests, int concurrentUsers) {
        this.url = url;
        this.totalRequests = totalRequests;
        this.concurrentUsers = concurrentUsers;
        
        // 创建高性能HTTP客户端
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    /**
     * 执行压测
     */
    public TestResult run() throws InterruptedException {
        System.out.println("开始压测...");
        System.out.println("URL: " + url);
        System.out.println("总请求数: " + totalRequests);
        System.out.println("并发用户数: " + concurrentUsers);
        System.out.println("-----------------------------------");

        ExecutorService executor = Executors.newFixedThreadPool(concurrentUsers);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);
        AtomicLong totalResponseTime = new AtomicLong(0);
        CountDownLatch latch = new CountDownLatch(totalRequests);

        long startTime = System.currentTimeMillis();

        // 提交任务
        for (int i = 0; i < totalRequests; i++) {
            executor.submit(() -> {
                try {
                    long requestStart = System.currentTimeMillis();
                    
                    HttpRequest request = HttpRequest.newBuilder()
                            .uri(java.net.URI.create(url))
                            .GET()
                            .timeout(Duration.ofSeconds(30))
                            .build();

                    HttpResponse<String> response = httpClient.send(request, 
                            HttpResponse.BodyHandlers.ofString());
                    
                    long responseTime = System.currentTimeMillis() - requestStart;
                    totalResponseTime.addAndGet(responseTime);

                    if (response.statusCode() == 200) {
                        successCount.incrementAndGet();
                    } else {
                        failCount.incrementAndGet();
                        System.err.println("请求失败，状态码: " + response.statusCode());
                    }
                } catch (Exception e) {
                    failCount.incrementAndGet();
                    System.err.println("请求异常: " + e.getMessage());
                } finally {
                    latch.countDown();
                }
            });
        }

        // 等待所有请求完成
        latch.await();
        long endTime = System.currentTimeMillis();
        executor.shutdown();

        // 计算结果
        long totalTime = endTime - startTime;
        int actualRequests = successCount.get() + failCount.get();
        double avgResponseTime = actualRequests > 0 ? (double) totalResponseTime.get() / actualRequests : 0;
        double qps = totalTime > 0 ? (actualRequests * 1000.0) / totalTime : 0;

        TestResult result = new TestResult();
        result.setTotalRequests(actualRequests);
        result.setSuccessCount(successCount.get());
        result.setFailCount(failCount.get());
        result.setTotalTime(totalTime);
        result.setAvgResponseTime(avgResponseTime);
        result.setQps(qps);

        return result;
    }

    /**
     * 压测结果
     */
    public static class TestResult {
        private int totalRequests;
        private int successCount;
        private int failCount;
        private long totalTime;
        private double avgResponseTime;
        private double qps;

        public void printReport() {
            System.out.println("-----------------------------------");
            System.out.println("压测报告:");
            System.out.println("总请求数: " + totalRequests);
            System.out.println("成功数: " + successCount);
            System.out.println("失败数: " + failCount);
            System.out.println("总耗时: " + totalTime + " ms");
            System.out.println("平均响应时间: " + String.format("%.2f", avgResponseTime) + " ms");
            System.out.println("QPS: " + String.format("%.2f", qps));
            System.out.println("-----------------------------------");
        }

        // Getters and Setters
        public int getTotalRequests() { return totalRequests; }
        public void setTotalRequests(int totalRequests) { this.totalRequests = totalRequests; }
        public int getSuccessCount() { return successCount; }
        public void setSuccessCount(int successCount) { this.successCount = successCount; }
        public int getFailCount() { return failCount; }
        public void setFailCount(int failCount) { this.failCount = failCount; }
        public long getTotalTime() { return totalTime; }
        public void setTotalTime(long totalTime) { this.totalTime = totalTime; }
        public double getAvgResponseTime() { return avgResponseTime; }
        public void setAvgResponseTime(double avgResponseTime) { this.avgResponseTime = avgResponseTime; }
        public double getQps() { return qps; }
        public void setQps(double qps) { this.qps = qps; }
    }
}
