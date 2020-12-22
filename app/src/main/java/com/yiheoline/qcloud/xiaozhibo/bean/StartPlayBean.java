package com.yiheoline.qcloud.xiaozhibo.bean;

import java.io.Serializable;

public class StartPlayBean implements Serializable {
    private String roomId;
    private int noticeId;

    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }

    public int getNoticeId() {
        return noticeId;
    }

    public void setNoticeId(int noticeId) {
        this.noticeId = noticeId;
    }
}
