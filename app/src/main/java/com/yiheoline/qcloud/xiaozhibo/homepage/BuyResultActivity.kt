package com.yiheoline.qcloud.xiaozhibo.homepage

import com.yiheoline.qcloud.xiaozhibo.base.BaseActivity
import com.yiheoline.qcloud.xiaozhibo.bean.ShowNoticeDetailBean
import com.yiheoline.qcloud.xiaozhibo.main.TCMainActivity
import com.yiheoline.qcloud.xiaozhibo.utils.TimeUtil
import com.yiheonline.qcloud.xiaozhibo.R
import kotlinx.android.synthetic.main.activity_buy_result.*
import kotlinx.android.synthetic.main.activity_buy_result.dateView
import kotlinx.android.synthetic.main.activity_buy_result.noticeTitleView
import kotlinx.android.synthetic.main.toolbar_layout.*
import org.jetbrains.anko.sdk27.coroutines.onClick
import org.jetbrains.anko.startActivity

class BuyResultActivity : BaseActivity() {
    var noticeDetailBean : ShowNoticeDetailBean? = null
    override fun getLayoutId(): Int {
        return R.layout.activity_buy_result
    }

    override fun initView() {
        super.initView()
        backView.onClick { finish() }
        titleView.text = "购买结果"

        noticeDetailBean = intent.getSerializableExtra("noticeDetailBean") as ShowNoticeDetailBean

        noticeTitleView.text = noticeDetailBean?.title
        dateView.text = TimeUtil.getYearMonthAndDayWithHour(noticeDetailBean!!.liveTime.toLong())
        durationView.text = "${noticeDetailBean?.duration.toString()}分钟"

        backHomeView.onClick {
            startActivity<TCMainActivity>()
        }
    }
}