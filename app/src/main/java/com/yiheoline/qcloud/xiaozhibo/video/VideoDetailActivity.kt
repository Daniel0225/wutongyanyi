package com.yiheoline.qcloud.xiaozhibo.video

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.alipay.sdk.app.PayTask
import com.bumptech.glide.Glide
import com.lzy.okgo.OkGo
import com.lzy.okgo.model.HttpParams
import com.lzy.okgo.model.Response
import com.shuyu.gsyvideoplayer.GSYBaseADActivityDetail
import com.shuyu.gsyvideoplayer.GSYVideoManager
import com.shuyu.gsyvideoplayer.builder.GSYVideoOptionBuilder
import com.shuyu.gsyvideoplayer.listener.LockClickListener
import com.shuyu.gsyvideoplayer.video.GSYADVideoPlayer
import com.shuyu.gsyvideoplayer.video.NormalGSYVideoPlayer
import com.shuyu.gsyvideoplayer.video.base.GSYVideoPlayer
import com.yiheoline.qcloud.xiaozhibo.Constant
import com.yiheoline.qcloud.xiaozhibo.TCApplication
import com.yiheoline.qcloud.xiaozhibo.bean.CreateOrderResult
import com.yiheoline.qcloud.xiaozhibo.bean.PayResult
import com.yiheoline.qcloud.xiaozhibo.bean.VideoDetailBean
import com.yiheoline.qcloud.xiaozhibo.dialog.CommentDialog
import com.yiheoline.qcloud.xiaozhibo.dialog.PayConfirmDialog
import com.yiheoline.qcloud.xiaozhibo.http.BaseResponse
import com.yiheoline.qcloud.xiaozhibo.http.JsonCallBack
import com.yiheoline.qcloud.xiaozhibo.http.response.CommentListResponse
import com.yiheoline.qcloud.xiaozhibo.utils.StatusBarUtil
import com.yiheoline.qcloud.xiaozhibo.video.adapter.CommentListAdapter
import com.yiheoline.qcloud.xiaozhibo.video.adapter.VideoListAdapter
import com.yiheonline.qcloud.xiaozhibo.R
import kotlinx.android.synthetic.main.activity_video_detail.*
import org.jetbrains.anko.sdk27.coroutines.onClick
import org.jetbrains.anko.startActivity
import org.jetbrains.anko.toast


class VideoDetailActivity : GSYBaseADActivityDetail<NormalGSYVideoPlayer, GSYADVideoPlayer>() {
    private val urlAd = ""
    private var url = ""
    private var imagePath = ""
    private var percent = 0
    private var pageNum = 1
    private var currentPosition = 0L
    private var isNeedPay = false
    var videoDetailBean: VideoDetailBean? = null
    var commentAdapter : CommentListAdapter? = null
    var isReply = false//????????????????????????????????? ??????????????????

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video_detail)
        StatusBarUtil.setStatusBarColor(this,Color.parseColor("#000000"))

        currentPosition = intent.getLongExtra("currentPosition",0L)
        initViews()

    }

    override fun onResume() {
        super.onResume()
        var videoId = 0
        if(intent.hasExtra("videoId")){
            videoId = intent.getIntExtra("videoId",0)
        }else{
            var bun = intent.extras
            if (bun !=null) {
                var keySet = bun.keySet()
                for(key in keySet) {
                    videoId = bun.getInt("id")
                }
            }
        }
        getData(videoId)

        if(!isNeedPay){
            detailPlayer?.onVideoPause()
        }

    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        setIntent(intent)
        val videoId = intent!!.getIntExtra("videoId",0)
        getDetail(videoId)
    }

    /**
     * ??????????????????
     */
    private fun getData(videoId : Int){
        getDetail(videoId)
        getCommentList(videoId)
    }

    /**
     * ????????????????????????
     */
    private fun initPlayer(){
        //??????title
        detailPlayer?.setLockClickListener(LockClickListener { _, lock ->
            if (orientationUtils != null) {
                //???????????????onConfigurationChanged
                orientationUtils.isEnable = !lock
            }
        })
        detailPlayer?.isStartAfterPrepared = true
        detailPlayer?.isReleaseWhenLossAudio = false
        initVideoBuilderMode()
        detailPlayer.seekOnStart = currentPosition
        detailPlayer?.startPlayLogic()
        detailPlayer?.setGSYVideoProgressListener { _, _, currentPosition, duration ->
            percent = 100 * currentPosition/duration
            this.currentPosition = currentPosition.toLong()
            //isNeedPay ??????????????????, 0:????????? 1:????????????
            if(videoDetailBean?.isNeedPay == 1 ){
                if(currentPosition >= 5*1000 && currentPosition < 10*1000){
                    warmText.visibility = View.VISIBLE
                }else if(currentPosition >= 10*1000){
                    //????????????
                    warmText.visibility = View.VISIBLE
                    if(currentPosition >= 6 * 60 * 1000){
                        isNeedPay = true
                        detailPlayer?.onVideoPause()
                        if(GSYVideoManager.isFullState(this)){
                            GSYVideoManager.backFromWindowFull(this)
                        }
                        detailPlayer.startButton.visibility = View.GONE
                        detailPlayer?.isStartAfterPrepared = false
                        finishContain.visibility = View.VISIBLE
                        warmText.visibility = View.GONE
                    }
                }
            }
        }

        GSYVideoManager.instance().setPlayerInitSuccessListener { _, _ ->
            GSYVideoManager.instance().isNeedMute = false
        }
    }

    private fun initSpan(){
        if(videoDetailBean?.isNeedPay == 1){
            warmText.onClick { toBuyMovie() }
            bugVideoBtn.text = "${videoDetailBean?.price}???????????????"
//            var spannableString = SpannableStringBuilder()
//            spannableString.append("??????6????????????????????????")
//            var clickableSpan = object : ClickableSpan(){
//                override fun onClick(p0: View) {
//                    toBuyMovie()
//                }
//
//            }
//            spannableString.setSpan(clickableSpan,7,11,Spannable.SPAN_EXCLUSIVE_INCLUSIVE)
//            warmText.text = spannableString
//            warmText.movementMethod = LinkMovementMethod.getInstance()
        }else{
            warmText.visibility = View.GONE
            isNeedPay = false
            finishContain.visibility = View.GONE
        }

    }

    /**
     * ???????????????
     */
    private fun toBuyMovie(){
        createRecOrder()
    }

    override fun onDestroy() {
        super.onDestroy()
//        if(percent > 0){//????????????????????????0% ?????????????????????????????????
//            var videoRecordBean = VideoRecordBean()
//            videoRecordBean.videoId = videoDetailBean!!.videoId
//            videoRecordBean.percent = percent
//            videoRecordBean.imgPath = videoDetailBean!!.imgPath
//            videoRecordBean.name = videoDetailBean!!.name
//            videoRecordBean.updateTime = System.currentTimeMillis()
//            videoRecordBean.currentPosition = currentPosition
//            videoRecordBean.saveOrUpdate("videoId = ${videoRecordBean.videoId}")
//        }
        TCApplication.isRelease = true
        GSYVideoManager.releaseAllVideos()
    }

    override fun onPause() {
        super.onPause()
        detailPlayer?.onVideoPause()
    }

    private fun initViews(){
        detailPlayer.titleTextView.visibility = View.GONE
        detailPlayer.backButton.visibility = View.GONE
        detailPlayer.backButton.onClick { finish() }
        backView.onClick { finish() }
        bugVideoBtn.onClick {
            toBuyMovie()
        }
        finishContain.onClick {

        }
        likeBtn.onClick {
            if(videoDetailBean == null){
                return@onClick
            }
            if(videoDetailBean!!.isLike == null){
                likeVideo()
            }else{
                unLikeVideo()
            }

        }
        collectBtn.onClick {
            if(videoDetailBean?.isCollect == null){
                collectVideo(videoDetailBean?.videoId)
            }else{
                cancelCollectVideo(videoDetailBean?.videoId)
            }
        }
        pubCommentBtn.onClick {
            isReply = false
            CommentDialog.onCreateDialog(this@VideoDetailActivity,"",object : CommentDialog.PublishCommentListener{
                override fun publish(commentContent: String) {
                    publishComment(commentContent)
                }

            })
        }
        initCommentRv()
    }

    /**
     * ???????????????RV
     */
    private fun initRv(videoDetailBean: VideoDetailBean){
        initSpan()
        imagePath = "${Constant.IMAGE_BASE}/${videoDetailBean?.imgPath}"
        movieTitleView.text = videoDetailBean?.title
        videoDescView.text = videoDetailBean?.desc
        checkMoreView.onClick {
            videoDescView.maxLines = 10
            checkMoreView.visibility = View.GONE
            checkLessView.visibility = View.VISIBLE
        }
        checkLessView.onClick {
            videoDescView.maxLines = 2
            checkMoreView.visibility = View.VISIBLE
            checkLessView.visibility = View.GONE
        }

        recyclerView.layoutManager = GridLayoutManager(this,2)
        var newstAdapter = VideoListAdapter(R.layout.video_list_item, videoDetailBean.videoList)
        recyclerView.adapter = newstAdapter
        newstAdapter.setOnItemClickListener { _, _, position ->
            startActivity<VideoDetailActivity>("videoId" to newstAdapter!!.data[position].videoId)
        }

        //????????????
        if(videoDetailBean.isLike == null){
            likeBtn.setImageResource(R.mipmap.dianzan)
        }else{
            likeBtn.setImageResource(R.mipmap.dianzan1)
        }
        //????????????
        if(videoDetailBean.isCollect == null){
            collectBtn.setImageResource(R.mipmap.shoucang)
        }else{
            collectBtn.setImageResource(R.mipmap.shoucang1)
        }
        playNumView.text = videoDetailBean.views.toString()
        recommendView.text = videoDetailBean.commentNum.toString()
    }

    /**
     * ???????????????RV
     */
    private fun initCommentRv(){
        var layoutManager = LinearLayoutManager(this)
        layoutManager.orientation = LinearLayoutManager.VERTICAL
        recommendRv.layoutManager = layoutManager
        commentAdapter = CommentListAdapter(R.layout.video_recommend_item_layout, arrayListOf())
        recommendRv.adapter = commentAdapter
        commentAdapter?.addChildClickViewIds(R.id.isLikeContain,R.id.replyBtn)
        commentAdapter?.setOnItemChildClickListener { _, view, position ->
            when(view.id){
                R.id.isLikeContain ->{
                    var commentBean = commentAdapter!!.data[position]
                    if(commentBean.isLike == null){
                        likeComment(commentBean.commentId)
                        commentAdapter!!.data[position].isLike = "1"
                        commentAdapter!!.data[position].likes++
                    }else{
                        unLikeComment(commentBean.commentId)
                        commentAdapter!!.data[position].isLike = null
                        commentAdapter!!.data[position].likes--
                    }
                    commentAdapter?.notifyItemChanged(position)
                }
                R.id.replyBtn ->{
                    isReply = true
                    var commentBean = commentAdapter!!.data[position]
                    CommentDialog.onCreateDialog(this@VideoDetailActivity, "??????@" + commentBean.nickname,object : CommentDialog.PublishCommentListener{
                        override fun publish(commentContent: String) {
                            if(isReply){
                                //???????????????
                                replyComment(commentContent,commentBean.commentId,commentBean.userId)
                            }else{
                                //?????????????????????
                                publishComment(commentContent)
                            }
                        }

                    })
                }
            }

        }
    }


    override fun getGSYVideoOptionBuilder(): GSYVideoOptionBuilder {
        //?????????builder???
        val imageView = ImageView(this)
        imageView.scaleType = ImageView.ScaleType.CENTER_CROP
        Glide.with(this).load(imagePath).into(imageView)
        return getCommonBuilder()
            .setUrl(url)
            .setThumbImageView(imageView)
    }
    /**
     * ???????????????????????????true????????????
     *
     * @return true
     */
    override fun getDetailOrientationRotateAuto(): Boolean {
        return true
    }

    override fun getGSYVideoPlayer(): NormalGSYVideoPlayer {
        return detailPlayer
    }

    override fun getGSYADVideoOptionBuilder(): GSYVideoOptionBuilder {
        return getCommonBuilder()
            .setUrl(urlAd)
    }
    override fun getGSYADVideoPlayer(): GSYADVideoPlayer {
        return adPlayer
    }

    override fun isNeedAdOnStart(): Boolean {
        return false
    }

    override fun onEnterFullscreen(url: String?, vararg objects: Any?) {
        super.onEnterFullscreen(url, *objects)
        //????????????????????????????????????
        val gsyVideoPlayer = objects[1] as GSYVideoPlayer
        gsyVideoPlayer.backButton.visibility = View.GONE
    }

    /**
     * ?????????????????????
     */
    private fun getCommonBuilder(): GSYVideoOptionBuilder {
        return GSYVideoOptionBuilder()
            .setCacheWithPlay(true)
            .setVideoTitle(" ")
            .setFullHideActionBar(true)
            .setFullHideStatusBar(true)
            .setIsTouchWiget(true)
            .setRotateViewAuto(false)
            .setLockLand(false)
            .setShowFullAnimation(false)//????????????
            .setNeedLockFull(true)
            .setSeekRatio(1f)
    }

    /**
     *??????????????????
     */
    private fun getDetail(videoId:Int){
        var params = HttpParams()
        params.put("videoId",videoId)
        var detailUrl = Constant.VIDEO_DETAIL
        OkGo.post<BaseResponse<VideoDetailBean>>(detailUrl)
            .params(params)
            .execute(object : JsonCallBack<BaseResponse<VideoDetailBean>>(){
                override fun onSuccess(response: Response<BaseResponse<VideoDetailBean>>?) {
                    if(response?.body()?.res == 0){
                        videoDetailBean = response.body()?.data
                        initRv(videoDetailBean!!)
                        url = "${Constant.VIDEO_BASE}/${response.body()?.data?.videoPath!!}"
                        initPlayer()
                    }else{
                        Toast.makeText(this@VideoDetailActivity,response?.body()?.msg+"",Toast.LENGTH_LONG).show()
                    }
                }

            })
    }

    /**
     * ??????????????????
     */
    private fun createRecOrder(){
        var params = HttpParams()
        params.put("relationId",videoDetailBean!!.videoId)
        params.put("type",2)
        OkGo.post<BaseResponse<CreateOrderResult>>(Constant.PLACE_ORDER)
            .params(params)
            .execute(object : JsonCallBack<BaseResponse<CreateOrderResult>>(){

                override fun onError(response: Response<BaseResponse<CreateOrderResult>>?) {
                    super.onError(response)
                    Toast.makeText(this@VideoDetailActivity,"????????????",Toast.LENGTH_LONG).show()
                }
                override fun onSuccess(response: Response<BaseResponse<CreateOrderResult>>?) {
                    if(response?.body()?.res == 0){
                        PayConfirmDialog.onCreateDialog(this@VideoDetailActivity,
                                response.body()?.data?.totalMoney!!,videoDetailBean!!.title,
                                object : PayConfirmDialog.PayConfirmListener{
                                    override fun isNeedPay(isUpdate: Boolean, dialog: Dialog) {
                                        if(isUpdate){
                                            //???????????????
                                            getRecPayInfo(response.body()?.data?.orderId.toString())
                                            dialog.dismiss()
                                        }

                                    }

                                })
                    }else{
                        Toast.makeText(this@VideoDetailActivity,response?.body()?.msg+"",Toast.LENGTH_LONG).show()
                    }
                }

            })
    }

    /**
     * ??????
     */
    private fun likeVideo(){
        var params = HttpParams()
        params.put("videoId",videoDetailBean?.videoId.toString())
        OkGo.post<BaseResponse<String>>(Constant.VIDEO_LIKE)
                .params(params)
                .execute(object : JsonCallBack<BaseResponse<String>>(){
                    override fun onSuccess(response: Response<BaseResponse<String>>?) {
                        if(response?.body()?.res == 0){
                            videoDetailBean?.isLike = "1"
                            likeBtn.setImageResource(R.mipmap.dianzan1)
                        }
                    }

                })
    }
    /**
     * ????????????
     */
    private fun unLikeVideo(){
        var params = HttpParams()
        params.put("videoId",videoDetailBean?.videoId.toString())
        OkGo.post<BaseResponse<String>>(Constant.UN_LIKE_VIDEO)
                .params(params)
                .execute(object : JsonCallBack<BaseResponse<String>>(){
                    override fun onSuccess(response: Response<BaseResponse<String>>?) {
                        if(response?.body()?.res == 0){
                            videoDetailBean?.isLike = null
                            likeBtn.setImageResource(R.mipmap.dianzan)
                        }
                    }

                })
    }
    /**
     * ??????????????????
     */
    private fun getCommentList(videoId:Int){
        var params = HttpParams()
        params.put("pageNum",pageNum)
        params.put("pageSize",Constant.PAGE_SIZE)
        params.put("videoId",videoId)
        OkGo.post<BaseResponse<CommentListResponse>>(Constant.VIDEO_COMMENT_LIST)
                .params(params)
                .execute(object : JsonCallBack<BaseResponse<CommentListResponse>>(){
                    override fun onSuccess(response: Response<BaseResponse<CommentListResponse>>?) {
                        if(response?.body()?.res == 0){
                            commentAdapter?.setList(response?.body()?.data?.list)
                        }else{

                        }
                    }

                })
    }
    /**
     * ????????????
     */
    private fun likeComment(commentId : Int){
        var params = HttpParams()
        params.put("commentId",commentId)
        OkGo.post<BaseResponse<String>>(Constant.COMMENT_LIKE)
                .params(params)
                .execute(object : JsonCallBack<BaseResponse<String>>(){
                    override fun onSuccess(response: Response<BaseResponse<String>>?) {
                        if(response?.body()?.res == 0){

                        }
                    }

                })
    }
    /**
     * ??????????????????
     */
    private fun unLikeComment(commentId: Int){
        var params = HttpParams()
        params.put("commentId",commentId)
        OkGo.post<BaseResponse<String>>(Constant.COMMENT_UNLIKE)
                .params(params)
                .execute(object : JsonCallBack<BaseResponse<String>>(){
                    override fun onSuccess(response: Response<BaseResponse<String>>?) {
                        if(response?.body()?.res == 0){

                        }
                    }

                })
    }

    /**
     * ????????????
     */
    private fun publishComment(content:String){
        var params = HttpParams()
        params.put("content",content)
        params.put("videoId",videoDetailBean!!.videoId)
        params.put("type",1)
        OkGo.post<BaseResponse<String>>(Constant.VIDEO_COMMENT)
                .params(params)
                .execute(object : JsonCallBack<BaseResponse<String>>(){
                    override fun onSuccess(response: Response<BaseResponse<String>>?) {
                        if(response?.body()?.res == 0){
                            toast("????????????")
                            getCommentList(videoDetailBean!!.videoId)
                        }else{
                            toast(response?.body()?.msg.toString())
                        }
                    }

                })
    }
    /**
     * ?????????????????? ????????? ????????????
     */
    private fun getRecPayInfo(orderId:String) {
        var params = HttpParams()
        params.put("orderId", orderId)
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
     * ???????????????
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

        // ??????????????????
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
                     * ???????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????
                     */
                    val resultInfo = payResult.getResult()// ?????????????????????????????????
                    val resultStatus = payResult.getResultStatus()
                    // ??????resultStatus ???9000?????????????????????
                    if (TextUtils.equals(resultStatus, "9000")) {
                        // ??????????????????????????????????????????????????????????????????????????????
                        toast("????????????")
                        getDetail(videoDetailBean!!.videoId)
                    } else {
                        // ???????????????????????????????????????????????????????????????????????????
                        toast("????????????")
                    }
                }
                else -> {
                }
            }
        }
    }
    /**
     * ????????????
     */
    private fun collectVideo(videoId: Int?){
        var params = HttpParams()
        params.put("videoId",videoId!!)
        OkGo.post<BaseResponse<String>>(Constant.VIDEO_COLLECT)
                .params(params)
                .execute(object:JsonCallBack<BaseResponse<String>>(){
                    override fun onSuccess(response: Response<BaseResponse<String>>?) {
                        if(response?.body()?.res == 0){
                            videoDetailBean?.isCollect = "1"
                            collectBtn.setImageResource(R.mipmap.shoucang1)
                        }else{

                        }
                    }

                })
    }
    /**
     * ??????????????????
     */
    private fun cancelCollectVideo(videoId:Int?){
        var params = HttpParams()
        params.put("videoId",videoId!!)
        OkGo.post<BaseResponse<String>>(Constant.CANCEL_VIDEO_COLLECT)
                .params(params)
                .execute(object:JsonCallBack<BaseResponse<String>>(){
                    override fun onSuccess(response: Response<BaseResponse<String>>?) {
                        if(response?.body()?.res == 0){
                            videoDetailBean?.isCollect = null
                            collectBtn.setImageResource(R.mipmap.shoucang)
                        }else{

                        }
                    }

                })
    }
    /**
     * ????????????
     */
    private fun replyComment(content:String,commentId:Int,replyId:Int){
        var params = HttpParams()
        params.put("content",content)
        params.put("videoId",videoDetailBean!!.videoId)
        params.put("type",2)
        params.put("commentId",commentId)
        params.put("replyId",replyId)
        OkGo.post<BaseResponse<String>>(Constant.VIDEO_COMMENT)
                .params(params)
                .execute(object : JsonCallBack<BaseResponse<String>>(){
                    override fun onSuccess(response: Response<BaseResponse<String>>?) {
                        if(response?.body()?.res == 0){
                            getCommentList(videoDetailBean!!.videoId)
                        }else{
                            toast(response?.body()?.msg.toString())
                        }
                    }

                })
    }
}
