package com.example.demo.controller;

import com.example.demo.model.RedPacketRecord;
import com.example.demo.model.User;
import com.example.demo.service.RedPacketMySqlService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 红包 Controller（纯MySQL版本，用于性能对比）
 */
@RestController
@RequestMapping("/api/mysql/redpacket")
public class RedPacketMySqlController {

    @Autowired
    private RedPacketMySqlService redPacketMySqlService;

    /**
     * 发布红包（仅管理员）
     */
    @PostMapping("/publish")
    public Map<String, Object> publishRedPacket(
            Authentication authentication,
            @RequestParam String packetNo,
            @RequestParam BigDecimal totalAmount,
            @RequestParam Integer totalCount) {
        
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
        return redPacketMySqlService.publishRedPacket(userName, packetNo, totalAmount, totalCount);
    }

    /**
     * 抢红包（无需鉴权）
     */
    @PostMapping("/grab")
    public Map<String, Object> grabRedPacket(
            @RequestParam String packetNo,
            @RequestParam String userName) {
        return redPacketMySqlService.grabRedPacket(packetNo, userName);
    }

    /**
     * 查询红包详情
     */
    @GetMapping("/detail/{packetNo}")
    public Map<String, Object> getRedPacketDetail(@PathVariable String packetNo) {
        return redPacketMySqlService.getRedPacketDetail(packetNo);
    }

    /**
     * 查询用户的抢红包记录
     */
    @GetMapping("/records")
    public List<RedPacketRecord> getUserGrabRecords(@RequestParam String userName) {
        return redPacketMySqlService.getUserGrabRecords(userName);
    }
}
