package com.shengda.model;

import lombok.Data;

/**
 * 压测结果报告（用于返回给网页的 JSON）
 * 包含单个版本的统计明细，以及 Redis / MySQL 两个版本的对比
 */
@Data
public class LoadTestReport {

    /**
     * 本次压测的目标基础地址，例如 http://localhost:10010
     */
    private String baseUrl;

    /**
     * 本次压测使用的红包编号
     */
    private String packetNo;

    /**
     * Redis 版本压测明细
     */
    private SingleReport redis;

    /**
     * MySQL 版本压测明细
     */
    private SingleReport mysql;

    /**
     * Redis 相对 MySQL 的加速倍数（MySQL总耗时 / Redis总耗时），两者都执行时才有值
     */
    private Double speedRatio;

    /**
     * 单个版本的压测统计明细
     */
    @Data
    public static class SingleReport {
        /** 版本名称（Redis / MySQL） */
        private String version;
        /** 并发线程数 */
        private int concurrency;
        /** 总请求数 */
        private int totalRequests;
        /** 成功次数（抢到红包） */
        private int successCount;
        /** 失败次数（未抢到，业务失败） */
        private int failCount;
        /** 异常次数（网络错误、超时等） */
        private int errorCount;
        /** 成功率（百分比） */
        private double successRate;
        /** 总耗时（毫秒） */
        private long totalTimeMs;
        /** TPS（每秒事务数） */
        private double tps;
        /** 平均响应时间（毫秒） */
        private double avgResponseTime;
        /** 最小响应时间（毫秒），无数据时为 -1 */
        private long minResponseTime;
        /** 最大响应时间（毫秒） */
        private long maxResponseTime;

        /**
         * 由内部统计对象 {@link LoadTestResult} 转换为可序列化的报告明细
         */
        public static SingleReport from(LoadTestResult r) {
            SingleReport s = new SingleReport();
            s.setVersion(r.getVersion());
            s.setConcurrency(r.getConcurrency());
            s.setTotalRequests(r.getTotalRequests());
            s.setSuccessCount(r.getSuccessCount().get());
            s.setFailCount(r.getFailCount().get());
            s.setErrorCount(r.getErrorCount().get());
            s.setSuccessRate(round2(r.getSuccessRate()));
            s.setTotalTimeMs(r.getTotalTimeMs());
            s.setTps(round2(r.getTps()));
            s.setAvgResponseTime(round2(r.getAvgResponseTime()));
            s.setMinResponseTime(r.getMinResponseTime().get() == Long.MAX_VALUE ? -1 : r.getMinResponseTime().get());
            s.setMaxResponseTime(r.getMaxResponseTime().get());
            return s;
        }

        private static double round2(double v) {
            return Math.round(v * 100.0) / 100.0;
        }
    }
}
