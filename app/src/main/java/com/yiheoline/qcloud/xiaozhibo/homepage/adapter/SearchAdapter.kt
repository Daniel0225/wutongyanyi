package com.yiheoline.qcloud.xiaozhibo.homepage.adapter

import com.bumptech.glide.Glide
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.yiheoline.qcloud.xiaozhibo.bean.ShowNoticeBean
import com.yiheoline.qcloud.xiaozhibo.utils.TimeUtil
import com.yiheonline.qcloud.xiaozhibo.R

class SearchAdapter(layoutId:Int, data:MutableList<ShowNoticeBean>): BaseQuickAdapter<ShowNoticeBean, BaseViewHolder>(layoutId,data){
    override fun convert(holder: BaseViewHolder, item: ShowNoticeBean) {
        holder.setText(R.id.noticeTitleView,item.title)
        holder.setText(R.id.dateView,TimeUtil.getYearMonthAndDayWithHour(item.liveTime.toLong()))
        holder.setText(R.id.priceView,item.price.toString())
        Glide.with(context).load(item.cover).into(holder.getView(R.id.coverImage))
    }


}