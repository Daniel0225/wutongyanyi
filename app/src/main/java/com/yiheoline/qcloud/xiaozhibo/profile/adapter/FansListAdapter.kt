package com.yiheoline.qcloud.xiaozhibo.profile.adapter

import android.widget.ImageView
import com.bumptech.glide.Glide
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.module.DraggableModule
import com.chad.library.adapter.base.module.LoadMoreModule
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.yiheoline.qcloud.xiaozhibo.Constant
import com.yiheoline.qcloud.xiaozhibo.bean.FansBean
import com.yiheoline.qcloud.xiaozhibo.bean.SeeRecordBean
import com.yiheoline.qcloud.xiaozhibo.bean.UpRecordBean
import com.yiheonline.qcloud.xiaozhibo.R
import kotlinx.android.synthetic.main.up_record_item_layout.view.*

class FansListAdapter(layoutId:Int, data:MutableList<FansBean>): BaseQuickAdapter<FansBean, BaseViewHolder>(layoutId,data), LoadMoreModule {
    override fun convert(holder: BaseViewHolder, item: FansBean) {
        Glide.with(context).load(Constant.IMAGE_BASE+item.avatar).into(holder.getView(R.id.headerView))
        holder.setText(R.id.nameView,item.nickname)
        if(item.isFollow == null){
            holder.getView<ImageView>(R.id.likeBtn).setImageResource(R.mipmap.weiguanzhu)
        }else{
            holder.getView<ImageView>(R.id.likeBtn).setImageResource(R.mipmap.yiguanzhu)
        }
    }


}