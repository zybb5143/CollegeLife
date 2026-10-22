package com.example.demo.controller;

import com.example.demo.ai.AIService;
import com.example.demo.ai.AIServiceFactory;
import com.example.demo.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * AI 对话 Controller
 * 提供用户与大模型的独立对话功能
 */
@RestController
@RequestMapping("/api/ai-chat")
public class AIChatController {

    @Autowired
    private AIServiceFactory aiServiceFactory;

    /**
     * 与 AI 对话
     */
    @PostMapping("/chat")
    public Map<String, Object> chat(@RequestParam("message") String message) {
        Map<String, Object> result = new HashMap<>();

        try {
            // 获取当前登录用户
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !(authentication.getPrincipal() instanceof User)) {
                result.put("success", false);
                result.put("message", "请先登录");
                return result;
            }

            String userName = ((User) authentication.getPrincipal()).getUserName();

            // 调用 AI API
            String reply = aiServiceFactory.getCurrentService().chat(message);

            result.put("success", true);
            result.put("message", reply);
            result.put("userName", userName);

        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "对话失败：" + e.getMessage());
        }

        return result;
    }

    /**
     * 获取当前 AI 服务商信息
     */
    @GetMapping("/provider")
    public Map<String, String> getCurrentProvider() {
        AIService currentService = aiServiceFactory.getCurrentService();
        return Map.of(
                "provider", aiServiceFactory.getCurrentProvider(),
                "name", currentService.getProviderName()
        );
    }

    /**
     * 管理员切换AI服务商（仅内存生效，重启后恢复默认 deepseek）
     */
    @PostMapping("/switch-provider")
    @PreAuthorize("@securityCheck.isAdmin()")
    public Map<String, Object> switchProvider(@RequestParam("provider") String provider) {
        Map<String, Object> result = new HashMap<>();

        try {
            aiServiceFactory.switchProvider(provider);
            result.put("success", true);
            result.put("message", "AI 服务商已切换为: " + aiServiceFactory.getCurrentProvider());
            result.put("provider", aiServiceFactory.getCurrentProvider());
        } catch (IllegalArgumentException e) {
            result.put("success", false);
            result.put("message", e.getMessage());
        }

        return result;
    }

}
