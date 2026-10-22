package com.example.demo.model;

import lombok.Data;

import java.util.Date;

/**
 * 个人网盘文件实体类
 */
@Data
public class UserFile {

    /**
     * 文件 ID
     */
    private Long fileId;

    /**
     * 用户名
     */
    private String userName;

    /**
     * 原始文件名
     */
    private String fileName;

    /**
     * 文件存储路径（UUID 命名）
     */
    private String filePath;

    /**
     * 文件大小（字节）
     */
    private Long fileSize;

    /**
     * 文件类型（MIME 类型）
     */
    private String fileType;

    /**
     * 上传时间
     */
    private Date uploadTime;
}
