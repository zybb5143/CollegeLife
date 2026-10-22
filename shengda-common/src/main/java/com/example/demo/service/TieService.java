package com.example.demo.service;

import com.example.demo.feign.TieServiceClient;
import com.example.demo.model.Tie;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 帖子业务逻辑层：读写全部走 shengda-tie-service，微服务不可用时请求直接失败。
 */
@Service
public class TieService {

    @Autowired
    private TieServiceClient tieServiceClient;

    /**
     * 创建帖子
     */
    public boolean createTie(String tieTitle, String tieContent, String userName, String tieImage, String tieType, Integer productId) {
        Map<String, Object> body = new HashMap<>();
        body.put("tieTitle", tieTitle);
        body.put("tieContent", tieContent);
        body.put("userName", userName);
        body.put("tieImage", tieImage);
        body.put("tieType", tieType);
        body.put("productId", productId);
        return isSuccess(tieServiceClient.createTie(body));
    }

    /**
     * 根据用户名查询帖子
     */
    public List<Tie> getTiesByUserName(String userName) {
        return tieServiceClient.getTiesByUserName(userName);
    }

    /**
     * 查询所有帖子
     */
    public List<Tie> getAllTies() {
        return tieServiceClient.getAllTies();
    }

    /**
     * 根据 ID 查询帖子详情
     */
    public Tie getTieById(Integer tieId) {
        return tieServiceClient.getTieById(tieId);
    }

    /**
     * 更新帖子图片
     */
    public boolean updateTieImage(Integer tieId, String tieImage) {
        Map<String, Object> body = new HashMap<>();
        body.put("tieId", tieId);
        body.put("tieImage", tieImage);
        return isSuccess(tieServiceClient.updateTieImage(body));
    }

    /**
     * 获取最新的 10 个帖子
     */
    public List<Tie> getLatest10Ties() {
        return tieServiceClient.getLatest10Ties();
    }

    /**
     * 获取用户最新创建的帖子 ID
     */
    public Integer getLatestTieId(String userName) {
        return tieServiceClient.getLatestTieId(userName);
    }

    /**
     * 标记帖子为待审核状态
     */
    public boolean markTieAsPending(Integer tieId) {
        Map<String, Object> body = new HashMap<>();
        body.put("tieId", tieId);
        return isSuccess(tieServiceClient.markTieAsPending(body));
    }

    /**
     * 更新帖子标题与内容
     */
    public boolean updateTie(Integer tieId, String tieTitle, String tieContent) {
        Map<String, Object> body = new HashMap<>();
        body.put("tieId", tieId);
        body.put("tieTitle", tieTitle);
        body.put("tieContent", tieContent);
        return isSuccess(tieServiceClient.editTie(body));
    }

    /**
     * 根据 ID 删除帖子
     */
    public boolean deleteTie(Integer tieId) {
        return isSuccess(tieServiceClient.deleteTie(tieId));
    }

    private boolean isSuccess(Map<String, Object> response) {
        return response != null && Boolean.TRUE.equals(response.get("success"));
    }
}
