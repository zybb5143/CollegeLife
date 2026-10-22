package com.example.demo.model;

import lombok.Data;
import java.math.BigDecimal;
import java.sql.Timestamp;

/**
 * 二手商品
 */
@Data
public class SecondHandProduct {
    private Integer productId;          // 商品 ID
    private String userName;            // 卖家用户名
    private String productName;         // 商品名称
    private String productDescription;  // 商品描述
    private String productCondition;    // 几成新
    private BigDecimal price;           // 价格
    private BigDecimal originalPrice;   // 原价
    private String productImages;       // 商品图片 URL（多张逗号分隔）
    private String category;            // 商品分类
    private String status;              // 状态：on_sale-在售，sold-已售出，reserved-已预订
    private Integer viewCount;          // 浏览次数
    private Integer wantCount;          // 想要人数
    private Timestamp createTime;       // 发布时间
    private Timestamp updateTime;       // 更新时间
}
