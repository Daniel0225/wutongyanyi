package com.yiheoline.qcloud.xiaozhibo.homepage.adapter

import android.graphics.Color
import android.widget.TextView
import com.bumptech.glide.Glide
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.yiheoline.qcloud.xiaozhibo.bean.TypeBean
import com.yiheoline.qcloud.xiaozhibo.utils.DpUtil
import com.yiheonline.qcloud.xiaozhibo.R
import org.jetbrains.anko.textColor

class CatTabAdapter(layoutId:Int, data:MutableList<TypeBean>): BaseQuickAdapter<TypeBean, BaseViewHolder>(layoutId,data){
    var selectPosition = 0
    override fun convert(holder: BaseViewHolder, item: TypeBean) {
        var nameView = holder.getView<TextView>(R.id.tabName)
        nameView.text = item.name
        if(getItemPosition(item) == selectPosition){
            nameView.textSize = 18f
            nameView.textColor = Color.parseColor("#333333")
            holder.setVisible(R.id.tabLine,true)
        }else{
            nameView.textSize = 14f
            nameView.textColor = Color.parseColor("#999999")
            holder.setVisible(R.id.tabLine,false)
        }
    }


}