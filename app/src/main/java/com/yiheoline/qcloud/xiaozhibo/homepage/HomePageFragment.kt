package com.yiheoline.qcloud.xiaozhibo.homepage

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.bilibili.boxing.Boxing
import com.bilibili.boxing.BoxingMediaLoader
import com.bilibili.boxing.model.config.BoxingConfig
import com.bilibili.boxing_impl.ui.BoxingActivity
import com.bumptech.glide.Glide
import com.lzy.okgo.OkGo
import com.lzy.okgo.model.HttpParams
import com.lzy.okgo.model.Response
import com.shuyu.gsyvideoplayer.GSYVideoManager
import com.tencent.liteav.demo.videorecord.TCVideoRecordActivity
import com.tencent.liteav.demo.videouploader.ui.TCVideoPublishActivity
import com.tencent.liteav.demo.videouploader.ui.utils.Constants
import com.tencent.mmkv.MMKV
import com.tencent.qcloud.ugckit.UGCKitConstants
import com.tencent.rtmp.TXLiveConstants
import com.tencent.ugc.TXRecordCommon
import com.yiheoline.qcloud.xiaozhibo.Constant
import com.yiheoline.qcloud.xiaozhibo.TCApplication
import com.yiheoline.qcloud.xiaozhibo.base.BaseFragment
import com.yiheoline.qcloud.xiaozhibo.bean.TypeBean
import com.yiheoline.qcloud.xiaozhibo.dialog.ConfirmDialog
import com.yiheoline.qcloud.xiaozhibo.dialog.StartPlayDialog
import com.yiheoline.qcloud.xiaozhibo.homepage.adapter.CatTabAdapter
import com.yiheoline.qcloud.xiaozhibo.homepage.adapter.NearShowListAdapter
import com.yiheoline.qcloud.xiaozhibo.homepage.adapter.PreListAdapter
import com.yiheoline.qcloud.xiaozhibo.homepage.adapter.SelectListAdapter
import com.yiheoline.qcloud.xiaozhibo.http.BaseResponse
import com.yiheoline.qcloud.xiaozhibo.http.JsonCallBack
import com.yiheoline.qcloud.xiaozhibo.http.response.HomeChoiceResponse
import com.yiheoline.qcloud.xiaozhibo.http.response.HomePageResponse
import com.yiheoline.qcloud.xiaozhibo.http.response.ShortVideoListResponse
import com.yiheoline.qcloud.xiaozhibo.profile.CompanyAuthActivity
import com.yiheoline.qcloud.xiaozhibo.show.TCVodPlayerActivity
import com.yiheoline.qcloud.xiaozhibo.show.adapter.ShowListAdapter
import com.yiheoline.qcloud.xiaozhibo.utils.BoxingGlideLoader
import com.yiheoline.qcloud.xiaozhibo.video.VideoDetailActivity
import com.yiheoline.qcloud.xiaozhibo.video.adapter.VideoListAdapter
import com.yiheonline.qcloud.xiaozhibo.R
import kotlinx.android.synthetic.main.activity_edit_user_info2.*
import kotlinx.android.synthetic.main.fragment_home_page.*
import kotlinx.android.synthetic.main.fragment_home_page.recyclerView
import kotlinx.android.synthetic.main.fragment_show.*
import kotlinx.android.synthetic.main.home_ad_layout.*
import kotlinx.android.synthetic.main.home_short_item_layout.*
import kotlinx.android.synthetic.main.home_video_item_layout.*
import kotlinx.android.synthetic.main.near_show_item_layout.*
import kotlinx.android.synthetic.main.select_item_layout.*
import org.jetbrains.anko.sdk27.coroutines.onClick
import org.jetbrains.anko.support.v4.startActivity
import org.jetbrains.anko.support.v4.toast
import java.io.Serializable

class HomePageFragment : BaseFragment() {
    private var mParam1: String? = null
    private var mParam2: String? = null
    private var pageNum = 1
    private var preListAdapter : PreListAdapter? = null
    private var selectAdapter : SelectListAdapter? = null
    private var nearShowListAdapter : NearShowListAdapter? = null
    private var listByCatAdapter : NearShowListAdapter? = null
    private var videoSelectAdapter : VideoListAdapter? = null
    private var shortListAdapter : ShowListAdapter? = null
    private var catAdapter : CatTabAdapter? = null
    private var oldPosition = 0
    private var currentVideoPath = ""
    private var isPrepared = false
    override fun getLayout(): Int {
        return R.layout.fragment_home_page
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (arguments != null) {
            mParam1 = arguments!!.getString(ARG_PARAM1)
            mParam2 = arguments!!.getString(ARG_PARAM2)
        }
    }

    override fun initView() {
        super.initView()

        BoxingMediaLoader.getInstance().init(BoxingGlideLoader()) // 需要实现IBoxingMediaLoader
        //初始化预告rv
        var manager = LinearLayoutManager(context)
        manager.orientation = LinearLayoutManager.HORIZONTAL
        recyclerView.layoutManager = manager
        preListAdapter = PreListAdapter(R.layout.pre_list_item_layout, arrayListOf())
        recyclerView.adapter = preListAdapter
        preListAdapter?.setOnItemClickListener { _, _, position ->
            if(oldPosition == position){
                startActivity<NoticeDetailActivity>("noticeId" to preListAdapter!!.data[position].noticeId.toString())
            }else{
                preListAdapter?.selectPosition = position
                preListAdapter?.notifyDataSetChanged()
                currentVideoPath = Constant.VIDEO_BASE+preListAdapter!!.data[preListAdapter!!.selectPosition].videoDetail
                videoPlayer.setUp(currentVideoPath,true,"")
                videoPlayer.startPlayLogic()
            }
            oldPosition = position
        }
        preListAdapter?.addChildClickViewIds(R.id.toDetailBtn)
        preListAdapter?.setOnItemChildClickListener { _, _, position ->
            startActivity<NoticeDetailActivity>("noticeId" to preListAdapter!!.data[position].noticeId.toString())
        }
        //初始化类别RV
        var catManager = LinearLayoutManager(context)
        catManager.orientation = LinearLayoutManager.HORIZONTAL
        catRecyclerView.layoutManager = catManager
        catAdapter = CatTabAdapter(R.layout.cat_tab_layout, arrayListOf())
        catRecyclerView.adapter = catAdapter
        catAdapter?.setOnItemClickListener { _, _, position ->
            catAdapter?.selectPosition = position
            catAdapter?.notifyDataSetChanged()
            if(position == 0){
                selectContain.visibility = View.VISIBLE
                catListRv.visibility = View.GONE
                videoPlayer.onVideoResume()
                getSelectData()
            }else{
                videoPlayer.onVideoPause()
                selectContain.visibility = View.GONE
                catListRv.visibility = View.VISIBLE
                getListByType(catAdapter!!.data[position].catId)
            }
        }
        //初始化最近直播RV
        nearShowRecycler.layoutManager = LinearLayoutManager(context,LinearLayoutManager.HORIZONTAL,false)

        nearShowListAdapter = NearShowListAdapter(R.layout.near_show_list_item_layout, arrayListOf())

        nearShowRecycler.adapter = nearShowListAdapter
        nearShowListAdapter?.setOnItemClickListener { _, _, position ->
            startActivity<NoticeDetailActivity>("noticeId" to nearShowListAdapter!!.data[position].noticeId.toString())
        }

        //初始化精选推荐RV
        selectRecycler.layoutManager = GridLayoutManager(context,3)
        selectAdapter = SelectListAdapter(R.layout.home_select_item_layout, arrayListOf())
        selectRecycler.adapter = selectAdapter
        selectAdapter?.setOnItemClickListener { _, _, position ->
            startActivity<NoticeDetailActivity>("noticeId" to nearShowListAdapter!!.data[position].noticeId.toString())
        }

        //初始化分类列表RV
        catListRv.layoutManager = GridLayoutManager(context,3)
        listByCatAdapter = NearShowListAdapter(R.layout.near_show_list_item_layout, arrayListOf())
        catListRv.adapter = listByCatAdapter
        listByCatAdapter?.setOnItemClickListener { _, _, position ->
            startActivity<NoticeDetailActivity>("noticeId" to nearShowListAdapter!!.data[position].noticeId.toString())
        }

        //初始化剧场推荐RV
        videoRecycler.layoutManager = GridLayoutManager(context,2)
        videoSelectAdapter = VideoListAdapter(R.layout.video_list_item, arrayListOf())
        videoRecycler.adapter = videoSelectAdapter
        videoSelectAdapter?.setOnItemClickListener { _, _, position ->
            startActivity<VideoDetailActivity>("videoId" to videoSelectAdapter!!.data[position].videoId)
        }

        //互动专区RV
        shortRecycler.layoutManager = GridLayoutManager(context,2)
        shortListAdapter = ShowListAdapter(R.layout.show_list_item_layout, arrayListOf())
        shortRecycler.adapter = shortListAdapter
        shortListAdapter?.setOnItemClickListener { _, _, position ->
            //跳转到播放页面
            var item = shortListAdapter!!.data[position]
            val intent = Intent(activity, TCVodPlayerActivity::class.java)
            intent.putExtra(UGCKitConstants.PLAY_URL, item.videoPath)
            intent.putExtra(UGCKitConstants.PUSHER_NAME, item.nickname)
            intent.putExtra(UGCKitConstants.PUSHER_AVATAR, item.avatar)
            intent.putExtra(UGCKitConstants.COVER_PIC, item.cover)
            intent.putExtra(UGCKitConstants.FILE_ID, item.shortVideoId)
            intent.putExtra(UGCKitConstants.TCLIVE_INFO_LIST, shortListAdapter!!.data as Serializable)
            intent.putExtra(UGCKitConstants.TIMESTAMP, item.createTime)
            intent.putExtra(UGCKitConstants.TCLIVE_INFO_POSITION, position)
            startActivityForResult(intent, 2000)
        }

        Glide.with(context).load(R.mipmap.start_btn_gif).asGif().crossFade().into(startPlay)

        isPrepared = true

    }

    override fun lazyLoad() {
        //获取首页信息
        getHomeInfo()
        //获取互动视频列表
        getVideoList()
    }

    override fun initData() {
        super.initData()
        //获取顶部分类 暂时不用
//        getTypeList()

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode == AppCompatActivity.RESULT_OK){
            var medias = Boxing.getResult(data)
            if(medias != null){
                startActivity<TCVideoPublishActivity>(Constants.VIDEO_EDITER_PATH to medias[0].path)
            }
        }
    }

    override fun initListener() {
        super.initListener()

        startPlay.onClick {
            if(topDialogView.visibility == View.GONE){
                topDialogView.visibility = View.VISIBLE
            }else{
                topDialogView.visibility = View.GONE
            }
        }
        closeDialog.onClick {
            topDialogView.visibility = View.GONE
        }
        topDialogView.onClick {  }

        showPlay.onClick {
            if(isAuth())
                startActivity(Intent(context,ShowSettingActivity::class.java))
            topDialogView.visibility = View.GONE
        }
        showerPlayBtn.onClick {
            if(isAuth())
                startActivity(Intent(context,ShowerPlayStartActivity::class.java))
            topDialogView.visibility = View.GONE
        }
        shotBtn.onClick {
            if(isAuth()){
                var intent = Intent(context, TCVideoRecordActivity::class.java)
                intent.putExtra(UGCKitConstants.RECORD_CONFIG_MIN_DURATION,5 * 1000)
                intent.putExtra(UGCKitConstants.RECORD_CONFIG_MAX_DURATION,60 * 1000)
                intent.putExtra(UGCKitConstants.RECORD_CONFIG_ASPECT_RATIO, TXRecordCommon.VIDEO_ASPECT_RATIO_9_16)
                intent.putExtra(UGCKitConstants.RECORD_CONFIG_RECOMMEND_QUALITY, TXRecordCommon.VIDEO_QUALITY_MEDIUM)
                intent.putExtra(UGCKitConstants.RECORD_CONFIG_HOME_ORIENTATION, TXLiveConstants.VIDEO_ANGLE_HOME_DOWN)
                intent.putExtra(UGCKitConstants.RECORD_CONFIG_TOUCH_FOCUS,false)
                intent.putExtra(UGCKitConstants.RECORD_CONFIG_NEED_EDITER,true)
                startActivity(intent)
            }
            topDialogView.visibility = View.GONE
        }
        upLoadBtn.onClick {
            if(isAuth()){
                var config = BoxingConfig(BoxingConfig.Mode.VIDEO)
                config.needCamera(R.mipmap.ic_boxing_camera_white)
                Boxing.of(config).withIntent(context, BoxingActivity::class.java).start(this@HomePageFragment,200)
            }
            topDialogView.visibility = View.GONE
        }
        keyWordsInputView.onClick {
            startActivity<SearchActivity>()
        }

        recentCheckMore.onClick {
            startActivity<NoticeListActivity>()
        }
        selectCheckMore.onClick {
            startActivity<NoticeListActivity>()
        }
        openMusicView.onClick {
            if(GSYVideoManager.instance().isNeedMute){
                GSYVideoManager.instance().isNeedMute = false
                openMusicView.setImageResource(R.mipmap.close_music)
            }else{
                GSYVideoManager.instance().isNeedMute = true
                openMusicView.setImageResource(R.mipmap.open_music)
            }

        }
    }

    /**
     * 获取到数据后刷新页面内容
     */
    private fun refreshUi(homeChoiceResponse: HomeChoiceResponse){
        adLayout?.visibility = View.VISIBLE
        nearLayout?.visibility = View.VISIBLE
        selectLayout?.visibility = View.VISIBLE
        shortLayout?.visibility = View.VISIBLE
        videoLayout?.visibility = View.VISIBLE
        //顶部带视频的预告
        preListAdapter?.setList(homeChoiceResponse.showList)
        initVideoPlayer()

        //最近直播
        nearShowListAdapter?.setList(homeChoiceResponse.latelyList)
        //推荐的
        selectAdapter?.setList(homeChoiceResponse.recommendList)
        //剧场推荐的
        videoSelectAdapter?.setList(homeChoiceResponse.videoList)
        //互动专区
//        shortListAdapter?.setList(homeChoiceResponse.shortVideoList)
    }

    /**
     * 获取精选数据
     */
    private fun getSelectData(){
        getPreList()
        getNearList()
        getRecommendList()
    }

    private fun initVideoPlayer(){
        //初始化视频播放器
        if(preListAdapter!!.data.size == 0){
            return
        }
        currentVideoPath= Constant.VIDEO_BASE+preListAdapter!!.data[preListAdapter!!.selectPosition].videoDetail
        videoPlayer.setUp(currentVideoPath,true,"测试视频")
        videoPlayer.isLooping = true
        videoPlayer.setIsTouchWiget(false)
        videoPlayer.isHideKey = true
        videoPlayer.dismissControlTime = 0
        videoPlayer.isStartAfterPrepared = true
        videoPlayer.isReleaseWhenLossAudio = false
        videoPlayer.startPlayLogic()
    }

    private fun isAuth():Boolean{
        var userType = MMKV.defaultMMKV().decodeInt("userType",1)
        return if(userType == 1){
            ConfirmDialog.onCreateDialog(context,"此功能需先进行身份认证！",object : ConfirmDialog.ConfirmListener{
                override fun onConfirm(isConfirm: Boolean) {
                    if(isConfirm){
                        startActivity<CompanyAuthActivity>()
                    }
                }

            })
            false
        }else{
            true
        }
    }

    private fun getHomeInfo(){
        OkGo.post<BaseResponse<HomeChoiceResponse>>(Constant.NOTICE_QUERY_CHOICE)
                .execute(object:JsonCallBack<BaseResponse<HomeChoiceResponse>>(){
                    override fun onSuccess(response: Response<BaseResponse<HomeChoiceResponse>>?) {
                        if(response?.body()?.res == 0){
                            refreshUi(response.body()?.data!!)
                        }else{
                            toast(response?.body()?.msg.toString())
                        }
                    }

                })
    }

    /**
     * 获取首页直播预告信息
     */
    private fun getPreList(){
        var params = HttpParams()
        params.put("isShow",1)
        OkGo.post<BaseResponse<HomePageResponse>>(Constant.HOME_PAGE_LIST)
                .params(params)
                .execute(object : JsonCallBack<BaseResponse<HomePageResponse>>(){
                    override fun onSuccess(response: Response<BaseResponse<HomePageResponse>>?) {
                        if(response?.body()?.res == 0){
                            preListAdapter?.setList(response.body().data.list)
                            initVideoPlayer()
                        }else{
                            toast(response?.body()?.msg.toString())
                        }
                    }

                })
    }

    /**
     * 获取首页最近直播信息
     */
    private fun getNearList(){
        var params = HttpParams()
        params.put("lately",1)
        OkGo.post<BaseResponse<HomePageResponse>>(Constant.HOME_PAGE_LIST)
                .params(params)
                .execute(object : JsonCallBack<BaseResponse<HomePageResponse>>(){
                    override fun onSuccess(response: Response<BaseResponse<HomePageResponse>>?) {
                        if(response?.body()?.res == 0){
                            nearShowListAdapter?.setList(response.body().data!!.list)
                        }else{
                            toast(response?.body()?.msg.toString())
                        }
                    }

                })
    }

    /**
     * 获取首页推荐信息信息
     */
    private fun getRecommendList(){
        var params = HttpParams()
        params.put("isRecommend",1)
        OkGo.post<BaseResponse<HomePageResponse>>(Constant.HOME_PAGE_LIST)
                .params(params)
                .execute(object : JsonCallBack<BaseResponse<HomePageResponse>>(){
                    override fun onSuccess(response: Response<BaseResponse<HomePageResponse>>?) {
                        if(response?.body()?.res == 0){
                            selectAdapter?.setList(response.body().data!!.list)
                        }else{
                            toast(response?.body()?.msg.toString())
                        }
                    }

                })
    }

    /**
     * 根据分类获取预告列表
     */
    private fun getListByType(catId:Int){
        var params = HttpParams()
        params.put("catId",catId)
        OkGo.post<BaseResponse<HomePageResponse>>(Constant.HOME_PAGE_LIST)
                .params(params)
                .execute(object : JsonCallBack<BaseResponse<HomePageResponse>>(){
                    override fun onSuccess(response: Response<BaseResponse<HomePageResponse>>?) {
                        if(response?.body()?.res == 0){
                            listByCatAdapter?.setList(response.body().data!!.list)
                        }else{
                            toast(response?.body()?.msg.toString())
                        }
                    }

                })
    }

    /**
     * 获取分类集合
     */
    private fun getTypeList(){
        OkGo.get<BaseResponse<List<TypeBean>>>(Constant.QUERY_CAT_LIST)
                .execute(object : JsonCallBack<BaseResponse<List<TypeBean>>>(){
                    override fun onSuccess(response: Response<BaseResponse<List<TypeBean>>>?) {
                        if(response?.body()?.res == 0){
                            catAdapter?.setList(response.body()?.data!!)
                            var typeBean = TypeBean()
                            typeBean.name = "精选"
                            catAdapter?.addData(0,typeBean)
                        }else{
                            toast(response?.body()?.msg.toString())
                        }
                    }

                })
    }

    /**
     * 获取短视频列表
     */
    private fun getVideoList(){
        var params = HttpParams()
        params.put("pageNum",pageNum)
        params.put("pageSize", Constant.PAGE_SIZE)
        OkGo.post<BaseResponse<ShortVideoListResponse>>(Constant.SHORT_VIDEO_LIST)
                .params(params)
                .execute(object : JsonCallBack<BaseResponse<ShortVideoListResponse>>(){

                    override fun onFinish() {
                        super.onFinish()
                        shortListAdapter?.loadMoreModule?.loadMoreComplete()
                    }

                    override fun onError(response: Response<BaseResponse<ShortVideoListResponse>>?) {
                        super.onError(response)
                        shortListAdapter?.loadMoreModule?.loadMoreFail()
                    }
                    override fun onSuccess(response: Response<BaseResponse<ShortVideoListResponse>>?) {
                        if(response?.body()?.res == 0){
                            if(pageNum == 1){
                                shortListAdapter?.setList(response.body()?.data?.list)
                            }else{
                                shortListAdapter?.addData(response.body().data.list)
                            }
                        }else{
                            toast(response?.body()?.msg.toString())
                        }
                    }

                })
    }

    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)
        if(userVisibleHint){
            videoPlayer?.onVideoResume()
        }else{
            videoPlayer?.onVideoPause()
        }
    }

    override fun onResume() {
        super.onResume()
        if(TCApplication.isRelease){
            videoPlayer.setUp(currentVideoPath,true,"测试视频")
            videoPlayer.startPlayLogic()
            videoPlayer.seekOnStart
            TCApplication.isRelease = false
        }else{
            videoPlayer.onVideoResume()
        }
        GSYVideoManager.instance().setPlayerInitSuccessListener { player, model ->
            GSYVideoManager.instance().isNeedMute = true
        }
    }

    override fun onPause() {
        super.onPause()
        videoPlayer.onVideoPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        GSYVideoManager.releaseAllVideos()
    }


    companion object {
        private const val ARG_PARAM1 = "param1"
        private const val ARG_PARAM2 = "param2"

        @JvmStatic
        fun newInstance(param1: String?, param2: String?): HomePageFragment {
            val fragment = HomePageFragment()
            val args = Bundle()
            args.putString(ARG_PARAM1, param1)
            args.putString(ARG_PARAM2, param2)
            fragment.arguments = args
            return fragment
        }
    }
}