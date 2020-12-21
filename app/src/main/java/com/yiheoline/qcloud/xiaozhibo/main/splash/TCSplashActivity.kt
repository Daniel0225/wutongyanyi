package com.yiheoline.qcloud.xiaozhibo.main.splash

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.TextUtils
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import com.lzy.okgo.OkGo
import com.lzy.okgo.model.HttpHeaders
import com.tencent.mmkv.MMKV
import com.yiheoline.liteav.demo.lvb.liveroom.MLVBLiveRoomImpl
import com.yiheoline.liteav.demo.lvb.liveroom.roomutil.commondef.LoginInfo
import com.yiheoline.qcloud.xiaozhibo.TCApplication
import com.yiheoline.qcloud.xiaozhibo.common.report.TCELKReportMgr
import com.yiheoline.qcloud.xiaozhibo.common.utils.TCConstants
import com.yiheoline.qcloud.xiaozhibo.login.LoginActivity
import com.yiheoline.qcloud.xiaozhibo.login.TCUserMgr
import com.yiheoline.qcloud.xiaozhibo.main.TCMainActivity
import com.yiheoline.qcloud.xiaozhibo.utils.FastJsonUtil

/**
 * Module:   TCSplashActivity
 *
 * Function: 闪屏页面，只是显示一张图
 *
 * Note：需要注意配置小直播后台的 server 地址；配置教程，详见：https://cloud.tencent.com/document/product/454/15187
 */
class TCSplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!isTaskRoot
                && intent.hasCategory(Intent.CATEGORY_LAUNCHER)
                && intent.action != null && intent.action == Intent.ACTION_MAIN) {
            finish()
            return
        }
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
        val isFirstRun = isFirstRun(this)
        if (isFirstRun) {
            saveFirstRun(this)
            TCELKReportMgr.getInstance().reportELK(TCConstants.ELK_ACTION_INSTALL, TCUserMgr.getInstance().userId, 0, "首次安装成功", null)
        }
        val handler = Handler(Looper.getMainLooper())
        handler.postDelayed({ jumpToLoginActivity() }, 1000)
    }

    /**
     * 判定是否第一次运行
     *
     * @param context
     * @return
     */
    fun isFirstRun(context: Context): Boolean {
        val sharedPreferences = context.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE)
        return sharedPreferences.getBoolean(KEY_FIRST_RUN, true)
    }

    /**
     * 本地保存 sharepreferences 变量，表明已经运行过。
     * @param context
     */
    private fun saveFirstRun(context: Context) {
        val sharedPreferences = context.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE)
        sharedPreferences.edit().putBoolean(KEY_FIRST_RUN, false).commit()
    }

    /**
     * 跳转到登录界面
     */
    private fun jumpToLoginActivity() {
        var token = MMKV.defaultMMKV().decodeString("token")
        var mlvbInfo = MMKV.defaultMMKV().decodeString("MLVB")
        if(TextUtils.isEmpty(token)||TextUtils.isEmpty(mlvbInfo)){
            //未登录
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
        }else{
            //已登录
            var token = MMKV.defaultMMKV().decodeString("token")
            if(!token.isNullOrEmpty()){
                TCApplication.isLogin = true
                val headers = HttpHeaders()
                headers.put("token", token)
                OkGo.getInstance().addCommonHeaders(headers)
            }
            var loginInfo = FastJsonUtil.getObject(mlvbInfo,LoginInfo::class.java)
            var mlvbLiveRoomImpl = MLVBLiveRoomImpl.sharedInstance(this)
            mlvbLiveRoomImpl.initMlvb(loginInfo)
            val intent = Intent(this, TCMainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
        }
        finish()
    }

    companion object {
        private const val SP_NAME = "xiaozhibo_info"
        private const val KEY_FIRST_RUN = "is_first_run"
    }
}