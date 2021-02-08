package com.yiheoline.qcloud.xiaozhibo.profile

import androidx.recyclerview.widget.LinearLayoutManager
import com.lzy.okgo.OkGo
import com.lzy.okgo.model.HttpParams
import com.lzy.okgo.model.Response
import com.yiheoline.qcloud.xiaozhibo.Constant
import com.yiheoline.qcloud.xiaozhibo.base.BaseActivity
import com.yiheoline.qcloud.xiaozhibo.bean.AnchorCenterBean
import com.yiheoline.qcloud.xiaozhibo.http.BaseResponse
import com.yiheoline.qcloud.xiaozhibo.http.JsonCallBack
import com.yiheoline.qcloud.xiaozhibo.http.response.LiveRecordResponse
import com.yiheoline.qcloud.xiaozhibo.profile.adapter.AnchorCenterItemAdapter
import com.yiheonline.qcloud.xiaozhibo.R
import kotlinx.android.synthetic.main.activity_anchor_center.*
import org.jetbrains.anko.sdk27.coroutines.onClick
import org.jetbrains.anko.toast

class AnchorCenterActivity : BaseActivity() {
    var pageNum = 1
    var adapter : AnchorCenterItemAdapter? = null
    override fun getLayoutId(): Int {
        return R.layout.activity_anchor_center
    }

    override fun getFitSystemWindows(): Boolean {
        return false
    }

    override fun initView() {
        super.initView()
        backView.onClick { finish() }
        var layoutManager = LinearLayoutManager(this)
        layoutManager.orientation = LinearLayoutManager.VERTICAL
        recyclerView.layoutManager = layoutManager
        adapter = AnchorCenterItemAdapter(R.layout.anchor_center_item_layout, arrayListOf())
        adapter?.loadMoreModule?.isEnableLoadMore = true
        adapter?.loadMoreModule?.isAutoLoadMore = true
        adapter?.loadMoreModule?.isEnableLoadMoreIfNotFullPage = false
        recyclerView.adapter = adapter

        adapter?.loadMoreModule?.setOnLoadMoreListener {
            pageNum++
            getLiveData()
        }
    }

    override fun initData() {
        super.initData()
        getCenterData()
        getLiveData()
    }

    private fun setUi(anchorCenterBean: AnchorCenterBean){
        getWtbNumView.text = anchorCenterBean.totalWutongbi
        timeLongView.text = anchorCenterBean.liveHours
    }

    /**
     * 获取主播直播数据
     */
    private fun getCenterData(){
        OkGo.post<BaseResponse<AnchorCenterBean>>(Constant.ANCHOR_CENTER)
                .execute(object : JsonCallBack<BaseResponse<AnchorCenterBean>>(){
                    override fun onSuccess(response: Response<BaseResponse<AnchorCenterBean>>?) {
                        if(response?.body()?.res == 0){
                            setUi(response?.body()?.data!!)
                        }else{
                            toast(response?.body()?.msg.toString())
                        }
                    }

                })
    }
    /**
     * 获取直播场次数据
     */
    private fun getLiveData(){
        var params = HttpParams()
        params.put("pageNum",pageNum)
        params.put("pageSize", Constant.PAGE_SIZE)
        OkGo.post<BaseResponse<LiveRecordResponse>>(Constant.LIVE_RECORD)
                .params(params)
                .execute(object:JsonCallBack<BaseResponse<LiveRecordResponse>>(){
                    override fun onFinish() {
                        super.onFinish()
                        adapter?.loadMoreModule?.loadMoreComplete()
                    }

                    override fun onError(response: Response<BaseResponse<LiveRecordResponse>>?) {
                        super.onError(response)
                        adapter?.loadMoreModule?.loadMoreFail()
                    }
                    override fun onSuccess(response: Response<BaseResponse<LiveRecordResponse>>?) {
                        if(response?.body()?.res == 0){
                            if(pageNum == 1){
                                adapter?.setList(response.body()?.data?.list)
                            }else{
                                adapter?.addData(response.body().data.list)
                            }
                            if(response?.body()?.data?.pages == pageNum){
                                adapter?.loadMoreModule?.loadMoreEnd(true)
                            }
                        }else{
                            toast(response?.body()?.msg.toString())
                        }
                    }

                })
    }
}