package com.yiheoline.qcloud.xiaozhibo.profile

import android.annotation.SuppressLint
import android.os.Handler
import android.os.Message
import android.text.TextUtils
import android.util.Log
import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import com.alipay.sdk.app.PayTask
import com.lzy.okgo.OkGo
import com.lzy.okgo.model.HttpParams
import com.lzy.okgo.model.Response
import com.yiheoline.qcloud.xiaozhibo.Constant
import com.yiheoline.qcloud.xiaozhibo.base.BaseActivity
import com.yiheoline.qcloud.xiaozhibo.bean.PayResult
import com.yiheoline.qcloud.xiaozhibo.http.BaseResponse
import com.yiheoline.qcloud.xiaozhibo.http.JsonCallBack
import com.yiheoline.qcloud.xiaozhibo.profile.adapter.RechargeItemAdapter
import com.yiheonline.qcloud.xiaozhibo.R
import kotlinx.android.synthetic.main.activity_account.*
import kotlinx.android.synthetic.main.toolbar_layout.*
import org.jetbrains.anko.sdk27.coroutines.onClick
import org.jetbrains.anko.startActivity
import org.jetbrains.anko.toast

class AccountActivity : BaseActivity() {
    var payType = 1
    var total = 0
    var chargeNum = 0
    override fun getLayoutId(): Int {
        return R.layout.activity_account
    }

    override fun initView() {
        super.initView()
        backView.onClick { finish() }
        titleView.text = "我的账户"
        rightBtn.visibility = View.VISIBLE
        rightBtn.text = "充值记录"
        rightBtn.onClick {
            startActivity<RechargeRecordActivity>()
        }
        total = intent.getIntExtra("total",0)
        totalView.text = total.toString()

        recyclerView.layoutManager = GridLayoutManager(this,3)
        var adapter = RechargeItemAdapter(R.layout.recharge_list_item, arrayListOf("","","","","",""))
        recyclerView.adapter = adapter
        adapter.setOnItemClickListener { _, _, position ->
            adapter.selectPosition = position
            adapter.notifyDataSetChanged()
        }

        confirmBtn.onClick {
            chargeNum = 100
            recharge("0.01")
        }
    }

    /**
     * 充值
     */
    private fun recharge(money:String){
        var params = HttpParams()
        params.put("money",money)
        params.put("payType",payType)
        params.put("wutongye",100)
        OkGo.post<BaseResponse<String>>(Constant.USER_RECHARGE)
                .params(params)
                .execute(object:JsonCallBack<BaseResponse<String>>(){
                    override fun onSuccess(response: Response<BaseResponse<String>>?) {
                        if(response?.body()?.res == 0){
                            aliPay(response?.body()?.data!!)
                        }else{
                            toast(response?.body()?.msg.toString())
                        }
                    }

                })
    }
    /**
     * 支付宝支付
     */
    private fun aliPay(orderInfo: String) {
        val payRunnable = Runnable {
            val alipay = PayTask(this)
            val result = alipay.payV2(orderInfo, true)
            Log.e("msp", result.toString())

            val msg = Message()
            msg.what = SDK_PAY_FLAG
            msg.obj = result
            mHandler.sendMessage(msg)
        }

        // 必须异步调用
        val payThread = Thread(payRunnable)
        payThread.start()
    }
    private val SDK_PAY_FLAG = 1

    @SuppressLint("HandlerLeak")
    private val mHandler = object : Handler() {
        override fun handleMessage(msg: Message) {
            when (msg.what) {
                SDK_PAY_FLAG -> {
                    val payResult = PayResult(msg.obj as Map<String, String>)
                    /**
                     * 对于支付结果，请商户依赖服务端的异步通知结果。同步通知结果，仅作为支付结束的通知。
                     */
                    val resultInfo = payResult.getResult()// 同步返回需要验证的信息
                    val resultStatus = payResult.getResultStatus()
                    // 判断resultStatus 为9000则代表支付成功
                    if (TextUtils.equals(resultStatus, "9000")) {
                        // 该笔订单是否真实支付成功，需要依赖服务端的异步通知。
                        toast("支付成功")
                        total += chargeNum
                        totalView.text = total.toString()
                    } else {
                        // 该笔订单真实的支付结果，需要依赖服务端的异步通知。
                        toast("支付失败")
                    }
                }
                else -> {
                }
            }
        }
    }
}