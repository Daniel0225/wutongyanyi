package com.yiheoline.qcloud.xiaozhibo.video

import androidx.recyclerview.widget.GridLayoutManager
import com.lzy.okgo.OkGo
import com.lzy.okgo.model.HttpParams
import com.lzy.okgo.model.Response
import com.yiheoline.qcloud.xiaozhibo.Constant
import com.yiheoline.qcloud.xiaozhibo.base.BaseActivity
import com.yiheoline.qcloud.xiaozhibo.http.BaseResponse
import com.yiheoline.qcloud.xiaozhibo.http.JsonCallBack
import com.yiheoline.qcloud.xiaozhibo.http.response.VideoListResponse
import com.yiheoline.qcloud.xiaozhibo.video.adapter.VideoListAdapter
import com.yiheonline.qcloud.xiaozhibo.R
import kotlinx.android.synthetic.main.activity_video_list.*
import kotlinx.android.synthetic.main.toolbar_layout.*
import org.jetbrains.anko.sdk27.coroutines.onClick
import org.jetbrains.anko.startActivity
import org.jetbrains.anko.support.v4.startActivity
import org.jetbrains.anko.support.v4.toast
import org.jetbrains.anko.toast

class VideoListActivity : BaseActivity() {
    var pageNum = 1
    var type = 0
    var titles = arrayListOf("最近现场","推荐现场","免费专区")
    private var listByCatAdapter : VideoListAdapter? = null
    override fun getLayoutId(): Int {
        return R.layout.activity_video_list
    }

    override fun initView() {
        super.initView()
        backView.onClick { finish() }

        titleView.text = titles[type]

        recyclerView.layoutManager = GridLayoutManager(this,2)
        listByCatAdapter = VideoListAdapter(R.layout.video_list_item, arrayListOf())
        recyclerView.adapter = listByCatAdapter
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
    }

    override fun initData() {
        super.initData()
        type = intent.getIntExtra("type",0)
        getListByType()
    }

    /**
     * 获取视频列表
     */
    private fun getListByType(){
        var params = HttpParams()
        params.put("pageNum",pageNum)
        params.put("pageSize", Constant.PAGE_SIZE)
        when (type) {
            0 -> {
                params.put("lately",1)
            }
            1 -> {
                params.put("isRecommend",1)
            }
            else -> {
                params.put("isFree",1)
            }
        }
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
}