package com.yiheoline.qcloud.xiaozhibo.login

import android.text.TextUtils
import android.util.Log
import com.dyhdyh.widget.loadingbar2.LoadingBar
import com.lzy.okgo.OkGo
import com.lzy.okgo.model.HttpHeaders
import com.lzy.okgo.model.HttpParams
import com.lzy.okgo.model.Response
import com.lzy.okgo.request.base.Request
import com.tencent.mmkv.MMKV
import com.yiheoline.liteav.demo.lvb.liveroom.IMLVBLiveRoomListener.LoginCallback
import com.yiheoline.liteav.demo.lvb.liveroom.MLVBLiveRoom
import com.yiheoline.liteav.demo.lvb.liveroom.MLVBLiveRoomImpl
import com.yiheoline.liteav.demo.lvb.liveroom.roomutil.commondef.LoginInfo
import com.yiheoline.qcloud.xiaozhibo.Constant
import com.yiheoline.qcloud.xiaozhibo.TCApplication
import com.yiheoline.qcloud.xiaozhibo.base.BaseActivity
import com.yiheoline.qcloud.xiaozhibo.http.BaseResponse
import com.yiheoline.qcloud.xiaozhibo.http.JsonCallBack
import com.yiheoline.qcloud.xiaozhibo.http.response.APPidResponse
import com.yiheoline.qcloud.xiaozhibo.http.response.LoginResponse
import com.yiheoline.qcloud.xiaozhibo.main.TCMainActivity
import com.yiheoline.qcloud.xiaozhibo.utils.FastJsonUtil
import com.yiheoline.qcloud.xiaozhibo.widgets.MyCountDownTimer
import com.yiheonline.qcloud.xiaozhibo.R
import kotlinx.android.synthetic.main.activity_login_code.*
import org.jetbrains.anko.sdk27.coroutines.onClick
import org.jetbrains.anko.startActivity
import org.jetbrains.anko.toast

class LoginActivity : BaseActivity() {
    override fun getLayoutId(): Int {
        return R.layout.activity_login_code
    }

    override fun initView() {
        super.initView()
        var loginName = MMKV.defaultMMKV().decodeString("loginName")
        userNameInput.setText(loginName)
    }

    override fun initListener() {
        super.initListener()
        backView.onClick {
            finish()
        }

        sendBtn.onClick {
            var mobile = userNameInput.text.toString()
            when{
                mobile.isEmpty() ->toast("请输入手机号")
                else ->{
                    var counter = MyCountDownTimer(sendBtn,60000-1,1000)
                    counter.start()
                    sendCode(mobile)
                }
            }

        }

        loginBtn.onClick {
            var userName = userNameInput.text.toString()
            var code = passwordInput.text.toString()
            when{
                userName.isEmpty() ->toast("请输入手机号")
                code.isEmpty() || code.length < 6 -> toast("请输入验证码")
                else ->{
                    login(userName,code.toInt())
                }
            }
        }
    }

    /**
     * 发送验证码
     */
    private fun sendCode(mobile : String){
        var params = HttpParams()
        params.put("mobile",mobile)
        params.put("templateCode",2)
        OkGo.post<BaseResponse<String>>(Constant.SEND_CODE)
                .params(params)
                .execute(object : JsonCallBack<BaseResponse<String>>(){
                    override fun onSuccess(response: Response<BaseResponse<String>>?) {
                        if(response?.body()?.res == 0){
                            toast("验证码已发送")
                        }else{
                            toast(response?.body()?.msg+"")
                        }
                    }

                })
    }

    /**
     * 登录
     */
    private fun login(loginName :String,code :Int){
        var params = HttpParams()
        params.put("platform", 1)
        params.put("loginName",loginName)
        params.put("code",code)
        OkGo.post<BaseResponse<LoginResponse>>(Constant.CODE_LOGIN)
            .params(params)
            .execute(object : JsonCallBack<BaseResponse<LoginResponse>>(){
                override fun onStart(request: Request<BaseResponse<LoginResponse>, out Request<Any, Request<*, *>>>?) {
                    super.onStart(request)
                    LoadingBar.dialog(mContext).extras(arrayOf("正在登录")).show()
                }

                override fun onFinish() {
                    super.onFinish()
                }

                override fun onError(response: Response<BaseResponse<LoginResponse>>?) {
                    super.onError(response)
                    LoadingBar.dialog(mContext).cancel()
                    toast("请求超时，请检查网络后重试")
                }
                override fun onSuccess(response: Response<BaseResponse<LoginResponse>>?) {
                    LoadingBar.dialog(mContext).cancel()
                    if(response?.body()?.res == 0){
                        toast("登陆成功")
                        MMKV.defaultMMKV().encode("loginName",loginName)
                        TCApplication.token = response.body()?.data!!.token
                        TCApplication.isLogin = true
                        TCApplication.userId = response.body()?.data!!.userId
                        MMKV.defaultMMKV().encode("token",TCApplication.token)
                        val headers = HttpHeaders()
                        headers.put("token", TCApplication.token)
                        OkGo.getInstance().addCommonHeaders(headers)
                        getAppId(response.body().data!!)
//                        setResult(Activity.RESULT_OK)
//                        EventBus.getDefault().post(LoginEvent())
                        startActivity<TCMainActivity>()
                        finish()
                    }else{
                        toast(response?.body()?.msg+"")
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
        loginInfo.userAvatar = loginResponse.avatar
        MMKV.defaultMMKV().encode("MLVB",FastJsonUtil.createJsonString(loginInfo))
        var mlvbLiveRoomImpl = MLVBLiveRoomImpl.sharedInstance(this)
        mlvbLiveRoomImpl.initMlvb(loginInfo)
    }
}
