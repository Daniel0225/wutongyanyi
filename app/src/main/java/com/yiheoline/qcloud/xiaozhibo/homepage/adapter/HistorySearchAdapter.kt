package com.yiheoline.qcloud.xiaozhibo.homepage.adapter

import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.yiheonline.qcloud.xiaozhibo.R

class HistorySearchAdapter(layoutId:Int, data:MutableList<String>): BaseQuickAdapter<String, BaseViewHolder>(layoutId,data){
    override fun convert(holder: BaseViewHolder, item: String) {
        holder.setText(R.id.searchHistoryWord,item)
    }

}