package com.yiheoline.qcloud.xiaozhibo.http.response;

import com.yiheoline.qcloud.xiaozhibo.bean.GiftRecord;
import com.yiheoline.qcloud.xiaozhibo.bean.WithDrawBean;

import java.util.List;

public class WithDrawResponse {
    private List<WithDrawBean> list;

    private int total;
    private int pageNum;
    private int pages;

    public List<WithDrawBean> getList() {
        return list;
    }

    public void setList(List<WithDrawBean> list) {
        this.list = list;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public int getPageNum() {
        return pageNum;
    }

    public void setPageNum(int pageNum) {
        this.pageNum = pageNum;
    }

    public int getPages() {
        return pages;
    }

    public void setPages(int pages) {
        this.pages = pages;
    }
}
