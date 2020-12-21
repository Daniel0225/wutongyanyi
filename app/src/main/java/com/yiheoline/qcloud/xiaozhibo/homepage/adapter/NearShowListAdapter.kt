package com.yiheoline.qcloud.xiaozhibo.homepage.adapter

import com.bumptech.glide.Glide
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.yiheoline.qcloud.xiaozhibo.bean.ShowNoticeBean

class NearShowListAdapter(layoutId:Int, data:MutableList<ShowNoticeBean>):BaseQuickAdapter<ShowNoticeBean, BaseViewHolder>(layoutId,data){
    override fun convert(holder: BaseViewHolder, item: ShowNoticeBean) {

    }

}