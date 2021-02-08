package com.yiheoline.qcloud.xiaozhibo.profile

import android.content.Intent
import android.graphics.Color
import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import com.bumptech.glide.Glide
import com.lzy.okgo.OkGo
import com.lzy.okgo.model.HttpParams
import com.lzy.okgo.model.Response
import com.tencent.qcloud.ugckit.UGCKitConstants
import com.yiheoline.qcloud.xiaozhibo.Constant
import com.yiheoline.qcloud.xiaozhibo.base.BaseActivity
import com.yiheoline.qcloud.xiaozhibo.bean.UserProfileBean
import com.yiheoline.qcloud.xiaozhibo.http.BaseResponse
import com.yiheoline.qcloud.xiaozhibo.http.JsonCallBack
import com.yiheoline.qcloud.xiaozhibo.http.response.ShortVideoListResponse
import com.yiheoline.qcloud.xiaozhibo.show.TCVodPlayerActivity
import com.yiheoline.qcloud.xiaozhibo.show.adapter.ShowListAdapter
import com.yiheonline.qcloud.xiaozhibo.R
import kotlinx.android.synthetic.main.activity_shower_info.*
import org.jetbrains.anko.sdk27.coroutines.onClick
import org.jetbrains.anko.textColor
import org.jetbrains.anko.toast
import java.io.Serializable

class ShowerInfoActivity : BaseActivity() {
    var userId = 0
    var pageNum = 1
    var adapter : ShowListAdapter? = null
    var type = 1
    var userProfileBean: UserProfileBean? = null

    override fun getLayoutId(): Int {
        return R.layout.activity_shower_info
    }

    override fun initView() {
        super.initView()
        backBtn.onClick { finish() }
        var manager = GridLayoutManager(this,2)
        recyclerView.layoutManager = manager
        adapter = ShowListAdapter(R.layout.show_list_item_layout, arrayListOf())
        var emptyView = layoutInflater.inflate(R.layout.order_empty_layout,null)
        adapter?.setEmptyView(emptyView)
        recyclerView.adapter = adapter
        adapter?.setOnItemClickListener { _, _, position ->
            //跳转到播放页面
            var item = adapter!!.data[position]
            val intent = Intent(this, TCVodPlayerActivity::class.java)
            intent.putExtra(UGCKitConstants.PLAY_URL, item.videoPath)
            intent.putExtra(UGCKitConstants.PUSHER_NAME, item.nickname)
            intent.putExtra(UGCKitConstants.PUSHER_AVATAR, item.avatar)
            intent.putExtra(UGCKitConstants.COVER_PIC, item.cover)
            intent.putExtra(UGCKitConstants.FILE_ID, item.shortVideoId)
            intent.putExtra(UGCKitConstants.TCLIVE_INFO_LIST, adapter!!.data as Serializable)
            intent.putExtra(UGCKitConstants.TIMESTAMP, item.createTime)
            intent.putExtra(UGCKitConstants.TCLIVE_INFO_POSITION, position)

            intent.putExtra("jumpType",1)

            startActivityForResult(intent, 2000)
        }
        adapter?.loadMoreModule?.isEnableLoadMore = true
        adapter?.loadMoreModule?.isAutoLoadMore = true
        adapter?.loadMoreModule?.isEnableLoadMoreIfNotFullPage = false
        adapter?.loadMoreModule?.setOnLoadMoreListener {
            pageNum++
            getVideoList()
        }
    }

    override fun getFitSystemWindows(): Boolean {
        return false
    }

    override fun initData() {
        super.initData()
        userId = intent.getIntExtra("userId",0)
        getCenterData()
        getVideoList()
    }

    private fun refreshUi(userProfileBean: UserProfileBean){
        this.userProfileBean = userProfileBean
        if(userProfileBean.isFollow == null){
            followBtn.setImageResource(R.mipmap.guanzhu2)
        }else{
            followBtn.setImageResource(R.mipmap.guanzhu1)
        }
        nameView.text = userProfileBean.nickname
        if(userProfileBean.gender == 0){
            sexImageView.setImageResource(R.mipmap.female)
        }else{
            sexImageView.setImageResource(R.mipmap.male)
        }
        followNumView.text = userProfileBean.followNum.toString()
        fansNumView.text = userProfileBean.fanNum.toString()
        likeNumView.text = userProfileBean.likes.toString()
        professionView.text = userProfileBean.profession
        descView.text = userProfileBean.introduction

        Glide.with(this).load(Constant.IMAGE_BASE+userProfileBean.cover).into(bgView)
    }

    override fun initListener() {
        super.initListener()
        singleShowContain.onClick {
            type = 1
            singleShowLine.visibility = View.VISIBLE
            singleShowText.textSize = 16f
            singleShowText.textColor = Color.parseColor("#000000")

            playLineView.visibility = View.GONE
            playTextView.textSize = 14f
            playTextView.textColor = Color.parseColor("#999999")
            pageNum = 1
            getVideoList()
        }
        playContain.onClick {
            type = 2
            playLineView.visibility = View.VISIBLE
            playTextView.textSize = 16f
            playTextView.textColor = Color.parseColor("#000000")

            singleShowLine.visibility = View.GONE
            singleShowText.textSize = 14f
            singleShowText.textColor = Color.parseColor("#999999")
            pageNum = 1
            getVideoList()
        }

        followBtn.onClick {
            if(userProfileBean?.isFollow == null){
                follow()
            }else{
                unFollow()
            }
        }
    }

    /**
     * 获取中心数据
     */
    private fun getCenterData(){
        var params = HttpParams()
        params.put("targetUserId",userId)
        OkGo.post<BaseResponse<UserProfileBean>>(Constant.USER_PROFILE)
                .params(params)
                .execute(object : JsonCallBack<BaseResponse<UserProfileBean>>(){
                    override fun onSuccess(response: Response<BaseResponse<UserProfileBean>>?) {
                        if(response?.body()?.res == 0){
                            refreshUi(response.body()?.data!!)
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
        params.put("targetUserId",userId)
        params.put("type",type)
        OkGo.post<BaseResponse<ShortVideoListResponse>>(Constant.QUERY_SHORT_BY_TYPE)
                .params(params)
                .execute(object : JsonCallBack<BaseResponse<ShortVideoListResponse>>(){

                    override fun onFinish() {
                        super.onFinish()
                        adapter?.loadMoreModule?.loadMoreComplete()
                    }

                    override fun onError(response: Response<BaseResponse<ShortVideoListResponse>>?) {
                        super.onError(response)
                        adapter?.loadMoreModule?.loadMoreFail()
                    }
                    override fun onSuccess(response: Response<BaseResponse<ShortVideoListResponse>>?) {
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
                            toast(response?.body()?.msg.toString())
                        }
                    }

                })
    }

    /**
     * 关注
     */
    private fun follow() {
        val httpParams = HttpParams()
        httpParams.put("targetUserId", userId)
        OkGo.post<BaseResponse<String>>(Constant.USER_FOLLOW)
                .params(httpParams)
                .execute(object : JsonCallBack<BaseResponse<String>>(){
                    override fun onSuccess(response: Response<BaseResponse<String>>?) {
                        if(response?.body()?.res == 0){
                            followBtn.setImageResource(R.mipmap.guanzhu1)
                            userProfileBean?.isFollow = "1"
                        }else{
                            toast(response?.body()?.msg.toString())
                        }
                    }

                })
    }
    /**
     * 取消关注
     */
    private fun unFollow(){
        val httpParams = HttpParams()
        httpParams.put("targetUserId", userId)
        OkGo.post<BaseResponse<String>>(Constant.CANCEL_FOLLOW)
                .params(httpParams)
                .execute(object : JsonCallBack<BaseResponse<String>>(){
                    override fun onSuccess(response: Response<BaseResponse<String>>?) {
                        if(response?.body()?.res == 0){
                            followBtn.setImageResource(R.mipmap.guanzhu2)
                            userProfileBean?.isFollow = null
                        }else{
                            toast(response?.body()?.msg.toString())
                        }
                    }

                })
    }
}