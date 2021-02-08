package com.yiheoline.qcloud.xiaozhibo.dialog

import android.app.Dialog
import android.content.Context
import android.view.Gravity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.yiheoline.qcloud.xiaozhibo.bean.GiftBean
import com.yiheoline.qcloud.xiaozhibo.homepage.adapter.GiftListAdapter
import com.yiheonline.qcloud.xiaozhibo.R

object GiftDialog {
    var selectGiftListener : SelectGiftListener? = null
    fun onCreateDialog(
        context: Context,
        list:MutableList<GiftBean>,
        selectGiftListener : SelectGiftListener
    ) {
        this.selectGiftListener = selectGiftListener
        var dialog = Dialog(context, R.style.NoBGDialog)
        dialog.setContentView(R.layout.gift_dialog_layout)
        dialog.show()
        var mDialogWindow = dialog.window
        val lp = mDialogWindow!!.getAttributes()
        val windowManager = mDialogWindow.getWindowManager()
        val display = windowManager!!.getDefaultDisplay()
        lp.width = display.width
        mDialogWindow.setGravity(Gravity.BOTTOM)
        mDialogWindow.setAttributes(lp)
        mDialogWindow.setWindowAnimations(R.style.BottomAnimation)

        var layoutParam = GridLayoutManager(context,2)
        layoutParam.orientation = GridLayoutManager.HORIZONTAL
        var recyclerView = dialog.findViewById<RecyclerView>(R.id.recyclerView)
        recyclerView.layoutManager = layoutParam
        var giftAdapter = GiftListAdapter(R.layout.gift_layout,list)
        recyclerView.adapter = giftAdapter
        giftAdapter.setOnItemClickListener { _, _, position ->
            selectGiftListener.select(position)
            dialog.dismiss()
        }
    }

    interface SelectGiftListener{
        fun select(position:Int)
    }
}