package com.example.demo.model;

import lombok.Data;
import java.sql.Timestamp;

/**
 * 私信消息
 */
@Data
public class PrivateMessage {
    private Integer messageId;          // 消息 ID
    private String senderName;          // 发送者用户名
    private String receiverName;        // 接收者用户名
    private String messageContent;      // 消息内容
    private String messageType;         // 消息类型：text-文本，image-图片，product-商品链接
    private Integer productId;          // 关联商品 ID
    private Integer isRead;             // 是否已读：0-未读，1-已读
    private Timestamp sendTime;         // 发送时间
}
