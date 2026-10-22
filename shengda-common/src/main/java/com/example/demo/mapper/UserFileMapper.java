package com.example.demo.mapper;

import com.example.demo.model.UserFile;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

/**
 * 个人网盘文件 Mapper 接口
 */
@Mapper
public interface UserFileMapper {
    
    /**
     * 插入文件信息
     */
    int insert(UserFile userFile);
    
    /**
     * 根据用户名查询文件列表
     */
    List<UserFile> selectByUserName(@Param("userName") String userName);
    
    /**
     * 查询所有文件（管理员用）
     */
    List<UserFile> selectAllFiles();
    
    /**
     * 根据文件 ID 查询
     */
    UserFile selectByFileId(@Param("fileId") Long fileId);
    
    /**
     * 根据文件 ID 删除
     */
    int deleteByFileId(@Param("fileId") Long fileId);
}
