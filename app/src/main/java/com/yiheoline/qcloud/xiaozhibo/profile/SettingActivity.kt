package com.yiheoline.qcloud.xiaozhibo.profile

import android.content.Intent
import com.yiheoline.qcloud.xiaozhibo.base.BaseActivity
import com.yiheoline.qcloud.xiaozhibo.login.OneKeyLoginActivity
import com.yiheonline.qcloud.xiaozhibo.R
import kotlinx.android.synthetic.main.activity_setting.*
import kotlinx.android.synthetic.main.toolbar_layout.*
import org.jetbrains.anko.sdk27.coroutines.onClick
import org.jetbrains.anko.startActivity

class SettingActivity : BaseActivity() {

    override fun getLayoutId(): Int {
        return R.layout.activity_setting
    }

    override fun initView() {
        super.initView()
        backView.onClick { finish() }
        titleView.text = "设置"
        logoutBtn.onClick {
            var intent = Intent(this@SettingActivity,OneKeyLoginActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            finish()
        }

    }
}