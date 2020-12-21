package com.yiheoline.qcloud.xiaozhibo.bean;

import java.io.Serializable;

public class TagBean implements Serializable {
    private String name;
    private int tagId;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getTagId() {
        return tagId;
    }

    public void setTagId(int tagId) {
        this.tagId = tagId;
    }
}
