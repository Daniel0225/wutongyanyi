package com.yiheoline.qcloud.xiaozhibo.homepage.adapter

import android.graphics.Color
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.yiheoline.qcloud.xiaozhibo.bean.MyNoticeBean
import com.yiheoline.qcloud.xiaozhibo.utils.TimeUtil
import com.yiheonline.qcloud.xiaozhibo.R

class PreShowAdapter(layoutId:Int, data:MutableList<MyNoticeBean>): BaseQuickAdapter<MyNoticeBean, BaseViewHolder>(layoutId,data){
    //state 状态 0: 审核中 1: 审核通过 2: 审核不通过
    var selectPosition = -1
    override fun convert(holder: BaseViewHolder, item: MyNoticeBean) {
        holder.setText(R.id.nameView,item.title)
        holder.setText(R.id.dateView,TimeUtil.getYearMonthAndDayWithHour(item.liveTime.toLong()))
        when (item.state) {
            2 -> {
                holder.setText(R.id.statusView,"审核不通过~")
                holder.setTextColor(R.id.statusView,Color.parseColor("#FD8318"))
            }
            1 -> {
                holder.setText(R.id.statusView,"审核通过~")
                holder.setTextColor(R.id.statusView,Color.parseColor("#FD8318"))
            }
            else -> {
                holder.setText(R.id.statusView,"审核中~")
                holder.setTextColor(R.id.statusView,Color.parseColor("#FD8318"))
            }
        }


        if(getItemPosition(item) == selectPosition){
            holder.setImageResource(R.id.editView,R.mipmap.selected)
        }else{
            holder.setImageResource(R.id.editView,R.mipmap.uncheck)
        }
    }


}