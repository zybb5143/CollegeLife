package com.example.demo.service;

import com.example.demo.mapper.PrivateMessageMapper;
import com.example.demo.model.PrivateMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 私信消息服务
 */
@Service
public class PrivateMessageService {
    
    @Autowired
    private PrivateMessageMapper privateMessageMapper;
    
    /**
     * 发送消息
     */
    @Transactional
    public boolean sendMessage(PrivateMessage message) {
        message.setIsRead(0);
        return privateMessageMapper.insert(message) > 0;
    }
    
    /**
     * 查询两人之间的聊天记录
     */
    public List<PrivateMessage> getConversation(String user1, String user2) {
        return privateMessageMapper.getConversation(user1, user2);
    }
    
    /**
     * 查询用户的对话列表
     */
    public List<PrivateMessage> getUserConversations(String userName) {
        return privateMessageMapper.getUserConversations(userName);
    }
    
    /**
     * 获取未读消息数量
     */
    public int getUnreadCount(String receiverName) {
        return privateMessageMapper.getUnreadCount(receiverName);
    }
    
    /**
     * 标记消息为已读
     */
    @Transactional
    public boolean markAsRead(Integer messageId) {
        return privateMessageMapper.markAsRead(messageId) > 0;
    }
    
    /**
     * 批量标记某人的消息为已读
     */
    @Transactional
    public boolean markAllAsRead(String senderName, String receiverName) {
        return privateMessageMapper.markAllAsRead(senderName, receiverName) > 0;
    }
}
