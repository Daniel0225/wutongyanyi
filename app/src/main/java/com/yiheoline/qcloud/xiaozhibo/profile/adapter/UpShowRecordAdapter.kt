package com.yiheoline.qcloud.xiaozhibo.profile.adapter

import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.module.LoadMoreModule
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.yiheoline.qcloud.xiaozhibo.bean.ShortVideoBean
import com.yiheoline.qcloud.xiaozhibo.bean.UpRecordBean
import com.yiheoline.qcloud.xiaozhibo.utils.GlideRoundTransform
import com.yiheonline.qcloud.xiaozhibo.R

class UpShowRecordAdapter(layoutId:Int, data:MutableList<ShortVideoBean>): BaseQuickAdapter<ShortVideoBean, BaseViewHolder>(layoutId,data), LoadMoreModule {
    override fun convert(holder: BaseViewHolder, item: ShortVideoBean) {
        holder.setText(R.id.numView,item.views.toString())
        Glide.with(context).load(item.cover).transform(CenterCrop(context),
                GlideRoundTransform(context,5)).into(holder.getView(R.id.productImage))
    }


}