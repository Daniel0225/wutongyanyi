package com.yiheoline.qcloud.xiaozhibo.homepage

import android.annotation.SuppressLint
import android.os.Handler
import android.os.Message
import android.text.TextUtils
import android.util.Log
import com.alipay.sdk.app.PayTask
import com.bumptech.glide.Glide
import com.lzy.okgo.OkGo
import com.lzy.okgo.model.HttpParams
import com.lzy.okgo.model.Response
import com.yiheoline.qcloud.xiaozhibo.Constant
import com.yiheoline.qcloud.xiaozhibo.base.BaseActivity
import com.yiheoline.qcloud.xiaozhibo.bean.CreateOrderResult
import com.yiheoline.qcloud.xiaozhibo.bean.PayResult
import com.yiheoline.qcloud.xiaozhibo.bean.ShowNoticeDetailBean
import com.yiheoline.qcloud.xiaozhibo.http.BaseResponse
import com.yiheoline.qcloud.xiaozhibo.http.JsonCallBack
import com.yiheoline.qcloud.xiaozhibo.utils.TimeUtil
import com.yiheonline.qcloud.xiaozhibo.R
import kotlinx.android.synthetic.main.activity_confirm_order.*
import kotlinx.android.synthetic.main.activity_notice_detail.*
import kotlinx.android.synthetic.main.activity_notice_detail.coverImage
import kotlinx.android.synthetic.main.activity_notice_detail.dateView
import kotlinx.android.synthetic.main.activity_notice_detail.noticeTitleView
import kotlinx.android.synthetic.main.activity_notice_detail.priceView
import kotlinx.android.synthetic.main.toolbar_layout.*
import org.jetbrains.anko.sdk27.coroutines.onClick
import org.jetbrains.anko.startActivity
import org.jetbrains.anko.toast

class ConfirmOrderActivity : BaseActivity() {
    var noticeDetailBean : ShowNoticeDetailBean? = null
    var createOrderResult : CreateOrderResult? = null
    override fun getLayoutId(): Int {
        return R.layout.activity_confirm_order
    }

    override fun initView() {
        super.initView()
        backView.onClick { finish() }
        titleView.text = "确认订单"
        noticeDetailBean = intent.getSerializableExtra("noticeDetailBean") as ShowNoticeDetailBean
        createOrderResult = intent.getSerializableExtra("createOrderResult") as CreateOrderResult
        noticeTitleView.text = noticeDetailBean?.title
        dateView.text = TimeUtil.getYearMonthAndDayWithHour(noticeDetailBean!!.liveTime.toLong())
        priceView.text = noticeDetailBean?.price.toString()
        Glide.with(this).load(Constant.IMAGE_BASE+noticeDetailBean?.cover).into(coverImage)
    }

    override fun initListener() {
        super.initListener()
        weixinPay.onClick {
            aliPay.isChecked = false
            weixinPay.isChecked = true
        }
        aliPay.onClick {
            aliPay.isChecked = true
            weixinPay.isChecked = false
        }

        payNowBtn.onClick {
            getRecPayInfo()
        }
    }

    /**
     * 获取娱乐订单 支付宝 支付信息
     */
    private fun getRecPayInfo() {
        var params = HttpParams()
        params.put("orderId", createOrderResult!!.orderId.toString())
        params.put("payType",1)
        OkGo.post<BaseResponse<String>>(Constant.ORDER_PAYMENT)
                .params(params)
                .execute(object : JsonCallBack<BaseResponse<String>>() {

                    override fun onError(response: Response<BaseResponse<String>>?) {
                        super.onError(response)
                    }

                    override fun onSuccess(response: Response<BaseResponse<String>>?) {
                        if (response?.body()?.res == 0) {
                            aliPay(response?.body()?.data!!)
                        } else {
                            toast(response?.body()?.msg + "")
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
                        startActivity<BuyResultActivity>("noticeDetailBean" to noticeDetailBean)
                        finish()
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