package com.yiheoline.qcloud.xiaozhibo.bean;

public class SeeRecordBean {
    private String cover;
    private String firstCover;
    private String secondCover;
    private String title;
    private double price;
    private int videoId;

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

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public int getVideoId() {
        return videoId;
    }

    public void setVideoId(int videoId) {
        this.videoId = videoId;
    }

    public String getCover() {
        return cover;
    }

    public void setCover(String cover) {
        this.cover = cover;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
