package com.yiheoline.qcloud.xiaozhibo.show.adapter

import com.bumptech.glide.Glide
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.yiheoline.qcloud.xiaozhibo.Constant
import com.yiheoline.qcloud.xiaozhibo.bean.LiveRoom
import com.yiheonline.qcloud.xiaozhibo.R

class LiveListAdapter(layoutId:Int, data:MutableList<LiveRoom>): BaseQuickAdapter<LiveRoom, BaseViewHolder>(layoutId,data){
    override fun convert(holder: BaseViewHolder, item: LiveRoom) {
        Glide.with(context).load(Constant.IMAGE_BASE+item.cover).into(holder.getView(R.id.imageView))
        Glide.with(context).load(Constant.IMAGE_BASE+item.avatar).into(holder.getView(R.id.headerView))
        holder.setText(R.id.titleView,item.title)
        holder.setText(R.id.nameView,item.nickname)
        holder.setText(R.id.likeNumView,item.number.toString())
    }


}