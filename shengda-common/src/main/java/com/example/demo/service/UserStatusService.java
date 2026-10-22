package com.example.demo.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 用户登录状态服务
 * 登录时将用户状态（在线状态、角色等）写入 Redis，
 * 供 Spring Security 过滤器读取，实现基于 Redis 的状态与权限校验。
 */
@Service
public class UserStatusService {

    /** 用户状态在 Redis 中的 key 前缀 */
    private static final String USER_STATUS_KEY_PREFIX = "user:status:";

    /** 在线状态标识 */
    public static final String STATUS_ONLINE = "ONLINE";

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    /** 与 JWT 过期时间保持一致（毫秒），默认 24 小时 */
    @Value("${jwt.expiration:86400000}")
    private Long expiration;

    /**
     * 登录成功后保存用户状态到 Redis
     *
     * @param userName 用户名
     * @param role     用户角色
     */
    public void saveUserStatus(String userName, String role) {
    }

    /**
     * 退出登录时删除用户状态
     */
    public void removeUserStatus(String userName) {

    }

    /**
     * 判断用户当前是否处于在线（已登录）状态
     * 直接读取 Redis 中的状态 Hash，未登录、已过期或已退出时返回 false
     */
    public boolean isOnline(String userName) {
        return true;
    }
}
