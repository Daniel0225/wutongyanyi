package com.yiheoline.qcloud.xiaozhibo.profile

import android.app.Activity
import android.content.Intent
import com.yiheoline.qcloud.xiaozhibo.base.BaseActivity
import com.yiheonline.qcloud.xiaozhibo.R
import kotlinx.android.synthetic.main.activity_change_account.*
import kotlinx.android.synthetic.main.toolbar_layout.*
import org.jetbrains.anko.sdk27.coroutines.onClick
import org.jetbrains.anko.toast

class ChangeAccountActivity : BaseActivity() {

    override fun getLayoutId(): Int {
        return R.layout.activity_change_account
    }

    override fun initView() {
        super.initView()
        backView.onClick { finish() }
        titleView.text = ""
        var account = intent.getStringExtra("account")
        originAccountView.text = account
    }

    override fun initListener() {
        super.initListener()
        confirmBtn.onClick {
            var newAccount = accountInputView.text.toString()
            if(newAccount.isEmpty()){
                toast("新账号不能为空")
                return@onClick
            }
            var intent = Intent()
            intent.putExtra("newAccount",newAccount)
            setResult(Activity.RESULT_OK,intent)
            finish()
        }
    }

}