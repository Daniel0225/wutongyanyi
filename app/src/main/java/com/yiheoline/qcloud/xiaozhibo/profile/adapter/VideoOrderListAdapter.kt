package com.yiheoline.qcloud.xiaozhibo.profile.adapter

import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.module.LoadMoreModule
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.yiheoline.qcloud.xiaozhibo.bean.NoticeOrderBean
import com.yiheoline.qcloud.xiaozhibo.bean.VideoOrderBean
import com.yiheoline.qcloud.xiaozhibo.utils.TimeUtil
import com.yiheonline.qcloud.xiaozhibo.R
import kotlinx.android.synthetic.main.order_item_layout.view.*

class VideoOrderListAdapter(layoutId:Int, data:MutableList<VideoOrderBean>): BaseQuickAdapter<VideoOrderBean, BaseViewHolder>(layoutId,data),LoadMoreModule{
    override fun convert(holder: BaseViewHolder, item: VideoOrderBean) {
        holder.setText(R.id.titleView,item.title)
        holder.setText(R.id.timeView,TimeUtil.getYearMonthAndDayWithHour(item.createTime.toLong()))
        holder.setText(R.id.priceView,"¥ ${item.totalMoney}")
    }


}