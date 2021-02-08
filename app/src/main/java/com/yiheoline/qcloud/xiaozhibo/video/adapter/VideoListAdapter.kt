package com.yiheoline.qcloud.xiaozhibo.video.adapter

import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.module.LoadMoreModule
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.yiheoline.qcloud.xiaozhibo.Constant
import com.yiheoline.qcloud.xiaozhibo.bean.VideoBean
import com.yiheoline.qcloud.xiaozhibo.utils.GlideRoundTransform
import com.yiheonline.qcloud.xiaozhibo.R

class VideoListAdapter(layoutId:Int, data:MutableList<VideoBean>):BaseQuickAdapter<VideoBean, BaseViewHolder>(layoutId,data), LoadMoreModule {
    override fun convert(holder: BaseViewHolder, item: VideoBean) {
        holder.setText(R.id.titleView,item.title)
        holder.setText(R.id.descView,item.subtitle)
        Glide.with(context).load(Constant.IMAGE_BASE+item.secondCover)
                .transform(CenterCrop(context), GlideRoundTransform(context,5))
                .into(holder.getView(R.id.imageView))

    }

}