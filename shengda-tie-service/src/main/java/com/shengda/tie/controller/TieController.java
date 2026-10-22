package com.shengda.tie.controller;

import com.shengda.tie.model.Tie;
import com.shengda.tie.service.TieService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 帖子服务对外 REST 接口（实验级）。
 * 该 provider 不接入 Security，调用方需显式传 userName；
 * 仅"迁移 + 暴露接口"，不涉及跨服务事务/一致性。
 */
@RestController
@RequestMapping("/api/tie")
public class TieController {

    @Autowired
    private TieService tieService;

    /** 服务健康/联通性检查 */
    @GetMapping("/ping")
    public Map<String, Object> ping() {
        Map<String, Object> r = new HashMap<>();
        r.put("service", "shengda-tie-service");
        r.put("status", "UP");
        return r;
    }

    /** 查询所有帖子 */
    @GetMapping("/all")
    public List<Tie> getAllTies() {
        return tieService.getAllTies();
    }

    /** 获取最新的 10 个帖子 */
    @GetMapping("/latest")
    public List<Tie> getLatest10Ties() {
        return tieService.getLatest10Ties();
    }

    /** 根据用户名查询帖子 */
    @GetMapping("/list")
    public List<Tie> getUserTies(@RequestParam("userName") String userName) {
        return tieService.getTiesByUserName(userName);
    }

    /** 获取帖子详情 */
    @GetMapping("/detail")
    public Tie getTieDetail(@RequestParam("tieId") Integer tieId) {
        return tieService.getTieById(tieId);
    }

    /** 获取用户最新创建的帖子 ID */
    @GetMapping("/latest-id")
    public Integer getLatestTieId(@RequestParam("userName") String userName) {
        return tieService.getLatestTieId(userName);
    }

    /** 创建帖子 */
    @PostMapping("/create")
    public Map<String, Object> createTie(@RequestBody Map<String, Object> body) {
        boolean success = tieService.createTie(str(body, "tieTitle"), str(body, "tieContent"), str(body, "userName"),
                str(body, "tieImage", ""), str(body, "tieType", "academic"), integer(body, "productId"));
        Map<String, Object> r = new HashMap<>();
        r.put("success", success);
        return r;
    }

    /** 更新帖子标题与内容 */
    @PostMapping("/edit")
    public Map<String, Object> editTie(@RequestBody Map<String, Object> body) {
        boolean success = tieService.updateTie(integer(body, "tieId"), str(body, "tieTitle"), str(body, "tieContent"));
        Map<String, Object> r = new HashMap<>();
        r.put("success", success);
        return r;
    }

    /** 更新帖子图片 */
    @PostMapping("/update-image")
    public Map<String, Object> updateTieImage(@RequestBody Map<String, Object> body) {
        boolean success = tieService.updateTieImage(integer(body, "tieId"), str(body, "tieImage"));
        Map<String, Object> r = new HashMap<>();
        r.put("success", success);
        return r;
    }

    /** 标记帖子为待审核状态 */
    @PostMapping("/pending")
    public Map<String, Object> markTieAsPending(@RequestBody Map<String, Object> body) {
        boolean success = tieService.markTieAsPending(integer(body, "tieId"));
        Map<String, Object> r = new HashMap<>();
        r.put("success", success);
        return r;
    }

    /** 删除帖子 */
    @DeleteMapping("/delete")
    public Map<String, Object> deleteTie(@RequestParam("tieId") Integer tieId) {
        boolean success = tieService.deleteTie(tieId);
        Map<String, Object> r = new HashMap<>();
        r.put("success", success);
        return r;
    }

    private static String str(Map<String, Object> body, String key) {
        return str(body, key, null);
    }

    private static String str(Map<String, Object> body, String key, String defaultValue) {
        Object value = body.get(key);
        return value == null ? defaultValue : value.toString();
    }

    private static Integer integer(Map<String, Object> body, String key) {
        Object value = body.get(key);
        if (value == null) {
            return null;
        }
        return value instanceof Number ? ((Number) value).intValue() : Integer.valueOf(value.toString());
    }
}
