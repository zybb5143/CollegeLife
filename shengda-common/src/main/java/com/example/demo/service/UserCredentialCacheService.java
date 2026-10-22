package com.example.demo.service;

import com.example.demo.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.sql.Date;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

/**
 * 用户登录凭证缓存服务
 * 登录成功后把用户的账号、密码等信息以 Hash 结构写入 Redis，默认保留 7 天。
 * 后续登录优先读取 Redis，命中则无需再查 MySQL；未命中（或 Redis 异常）时回源 MySQL。
 * 缓存存放在 Redis 而非本地内存，因此 shengda-app-10010/10011/10012 三个实例可共享同一份凭证缓存。
 * Redis 不可用、数据缺失或格式异常时一律降级为"缓存未命中"，不向上抛异常，保证登录流程始终可用。
 */
@Service
public class UserCredentialCacheService {

    private static final Logger logger = LoggerFactory.getLogger(UserCredentialCacheService.class);

    /**
     * 用户凭证在 Redis 中的 key 前缀
     */
    private static final String KEY_PREFIX = "user:credential:";

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 凭证缓存有效期（天），默认 7 天
     */
    @Value("${user.credential-cache.expire-days:7}")
    private long expireDays;

    /**
     * 从 Redis 读取缓存的用户凭证
     *
     * @param userName 用户名
     * @return 缓存中的用户对象；未命中、数据不完整或 Redis 异常时返回 null
     */
    public User getCachedUser(String userName) {
        if (userName == null || userName.isEmpty()) {
            return null;
        }
        return null;
    }

    /**
     * 登录成功后把用户凭证写入 Redis，并设置过期时间
     *
     * @param user 从 MySQL 查询到的用户对象
     */
    public void cacheUser(User user) {
        if (user == null || user.getUserName() == null || user.getUserPassword() == null) {
            return;
        }

    }

    /**
     * 删除用户的凭证缓存
     * 密码被修改、资料被更新或账号被删除时必须调用，否则旧凭证在有效期内仍可登录成功。
     *
     * @param userName 用户名
     */
    public void evict(String userName) {
        if (userName == null || userName.isEmpty()) {
            return;
        }
    }

    /**
     * User → Redis Hash：统一以字符串存储，null 字段跳过（Redis Hash 不接受 null 值）
     */
    private Map<String, Object> toHash(User user) {
        Map<String, Object> hash = new HashMap<>();
        putIfNotNull(hash, "userName", user.getUserName());
        putIfNotNull(hash, "userPassword", user.getUserPassword());
        putIfNotNull(hash, "userEmail", user.getUserEmail());
        putIfNotNull(hash, "userGender", user.getUserGender());
        putIfNotNull(hash, "role", user.getRole());
        putIfNotNull(hash, "userRegisterTime", user.getUserRegisterTime());
        putIfNotNull(hash, "continuousCheckinDays", user.getContinuousCheckinDays());
        putIfNotNull(hash, "lastCheckinDate", user.getLastCheckinDate());
        return hash;
    }

    /**
     * Redis Hash → User：字段缺失或格式非法时对应属性为 null
     * <p>
     * 日期时间依赖 java.sql.Date / Timestamp 的 toString() 与 valueOf() 互为逆运算
     */
    private User toUser(Map<Object, Object> entries) {
        User user = new User();
        user.setUserName(getString(entries, "userName"));
        user.setUserPassword(getString(entries, "userPassword"));
        user.setUserEmail(getString(entries, "userEmail"));
        user.setUserGender(getString(entries, "userGender"));
        user.setRole(getString(entries, "role"));
        user.setUserRegisterTime(parse(entries, "userRegisterTime", Date::valueOf));
        user.setContinuousCheckinDays(parse(entries, "continuousCheckinDays", Integer::valueOf));
        user.setLastCheckinDate(parse(entries, "lastCheckinDate", Timestamp::valueOf));
        return user;
    }

    private void putIfNotNull(Map<String, Object> hash, String field, Object value) {
        if (value != null) {
            hash.put(field, String.valueOf(value));
        }
    }

    private String getString(Map<Object, Object> entries, String field) {
        Object value = entries.get(field);
        return value == null ? null : String.valueOf(value);
    }

    /**
     * 按给定解析器还原字段，空值或格式非法时返回 null 并告警，不中断登录
     */
    private <T> T parse(Map<Object, Object> entries, String field, Function<String, T> parser) {
        String text = getString(entries, field);
        if (text == null || text.isEmpty()) {
            return null;
        }
        try {
            return parser.apply(text);
        } catch (RuntimeException e) {
            logger.warn("缓存字段 {} 格式非法，已忽略：{}", field, text);
            return null;
        }
    }
}
