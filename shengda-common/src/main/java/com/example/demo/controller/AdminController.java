package com.example.demo.controller;

import com.example.demo.model.Tie;
import com.example.demo.model.User;
import com.example.demo.service.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 管理员专用 Controller
 */
@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Autowired
    private AdminService adminService;

    /**
     * 获取管理员仪表盘数据
     */
    @GetMapping("/dashboard")
    public Map<String, Object> getDashboard() {
        Map<String, Object> result = adminService.getDashboardData();
        result.put("success", true);
        return result;
    }

    /**
     * 删除指定帖子（仅管理员）
     */
    @DeleteMapping("/tie/{tieId}")
    @PreAuthorize("@securityCheck.isAdmin()")
    public Map<String, Object> deleteTie(@PathVariable Integer tieId) {
        Map<String, Object> result = new HashMap<>();

        try {
            boolean success = adminService.deleteTie(tieId);
            result.put("success", success);
            result.put("message", success ? "帖子已删除" : "删除失败");
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "删除失败：" + e.getMessage());
        }

        return result;
    }

    /**
     * 删除用户（仅老板）
     */
    @DeleteMapping("/user/{userName}")
    @PreAuthorize("@securityCheck.isBoss()")
    public Map<String, Object> deleteUser(@PathVariable String userName) {
        Map<String, Object> result = new HashMap<>();

        try {
            // 不能删除自己
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.getPrincipal() instanceof User) {
                User currentUser = (User) authentication.getPrincipal();
                if (adminService.isBoss(currentUser) && currentUser.getUserName().equals(userName)) {
                    result.put("success", false);
                    result.put("message", "不能删除自己");
                    return result;
                }
            }

            // 执行删除操作
            boolean success = adminService.deleteUser(userName);
            if (success) {
                result.put("success", true);
                result.put("message", "用户已删除");
            } else {
                result.put("success", false);
                result.put("message", "删除失败");
            }
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "删除失败：" + e.getMessage());
        }

        return result;
    }

    /**
     * 测试权限接口
     */
    @GetMapping("/test-role")
    public Map<String, Object> testRole() {
        Map<String, Object> result = new HashMap<>();

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof User) {
            User user = (User) authentication.getPrincipal();

            result.put("success", true);
            result.put("username", user.getUserName());
            result.put("role", user.getRole());
            result.put("isAdmin", adminService.isAdmin(user));
        } else {
            result.put("success", false);
            result.put("message", "未登录");
        }

        return result;
    }
}
