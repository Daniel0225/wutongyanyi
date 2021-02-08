package com.yiheoline.qcloud.xiaozhibo.video.adapter

import android.widget.ImageView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.yiheoline.qcloud.xiaozhibo.Constant
import com.yiheoline.qcloud.xiaozhibo.bean.CommentBean
import com.yiheoline.qcloud.xiaozhibo.utils.GlideRoundTransform
import com.yiheoline.qcloud.xiaozhibo.utils.TimeUtil
import com.yiheonline.qcloud.xiaozhibo.R

class ReplyAdapter(layoutId:Int, data:MutableList<CommentBean>):BaseQuickAdapter<CommentBean, BaseViewHolder>(layoutId,data){
    override fun convert(holder: BaseViewHolder, item: CommentBean) {
        holder.setText(R.id.nameView,item.nickname)
        holder.setText(R.id.nameView2,item.replyName)
        holder.setText(R.id.contentView,item.content)
        holder.setText(R.id.likeNumView,item.likes.toString())
        if(item.createTime != null)
            holder.setText(R.id.timeView, TimeUtil.castLastDate(item.createTime.toLong()))
        var isLikeImageView = holder.getView<ImageView>(R.id.isLikeImageView)
        if(item.isLike == null){
            isLikeImageView.setImageResource(R.mipmap.like2)
        }else{
            isLikeImageView.setImageResource(R.mipmap.comment_like)
        }

        Glide.with(context).load(Constant.IMAGE_BASE+item.avatar)
                .into(holder.getView(R.id.headerView))

    }

}