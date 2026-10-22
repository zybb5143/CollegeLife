package com.example.demo.ai;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * AI服务工厂
 * 默认使用 deepseek，可调用 switchProvider 切换 AI 服务商（仅内存生效）
 */
@Service
public class AIServiceFactory {

    @Autowired
    private DoubaoAIService doubaoAIService;

    @Autowired
    private DeepSeekAIService deepSeekAIService;

    private volatile String currentProvider = "deepseek";

    /**
     * 切换当前 AI 服务商（仅内存生效，重启后恢复默认 deepseek）
     */
    public void switchProvider(String provider) {
        if (provider == null || provider.trim().isEmpty()) {
            throw new IllegalArgumentException("请指定 AI 服务商 (doubao 或 deepseek)");
        }
        if (!"doubao".equalsIgnoreCase(provider) && !"deepseek".equalsIgnoreCase(provider)) {
            throw new IllegalArgumentException("不支持的AI服务商: " + provider);
        }
        this.currentProvider = provider.toLowerCase();
        System.out.println("AI 服务商切换为: " + currentProvider);
    }

    public AIService getCurrentService() {
        return getService(currentProvider);
    }

    public AIService getService(String provider) {
        if ("doubao".equalsIgnoreCase(provider)) {
            return doubaoAIService;
        } else if ("deepseek".equalsIgnoreCase(provider)) {
            return deepSeekAIService;
        }
        throw new IllegalArgumentException("不支持的AI服务商: " + provider);
    }

    public String getCurrentProvider() {
        return currentProvider;
    }
}
