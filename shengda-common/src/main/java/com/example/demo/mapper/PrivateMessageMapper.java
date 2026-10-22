package com.example.demo.mapper;

import com.example.demo.model.PrivateMessage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface PrivateMessageMapper {
    
    // 发送消息
    int insert(PrivateMessage message);
    
    // 查询两人之间的聊天记录（按时间正序）
    List<PrivateMessage> getConversation(@Param("user1") String user1, @Param("user2") String user2);
    
    // 查询用户的对话列表（按最近消息时间倒序）
    List<PrivateMessage> getUserConversations(@Param("userName") String userName);
    
    // 获取未读消息数量
    int getUnreadCount(@Param("receiverName") String receiverName);
    
    // 标记消息为已读
    int markAsRead(@Param("messageId") Integer messageId);
    
    // 批量标记某人的消息为已读
    int markAllAsRead(@Param("senderName") String senderName, @Param("receiverName") String receiverName);
}
