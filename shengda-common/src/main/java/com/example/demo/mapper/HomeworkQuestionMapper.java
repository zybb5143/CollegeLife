package com.example.demo.mapper;

import com.example.demo.model.HomeworkQuestion;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface HomeworkQuestionMapper {
    // 管理员发布题目
    int insertQuestion(HomeworkQuestion question);
    
    // 查询所有题目
    List<HomeworkQuestion> selectAllQuestions();
    
    // 根据ID查询题目
    HomeworkQuestion selectQuestionById(@Param("questionId") Integer questionId);
    
    // 根据课程名称查询
    List<HomeworkQuestion> selectByCourseName(@Param("courseName") String courseName);
}
