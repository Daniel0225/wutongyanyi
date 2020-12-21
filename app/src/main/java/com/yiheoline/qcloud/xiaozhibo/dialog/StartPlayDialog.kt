package com.yiheoline.qcloud.xiaozhibo.dialog

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.view.Gravity
import android.widget.RelativeLayout
import androidx.core.content.ContextCompat.startActivity
import com.yiheoline.qcloud.xiaozhibo.anchor.prepare.TCAnchorPrepareActivity
import com.yiheoline.qcloud.xiaozhibo.homepage.ShowSettingActivity
import com.yiheoline.qcloud.xiaozhibo.homepage.ShowerPlayStartActivity
import com.yiheonline.qcloud.xiaozhibo.R
import org.jetbrains.anko.sdk27.coroutines.onClick

object StartPlayDialog {

    fun onCreateDialog(
        context: Context
    ) {
        var dialog = Dialog(context, R.style.NoBGDialog)
        dialog.setContentView(R.layout.start_play_layout)
        dialog.show()
        var mDialogWindow = dialog.window
        val lp = mDialogWindow!!.getAttributes()
        val windowManager = mDialogWindow.getWindowManager()
        val display = windowManager!!.getDefaultDisplay()
        lp.width = display.width
//        lp.height = display.height * 430 / 667
        mDialogWindow.setGravity(Gravity.BOTTOM)
        mDialogWindow.setAttributes(lp)
        mDialogWindow.setWindowAnimations(R.style.BottomAnimation)

        dialog.findViewById<RelativeLayout>(R.id.cancelBtn).onClick {
            dialog.dismiss()
        }

        dialog.findViewById<RelativeLayout>(R.id.showPlay).onClick {
//            context.startActivity(Intent(context,TCAnchorPrepareActivity::class.java))
            context.startActivity(Intent(context,ShowSettingActivity::class.java))
            dialog.dismiss()
        }
        dialog.findViewById<RelativeLayout>(R.id.showerPlayBtn).onClick {
            context.startActivity(Intent(context,ShowerPlayStartActivity::class.java))
            dialog.dismiss()
        }
    }
}