package com.yiheoline.qcloud.xiaozhibo.profile.adapter

import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.module.DraggableModule
import com.chad.library.adapter.base.module.LoadMoreModule
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.yiheoline.qcloud.xiaozhibo.Constant
import com.yiheoline.qcloud.xiaozhibo.bean.LikeVideoBean
import com.yiheoline.qcloud.xiaozhibo.bean.UpRecordBean
import com.yiheoline.qcloud.xiaozhibo.utils.GlideRoundTransform
import com.yiheonline.qcloud.xiaozhibo.R
import kotlinx.android.synthetic.main.up_record_item_layout.view.*

class LikeVideoAdapter(layoutId:Int, data:MutableList<LikeVideoBean>): BaseQuickAdapter<LikeVideoBean, BaseViewHolder>(layoutId,data), LoadMoreModule {
    override fun convert(holder: BaseViewHolder, item: LikeVideoBean) {
        holder.setText(R.id.nameView,item.title)
        Glide.with(context).load(Constant.IMAGE_BASE+item.firstCover).transform(CenterCrop(context),
                GlideRoundTransform(context,5)).into(holder.getView(R.id.imageView))
        holder.setGone(R.id.numView,true)
    }


}