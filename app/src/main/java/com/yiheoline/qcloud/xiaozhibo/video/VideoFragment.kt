package com.yiheoline.qcloud.xiaozhibo.video

import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewpager.widget.ViewPager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.lzy.okgo.OkGo
import com.lzy.okgo.model.HttpParams
import com.lzy.okgo.model.Response
import com.yiheoline.qcloud.xiaozhibo.Constant
import com.yiheoline.qcloud.xiaozhibo.base.BaseFragment
import com.yiheoline.qcloud.xiaozhibo.bean.AdBean
import com.yiheoline.qcloud.xiaozhibo.bean.TypeBean
import com.yiheoline.qcloud.xiaozhibo.homepage.adapter.CatTabAdapter
import com.yiheoline.qcloud.xiaozhibo.video.adapter.VideoListAdapter
import com.yiheoline.qcloud.xiaozhibo.http.BaseResponse
import com.yiheoline.qcloud.xiaozhibo.http.JsonCallBack
import com.yiheoline.qcloud.xiaozhibo.http.response.HomePageResponse
import com.yiheoline.qcloud.xiaozhibo.http.response.VideoChoiceResponse
import com.yiheoline.qcloud.xiaozhibo.http.response.VideoListResponse
import com.yiheoline.qcloud.xiaozhibo.utils.GlideImageLoader
import com.yiheoline.qcloud.xiaozhibo.utils.GlideRoundTransform
import com.yiheoline.qcloud.xiaozhibo.video.adapter.SecondVideoListAdapter
import com.yiheonline.qcloud.xiaozhibo.R
import com.youth.banner.BannerConfig
import kotlinx.android.synthetic.main.all_see_item_layout.*
import kotlinx.android.synthetic.main.fragment_video.*
import kotlinx.android.synthetic.main.fragment_video.catRecyclerView
import kotlinx.android.synthetic.main.fragment_video.keyWordsInputView
import kotlinx.android.synthetic.main.fragment_video.selectContain
import kotlinx.android.synthetic.main.free_video_item_layout.*
import kotlinx.android.synthetic.main.near_show_item_layout.*
import kotlinx.android.synthetic.main.select_item_layout.*
import kotlinx.android.synthetic.main.select_item_layout.selectCheckMore
import kotlinx.android.synthetic.main.select_item_layout.selectRecycler
import org.jetbrains.anko.sdk27.coroutines.onClick
import org.jetbrains.anko.support.v4.startActivity
import org.jetbrains.anko.support.v4.toast
import kotlin.math.sin

class VideoFragment : BaseFragment() {
    var pageNum = 1
    var catId = 0
    private var mParam1: String? = null
    private var mParam2: String? = null
    private var selectAdapter : VideoListAdapter? = null
    private var nearShowListAdapter : SecondVideoListAdapter? = null
    private var listByCatAdapter : VideoListAdapter? = null
    private var freeAdapter : VideoListAdapter? = null
    private var catAdapter : CatTabAdapter? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (arguments != null) {
            mParam1 = arguments!!.getString(ARG_PARAM1)
            mParam2 = arguments!!.getString(ARG_PARAM2)
        }
    }

    override fun lazyLoad() {
        getTypeList()
        getList()
    }

    override fun getLayout(): Int {
        return R.layout.fragment_video
    }

    override fun initData() {
        super.initData()
    }

    override fun initView() {
        super.initView()
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
                listByCatRv.visibility = View.GONE
                getList()
            }else{
                selectContain.visibility = View.GONE
                listByCatRv.visibility = View.VISIBLE
                catId = catAdapter!!.data[position].catId
                getListByType()
            }
        }
        //初始化最近直播RV
        nearShowRecycler.layoutManager = GridLayoutManager(context,3)
        nearShowListAdapter = SecondVideoListAdapter(R.layout.video_list_item2, arrayListOf())
        nearShowRecycler.adapter = nearShowListAdapter
        nearShowListAdapter?.setOnItemClickListener { _, _, position ->
            startActivity<VideoDetailActivity>("videoId" to nearShowListAdapter!!.data[position].videoId)
        }

        //初始化精选推荐RV
        selectRecycler.layoutManager = GridLayoutManager(context,2)
        selectAdapter = VideoListAdapter(R.layout.video_list_item, arrayListOf())
        selectRecycler.adapter = selectAdapter
        selectAdapter?.setOnItemClickListener { _, _, position ->
            startActivity<VideoDetailActivity>("videoId" to selectAdapter!!.data[position].videoId)
        }
        //初始化分类显示列表RV
        listByCatRv.layoutManager = GridLayoutManager(context,2)
        listByCatAdapter = VideoListAdapter(R.layout.video_list_item, arrayListOf())
        listByCatRv.adapter = listByCatAdapter
        listByCatAdapter?.loadMoreModule?.isEnableLoadMore = true
        listByCatAdapter?.loadMoreModule?.isAutoLoadMore = true
        listByCatAdapter?.loadMoreModule?.isEnableLoadMoreIfNotFullPage = false
        listByCatAdapter?.loadMoreModule?.setOnLoadMoreListener {
            pageNum++
            getListByType()
        }
        listByCatAdapter?.setOnItemClickListener { _, _, position ->
            startActivity<VideoDetailActivity>("videoId" to listByCatAdapter!!.data[position].videoId)
        }
        //初始化免费专区
        freeRecycler.layoutManager = GridLayoutManager(context,2)
        freeAdapter = VideoListAdapter(R.layout.video_list_item, arrayListOf())
        freeRecycler.adapter = freeAdapter
        freeAdapter?.setOnItemClickListener { _, _, position ->
            startActivity<VideoDetailActivity>("videoId" to freeAdapter!!.data[position].videoId)
        }

        keyWordsInputView.onClick{
            startActivity<VideoSearchActivity>()
        }
    }

    override fun initListener() {
        super.initListener()
        recentCheckMore.onClick { startActivity<VideoListActivity>("type" to 0) }
        selectCheckMore.onClick { startActivity<VideoListActivity>("type" to 1) }
        freeCheckMore.onClick { startActivity<VideoListActivity>("type" to 2) }
    }

    private fun initAdView(adList: List<AdBean>) {
        var imageList = arrayListOf<String>("https://ss3.bdstatic.com/70cFv8Sh_Q1YnxGkpoWK1HF6hhy/it/u=3141142430,3556848497&fm=26&gp=0.jpg",
        "https://gimg2.baidu.com/image_search/src=http%3A%2F%2Fimage1.thepaper.cn%2Fimage%2F5%2F266%2F138.jpg&refer=http%3A%2F%2Fimage1.thepaper.cn&app=2002&size=f9999,10000&q=a80&n=0&g=0n&fmt=jpeg?sec=1615015135&t=facc8d8f1fb22ebc1407de1d37d4e3ad")
        var titles = arrayListOf("立冬：众明星倾力演艺","亲爱的胡雪严：内地巡演")
        for (item in adList) {
            imageList.add("${Constant.IMAGE_BASE}/${item.filePath}")
        }
        if(imageList.size == 0){
            banner?.visibility = View.GONE
            return
        }else{
            banner?.visibility = View.VISIBLE
        }
        banner?.setIndicatorGravity(BannerConfig.RIGHT)
        banner?.setImageLoader(GlideImageLoader())
        banner?.setImages(imageList)
        banner?.start()
        banner?.setOnBannerListener {
//            var adBean = adList[it]
            startActivity<VideoDetailActivity>("videoId" to 41)
        }

        banner.setOnPageChangeListener(object : ViewPager.OnPageChangeListener{
            override fun onPageScrollStateChanged(state: Int) {

            }

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {

            }

            override fun onPageSelected(position: Int) {
                bannerTitleView?.text = titles[position]
            }

        })
    }

    private fun initUi(videoChoiceResponse: VideoChoiceResponse){
        initAdView(videoChoiceResponse.advertList)
        nearShowListAdapter?.setList(videoChoiceResponse.latelyList)
        freeAdapter?.setList(videoChoiceResponse.freeList)

        var hotList = videoChoiceResponse.hotList
        var singleBean = hotList[0]
        Glide.with(context).load(Constant.IMAGE_BASE+singleBean.secondCover)
                .transform(CenterCrop(context), GlideRoundTransform(context,5))
                .into(singleImageView)
        singleTitleView?.text = singleBean.title
        singleDescView?.text = singleBean.subtitle
        timeView?.text = singleBean.duration
        hotList.removeAt(0)

        selectAdapter?.setList(hotList)
        singleContain?.onClick {
            startActivity<VideoDetailActivity>("videoId" to singleBean.videoId)
        }
    }
    /**
     * 获取首页数据
     */
    private fun getList(){
        OkGo.post<BaseResponse<VideoChoiceResponse>>(Constant.VIDEO_QUERY_CHOICE)
                .execute(object : JsonCallBack<BaseResponse<VideoChoiceResponse>>(){
                    override fun onSuccess(response: Response<BaseResponse<VideoChoiceResponse>>?) {
                        if(response?.body()?.res == 0){
                            initUi(response.body().data)
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
     * 根据分类获取预告列表
     */
    private fun getListByType(){
        var params = HttpParams()
        params.put("catId",catId)
        params.put("pageNum",pageNum)
        params.put("pageSize", Constant.PAGE_SIZE)
        OkGo.post<BaseResponse<VideoListResponse>>(Constant.QUERY_VIDEO_LIST)
                .params(params)
                .execute(object : JsonCallBack<BaseResponse<VideoListResponse>>(){
                    override fun onFinish() {
                        super.onFinish()
                        listByCatAdapter?.loadMoreModule?.loadMoreComplete()
                    }

                    override fun onError(response: Response<BaseResponse<VideoListResponse>>?) {
                        super.onError(response)
                        listByCatAdapter?.loadMoreModule?.loadMoreFail()
                    }
                    override fun onSuccess(response: Response<BaseResponse<VideoListResponse>>?) {
                        if(response?.body()?.res == 0){
                            if(pageNum == 1){
                                listByCatAdapter?.setList(response.body()?.data?.list)
                            }else{
                                listByCatAdapter?.addData(response.body().data.list)
                            }
                            if(response?.body()?.data?.pages == pageNum){
                                listByCatAdapter?.loadMoreModule?.loadMoreEnd(true)
                            }
                        }else{
                            toast(response?.body()?.msg.toString())
                        }
                    }

                })
    }

    companion object {
        private const val ARG_PARAM1 = "param1"
        private const val ARG_PARAM2 = "param2"

        @JvmStatic
        fun newInstance(param1: String?, param2: String?): VideoFragment {
            val fragment = VideoFragment()
            val args = Bundle()
            args.putString(ARG_PARAM1, param1)
            args.putString(ARG_PARAM2, param2)
            fragment.arguments = args
            return fragment
        }
    }
}