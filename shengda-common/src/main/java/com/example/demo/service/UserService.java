package com.example.demo.service;

import com.example.demo.mapper.UserMapper;
import com.example.demo.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.List;
import java.util.Objects;

/**
 * 用户业务逻辑层
 */
@Service
public class UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private UserCredentialCacheService userCredentialCacheService;  // 登录凭证的 Redis 缓存

    /**
     * 根据用户名查询用户
     */
    public User getUserByUserName(String userName) {
        return userMapper.getUserByUserName(userName);
    }

    /**
     * 用户登录验证
     * <p>
     * 先读取 Redis 中缓存的账号密码（有效期 7 天），命中则直接返回，不访问 MySQL；
     * 未命中、密码不匹配或 Redis 异常时回源 MySQL，验证通过后把凭证写回 Redis。
     */
    public User login(String userName, String userPassword) {
        // 第一步：优先走 Redis 缓存
        User cachedUser = userCredentialCacheService.getCachedUser(userName);
        if (cachedUser != null && Objects.equals(cachedUser.getUserPassword(), userPassword)) {
            return cachedUser;
        }

        // 第二步：缓存读取不成功，回源 MySQL
        User user = userMapper.getUserByUserName(userName);
        if (user != null && Objects.equals(user.getUserPassword(), userPassword)) {
            // 第三步：登录成功后写入 Redis，7 天内的后续登录可直接命中缓存
            userCredentialCacheService.cacheUser(user);
            return user;
        }
        return null;
    }

    /**
     * 用户注册
     */
    @Transactional
    public boolean register(String userName, String userPassword, String userEmail, String userGender) {
        // 检查用户名是否已存在
        if (userMapper.getUserByUserName(userName) != null) {
            return false;
        }
        int result = userMapper.insertUser(userName, userPassword, userEmail, userGender);
        return result > 0;
    }

    /**
     * 更新用户信息
     */
    @Transactional
    public boolean update(String userName, String userPassword, String userEmail, String userGender) {
        // 检查用户是否存在
        if (userMapper.getUserByUserName(userName) == null) {
            return false;
        }
        int result = userMapper.updateUser(userName, userPassword, userEmail, userGender);
        if (result > 0) {
            // 密码等资料已变更，必须清除 Redis 中的旧凭证，否则旧密码在缓存有效期内仍可登录
            userCredentialCacheService.evict(userName);
        }
        return result > 0;
    }

    /**
     * 删除用户
     */
    @Transactional
    public boolean deleteUser(String userName) {
        int result = userMapper.deleteUser(userName);
        if (result > 0) {
            // 账号已删除，同步清除凭证缓存，避免被删用户在缓存有效期内继续登录
            userCredentialCacheService.evict(userName);
        }
        return result > 0;
    }

    /**
     * 获取所有用户名
     */
    public List<String> getAllUserNames() {
        return userMapper.getAllUserNames();
    }

    /**
     * 验证用户是否存在
     */
    public boolean validateUser(String userName) {
        return userMapper.getUserByUserName(userName) != null;
    }
}
