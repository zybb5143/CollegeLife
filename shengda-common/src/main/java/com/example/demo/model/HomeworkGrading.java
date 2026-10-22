package com.example.demo.model;

import lombok.Data;
import java.sql.Timestamp;

@Data
public class HomeworkGrading {
    private Integer gradingId;
    private Integer submissionId;
    private String userName;
    private Double score;
    private String aiFeedback;
    private String correctnessAnalysis;
    private String improvementSuggestions;
    private String knowledgePoints; // JSON数组
    private Timestamp gradeTime;
}
