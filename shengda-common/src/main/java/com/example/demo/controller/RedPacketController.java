package com.example.demo.controller;

import com.example.demo.model.User;
import com.example.demo.service.RedPacketMySqlService;
import com.example.demo.service.RedPacketService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

/**
 * 红包 Controller
 */
@RestController
@RequestMapping("/api/redpacket")
public class RedPacketController {

    @Autowired
    private RedPacketService redPacketService;

    @Autowired
    private RedPacketMySqlService redPacketMySqlService;

    /**
     * 发布红包（仅管理员）
     * @param storageType 存储类型：redis 或 mysql，默认redis
     */
    @PostMapping("/publish")
    public Map<String, Object> publishRedPacket(
            Authentication authentication,
            @RequestParam String packetNo,
            @RequestParam BigDecimal totalAmount,
            @RequestParam Integer totalCount,
            @RequestParam(required = false, defaultValue = "redis") String storageType) {
        
        // 从登录凭证中取真实用户名，不能用 authentication.getName()：
        // 此处 principal 是 User 对象且未实现 UserDetails/Principal，
        // getName() 会返回 User.toString() 整串内容，远超 publisher 字段长度导致 data too long
        if (authentication == null || !(authentication.getPrincipal() instanceof User)) {
            Map<String, Object> result = new java.util.HashMap<>();
            result.put("success", false);
            result.put("message", "发布失败：登录状态失效，请重新登录");
            return result;
        }
        String userName = ((User) authentication.getPrincipal()).getUserName();

        System.out.println("发布红包 - 用户名: " + userName);
        
        if ("mysql".equalsIgnoreCase(storageType)) {
            return redPacketMySqlService.publishRedPacket(userName, packetNo, totalAmount, totalCount);
        } else {
            return redPacketService.publishRedPacket(userName, packetNo, totalAmount, totalCount);
        }
    }

    /**
     * 抢红包（无需鉴权）
     * @param storageType 存储类型：redis 或 mysql，默认redis
     */
    @PostMapping("/grab")
    public Map<String, Object> grabRedPacket(
            @RequestParam String packetNo,
            @RequestParam String userName,
            @RequestParam(required = false, defaultValue = "redis") String storageType) {
        
        if ("mysql".equalsIgnoreCase(storageType)) {
            return redPacketMySqlService.grabRedPacket(packetNo, userName);
        } else {
            return redPacketService.grabRedPacket(packetNo, userName);
        }
    }

    /**
     * 查询红包详情
     * @param storageType 存储类型：redis 或 mysql，默认redis
     */
    @GetMapping("/detail/{packetNo}")
    public Map<String, Object> getRedPacketDetail(
            @PathVariable String packetNo,
            @RequestParam(required = false, defaultValue = "redis") String storageType) {
        
        if ("mysql".equalsIgnoreCase(storageType)) {
            return redPacketMySqlService.getRedPacketDetail(packetNo);
        } else {
            return redPacketService.getRedPacketDetail(packetNo);
        }
    }

    /**
     * 查询用户的抢红包记录
     * 注意：无论使用哪种存储类型，记录都统一从 MySQL 查询
     */
    @GetMapping("/records")
    public Object getUserGrabRecords(@RequestParam String userName) {
        // 统一从 MySQL 查询抢红包记录
        return redPacketMySqlService.getUserGrabRecords(userName);
    }
}
