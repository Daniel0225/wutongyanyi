package com.yiheoline.qcloud.xiaozhibo.homepage

import android.content.Intent
import android.view.View
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.dyhdyh.widget.loadingbar2.LoadingBar
import com.lzy.okgo.OkGo
import com.lzy.okgo.model.Response
import com.lzy.okgo.request.base.Request
import com.yiheoline.qcloud.xiaozhibo.Constant
import com.yiheoline.qcloud.xiaozhibo.TCApplication
import com.yiheoline.qcloud.xiaozhibo.anchor.TCCameraAnchorActivity
import com.yiheoline.qcloud.xiaozhibo.base.BaseActivity
import com.yiheoline.qcloud.xiaozhibo.bean.MyNoticeBean
import com.yiheoline.qcloud.xiaozhibo.common.utils.TCConstants
import com.yiheoline.qcloud.xiaozhibo.homepage.adapter.PreShowAdapter
import com.yiheoline.qcloud.xiaozhibo.http.BaseResponse
import com.yiheoline.qcloud.xiaozhibo.http.JsonCallBack
import com.yiheonline.qcloud.xiaozhibo.R
import kotlinx.android.synthetic.main.activity_show_setting.*
import kotlinx.android.synthetic.main.toolbar_layout.*
import org.jetbrains.anko.sdk27.coroutines.onClick
import org.jetbrains.anko.startActivity
import org.jetbrains.anko.toast

class ShowSettingActivity : BaseActivity() {
    var preShowAdapter : PreShowAdapter? = null
    override fun getLayoutId(): Int {
        return R.layout.activity_show_setting
    }

    override fun initView() {
        super.initView()
        backView.onClick { finish() }
        titleView.text = "演出直播设置"
        rightBtn.visibility = View.VISIBLE
        rightBtn.text = "申请直播"
        rightBtn.onClick {
            startActivity<ApplyShowActivity>()
        }


        var layoutManager = LinearLayoutManager(this)
        layoutManager.orientation = LinearLayoutManager.VERTICAL
        recyclerView.layoutManager = layoutManager
        preShowAdapter = PreShowAdapter(R.layout.pre_show_item_layout, arrayListOf())
        recyclerView.adapter = preShowAdapter
        preShowAdapter?.setOnItemClickListener { _, _, position ->
            if(preShowAdapter!!.data[position].state == 1){//审核通过
                preShowAdapter?.selectPosition = position
                preShowAdapter?.notifyDataSetChanged()
            }
        }

        startPlayBtn.onClick {
            if(preShowAdapter?.selectPosition == -1){
                toast("请选择直播场次")
                return@onClick
            }
            var item = preShowAdapter!!.data[preShowAdapter!!.selectPosition]
            var intent = Intent(mContext, TCCameraAnchorActivity::class.java)
            intent.putExtra(TCConstants.ROOM_TITLE,item.title)
            intent.putExtra(TCConstants.USER_NICK,TCApplication.loginInfo?.nickname)
            intent.putExtra(TCConstants.COVER_PIC,item.cover)
            intent.putExtra(TCConstants.USER_HEADPIC,Constant.IMAGE_BASE+TCApplication.loginInfo?.avatar)
            intent.putExtra(TCConstants.USER_ID, TCApplication.loginInfo?.userId)
            intent.putExtra(TCConstants.NOTICE_ID,item.noticeId)
            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        getNoticeList()
    }

    /**
     * 获取我的申请列表
     */
    private fun getNoticeList(){
        OkGo.post<BaseResponse<List<MyNoticeBean>>>(Constant.QUERY_MY_NOTICE_LIST)
                .execute(object : JsonCallBack<BaseResponse<List<MyNoticeBean>>>(){

                    override fun onStart(request: Request<BaseResponse<List<MyNoticeBean>>, out Request<Any, Request<*, *>>>?) {
                        super.onStart(request)
                        LoadingBar.dialog(mContext).show()
                    }

                    override fun onFinish() {
                        super.onFinish()
                        LoadingBar.dialog(mContext).cancel()
                    }
                    override fun onSuccess(response: Response<BaseResponse<List<MyNoticeBean>>>?) {
                        if(response?.body()?.res == 0){
                            preShowAdapter?.setList(response?.body()?.data)
                        }else{
                            toast(response?.body()?.msg+"")
                        }
                    }

                })
    }
}