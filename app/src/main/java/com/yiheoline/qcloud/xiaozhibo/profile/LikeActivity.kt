package com.yiheoline.qcloud.xiaozhibo.profile

import android.content.Intent
import android.graphics.Color
import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.lzy.okgo.OkGo
import com.lzy.okgo.model.HttpParams
import com.lzy.okgo.model.Response
import com.tencent.qcloud.ugckit.UGCKitConstants
import com.yiheoline.qcloud.xiaozhibo.Constant
import com.yiheoline.qcloud.xiaozhibo.base.BaseActivity
import com.yiheoline.qcloud.xiaozhibo.http.BaseResponse
import com.yiheoline.qcloud.xiaozhibo.http.JsonCallBack
import com.yiheoline.qcloud.xiaozhibo.http.response.LikeShowResponse
import com.yiheoline.qcloud.xiaozhibo.http.response.LikeVideoResponse
import com.yiheoline.qcloud.xiaozhibo.http.response.ShortVideoListResponse
import com.yiheoline.qcloud.xiaozhibo.profile.adapter.LikeShowAdapter
import com.yiheoline.qcloud.xiaozhibo.profile.adapter.LikeVideoAdapter
import com.yiheoline.qcloud.xiaozhibo.show.TCVodPlayerActivity
import com.yiheoline.qcloud.xiaozhibo.show.adapter.ShowListAdapter
import com.yiheoline.qcloud.xiaozhibo.video.VideoDetailActivity
import com.yiheonline.qcloud.xiaozhibo.R
import kotlinx.android.synthetic.main.activity_fans_list.playContain
import kotlinx.android.synthetic.main.activity_fans_list.playLineView
import kotlinx.android.synthetic.main.activity_fans_list.playTextView
import kotlinx.android.synthetic.main.activity_fans_list.recyclerView
import kotlinx.android.synthetic.main.activity_fans_list.singleShowContain
import kotlinx.android.synthetic.main.activity_fans_list.singleShowLine
import kotlinx.android.synthetic.main.activity_fans_list.singleShowText
import kotlinx.android.synthetic.main.activity_like.*
import kotlinx.android.synthetic.main.toolbar_layout.*
import org.jetbrains.anko.sdk27.coroutines.onClick
import org.jetbrains.anko.startActivity
import org.jetbrains.anko.textColor
import org.jetbrains.anko.toast
import java.io.Serializable

class LikeActivity : BaseActivity() {
    var pageNum = 1
    var likeShowAdapter:LikeShowAdapter?=null
    var likeVideoAdapter : LikeVideoAdapter? = null
    var likeShortAdapter : ShowListAdapter? = null

    override fun getLayoutId(): Int {
        return R.layout.activity_like
    }

    override fun initView() {
        super.initView()
        backView.onClick { finish() }
        titleView.text = "我喜欢的"
        var layoutManager = LinearLayoutManager(this)
        layoutManager.orientation = LinearLayoutManager.VERTICAL
        recyclerView.layoutManager = layoutManager
        likeShowAdapter = LikeShowAdapter(R.layout.like_show_item_layout, arrayListOf())
        var emptyView = layoutInflater.inflate(R.layout.like_list_empty_layout,null)
        likeShowAdapter?.setEmptyView(emptyView)
        recyclerView.adapter = likeShowAdapter
        likeShowAdapter?.loadMoreModule?.isEnableLoadMore = true
        likeShowAdapter?.loadMoreModule?.isAutoLoadMore = true
        likeShowAdapter?.loadMoreModule?.isEnableLoadMoreIfNotFullPage = false
        likeShowAdapter?.loadMoreModule?.setOnLoadMoreListener {
            pageNum++
            getLikeShowList()
        }

        likeVideoAdapter = LikeVideoAdapter(R.layout.up_record_item_layout, arrayListOf())
        var emptyView2 = layoutInflater.inflate(R.layout.like_list_empty_layout,null)
        likeVideoAdapter?.setEmptyView(emptyView2)
        likeVideoAdapter?.loadMoreModule?.isEnableLoadMore = true
        likeVideoAdapter?.loadMoreModule?.isAutoLoadMore = true
        likeVideoAdapter?.loadMoreModule?.isEnableLoadMoreIfNotFullPage = false
        likeVideoAdapter?.loadMoreModule?.setOnLoadMoreListener {
            pageNum++
            getLikeVideoList()
        }
        likeVideoAdapter?.setOnItemClickListener { _, _, position ->
            startActivity<VideoDetailActivity>("videoId" to likeVideoAdapter!!.data[position].videoId)
        }

        likeShortAdapter = ShowListAdapter(R.layout.show_list_item_layout, arrayListOf())
        var emptyView3 = layoutInflater.inflate(R.layout.like_list_empty_layout,null)
        likeShortAdapter?.setEmptyView(emptyView3)
        likeShortAdapter?.loadMoreModule?.isEnableLoadMore = true
        likeShortAdapter?.loadMoreModule?.isAutoLoadMore = true
        likeShortAdapter?.loadMoreModule?.isEnableLoadMoreIfNotFullPage = false
        likeShortAdapter?.setOnItemClickListener { _, _, position ->
            //跳转到播放页面
            var item = likeShortAdapter!!.data[position]
            val intent = Intent(this@LikeActivity, TCVodPlayerActivity::class.java)
            intent.putExtra(UGCKitConstants.PLAY_URL, item.videoPath)
            intent.putExtra(UGCKitConstants.PUSHER_NAME, item.nickname)
            intent.putExtra(UGCKitConstants.PUSHER_AVATAR, item.avatar)
            intent.putExtra(UGCKitConstants.COVER_PIC, item.cover)
            intent.putExtra(UGCKitConstants.FILE_ID, item.shortVideoId)
            intent.putExtra(UGCKitConstants.TCLIVE_INFO_LIST, likeShortAdapter!!.data as Serializable)
            intent.putExtra(UGCKitConstants.TIMESTAMP, item.createTime)
            intent.putExtra(UGCKitConstants.TCLIVE_INFO_POSITION, position)
            startActivityForResult(intent, 2000)
        }
        likeShortAdapter?.loadMoreModule?.setOnLoadMoreListener {
            pageNum++
            getLikeShortVideoList()
        }
    }

    override fun initData() {
        super.initData()
        getLikeShowList()
    }

    override fun initListener() {
        super.initListener()
        singleShowContain.onClick {
            singleShowLine.visibility = View.VISIBLE
            singleShowText.textSize = 16f
            singleShowText.textColor = Color.parseColor("#000000")

            playLineView.visibility = View.GONE
            playTextView.textSize = 14f
            playTextView.textColor = Color.parseColor("#999999")

            outLineView.visibility = View.GONE
            outTextView.textSize = 14f
            outTextView.textColor = Color.parseColor("#999999")

            recyclerView.layoutManager = LinearLayoutManager(this@LikeActivity,LinearLayoutManager.VERTICAL,false)
            recyclerView.adapter = likeShowAdapter
            pageNum = 1
            getLikeShowList()
        }
        playContain.onClick {
            playLineView.visibility = View.VISIBLE
            playTextView.textSize = 16f
            playTextView.textColor = Color.parseColor("#000000")

            singleShowLine.visibility = View.GONE
            singleShowText.textSize = 14f
            singleShowText.textColor = Color.parseColor("#999999")

            outLineView.visibility = View.GONE
            outTextView.textSize = 14f
            outTextView.textColor = Color.parseColor("#999999")

            recyclerView.layoutManager = LinearLayoutManager(this@LikeActivity,LinearLayoutManager.VERTICAL,false)
            recyclerView.adapter = likeVideoAdapter
            pageNum = 1
            getLikeVideoList()
        }
        outContain.onClick {
            playLineView.visibility = View.GONE
            playTextView.textSize = 14f
            playTextView.textColor = Color.parseColor("#999999")

            singleShowLine.visibility = View.GONE
            singleShowText.textSize = 14f
            singleShowText.textColor = Color.parseColor("#999999")

            outLineView.visibility = View.VISIBLE
            outTextView.textSize = 16f
            outTextView.textColor = Color.parseColor("#000000")

            recyclerView.layoutManager = GridLayoutManager(this@LikeActivity,2)
            recyclerView.adapter = likeShortAdapter
            pageNum = 1
            getLikeShortVideoList()
        }
    }

    /**
     * 获取喜欢的直播列表
     */
    private fun getLikeShowList(){
        var params = HttpParams()
        params.put("pageNum",pageNum)
        params.put("pageSize", Constant.PAGE_SIZE)
        OkGo.post<BaseResponse<LikeShowResponse>>(Constant.QUERY_LIKE_SHOW)
                .params(params)
                .execute(object : JsonCallBack<BaseResponse<LikeShowResponse>>(){
                    override fun onFinish() {
                        super.onFinish()
                        likeShowAdapter?.loadMoreModule?.loadMoreComplete()
                    }

                    override fun onError(response: Response<BaseResponse<LikeShowResponse>>?) {
                        super.onError(response)
                        likeShowAdapter?.loadMoreModule?.loadMoreFail()
                    }
                    override fun onSuccess(response: Response<BaseResponse<LikeShowResponse>>?) {
                        if(response?.body()?.res == 0){
                            if(pageNum == 1){
                                likeShowAdapter?.setList(response.body()?.data?.list)
                            }else{
                                likeShowAdapter?.addData(response.body().data.list)
                            }
                            if(response?.body()?.data?.pages == pageNum){
                                likeShowAdapter?.loadMoreModule?.loadMoreEnd(true)
                            }
                        }else{
                            toast(response?.body()?.msg.toString())
                        }
                    }

                })
    }

    /**
     * 获取喜欢的视频列表
     */
    private fun getLikeVideoList(){
        var params = HttpParams()
        params.put("pageNum",pageNum)
        params.put("pageSize", Constant.PAGE_SIZE)
        OkGo.post<BaseResponse<LikeVideoResponse>>(Constant.QUERY_LIKE_VIDEO)
                .params(params)
                .execute(object : JsonCallBack<BaseResponse<LikeVideoResponse>>(){
                    override fun onFinish() {
                        super.onFinish()
                        likeVideoAdapter?.loadMoreModule?.loadMoreComplete()
                    }

                    override fun onError(response: Response<BaseResponse<LikeVideoResponse>>?) {
                        super.onError(response)
                        likeVideoAdapter?.loadMoreModule?.loadMoreFail()
                    }
                    override fun onSuccess(response: Response<BaseResponse<LikeVideoResponse>>?) {
                        if(response?.body()?.res == 0){
                            if(pageNum == 1){
                                likeVideoAdapter?.setList(response.body()?.data?.list)
                            }else{
                                likeVideoAdapter?.addData(response.body().data.list)
                            }
                            if(response?.body()?.data?.pages == pageNum){
                                likeVideoAdapter?.loadMoreModule?.loadMoreEnd(true)
                            }
                        }else{
                            toast(response?.body()?.msg.toString())
                        }
                    }

                })
    }
    /**
     * 获取喜欢的短视频列表
     */
    private fun getLikeShortVideoList(){
        var params = HttpParams()
        params.put("pageNum",pageNum)
        params.put("pageSize", Constant.PAGE_SIZE)
        OkGo.post<BaseResponse<ShortVideoListResponse>>(Constant.QUERY_LIKE_SHORT)
                .params(params)
                .execute(object : JsonCallBack<BaseResponse<ShortVideoListResponse>>(){

                    override fun onFinish() {
                        super.onFinish()
                        likeShortAdapter?.loadMoreModule?.loadMoreComplete()
                    }

                    override fun onError(response: Response<BaseResponse<ShortVideoListResponse>>?) {
                        super.onError(response)
                        likeShortAdapter?.loadMoreModule?.loadMoreFail()
                    }
                    override fun onSuccess(response: Response<BaseResponse<ShortVideoListResponse>>?) {
                        if(response?.body()?.res == 0){
                            if(pageNum == 1){
                                likeShortAdapter?.setList(response.body()?.data?.list)
                            }else{
                                likeShortAdapter?.addData(response.body().data.list)
                            }
                            if(response?.body()?.data?.pages == pageNum){
                                likeShortAdapter?.loadMoreModule?.loadMoreEnd(true)
                            }
                        }else{
                            toast(response?.body()?.msg.toString())
                        }
                    }

                })
    }
}