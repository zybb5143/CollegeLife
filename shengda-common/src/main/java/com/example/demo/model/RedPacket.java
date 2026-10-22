package com.example.demo.model;

import lombok.Data;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 红包实体类
 */
@Data
public class RedPacket {
    private Long id;                    // 红包ID
    private String packetNo;            // 红包编号
    private BigDecimal totalAmount;     // 总金额
    private Integer totalCount;         // 总个数
    private Integer remainCount;        // 剩余个数
    private BigDecimal remainAmount;    // 剩余金额
    private String publisher;           // 发布者
    private Date createTime;            // 创建时间
    private Date expireTime;            // 过期时间
    private Integer status;             // 状态：0-进行中，1-已结束
}
