package com.yiheoline.qcloud.xiaozhibo.show.adapter

import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.yiheoline.qcloud.xiaozhibo.http.response.ShowListResponse
import com.yiheonline.qcloud.xiaozhibo.R

class ShowListAdapter(layoutId:Int, data:MutableList<ShowListResponse>): BaseQuickAdapter<ShowListResponse, BaseViewHolder>(layoutId,data){
    override fun convert(holder: BaseViewHolder, item: ShowListResponse) {
        holder.setText(R.id.showName,item.mixedPlayURL)
    }


}