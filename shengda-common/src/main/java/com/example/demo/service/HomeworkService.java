package com.example.demo.service;

import com.example.demo.ai.AIServiceFactory;
import com.example.demo.mapper.HomeworkGradingMapper;
import com.example.demo.mapper.HomeworkQuestionMapper;
import com.example.demo.mapper.HomeworkSubmissionMapper;
import com.example.demo.model.HomeworkGrading;
import com.example.demo.model.HomeworkQuestion;
import com.example.demo.model.HomeworkSubmission;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class HomeworkService {

    @Autowired
    private HomeworkQuestionMapper questionMapper;

    @Autowired
    private HomeworkSubmissionMapper submissionMapper;

    @Autowired
    private HomeworkGradingMapper gradingMapper;

    @Autowired
    private AIServiceFactory aiServiceFactory;

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 管理员发布题目
     */
    public boolean publishQuestion(HomeworkQuestion question) {
        try {
            return questionMapper.insertQuestion(question) > 0;
        } catch (Exception e) {
            System.err.println("发布题目失败：" + e.getMessage());
            return false;
        }
    }

    /**
     * 查询所有题目
     */
    public List<HomeworkQuestion> getAllQuestions() {
        return questionMapper.selectAllQuestions();
    }

    /**
     * 根据ID查询题目
     */
    public HomeworkQuestion getQuestionById(Integer questionId) {
        return questionMapper.selectQuestionById(questionId);
    }

    /**
     * 学生提交答案
     */
    public Integer submitAnswer(HomeworkSubmission submission) {
        try {
            // 设置初始状态为"已提交"
            submission.setStatus("submitted");
            
            int result = submissionMapper.insertSubmission(submission);
            if (result > 0) {
                // 返回submission_id（需要重新查询）
                List<HomeworkSubmission> submissions = submissionMapper.selectByUserName(submission.getUserName());
                return submissions.get(0).getSubmissionId();
            }
            return null;
        } catch (Exception e) {
            System.err.println("提交答案失败：" + e.getMessage());
            return null;
        }
    }

    /**
     * 智能批改作业（异步调用）
     */
    public void gradeHomeworkAsync(Integer submissionId) {
        new Thread(() -> {
            try {
                // 1. 更新状态为"批改中"
                submissionMapper.updateStatus(submissionId, "grading");

                // 2. 获取提交信息
                HomeworkSubmission submission = submissionMapper.selectSubmissionById(submissionId);
                if (submission == null) {
                    System.err.println("提交记录不存在：" + submissionId);
                    return;
                }

                // 3. 获取题目信息
                HomeworkQuestion question = questionMapper.selectQuestionById(submission.getQuestionId());
                if (question == null) {
                    System.err.println("题目不存在：" + submission.getQuestionId());
                    return;
                }

                // 4. 构建批改 Prompt
                String gradingPrompt = buildGradingPrompt(question, submission);

                // 5. 调用 AI 批改
                String aiResponse = aiServiceFactory.getCurrentService().chat(gradingPrompt);

                // 6. 解析 AI 响应
                GradingResult gradingResult = parseGradingResult(aiResponse);
                if (gradingResult == null) {
                    System.err.println("AI 批改结果解析失败");
                    submissionMapper.updateStatus(submissionId, "submitted");
                    return;
                }

                // 7. 保存批改记录
                HomeworkGrading grading = new HomeworkGrading();
                grading.setSubmissionId(submissionId);
                grading.setUserName(submission.getUserName());
                grading.setScore(gradingResult.score);
                grading.setAiFeedback(gradingResult.feedback);
                grading.setCorrectnessAnalysis(gradingResult.correctnessAnalysis);
                grading.setImprovementSuggestions(gradingResult.suggestions);
                grading.setKnowledgePoints(gradingResult.knowledgePoints);

                gradingMapper.insertGrading(grading);

                // 8. 更新提交状态为"已批改"
                submissionMapper.updateStatus(submissionId, "graded");

                System.out.println("✅ 作业批改完成，提交ID：" + submissionId);

            } catch (Exception e) {
                System.err.println("❌ 作业批改失败：" + e.getMessage());
                e.printStackTrace();
                // 批改失败，恢复状态
                try {
                    submissionMapper.updateStatus(submissionId, "submitted");
                } catch (Exception ex) {
                    System.err.println("恢复状态失败：" + ex.getMessage());
                }
            }
        }).start();
    }

    /**
     * 查询用户的提交记录
     */
    public List<HomeworkSubmission> getUserSubmissions(String userName) {
        return submissionMapper.selectByUserName(userName);
    }

    /**
     * 查询批改记录
     */
    public HomeworkGrading getGradingBySubmissionId(Integer submissionId) {
        return gradingMapper.selectBySubmissionId(submissionId);
    }

    /**
     * 查询用户的批改记录
     */
    public List<HomeworkGrading> getUserGradings(String userName) {
        return gradingMapper.selectByUserName(userName);
    }

    /**
     * 构建批改 Prompt
     */
    private String buildGradingPrompt(HomeworkQuestion question, HomeworkSubmission submission) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("你是一位专业的教师，请对学生的作业进行智能批改。\n\n");

        prompt.append("【题目信息】\n");
        prompt.append("课程：").append(question.getCourseName()).append("\n");
        prompt.append("题目：").append(question.getQuestionTitle()).append("\n");
        prompt.append("内容：").append(question.getQuestionContent()).append("\n");
        if (question.getReferenceAnswer() != null && !question.getReferenceAnswer().isEmpty()) {
            prompt.append("参考答案：").append(question.getReferenceAnswer()).append("\n");
        }
        prompt.append("总分：").append(question.getTotalScore()).append("\n\n");

        prompt.append("【作答语言】\n");
        String language = submission.getAnswerLanguage() != null ? submission.getAnswerLanguage() : "txt";
        prompt.append(language).append("\n\n");

        prompt.append("【学生答案】\n");
        prompt.append(submission.getAnswerContent()).append("\n\n");

        prompt.append("请以 JSON 格式返回批改结果，格式如下：\n");
        prompt.append("{\n");
        prompt.append("  \"score\": 得分（数字），\n");
        prompt.append("  \"correctnessAnalysis\": \"正确性分析（100-150字）\",\n");
        prompt.append("  \"suggestions\": \"改进建议（100-150字）\",\n");
        prompt.append("  \"knowledgePoints\": [\"知识点1\", \"知识点2\"],\n");
        prompt.append("  \"feedback\": \"总体评语（100-150字）\"\n");
        prompt.append("}\n\n");
        prompt.append("注意：必须返回合法的 JSON 格式，不要添加其他文字。");

        return prompt.toString();
    }

    /**
     * 解析批改结果
     */
    private GradingResult parseGradingResult(String aiResponse) {
        try {
            if (aiResponse == null || aiResponse.isEmpty() || aiResponse.contains("抱歉")) {
                return null;
            }

            JsonNode jsonNode = objectMapper.readTree(aiResponse);

            GradingResult result = new GradingResult();
            result.score = jsonNode.has("score") ? jsonNode.get("score").asDouble() : 0.0;
            result.correctnessAnalysis = jsonNode.has("correctnessAnalysis") ? jsonNode.get("correctnessAnalysis").asText() : "";
            result.suggestions = jsonNode.has("suggestions") ? jsonNode.get("suggestions").asText() : "";
            result.feedback = jsonNode.has("feedback") ? jsonNode.get("feedback").asText() : "";

            // 处理知识点数组
            if (jsonNode.has("knowledgePoints") && jsonNode.get("knowledgePoints").isArray()) {
                result.knowledgePoints = jsonNode.get("knowledgePoints").toString();
            } else {
                result.knowledgePoints = "[]";
            }

            return result;

        } catch (Exception e) {
            System.err.println("解析批改结果失败：" + e.getMessage());
            return null;
        }
    }

    /**
     * 内部类：批改结果
     */
    private static class GradingResult {
        Double score;
        String correctnessAnalysis;
        String suggestions;
        String knowledgePoints;
        String feedback;
    }
}
