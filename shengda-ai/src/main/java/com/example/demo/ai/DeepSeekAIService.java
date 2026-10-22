package com.example.demo.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/**
 * DeepSeek AI 服务实现
 * 基于 OpenAI 兼容 API，与豆包并行可用
 */
@Service
public class DeepSeekAIService implements AIService {

    @Value("${deepseek.api.key}")
    private String apiKey;

    @Value("${deepseek.model:deepseek-chat}")
    private String model;

    private static final String DEEPSEEK_API_URL = "https://api.deepseek.com/chat/completions";

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public DeepSeekAIService() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public String chat(String customPrompt) {
        return chatWithPrompt(customPrompt);
    }

    @Override
    public String getProviderName() {
        return "DeepSeek";
    }

    public String chatWithPrompt(String customPrompt) {
        if (apiKey == null || apiKey.trim().isEmpty()) {
            System.out.println("警告：DeepSeek API Key 未配置，返回默认回复");
            return "AI 服务未配置，请联系管理员";
        }

        for (int attempt = 1; attempt <= 3; attempt++) {
            try {
                String requestBody = objectMapper.writeValueAsString(
                        new RequestBody(model, customPrompt)
                );

                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(DEEPSEEK_API_URL))
                        .timeout(Duration.ofSeconds(60))
                        .header("Content-Type", "application/json")
                        .header("Authorization", "Bearer " + apiKey)
                        .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                        .build();

                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() == 200) {
                    JsonNode jsonResponse = objectMapper.readTree(response.body());
                    JsonNode choicesNode = jsonResponse.path("choices");

                    if (choicesNode.isArray() && choicesNode.size() > 0) {
                        return choicesNode.get(0).path("message").path("content").asText();
                    }
                } else {
                    System.err.println("DeepSeek API 调用失败，状态码：" + response.statusCode());
                    System.err.println("响应内容：" + response.body());
                }

            } catch (Exception e) {
                System.err.println("调用 DeepSeek AI 失败（第 " + attempt + " 次）：" + e.getMessage());
                if (attempt < 3) {
                    System.out.println("等待 2 秒后重试...");
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
        }

        return "AI 服务暂时不可用";
    }

    @Override
    public boolean isLearningRelated(String content) {
        if (apiKey == null || apiKey.trim().isEmpty()) {
            System.out.println("警告：DeepSeek API Key 未配置，默认允许发帖");
            return true;
        }

        String prompt = "请判断以下帖子（标题+正文）是否与学习相关。课程学习、编程语言与技术框架、技能培训、知识分享、学术交流、考试准备等都算学习相关。"
                + "如果是学习相关内容，只回复'是'；如果确实与学习无关，只回复'否'。不要添加任何其他文字。\n\n" + content;

        try {
            String requestBody = objectMapper.writeValueAsString(
                    new RequestBody(model, prompt)
            );

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(DEEPSEEK_API_URL))
                    .timeout(Duration.ofSeconds(30))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + apiKey)
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                JsonNode jsonResponse = objectMapper.readTree(response.body());
                JsonNode choicesNode = jsonResponse.path("choices");

                if (choicesNode.isArray() && choicesNode.size() > 0) {
                    String reply = choicesNode.get(0).path("message").path("content").asText();
                    System.out.println("DeepSeek AI 回复：" + reply);
                    // 只要明确回答"否"才算无关，避免"不是学习相关"这类措辞被 contains("是") 误判
                    return !reply.contains("否");
                }
            } else {
                System.err.println("DeepSeek API 调用失败，状态码：" + response.statusCode());
                System.err.println("响应内容：" + response.body());
            }

        } catch (Exception e) {
            System.err.println("调用 DeepSeek AI 失败：" + e.getMessage());
            e.printStackTrace();
        }

        return true;
    }

    @Override
    public String extractKeywords(String text) {
        if (apiKey == null || apiKey.trim().isEmpty()) {
            System.out.println("警告：DeepSeek API Key 未配置，返回原文本");
            return text;
        }

        String prompt = "请从以下内容中提取3-5个最重要的关键词，用逗号分隔，不要添加任何其他文字。\n\n内容：" + text;

        try {
            String requestBody = objectMapper.writeValueAsString(
                    new RequestBody(model, prompt)
            );

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(DEEPSEEK_API_URL))
                    .timeout(Duration.ofSeconds(30))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + apiKey)
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                JsonNode jsonResponse = objectMapper.readTree(response.body());
                JsonNode choicesNode = jsonResponse.path("choices");

                if (choicesNode.isArray() && choicesNode.size() > 0) {
                    return choicesNode.get(0).path("message").path("content").asText();
                }
            }

        } catch (Exception e) {
            System.err.println("提取关键词失败：" + e.getMessage());
        }

        return text;
    }

    // 内部类，用于构建请求体
    private static class RequestBody {
        public String model;
        public Message[] messages;

        public RequestBody(String model, String prompt) {
            this.model = model;
            this.messages = new Message[]{new Message(prompt)};
        }
    }

    private static class Message {
        public String role = "user";
        public String content;

        public Message(String content) {
            this.content = content;
        }
    }
}
