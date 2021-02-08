package com.yiheoline.qcloud.xiaozhibo.profile.adapter

import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.module.LoadMoreModule
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.yiheoline.qcloud.xiaozhibo.bean.RechargeBean
import com.yiheoline.qcloud.xiaozhibo.bean.WithDrawBean
import com.yiheoline.qcloud.xiaozhibo.utils.TimeUtil
import com.yiheonline.qcloud.xiaozhibo.R

class WithDrawListAdapter(layoutId:Int, data:MutableList<WithDrawBean>): BaseQuickAdapter<WithDrawBean, BaseViewHolder>(layoutId,data), LoadMoreModule {
    var states = arrayListOf("","处理中","已处理","已驳回")
    override fun convert(holder: BaseViewHolder, item: WithDrawBean) {
        holder.setText(R.id.moneyView,"¥ ${item.money}")
        holder.setText(R.id.payTypeView,"支付宝：${item.alipayAccount}")
        holder.setText(R.id.timeView,TimeUtil.getYearMonthAndDayWithHour(item.createTime.toLong()))
        holder.setText(R.id.stateView,states[item.state])
    }


}