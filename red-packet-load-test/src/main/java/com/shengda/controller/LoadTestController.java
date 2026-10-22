package com.shengda.controller;

import com.shengda.model.LoadTestReport;
import com.shengda.service.LoadTestService;
import com.shengda.service.LoadTestService.LoadTestParams;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 压测 Web 接口
 * <p>
 * 网页只需提交端口号等参数，主机固定为 localhost，一次只压测一个目标端口。
 */
@RestController
@RequestMapping("/api/loadtest")
public class LoadTestController {

    @Autowired
    private LoadTestService loadTestService;

    /** 防止同一时间重复触发压测 */
    private final AtomicBoolean running = new AtomicBoolean(false);

    /**
     * 执行一次压测，返回 Redis 与 MySQL 的对比报告
     */
    @PostMapping("/run")
    public ResponseEntity<?> run(@RequestBody LoadTestParams params) {
        // 参数基础校验
        if (params.getPort() <= 0 || params.getPort() > 65535) {
            return ResponseEntity.badRequest().body(Map.of("error", "端口号必须在 1 ~ 65535 之间"));
        }
        if (params.getConcurrency() <= 0 || params.getTotalRequests() <= 0) {
            return ResponseEntity.badRequest().body(Map.of("error", "并发数和总请求数必须大于 0"));
        }
        if (!params.isEnableRedis() && !params.isEnableMysql()) {
            return ResponseEntity.badRequest().body(Map.of("error", "至少需要选择一个版本进行压测"));
        }

        // 同一时间只允许一个压测任务
        if (!running.compareAndSet(false, true)) {
            return ResponseEntity.status(409).body(Map.of("error", "已有压测任务正在执行，请稍后再试"));
        }
        try {
            LoadTestReport report = loadTestService.runTest(params);
            return ResponseEntity.ok(report);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", "压测执行失败: " + e.getMessage()));
        } finally {
            running.set(false);
        }
    }
}
