package com.yiheoline.qcloud.xiaozhibo.profile.adapter

import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.module.LoadMoreModule
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.yiheoline.qcloud.xiaozhibo.Constant
import com.yiheoline.qcloud.xiaozhibo.bean.LikeShowBean
import com.yiheoline.qcloud.xiaozhibo.utils.GlideRoundTransform
import com.yiheoline.qcloud.xiaozhibo.utils.TimeUtil
import com.yiheonline.qcloud.xiaozhibo.R

class LikeShowAdapter(layoutId:Int, data:MutableList<LikeShowBean>): BaseQuickAdapter<LikeShowBean, BaseViewHolder>(layoutId,data), LoadMoreModule {
    override fun convert(holder: BaseViewHolder, item: LikeShowBean) {
        Glide.with(context).load(Constant.IMAGE_BASE+item.cover).transform(CenterCrop(context),
                GlideRoundTransform(context,5)).into(holder.getView(R.id.imageView))
        holder.setText(R.id.nameView,item.title)
        holder.setText(R.id.dateView,TimeUtil.getYearMonthAndDayWithHour(item.liveTime.toLong()))
    }


}