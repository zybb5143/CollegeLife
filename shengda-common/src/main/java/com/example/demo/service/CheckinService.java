package com.example.demo.service;

import com.example.demo.mapper.UserMapper;
import com.example.demo.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
public class CheckinService {

    @Autowired
    private UserMapper userMapper;

    /**
     * 用户签到（每 15 分钟可签到一次）
     * @param userName 用户名
     * @return 返回签到结果：0-15 分钟内已签到，1-签到成功，2-连续签到中断
     */
    public int checkin(String userName) {
        User user = userMapper.getUserByUserName(userName);
        if (user == null) {
            return -1; // 用户不存在
        }

        java.util.Date now = new java.util.Date();
        java.sql.Timestamp lastCheckinDate = user.getLastCheckinDate() != null ? 
            new java.sql.Timestamp(user.getLastCheckinDate().getTime()) : null;

        // 检查是否在 15 分钟内已经签到
        if (lastCheckinDate != null) {
            long minutesSinceLastCheckin = (now.getTime() - lastCheckinDate.getTime()) / (1000 * 60);
            if (minutesSinceLastCheckin < 15) {
                return 0; // 15 分钟内已签到
            }
        }

        int continuousDays = user.getContinuousCheckinDays() != null ? user.getContinuousCheckinDays() : 0;

        // 判断是否是连续签到（上次签到在 15-30 分钟内）
        if (lastCheckinDate != null) {
            long minutesSinceLastCheckin = (now.getTime() - lastCheckinDate.getTime()) / (1000 * 60);
            if (minutesSinceLastCheckin >= 15 && minutesSinceLastCheckin < 30) {
                continuousDays++; // 连续签到
            } else if (minutesSinceLastCheckin >= 30) {
                // 超过 30 分钟，中断，重置为 1
                continuousDays = 1;
                return 2; // 连续签到中断
            }
        } else {
            // 第一次签到
            continuousDays = 1;
        }

        // 更新签到信息
        userMapper.updateCheckinInfo(userName, continuousDays, new java.sql.Timestamp(now.getTime()));
        return 1; // 签到成功
    }

    /**
     * 获取用户的签到信息
     * @param userName 用户名
     * @return 返回包含连续签到天数和是否可签到的数组
     */
    public int[] getCheckinInfo(String userName) {
        User user = userMapper.getUserByUserName(userName);
        if (user == null) {
            return new int[]{0, 0};
        }

        int continuousDays = user.getContinuousCheckinDays() != null ? user.getContinuousCheckinDays() : 0;
        int canCheckin = 1; // 默认可以签到

        if (user.getLastCheckinDate() != null) {
            java.util.Date now = new java.util.Date();
            long minutesSinceLastCheckin = (now.getTime() - user.getLastCheckinDate().getTime()) / (1000 * 60);
            if (minutesSinceLastCheckin < 15) {
                canCheckin = 0; // 15 分钟内不能签到
            }
        }

        return new int[]{continuousDays, canCheckin};
    }

    /**
     * 获取距离下次签到还剩多少分钟
     * @param userName 用户名
     * @return 剩余分钟数，如果可以直接签到则返回 0
     */
    public long getMinutesUntilNextCheckin(String userName) {
        User user = userMapper.getUserByUserName(userName);
        if (user == null || user.getLastCheckinDate() == null) {
            return 0;
        }

        java.util.Date now = new java.util.Date();
        long minutesSinceLastCheckin = (now.getTime() - user.getLastCheckinDate().getTime()) / (1000 * 60);
        
        if (minutesSinceLastCheckin >= 15) {
            return 0;
        }
        
        return 15 - minutesSinceLastCheckin;
    }
}
