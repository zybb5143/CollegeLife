package com.example.demo.controller;

import com.example.demo.model.User;
import com.example.demo.model.UserFile;
import com.example.demo.service.UserFileService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 个人网盘文件 Controller
 */
@RestController
@RequestMapping("/api/userfile")
public class UserFileController {

    private static final Logger logger = LoggerFactory.getLogger(UserFileController.class);

    @Autowired
    private UserFileService userFileService;

    /**
     * 上传文件
     */
    @PostMapping("/upload")
    public Map<String, Object> uploadFile(
            @RequestParam("file") MultipartFile file) {

        Map<String, Object> result = new HashMap<>();
        try {
            if (file == null || file.isEmpty()) {
                result.put("success", false);
                result.put("message", "文件不能为空");
                return result;
            }

            // 从 SecurityContext 获取当前用户
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !(authentication.getPrincipal() instanceof User)) {
                result.put("success", false);
                result.put("message", "请先登录");
                return result;
            }

            User currentUser = (User) authentication.getPrincipal();
            String userName = currentUser.getUserName();

            UserFile userFile = userFileService.uploadFile(file, userName);
            logger.info("文件上传成功：{}, 用户：{}, 文件 ID: {}",
                    file.getOriginalFilename(), userName, userFile.getFileId());
            result.put("success", true);
            result.put("message", "上传成功");
            result.put("data", userFile);

        } catch (Exception e) {
            logger.error("文件上传失败：{}", file.getOriginalFilename(), e);
            result.put("success", false);
            result.put("message", "上传失败：" + e.getMessage());
        }

        return result;
    }

    /**
     * 获取文件列表（当前用户）
     */
    @GetMapping("/list")
    public Map<String, Object> getFileList() {
        Map<String, Object> result = new HashMap<>();
        try {
            // 从 SecurityContext 获取当前用户
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !(authentication.getPrincipal() instanceof User)) {
                result.put("success", false);
                result.put("message", "请先登录");
                return result;
            }

            User currentUser = (User) authentication.getPrincipal();
            String userName = currentUser.getUserName();

            List<UserFile> fileList = userFileService.getFileList(userName);
            result.put("success", true);
            result.put("data", fileList);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "查询失败：" + e.getMessage());
        }
        return result;
    }

    /**
     * 获取所有用户的文件列表（仅管理员）
     */
    @GetMapping("/list/all")
    public Map<String, Object> getAllFiles() {
        Map<String, Object> result = new HashMap<>();
        try {
            // 从 SecurityContext 获取当前用户并检查是否为管理员
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !(authentication.getPrincipal() instanceof User)) {
                result.put("success", false);
                result.put("message", "请先登录");
                return result;
            }

            User currentUser = (User) authentication.getPrincipal();
            if (!"admin".equals(currentUser.getRole()) && !"boss".equals(currentUser.getRole())) {
                result.put("success", false);
                result.put("message", "需要管理员或老板权限");
                return result;
            }

            List<UserFile> fileList = userFileService.getAllFiles();
            result.put("success", true);
            result.put("data", fileList);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "查询失败：" + e.getMessage());
        }
        return result;
    }

    /**
     * 删除文件
     */
    @DeleteMapping("/delete/{fileId}")
    public Map<String, Object> deleteFile(@PathVariable Long fileId) {
        Map<String, Object> result = new HashMap<>();
        try {
            // 从 SecurityContext 获取当前用户
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !(authentication.getPrincipal() instanceof User)) {
                result.put("success", false);
                result.put("message", "请先登录");
                return result;
            }

            User currentUser = (User) authentication.getPrincipal();
            String currentUserName = currentUser.getUserName();

            boolean success = userFileService.deleteFile(fileId, currentUserName);
            result.put("success", success);
            result.put("message", success ? "删除成功" : "删除失败");
        } catch (SecurityException e) {
            result.put("success", false);
            result.put("message", "权限错误：" + e.getMessage());
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "删除失败：" + e.getMessage());
        }
        return result;
    }

    /**
     * 下载文件
     */
    @GetMapping("/download")
    public Map<String, Object> downloadFile(@RequestParam("filePath") String filePath) {
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("downloadUrl", "/api/download/download/" + filePath);
        return result;
    }
}
