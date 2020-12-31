package com.yiheoline.qcloud.xiaozhibo.http.response;

import com.yiheoline.qcloud.xiaozhibo.bean.AdBean;
import com.yiheoline.qcloud.xiaozhibo.bean.VideoBean;

import java.util.List;

public class VideoChoiceResponse {
    private List<AdBean> advertList;
    private List<VideoBean> hotList;
    private List<VideoBean> latelyList;

    public List<AdBean> getAdvertList() {
        return advertList;
    }

    public void setAdvertList(List<AdBean> advertList) {
        this.advertList = advertList;
    }

    public List<VideoBean> getHotList() {
        return hotList;
    }

    public void setHotList(List<VideoBean> hotList) {
        this.hotList = hotList;
    }

    public List<VideoBean> getLatelyList() {
        return latelyList;
    }

    public void setLatelyList(List<VideoBean> latelyList) {
        this.latelyList = latelyList;
    }
}
