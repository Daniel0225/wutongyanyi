package com.yiheoline.qcloud.xiaozhibo.show.adapter

import com.bumptech.glide.Glide
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.module.LoadMoreModule
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.yiheoline.qcloud.xiaozhibo.Constant
import com.yiheoline.qcloud.xiaozhibo.bean.ShortVideoBean
import com.yiheonline.qcloud.xiaozhibo.R

class ShowListAdapter(layoutId:Int, data:MutableList<ShortVideoBean>): BaseQuickAdapter<ShortVideoBean, BaseViewHolder>(layoutId,data),LoadMoreModule{
    override fun convert(holder: BaseViewHolder, item: ShortVideoBean) {
        Glide.with(context).load(item.cover).into(holder.getView(R.id.imageView))
        Glide.with(context).load(Constant.IMAGE_BASE+item.avatar).into(holder.getView(R.id.headerView))
        holder.setText(R.id.titleView,item.title)
        holder.setText(R.id.nameView,item.nickname)
//        holder.setText(R.id.likeNumView,item.likes.toString())
        if(item.isLike == null){
            holder.setImageResource(R.id.likeImageView,R.mipmap.xin)
        }else{
            holder.setImageResource(R.id.likeImageView,R.mipmap.xin2)
        }
    }


}