package com.yiheoline.qcloud.xiaozhibo.utils

import com.yiheoline.qcloud.xiaozhibo.TCApplication

object DpUtil {
    fun dp2px(dpValue:Int): Float {
        var scale = TCApplication.application!!.resources.displayMetrics.density
        return (dpValue * scale + 0.5f)
    }


}