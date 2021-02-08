package com.yiheoline.qcloud.xiaozhibo.profile.adapter

import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.module.LoadMoreModule
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.yiheoline.qcloud.xiaozhibo.bean.LiveRecordBean
import com.yiheoline.qcloud.xiaozhibo.utils.TimeUtil
import com.yiheonline.qcloud.xiaozhibo.R
import kotlinx.android.synthetic.main.anchor_center_item_layout.view.*

class AnchorCenterItemAdapter(layoutId:Int, data:MutableList<LiveRecordBean>): BaseQuickAdapter<LiveRecordBean, BaseViewHolder>(layoutId,data),LoadMoreModule{
    override fun convert(holder: BaseViewHolder, item: LiveRecordBean) {
        holder.setText(R.id.nameView,item.title)
        holder.setText(R.id.dateView,"直播时间：${TimeUtil.getYearMonthAndDayWithHour(item.liveTime.toLong())}")
        holder.setText(R.id.getWtbNumView,item.totalWutongbi.toString())
        holder.setText(R.id.seeNumView,item.number.toString())
    }


}