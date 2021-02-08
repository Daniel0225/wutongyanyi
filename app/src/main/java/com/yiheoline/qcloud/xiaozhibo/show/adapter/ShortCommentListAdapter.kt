package com.yiheoline.qcloud.xiaozhibo.show.adapter

import android.widget.ImageView
import android.widget.RelativeLayout
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
import com.yiheoline.qcloud.xiaozhibo.video.adapter.ReplyAdapter
import com.yiheonline.qcloud.xiaozhibo.R
import org.jetbrains.anko.sdk27.coroutines.onClick

class ShortCommentListAdapter(layoutId:Int, data:MutableList<CommentBean>):BaseQuickAdapter<CommentBean, BaseViewHolder>(layoutId,data){
    override fun convert(holder: BaseViewHolder, item: CommentBean) {
        holder.setText(R.id.nameView,item.nickname)
        holder.setText(R.id.contentView,item.content)
        holder.setText(R.id.likeNumView,item.likes.toString())
        holder.setText(R.id.timeView, TimeUtil.castLastDate(item.createTime.toLong()))
        var isLikeImageView = holder.getView<ImageView>(R.id.isLikeImageView)
        if(item.isLike == null){
            isLikeImageView.setImageResource(R.mipmap.video_unlike)
        }else{
            isLikeImageView.setImageResource(R.mipmap.video_like)
        }

        Glide.with(context).load(Constant.IMAGE_BASE+item.avatar)
                .into(holder.getView(R.id.headerView))

        var replyRv = holder.getView<RecyclerView>(R.id.replyRv)
        replyRv.layoutManager = LinearLayoutManager(context)
        var replyAdapter = ReplyAdapter(R.layout.short_reply_item_layout,item.replyList.list)
        replyRv.adapter = replyAdapter
    }
}