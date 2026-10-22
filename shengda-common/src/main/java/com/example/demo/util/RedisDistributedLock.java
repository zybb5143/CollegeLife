package com.example.demo.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * 基于 Redis 的分布式锁（SETNX 实现）
 * <p>
 * 用于替代 Lua 脚本来保证同一红包抢红包操作的原子性：
 * 通过 {@code SET key value NX PX expire} 抢锁，锁值使用唯一 token，
 * 释放时校验 token 避免误删他人的锁。
 */
@Component
public class RedisDistributedLock {

    private static final Logger log = LoggerFactory.getLogger(RedisDistributedLock.class);

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 尝试获取锁（自旋直到成功或超时）
     *
     * @param lockKey  锁的 key
     * @param expireMs 锁的过期时间（毫秒），防止持有者宕机导致死锁
     * @param waitMs   最长等待时间（毫秒），超过仍未拿到锁则返回 null
     * @return 拿到锁返回唯一 token（释放锁时需要），失败返回 null
     */
    public String tryLock(String lockKey, long expireMs, long waitMs) {
        String token = UUID.randomUUID().toString();
        long deadline = System.currentTimeMillis() + waitMs;
        while (true) {
            Boolean ok = redisTemplate.opsForValue()
                    .setIfAbsent(lockKey, token, expireMs, TimeUnit.MILLISECONDS);
            if (Boolean.TRUE.equals(ok)) {
                return token;
            }
            if (System.currentTimeMillis() >= deadline) {
                return null;
            }
            try {
                // 短暂休眠后重试，避免空转打满 CPU
                Thread.sleep(20);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return null;
            }
        }
    }

    /**
     * 释放锁：仅当锁仍由当前 token 持有时才删除，避免误删他人重新获取的锁
     *
     * @return 是否成功释放
     */
    public boolean unlock(String lockKey, String token) {
        if (token == null) {
            return false;
        }
        try {
            Object current = redisTemplate.opsForValue().get(lockKey);
            if (token.equals(current)) {
                return redisTemplate.delete(lockKey);
            }
        } catch (Exception e) {
            log.error("释放分布式锁失败, lockKey={}", lockKey, e);
        }
        return false;
    }
}
