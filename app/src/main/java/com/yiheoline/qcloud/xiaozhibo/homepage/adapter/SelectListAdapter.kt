package com.yiheoline.qcloud.xiaozhibo.homepage.adapter

import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.yiheoline.qcloud.xiaozhibo.Constant
import com.yiheoline.qcloud.xiaozhibo.bean.ShowNoticeBean
import com.yiheoline.qcloud.xiaozhibo.utils.GlideRoundTransform
import com.yiheonline.qcloud.xiaozhibo.R

class SelectListAdapter(layoutId:Int, data:MutableList<ShowNoticeBean>):BaseQuickAdapter<ShowNoticeBean, BaseViewHolder>(layoutId,data){
    override fun convert(holder: BaseViewHolder, item: ShowNoticeBean) {
        holder.setText(R.id.titleView,item.title)
        Glide.with(context).load(Constant.IMAGE_BASE+item.cover)
                .transform(CenterCrop(context),
                        GlideRoundTransform(context,5)).into(holder.getView(R.id.imageView))
    }

}