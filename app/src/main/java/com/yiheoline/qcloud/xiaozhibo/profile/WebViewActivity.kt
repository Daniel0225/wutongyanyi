package com.yiheoline.qcloud.xiaozhibo.profile

import com.yiheoline.qcloud.xiaozhibo.base.BaseActivity
import com.yiheonline.qcloud.xiaozhibo.R
import kotlinx.android.synthetic.main.activity_web_view.*
import kotlinx.android.synthetic.main.toolbar_layout.*


class WebViewActivity : BaseActivity() {
    override fun getLayoutId(): Int {
        return R.layout.activity_web_view
    }

    override fun initView() {
        super.initView()
        titleView.text = ""
        var url = intent.getStringExtra("url")
        var webSettings = webView.settings
        webSettings.javaScriptEnabled = true
        webView.loadUrl(url)
    }

}
