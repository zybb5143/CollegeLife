package com.example.demo.util;

import com.example.demo.model.User;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpSession;

import java.util.Objects;

/**
 * Session 工具类
 * 用于管理用户会话信息，包括登录状态、用户信息和权限等
 */
@Component
public class SessionUtil {

    private static final String CURRENT_USER_KEY = "currentUser";

    /**
     * 获取当前 HttpSession
     */
    public static HttpSession getSession() {
        ServletRequestAttributes attributes = 
            (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            return null;
        }
        return attributes.getRequest().getSession();
    }

    /**
     * 保存用户登录信息到 Session
     */
    public static void login(User user) {
        HttpSession session = getSession();
        if (session != null) {
            session.setAttribute(CURRENT_USER_KEY, user);
        }
    }

    /**
     * 获取当前登录的用户
     */
    public static User getCurrentUser() {
        HttpSession session = getSession();
        if (session != null) {
            return (User) session.getAttribute(CURRENT_USER_KEY);
        }
        return null;
    }

    /**
     * 检查用户是否已登录
     */
    public static boolean isLoggedIn() {
        return Objects.nonNull(getCurrentUser());
    }

    /**
     * 检查当前用户是否为管理员
     */
    public static boolean isAdmin() {
        User user = getCurrentUser();
        return user != null && "admin".equals(user.getRole());
    }

    /**
     * 退出登录，清除 Session 信息
     */
    public static void logout() {
        HttpSession session = getSession();
        if (session != null) {
            session.removeAttribute(CURRENT_USER_KEY);
        }
    }

    /**
     * 获取当前用户的用户名
     */
    public static String getCurrentUserName() {
        User user = getCurrentUser();
        return user != null ? user.getUserName() : null;
    }

    /**
     * 获取当前用户的角色
     */
    public static String getCurrentUserRole() {
        User user = getCurrentUser();
        return user != null ? user.getRole() : "user";
    }
}
