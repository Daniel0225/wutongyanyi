package com.yiheoline.qcloud.xiaozhibo.show

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import com.lzy.okgo.OkGo
import com.lzy.okgo.model.HttpParams
import com.lzy.okgo.model.Response
import com.tencent.qcloud.ugckit.UGCKitConstants
import com.yiheoline.liteav.demo.lvb.liveroom.roomutil.http.HttpRequests
import com.yiheoline.qcloud.xiaozhibo.Constant
import com.yiheoline.qcloud.xiaozhibo.TCApplication
import com.yiheoline.qcloud.xiaozhibo.audience.TCAudienceActivity
import com.yiheoline.qcloud.xiaozhibo.base.BaseFragment
import com.yiheoline.qcloud.xiaozhibo.bean.LiveRoom
import com.yiheoline.qcloud.xiaozhibo.bean.RoomInfoBean
import com.yiheoline.qcloud.xiaozhibo.common.utils.TCConstants
import com.yiheoline.qcloud.xiaozhibo.http.BaseResponse
import com.yiheoline.qcloud.xiaozhibo.http.JsonCallBack
import com.yiheoline.qcloud.xiaozhibo.http.response.ShortVideoListResponse
import com.yiheoline.qcloud.xiaozhibo.show.adapter.LiveListAdapter
import com.yiheoline.qcloud.xiaozhibo.show.adapter.ShowListAdapter
import com.yiheoline.qcloud.xiaozhibo.utils.FastJsonUtil
import com.yiheonline.qcloud.xiaozhibo.R
import kotlinx.android.synthetic.main.fragment_show.*
import org.jetbrains.anko.sdk27.coroutines.onClick
import org.jetbrains.anko.support.v4.onRefresh
import org.jetbrains.anko.support.v4.toast
import org.jetbrains.anko.textColor
import java.io.Serializable

class ShowFragment : BaseFragment() {
    var pageNum = 1
    var adapter : ShowListAdapter? = null
    var liveAdapter : LiveListAdapter? = null
    var type = 0 //0 短视频 1 直播

    override fun getLayout(): Int {
        return R.layout.fragment_show
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        var manager = GridLayoutManager(context,2)
        recyclerView.layoutManager = manager
        adapter = ShowListAdapter(R.layout.show_list_item_layout, arrayListOf())
        liveAdapter = LiveListAdapter(R.layout.show_list_item_layout, arrayListOf())
        var emptyView = layoutInflater.inflate(R.layout.order_empty_layout,null)
        adapter?.setEmptyView(emptyView)
        recyclerView.adapter = adapter
        adapter?.setOnItemClickListener { _, _, position ->
            //跳转到播放页面
            var item = adapter!!.data[position]
            val intent = Intent(activity, TCVodPlayerActivity::class.java)
            intent.putExtra(UGCKitConstants.PLAY_URL, item.videoPath)
            intent.putExtra(UGCKitConstants.PUSHER_NAME, item.nickname)
            intent.putExtra(UGCKitConstants.PUSHER_AVATAR, item.avatar)
            intent.putExtra(UGCKitConstants.COVER_PIC, item.cover)
            intent.putExtra(UGCKitConstants.FILE_ID, item.shortVideoId)
            intent.putExtra(UGCKitConstants.TCLIVE_INFO_LIST, adapter!!.data as Serializable)
            intent.putExtra(UGCKitConstants.TIMESTAMP, item.createTime)
            intent.putExtra(UGCKitConstants.TCLIVE_INFO_POSITION, position)
            startActivityForResult(intent, 2000)
        }

        adapter?.loadMoreModule?.isEnableLoadMore = true
        adapter?.loadMoreModule?.isAutoLoadMore = true
        adapter?.loadMoreModule?.isEnableLoadMoreIfNotFullPage = false
        adapter?.loadMoreModule?.setOnLoadMoreListener {
            pageNum++
            getVideoList()
        }
        //直播列表adapter
        liveAdapter?.setOnItemClickListener { _, _, position ->
            var liveBean = liveAdapter!!.data[position]
            getPlayUrl(liveBean.roomId,liveBean.likes,liveBean.liveId)
        }
        swipeRefresh.setColorSchemeResources(R.color.colorAccent)
        swipeRefresh.onRefresh {
            pageNum = 1
            if(type == 0){
                getVideoList()
            }else{
                getShowList()
            }
        }
    }

    override fun lazyLoad() {
        getVideoList()
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

            recyclerView.adapter = adapter
            getVideoList()
        }
        playContain.onClick {
            type = 1
            playLineView.visibility = View.VISIBLE
            playTextView.textSize = 16f
            playTextView.textColor = Color.parseColor("#000000")

            singleShowLine.visibility = View.GONE
            singleShowText.textSize = 14f
            singleShowText.textColor = Color.parseColor("#999999")

            recyclerView.adapter = liveAdapter
            getShowList()
        }
    }

    /**
     * 获取短视频列表
     */
    private fun getVideoList(){
        var params = HttpParams()
        params.put("pageNum",pageNum)
        params.put("pageSize", Constant.PAGE_SIZE)
        OkGo.post<BaseResponse<ShortVideoListResponse>>(Constant.SHORT_VIDEO_LIST)
                .params(params)
                .execute(object : JsonCallBack<BaseResponse<ShortVideoListResponse>>(){

                    override fun onFinish() {
                        super.onFinish()
                        swipeRefresh.isRefreshing = false
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
     * 获取直播房间
     */
    private fun getShowList(){
        OkGo.get<BaseResponse<List<LiveRoom>>>(Constant.LIVE_LIST)
                .execute(object : JsonCallBack<BaseResponse<List<LiveRoom>>>(){
                    override fun onSuccess(response: Response<BaseResponse<List<LiveRoom>>>?) {
                        if(response?.body()?.res == 0){
                            liveAdapter?.setList(response?.body().data)
                        }else{
                            toast(response?.body()?.msg.toString())
                        }
                    }

                })
    }

    /**
     * 调用直播服务器 查询直播地址
     */
    private fun getPlayUrl(roomId: String,likeNum:Int,liveId:Int){
        var mHttpRequest = HttpRequests("https://liveroom.qcloud.com/weapp/live_room")
        mHttpRequest.setToken(TCApplication.mlvbToken)
        mHttpRequest.setUserID(TCApplication.loginInfo?.userId)
        mHttpRequest.getPushers(roomId) { retcode, retmsg, data ->
            var intent = Intent(context, TCAudienceActivity::class.java)
            intent.putExtra(TCConstants.PLAY_URL, data!!.mixedPlayURL)
            intent.putExtra(TCConstants.HEART_COUNT, likeNum.toString())
            intent.putExtra(TCConstants.MEMBER_COUNT, data.audienceCount.toString())
            intent.putExtra(TCConstants.GROUP_ID, data.roomID)
            intent.putExtra(TCConstants.PUSHER_ID, data.roomCreator)
            intent.putExtra(TCConstants.PUSHER_NAME, data.pushers[0].userName)
            intent.putExtra(TCConstants.PUSHER_AVATAR, data.pushers[0].userAvatar)
            intent.putExtra(TCConstants.PLAY_TYPE, true)
            intent.putExtra(TCConstants.FILE_ID,  "")
            var roomInfo = FastJsonUtil.getObject(data.roomInfo, RoomInfoBean::class.java)
            intent.putExtra(TCConstants.ROOM_TITLE, roomInfo.title)
            intent.putExtra("LIVE_ID",liveId)
            startActivityForResult(intent,2000)
        }
    }

    companion object {
        private const val ARG_PARAM1 = "param1"
        private const val ARG_PARAM2 = "param2"

        @JvmStatic
        fun newInstance(param1: String?, param2: String?): ShowFragment {
            val fragment = ShowFragment()
            val args = Bundle()
            args.putString(ARG_PARAM1, param1)
            args.putString(ARG_PARAM2, param2)
            fragment.arguments = args
            return fragment
        }
    }
}