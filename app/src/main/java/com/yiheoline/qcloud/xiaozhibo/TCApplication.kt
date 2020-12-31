package com.yiheoline.qcloud.xiaozhibo

import androidx.multidex.MultiDexApplication
import com.lzy.okgo.OkGo
import com.lzy.okgo.cache.CacheMode
import com.lzy.okgo.cookie.CookieJarImpl
import com.lzy.okgo.cookie.store.SPCookieStore
import com.lzy.okgo.https.HttpsUtils
import com.lzy.okgo.interceptor.HttpLoggingInterceptor
import com.lzy.okgo.model.HttpHeaders
import com.tencent.mmkv.MMKV
import com.tencent.rtmp.TXLiveBase
import com.yiheoline.liteav.demo.lvb.liveroom.MLVBLiveRoomImpl
import com.yiheoline.qcloud.xiaozhibo.common.report.TCELKReportMgr
import com.yiheoline.qcloud.xiaozhibo.common.utils.TCConstants
import com.yiheoline.qcloud.xiaozhibo.login.TCUserMgr
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit
import java.util.logging.Level

/**
 * Module:   TCApplication
 *
 * Function: 初始化 App 所需要的组件
 *
 * 1. 【重要】初始化直播需要的 Licence : [TXLiveBase.setLicence]
 *
 * 2. 初始化 App 用户逻辑管理类。
 *
 * 3. 初始化 bugly 组件上报 crash。
 *
 * 4. 初始化友盟分享组件，分享内容到 QQ 或 微信。
 *
 * 5. 初始化小直播ELK上报数据系统，此系统用于 Demo 收集使用数据；您可以不关注相关代码。
 */
class TCApplication : MultiDexApplication() {
    override fun onCreate() {
        super.onCreate()

        application = this

        // 必须：初始化 LiteAVSDK Licence。 用于直播推流鉴权。
        TXLiveBase.getInstance().setLicence(this, TCGlobalConfig.LICENCE_URL, TCGlobalConfig.LICENCE_KEY)

        // 必须：初始化 MLVB 组件
        MLVBLiveRoomImpl.sharedInstance(this)

        // 必须：初始化全局的 用户信息管理类，记录个人信息。
        TCUserMgr.getInstance().initContext(applicationContext)

        // 可选：初始化小直播上报组件
        initXZBAppELKReport()

        MMKV.initialize(this)

        initOkGo()
    }

    /**
     *
     * 初始化 ELK 数据上报：仅仅适用于数据收集上报，您可以不关注；或者将相关代码删除。
     */
    private fun initXZBAppELKReport() {
        TCELKReportMgr.getInstance().init(this)
        TCELKReportMgr.getInstance().registerActivityCallback(this)
        TCELKReportMgr.getInstance().reportELK(TCConstants.ELK_ACTION_START_UP, TCUserMgr.getInstance().userId, 0, "启动成功", null)
    }

    private fun initOkGo(){
        var builder = OkHttpClient.Builder()
        builder.cookieJar(CookieJarImpl(SPCookieStore(application)))
        builder.connectTimeout(10000, TimeUnit.MILLISECONDS)
        builder.sslSocketFactory(HttpsUtils.getSslSocketFactory().sSLSocketFactory)
        var loggingInterceptor = HttpLoggingInterceptor("OkGo")
        loggingInterceptor.setPrintLevel(HttpLoggingInterceptor.Level.BODY)
        loggingInterceptor.setColorLevel(Level.INFO)
        builder.addInterceptor(loggingInterceptor)
        var okGo = OkGo.getInstance().init(application)
        okGo.okHttpClient = builder.build()
        okGo.cacheMode = CacheMode.NO_CACHE
        okGo.retryCount = 0
    }

    companion object {
        /**
         * bugly 组件的 AppId
         *
         * bugly sdk 系腾讯提供用于 APP Crash 收集和分析的组件。
         */
        var application: TCApplication? = null
        var userId = ""
        var token = ""
        var isLogin = false
        var currentPlayId = 0
        var mlvbToken = ""
    }
}