package com.yiheoline.qcloud.xiaozhibo.profile

import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.chad.library.adapter.base.listener.OnItemSwipeListener
import com.lzy.okgo.OkGo
import com.lzy.okgo.model.HttpParams
import com.lzy.okgo.model.Response
import com.tencent.qcloud.ugckit.UGCKitConstants
import com.yiheoline.qcloud.xiaozhibo.Constant
import com.yiheoline.qcloud.xiaozhibo.base.BaseActivity
import com.yiheoline.qcloud.xiaozhibo.http.BaseResponse
import com.yiheoline.qcloud.xiaozhibo.http.JsonCallBack
import com.yiheoline.qcloud.xiaozhibo.http.response.ShortVideoListResponse
import com.yiheoline.qcloud.xiaozhibo.http.response.UpRecordResponse
import com.yiheoline.qcloud.xiaozhibo.profile.adapter.UpRecordAdapter
import com.yiheoline.qcloud.xiaozhibo.profile.adapter.UpShowRecordAdapter
import com.yiheoline.qcloud.xiaozhibo.show.TCVodPlayerActivity
import com.yiheonline.qcloud.xiaozhibo.R
import kotlinx.android.synthetic.main.activity_up_load_record.playContain
import kotlinx.android.synthetic.main.activity_up_load_record.playLineView
import kotlinx.android.synthetic.main.activity_up_load_record.playTextView
import kotlinx.android.synthetic.main.activity_up_load_record.recyclerView
import kotlinx.android.synthetic.main.activity_up_load_record.singleShowContain
import kotlinx.android.synthetic.main.activity_up_load_record.singleShowLine
import kotlinx.android.synthetic.main.activity_up_load_record.singleShowText
import kotlinx.android.synthetic.main.toolbar_layout.*
import org.jetbrains.anko.sdk27.coroutines.onClick
import org.jetbrains.anko.textColor
import org.jetbrains.anko.toast
import java.io.Serializable

class UpLoadRecordActivity : BaseActivity() {
    var pageNum = 1
    var upLoadAdapter : UpRecordAdapter? = null
    var upShowLoadAdapter : UpShowRecordAdapter? = null
    override fun getLayoutId(): Int {
        return R.layout.activity_up_load_record
    }

    override fun initView() {
        super.initView()
        backView.onClick { finish() }
        titleView.text = "上传记录"
        var layoutManager = LinearLayoutManager(this)
        layoutManager.orientation = LinearLayoutManager.VERTICAL
        recyclerView.layoutManager = layoutManager
        upLoadAdapter = UpRecordAdapter(R.layout.up_record_item_layout, arrayListOf())


        var onItemSwipeListener = object : OnItemSwipeListener{
            override fun clearView(viewHolder: RecyclerView.ViewHolder?, pos: Int) {

            }

            override fun onItemSwiped(viewHolder: RecyclerView.ViewHolder?, pos: Int) {
            }

            override fun onItemSwipeStart(viewHolder: RecyclerView.ViewHolder?, pos: Int) {
            }

            override fun onItemSwipeMoving(canvas: Canvas?, viewHolder: RecyclerView.ViewHolder?, dX: Float, dY: Float, isCurrentlyActive: Boolean) {
            }

        }
        upLoadAdapter?.loadMoreModule?.isEnableLoadMore = true
        upLoadAdapter?.loadMoreModule?.isAutoLoadMore = true
        upLoadAdapter?.loadMoreModule?.isEnableLoadMoreIfNotFullPage = false
        upShowLoadAdapter?.loadMoreModule?.setOnLoadMoreListener {
            pageNum++
            getUpRecord()
        }

        upLoadAdapter?.draggableModule?.isSwipeEnabled = true
        upLoadAdapter?.draggableModule?.setOnItemSwipeListener(onItemSwipeListener)
        upLoadAdapter?.draggableModule?.itemTouchHelperCallback?.setSwipeMoveFlags(ItemTouchHelper.START)

        recyclerView.adapter = upLoadAdapter

        upShowLoadAdapter = UpShowRecordAdapter(R.layout.up_record_item2_layout, arrayListOf())
        upShowLoadAdapter?.addChildClickViewIds(R.id.deleteImageView)
        upShowLoadAdapter?.setOnItemChildClickListener { _, _, position ->
            //删除对应的
        }
        upShowLoadAdapter?.setOnItemClickListener { _, _, position ->
            //跳转到播放页面
            var item = upShowLoadAdapter!!.data[position]
            val intent = Intent(this, TCVodPlayerActivity::class.java)
            intent.putExtra(UGCKitConstants.PLAY_URL, item.videoPath)
            intent.putExtra(UGCKitConstants.PUSHER_NAME, item.nickname)
            intent.putExtra(UGCKitConstants.PUSHER_AVATAR, item.avatar)
            intent.putExtra(UGCKitConstants.COVER_PIC, item.cover)
            intent.putExtra(UGCKitConstants.FILE_ID, item.shortVideoId)
            intent.putExtra(UGCKitConstants.TCLIVE_INFO_LIST, upShowLoadAdapter!!.data as Serializable)
            intent.putExtra(UGCKitConstants.TIMESTAMP, item.createTime)
            intent.putExtra(UGCKitConstants.TCLIVE_INFO_POSITION, position)
            startActivityForResult(intent, 2000)
        }
        upShowLoadAdapter?.loadMoreModule?.isEnableLoadMore = true
        upShowLoadAdapter?.loadMoreModule?.isAutoLoadMore = true
        upShowLoadAdapter?.loadMoreModule?.isEnableLoadMoreIfNotFullPage = false
        upShowLoadAdapter?.loadMoreModule?.setOnLoadMoreListener {
            pageNum++
            getShortUpRecord()
        }
    }

    override fun initData() {
        super.initData()
        getUpRecord()
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

            var layoutManager = LinearLayoutManager(this@UpLoadRecordActivity)
            layoutManager.orientation = LinearLayoutManager.VERTICAL
            recyclerView.layoutManager = layoutManager
            recyclerView.adapter = upLoadAdapter
            pageNum = 1
            getUpRecord()
        }
        playContain.onClick {
            playLineView.visibility = View.VISIBLE
            playTextView.textSize = 16f
            playTextView.textColor = Color.parseColor("#000000")

            singleShowLine.visibility = View.GONE
            singleShowText.textSize = 14f
            singleShowText.textColor = Color.parseColor("#999999")

            recyclerView.layoutManager = GridLayoutManager(this@UpLoadRecordActivity,2)
            recyclerView.adapter = upShowLoadAdapter
            pageNum = 1
            getShortUpRecord()
        }
    }

    /**
     * 获取剧场上传记录
     */
    private fun getUpRecord(){
        var params = HttpParams()
        params.put("pageNum",pageNum)
        params.put("pageSize", Constant.PAGE_SIZE)
        OkGo.get<BaseResponse<UpRecordResponse>>(Constant.QUERY_UP_RECORD)
                .params(params)
                .execute(object : JsonCallBack<BaseResponse<UpRecordResponse>>(){
                    override fun onFinish() {
                        super.onFinish()
                        upLoadAdapter?.loadMoreModule?.loadMoreComplete()
                    }

                    override fun onError(response: Response<BaseResponse<UpRecordResponse>>?) {
                        super.onError(response)
                        upLoadAdapter?.loadMoreModule?.loadMoreFail()
                    }

                    override fun onSuccess(response: Response<BaseResponse<UpRecordResponse>>?) {
                        if(response?.body()?.res == 0){
                            if(pageNum == 1){
                                upLoadAdapter?.setList(response.body()?.data!!.list)
                            }else{
                                upLoadAdapter?.addData(response.body()?.data!!.list)
                            }
                            if(response?.body()?.data?.pages == pageNum){
                                upLoadAdapter?.loadMoreModule?.loadMoreEnd(true)
                            }

                        }else{
                            toast(response?.body()?.msg.toString())
                        }
                    }

                })
    }

    /**
     * 获取短视频上传记录
     */
    private fun getShortUpRecord(){
        var params = HttpParams()
        params.put("pageNum",pageNum)
        params.put("pageSize", Constant.PAGE_SIZE)
        OkGo.get<BaseResponse<ShortVideoListResponse>>(Constant.QUERY_SHORT_UP_RECORD)
                .params(params)
                .execute(object : JsonCallBack<BaseResponse<ShortVideoListResponse>>(){
                    override fun onFinish() {
                        super.onFinish()
                        upShowLoadAdapter?.loadMoreModule?.loadMoreComplete()
                    }

                    override fun onError(response: Response<BaseResponse<ShortVideoListResponse>>?) {
                        super.onError(response)
                        upShowLoadAdapter?.loadMoreModule?.loadMoreFail()
                    }

                    override fun onSuccess(response: Response<BaseResponse<ShortVideoListResponse>>?) {
                        if(response?.body()?.res == 0){
                            if(pageNum == 1){
                                upShowLoadAdapter?.setList(response.body().data.list)
                            }else{
                                upShowLoadAdapter?.addData(response.body().data.list)
                            }
                            if(response?.body()?.data?.pages == pageNum){
                                upShowLoadAdapter?.loadMoreModule?.loadMoreEnd(true)
                            }

                        }else{
                            toast(response?.body()?.msg.toString())
                        }
                    }

                })
    }

}