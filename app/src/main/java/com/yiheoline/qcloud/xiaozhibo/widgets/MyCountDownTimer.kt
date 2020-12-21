package com.yiheoline.qcloud.xiaozhibo.widgets

import android.os.CountDownTimer
import android.widget.TextView

class MyCountDownTimer(timeButton: TextView, millisInFuture:Long, countDownInterval:Long) : CountDownTimer(millisInFuture,countDownInterval) {
    var button = timeButton

    override fun onFinish() {
        button.isClickable = true
        button.text = "发送验证码"
    }

    override fun onTick(millisUntilFinished: Long) {
        button.isClickable = false
        button.text = "${millisUntilFinished/1000}S"
    }

}