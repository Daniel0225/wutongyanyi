package com.yiheoline.qcloud.xiaozhibo.homepage.adapter

import android.widget.ImageView
import com.bumptech.glide.Glide
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.yiheoline.qcloud.xiaozhibo.Constant
import com.yiheonline.qcloud.xiaozhibo.R
import kotlinx.android.synthetic.main.activity_apply_show.*

class AddPicListAdapter(layoutId:Int, data:MutableList<String>): BaseQuickAdapter<String, BaseViewHolder>(layoutId,data){
    override fun convert(holder: BaseViewHolder, item: String) {
        if(item != "empty"){
            holder.setVisible(R.id.addBtn,false)
            holder.setVisible(R.id.deleteView,true)
            holder.setVisible(R.id.selectImageContain,true)
//            holder.addOnClickListener(R.id.deleteView)
            var imageView = holder.getView<ImageView>(R.id.selectImage)
            Glide.with(context)
                    .load("${Constant.IMAGE_BASE}/$item")
                    .into(imageView)
        }else{
            holder.setVisible(R.id.addBtn,true)
            holder.setVisible(R.id.deleteView,false)
            holder.setVisible(R.id.selectImageContain,false)
        }
    }


}