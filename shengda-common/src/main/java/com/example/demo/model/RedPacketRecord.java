package com.example.demo.model;

import lombok.Data;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 红包记录实体类
 */
@Data
public class RedPacketRecord {
    private Long id;                    // 记录ID
    private String packetNo;            // 红包编号
    private String userName;            // 抢红包用户
    private BigDecimal amount;          // 抢到金额
    private Date grabTime;              // 抢红包时间
}
