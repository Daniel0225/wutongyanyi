package com.yiheoline.qcloud.xiaozhibo.bean;

public class RechargeBean {
    private double money;
    private String payTime;
    private int payPlatform;
    private int state;
    private int topUpId;
    private int wutongye;

    public double getMoney() {
        return money;
    }

    public void setMoney(double money) {
        this.money = money;
    }

    public String getPayTime() {
        return payTime;
    }

    public void setPayTime(String payTime) {
        this.payTime = payTime;
    }

    public int getPayPlatform() {
        return payPlatform;
    }

    public void setPayPlatform(int payPlatform) {
        this.payPlatform = payPlatform;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public int getTopUpId() {
        return topUpId;
    }

    public void setTopUpId(int topUpId) {
        this.topUpId = topUpId;
    }

    public int getWutongye() {
        return wutongye;
    }

    public void setWutongye(int wutongye) {
        this.wutongye = wutongye;
    }
}
