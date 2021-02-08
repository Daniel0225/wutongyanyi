package com.yiheoline.qcloud.xiaozhibo.bean;

public class WithDrawBean {
    private String alipayAccount;
    private String money;
    private int state;
    private int withdrawId;
    private int wutongbi;
    private String createTime;

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public String getAlipayAccount() {
        return alipayAccount;
    }

    public void setAlipayAccount(String alipayAccount) {
        this.alipayAccount = alipayAccount;
    }

    public String getMoney() {
        return money;
    }

    public void setMoney(String money) {
        this.money = money;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public int getWithdrawId() {
        return withdrawId;
    }

    public void setWithdrawId(int withdrawId) {
        this.withdrawId = withdrawId;
    }

    public int getWutongbi() {
        return wutongbi;
    }

    public void setWutongbi(int wutongbi) {
        this.wutongbi = wutongbi;
    }
}
