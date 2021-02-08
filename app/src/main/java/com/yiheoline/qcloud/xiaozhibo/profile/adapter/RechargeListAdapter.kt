package com.yiheoline.qcloud.xiaozhibo.profile.adapter

import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.module.LoadMoreModule
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.yiheoline.qcloud.xiaozhibo.bean.RechargeBean
import com.yiheoline.qcloud.xiaozhibo.utils.TimeUtil
import com.yiheonline.qcloud.xiaozhibo.R

class RechargeListAdapter(layoutId:Int, data:MutableList<RechargeBean>): BaseQuickAdapter<RechargeBean, BaseViewHolder>(layoutId,data), LoadMoreModule {
    var payTypes = arrayListOf("支付宝","微信","银行卡")
    override fun convert(holder: BaseViewHolder, item: RechargeBean) {
        holder.setText(R.id.payTypeView,payTypes[item.payPlatform])
        holder.setText(R.id.timeView,TimeUtil.getYearMonthAndDayWithHour(item.payTime.toLong()))
        holder.setText(R.id.moneyView,"¥ ${item.money}")
    }


}