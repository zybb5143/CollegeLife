package com.example.demo.mapper;

import com.example.demo.model.RedPacketRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface RedPacketRecordMapper {
    
    // 插入领取记录
    int insertRecord(RedPacketRecord record);
    
    // 检查用户是否已抢过该红包
    int countByPacketNoAndUserName(@Param("packetNo") String packetNo, @Param("userName") String userName);
    
    // 查询用户的领取记录
    List<RedPacketRecord> selectByUserName(@Param("userName") String userName);
    
    // 查询红包的领取记录
    List<RedPacketRecord> selectByPacketNo(@Param("packetNo") String packetNo);
}
