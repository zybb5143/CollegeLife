package com.example.demo.model;

import lombok.Data;
import java.sql.Timestamp;

@Data
public class HomeworkSubmission {
    private Integer submissionId;
    private Integer questionId;
    private String userName;
    private String answerContent;
    private String answerLanguage; // txt, java, javascript, python, cpp
    private String status; // submitted, grading, graded
    private Timestamp submitTime;
    
    // 关联查询字段
    private String questionTitle;
    private String courseName;
}
