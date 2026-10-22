package com.example.demo.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Mapper
public interface RedPacketAmountMapper {
    
    // 批量插入红包金额
    int batchInsertAmounts(@Param("packetNo") String packetNo, @Param("amounts") List<BigDecimal> amounts);
    
    // 查询未抢的金额（按ID顺序获取第一个）
    BigDecimal selectFirstUngrabbedAmount(@Param("packetNo") String packetNo);

    // 加锁查询第一个未抢的金额行（SELECT ... FOR UPDATE），返回 id 与 amount
    Map<String, Object> selectFirstUngrabbedForUpdate(@Param("packetNo") String packetNo);

    // 按主键 id 标记为已抢（配合 FOR UPDATE 精确锁定单行）
    int markAsGrabbedById(@Param("id") Long id);
    
    // 标记金额为已抢
    int markAsGrabbed(@Param("packetNo") String packetNo, @Param("amount") BigDecimal amount);
    
    // 统计未抢的数量
    int countUngrabbed(@Param("packetNo") String packetNo);
}
