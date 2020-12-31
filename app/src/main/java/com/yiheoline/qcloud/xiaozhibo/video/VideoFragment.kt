package com.yiheoline.qcloud.xiaozhibo.video

import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
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
import com.yiheonline.qcloud.xiaozhibo.R
import kotlinx.android.synthetic.main.fragment_video.*
import kotlinx.android.synthetic.main.fragment_video.catRecyclerView
import kotlinx.android.synthetic.main.fragment_video.keyWordsInputView
import kotlinx.android.synthetic.main.fragment_video.selectContain
import kotlinx.android.synthetic.main.near_show_item_layout.*
import kotlinx.android.synthetic.main.select_item_layout.*
import org.jetbrains.anko.sdk27.coroutines.onClick
import org.jetbrains.anko.support.v4.startActivity
import org.jetbrains.anko.support.v4.toast

class VideoFragment : BaseFragment() {
    private var mParam1: String? = null
    private var mParam2: String? = null
    private var selectAdapter : VideoListAdapter? = null
    private var nearShowListAdapter : VideoListAdapter? = null
    private var listByCatAdapter : VideoListAdapter? = null
    private var catAdapter : CatTabAdapter? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (arguments != null) {
            mParam1 = arguments!!.getString(ARG_PARAM1)
            mParam2 = arguments!!.getString(ARG_PARAM2)
        }
    }

    override fun getLayout(): Int {
        return R.layout.fragment_video
    }

    override fun initData() {
        super.initData()
        getTypeList()
        getList()
    }

    override fun initView() {
        super.initView()
        title2.text = "最近现场"
        title3.text = "大家都在看"
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
                getListByType(catAdapter!!.data[position].catId)
            }
        }
        //初始化最近直播RV
        nearShowRecycler.layoutManager = GridLayoutManager(context,2)
        nearShowListAdapter = VideoListAdapter(R.layout.video_list_item, arrayListOf())
        nearShowRecycler.adapter = nearShowListAdapter
        nearShowListAdapter?.setOnItemClickListener { _, _, position ->
            startActivity<VideoDetailActivity>("videoId" to nearShowListAdapter!!.data[position].videoId)
        }

        //初始化精选推荐RV
        selectRecycler.layoutManager = GridLayoutManager(context,2)
        selectAdapter = VideoListAdapter(R.layout.video_list_item, arrayListOf())
        selectRecycler.adapter = selectAdapter
        selectAdapter?.setOnItemClickListener { _, _, position ->
            startActivity<VideoDetailActivity>("videoId" to nearShowListAdapter!!.data[position].videoId)
        }
        //初始化分类显示列表RV
        listByCatRv.layoutManager = GridLayoutManager(context,2)
        listByCatAdapter = VideoListAdapter(R.layout.video_list_item, arrayListOf())
        listByCatRv.adapter = listByCatAdapter
        listByCatAdapter?.setOnItemClickListener { _, _, position ->
            startActivity<VideoDetailActivity>("videoId" to nearShowListAdapter!!.data[position].videoId)
        }

        keyWordsInputView.onClick{
            startActivity<VideoSearchActivity>()
        }
    }

    private fun initAdView(adList: List<AdBean>) {
        var imageList = arrayListOf<String>()
        for (item in adList) {
            imageList.add("${Constant.IMAGE_BASE}/${item.filePath}")
        }
        if(imageList.size == 0){
            banner.visibility = View.GONE
            return
        }else{
            banner.visibility = View.VISIBLE
        }
        banner?.setImageLoader(GlideImageLoader())
        banner?.setImages(imageList)
        banner?.start()
        banner?.setOnBannerListener {
            var adBean = adList[it]

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
                            initAdView(response?.body()?.data!!.advertList)
                            selectAdapter?.setList(response.body().data!!.latelyList)
                            nearShowListAdapter?.setList(response.body().data!!.hotList)
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
    private fun getListByType(catId:Int){
        var params = HttpParams()
        params.put("catId",catId)
        OkGo.post<BaseResponse<VideoListResponse>>(Constant.QUERY_VIDEO_LIST)
                .params(params)
                .execute(object : JsonCallBack<BaseResponse<VideoListResponse>>(){
                    override fun onSuccess(response: Response<BaseResponse<VideoListResponse>>?) {
                        if(response?.body()?.res == 0){
                            listByCatAdapter?.setList(response.body().data.list)
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