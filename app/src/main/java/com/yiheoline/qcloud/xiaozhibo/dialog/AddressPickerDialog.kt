package com.yiheoline.qcloud.xiaozhibo.dialog

import android.app.Dialog
import android.content.Context
import android.view.Gravity
import com.yiheoline.qcloud.xiaozhibo.widgets.addressPicker.AddressPickerView
import com.yiheonline.qcloud.xiaozhibo.R

object AddressPickerDialog {
    var onAddressPickListener:OnAddressPickListener?=null

    fun onCreateDialog(context : Context,onAddressPickListener:OnAddressPickListener?) {
        this.onAddressPickListener = onAddressPickListener
        var dialog = Dialog(context, R.style.NoBGDialog)
        dialog.setContentView(R.layout.pop_address_picker)
        initViews(dialog)
        dialog.show()
        var mDialogWindow = dialog.window
        val lp = mDialogWindow!!.getAttributes()
        val windowManager = mDialogWindow.getWindowManager()
        val display = windowManager!!.getDefaultDisplay()
        lp.width = display.width
//        lp.height = display.height * 430 / 667
        mDialogWindow.setGravity(Gravity.BOTTOM)
        mDialogWindow.setAttributes(lp)
        mDialogWindow.setWindowAnimations(R.style.BottomAnimation);
    }

    private fun initViews(dialog:Dialog){

        val addressView = dialog.findViewById<AddressPickerView>(R.id.apvAddress)
        addressView.setOnAddressPickerSure { address, provinceCode, cityCode, areaCode ->
            onAddressPickListener?.select(address,provinceCode,cityCode,areaCode)
            dialog.dismiss()
        }
    }

    interface OnAddressPickListener{
        fun select(address:String,provinceCode:String,cityCode:String,areaCode:String)
    }
}