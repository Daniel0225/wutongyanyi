package com.yiheoline.qcloud.xiaozhibo.homepage.adapter

import com.bumptech.glide.Glide
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.yiheoline.qcloud.xiaozhibo.Constant
import com.yiheonline.qcloud.xiaozhibo.R

class DetailImageAdapter(layoutId:Int, data:MutableList<String>): BaseQuickAdapter<String, BaseViewHolder>(layoutId,data){
    override fun convert(holder: BaseViewHolder, item: String) {
        Glide.with(context).load(Constant.IMAGE_BASE+item).into(holder.getView(R.id.imageView))
    }
}