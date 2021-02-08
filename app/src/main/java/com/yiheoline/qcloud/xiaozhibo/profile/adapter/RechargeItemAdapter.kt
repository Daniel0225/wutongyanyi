package com.yiheoline.qcloud.xiaozhibo.profile.adapter

import android.graphics.Color
import android.widget.RelativeLayout
import android.widget.TextView
import com.bumptech.glide.Glide
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.yiheoline.qcloud.xiaozhibo.bean.UpRecordBean
import com.yiheonline.qcloud.xiaozhibo.R

class RechargeItemAdapter(layoutId:Int, data:MutableList<String>): BaseQuickAdapter<String, BaseViewHolder>(layoutId,data){
    var selectPosition = 0
    override fun convert(holder: BaseViewHolder, item: String) {
        var rootView = holder.getView<RelativeLayout>(R.id.rootView)

        if(holder.layoutPosition == selectPosition){
            holder.setVisible(R.id.selectTagView,true)
            holder.setTextColor(R.id.contentView,Color.parseColor("#FD8318"))
            holder.setTextColor(R.id.priceView,Color.parseColor("#FD8318"))
            rootView.setBackgroundResource(R.drawable.bg_yellow_line)
        }else{
            holder.setVisible(R.id.selectTagView,false)
            holder.setTextColor(R.id.contentView,Color.parseColor("#333333"))
            holder.setTextColor(R.id.priceView,Color.parseColor("#999999"))
            rootView.setBackgroundResource(R.drawable.bg_gray_line)
        }
    }


}