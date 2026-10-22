package com.example.demo.service;

import com.example.demo.mapper.TieMapper;
import com.example.demo.mapper.UserMapper;
import com.example.demo.model.Tie;
import com.example.demo.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 管理员业务逻辑层
 */
@Service
public class AdminService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private TieMapper tieMapper;

    @Autowired
    private UserCredentialCacheService userCredentialCacheService;  // 登录凭证的 Redis 缓存

    /**
     * 获取管理员仪表盘数据
     */
    public Map<String, Object> getDashboardData() {
        Map<String, Object> dashboard = new HashMap<>();
        
        // 统计用户数
        List<String> allUsers = userMapper.getAllUserNames();
        dashboard.put("totalUsers", allUsers.size());
        dashboard.put("users", allUsers);
        
        // 统计帖子数
        List<Tie> allTies = tieMapper.getAllTies();
        dashboard.put("totalTies", allTies.size());
        
        // 最新 10 个帖子
        List<Tie> latestTies = tieMapper.getLatest10Ties();
        dashboard.put("latestTies", latestTies);
        
        return dashboard;
    }

    /**
     * 删除帖子
     */
    public boolean deleteTie(Integer tieId) {
        if (tieId == null) {
            return false;
        }
        int result = tieMapper.deleteTie(tieId);
        return result > 0;
    }

    /**
     * 删除用户
     */
    public boolean deleteUser(String userName) {
        int result = userMapper.deleteUser(userName);
        if (result > 0) {
            // 同步清除 Redis 中的登录凭证缓存，否则被删用户在缓存有效期内仍可登录
            userCredentialCacheService.evict(userName);
        }
        return result > 0;
    }

    /**
     * 验证用户是否为管理员
     */
    public boolean isAdmin(User user) {
        return user != null && "admin".equals(user.getRole());
    }

    /**
     * 验证用户是否为老板
     */
    public boolean isBoss(User user) {
        return user != null && "boss".equals(user.getRole());
    }
}
