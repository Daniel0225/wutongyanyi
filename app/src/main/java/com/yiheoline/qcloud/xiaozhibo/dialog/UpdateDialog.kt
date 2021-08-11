package com.yiheoline.qcloud.xiaozhibo.dialog

import android.app.Dialog
import android.content.Context
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import com.yiheoline.qcloud.xiaozhibo.bean.VersionInfo
import com.yiheonline.qcloud.xiaozhibo.R
import org.jetbrains.anko.sdk27.coroutines.onClick

object UpdateDialog{
    var selectWorkerDialog : Dialog? = null
    var updateSelectListener : UpdateSelectListener? = null

    private fun onCreateDialog(context : Context, versionInfo: VersionInfo): Dialog {
        var dialog = Dialog(context, R.style.customDialog)
        dialog.setContentView(R.layout.update_dialog_layout)
        dialog.setCancelable(true)
        var content = dialog.findViewById<TextView>(R.id.update_content)
        content.text = versionInfo.versionDesc
        dialog.findViewById<TextView>(R.id.updateNow).onClick {
            updateSelectListener?.onUpdate(true)
        }

        dialog.findViewById<TextView>(R.id.updateCancel).onClick {
            updateSelectListener?.onUpdate(false)
            dialog.dismiss()
        }

        return dialog
    }

    fun showDialog(context : Context, versionInfo: VersionInfo, listener: UpdateSelectListener){
        selectWorkerDialog = onCreateDialog(context,versionInfo)
        updateSelectListener = listener
        selectWorkerDialog?.show()
    }

    interface UpdateSelectListener{
        fun onUpdate(isUpdate : Boolean)
    }

    fun setProgress(progress : Int){
        var progressBar = selectWorkerDialog?.findViewById<ProgressBar>(R.id.progressBar)
        if(progressBar?.visibility == View.GONE){
            progressBar?.visibility = View.VISIBLE
        }
        progressBar?.progress = progress
        if(progress == 100 ){
            selectWorkerDialog?.dismiss()
        }
    }

}