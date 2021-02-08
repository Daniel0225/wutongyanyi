package com.yiheoline.qcloud.xiaozhibo

import androidx.multidex.MultiDex
import androidx.multidex.MultiDexApplication
import com.lzy.okgo.OkGo
import com.lzy.okgo.cache.CacheMode
import com.lzy.okgo.cookie.CookieJarImpl
import com.lzy.okgo.cookie.store.SPCookieStore
import com.lzy.okgo.https.HttpsUtils
import com.lzy.okgo.interceptor.HttpLoggingInterceptor
import com.tencent.bugly.crashreport.CrashReport
import com.tencent.bugly.crashreport.CrashReport.UserStrategy
import com.tencent.mmkv.MMKV
import com.tencent.qcloud.ugckit.UGCKit
import com.tencent.qcloud.ugckit.UGCKitConstants
import com.tencent.rtmp.TXLiveBase
import com.tencent.rtmp.TXLog
import com.tencent.ugc.TXUGCBase
import com.umeng.analytics.MobclickAgent
import com.umeng.commonsdk.UMConfigure
import com.yiheoline.liteav.demo.lvb.liveroom.MLVBLiveRoomImpl
import com.yiheoline.qcloud.xiaozhibo.http.response.LoginResponse
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
 *
 */
class TCApplication : MultiDexApplication() {
    override fun onCreate() {
        super.onCreate()

        application = this
        MultiDex.install(this)
        // 必须：初始化 LiteAVSDK Licence。 用于直播推流鉴权。
        TXLiveBase.getInstance().setLicence(this, TCGlobalConfig.LICENCE_URL, TCGlobalConfig.LICENCE_KEY)

        // 必须：初始化 MLVB 组件
        MLVBLiveRoomImpl.sharedInstance(this)

        // 必须：初始化全局的 用户信息管理类，记录个人信息。
        TCUserMgr.getInstance().initContext(applicationContext)

        MMKV.initialize(this)

        TXUGCBase.getInstance().setLicence(this,TCGlobalConfig.LICENCE_URL_VIDEO,TCGlobalConfig.LICENCE_KEY_VIDEO)
        UGCKit.init(this)
        initOkGo()

        UMConfigure.init(this,"6012a32ff1eb4f3f9b7a1dba","Umeng",UMConfigure.DEVICE_TYPE_PHONE, "")
        MobclickAgent.setPageCollectionMode(MobclickAgent.PageMode.AUTO);
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
        var isLogin = false
        var currentPlayId = 0
        var mlvbToken = ""
        var loginInfo : LoginResponse? = null
        var isRelease = false
    }
}