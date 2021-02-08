package com.yiheoline.qcloud.xiaozhibo.http.response;

import com.yiheoline.qcloud.xiaozhibo.bean.ShortVideoBean;
import com.yiheoline.qcloud.xiaozhibo.bean.ShowNoticeBean;
import com.yiheoline.qcloud.xiaozhibo.bean.VideoBean;

import java.util.List;

public class HomeChoiceResponse {
    private List<ShowNoticeBean> latelyList;
    private List<ShowNoticeBean> recommendList;
    private List<ShortVideoBean> shortVideoList;
    private List<ShowNoticeBean> showList;
    private List<VideoBean> videoList;

    public List<ShowNoticeBean> getLatelyList() {
        return latelyList;
    }

    public void setLatelyList(List<ShowNoticeBean> latelyList) {
        this.latelyList = latelyList;
    }

    public List<ShowNoticeBean> getRecommendList() {
        return recommendList;
    }

    public void setRecommendList(List<ShowNoticeBean> recommendList) {
        this.recommendList = recommendList;
    }

    public List<ShortVideoBean> getShortVideoList() {
        return shortVideoList;
    }

    public void setShortVideoList(List<ShortVideoBean> shortVideoList) {
        this.shortVideoList = shortVideoList;
    }

    public List<ShowNoticeBean> getShowList() {
        return showList;
    }

    public void setShowList(List<ShowNoticeBean> showList) {
        this.showList = showList;
    }

    public List<VideoBean> getVideoList() {
        return videoList;
    }

    public void setVideoList(List<VideoBean> videoList) {
        this.videoList = videoList;
    }
}
