package com.yiheoline.qcloud.xiaozhibo.homepage.adapter

import com.bumptech.glide.Glide
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.yiheoline.qcloud.xiaozhibo.Constant
import com.yiheoline.qcloud.xiaozhibo.bean.GiftBean
import com.yiheonline.qcloud.xiaozhibo.R

class GiftListAdapter(layoutId:Int, data:MutableList<GiftBean>): BaseQuickAdapter<GiftBean, BaseViewHolder>(layoutId,data){
    override fun convert(holder: BaseViewHolder, item: GiftBean) {
        holder.setText(R.id.nameView,item.name)
        holder.setText(R.id.numView,"${item.wutongye}梧桐叶")
        Glide.with(context).load(Constant.IMAGE_BASE+item.giftLogo).into(holder.getView(R.id.imageView))
    }
}