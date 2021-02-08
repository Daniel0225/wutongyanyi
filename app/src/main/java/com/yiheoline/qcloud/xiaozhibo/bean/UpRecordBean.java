package com.yiheoline.qcloud.xiaozhibo.bean;

public class UpRecordBean {
    private String firstCover;
    private String secondCover;
    private String title;
    private int purchases;
    private int relationId;
    private int views;

    public String getFirstCover() {
        return firstCover;
    }

    public void setFirstCover(String firstCover) {
        this.firstCover = firstCover;
    }

    public String getSecondCover() {
        return secondCover;
    }

    public void setSecondCover(String secondCover) {
        this.secondCover = secondCover;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getPurchases() {
        return purchases;
    }

    public void setPurchases(int purchases) {
        this.purchases = purchases;
    }

    public int getRelationId() {
        return relationId;
    }

    public void setRelationId(int relationId) {
        this.relationId = relationId;
    }

    public int getViews() {
        return views;
    }

    public void setViews(int views) {
        this.views = views;
    }
}
