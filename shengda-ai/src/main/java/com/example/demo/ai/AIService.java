package com.example.demo.ai;

/**
 * AI服务统一接口
 * 支持多种AI提供商(豆包、DeepSeek等)的动态切换
 */
public interface AIService {

    /**
     * 通用对话接口
     *
     * @param prompt 提示词
     * @return AI回复内容
     */
    String chat(String prompt);

    /**
     * 检查内容是否与学习相关
     *
     * @param content 帖子内容
     * @return true-与学习相关，false-与学习无关
     */
    boolean isLearningRelated(String content);

    /**
     * 从文本中提取关键词（用于智能搜索）
     *
     * @param text 输入文本
     * @return 关键词列表，逗号分隔
     */
    String extractKeywords(String text);

    /**
     * 获取服务商名称
     *
     * @return 服务商名称(如"豆包"、"DeepSeek")
     */
    String getProviderName();
}
