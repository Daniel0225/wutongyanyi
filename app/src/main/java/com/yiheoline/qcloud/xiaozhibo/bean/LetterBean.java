package com.yiheoline.qcloud.xiaozhibo.bean;

public class LetterBean {
    private String content;
    private int letterId;
    private String title;
    private int userLetterId;
    private String createTime;
    private String param;
    private int readState;//已读状态: 0:未读 1:已读
    private int type;//类型 1:普通消息 2:打开特定的activity 3:跳转url
    private String activity;

    public String getActivity() {
        return activity;
    }

    public void setActivity(String activity) {
        this.activity = activity;
    }

    public String getParam() {
        return param;
    }

    public void setParam(String param) {
        this.param = param;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

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
