package com.yiheoline.qcloud.xiaozhibo.bean;

import java.io.Serializable;

public class RoomInfoBean implements Serializable {
    private String mixedPlayURL;
    private String roomID;

    public String getMixedPlayURL() {
        return mixedPlayURL;
    }

    public void setMixedPlayURL(String mixedPlayURL) {
        this.mixedPlayURL = mixedPlayURL;
    }

    public String getRoomID() {
        return roomID;
    }

    public void setRoomID(String roomID) {
        this.roomID = roomID;
    }
}
