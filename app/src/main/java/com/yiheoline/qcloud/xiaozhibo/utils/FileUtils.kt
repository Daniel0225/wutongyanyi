package com.yiheoline.qcloud.xiaozhibo.utils

import android.content.Context

object FileUtils {
    fun getCachePath(context: Context):String{
        return "${context.externalCacheDir}/"
    }
}