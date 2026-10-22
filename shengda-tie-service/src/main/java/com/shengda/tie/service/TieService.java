package com.shengda.tie.service;

import com.shengda.tie.mapper.TieMapper;
import com.shengda.tie.model.Tie;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 帖子业务逻辑层（从 shengda-common 迁移，逻辑不变）
 */
@Service
public class TieService {

    @Autowired
    private TieMapper tieMapper;

    /**
     * 创建帖子
     */
    @Transactional
    public boolean createTie(String tieTitle, String tieContent, String userName, String tieImage, String tieType, Integer productId) {
        Tie tie = new Tie();
        tie.setTieTitle(tieTitle);
        tie.setTieContent(tieContent);
        tie.setUserName(userName);
        tie.setTieCreateTime(LocalDateTime.now());
        tie.setTieImage(tieImage);
        tie.setTieType(tieType);
        tie.setProductId(productId);

        int result = tieMapper.insertTie(tie);
        return result > 0;
    }

    /**
     * 根据用户名查询帖子
     */
    public List<Tie> getTiesByUserName(String userName) {
        return tieMapper.getTiesByUserName(userName);
    }

    /**
     * 查询所有帖子
     */
    public List<Tie> getAllTies() {
        return tieMapper.getAllTies();
    }

    /**
     * 根据 ID 查询帖子详情
     */
    public Tie getTieById(Integer tieId) {
        return tieMapper.getTieById(tieId);
    }

    /**
     * 更新帖子图片
     */
    @Transactional
    public boolean updateTieImage(Integer tieId, String tieImage) {
        int result = tieMapper.updateTieImage(tieId, tieImage);
        return result > 0;
    }

    /**
     * 获取最新的 10 个帖子
     */
    public List<Tie> getLatest10Ties() {
        return tieMapper.getLatest10Ties();
    }

    /**
     * 获取用户最新创建的帖子 ID
     */
    public Integer getLatestTieId(String userName) {
        return tieMapper.getLatestTieId(userName);
    }

    /**
     * 标记帖子为待审核状态
     */
    @Transactional
    public boolean markTieAsPending(Integer tieId) {
        int result = tieMapper.markTieAsPending(tieId);
        return result > 0;
    }

    /**
     * 更新帖子标题与内容
     */
    @Transactional
    public boolean updateTie(Integer tieId, String tieTitle, String tieContent) {
        int result = tieMapper.updateTieContent(tieId, tieTitle, tieContent);
        return result > 0;
    }

    /**
     * 根据 ID 删除帖子
     */
    @Transactional
    public boolean deleteTie(Integer tieId) {
        int result = tieMapper.deleteTie(tieId);
        return result > 0;
    }
}
