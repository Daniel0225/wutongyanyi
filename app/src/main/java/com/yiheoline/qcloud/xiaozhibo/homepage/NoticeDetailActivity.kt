package com.yiheoline.qcloud.xiaozhibo.homepage

import android.content.Intent
import android.graphics.Color
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.lzy.okgo.OkGo
import com.lzy.okgo.model.HttpParams
import com.lzy.okgo.model.Response
import com.shuyu.gsyvideoplayer.GSYVideoManager
import com.yiheoline.liteav.demo.lvb.liveroom.roomutil.http.HttpRequests
import com.yiheoline.qcloud.xiaozhibo.Constant
import com.yiheoline.qcloud.xiaozhibo.TCApplication
import com.yiheoline.qcloud.xiaozhibo.audience.TCAudienceActivity
import com.yiheoline.qcloud.xiaozhibo.base.BaseActivity
import com.yiheoline.qcloud.xiaozhibo.bean.*
import com.yiheoline.qcloud.xiaozhibo.common.utils.TCConstants
import com.yiheoline.qcloud.xiaozhibo.homepage.adapter.DetailImageAdapter
import com.yiheoline.qcloud.xiaozhibo.http.BaseResponse
import com.yiheoline.qcloud.xiaozhibo.http.JsonCallBack
import com.yiheoline.qcloud.xiaozhibo.utils.FastJsonUtil
import com.yiheoline.qcloud.xiaozhibo.utils.GlideRoundTransform
import com.yiheoline.qcloud.xiaozhibo.utils.TimeUtil
import com.yiheonline.qcloud.xiaozhibo.R
import kotlinx.android.synthetic.main.activity_notice_detail.*
import kotlinx.android.synthetic.main.activity_notice_detail.playContain
import kotlinx.android.synthetic.main.activity_notice_detail.playLineView
import kotlinx.android.synthetic.main.activity_notice_detail.playTextView
import kotlinx.android.synthetic.main.activity_notice_detail.singleShowContain
import kotlinx.android.synthetic.main.activity_notice_detail.singleShowLine
import kotlinx.android.synthetic.main.activity_notice_detail.singleShowText
import kotlinx.android.synthetic.main.activity_notice_detail.videoPlayer
import kotlinx.android.synthetic.main.toolbar_layout.*
import kotlinx.android.synthetic.main.toolbar_layout.backView
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
        wantSeeBtn.onClick {
            if(noticeDetailBean?.isIntent == null){
                likeNotice()
            }
        }
        startNow.onClick {
            if(noticeDetailBean?.isNeedPay == 0){
                getOnLine(noticeDetailBean?.noticeId.toString())
            }else{
                placeOrder(noticeDetailBean?.noticeId.toString())
            }

        }
    }

    override fun onResume() {
        super.onResume()
        var noticeId = ""
        if(intent.hasExtra("noticeId")){
            noticeId = intent.getStringExtra("noticeId")
        }else{
            var bun = intent.extras
            if (bun !=null) {
                var keySet = bun.keySet()
                for(key in keySet) {
                    noticeId = bun.getString("noticeId")
                }
            }
        }
        getNoticeDetail(noticeId)
        videoPlayer.onVideoResume()
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        setIntent(intent)
    }

    override fun onPause() {
        super.onPause()
        videoPlayer.onVideoPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        GSYVideoManager.releaseAllVideos()
    }

    private fun initVideoPlayer(){
        //初始化视频播放器
        var currentVideoPath= Constant.VIDEO_BASE+noticeDetailBean?.videoDetail
        videoPlayer.setUp(currentVideoPath,true,"测试视频")
        videoPlayer.isLooping = true
        videoPlayer.setIsTouchWiget(false)
        videoPlayer.isHideKey = true
        videoPlayer.dismissControlTime = 0
        videoPlayer.isStartAfterPrepared = true
        videoPlayer.isReleaseWhenLossAudio = false
        videoPlayer.startPlayLogic()
        GSYVideoManager.instance().setPlayerInitSuccessListener { _, _ ->
            GSYVideoManager.instance().isNeedMute = false
        }
        TCApplication.isRelease = true
    }

    override fun initListener() {
        super.initListener()
        singleShowContain.onClick {
            singleShowLine.visibility = View.VISIBLE
            singleShowText.textSize = 18f
            singleShowText.textColor = Color.parseColor("#000000")

            playLineView.visibility = View.GONE
            playTextView.textSize = 14f
            playTextView.textColor = Color.parseColor("#999999")

            detailImageRv.visibility = View.VISIBLE
            detailTextView.visibility = View.VISIBLE
            warmView.visibility = View.GONE
        }
        playContain.onClick {
            playLineView.visibility = View.VISIBLE
            playTextView.textSize = 18f
            playTextView.textColor = Color.parseColor("#000000")

            singleShowLine.visibility = View.GONE
            singleShowText.textSize = 14f
            singleShowText.textColor = Color.parseColor("#999999")

            detailImageRv.visibility = View.GONE
            detailTextView.visibility = View.GONE
            warmView.visibility = View.VISIBLE
        }
    }

    private fun refreshUi(){
        noticeTitleView.text = noticeDetailBean?.title
        dateView.text = TimeUtil.getYearMonthAndDayWithHour(noticeDetailBean!!.liveTime.toLong())
        priceView.text = noticeDetailBean?.price.toString()
        detailTextView.text = noticeDetailBean?.detail
        Glide.with(this).load(Constant.IMAGE_BASE+noticeDetailBean?.cover)
                .transform(CenterCrop(this),GlideRoundTransform(this,5))
                .into(coverImage)
        var imageDetail = noticeDetailBean?.imageDetail

        noticeDetailBean?.videoDetail?.let {
            videoPlayer.visibility = View.VISIBLE
            initVideoPlayer()
        }

        if(imageDetail!!.isNotEmpty()){
            var images = imageDetail.split(",")
            detailImageRv.layoutManager = LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false)
            var adapter = DetailImageAdapter(R.layout.detail_image_item_layout,images as MutableList<String>)
            detailImageRv.adapter = adapter
        }
        if(noticeDetailBean?.isNeedPay == 0){
            startNow.text = "立即观看"
            if(noticeDetailBean?.liveState != 1){
                startNow.setBackgroundResource(R.drawable.login_btn_gray)
                startNow.isEnabled = false
            }
        }else{
            startNow.text = "立即购买"
        }

        if(noticeDetailBean?.isIntent == null){
            var drawable = resources.getDrawable(R.mipmap.unlike)
            drawable.setBounds(0, 0, drawable.minimumWidth, drawable.minimumHeight) //设置边界
            wantSeeBtn.setCompoundDrawables(null,drawable,null,null)
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
                            startActivity<ConfirmOrderActivity>("noticeDetailBean" to noticeDetailBean,
                            "createOrderResult" to response?.body()?.data)
                        }else{
                            toast(response?.body()?.msg.toString())
                        }
                    }

                })
    }
    /**
     * 查找直播信息
     */
    private fun getOnLine(noticeId: String){
        var params = HttpParams()
        params.put("noticeId",noticeId)
        OkGo.get<BaseResponse<OnLinePlayBean>>(Constant.ONLINE_PLAY)
                .params(params)
                .execute(object : JsonCallBack<BaseResponse<OnLinePlayBean>>(){
                    override fun onSuccess(response: Response<BaseResponse<OnLinePlayBean>>?) {
                        if(response?.body()?.res == 0){
                            getPlayUrl(response.body()?.data!!)
                        }else{
                            toast(response?.body()?.msg.toString())
                        }
                    }

                })
    }
    /**
     * 调用直播服务器 查询直播地址
     */
    private fun getPlayUrl(onLinePlayBean: OnLinePlayBean){
        var mHttpRequest = HttpRequests("https://liveroom.qcloud.com/weapp/live_room")
        mHttpRequest.setToken(TCApplication.mlvbToken)
        mHttpRequest.setUserID(TCApplication.loginInfo?.userId)
        mHttpRequest.getPushers(onLinePlayBean.roomId) { retcode, retmsg, data ->
            var intent = Intent(this, TCAudienceActivity::class.java)
            intent.putExtra(TCConstants.PLAY_URL, data!!.mixedPlayURL)
            intent.putExtra(TCConstants.HEART_COUNT, onLinePlayBean.likes.toString())
            intent.putExtra(TCConstants.MEMBER_COUNT, data.audienceCount.toString())
            intent.putExtra(TCConstants.GROUP_ID, data.roomID)
            intent.putExtra(TCConstants.PUSHER_ID, data.roomCreator)
            intent.putExtra(TCConstants.PUSHER_NAME, data.pushers[0].userName)
            intent.putExtra(TCConstants.PUSHER_AVATAR, data.pushers[0].userAvatar)
            intent.putExtra(TCConstants.PLAY_TYPE, true)
            intent.putExtra(TCConstants.FILE_ID,  "")
            var roomInfo = FastJsonUtil.getObject(data.roomInfo,RoomInfoBean::class.java)
            intent.putExtra(TCConstants.ROOM_TITLE, roomInfo.title)
            intent.putExtra("LIVE_ID",onLinePlayBean.liveId)
            startActivityForResult(intent,2000)
        }
    }
}