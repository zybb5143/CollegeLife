package com.example.demo.mapper;

import com.example.demo.model.HomeworkGrading;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface HomeworkGradingMapper {
    // 保存批改记录
    int insertGrading(HomeworkGrading grading);
    
    // 根据提交ID查询批改记录
    HomeworkGrading selectBySubmissionId(@Param("submissionId") Integer submissionId);
    
    // 查询用户的批改记录
    List<HomeworkGrading> selectByUserName(@Param("userName") String userName);
}
