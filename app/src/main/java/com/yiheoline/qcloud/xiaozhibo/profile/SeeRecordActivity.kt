package com.yiheoline.qcloud.xiaozhibo.profile

import androidx.recyclerview.widget.LinearLayoutManager
import com.lzy.okgo.OkGo
import com.lzy.okgo.model.HttpParams
import com.lzy.okgo.model.Response
import com.yiheoline.qcloud.xiaozhibo.Constant
import com.yiheoline.qcloud.xiaozhibo.base.BaseActivity
import com.yiheoline.qcloud.xiaozhibo.http.BaseResponse
import com.yiheoline.qcloud.xiaozhibo.http.JsonCallBack
import com.yiheoline.qcloud.xiaozhibo.http.response.SeeRecordResponse
import com.yiheoline.qcloud.xiaozhibo.profile.adapter.SeeRecordAdapter
import com.yiheoline.qcloud.xiaozhibo.video.VideoDetailActivity
import com.yiheonline.qcloud.xiaozhibo.R
import kotlinx.android.synthetic.main.activity_see_record.*
import kotlinx.android.synthetic.main.toolbar_layout.*
import org.jetbrains.anko.sdk27.coroutines.onClick
import org.jetbrains.anko.startActivity
import org.jetbrains.anko.toast

class SeeRecordActivity : BaseActivity() {
    var pageNum = 1
    var seeRecordAdapter : SeeRecordAdapter? = null
    override fun getLayoutId(): Int {
        return R.layout.activity_see_record
    }

    override fun initView() {
        super.initView()
        backView.onClick { finish() }
        titleView.text = "现场"

        var layoutManager = LinearLayoutManager(this)
        layoutManager.orientation = LinearLayoutManager.VERTICAL
        recyclerView.layoutManager = layoutManager
        seeRecordAdapter = SeeRecordAdapter(R.layout.see_record_item_layout, arrayListOf())
        recyclerView.adapter = seeRecordAdapter
        seeRecordAdapter?.loadMoreModule?.isEnableLoadMore = true
        seeRecordAdapter?.loadMoreModule?.isAutoLoadMore = true
        seeRecordAdapter?.loadMoreModule?.isEnableLoadMoreIfNotFullPage = false
        seeRecordAdapter?.setOnItemClickListener { _, _, position ->
            startActivity<VideoDetailActivity>("videoId" to seeRecordAdapter!!.data[position].videoId)
        }
    }

    override fun initData() {
        super.initData()
        getList()
    }

    /**
     * 获取观看记录
     */
    private fun getList(){
        var params = HttpParams()
        params.put("type",1)
        params.put("pageNum",pageNum)
        params.put("pageSize", Constant.PAGE_SIZE)
        OkGo.post<BaseResponse<SeeRecordResponse>>(Constant.VIEW_LIST)
                .params(params)
                .execute(object : JsonCallBack<BaseResponse<SeeRecordResponse>>(){
                    override fun onFinish() {
                        super.onFinish()
                        seeRecordAdapter?.loadMoreModule?.loadMoreComplete()
                    }

                    override fun onError(response: Response<BaseResponse<SeeRecordResponse>>?) {
                        super.onError(response)
                        seeRecordAdapter?.loadMoreModule?.loadMoreFail()
                    }
                    override fun onSuccess(response: Response<BaseResponse<SeeRecordResponse>>?) {
                        if(response?.body()?.res == 0){
                            if(pageNum == 1){
                                seeRecordAdapter?.setList(response.body()?.data?.list)
                            }else{
                                seeRecordAdapter?.addData(response.body().data.list)
                            }
                            if(response?.body()?.data?.pages == pageNum){
                                seeRecordAdapter?.loadMoreModule?.loadMoreEnd(true)
                            }
                        }else{
                            toast(response?.body()?.msg.toString())
                        }
                    }

                })
    }
}