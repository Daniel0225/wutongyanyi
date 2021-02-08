package com.yiheoline.qcloud.xiaozhibo.dialog

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.view.Gravity
import android.widget.RelativeLayout
import androidx.core.content.ContextCompat.startActivity
import com.tencent.liteav.demo.videorecord.TCVideoRecordActivity
import com.tencent.qcloud.ugckit.UGCKitConstants
import com.tencent.rtmp.TXLiveConstants
import com.tencent.ugc.TXRecordCommon
import com.yiheoline.qcloud.xiaozhibo.homepage.ShowSettingActivity
import com.yiheoline.qcloud.xiaozhibo.homepage.ShowerPlayStartActivity
import com.yiheonline.qcloud.xiaozhibo.R
import org.jetbrains.anko.sdk27.coroutines.onClick

object StartPlayDialog {
    var chooseVideoListener:ChooseVideoListener? = null
    fun onCreateDialog(
        context: Context,chooseVideoListener: ChooseVideoListener
    ) {
        this.chooseVideoListener = chooseVideoListener
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
            context.startActivity(Intent(context,ShowSettingActivity::class.java))
            dialog.dismiss()
        }
        dialog.findViewById<RelativeLayout>(R.id.showerPlayBtn).onClick {
            context.startActivity(Intent(context,ShowerPlayStartActivity::class.java))
            dialog.dismiss()
        }
        dialog.findViewById<RelativeLayout>(R.id.shotBtn).onClick {
//            context.startActivity(Intent(context, TCVideoSettingActivity::class.java))
            var intent = Intent(context, TCVideoRecordActivity::class.java)
            intent.putExtra(UGCKitConstants.RECORD_CONFIG_MIN_DURATION,5 * 1000)
            intent.putExtra(UGCKitConstants.RECORD_CONFIG_MAX_DURATION,60 * 1000)
            intent.putExtra(UGCKitConstants.RECORD_CONFIG_ASPECT_RATIO, TXRecordCommon.VIDEO_ASPECT_RATIO_9_16)
            intent.putExtra(UGCKitConstants.RECORD_CONFIG_RECOMMEND_QUALITY,TXRecordCommon.VIDEO_QUALITY_MEDIUM)
            intent.putExtra(UGCKitConstants.RECORD_CONFIG_HOME_ORIENTATION, TXLiveConstants.VIDEO_ANGLE_HOME_DOWN)
            intent.putExtra(UGCKitConstants.RECORD_CONFIG_TOUCH_FOCUS,false)
            intent.putExtra(UGCKitConstants.RECORD_CONFIG_NEED_EDITER,true)
            startActivity(context,intent,null)
            dialog.dismiss()
        }
        dialog.findViewById<RelativeLayout>(R.id.upLoadBtn).onClick {
            chooseVideoListener.chooseVideo()
            dialog.dismiss()
        }
    }

    interface ChooseVideoListener{
        fun chooseVideo()
    }
}