package com.yiheoline.qcloud.xiaozhibo.http.response;

import com.yiheoline.qcloud.xiaozhibo.bean.ShowNoticeBean;

import java.util.List;

public class HomePageResponse {
    private List<ShowNoticeBean> list;
    private int pageNum;

    public List<ShowNoticeBean> getList() {
        return list;
    }

    public void setList(List<ShowNoticeBean> list) {
        this.list = list;
    }

    public int getPageNum() {
        return pageNum;
    }

    public void setPageNum(int pageNum) {
        this.pageNum = pageNum;
    }
}
