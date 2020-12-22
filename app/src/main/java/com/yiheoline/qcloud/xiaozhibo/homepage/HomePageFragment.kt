package com.yiheoline.qcloud.xiaozhibo.homepage

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.chad.library.adapter.base.BaseQuickAdapter
import com.lzy.okgo.OkGo
import com.lzy.okgo.model.HttpParams
import com.lzy.okgo.model.Response
import com.shuyu.gsyvideoplayer.GSYVideoManager
import com.yiheoline.qcloud.xiaozhibo.Constant
import com.yiheoline.qcloud.xiaozhibo.anchor.prepare.TCAnchorPrepareActivity
import com.yiheoline.qcloud.xiaozhibo.base.BaseFragment
import com.yiheoline.qcloud.xiaozhibo.bean.ShowNoticeBean
import com.yiheoline.qcloud.xiaozhibo.dialog.StartPlayDialog
import com.yiheoline.qcloud.xiaozhibo.homepage.adapter.CatTabAdapter
import com.yiheoline.qcloud.xiaozhibo.homepage.adapter.NearShowListAdapter
import com.yiheoline.qcloud.xiaozhibo.homepage.adapter.PreListAdapter
import com.yiheoline.qcloud.xiaozhibo.http.BaseResponse
import com.yiheoline.qcloud.xiaozhibo.http.JsonCallBack
import com.yiheoline.qcloud.xiaozhibo.http.response.HomePageResponse
import com.yiheoline.qcloud.xiaozhibo.login.LoginActivity
import com.yiheonline.qcloud.xiaozhibo.R
import kotlinx.android.synthetic.main.fragment_home_page.*
import kotlinx.android.synthetic.main.near_show_item_layout.*
import kotlinx.android.synthetic.main.select_item_layout.*
import kotlinx.android.synthetic.main.view_title.view.*
import org.jetbrains.anko.sdk27.coroutines.onClick
import org.jetbrains.anko.sdk27.coroutines.onTouch
import org.jetbrains.anko.support.v4.startActivity
import org.jetbrains.anko.support.v4.toast

class HomePageFragment : BaseFragment() {
    private var mParam1: String? = null
    private var mParam2: String? = null
    private var preListAdapter : PreListAdapter? = null
    private var selectAdapter : NearShowListAdapter? = null
    private var nearShowListAdapter : NearShowListAdapter? = null
    private var categoryList = arrayListOf("精选","话剧","粤剧","京剧","黄梅戏","音乐会","歌舞剧","花鼓戏")

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

        //初始化预告rv
        var manager = LinearLayoutManager(context)
        manager.orientation = LinearLayoutManager.HORIZONTAL
        recyclerView.layoutManager = manager
        preListAdapter = PreListAdapter(R.layout.pre_list_item_layout, arrayListOf())
        recyclerView.adapter = preListAdapter
        preListAdapter?.setOnItemClickListener { _, _, position ->
            preListAdapter?.selectPosition = position
            preListAdapter?.notifyDataSetChanged()
        }

        //初始化类别RV
        var catManager = LinearLayoutManager(context)
        catManager.orientation = LinearLayoutManager.HORIZONTAL
        catRecyclerView.layoutManager = catManager
        var catAdapter = CatTabAdapter(R.layout.cat_tab_layout,categoryList)
        catRecyclerView.adapter = catAdapter
        catAdapter.setOnItemClickListener { _, _, position ->
            catAdapter.selectPosition = position
            catAdapter.notifyDataSetChanged()
        }
        //初始化最近直播RV
        nearShowRecycler.layoutManager = GridLayoutManager(context,3)
        nearShowListAdapter = NearShowListAdapter(R.layout.near_show_list_item_layout, arrayListOf())
        nearShowRecycler.adapter = nearShowListAdapter
        nearShowListAdapter?.setOnItemClickListener { _, _, position ->
            startActivity<NoticeDetailActivity>("noticeBean" to nearShowListAdapter!!.data[position])
        }

        //初始化精选推荐RV
        selectRecycler.layoutManager = GridLayoutManager(context,3)
        selectAdapter = NearShowListAdapter(R.layout.near_show_list_item_layout, arrayListOf())
        selectRecycler.adapter = selectAdapter
        selectAdapter?.setOnItemClickListener { _, _, position ->
            startActivity<NoticeDetailActivity>("noticeBean" to nearShowListAdapter!!.data[position])
        }

        startPlay.onClick {
            StartPlayDialog.onCreateDialog(context!!)
        }

        keyWordsInputView.onClick {
            startActivity<SearchActivity>()
        }

        //获取数据
        getPreList()
        getNearList()
        getRecommendList()
    }

    private fun initVideoPlayer(){
        //初始化视频播放器
        val source1 = "http://9890.vod.myqcloud.com/9890_4e292f9a3dd011e6b4078980237cc3d3.f20.mp4"
//        var source1 = Constant.VIDEO_BASE+preListAdapter!!.data[preListAdapter!!.selectPosition].videoDetail
        videoPlayer.setUp(source1,true,"测试视频")
        videoPlayer.isLooping = true
        videoPlayer.setIsTouchWiget(false)
        videoPlayer.isHideKey = true
        videoPlayer.dismissControlTime = 0
        videoPlayer.isStartAfterPrepared = true
        videoPlayer.isReleaseWhenLossAudio = false
        videoPlayer.startPlayLogic()
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

    override fun onPause() {
        super.onPause()
        videoPlayer.onVideoPause()
    }

    override fun onResume() {
        super.onResume()
        videoPlayer.onVideoResume()
    }

    override fun onDestroy() {
        super.onDestroy()
        GSYVideoManager.releaseAllVideos();
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