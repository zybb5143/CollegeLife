package com.example.demo.model;

import lombok.Data;
import java.math.BigDecimal;
import java.sql.Timestamp;

/**
 * 订单
 */
@Data
public class ProductOrder {
    private Integer orderId;            // 订单 ID
    private String orderNo;             // 订单编号
    private Integer productId;          // 商品 ID
    private String sellerName;          // 卖家用户名
    private String buyerName;           // 买家用户名
    private String productName;         // 商品名称
    private String productImage;        // 商品主图
    private BigDecimal price;           // 成交价格
    private String status;              // 订单状态：pending_payment-待支付，paid-已支付，completed-已完成，cancelled-已取消
    private Timestamp paymentTime;      // 支付时间
    private Timestamp completeTime;     // 完成时间
    private String cancelReason;        // 取消原因
    private Timestamp createTime;       // 下单时间
    private Timestamp updateTime;       // 更新时间
}
