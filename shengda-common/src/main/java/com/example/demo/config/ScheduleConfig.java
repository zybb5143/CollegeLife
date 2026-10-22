package com.example.demo.config;

import com.example.demo.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import java.time.LocalDate;
import java.util.List;

@Configuration
@EnableScheduling
public class ScheduleConfig {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private com.example.demo.service.ExcelExportService excelExportService;

    @Scheduled(cron = "0 0 0 1 1 ?")
    public void resetDailyCheckin() {
        System.out.println("=== 开始执行签到重置任务 [" + new java.util.Date() + "] ===");

        try {
            long currentTime = System.currentTimeMillis();

            List<String> allUserNames = userMapper.getAllUserNames();

            if (allUserNames == null || allUserNames.isEmpty()) {
                System.out.println("没有用户需要处理");
                return;
            }

            int resetCount = 0;
            for (String userName : allUserNames) {
                com.example.demo.model.User user = userMapper.getUserByUserName(userName);
                if (user != null && user.getLastCheckinDate() != null) {
                    long minutesSinceLastCheckin = (currentTime - user.getLastCheckinDate().getTime()) / (1000 * 60);

                    // 如果超过 30 分钟未签到，重置连续签到次数
                    if (minutesSinceLastCheckin > 30) {
                        userMapper.updateCheckinInfo(userName, 0, null);
                        resetCount++;
                        System.out.println("用户 [" + userName + "] 连续签到已重置（上次签到：" + user.getLastCheckinDate() + "，已超过 " + minutesSinceLastCheckin + " 分钟）");
                    }
                }
            }

            System.out.println("=== 签到重置任务完成 ===");
            System.out.println("总用户数：" + allUserNames.size());
            System.out.println("重置用户数：" + resetCount);

        } catch (Exception e) {
            System.err.println("签到重置任务执行失败：" + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 定时任务：每 10 分钟导出最新 10 个帖子到 Excel
     */
    @Scheduled(cron = "0 0 0 1 1 ?")
    public void exportLatestTiesToExcel() {
        System.out.println("=== 开始执行帖子导出任务 [" + new java.util.Date() + "] ===");

        try {
            String filePath = excelExportService.exportLatestTies();
            System.out.println("=== 帖子导出任务完成 ===");
            System.out.println("文件路径：" + filePath);

        } catch (Exception e) {
            System.err.println("帖子导出任务执行失败：" + e.getMessage());
            e.printStackTrace();
        }
    }
}
