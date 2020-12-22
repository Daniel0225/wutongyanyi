package com.yiheoline.qcloud.xiaozhibo.homepage

import android.graphics.Color
import android.widget.Toast
import com.bumptech.glide.Glide
import com.lzy.okgo.OkGo
import com.lzy.okgo.model.HttpParams
import com.lzy.okgo.model.Response
import com.yiheoline.qcloud.xiaozhibo.Constant
import com.yiheoline.qcloud.xiaozhibo.base.BaseActivity
import com.yiheoline.qcloud.xiaozhibo.bean.CreateOrderResult
import com.yiheoline.qcloud.xiaozhibo.bean.OnLinePlayBean
import com.yiheoline.qcloud.xiaozhibo.bean.ShowNoticeBean
import com.yiheoline.qcloud.xiaozhibo.bean.ShowNoticeDetailBean
import com.yiheoline.qcloud.xiaozhibo.http.BaseResponse
import com.yiheoline.qcloud.xiaozhibo.http.JsonCallBack
import com.yiheoline.qcloud.xiaozhibo.utils.TimeUtil
import com.yiheonline.qcloud.xiaozhibo.R
import kotlinx.android.synthetic.main.activity_notice_detail.*
import kotlinx.android.synthetic.main.toolbar_layout.*
import kotlinx.android.synthetic.main.toolbar_layout.titleView
import org.jetbrains.anko.sdk27.coroutines.onClick
import org.jetbrains.anko.startActivity
import org.jetbrains.anko.textColor
import org.jetbrains.anko.toast

class NoticeDetailActivity : BaseActivity() {
    var noticeDetailBean : ShowNoticeDetailBean? = null
    override fun getLayoutId(): Int {
        return R.layout.activity_notice_detail
    }

    override fun initView() {
        super.initView()
        titleView.text = "直播预告"
        backView.onClick { finish() }

        var noticeBean = intent.getSerializableExtra("noticeBean") as ShowNoticeBean



        wantSeeBtn.onClick {
            if(noticeDetailBean?.isIntent == null){
                likeNotice()
            }
        }

        getNoticeDetail(noticeBean.noticeId.toString())

        startNow.onClick {
            getOnLine(noticeDetailBean?.noticeId.toString())
        }
    }

    private fun refreshUi(){
        noticeTitleView.text = noticeDetailBean?.title
        dateView.text = TimeUtil.getYearMonthAndDayWithHour(noticeDetailBean!!.liveTime.toLong())
        priceView.text = noticeDetailBean?.price.toString()
        detailTextView.text = noticeDetailBean?.detail
        Glide.with(this).load(Constant.IMAGE_BASE+noticeDetailBean?.cover).into(coverImage)
        if(noticeDetailBean?.isIntent == null){

        }else{
            var drawable = resources.getDrawable(R.mipmap.like)
            drawable.setBounds(0, 0, drawable.minimumWidth, drawable.minimumHeight) //设置边界
            wantSeeBtn.setCompoundDrawables(null,drawable,null,null)
        }

        when(noticeDetailBean?.liveState){
            0 -> {
                statusView.setImageResource(R.mipmap.unstarted)
                statusTextView.text = "未开播"
                statusTextView.textColor = Color.parseColor("#999999")
            }
            1 ->{

            }
            2 ->{
                statusView.setImageResource(R.mipmap.unstarted)
                statusTextView.text = "已结束"
                statusTextView.textColor = Color.parseColor("#999999")
            }

        }
    }
    /**
     * 想看
     */
    private fun likeNotice(){
        var params = HttpParams()
        params.put("noticeId",noticeDetailBean?.noticeId!!)
        OkGo.post<BaseResponse<String>>(Constant.NOTICE_INTENT)
                .params(params)
                .execute(object : JsonCallBack<BaseResponse<String>>(){
                    override fun onSuccess(response: Response<BaseResponse<String>>?) {
                        if(response?.body()?.res == 0){
                            var drawable = resources.getDrawable(R.mipmap.like)
                            drawable.setBounds(0, 0, drawable.minimumWidth, drawable.minimumHeight) //设置边界
                            wantSeeBtn.setCompoundDrawables(null,drawable,null,null)
                        }else{
                            toast(response?.body()?.msg.toString())
                        }
                    }

                })
    }

    /**
     * 获取详情
     */
    private fun getNoticeDetail(noticeId : String){
        var params = HttpParams()
        params.put("noticeId",noticeId)
        OkGo.post<BaseResponse<ShowNoticeDetailBean>>(Constant.NOTICE_DETAIL)
                .params(params)
                .execute(object : JsonCallBack<BaseResponse<ShowNoticeDetailBean>>(){
                    override fun onSuccess(response: Response<BaseResponse<ShowNoticeDetailBean>>?) {
                        if(response?.body()?.res == 0){
                            noticeDetailBean = response?.body()?.data
                            refreshUi()
                        }else{
                            toast(response?.body()?.msg.toString())
                        }
                    }

                })
    }
    /**
     * 下单
     */
    private fun placeOrder(noticeId : String){
        var params = HttpParams()
        params.put("relationId",noticeId)
        params.put("type",1)
        OkGo.post<BaseResponse<CreateOrderResult>>(Constant.PLACE_ORDER)
                .params(params)
                .execute(object : JsonCallBack<BaseResponse<CreateOrderResult>>(){

                    override fun onSuccess(response: Response<BaseResponse<CreateOrderResult>>?) {
                        if(response?.body()?.res == 0){
//                            startActivity<OrderPayActivity>("createOrderResult" to response.body()?.data,"orderType" to 2)
                        }else{
//                            Toast.makeText(this@VideoDetailActivity,response?.body()?.msg+"", Toast.LENGTH_LONG).show()
                        }
                    }

                })
    }
    /**
     * 查找直播信息
     */
    private fun getOnLine(noticeId: String){
        OkGo.get<BaseResponse<OnLinePlayBean>>(Constant.ONLINE_PLAY+noticeId)
                .execute(object : JsonCallBack<BaseResponse<OnLinePlayBean>>(){
                    override fun onSuccess(response: Response<BaseResponse<OnLinePlayBean>>?) {
                        if(response?.body()?.res == 0){

                        }else{
                            toast(response?.body()?.msg.toString())
                        }
                    }

                })
    }
}