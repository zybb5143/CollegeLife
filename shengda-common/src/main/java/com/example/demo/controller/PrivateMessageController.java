package com.example.demo.controller;

import com.example.demo.model.PrivateMessage;
import com.example.demo.service.PrivateMessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 私信控制器
 */
@RestController
@RequestMapping("/api/message")
public class PrivateMessageController {
    
    @Autowired
    private PrivateMessageService privateMessageService;
    
    /**
     * 发送消息
     */
    @PostMapping("/send")
    public Map<String, Object> sendMessage(
            @RequestParam("receiverName") String receiverName,
            @RequestParam("messageContent") String messageContent,
            @RequestParam(value = "messageType", defaultValue = "text") String messageType,
            @RequestParam(value = "productId", required = false) Integer productId) {
        Map<String, Object> result = new HashMap<>();
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !(authentication.getPrincipal() instanceof com.example.demo.model.User)) {
                result.put("success", false);
                result.put("message", "请先登录");
                return result;
            }
            
            com.example.demo.model.User currentUser = (com.example.demo.model.User) authentication.getPrincipal();
            
            PrivateMessage message = new PrivateMessage();
            message.setSenderName(currentUser.getUserName());
            message.setReceiverName(receiverName);
            message.setMessageContent(messageContent);
            message.setMessageType(messageType);
            message.setProductId(productId);
            
            boolean success = privateMessageService.sendMessage(message);
            if (success) {
                result.put("success", true);
                result.put("message", "发送成功");
            } else {
                result.put("success", false);
                result.put("message", "发送失败");
            }
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "发送失败：" + e.getMessage());
        }
        return result;
    }
    
    /**
     * 查询与某人的聊天记录
     */
    @GetMapping("/conversation/{targetUser}")
    public Map<String, Object> getConversation(@PathVariable String targetUser) {
        Map<String, Object> result = new HashMap<>();
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !(authentication.getPrincipal() instanceof com.example.demo.model.User)) {
                result.put("success", false);
                result.put("message", "请先登录");
                return result;
            }
            
            com.example.demo.model.User currentUser = (com.example.demo.model.User) authentication.getPrincipal();
            
            List<PrivateMessage> messages = privateMessageService.getConversation(currentUser.getUserName(), targetUser);
            
            // 标记为已读
            privateMessageService.markAllAsRead(targetUser, currentUser.getUserName());
            
            result.put("success", true);
            result.put("data", messages);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "查询失败：" + e.getMessage());
        }
        return result;
    }
    
    /**
     * 查询我的对话列表
     */
    @GetMapping("/conversations")
    public Map<String, Object> getUserConversations() {
        Map<String, Object> result = new HashMap<>();
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !(authentication.getPrincipal() instanceof com.example.demo.model.User)) {
                result.put("success", false);
                result.put("message", "请先登录");
                return result;
            }
            
            com.example.demo.model.User currentUser = (com.example.demo.model.User) authentication.getPrincipal();
            
            List<PrivateMessage> conversations = privateMessageService.getUserConversations(currentUser.getUserName());
            result.put("success", true);
            result.put("data", conversations);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "查询失败：" + e.getMessage());
        }
        return result;
    }
    
    /**
     * 获取未读消息数量
     */
    @GetMapping("/unread/count")
    public Map<String, Object> getUnreadCount() {
        Map<String, Object> result = new HashMap<>();
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !(authentication.getPrincipal() instanceof com.example.demo.model.User)) {
                result.put("success", false);
                result.put("message", "请先登录");
                return result;
            }
            
            com.example.demo.model.User currentUser = (com.example.demo.model.User) authentication.getPrincipal();
            
            int count = privateMessageService.getUnreadCount(currentUser.getUserName());
            result.put("success", true);
            result.put("count", count);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "查询失败：" + e.getMessage());
        }
        return result;
    }
}
