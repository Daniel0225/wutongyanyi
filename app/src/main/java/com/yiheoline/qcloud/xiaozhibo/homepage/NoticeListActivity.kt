package com.yiheoline.qcloud.xiaozhibo.homepage

import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.lzy.okgo.OkGo
import com.lzy.okgo.model.HttpParams
import com.lzy.okgo.model.Response
import com.yiheoline.qcloud.xiaozhibo.Constant
import com.yiheoline.qcloud.xiaozhibo.base.BaseActivity
import com.yiheoline.qcloud.xiaozhibo.homepage.adapter.SearchAdapter
import com.yiheoline.qcloud.xiaozhibo.http.BaseResponse
import com.yiheoline.qcloud.xiaozhibo.http.JsonCallBack
import com.yiheoline.qcloud.xiaozhibo.http.response.HomePageResponse
import com.yiheonline.qcloud.xiaozhibo.R
import kotlinx.android.synthetic.main.activity_notice_list.*
import kotlinx.android.synthetic.main.activity_notice_list.recyclerView
import kotlinx.android.synthetic.main.activity_search.*
import kotlinx.android.synthetic.main.toolbar_layout.*
import org.jetbrains.anko.sdk27.coroutines.onClick
import org.jetbrains.anko.startActivity
import org.jetbrains.anko.toast

class NoticeListActivity : BaseActivity() {
    var searchAdapter : SearchAdapter? = null
    var pageNum = 1
    override fun getLayoutId(): Int {
        return R.layout.activity_notice_list
    }

    override fun initView() {
        super.initView()
        backView.onClick { finish() }
        titleView.text = "预告列表"

        var manager = LinearLayoutManager(this)
        manager.orientation = LinearLayoutManager.VERTICAL
        recyclerView.layoutManager = manager
        searchAdapter = SearchAdapter(R.layout.search_item_layout, arrayListOf())
        recyclerView.adapter = searchAdapter
        searchAdapter?.setOnItemClickListener { _, _, position ->
            startActivity<NoticeDetailActivity>("noticeId" to searchAdapter!!.data[position].noticeId.toString())
        }
    }

    override fun initData() {
        super.initData()
        searchNotice()
    }

    /**
     * 获取预告列表
     */
    private fun searchNotice(){
        var params = HttpParams()
        params.put("pageNum",pageNum)
        params.put("pageSize", Constant.PAGE_SIZE)
        params.put("lately",1)
        OkGo.post<BaseResponse<HomePageResponse>>(Constant.HOME_PAGE_LIST)
                .params(params)
                .execute(object : JsonCallBack<BaseResponse<HomePageResponse>>(){
                    override fun onSuccess(response: Response<BaseResponse<HomePageResponse>>?) {
                        if(response?.body()?.res == 0){
                            searchAdapter?.setList(response.body().data.list)
                        }else{
                            toast(response?.body()?.msg.toString())
                        }
                    }

                })
    }
}