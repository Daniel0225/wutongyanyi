package com.yiheoline.qcloud.xiaozhibo.homepage.adapter

import android.graphics.Color
import android.widget.TextView
import com.bumptech.glide.Glide
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.yiheoline.qcloud.xiaozhibo.utils.DpUtil
import com.yiheonline.qcloud.xiaozhibo.R
import org.jetbrains.anko.textColor

class TagListAdapter(layoutId:Int, data:MutableList<String>): BaseQuickAdapter<String, BaseViewHolder>(layoutId,data){
    override fun convert(holder: BaseViewHolder, item: String) {
        holder.setText(R.id.tagTextView,item)
    }


}