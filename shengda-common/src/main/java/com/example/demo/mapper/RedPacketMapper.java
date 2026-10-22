package com.example.demo.mapper;

import com.example.demo.model.RedPacket;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface RedPacketMapper {
    
    // 插入红包
    int insertRedPacket(RedPacket redPacket);
    
    // 根据红包编号查询
    RedPacket selectByPacketNo(@Param("packetNo") String packetNo);
    
    // 更新剩余数量和金额
    int updateRemainCountAndAmount(@Param("packetNo") String packetNo, 
                                    @Param("remainCount") Integer remainCount,
                                    @Param("remainAmount") java.math.BigDecimal remainAmount);
    
    // 更新状态
    int updateStatus(@Param("packetNo") String packetNo, @Param("status") Integer status);

    // 原子扣减剩余数量和剩余金额（避免先查后写的丢失更新）
    int decrementRemain(@Param("packetNo") String packetNo,
                        @Param("amount") java.math.BigDecimal amount);

    // 若剩余数量已为 0，则将状态置为已结束
    int finishIfEmpty(@Param("packetNo") String packetNo);
}
