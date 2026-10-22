package com.example.demo.mapper;

import com.example.demo.model.HomeworkSubmission;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface HomeworkSubmissionMapper {
    // 学生提交答案
    int insertSubmission(HomeworkSubmission submission);
    
    // 根据ID查询提交
    HomeworkSubmission selectSubmissionById(@Param("submissionId") Integer submissionId);
    
    // 查询用户的提交记录
    List<HomeworkSubmission> selectByUserName(@Param("userName") String userName);
    
    // 查询某题目的所有提交
    List<HomeworkSubmission> selectByQuestionId(@Param("questionId") Integer questionId);
    
    // 更新提交状态
    int updateStatus(@Param("submissionId") Integer submissionId, @Param("status") String status);
}
