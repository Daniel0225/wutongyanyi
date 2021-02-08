package com.yiheoline.qcloud.xiaozhibo.dialog;

import android.app.Dialog;
import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.tencent.qcloud.ugckit.utils.ToastUtil;
import com.yiheoline.qcloud.xiaozhibo.bean.CommentBean;
import com.yiheonline.qcloud.xiaozhibo.R;

public class InputDialog extends Dialog {

    private SendCommentListener sendCommentListener;
    private CommentBean commentBean;
    public InputDialog(@NonNull Context context, CommentBean commentBean, SendCommentListener sendCommentListener) {
        super(context);
        this.sendCommentListener = sendCommentListener;
        this.commentBean = commentBean;
        init(context);
    }

    public Context mContext;
    public View mRootView;

    public void init(Context context) {
        mContext = context;
        mRootView = LayoutInflater.from(context).inflate(R.layout.dialog_input, null);
        setContentView(mRootView);
        Window window = getWindow();
        WindowManager.LayoutParams params = window.getAttributes();
        params.width = WindowManager.LayoutParams.MATCH_PARENT;
        window.setAttributes(params);
        window.setGravity(Gravity.BOTTOM);
        EditText editText = mRootView.findViewById(R.id.et);
        //如果是回复 则显示回复xx hint
        if(commentBean != null){
            editText.setHint("回复@"+commentBean.getNickname());
        }
        TextView sendBtn = mRootView.findViewById(R.id.sendBtn);
        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String comment = editText.getText().toString();
                if(comment.isEmpty()){
                    ToastUtil.toastShortMessage("评论内容不能为空");
                }else{
                    sendCommentListener.onSend(comment,commentBean);
                    dismiss();
                }
            }
        });
        showSoftKeyboard(editText,context);
    }

    public interface SendCommentListener{
        void onSend(String comment,CommentBean commentBean);
    }

    public void showSoftKeyboard(View view, Context mContext) {
        if (view.requestFocus()) {
            InputMethodManager imm = (InputMethodManager)mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
        }
    }
}
