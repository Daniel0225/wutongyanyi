package com.yiheoline.qcloud.xiaozhibo.widgets;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

import com.shuyu.gsyvideoplayer.video.StandardGSYVideoPlayer;
import com.yiheonline.qcloud.xiaozhibo.R;

public class EmptyVideoPlayer extends StandardGSYVideoPlayer {

    public EmptyVideoPlayer(Context context, Boolean fullFlag) {
        super(context, fullFlag);
    }

    public EmptyVideoPlayer(Context context) {
        super(context);
    }

    public EmptyVideoPlayer(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void touchDoubleUp() {
//        super.touchDoubleUp();
    }

    @Override
    public int getLayoutId() {
        return R.layout.video_layout_empty;
    }

}
