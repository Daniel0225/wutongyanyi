package com.yiheoline.qcloud.xiaozhibo.login

import android.content.Intent
import android.graphics.Color
import android.text.TextUtils
import android.util.Log
import android.widget.ImageView
import android.widget.Toast
import com.dyhdyh.widget.loadingbar2.LoadingBar
import com.lzy.okgo.OkGo
import com.lzy.okgo.model.HttpHeaders
import com.lzy.okgo.model.HttpParams
import com.lzy.okgo.model.Response
import com.mobile.auth.gatewayauth.AuthUIConfig
import com.mobile.auth.gatewayauth.PhoneNumberAuthHelper
import com.mobile.auth.gatewayauth.ResultCode
import com.mobile.auth.gatewayauth.TokenResultListener
import com.mobile.auth.gatewayauth.model.TokenRet
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
import com.yiheoline.qcloud.xiaozhibo.login.onekey.BaseUIConfig
import com.yiheoline.qcloud.xiaozhibo.main.TCMainActivity
import com.yiheoline.qcloud.xiaozhibo.utils.FastJsonUtil
import com.yiheonline.qcloud.xiaozhibo.R
import kotlinx.android.synthetic.main.activity_one_key_login.*
import org.jetbrains.anko.sdk27.coroutines.onClick
import org.jetbrains.anko.startActivity
import org.jetbrains.anko.toast

class OneKeyLoginActivity : BaseActivity() {

    private var mAuthHelper : PhoneNumberAuthHelper? = null
    private var mTokenResultListener: TokenResultListener? = null
    private var mUIConfig: BaseUIConfig? = null
    override fun getLayoutId(): Int {
        return R.layout.activity_one_key_login
    }

    override fun initView() {
        super.initView()
        mTokenResultListener = object : TokenResultListener {
            override fun onTokenFailed(ret: String?) {
                Log.e("xxxxxx", "onTokenSuccess:$ret")
                LoadingBar.dialog(this@OneKeyLoginActivity).cancel()
                var tokenRet: TokenRet? = null
                try {
                    tokenRet = TokenRet.fromJson(ret)
                    if (ResultCode.CODE_ERROR_USER_CANCEL == tokenRet.code) {
                        //模拟的是必须登录 否则直接退出app的场景
                        finish()
                    } else {
                        toAnotherLogin()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            override fun onTokenSuccess(ret: String?) {
                LoadingBar.dialog(this@OneKeyLoginActivity).cancel()
                Log.e("xxxxxx", "onTokenFailed:$ret")
                var tokenRet: TokenRet? = null
                try {
                    tokenRet = TokenRet.fromJson(ret)
                    if (ResultCode.CODE_SUCCESS == tokenRet?.code) {
                        onKeyLogin(tokenRet!!.token)
                        mAuthHelper?.setAuthListener(null)
                    }
                } catch (e: java.lang.Exception) {
                    e.printStackTrace()
                }

//                when (tokenRet!!.code) {
//                    "600005" -> {
//                        toast("手机终端不安全,请通过其他方式登录")
//                    }
//                    "600007" -> {
//                        toast("未检测到sim卡")
//                    }
//                    "600008" -> {
//                        toast("数据流量未开启")
//                    }
//                    "600011" -> {
//                        toast("获取token失败,,请通过其他方式登录")
//                    }
//                    "600002" -> {
//                        toast("唤起授权页失败,请通过其他方式登录")
//                    }
//                    "600015" -> {
//                        toast("接口超时,请通过其他方式登录")
//                    }
//                    "600021" -> {
//                        toast("运营商已切换,请通过其他方式登录")
//                    }
//                    else -> {
//                        toast("接口超时,请通过其他方式登录" + tokenRet!!.code)
//                    }
//                }
            }

        }

        mAuthHelper = PhoneNumberAuthHelper.getInstance(this,mTokenResultListener)

        mAuthHelper?.setAuthSDKInfo(getString(R.string.authSecret))

        mUIConfig = BaseUIConfig.init(this, mAuthHelper)

        oneKeyLogin()

    }

    /**
     * 一键登录失败跳转倒验证码登陆
     */
    private fun toAnotherLogin(){
        Toast.makeText(applicationContext, "一键登录失败切换到其他登录方式", Toast.LENGTH_SHORT).show()
        val pIntent = Intent(this@OneKeyLoginActivity, LoginActivity::class.java)
        startActivityForResult(pIntent, 1002)
        finish()
    }

    /**
     * 进入app就需要登录的场景使用
     */
    private fun oneKeyLogin() {
        mAuthHelper = PhoneNumberAuthHelper.getInstance(applicationContext, mTokenResultListener)
        mUIConfig?.configAuthPage()
        mAuthHelper?.getLoginToken(this, 5000)
        LoadingBar.dialog(this).show()
    }



    /**
     * 一键登录
     */
    private fun onKeyLogin(token:String){
        var params = HttpParams()
        params.put("accessToken",token)
        params.put("platform",1)
        OkGo.post<BaseResponse<LoginResponse>>(Constant.ONE_CLICK_LOGIN)
                .params(params)
                .execute(object:JsonCallBack<BaseResponse<LoginResponse>>(){
                    override fun onFinish() {
                        super.onFinish()
                        mAuthHelper?.quitLoginPage()
                    }

                    override fun onError(response: Response<BaseResponse<LoginResponse>>?) {
                        super.onError(response)
                        toAnotherLogin()
                    }
                    override fun onSuccess(response: Response<BaseResponse<LoginResponse>>?) {
                        if(response?.body()?.res == 0){
                            toast("登陆成功")
                            var loginInfo = response.body().data
                            TCApplication.isLogin = true
                            TCApplication.loginInfo = loginInfo
                            MMKV.defaultMMKV().encode("token",loginInfo.token)
                            MMKV.defaultMMKV().encode("loginInfo", FastJsonUtil.createJsonString(loginInfo))
                            val headers = HttpHeaders()
                            headers.put("token", loginInfo.token)
                            OkGo.getInstance().addCommonHeaders(headers)
                            getAppId(response.body().data!!)
                            startActivity<TCMainActivity>()
                            finish()
                        }else{
                            toast(response?.body()?.msg.toString())
                            toAnotherLogin()
                        }
                    }

                })
    }

    /**
     * 获取初始化直播间东西
     */
    private fun getAppId(loginResponse: LoginResponse){
        var params = HttpParams()
        params.put("userID",loginResponse.userId)
        OkGo.get<BaseResponse<APPidResponse>>(Constant.SDK_INFO)
                .params(params)
                .execute(object : JsonCallBack<BaseResponse<APPidResponse>>(){
                    override fun onSuccess(response: Response<BaseResponse<APPidResponse>>?) {
                        if(response?.body()?.res == 0){
                            loginMLVB(response.body().data!!,loginResponse)
                        }
                    }

                })
    }

    /**
     * 初始化 MLVB 组件
     */
    private fun loginMLVB(apPidResponse: APPidResponse,loginResponse: LoginResponse) {
        if (mContext == null) return
        val loginInfo = LoginInfo()
        loginInfo.sdkAppID = apPidResponse.sdkAppID.toLong()
        loginInfo.userID = loginResponse.userId
        loginInfo.userSig = apPidResponse.userSig
        val userName: String = loginResponse.nickname
        loginInfo.userName = if (!TextUtils.isEmpty(userName)) userName else loginResponse.userId
        loginInfo.userAvatar = Constant.IMAGE_BASE+loginResponse.avatar
        var mlvbLiveRoomImpl = MLVBLiveRoomImpl.sharedInstance(this)
        mlvbLiveRoomImpl.initMlvb(loginInfo)
    }

    override fun initListener() {
        super.initListener()
        loginBtn.onClick {
            mAuthHelper?.getLoginToken(this@OneKeyLoginActivity,5000)
        }
    }

}