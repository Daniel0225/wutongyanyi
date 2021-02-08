package com.yiheoline.qcloud.xiaozhibo.profile.adapter

import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.module.LoadMoreModule
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.yiheoline.qcloud.xiaozhibo.bean.NoticeOrderBean
import com.yiheoline.qcloud.xiaozhibo.utils.TimeUtil
import com.yiheonline.qcloud.xiaozhibo.R
import kotlinx.android.synthetic.main.order_item_layout.view.*

class OrderListAdapter(layoutId:Int, data:MutableList<NoticeOrderBean>): BaseQuickAdapter<NoticeOrderBean, BaseViewHolder>(layoutId,data),LoadMoreModule{
    var states = arrayListOf("","待支付","已支付","退款中","已退款")
    var liveStates = arrayListOf("未开播","直播中","已完成")
    override fun convert(holder: BaseViewHolder, item: NoticeOrderBean) {
        holder.setText(R.id.dateView,"购买时间：${TimeUtil.getYearMonthAndDayWithHour(item.createTime.toLong())}")
        holder.setText(R.id.statusView,states[item.state])
        holder.setText(R.id.nameView,item.title)
        if(item.liveTime != null){
            holder.setText(R.id.startTimeView,"${TimeUtil.getYearMonthAndDayWithHour(item.liveTime.toLong())} 开播")
        }
        holder.setText(R.id.priceView,"¥ ${item.totalMoney}")
        holder.setText(R.id.liveStateView,liveStates[item.liveState])
    }


}