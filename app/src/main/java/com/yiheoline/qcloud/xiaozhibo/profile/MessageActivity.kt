package com.yiheoline.qcloud.xiaozhibo.profile

import android.graphics.Color
import android.view.View
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.lzy.okgo.OkGo
import com.lzy.okgo.model.HttpParams
import com.lzy.okgo.model.Response
import com.yiheoline.qcloud.xiaozhibo.Constant
import com.yiheoline.qcloud.xiaozhibo.base.BaseActivity
import com.yiheoline.qcloud.xiaozhibo.http.BaseResponse
import com.yiheoline.qcloud.xiaozhibo.http.JsonCallBack
import com.yiheoline.qcloud.xiaozhibo.http.response.LetterListResponse
import com.yiheoline.qcloud.xiaozhibo.profile.adapter.LetterListAdapter
import com.yiheonline.qcloud.xiaozhibo.R
import kotlinx.android.synthetic.main.activity_fans_list.playContain
import kotlinx.android.synthetic.main.activity_fans_list.playLineView
import kotlinx.android.synthetic.main.activity_fans_list.playTextView
import kotlinx.android.synthetic.main.activity_fans_list.singleShowContain
import kotlinx.android.synthetic.main.activity_fans_list.singleShowLine
import kotlinx.android.synthetic.main.activity_fans_list.singleShowText
import kotlinx.android.synthetic.main.activity_message.*
import kotlinx.android.synthetic.main.toolbar_layout.*
import org.jetbrains.anko.sdk27.coroutines.onClick
import org.jetbrains.anko.textColor
import org.jetbrains.anko.toast

class MessageActivity : BaseActivity() {
    private var pageNum = 1
    var letterListAdapter : LetterListAdapter? = null
    override fun getLayoutId(): Int {
        return R.layout.activity_message
    }

    override fun initView() {
        super.initView()
        backView.onClick { finish() }
        titleView.text = "消息"
        var layoutManager = LinearLayoutManager(this)
        recyclerView.layoutManager = layoutManager
        letterListAdapter = LetterListAdapter(R.layout.letter_item_layout, arrayListOf())
        recyclerView.adapter = letterListAdapter
        letterListAdapter?.loadMoreModule?.isEnableLoadMore = true
        letterListAdapter?.loadMoreModule?.isAutoLoadMore = true
        letterListAdapter?.loadMoreModule?.isEnableLoadMoreIfNotFullPage = false
    }

    override fun initData() {
        super.initData()
        relationLetter()
        getUserLetters()
    }

    override fun initListener() {
        super.initListener()
        singleShowContain.onClick {
//            type = 0
            singleShowLine.visibility = View.VISIBLE
            singleShowText.textSize = 16f
            singleShowText.textColor = Color.parseColor("#000000")

            playLineView.visibility = View.GONE
            playTextView.textSize = 14f
            playTextView.textColor = Color.parseColor("#999999")

            outLineView.visibility = View.GONE
            outTextView.textSize = 14f
            outTextView.textColor = Color.parseColor("#999999")
//            pageNum == 1
        }
        playContain.onClick {
//            type = 1
            playLineView.visibility = View.VISIBLE
            playTextView.textSize = 16f
            playTextView.textColor = Color.parseColor("#000000")

            singleShowLine.visibility = View.GONE
            singleShowText.textSize = 14f
            singleShowText.textColor = Color.parseColor("#999999")

            outLineView.visibility = View.GONE
            outTextView.textSize = 14f
            outTextView.textColor = Color.parseColor("#999999")

            pageNum = 1
        }
        outContain.onClick {
//            type = 2
            playLineView.visibility = View.GONE
            playTextView.textSize = 14f
            playTextView.textColor = Color.parseColor("#999999")

            singleShowLine.visibility = View.GONE
            singleShowText.textSize = 14f
            singleShowText.textColor = Color.parseColor("#999999")

            outLineView.visibility = View.VISIBLE
            outTextView.textSize = 16f
            outTextView.textColor = Color.parseColor("#000000")

            pageNum = 1
        }
    }
    /**
     * 获取站内信
     */
    private fun getUserLetters(){
        var params = HttpParams()
        params.put("pageNum",pageNum)
        params.put("pageSize", Constant.PAGE_SIZE)
        OkGo.post<BaseResponse<LetterListResponse>>(Constant.USER_LETTER_LIST)
                .params(params)
                .execute(object:JsonCallBack<BaseResponse<LetterListResponse>>(){
                    override fun onFinish() {
                        super.onFinish()
                        letterListAdapter?.loadMoreModule?.loadMoreComplete()
                    }

                    override fun onError(response: Response<BaseResponse<LetterListResponse>>?) {
                        super.onError(response)
                        letterListAdapter?.loadMoreModule?.loadMoreFail()
                    }
                    override fun onSuccess(response: Response<BaseResponse<LetterListResponse>>?) {
                        if(response?.body()?.res == 0){
                            if(pageNum == 1){
                                letterListAdapter?.setList(response.body()?.data?.list)
                            }else{
                                letterListAdapter?.addData(response.body().data.list)
                            }
                            if(response.body()?.data?.pages == pageNum){
                                letterListAdapter?.loadMoreModule?.loadMoreEnd(true)
                            }
                        }else{
                            toast(response?.body()?.msg.toString())
                        }
                    }

                })
    }
    /**
     * 关联站内信
     */
    private fun relationLetter(){
        OkGo.post<BaseResponse<String>>(Constant.RELATION_LETTER)
                .execute(object : JsonCallBack<BaseResponse<String>>(){
                    override fun onSuccess(response: Response<BaseResponse<String>>?) {

                    }

                })
    }
}