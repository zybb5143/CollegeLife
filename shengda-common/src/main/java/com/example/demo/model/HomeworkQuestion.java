package com.example.demo.model;

import lombok.Data;
import java.sql.Timestamp;

@Data
public class HomeworkQuestion {
    private Integer questionId;
    private String adminName;
    private String courseName;
    private String questionTitle;
    private String questionContent;
    private String referenceAnswer;
    private Double totalScore;
    private Timestamp createTime;
}
