package com.yiheoline.qcloud.xiaozhibo.dialog

import android.app.Dialog
import android.content.Context
import android.widget.TextView
import com.yiheonline.qcloud.xiaozhibo.R
import org.jetbrains.anko.sdk27.coroutines.onClick

/**
 * Created by Administrator on 2020/1/12.
 */
object ConfirmDialog {
    var updateSelectListener : ConfirmListener? = null

     fun onCreateDialog(context : Context?, sendMsg: String?,updateSelectListener : ConfirmListener?) {
         this.updateSelectListener = updateSelectListener
        var dialog = Dialog(context, R.style.customDialog)
        dialog.setContentView(R.layout.confirm_dialog_layout)
        var content = dialog.findViewById<TextView>(R.id.content)
        content.text = sendMsg
        dialog.findViewById<TextView>(R.id.checkBtn).onClick {
            updateSelectListener?.onConfirm(true)
            dialog.dismiss()
        }

        dialog.findViewById<TextView>(R.id.cancelBtn).onClick {
            updateSelectListener?.onConfirm(false)
            dialog.dismiss()
        }
         dialog.show()
    }

    interface ConfirmListener{
        fun onConfirm(isConfirm : Boolean)
    }
}