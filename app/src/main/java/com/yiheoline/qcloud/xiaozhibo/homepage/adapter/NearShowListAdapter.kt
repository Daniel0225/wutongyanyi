package com.yiheoline.qcloud.xiaozhibo.homepage.adapter

import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.yiheoline.qcloud.xiaozhibo.Constant
import com.yiheoline.qcloud.xiaozhibo.bean.ShowNoticeBean
import com.yiheoline.qcloud.xiaozhibo.utils.GlideRoundTransform
import com.yiheoline.qcloud.xiaozhibo.utils.TimeUtil
import com.yiheonline.qcloud.xiaozhibo.R

class NearShowListAdapter(layoutId:Int, data:MutableList<ShowNoticeBean>):BaseQuickAdapter<ShowNoticeBean, BaseViewHolder>(layoutId,data){
    override fun convert(holder: BaseViewHolder, item: ShowNoticeBean) {
        holder.setText(R.id.titleView,item.title)
        var dayString = TimeUtil.getMonthAndDay(item.liveTime.toLong())
        var currentDayString = TimeUtil.getMonthAndDay(System.currentTimeMillis())
        dayString = when {
            dayString == currentDayString -> {
                "今日 " + TimeUtil.getHourAndMin(item.liveTime.toLong())
            }
            TimeUtil.isBeforeDay(item.liveTime.toLong()) -> {
                "明日 " + TimeUtil.getHourAndMin(item.liveTime.toLong())
            }
            else -> {
                TimeUtil.getMonthAndDayWithHour(item.liveTime.toLong())
            }
        }
        holder.setText(R.id.timeView,dayString)
        Glide.with(context).load(Constant.IMAGE_BASE+item.cover)
                .transform(CenterCrop(context),
                        GlideRoundTransform(context,5)).into(holder.getView(R.id.imageView))
    }

}