package com.yiheoline.qcloud.xiaozhibo.profile

import android.graphics.Color
import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.lzy.okgo.OkGo
import com.lzy.okgo.model.HttpParams
import com.lzy.okgo.model.Response
import com.yiheoline.qcloud.xiaozhibo.Constant
import com.yiheoline.qcloud.xiaozhibo.base.BaseActivity
import com.yiheoline.qcloud.xiaozhibo.http.BaseResponse
import com.yiheoline.qcloud.xiaozhibo.http.JsonCallBack
import com.yiheoline.qcloud.xiaozhibo.http.response.FansListResponse
import com.yiheoline.qcloud.xiaozhibo.profile.adapter.FansListAdapter
import com.yiheonline.qcloud.xiaozhibo.R
import kotlinx.android.synthetic.main.activity_fans_list.*
import kotlinx.android.synthetic.main.toolbar_layout.*
import org.jetbrains.anko.sdk27.coroutines.onClick
import org.jetbrains.anko.textColor
import org.jetbrains.anko.toast

class FansListActivity : BaseActivity() {
    var pageNum = 1
    var type = 0//0: 关注 1 粉丝
    var fansListAdapter : FansListAdapter? = null

    override fun getLayoutId(): Int {
        return R.layout.activity_fans_list
    }

    override fun initView() {
        super.initView()
        backView.onClick { finish() }
        titleView.text = "关注"

        type = intent.getIntExtra("type",0)
        var layoutManager = LinearLayoutManager(this)
        layoutManager.orientation = LinearLayoutManager.VERTICAL
        recyclerView.layoutManager = layoutManager
        fansListAdapter = FansListAdapter(R.layout.fans_list_item_layout, arrayListOf())
        fansListAdapter?.loadMoreModule?.isEnableLoadMore = true
        fansListAdapter?.loadMoreModule?.isAutoLoadMore = true
        fansListAdapter?.loadMoreModule?.isEnableLoadMoreIfNotFullPage = false

        recyclerView.adapter = fansListAdapter

        fansListAdapter?.loadMoreModule?.setOnLoadMoreListener {
            pageNum++
            if(type == 0){
                getFollowList()
            }else{
                getFansList()
            }
        }

        recyclerView.postDelayed({
            if(type == 1){
                playContain.performClick()
            }else{
                singleShowContain.performClick()
            }
        },500)

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
            getFollowList()
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
            getFansList()
        }
    }

    /**
     * 获取关注列表
     */
    private fun getFollowList(){
        var params = HttpParams()
        params.put("pageNum",pageNum)
        params.put("pageSize", Constant.PAGE_SIZE)
        OkGo.post<BaseResponse<FansListResponse>>(Constant.QUERY_FOLLOW)
                .params(params)
                .execute(object:JsonCallBack<BaseResponse<FansListResponse>>(){
                    override fun onFinish() {
                        super.onFinish()
                        fansListAdapter?.loadMoreModule?.loadMoreComplete()
                    }

                    override fun onError(response: Response<BaseResponse<FansListResponse>>?) {
                        super.onError(response)
                        fansListAdapter?.loadMoreModule?.loadMoreFail()
                    }
                    override fun onSuccess(response: Response<BaseResponse<FansListResponse>>?) {
                        if(response?.body()?.res == 0){
                            if(pageNum == 1){
                                fansListAdapter?.setList(response.body()?.data?.list)
                            }else{
                                fansListAdapter?.addData(response.body().data.list)
                            }
                            if(response?.body()?.data?.pages == pageNum){
                                fansListAdapter?.loadMoreModule?.loadMoreEnd(true)
                            }
                        }else{
                            toast(response?.body()?.msg.toString())
                        }
                    }

                })
    }
    /**
     * 获取粉丝列表
     */
    private fun getFansList(){
        var params = HttpParams()
        params.put("pageNum",pageNum)
        params.put("pageSize", Constant.PAGE_SIZE)
        OkGo.post<BaseResponse<FansListResponse>>(Constant.QUERY_FANS)
                .params(params)
                .execute(object:JsonCallBack<BaseResponse<FansListResponse>>(){
                    override fun onFinish() {
                        super.onFinish()
                        fansListAdapter?.loadMoreModule?.loadMoreComplete()
                    }

                    override fun onError(response: Response<BaseResponse<FansListResponse>>?) {
                        super.onError(response)
                        fansListAdapter?.loadMoreModule?.loadMoreFail()
                    }
                    override fun onSuccess(response: Response<BaseResponse<FansListResponse>>?) {
                        if(response?.body()?.res == 0){
                            if(pageNum == 1){
                                fansListAdapter?.setList(response.body()?.data?.list)
                            }else{
                                fansListAdapter?.addData(response.body().data.list)
                            }
                            if(response?.body()?.data?.pages == pageNum){
                                fansListAdapter?.loadMoreModule?.loadMoreEnd(true)
                            }
                        }else{
                            toast(response?.body()?.msg.toString())
                        }
                    }

                })
    }
}