package com.yiheoline.qcloud.xiaozhibo.video.adapter

import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.yiheoline.qcloud.xiaozhibo.Constant
import com.yiheoline.qcloud.xiaozhibo.bean.CommentBean
import com.yiheoline.qcloud.xiaozhibo.utils.GlideRoundTransform
import com.yiheonline.qcloud.xiaozhibo.R

class CommentListAdapter(layoutId:Int, data:MutableList<CommentBean>):BaseQuickAdapter<CommentBean, BaseViewHolder>(layoutId,data){
    override fun convert(holder: BaseViewHolder, item: CommentBean) {
        holder.setText(R.id.nameView,item.nickname)
        holder.setText(R.id.contentView,item.content)
        holder.setText(R.id.likeNumView,item.likes.toString())
        var isLikeImageView = holder.getView<ImageView>(R.id.isLikeImageView)
        if(item.isLike == null){
            isLikeImageView.setImageResource(R.mipmap.unlike)
        }else{
            isLikeImageView.setImageResource(R.mipmap.like)
        }
        Glide.with(context).load(Constant.IMAGE_BASE+item.avatar)
                .placeholder(R.mipmap.default_header)
                .into(holder.getView(R.id.headerView))

    }

}