package com.yiheoline.qcloud.xiaozhibo.bean;

import java.io.Serializable;

public class GiftUpBean implements Serializable {
    private int giftId;
    private int liveId;

    public GiftUpBean() {
    }

    public GiftUpBean(int giftId, int liveId) {
        this.giftId = giftId;
        this.liveId = liveId;
    }

    public int getGiftId() {
        return giftId;
    }

    public void setGiftId(int giftId) {
        this.giftId = giftId;
    }

    public int getLiveId() {
        return liveId;
    }

    public void setLiveId(int liveId) {
        this.liveId = liveId;
    }
}
