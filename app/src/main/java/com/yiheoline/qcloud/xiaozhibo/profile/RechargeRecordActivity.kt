package com.yiheoline.qcloud.xiaozhibo.profile

import android.graphics.Color
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.lzy.okgo.OkGo
import com.lzy.okgo.model.HttpParams
import com.lzy.okgo.model.Response
import com.yiheoline.qcloud.xiaozhibo.Constant
import com.yiheoline.qcloud.xiaozhibo.base.BaseActivity
import com.yiheoline.qcloud.xiaozhibo.http.BaseResponse
import com.yiheoline.qcloud.xiaozhibo.http.JsonCallBack
import com.yiheoline.qcloud.xiaozhibo.http.response.GiftRecordResponse
import com.yiheoline.qcloud.xiaozhibo.http.response.RechargeRecordResponse
import com.yiheoline.qcloud.xiaozhibo.http.response.WithDrawResponse
import com.yiheoline.qcloud.xiaozhibo.profile.adapter.GiftListAdapter
import com.yiheoline.qcloud.xiaozhibo.profile.adapter.RechargeListAdapter
import com.yiheoline.qcloud.xiaozhibo.profile.adapter.WithDrawListAdapter
import com.yiheonline.qcloud.xiaozhibo.R
import kotlinx.android.synthetic.main.activity_fans_list.*
import kotlinx.android.synthetic.main.activity_fans_list.playContain
import kotlinx.android.synthetic.main.activity_fans_list.playLineView
import kotlinx.android.synthetic.main.activity_fans_list.playTextView
import kotlinx.android.synthetic.main.activity_fans_list.singleShowContain
import kotlinx.android.synthetic.main.activity_fans_list.singleShowLine
import kotlinx.android.synthetic.main.activity_fans_list.singleShowText
import kotlinx.android.synthetic.main.activity_recharge_record.*
import kotlinx.android.synthetic.main.activity_recharge_record.recyclerView
import kotlinx.android.synthetic.main.toolbar_layout.*
import org.jetbrains.anko.sdk27.coroutines.onClick
import org.jetbrains.anko.textColor
import org.jetbrains.anko.toast

class RechargeRecordActivity : BaseActivity() {
    var pageNum = 1
    var type = 0//0: 礼物记录 1 充值记录 2 提现记录
    var rechargeListAdapter : RechargeListAdapter? = null
    var giftListAdapter:GiftListAdapter? = null
    var withDrawListAdapter:WithDrawListAdapter? = null

    override fun getLayoutId(): Int {
        return R.layout.activity_recharge_record
    }

    override fun initView() {
        super.initView()
        backView.onClick { finish() }
        titleView.text = "我的账户"
        var layoutManager = LinearLayoutManager(this)
        layoutManager.orientation = LinearLayoutManager.VERTICAL
        recyclerView.layoutManager = layoutManager
        //充值adapter
        rechargeListAdapter = RechargeListAdapter(R.layout.rechare_list_item, arrayListOf())
        rechargeListAdapter?.loadMoreModule?.isEnableLoadMore = true
        rechargeListAdapter?.loadMoreModule?.isAutoLoadMore = true
        rechargeListAdapter?.loadMoreModule?.isEnableLoadMoreIfNotFullPage = false
        //礼物adapter
        giftListAdapter = GiftListAdapter(R.layout.gift_record_item_layout, arrayListOf())
        giftListAdapter?.loadMoreModule?.isEnableLoadMore = true
        giftListAdapter?.loadMoreModule?.isAutoLoadMore = true
        giftListAdapter?.loadMoreModule?.isEnableLoadMoreIfNotFullPage = false
        //提现adapter
        withDrawListAdapter = WithDrawListAdapter(R.layout.withdraw_list_item, arrayListOf())
        withDrawListAdapter?.loadMoreModule?.isEnableLoadMore = true
        withDrawListAdapter?.loadMoreModule?.isAutoLoadMore = true
        withDrawListAdapter?.loadMoreModule?.isEnableLoadMoreIfNotFullPage = false

        type = intent.getIntExtra("type",0)

//        when(type){
//            0 -> recyclerView.adapter = giftListAdapter
//            1 -> recyclerView.adapter = rechargeListAdapter
//            2 -> recyclerView.adapter = rechargeListAdapter
//        }

        recyclerView.postDelayed({
            when(type){
                0 -> playContain.performClick()
                1 -> singleShowContain.performClick()
                2 -> outContain.performClick()
            }

        },500)

    }

    override fun initListener() {
        super.initListener()
        singleShowContain.onClick {
            type = 0
            singleShowLine.visibility = View.VISIBLE
            singleShowText.textSize = 16f
            singleShowText.textColor = Color.parseColor("#000000")

            playLineView.visibility = View.GONE
            playTextView.textSize = 14f
            playTextView.textColor = Color.parseColor("#999999")

            outLineView.visibility = View.GONE
            outTextView.textSize = 14f
            outTextView.textColor = Color.parseColor("#999999")

            recyclerView.adapter = giftListAdapter
            pageNum = 1
            giftList()
        }
        playContain.onClick {
            type = 1
            playLineView.visibility = View.VISIBLE
            playTextView.textSize = 16f
            playTextView.textColor = Color.parseColor("#000000")

            singleShowLine.visibility = View.GONE
            singleShowText.textSize = 14f
            singleShowText.textColor = Color.parseColor("#999999")

            outLineView.visibility = View.GONE
            outTextView.textSize = 14f
            outTextView.textColor = Color.parseColor("#999999")

            recyclerView.adapter = rechargeListAdapter
            pageNum = 1
            getRechargeList()
        }
        outContain.onClick {
            type = 2
            playLineView.visibility = View.GONE
            playTextView.textSize = 14f
            playTextView.textColor = Color.parseColor("#999999")

            singleShowLine.visibility = View.GONE
            singleShowText.textSize = 14f
            singleShowText.textColor = Color.parseColor("#999999")

            outLineView.visibility = View.VISIBLE
            outTextView.textSize = 16f
            outTextView.textColor = Color.parseColor("#000000")

            recyclerView.adapter = withDrawListAdapter
            pageNum = 1
            withDrawList()
        }
    }

    /**
     * 获取充值记录
     */
    private fun getRechargeList(){
        var params = HttpParams()
        params.put("pageNum",pageNum)
        params.put("pageSize", Constant.PAGE_SIZE)
        OkGo.post<BaseResponse<RechargeRecordResponse>>(Constant.RECHARGE_LIST)
                .params(params)
                .execute(object:JsonCallBack<BaseResponse<RechargeRecordResponse>>(){
                    override fun onFinish() {
                        super.onFinish()
                        rechargeListAdapter?.loadMoreModule?.loadMoreComplete()
                    }

                    override fun onError(response: Response<BaseResponse<RechargeRecordResponse>>?) {
                        super.onError(response)
                        rechargeListAdapter?.loadMoreModule?.loadMoreFail()
                    }
                    override fun onSuccess(response: Response<BaseResponse<RechargeRecordResponse>>?) {
                        if(response?.body()?.res == 0){
                            if(pageNum == 1){
                                rechargeListAdapter?.setList(response.body()?.data?.list)
                            }else{
                                rechargeListAdapter?.addData(response.body().data.list)
                            }
                            if(response?.body()?.data?.pages == pageNum){
                                rechargeListAdapter?.loadMoreModule?.loadMoreEnd(true)
                            }
                        }else{
                            toast(response?.body()?.msg.toString())
                        }
                    }

                })
    }

    /**
     * 礼物记录
     */
    private fun giftList(){
        var params = HttpParams()
        params.put("pageNum",pageNum)
        params.put("pageSize", Constant.PAGE_SIZE)
        OkGo.post<BaseResponse<GiftRecordResponse>>(Constant.GIFT_RECORD)
                .params(params)
                .execute(object:JsonCallBack<BaseResponse<GiftRecordResponse>>(){
                    override fun onFinish() {
                        super.onFinish()
                        giftListAdapter?.loadMoreModule?.loadMoreComplete()
                    }

                    override fun onError(response: Response<BaseResponse<GiftRecordResponse>>?) {
                        super.onError(response)
                        giftListAdapter?.loadMoreModule?.loadMoreFail()
                    }

                    override fun onSuccess(response: Response<BaseResponse<GiftRecordResponse>>?) {
                        if(response?.body()?.res == 0){
                            if(pageNum == 1){
                                giftListAdapter?.setList(response.body()?.data?.list)
                            }else{
                                giftListAdapter?.addData(response.body().data.list)
                            }
                            if(response?.body()?.data?.pages == pageNum){
                                giftListAdapter?.loadMoreModule?.loadMoreEnd(true)
                            }
                        }else{
                            toast(response?.body()?.msg.toString())
                        }
                    }

                })
    }
    /**
     * 提现记录
     */
    private fun withDrawList(){
        var params = HttpParams()
        params.put("pageNum",pageNum)
        params.put("pageSize", Constant.PAGE_SIZE)
        OkGo.post<BaseResponse<WithDrawResponse>>(Constant.QUERY_WITHDRAW_LIST)
                .params(params)
                .execute(object:JsonCallBack<BaseResponse<WithDrawResponse>>(){
                    override fun onFinish() {
                        super.onFinish()
                        withDrawListAdapter?.loadMoreModule?.loadMoreComplete()
                    }

                    override fun onError(response: Response<BaseResponse<WithDrawResponse>>?) {
                        super.onError(response)
                        withDrawListAdapter?.loadMoreModule?.loadMoreFail()
                    }

                    override fun onSuccess(response: Response<BaseResponse<WithDrawResponse>>?) {
                        if(response?.body()?.res == 0){
                            if(pageNum == 1){
                                withDrawListAdapter?.setList(response.body()?.data?.list)
                            }else{
                                withDrawListAdapter?.addData(response.body().data.list)
                            }
                            if(response?.body()?.data?.pages == pageNum){
                                withDrawListAdapter?.loadMoreModule?.loadMoreEnd(true)
                            }
                        }else{
                            toast(response?.body()?.msg.toString())
                        }
                    }

                })
    }
}