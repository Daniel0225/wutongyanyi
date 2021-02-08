package com.yiheoline.qcloud.xiaozhibo.main

import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.text.TextUtils
import com.lzy.okgo.OkGo
import com.lzy.okgo.model.HttpHeaders
import com.lzy.okgo.model.HttpParams
import com.lzy.okgo.model.Response
import com.tencent.mmkv.MMKV
import com.yiheoline.liteav.demo.lvb.liveroom.MLVBLiveRoomImpl
import com.yiheoline.liteav.demo.lvb.liveroom.roomutil.commondef.LoginInfo
import com.yiheoline.qcloud.xiaozhibo.Constant
import com.yiheoline.qcloud.xiaozhibo.TCApplication
import com.yiheoline.qcloud.xiaozhibo.base.BaseActivity
import com.yiheoline.qcloud.xiaozhibo.http.BaseResponse
import com.yiheoline.qcloud.xiaozhibo.http.JsonCallBack
import com.yiheoline.qcloud.xiaozhibo.http.response.APPidResponse
import com.yiheoline.qcloud.xiaozhibo.http.response.LoginResponse
import com.yiheoline.qcloud.xiaozhibo.login.OneKeyLoginActivity
import com.yiheoline.qcloud.xiaozhibo.utils.FastJsonUtil
import com.yiheonline.qcloud.xiaozhibo.R
import kotlinx.android.synthetic.main.activity_splash.*
import org.jetbrains.anko.sdk27.coroutines.onClick

class SplashActivity : BaseActivity() {
    override fun getLayoutId(): Int {
        return R.layout.activity_splash
    }

    override fun getFitSystemWindows(): Boolean {
        return false
    }

    override fun initView() {
        super.initView()
        if (!isTaskRoot
                && intent.hasCategory(Intent.CATEGORY_LAUNCHER)
                && intent.action != null && intent.action == Intent.ACTION_MAIN) {
            finish()
            return
        }

        var handler = Handler(Looper.getMainLooper())
        handler.postDelayed({ jumpToLoginActivity() }, 3000)

        jumpBtn?.onClick {
            handler.removeCallbacksAndMessages(null)
            jumpToLoginActivity()
        }
    }

    /**
     * 跳转到登录界面
     */
    private fun jumpToLoginActivity() {
        var token = MMKV.defaultMMKV().decodeString("token")
        if(TextUtils.isEmpty(token)){
            //未登录
            val intent = Intent(this, OneKeyLoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
            finish()
        }else{
            //已登录
            var token = MMKV.defaultMMKV().decodeString("token")
            var loginInfoString = MMKV.defaultMMKV().decodeString("loginInfo")
            var loginResponse = FastJsonUtil.getObject(loginInfoString, LoginResponse::class.java)
            TCApplication.loginInfo = loginResponse
            if(!token.isNullOrEmpty()){
                TCApplication.isLogin = true
                val headers = HttpHeaders()
                headers.put("token", token)
                OkGo.getInstance().addCommonHeaders(headers)
            }
            getAppId(loginResponse)
        }
    }

    /**
     * 获取初始化直播间东西
     */
    private fun getAppId(loginResponse:LoginResponse){
        var params = HttpParams()
        params.put("userID",loginResponse.userId)
        OkGo.get<BaseResponse<APPidResponse>>(Constant.SDK_INFO)
                .params(params)
                .execute(object : JsonCallBack<BaseResponse<APPidResponse>>(){
                    override fun onSuccess(response: Response<BaseResponse<APPidResponse>>?) {
                        if(response?.body()?.res == 0){
                            loginMLVB(response.body().data!!,loginResponse)
                        }else{
                            //未登录
                            val intent = Intent(this@SplashActivity, OneKeyLoginActivity::class.java)
                            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                            startActivity(intent)
                            finish()
                        }
                    }

                })
    }

    /**
     * 初始化 MLVB 组件
     */
    private fun loginMLVB(apPidResponse: APPidResponse,loginResponse:LoginResponse) {
        val loginInfo = LoginInfo()
        loginInfo.sdkAppID = apPidResponse.sdkAppID.toLong()
        loginInfo.userID = loginResponse.userId
        loginInfo.userSig = apPidResponse.userSig
        val userName: String = loginResponse.nickname
        loginInfo.userName = if (!TextUtils.isEmpty(userName)) userName else loginResponse.userId
        loginInfo.userAvatar = Constant.IMAGE_BASE+loginResponse.avatar
        var mlvbLiveRoomImpl = MLVBLiveRoomImpl.sharedInstance(this)
        mlvbLiveRoomImpl.initMlvb(loginInfo)

        val intent = Intent(this@SplashActivity, TCMainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        startActivity(intent)
        finish()
    }
}