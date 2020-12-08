package com.yiheoline.qcloud.xiaozhibo.widgets;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;


/**
 * Created by Administrator on 2019/12/27.
 */

public class NoAnimationViewPager extends ViewPager {

    public NoAnimationViewPager(Context context) {
        super(context);
    }

    public NoAnimationViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void setCurrentItem(int item, boolean smoothScroll) {
        super.setCurrentItem(item, smoothScroll);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return false;//直接返回false不进行事件拦截
    }


    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        return false;//直接返回false不进行事件消费
    }


    /**
     *调用父类两个参数的方法，传入false,禁止切换动画
     * @param item
     */
    @Override
    public void setCurrentItem(int item) {
        super.setCurrentItem(item, false);//传入false禁止切换动画
    }
}

