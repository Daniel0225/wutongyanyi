package com.yiheoline.qcloud.xiaozhibo.homepage.adapter

import android.widget.RelativeLayout
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.yiheoline.qcloud.xiaozhibo.bean.ShowNoticeBean
import com.yiheoline.qcloud.xiaozhibo.utils.GlideRoundTransform
import com.yiheonline.qcloud.xiaozhibo.R

class PreListAdapter(layoutId:Int, data:MutableList<ShowNoticeBean>): BaseQuickAdapter<ShowNoticeBean, BaseViewHolder>(layoutId,data){
    var selectPosition = 0
    override fun convert(holder: BaseViewHolder, item: ShowNoticeBean) {
        Glide.with(context).load(item.imageDetail).transform(CenterCrop(context),GlideRoundTransform(context,5)).into(holder.getView(R.id.image))
        holder.setText(R.id.priceView,item.price.toString())
        holder.setText(R.id.titleView,item.title)
        holder.setText(R.id.dateView,item.liveTime)
        var imageView = holder.getView<RelativeLayout>(R.id.coverContain)
        if(getItemPosition(item) == selectPosition){
            imageView.setBackgroundResource(R.mipmap.pre1)
            holder.setVisible(R.id.cover,false)
        }else{
            imageView.setBackgroundResource(R.mipmap.pre2)
            holder.setVisible(R.id.cover,true)
        }
    }


}