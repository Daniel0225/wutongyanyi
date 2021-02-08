package com.yiheoline.qcloud.xiaozhibo.bean;

public class LetterBean {
    private String content;
    private int letterId;
    private int readState;
    private String title;
    private int userLetterId;
    private String createTime;

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public int getLetterId() {
        return letterId;
    }

    public void setLetterId(int letterId) {
        this.letterId = letterId;
    }

    public int getReadState() {
        return readState;
    }

    public void setReadState(int readState) {
        this.readState = readState;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getUserLetterId() {
        return userLetterId;
    }

    public void setUserLetterId(int userLetterId) {
        this.userLetterId = userLetterId;
    }
}
