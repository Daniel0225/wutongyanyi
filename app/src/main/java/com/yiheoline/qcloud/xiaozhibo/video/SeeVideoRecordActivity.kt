package com.yiheoline.qcloud.xiaozhibo.video

import com.yiheoline.qcloud.xiaozhibo.base.BaseActivity
import com.yiheonline.qcloud.xiaozhibo.R
import kotlinx.android.synthetic.main.toolbar_layout.*

class SeeVideoRecordActivity : BaseActivity() {

    override fun getLayoutId(): Int {
        return R.layout.activity_see_video_record
    }

    override fun initView() {
        super.initView()
        titleView.text ="观影记录"
    }
}