package com.yiheoline.qcloud.xiaozhibo.http.response;


public class APPidResponse {
    private String sdkAppID;
    private String token;
    private String userID;
    private String userSig;

    public String getSdkAppID() {
        return sdkAppID;
    }

    public void setSdkAppID(String sdkAppID) {
        this.sdkAppID = sdkAppID;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public String getUserSig() {
        return userSig;
    }

    public void setUserSig(String userSig) {
        this.userSig = userSig;
    }
}
