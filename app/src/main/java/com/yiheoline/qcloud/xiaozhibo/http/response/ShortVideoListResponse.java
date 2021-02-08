package com.yiheoline.qcloud.xiaozhibo.http.response;

import com.yiheoline.qcloud.xiaozhibo.bean.ShortVideoBean;

import java.util.List;

public class ShortVideoListResponse {
    private List<ShortVideoBean> list;

    private int total;
    private int pageNum;
    private int pages;

    public List<ShortVideoBean> getList() {
        return list;
    }

    public void setList(List<ShortVideoBean> list) {
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
