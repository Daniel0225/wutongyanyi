package com.yiheoline.qcloud.xiaozhibo.http.response;

import com.yiheoline.qcloud.xiaozhibo.bean.LikeShowBean;
import com.yiheoline.qcloud.xiaozhibo.bean.LikeVideoBean;

import java.util.List;

public class LikeVideoResponse {
    private List<LikeVideoBean> list;

    private int total;
    private int pageNum;
    private int pages;

    public List<LikeVideoBean> getList() {
        return list;
    }

    public void setList(List<LikeVideoBean> list) {
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
