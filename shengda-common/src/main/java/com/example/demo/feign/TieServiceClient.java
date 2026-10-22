package com.example.demo.feign;

import com.example.demo.model.Tie;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

/**
 * shengda-tie-service（帖子微服务）的 Feign 客户端。
 * 服务名与 provider 的 spring.application.name 一致，实例地址由 Nacos + LoadBalancer 解析。
 */
@FeignClient(name = "shengda-tie-service", path = "/api/tie")
public interface TieServiceClient {

    @GetMapping("/all")
    List<Tie> getAllTies();

    @GetMapping("/latest")
    List<Tie> getLatest10Ties();

    @GetMapping("/list")
    List<Tie> getTiesByUserName(@RequestParam("userName") String userName);

    @GetMapping("/detail")
    Tie getTieById(@RequestParam("tieId") Integer tieId);

    @GetMapping("/latest-id")
    Integer getLatestTieId(@RequestParam("userName") String userName);

    @PostMapping("/create")
    Map<String, Object> createTie(@RequestBody Map<String, Object> body);

    @PostMapping("/edit")
    Map<String, Object> editTie(@RequestBody Map<String, Object> body);

    @PostMapping("/update-image")
    Map<String, Object> updateTieImage(@RequestBody Map<String, Object> body);

    @PostMapping("/pending")
    Map<String, Object> markTieAsPending(@RequestBody Map<String, Object> body);

    @DeleteMapping("/delete")
    Map<String, Object> deleteTie(@RequestParam("tieId") Integer tieId);
}
