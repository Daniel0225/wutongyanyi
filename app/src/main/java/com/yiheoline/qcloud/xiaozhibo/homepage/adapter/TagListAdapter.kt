package com.yiheoline.qcloud.xiaozhibo.homepage.adapter

import android.graphics.Color
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.yiheoline.qcloud.xiaozhibo.bean.TagBean
import com.yiheonline.qcloud.xiaozhibo.R

class TagListAdapter(layoutId:Int, data:MutableList<TagBean>): BaseQuickAdapter<TagBean, BaseViewHolder>(layoutId,data){
    override fun convert(holder: BaseViewHolder, item: TagBean) {
        holder.setText(R.id.tagTextView,item.name)
        if(item.isChecked){
            holder.setBackgroundColor(R.id.tagTextView,Color.parseColor("#FD8318"))
            holder.setTextColor(R.id.tagTextView,Color.parseColor("#FFFFFF"))
        }else{
            holder.setBackgroundColor(R.id.tagTextView,Color.parseColor("#FFF2E7"))
            holder.setTextColor(R.id.tagTextView,Color.parseColor("#333333"))
        }

    }


}