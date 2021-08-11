package com.yiheoline.qcloud.xiaozhibo.bean;

public class VersionInfo {
    private int versionCode;
    private String versionName;
    private int forceFlag;//0非强制  1 强制升级
    private int minVersionCode;
    private String publishDate;
    private String downloadUrl;
    private String versionDesc;

    public int getVersionCode() {
        return versionCode;
    }

    public void setVersionCode(int versionCode) {
        this.versionCode = versionCode;
    }

    public String getVersionName() {
        return versionName;
    }

    public void setVersionName(String versionName) {
        this.versionName = versionName;
    }

    public int getForceFlag() {
        return forceFlag;
    }

    public void setForceFlag(int forceFlag) {
        this.forceFlag = forceFlag;
    }

    public int getMinVersionCode() {
        return minVersionCode;
    }

    public void setMinVersionCode(int minVersionCode) {
        this.minVersionCode = minVersionCode;
    }

    public String getPublishDate() {
        return publishDate;
    }

    public void setPublishDate(String publishDate) {
        this.publishDate = publishDate;
    }

    public String getDownloadUrl() {
        return downloadUrl;
    }

    public void setDownloadUrl(String downloadUrl) {
        this.downloadUrl = downloadUrl;
    }

    public String getVersionDesc() {
        return versionDesc;
    }

    public void setVersionDesc(String versionDesc) {
        this.versionDesc = versionDesc;
    }
}
