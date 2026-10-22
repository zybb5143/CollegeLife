package com.example.demo.model;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Tie {
    private Integer tieId;
    private String tieTitle;
    private String tieContent;
    private LocalDateTime tieCreateTime;
    private String userName;
    private String tieImage;
    private String tieType; // academic-学术帖，product-商品帖
    private Integer productId; // 关联商品ID
}
