package com.shengda.model;

import lombok.Data;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 压测结果模型
 * 记录每个版本的压测统计数据
 */
@Data
public class LoadTestResult {

    /**
     * 版本名称（Redis / MySQL）
     */
    private String version;

    /**
     * 总请求数
     */
    private int totalRequests;

    /**
     * 并发线程数
     */
    private int concurrency;

    /**
     * 成功次数（抢到红包）
     */
    private AtomicInteger successCount = new AtomicInteger(0);

    /**
     * 失败次数（未抢到，包括已抢过、已抢完等）
     */
    private AtomicInteger failCount = new AtomicInteger(0);

    /**
     * 异常次数（网络错误、超时等）
     */
    private AtomicInteger errorCount = new AtomicInteger(0);

    /**
     * 总耗时（毫秒）
     */
    private long totalTimeMs;

    /**
     * 最小响应时间（毫秒）
     */
    private AtomicLong minResponseTime = new AtomicLong(Long.MAX_VALUE);

    /**
     * 最大响应时间（毫秒）
     */
    private AtomicLong maxResponseTime = new AtomicLong(0);

    /**
     * 响应时间总和（用于计算平均值）
     */
    private AtomicLong totalResponseTime = new AtomicLong(0);

    public LoadTestResult(String version, int totalRequests, int concurrency) {
        this.version = version;
        this.totalRequests = totalRequests;
        this.concurrency = concurrency;
    }

    /**
     * 记录一次请求的响应时间
     */
    public void recordResponseTime(long responseTimeMs) {
        totalResponseTime.addAndGet(responseTimeMs);
        // 更新最小值
        long currentMin;
        while (responseTimeMs < (currentMin = minResponseTime.get())) {
            if (minResponseTime.compareAndSet(currentMin, responseTimeMs)) {
                break;
            }
        }
        // 更新最大值
        long currentMax;
        while (responseTimeMs > (currentMax = maxResponseTime.get())) {
            if (maxResponseTime.compareAndSet(currentMax, responseTimeMs)) {
                break;
            }
        }
    }

    /**
     * 获取平均响应时间
     */
    public double getAvgResponseTime() {
        int total = successCount.get() + failCount.get() + errorCount.get();
        if (total == 0) return 0;
        return (double) totalResponseTime.get() / total;
    }

    /**
     * 获取TPS（每秒事务数）
     */
    public double getTps() {
        if (totalTimeMs == 0) return 0;
        return (double) totalRequests / (totalTimeMs / 1000.0);
    }

    /**
     * 获取成功率
     */
    public double getSuccessRate() {
        if (totalRequests == 0) return 0;
        return (double) successCount.get() / totalRequests * 100;
    }

    /**
     * 打印压测报告
     */
    public String getReport() {
        StringBuilder sb = new StringBuilder();
        sb.append("\n");
        sb.append("╔══════════════════════════════════════════════════════════╗\n");
        sb.append(String.format("║          【%s版本】抢红包压测报告                      ║\n", version));
        sb.append("╠══════════════════════════════════════════════════════════╣\n");
        sb.append(String.format("║  并发线程数:      %-38d ║\n", concurrency));
        sb.append(String.format("║  总请求数:        %-38d ║\n", totalRequests));
        sb.append(String.format("║  成功次数:        %-38d ║\n", successCount.get()));
        sb.append(String.format("║  失败次数:        %-38d ║\n", failCount.get()));
        sb.append(String.format("║  异常次数:        %-38d ║\n", errorCount.get()));
        sb.append(String.format("║  成功率:          %-38s ║\n", String.format("%.2f%%", getSuccessRate())));
        sb.append("╠══════════════════════════════════════════════════════════╣\n");
        sb.append(String.format("║  总耗时:          %-38s ║\n", String.format("%d ms", totalTimeMs)));
        sb.append(String.format("║  TPS:             %-38s ║\n", String.format("%.2f 次/秒", getTps())));
        sb.append(String.format("║  平均响应时间:    %-38s ║\n", String.format("%.2f ms", getAvgResponseTime())));
        sb.append(String.format("║  最小响应时间:    %-38s ║\n",
                minResponseTime.get() == Long.MAX_VALUE ? "N/A" : minResponseTime.get() + " ms"));
        sb.append(String.format("║  最大响应时间:    %-38s ║\n", maxResponseTime.get() + " ms"));
        sb.append("╚══════════════════════════════════════════════════════════╝\n");
        return sb.toString();
    }
}
