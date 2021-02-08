package com.yiheoline.qcloud.xiaozhibo.profile

import com.yiheoline.qcloud.xiaozhibo.base.BaseActivity
import com.yiheonline.qcloud.xiaozhibo.R
import kotlinx.android.synthetic.main.activity_phone.*
import kotlinx.android.synthetic.main.toolbar_layout.*
import org.jetbrains.anko.sdk27.coroutines.onClick

class PhoneActivity : BaseActivity() {

    override fun getLayoutId(): Int {
        return R.layout.activity_phone
    }

    override fun initView() {
        super.initView()
        backView.onClick { finish() }
        titleView.text = "绑定手机"

        var phoneNum = intent.getStringExtra("phoneNum")
        phoneNumView.text = phoneNum


        confirmBtn.onClick {

        }

        toCodeBindView.onClick {

        }
    }
}