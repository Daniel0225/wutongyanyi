package com.yiheoline.qcloud.xiaozhibo.bean;

public class GiftRecord {
    private String createTime;
    private String  name;
    private String nickname;
    private int giftRecordId;
    private int type;
    private int wutongye;
    private String avatar;

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public int getGiftRecordId() {
        return giftRecordId;
    }

    public void setGiftRecordId(int giftRecordId) {
        this.giftRecordId = giftRecordId;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getWutongye() {
        return wutongye;
    }

    public void setWutongye(int wutongye) {
        this.wutongye = wutongye;
    }
}
