package com.yiheoline.qcloud.xiaozhibo.bean;

public class HeartBean {
    private long watchCount;
    private int liveId;
    private long likeCount;

    public HeartBean() {
    }

    public HeartBean(long watchCount, int liveId, long likeCount) {
        this.watchCount = watchCount;
        this.liveId = liveId;
        this.likeCount = likeCount;
    }

    public long getWatchCount() {
        return watchCount;
    }

    public void setWatchCount(long watchCount) {
        this.watchCount = watchCount;
    }

    public int getLiveId() {
        return liveId;
    }

    public void setLiveId(int liveId) {
        this.liveId = liveId;
    }

    public long getLikeCount() {
        return likeCount;
    }

    public void setLikeCount(long likeCount) {
        this.likeCount = likeCount;
    }
}
