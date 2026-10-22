package com.example.demo.service;

import com.example.demo.mapper.ReplyMapper;
import com.example.demo.model.Reply;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 回复业务逻辑层
 */
@Service
public class ReplyService {

    @Autowired
    private ReplyMapper replyMapper;

    /**
     * 创建回复
     */
    @Transactional
    public boolean createReply(String replyContent, String userName, Integer tieId) {
        // 参数验证
        if (replyContent == null || replyContent.trim().isEmpty()) {
            throw new IllegalArgumentException("回复内容不能为空");
        }
        if (userName == null || userName.isEmpty()) {
            throw new IllegalArgumentException("用户名不能为空");
        }
        if (tieId == null || tieId <= 0) {
            throw new IllegalArgumentException("无效的帖子 ID");
        }

        String createTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        int result = replyMapper.insertReply(replyContent.trim(), createTime, userName, tieId);
        return result > 0;
    }

    /**
     * 根据帖子 ID 查询回复列表
     */
    public List<Reply> getRepliesByTieId(Integer tieId) {
        return replyMapper.getRepliesByTieId(tieId);
    }
}
