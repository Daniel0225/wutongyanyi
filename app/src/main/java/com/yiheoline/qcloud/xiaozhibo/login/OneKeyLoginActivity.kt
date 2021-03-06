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
import com.yiheoline.qcloud.xiaozhibo.main.MainActivity
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
                        //???????????????????????? ??????????????????app?????????
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
//                        toast("?????????????????????,???????????????????????????")
//                    }
//                    "600007" -> {
//                        toast("????????????sim???")
//                    }
//                    "600008" -> {
//                        toast("?????????????????????")
//                    }
//                    "600011" -> {
//                        toast("??????token??????,,???????????????????????????")
//                    }
//                    "600002" -> {
//                        toast("?????????????????????,???????????????????????????")
//                    }
//                    "600015" -> {
//                        toast("????????????,???????????????????????????")
//                    }
//                    "600021" -> {
//                        toast("??????????????????,???????????????????????????")
//                    }
//                    else -> {
//                        toast("????????????,???????????????????????????" + tokenRet!!.code)
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
     * ??????????????????????????????????????????
     */
    private fun toAnotherLogin(){
        Toast.makeText(applicationContext, "?????????????????????????????????????????????", Toast.LENGTH_SHORT).show()
        val pIntent = Intent(this@OneKeyLoginActivity, LoginActivity::class.java)
        startActivityForResult(pIntent, 1002)
        finish()
    }

    /**
     * ??????app??????????????????????????????
     */
    private fun oneKeyLogin() {
        mAuthHelper = PhoneNumberAuthHelper.getInstance(applicationContext, mTokenResultListener)
        mUIConfig?.configAuthPage()
        mAuthHelper?.getLoginToken(this, 5000)
        LoadingBar.dialog(this).show()
    }



    /**
     * ????????????
     */
    private fun onKeyLogin(token:String){
        var params = HttpParams()
        params.put("accessToken",token)
        params.put("platform",1)
        params.put("umengDeviceToken",MMKV.defaultMMKV().decodeString("deviceToken"))
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
                            toast("????????????")
                            var loginInfo = response.body().data
                            TCApplication.isLogin = true
                            TCApplication.loginInfo = loginInfo
                            MMKV.defaultMMKV().encode("token",loginInfo.token)
                            MMKV.defaultMMKV().encode("loginInfo", FastJsonUtil.createJsonString(loginInfo))
                            val headers = HttpHeaders()
                            headers.put("token", loginInfo.token)
                            OkGo.getInstance().addCommonHeaders(headers)
                            getAppId(response.body().data!!)
                            startActivity<MainActivity>()
                            finish()
                        }else{
                            toast(response?.body()?.msg.toString())
                            toAnotherLogin()
                        }
                    }

                })
    }

    /**
     * ??????????????????????????????
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
     * ????????? MLVB ??????
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