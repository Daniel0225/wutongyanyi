package com.yiheoline.qcloud.xiaozhibo.http;

import java.io.Serializable;

/**
 * Created by Administrator on 2018/5/28.
 */

public class BaseResponse<T> implements Serializable {
    public T data;
    private Integer res;
    private String msg;

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public Integer getRes() {
        return res;
    }

    public void setRes(Integer res) {
        this.res = res;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }
}
