package com.yiheoline.qcloud.xiaozhibo.homepage

import android.view.View
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.yiheoline.qcloud.xiaozhibo.base.BaseActivity
import com.yiheoline.qcloud.xiaozhibo.homepage.adapter.PreShowAdapter
import com.yiheonline.qcloud.xiaozhibo.R
import kotlinx.android.synthetic.main.activity_show_setting.*
import kotlinx.android.synthetic.main.toolbar_layout.*
import org.jetbrains.anko.sdk27.coroutines.onClick
import org.jetbrains.anko.startActivity

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
        preShowAdapter = PreShowAdapter(R.layout.pre_show_item_layout, arrayListOf("",""))
        recyclerView.adapter = preShowAdapter
    }
}