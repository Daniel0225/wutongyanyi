package com.yiheoline.qcloud.xiaozhibo.profile.adapter

import com.bumptech.glide.Glide
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.module.DraggableModule
import com.chad.library.adapter.base.module.LoadMoreModule
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.yiheoline.qcloud.xiaozhibo.Constant
import com.yiheoline.qcloud.xiaozhibo.bean.UpRecordBean
import com.yiheonline.qcloud.xiaozhibo.R
import kotlinx.android.synthetic.main.up_record_item_layout.view.*

class UpRecordAdapter(layoutId:Int, data:MutableList<UpRecordBean>): BaseQuickAdapter<UpRecordBean, BaseViewHolder>(layoutId,data), DraggableModule, LoadMoreModule {
    override fun convert(holder: BaseViewHolder, item: UpRecordBean) {
        holder.setText(R.id.nameView,item.title)
        holder.setText(R.id.numView,item.purchases.toString())
        Glide.with(context).load(Constant.IMAGE_BASE+item.secondCover).into(holder.getView(R.id.imageView))
    }


}