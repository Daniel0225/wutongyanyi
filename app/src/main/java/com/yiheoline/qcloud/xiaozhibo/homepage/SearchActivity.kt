package com.yiheoline.qcloud.xiaozhibo.homepage

import android.annotation.SuppressLint
import android.os.Handler
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.core.widget.addTextChangedListener
import androidx.core.widget.doOnTextChanged
import androidx.recyclerview.widget.LinearLayoutManager
import com.chad.library.adapter.base.BaseQuickAdapter
import com.lzy.okgo.OkGo
import com.lzy.okgo.model.HttpParams
import com.lzy.okgo.model.Response
import com.tencent.mmkv.MMKV
import com.yiheoline.qcloud.xiaozhibo.Constant
import com.yiheoline.qcloud.xiaozhibo.base.BaseActivity
import com.yiheoline.qcloud.xiaozhibo.homepage.adapter.HistorySearchAdapter
import com.yiheoline.qcloud.xiaozhibo.homepage.adapter.SearchAdapter
import com.yiheoline.qcloud.xiaozhibo.http.BaseResponse
import com.yiheoline.qcloud.xiaozhibo.http.JsonCallBack
import com.yiheoline.qcloud.xiaozhibo.http.response.HomePageResponse
import com.yiheonline.qcloud.xiaozhibo.R
import kotlinx.android.synthetic.main.activity_search.*
import org.jetbrains.anko.sdk27.coroutines.onClick
import org.jetbrains.anko.startActivity
import org.jetbrains.anko.toast

class SearchActivity : BaseActivity() {
    var searchAdapter : SearchAdapter? = null
    var handler : Handler = @SuppressLint("HandlerLeak")
    object : Handler(){}
    var mSearchTask = SearchTask()
    override fun getLayoutId(): Int {
        return R.layout.activity_search
    }

    override fun onResume() {
        super.onResume()
        getSearchHistory()
    }

    override fun initView() {
        super.initView()
        cancelBtn.onClick { finish() }
        var manager = LinearLayoutManager(this)
        manager.orientation = LinearLayoutManager.VERTICAL
        recyclerView.layoutManager = manager
        searchAdapter = SearchAdapter(R.layout.search_item_layout, arrayListOf())
        recyclerView.adapter = searchAdapter
        searchAdapter?.setOnItemClickListener { _, _, position ->
            startActivity<NoticeDetailActivity>("noticeId" to searchAdapter!!.data[position].noticeId.toString())
        }

        keyWordsInputView.addTextChangedListener(object : TextWatcher{
            override fun afterTextChanged(p0: Editable?) {
                handler.removeCallbacks(mSearchTask)
                handler.postDelayed(mSearchTask,500)
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

        })
    }

    inner class SearchTask : Runnable{
        override fun run() {
            searchNotice(keyWordsInputView.text.toString())
        }

    }

    /**
     * 搜索
     */
     fun searchNotice(keyWords:String){
        if(keyWords.isEmpty()){
            historyView.visibility = View.VISIBLE
            recyclerView.visibility = View.GONE
            return
        }else{
            historyView.visibility = View.GONE
            recyclerView.visibility = View.VISIBLE
            addSearchHistory(keyWords)
        }
        var params = HttpParams()
        params.put("search",keyWords)
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

    /**
     * 获取搜索历史记录
     */
    private fun getSearchHistory(){
        var historyString = MMKV.defaultMMKV().getString("searchHistory","")
        var historyList : MutableList<String>? = null
        historyList = if(historyString!!.isNotEmpty()){
            historyString?.split(",") as MutableList<String>
        }else{
            arrayListOf()
        }
        var historyAdapter = HistorySearchAdapter(R.layout.search_history_item_layout, historyList)
        var manager = LinearLayoutManager(this)
        searchHistoryRecycler.layoutManager = manager
        searchHistoryRecycler.adapter = historyAdapter
        historyAdapter.setOnItemChildClickListener { _, _, position ->
            historyAdapter?.data!!.removeAt(position)
            historyAdapter.notifyDataSetChanged()
            refreshHistory(historyAdapter?.data!!)
        }
        historyAdapter.setOnItemClickListener { _, _, position ->
            searchNotice(historyAdapter.data[position])
        }
    }

    /**
     * 添加历史搜索词
     */
    private fun addSearchHistory(searchWords : String){
        var historyString = MMKV.defaultMMKV().getString("searchHistory","")
        if(!historyString!!.contains(searchWords)){
            var stringBuffer = StringBuffer()
            stringBuffer.insert(0,historyString)
            if(historyString.isNotEmpty()){ stringBuffer.insert(0,",")}
            stringBuffer.insert(0,searchWords)
            MMKV.defaultMMKV().encode("searchHistory",stringBuffer.toString())
        }
    }

    /**
     * 更新历史搜索数据
     */
    private fun refreshHistory(list: List<String>){
        var stringBuffer = StringBuffer()
        for (item in list){
            stringBuffer.append(item)
            if(list.indexOf(item) != list.size -1){
                stringBuffer.append(",")
            }
        }
        MMKV.defaultMMKV().encode("searchHistory",stringBuffer.toString())
    }
}