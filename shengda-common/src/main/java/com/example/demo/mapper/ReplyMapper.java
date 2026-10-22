package com.example.demo.mapper;

import com.example.demo.model.Reply;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ReplyMapper {

    int insertReply(@Param("replyContent") String replyContent,
                    @Param("replyCreateTime") String replyCreateTime,
                    @Param("userName") String userName,
                    @Param("tieId") Integer tieId);

    List<Reply> getRepliesByTieId(@Param("tieId") Integer tieId);
}
