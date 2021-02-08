package com.yiheoline.qcloud.xiaozhibo.profile.adapter

import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.module.LoadMoreModule
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.yiheoline.qcloud.xiaozhibo.bean.LetterBean
import com.yiheoline.qcloud.xiaozhibo.utils.TimeUtil
import com.yiheonline.qcloud.xiaozhibo.R

class LetterListAdapter(layoutId:Int, data:MutableList<LetterBean>): BaseQuickAdapter<LetterBean, BaseViewHolder>(layoutId,data), LoadMoreModule {
    override fun convert(holder: BaseViewHolder, item: LetterBean) {
        holder.setText(R.id.letterTypeView,item.title)
        holder.setText(R.id.contentView,item.content)
        holder.setText(R.id.dateView,TimeUtil.getYearMonthAndDayWithHour(item.createTime.toLong()))
    }


}