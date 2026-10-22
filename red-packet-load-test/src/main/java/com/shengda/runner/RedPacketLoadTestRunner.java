package com.shengda.runner;

import com.shengda.config.LoadTestConfig;
import com.shengda.service.LoadTestService;
import com.shengda.service.LoadTestService.LoadTestParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * 抢红包压测命令行执行器（可选）
 * <p>
 * 默认不执行：仅当 loadtest.auto-run=true 时，应用启动后按 application.properties 的配置自动压测一次。
 * 常规使用请通过网页手动触发（见 static/index.html 与 LoadTestController）。
 */
@Component
public class RedPacketLoadTestRunner implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(RedPacketLoadTestRunner.class);

    @Autowired
    private LoadTestConfig config;

    @Autowired
    private LoadTestService loadTestService;

    @Override
    public void run(String... args) {
        if (!config.isAutoRun()) {
            log.info("已跳过启动自动压测（loadtest.auto-run=false）。请打开网页手动触发压测。");
            return;
        }

        // 从配置的 baseUrl 中解析端口，主机固定为 localhost
        int port = parsePort(config.getBaseUrl(), 10010);

        LoadTestParams params = new LoadTestParams();
        params.setPort(port);
        params.setPacketNo(config.getPacketNo());
        params.setConcurrency(config.getConcurrency());
        params.setTotalRequests(config.getTotalRequests());
        params.setUserNamePrefix(config.getUserNamePrefix());
        params.setEnableRedis(config.isEnableRedis());
        params.setEnableMysql(config.isEnableMysql());

        loadTestService.runTest(params);
    }

    /**
     * 从形如 http://localhost:10010 的地址中解析端口，解析失败返回默认值
     */
    private int parsePort(String baseUrl, int defaultPort) {
        try {
            String afterColon = baseUrl.substring(baseUrl.lastIndexOf(':') + 1);
            return Integer.parseInt(afterColon.replaceAll("[^0-9]", ""));
        } catch (Exception e) {
            return defaultPort;
        }
    }
}
