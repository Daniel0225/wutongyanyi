package com.yiheoline.qcloud.xiaozhibo.bean;

public class VideoBean {
    private int catId;
    private String desc;
    private String duration;
    private String firstCover;//4：3的封面
    private String secondCover;//16:9的封面 横的
    private String title;
    private String subtitle;
    private double price;
    private int videoId;
    private String videoPath;
    private String cover;

    public String getCover() {
        return cover;
    }

    public void setCover(String cover) {
        this.cover = cover;
    }

    public int getCatId() {
        return catId;
    }

    public void setCatId(int catId) {
        this.catId = catId;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

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

    public String getSubtitle() {
        return subtitle;
    }

    public void setSubtitle(String subtitle) {
        this.subtitle = subtitle;
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

    public String getVideoPath() {
        return videoPath;
    }

    public void setVideoPath(String videoPath) {
        this.videoPath = videoPath;
    }
}
