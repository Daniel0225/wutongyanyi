package com.yiheoline.qcloud.xiaozhibo.bean;

import java.io.Serializable;

public class TypeBean implements Serializable {
    private String name;
    private int catId;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getCatId() {
        return catId;
    }

    public void setCatId(int catId) {
        this.catId = catId;
    }
}
