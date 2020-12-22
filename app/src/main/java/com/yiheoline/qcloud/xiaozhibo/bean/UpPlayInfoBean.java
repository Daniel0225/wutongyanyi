package com.yiheoline.qcloud.xiaozhibo.bean;

import java.io.Serializable;

public class UpPlayInfoBean implements Serializable {
    private long likeCount;
    private long watchCount;
    private int theaterLiveId;
    private int noticeId;

    public int getNoticeId() {
        return noticeId;
    }

    public void setNoticeId(int noticeId) {
        this.noticeId = noticeId;
    }

    public long getLikeCount() {
        return likeCount;
    }

    public void setLikeCount(long likeCount) {
        this.likeCount = likeCount;
    }

    public long getWatchCount() {
        return watchCount;
    }

    public void setWatchCount(long watchCount) {
        this.watchCount = watchCount;
    }

    public int getTheaterLiveId() {
        return theaterLiveId;
    }

    public void setTheaterLiveId(int theaterLiveId) {
        this.theaterLiveId = theaterLiveId;
    }
}
