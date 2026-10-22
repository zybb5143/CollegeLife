package com.example.demo.mapper;


import com.example.demo.model.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.sql.Date;
import java.sql.Timestamp;
import java.util.List;

@Mapper
public interface UserMapper {
    //查
    User getUserByUserName(String userName);

    //改
    int updateUser(@Param("user_name") String userName, String userPassword, String userEmail, String userGender);

    //增
    int insertUser(String userName, String userPassword, String userEmail, String userGender);

    //删
    int deleteUser(@Param("userName") String userName);

    //签到相关
    int updateCheckinInfo(@Param("userName") String userName, @Param("continuousDays") Integer continuousDays, @Param("lastDate") Timestamp lastDate);

    //获取所有用户名
    List<String> getAllUserNames();
}
