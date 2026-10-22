package com.shengda.tie.model;

import java.time.LocalDateTime;

/**
 * 帖子实体（从 shengda-common 迁移，去掉 lombok 改为普通 POJO，保证独立可编译）
 */
public class Tie {
    private Integer tieId;
    private String tieTitle;
    private String tieContent;
    private LocalDateTime tieCreateTime;
    private String userName;
    private String tieImage;
    private String tieType; // academic-学术帖，product-商品帖
    private Integer productId; // 关联商品ID

    public Integer getTieId() {
        return tieId;
    }

    public void setTieId(Integer tieId) {
        this.tieId = tieId;
    }

    public String getTieTitle() {
        return tieTitle;
    }

    public void setTieTitle(String tieTitle) {
        this.tieTitle = tieTitle;
    }

    public String getTieContent() {
        return tieContent;
    }

    public void setTieContent(String tieContent) {
        this.tieContent = tieContent;
    }

    public LocalDateTime getTieCreateTime() {
        return tieCreateTime;
    }

    public void setTieCreateTime(LocalDateTime tieCreateTime) {
        this.tieCreateTime = tieCreateTime;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getTieImage() {
        return tieImage;
    }

    public void setTieImage(String tieImage) {
        this.tieImage = tieImage;
    }

    public String getTieType() {
        return tieType;
    }

    public void setTieType(String tieType) {
        this.tieType = tieType;
    }

    public Integer getProductId() {
        return productId;
    }

    public void setProductId(Integer productId) {
        this.productId = productId;
    }
}
