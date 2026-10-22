package com.example.demo.controller;

import com.example.demo.model.Reply;
import com.example.demo.service.ReplyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reply")
public class ReplyController {

    @Autowired
    private ReplyService replyService;

    @PostMapping("/create")
    public String createReply(@RequestParam("replyContent") String replyContent,
                              @RequestParam("userName") String userName,
                              @RequestParam("tieId") Integer tieId) {
        try {
            replyService.createReply(replyContent, userName, tieId);

            return "回复成功！";
        } catch (IllegalArgumentException e) {
            return "回复失败：" + e.getMessage();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("回复失败：" + e.getMessage());
        }
    }

    @GetMapping("/list")
    public List<Reply> getReplies(@RequestParam("tieId") Integer tieId) {
        try {
            return replyService.getRepliesByTieId(tieId);
        } catch (Exception e) {
            throw new RuntimeException("查询回复失败：" + e.getMessage());
        }
    }
}
