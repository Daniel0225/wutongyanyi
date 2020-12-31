package com.yiheoline.qcloud.xiaozhibo.dialog

import android.app.Dialog
import android.content.Context
import android.view.Gravity
import android.widget.EditText
import android.widget.RelativeLayout
import android.widget.Toast
import com.yiheonline.qcloud.xiaozhibo.R
import org.jetbrains.anko.sdk27.coroutines.onClick

object CommentDialog {
    var publishCommentListener : PublishCommentListener? = null
    fun onCreateDialog(
        context: Context,
        publishCommentListener : PublishCommentListener
    ) {
        this.publishCommentListener = publishCommentListener
        var dialog = Dialog(context, R.style.NoBGDialog)
        dialog.setContentView(R.layout.comment_dialog_layout)
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

        dialog.findViewById<RelativeLayout>(R.id.publishBtn).onClick {
            var inputView = dialog.findViewById<EditText>(R.id.commentInputView)
            var content = inputView.text.toString()
            if(content.isEmpty()){
                Toast.makeText(context, "评论不能为空", Toast.LENGTH_SHORT).show()
            }else{
                publishCommentListener.publish(content)
                dialog.dismiss()
            }
        }
    }

    interface PublishCommentListener {
        fun publish(commentContent:String)
    }
}