package com.yiheoline.qcloud.xiaozhibo.homepage

import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import com.github.sundeepk.compactcalendarview.CompactCalendarView
import com.lzy.okgo.OkGo
import com.lzy.okgo.model.HttpParams
import com.lzy.okgo.model.Response
import com.yiheoline.qcloud.xiaozhibo.Constant
import com.yiheoline.qcloud.xiaozhibo.base.BaseActivity
import com.yiheoline.qcloud.xiaozhibo.homepage.adapter.SearchAdapter
import com.yiheoline.qcloud.xiaozhibo.http.BaseResponse
import com.yiheoline.qcloud.xiaozhibo.http.JsonCallBack
import com.yiheoline.qcloud.xiaozhibo.http.response.HomePageResponse
import com.yiheoline.qcloud.xiaozhibo.utils.TimeUtil
import com.yiheonline.qcloud.xiaozhibo.R
import kotlinx.android.synthetic.main.activity_notice_list.*
import kotlinx.android.synthetic.main.activity_notice_select.*
import kotlinx.android.synthetic.main.activity_notice_select.recyclerView
import kotlinx.android.synthetic.main.toolbar_layout.*
import org.jetbrains.anko.sdk27.coroutines.onClick
import org.jetbrains.anko.startActivity
import org.jetbrains.anko.toast
import java.util.*

class NoticeSelectActivity : BaseActivity() {
    var searchAdapter : SearchAdapter? = null
    override fun getLayoutId(): Int {
        return R.layout.activity_notice_select
    }

    override fun initView() {
        super.initView()
        backView.onClick { finish() }
        titleView.text = "筛选"
        var layoutManager = LinearLayoutManager(this)
        layoutManager.orientation = LinearLayoutManager.VERTICAL
        recyclerView.layoutManager = layoutManager
        searchAdapter = SearchAdapter(R.layout.search_item_layout, arrayListOf())
        recyclerView.adapter = searchAdapter
        searchAdapter?.setOnItemClickListener { _, _, position ->
            startActivity<NoticeDetailActivity>("noticeId" to searchAdapter!!.data[position].noticeId.toString())
        }
        compactCalendar.setFirstDayOfWeek(Calendar.MONDAY)
        compactCalendar.setLocale(TimeZone.getDefault(),Locale.CHINESE)
        compactCalendar.setUseThreeLetterAbbreviation(true)
        compactCalendar.setListener(object : CompactCalendarView.CompactCalendarViewListener{
            override fun onDayClick(dateClicked: Date?) {
                searchNotice(TimeUtil.getYearMonthAndDay(dateClicked!!.time))
                dateView.text = TimeUtil.getYearMonthAndDay(dateClicked.time)
            }

            override fun onMonthScroll(firstDayOfNewMonth: Date?) {

            }

        })


    }

    override fun initData() {
        super.initData()
        searchNotice("")
    }
    var pageNum = 1
    /**
     * 获取预告列表
     */
    fun searchNotice(date:String){
        var params = HttpParams()
        params.put("pageNum",pageNum)
        params.put("pageSize", Constant.PAGE_SIZE)
        if(date.isNotEmpty()){
            params.put("date",date)
        }
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