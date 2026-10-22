package com.example.demo.model;

import lombok.Data;

import java.sql.Date;
import java.sql.Timestamp;

@Data
//使用 Data 注解，User 类所有的成员变量都会自动生成 Get 和 Set 方法，同时自动生成无参构造方法
public class User {
    private String userName;
    private String userPassword;
    private Date userRegisterTime;
    private String userEmail;
    private String userGender;
    private String role;  // 用户角色：admin-管理员，user-普通用户
    private Integer continuousCheckinDays;  // 最大连续签到次数
    private Timestamp lastCheckinDate;  // 上一次签到时间
}
