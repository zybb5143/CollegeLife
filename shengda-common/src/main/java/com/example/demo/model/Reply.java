package com.example.demo.model;

import lombok.Data;

@Data
public class Reply {
    private Integer replyId;
    private String replyContent;
    private String replyCreateTime;
    private String userName;
    private Integer tieId;
}
