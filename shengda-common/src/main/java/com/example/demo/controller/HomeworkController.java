package com.example.demo.controller;

import com.example.demo.model.HomeworkGrading;
import com.example.demo.model.HomeworkQuestion;
import com.example.demo.model.HomeworkSubmission;
import com.example.demo.service.HomeworkService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/homework")
public class HomeworkController {

    @Autowired
    private HomeworkService homeworkService;

    /**
     * 管理员发布题目
     */
    @PostMapping("/publish")
    public Map<String, Object> publishQuestion(@RequestBody HomeworkQuestion question) {
        Map<String, Object> response = new HashMap<>();
        try {
            boolean success = homeworkService.publishQuestion(question);
            if (success) {
                response.put("success", true);
                response.put("message", "题目发布成功");
            } else {
                response.put("success", false);
                response.put("message", "题目发布失败");
            }
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "发布失败：" + e.getMessage());
        }
        return response;
    }

    /**
     * 查询所有题目
     */
    @GetMapping("/questions")
    public Map<String, Object> getAllQuestions() {
        Map<String, Object> response = new HashMap<>();
        try {
            List<HomeworkQuestion> questions = homeworkService.getAllQuestions();
            response.put("success", true);
            response.put("data", questions);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "查询失败：" + e.getMessage());
        }
        return response;
    }

    /**
     * 根据ID查询题目
     */
    @GetMapping("/question/{questionId}")
    public Map<String, Object> getQuestionById(@PathVariable Integer questionId) {
        Map<String, Object> response = new HashMap<>();
        try {
            HomeworkQuestion question = homeworkService.getQuestionById(questionId);
            if (question != null) {
                response.put("success", true);
                response.put("data", question);
            } else {
                response.put("success", false);
                response.put("message", "题目不存在");
            }
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "查询失败：" + e.getMessage());
        }
        return response;
    }

    /**
     * 学生提交答案
     */
    @PostMapping("/submit")
    public Map<String, Object> submitAnswer(@RequestBody HomeworkSubmission submission) {
        Map<String, Object> response = new HashMap<>();
        try {
            Integer submissionId = homeworkService.submitAnswer(submission);
            if (submissionId != null) {
                // 异步启动智能批改
                homeworkService.gradeHomeworkAsync(submissionId);
                
                response.put("success", true);
                response.put("message", "提交成功，AI正在批改...");
                response.put("submissionId", submissionId);
            } else {
                response.put("success", false);
                response.put("message", "提交失败");
            }
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "提交失败：" + e.getMessage());
        }
        return response;
    }

    /**
     * 查询用户的提交记录
     */
    @GetMapping("/my-submissions")
    public Map<String, Object> getUserSubmissions(@RequestParam String userName) {
        Map<String, Object> response = new HashMap<>();
        try {
            List<HomeworkSubmission> submissions = homeworkService.getUserSubmissions(userName);
            response.put("success", true);
            response.put("data", submissions);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "查询失败：" + e.getMessage());
        }
        return response;
    }

    /**
     * 查询批改结果
     */
    @GetMapping("/grading/{submissionId}")
    public Map<String, Object> getGradingResult(@PathVariable Integer submissionId) {
        Map<String, Object> response = new HashMap<>();
        try {
            HomeworkGrading grading = homeworkService.getGradingBySubmissionId(submissionId);
            if (grading != null) {
                response.put("success", true);
                response.put("data", grading);
            } else {
                response.put("success", false);
                response.put("message", "批改记录不存在，可能还在批改中");
            }
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "查询失败：" + e.getMessage());
        }
        return response;
    }

    /**
     * 查询用户的批改记录
     */
    @GetMapping("/my-gradings")
    public Map<String, Object> getUserGradings(@RequestParam String userName) {
        Map<String, Object> response = new HashMap<>();
        try {
            List<HomeworkGrading> gradings = homeworkService.getUserGradings(userName);
            response.put("success", true);
            response.put("data", gradings);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "查询失败：" + e.getMessage());
        }
        return response;
    }
}
