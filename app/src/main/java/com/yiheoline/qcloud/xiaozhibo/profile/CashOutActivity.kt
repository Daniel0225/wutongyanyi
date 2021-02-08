package com.yiheoline.qcloud.xiaozhibo.profile

import android.content.Intent
import android.view.View
import com.lzy.okgo.OkGo
import com.lzy.okgo.model.HttpParams
import com.lzy.okgo.model.Response
import com.tencent.mmkv.MMKV
import com.yiheoline.qcloud.xiaozhibo.Constant
import com.yiheoline.qcloud.xiaozhibo.base.BaseActivity
import com.yiheoline.qcloud.xiaozhibo.http.BaseResponse
import com.yiheoline.qcloud.xiaozhibo.http.JsonCallBack
import com.yiheonline.qcloud.xiaozhibo.R
import kotlinx.android.synthetic.main.activity_cash_out.*
import kotlinx.android.synthetic.main.toolbar_layout.*
import org.jetbrains.anko.sdk27.coroutines.onClick
import org.jetbrains.anko.startActivity
import org.jetbrains.anko.toast

class CashOutActivity : BaseActivity() {
    var total = 0
    override fun getLayoutId(): Int {
        return R.layout.activity_cash_out
    }

    override fun initData() {
        super.initData()
        total = intent.getIntExtra("total",0)
    }

    override fun initView() {
        super.initView()
        backView.onClick { finish() }
        titleView.text = "提现"
        rightBtn.visibility = View.VISIBLE
        rightBtn.text = "提现记录"
        rightBtn.onClick { startActivity<RechargeRecordActivity>("type" to 2) }
        totalView.text = total.toString()
        aboutView.text = "约${(total/15)}元"
        defaultAccountView.text = MMKV.defaultMMKV().decodeString("loginName")
    }

    override fun initListener() {
        super.initListener()
        confirmBtn.onClick {
            var account = defaultAccountView.text.toString()
            if(total == 0){
                toast("梧桐币提现数量不能为0")
                return@onClick
            }
            if(account.isEmpty()){
                toast("支付宝账号不能为空")
                return@onClick
            }
            withDraw(account,total)
        }

        exchangeAccount.onClick {
            var intent = Intent(this@CashOutActivity,ChangeAccountActivity::class.java)
            intent.putExtra("account",defaultAccountView.text.toString())
            startActivityForResult(intent,2000)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        var newAccount = data?.getStringExtra("newAccount")
        defaultAccountView.text = newAccount
    }

    /**
     * 提现
     */
    private fun withDraw(alipayAccount:String,wutongbi :Int){
        var params = HttpParams()
        params.put("alipayAccount",alipayAccount)
        params.put("wutongbi",wutongbi)
        OkGo.post<BaseResponse<String>>(Constant.WITH_DRAW)
                .params(params)
                .execute(object : JsonCallBack<BaseResponse<String>>(){
                    override fun onSuccess(response: Response<BaseResponse<String>>?) {
                        if(response?.body()?.res == 0){
                            toast("提现申请已提交，等待审核")
                            total = 0
                            totalView.text = "0"
                            aboutView.text = "约 0 元"
                        }else{
                            toast(response?.body()?.msg.toString())
                        }
                    }

                })
    }
}