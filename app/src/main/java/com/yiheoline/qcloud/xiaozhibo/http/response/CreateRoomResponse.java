package com.yiheoline.qcloud.xiaozhibo.http.response;


public class CreateRoomResponse {
    private String pushURL;
    private String roomID;

    public String getPushURL() {
        return pushURL;
    }

    public void setPushURL(String pushURL) {
        this.pushURL = pushURL;
    }

    public String getRoomID() {
        return roomID;
    }

    public void setRoomID(String roomID) {
        this.roomID = roomID;
    }
}
