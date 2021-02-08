package com.yiheoline.qcloud.xiaozhibo.profile.adapter

import com.bumptech.glide.Glide
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.module.LoadMoreModule
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.yiheoline.qcloud.xiaozhibo.Constant
import com.yiheoline.qcloud.xiaozhibo.bean.GiftRecord
import com.yiheoline.qcloud.xiaozhibo.utils.TimeUtil
import com.yiheonline.qcloud.xiaozhibo.R

class GiftListAdapter(layoutId:Int, data:MutableList<GiftRecord>): BaseQuickAdapter<GiftRecord, BaseViewHolder>(layoutId,data), LoadMoreModule {
    override fun convert(holder: BaseViewHolder, item: GiftRecord) {
        holder.setText(R.id.nameView,item.nickname)
        holder.setText(R.id.showNameView,item.name)
        holder.setText(R.id.timeView,TimeUtil.getYearMonthAndDay(item.createTime.toLong()))
        Glide.with(context).load(Constant.IMAGE_BASE+item.avatar).into(holder.getView(R.id.headerView))
    }


}