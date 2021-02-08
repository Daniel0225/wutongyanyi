package com.yiheoline.qcloud.xiaozhibo.bean;

import java.io.Serializable;

public class ShowNoticeDetailBean implements Serializable {
    private int catId;
    private String detail;
    private String imageDetail;
    private String liveTime;
    private String title;
    private String videoDetail;
    private int duration;
    private int noticeId;
    private double price;
    private String cover;
    private int liveState;
    private String isIntent;
    private int isNeedPay;//是否需要付费, 0:不需要 1:需要付费

    public int getIsNeedPay() {
        return isNeedPay;
    }

    public void setIsNeedPay(int isNeedPay) {
        this.isNeedPay = isNeedPay;
    }

    public int getLiveState() {
        return liveState;
    }

    public void setLiveState(int liveState) {
        this.liveState = liveState;
    }

    public String getIsIntent() {
        return isIntent;
    }

    public void setIsIntent(String isIntent) {
        this.isIntent = isIntent;
    }

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

    public String getDetail() {
        return detail;
    }

    public void setDetail(String detail) {
        this.detail = detail;
    }

    public String getImageDetail() {
        return imageDetail;
    }

    public void setImageDetail(String imageDetail) {
        this.imageDetail = imageDetail;
    }

    public String getLiveTime() {
        return liveTime;
    }

    public void setLiveTime(String liveTime) {
        this.liveTime = liveTime;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getVideoDetail() {
        return videoDetail;
    }

    public void setVideoDetail(String videoDetail) {
        this.videoDetail = videoDetail;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public int getNoticeId() {
        return noticeId;
    }

    public void setNoticeId(int noticeId) {
        this.noticeId = noticeId;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }
}
