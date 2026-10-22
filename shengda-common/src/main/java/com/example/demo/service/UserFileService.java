package com.example.demo.service;

import com.example.demo.mapper.UserFileMapper;
import com.example.demo.mapper.UserMapper;
import com.example.demo.model.User;
import com.example.demo.model.UserFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

/**
 * 个人网盘文件 Service
 */
@Service
public class UserFileService {
    
    private static final Logger logger = LoggerFactory.getLogger(UserFileService.class);
    
    @Autowired
    private UserFileMapper userFileMapper;
    
    @Autowired
    private UserMapper userMapper;
    
    /**
     * 上传文件
     */
    public UserFile uploadFile(MultipartFile file, String userName) throws IOException {
        // 生成 UUID 文件名
        String originalFileName = file.getOriginalFilename();
        String extension = "";
        if (originalFileName != null && originalFileName.contains(".")) {
            extension = originalFileName.substring(originalFileName.lastIndexOf("."));
        }
        String uuidFileName = UUID.randomUUID().toString() + extension;
        
        // 保存文件到 uploads 目录
        String uploadDir = "src/main/resources/static/uploads/";
        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }
        
        Path filePath = uploadPath.resolve(uuidFileName);
        file.transferTo(filePath);
        logger.info("文件保存成功：{} -> {}", originalFileName, filePath.toAbsolutePath());
        
        // 创建文件记录
        UserFile userFile = new UserFile();
        userFile.setUserName(userName);
        userFile.setFileName(originalFileName);
        userFile.setFilePath(uuidFileName);
        userFile.setFileSize(file.getSize());
        userFile.setFileType(file.getContentType());
        
        // 保存到数据库
        userFileMapper.insert(userFile);
        logger.debug("数据库记录保存成功：{}", userFile);
        
        return userFile;
    }
    
    /**
     * 直接上传文件记录到数据库（用于发帖时自动保存）
     */
    public void uploadFileDirectly(UserFile userFile) {
        userFileMapper.insert(userFile);
        logger.debug("数据库记录保存成功：{}", userFile);
    }
    
    /**
     * 查询用户的文件列表（管理员可以查询所有用户）
     */
    public List<UserFile> getFileList(String userName) {
        return userFileMapper.selectByUserName(userName);
    }
    
    /**
     * 查询所有用户的文件列表（仅管理员）
     */
    public List<UserFile> getAllFiles() {
        return userFileMapper.selectAllFiles();
    }
    
    /**
     * 根据 ID 查询文件
     */
    public UserFile getFileById(Long fileId) {
        return userFileMapper.selectByFileId(fileId);
    }
    
    /**
     * 检查用户是否为管理员
     */
    public boolean isAdmin(String userName) {
        User user = userMapper.getUserByUserName(userName);
        return user != null && "admin".equals(user.getRole());
    }
    
    /**
     * 删除文件（带权限验证）
     * @param fileId 文件 ID
     * @param currentUserName 当前操作用户名
     * @return 删除结果
     */
    public boolean deleteFile(Long fileId, String currentUserName) {
        UserFile userFile = userFileMapper.selectByFileId(fileId);
        if (userFile == null) {
            return false;
        }
        
        // 权限验证：管理员可以删除任意文件，普通用户只能删除自己的文件
        boolean isAdmin = isAdmin(currentUserName);
        if (!isAdmin && !currentUserName.equals(userFile.getUserName())) {
            throw new SecurityException("无权删除该文件");
        }
        
        // 删除物理文件
        String uploadDir = "src/main/resources/static/uploads/";
        Path filePath = Paths.get(uploadDir).resolve(userFile.getFilePath());
        try {
            if (Files.exists(filePath)) {
                Files.delete(filePath);
            }
        } catch (IOException e) {
            logger.warn("删除物理文件失败：{}", userFile.getFilePath(), e);
        }
        
        // 删除数据库记录
        return userFileMapper.deleteByFileId(fileId) > 0;
    }
}
