package com.example.demo.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import com.example.demo.model.User;

/**
 * 安全权限检查工具类
 * 用于@PreAuthorize 注解中
 */
@Component("securityCheck")
public class SecurityCheck {

    /**
     * 检查当前用户是否为管理员
     */
    public boolean isAdmin() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof User) {
            User user = (User) authentication.getPrincipal();

            return "admin".equals(user.getRole()) || "boss".equals(user.getRole());
        }
        return false;
    }

    /**
     * 检查当前用户是否为老板
     */
    public boolean isBoss() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof User) {
            User user = (User) authentication.getPrincipal();
            return "boss".equals(user.getRole());
        }
        return false;
    }

    /**
     * 获取当前登录用户
     */
    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof User) {
            return (User) authentication.getPrincipal();
        }
        return null;
    }
}
