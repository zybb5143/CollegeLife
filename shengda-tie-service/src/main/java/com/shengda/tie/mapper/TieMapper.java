package com.shengda.tie.mapper;

import com.shengda.tie.model.Tie;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 帖子 Mapper（从 shengda-common 迁移，逻辑不变）
 */
@Mapper
public interface TieMapper {

    /** 插入帖子 */
    int insertTie(Tie tie);

    /** 获取最后插入的帖子ID */
    Integer getLastInsertId();

    /** 根据用户名查询帖子 */
    List<Tie> getTiesByUserName(@Param("userName") String userName);

    /** 查询所有帖子 */
    List<Tie> getAllTies();

    /** 根据 ID 查询帖子详情 */
    Tie getTieById(@Param("tieId") Integer tieId);

    /** 更新帖子图片 */
    int updateTieImage(@Param("tieId") Integer tieId, @Param("tieImage") String tieImage);

    /** 获取最新的 10 个帖子 */
    List<Tie> getLatest10Ties();

    /** 获取用户最新创建的帖子 ID */
    Integer getLatestTieId(@Param("userName") String userName);

    /** 标记帖子为待审核状态 */
    int markTieAsPending(@Param("tieId") Integer tieId);

    /** 更新帖子标题与内容 */
    int updateTieContent(@Param("tieId") Integer tieId,
                         @Param("tieTitle") String tieTitle,
                         @Param("tieContent") String tieContent);

    /** 根据 ID 删除帖子 */
    int deleteTie(@Param("tieId") Integer tieId);
}
