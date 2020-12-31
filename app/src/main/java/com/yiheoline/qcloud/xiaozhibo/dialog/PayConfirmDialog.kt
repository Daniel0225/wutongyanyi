package com.yiheoline.qcloud.xiaozhibo.dialog

import android.app.Dialog
import android.content.Context
import android.widget.CheckBox
import android.widget.TextView
import com.yiheonline.qcloud.xiaozhibo.R
import kotlinx.android.synthetic.main.activity_confirm_order.*
import org.jetbrains.anko.sdk27.coroutines.onClick

/**
 * Created by Administrator on 2020/1/12.
 */
object PayConfirmDialog {
    var updateSelectListener: PayConfirmListener? = null
    var currentDialog : Dialog? = null
    fun onCreateDialog(
        context: Context?,
        price:Double,
        sendMsg: String?,
        updateSelectListener: PayConfirmListener?
    ) {
        this.updateSelectListener = updateSelectListener
        var dialog = Dialog(context, R.style.cus_dialog)
        currentDialog = dialog
        dialog.setContentView(R.layout.pay_confirm_dialog_layout)
        dialog.setCanceledOnTouchOutside(false)
        dialog.setOnKeyListener { _, i, keyEvent ->
            true
        }
        var videoTitleView = dialog.findViewById<TextView>(R.id.videoTitleView)
        videoTitleView.text = sendMsg
        var priceView = dialog.findViewById<TextView>(R.id.priceView)
        priceView.text = "${price}元"

        var wxPay = dialog.findViewById<CheckBox>(R.id.weixinPay)
        var aliPay = dialog.findViewById<CheckBox>(R.id.aliPay)
        wxPay.onClick {
            aliPay.isChecked = false
            wxPay.isChecked = true
        }
        aliPay.onClick {
            aliPay.isChecked = true
            wxPay.isChecked = false
        }
        dialog.findViewById<TextView>(R.id.confirmPayBtn).onClick {
            updateSelectListener?.isNeedPay(true,dialog)
//            dialog.dismiss()
        }

        dialog.findViewById<TextView>(R.id.closeBtn).onClick {
            updateSelectListener?.isNeedPay(false,dialog)
            dialog.dismiss()
        }
        dialog.show()
    }

    public fun hide(){
        currentDialog?.dismiss()
    }

    interface PayConfirmListener {
        fun isNeedPay(isUpdate: Boolean,dialog: Dialog)
    }
}