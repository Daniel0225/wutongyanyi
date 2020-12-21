package com.yiheoline.qcloud.xiaozhibo.show

import android.content.Intent
import android.os.Bundle
import android.os.UserManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.lzy.okgo.OkGo
import com.lzy.okgo.model.Response
import com.yiheoline.qcloud.xiaozhibo.audience.TCAudienceActivity
import com.yiheoline.qcloud.xiaozhibo.common.utils.TCConstants
import com.yiheoline.qcloud.xiaozhibo.http.BaseResponse
import com.yiheoline.qcloud.xiaozhibo.http.JsonCallBack
import com.yiheoline.qcloud.xiaozhibo.http.response.ShowListResponse
import com.yiheoline.qcloud.xiaozhibo.login.TCUserMgr
import com.yiheoline.qcloud.xiaozhibo.show.adapter.ShowListAdapter
import com.yiheonline.qcloud.xiaozhibo.R
import kotlinx.android.synthetic.main.fragment_show.*

class ShowFragment : Fragment() {
    private var mParam1: String? = null
    private var mParam2: String? = null
    var adapter : ShowListAdapter? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (arguments != null) {
            mParam1 = arguments!!.getString(ARG_PARAM1)
            mParam2 = arguments!!.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_show, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        var manager = LinearLayoutManager(context)
        manager.orientation = LinearLayoutManager.VERTICAL
        recyclerView.layoutManager = manager
        adapter = ShowListAdapter(R.layout.show_list_item_layout, arrayListOf())
        var emptyView = layoutInflater.inflate(R.layout.order_empty_layout,null)
        adapter?.setEmptyView(emptyView)
        recyclerView.adapter = adapter
        adapter?.setOnItemClickListener { _, _, position ->
            var item = adapter!!.data[position]
            var intent = Intent(activity, TCAudienceActivity::class.java)
            intent.putExtra(TCConstants.PLAY_URL, item.mixedPlayURL)
            intent.putExtra(TCConstants.HEART_COUNT, "51512")
            intent.putExtra(TCConstants.MEMBER_COUNT, "65156")
            intent.putExtra(TCConstants.GROUP_ID, item.roomID)
            intent.putExtra(TCConstants.PLAY_TYPE, true)
            intent.putExtra(TCConstants.FILE_ID,  "")
            intent.putExtra(TCConstants.ROOM_TITLE, "121211212")
            startActivityForResult(intent,2000)
        }
        getShowList()
    }

    /**
     * 获取直播房间
     */
    private fun getShowList(){
//        OkGo.post<BaseResponse<List<ShowListResponse>>>("http://192.168.0.85:8085/live/getRoomList?userID="+TCUserMgr.getInstance().userId)
//                .execute(object : JsonCallBack<BaseResponse<List<ShowListResponse>>>(){
//                    override fun onSuccess(response: Response<BaseResponse<List<ShowListResponse>>>?) {
//                        adapter?.setNewData(response?.body()?.data!! as MutableList<ShowListResponse>)
//                    }
//
//                })

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