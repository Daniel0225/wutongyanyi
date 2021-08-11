package com.yiheoline.qcloud.xiaozhibo.profile

import android.graphics.Color
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.lzy.okgo.OkGo
import com.lzy.okgo.model.HttpParams
import com.lzy.okgo.model.Response
import com.yiheoline.qcloud.xiaozhibo.Constant
import com.yiheoline.qcloud.xiaozhibo.base.BaseActivity
import com.yiheoline.qcloud.xiaozhibo.homepage.NoticeDetailActivity
import com.yiheoline.qcloud.xiaozhibo.http.BaseResponse
import com.yiheoline.qcloud.xiaozhibo.http.JsonCallBack
import com.yiheoline.qcloud.xiaozhibo.http.response.NoticeOrderResponse
import com.yiheoline.qcloud.xiaozhibo.http.response.VideoOrderResponse
import com.yiheoline.qcloud.xiaozhibo.profile.adapter.OrderListAdapter
import com.yiheoline.qcloud.xiaozhibo.profile.adapter.VideoOrderListAdapter
import com.yiheoline.qcloud.xiaozhibo.video.VideoDetailActivity
import com.yiheonline.qcloud.xiaozhibo.R
import kotlinx.android.synthetic.main.activity_fans_list.playContain
import kotlinx.android.synthetic.main.activity_fans_list.playLineView
import kotlinx.android.synthetic.main.activity_fans_list.playTextView
import kotlinx.android.synthetic.main.activity_fans_list.recyclerView
import kotlinx.android.synthetic.main.activity_fans_list.singleShowContain
import kotlinx.android.synthetic.main.activity_fans_list.singleShowLine
import kotlinx.android.synthetic.main.activity_fans_list.singleShowText
import kotlinx.android.synthetic.main.toolbar_layout.*
import org.jetbrains.anko.sdk27.coroutines.onClick
import org.jetbrains.anko.startActivity
import org.jetbrains.anko.textColor

class OrderListActivity : BaseActivity() {
    var pageNum = 1
    var type = 0
    var adapter : OrderListAdapter? = null
    var videoAdapter : VideoOrderListAdapter? = null

    override fun getLayoutId(): Int {
        return R.layout.activity_order_list
    }

    override fun initView() {
        super.initView()
        backView.onClick { finish() }
        titleView.text = "我的订单"
        var layoutManager = LinearLayoutManager(this)
        layoutManager.orientation = LinearLayoutManager.VERTICAL
        recyclerView.layoutManager = layoutManager

        adapter = OrderListAdapter(R.layout.order_item_layout, arrayListOf())
        videoAdapter = VideoOrderListAdapter(R.layout.video_order_item_layout, arrayListOf())

        adapter?.loadMoreModule?.isEnableLoadMore = true
        adapter?.loadMoreModule?.isAutoLoadMore = true
        adapter?.loadMoreModule?.isEnableLoadMoreIfNotFullPage = false
        recyclerView.adapter = adapter
        adapter?.loadMoreModule?.setOnLoadMoreListener {
            pageNum++
            getNoticeOrderList()
        }
        adapter?.setOnItemClickListener { _, _, position ->
            startActivity<NoticeDetailActivity>("noticeId" to adapter!!.data[position].noticeId.toString())
        }

        videoAdapter?.loadMoreModule?.isEnableLoadMore = true
        videoAdapter?.loadMoreModule?.isAutoLoadMore = true
        videoAdapter?.loadMoreModule?.isEnableLoadMoreIfNotFullPage = false
        videoAdapter?.loadMoreModule?.setOnLoadMoreListener {
            pageNum++
            getVideoOrderList()
        }

        videoAdapter?.setOnItemClickListener { _, _, position ->
            startActivity<VideoDetailActivity>("videoId" to videoAdapter!!.data[position].videoId)
        }

        getNoticeOrderList()
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
            pageNum = 1

            recyclerView.adapter = adapter
            getNoticeOrderList()
        }
        playContain.onClick {
            type = 1
            playLineView.visibility = View.VISIBLE
            playTextView.textSize = 16f
            playTextView.textColor = Color.parseColor("#000000")

            singleShowLine.visibility = View.GONE
            singleShowText.textSize = 14f
            singleShowText.textColor = Color.parseColor("#999999")

            pageNum = 1
            recyclerView.adapter = videoAdapter
            getVideoOrderList()
        }
    }
    /**
     * 获取直播预告订单列表
     */
    private fun getNoticeOrderList(){
        var params = HttpParams()
        params.put("pageNum",pageNum)
        params.put("pageSize", Constant.PAGE_SIZE)
        OkGo.post<BaseResponse<NoticeOrderResponse>>(Constant.NOTICE_ORDER_LIST)
                .params(params)
                .execute(object:JsonCallBack<BaseResponse<NoticeOrderResponse>>(){
                    override fun onFinish() {
                        super.onFinish()
                        adapter?.loadMoreModule?.loadMoreComplete()
                    }

                    override fun onError(response: Response<BaseResponse<NoticeOrderResponse>>?) {
                        super.onError(response)
                        adapter?.loadMoreModule?.loadMoreFail()
                    }
                    override fun onSuccess(response: Response<BaseResponse<NoticeOrderResponse>>?) {
                        if(response?.body()?.res == 0){
                            if(pageNum == 1){
                                adapter?.setList(response.body()?.data?.list)
                            }else{
                                adapter?.addData(response.body().data.list)
                            }
                            if(response.body()?.data?.pages == pageNum){
                                adapter?.loadMoreModule?.loadMoreEnd(true)
                            }
                        }else{

                        }
                    }

                })
    }
    /**
     * 获取视频订单列表
     */
    private fun getVideoOrderList(){
        var params = HttpParams()
        params.put("pageNum",pageNum)
        params.put("pageSize", Constant.PAGE_SIZE)
        OkGo.post<BaseResponse<VideoOrderResponse>>(Constant.VIDEO_ORDER_LIST)
                .params(params)
                .execute(object:JsonCallBack<BaseResponse<VideoOrderResponse>>(){
                    override fun onFinish() {
                        super.onFinish()
                        videoAdapter?.loadMoreModule?.loadMoreComplete()
                    }

                    override fun onError(response: Response<BaseResponse<VideoOrderResponse>>?) {
                        super.onError(response)
                        videoAdapter?.loadMoreModule?.loadMoreFail()
                    }
                    override fun onSuccess(response: Response<BaseResponse<VideoOrderResponse>>?) {
                        if(response?.body()?.res == 0){
                            if(pageNum == 1){
                                videoAdapter?.setList(response.body()?.data?.list)
                            }else{
                                videoAdapter?.addData(response.body().data.list)
                            }
                            if(response.body()?.data?.pages == pageNum){
                                videoAdapter?.loadMoreModule?.loadMoreEnd(true)
                            }
                        }else{

                        }
                    }

                })
    }
}