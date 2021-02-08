package com.yiheoline.qcloud.xiaozhibo.profile

import com.bumptech.glide.Glide
import com.lzy.okgo.OkGo
import com.lzy.okgo.model.Response
import com.tencent.mmkv.MMKV
import com.yiheoline.qcloud.xiaozhibo.Constant
import com.yiheoline.qcloud.xiaozhibo.TCApplication
import com.yiheoline.qcloud.xiaozhibo.base.BaseFragment
import com.yiheoline.qcloud.xiaozhibo.bean.UserInfo
import com.yiheoline.qcloud.xiaozhibo.http.BaseResponse
import com.yiheoline.qcloud.xiaozhibo.http.JsonCallBack
import com.yiheoline.qcloud.xiaozhibo.utils.FastJsonUtil
import com.yiheonline.qcloud.xiaozhibo.R
import kotlinx.android.synthetic.main.fragment_profile.*
import org.jetbrains.anko.sdk27.coroutines.onClick
import org.jetbrains.anko.startActivity
import org.jetbrains.anko.support.v4.startActivity
import org.jetbrains.anko.support.v4.toast
import org.jetbrains.anko.toast

class ProfileFragment : BaseFragment() {
    var verifys = arrayListOf("","未认证","个人认证","团体认证")
    private var userInfo: UserInfo? = null

    override fun getLayout(): Int {
        return R.layout.fragment_profile
    }

    override fun lazyLoad() {
        getUerInfo()
    }

    override fun initView() {
        super.initView()
        upLoadItem.onClick {
            startActivity<UpLoadRecordActivity>()
        }

    }

    private fun setUi(userInfo: UserInfo){
        this.userInfo = userInfo
        //修改昵称或者头像之后 放入
        TCApplication.loginInfo?.nickname = userInfo.nickname
        TCApplication.loginInfo?.avatar = userInfo.avatar
        MMKV.defaultMMKV().encode("loginInfo", FastJsonUtil.createJsonString(TCApplication.loginInfo))
        MMKV.defaultMMKV().encode("userType",userInfo.type)
        nameView?.text = userInfo.nickname
        descView?.text = userInfo.introduction
        dynamicTextView?.text = userInfo.likes.toString()
        followTextView?.text = userInfo.followNum.toString()
        fansTextView?.text = userInfo.fanNum.toString()
        if(userInfo.gender == 0){
            genderImageView?.setImageResource(R.mipmap.female)
        }else{
            genderImageView?.setImageResource(R.mipmap.male)
        }
        if(userInfo.avatar != null && userInfo.avatar.isNotEmpty())
            Glide.with(context).load(Constant.IMAGE_BASE+userInfo.avatar).into(headerView)
        wtyNumView?.text = userInfo.totalWutongye.toString()
        wtbNumView?.text = userInfo.totalWutongbi.toString()
        phoneView?.text = userInfo.mobile

        verifyView?.text = verifys[userInfo.type]
    }

    override fun initListener() {
        super.initListener()
        verifyItem.onClick {
            if(userInfo?.type == 1)
                startActivity<CompanyAuthActivity>()
        }
        toEditInfoView.onClick {
            if(userInfo != null)
                startActivity<EditUserInfoActivity>("userInfo" to userInfo)
        }

        followView.onClick {
            startActivity<FansListActivity>("type" to 0)
        }

        fansView.onClick {
            startActivity<FansListActivity>("type" to 1)
        }
        toSetBtn.onClick {
            startActivity<SettingActivity>()
        }
        wtyContain.onClick {
            startActivity<AccountActivity>("total" to userInfo?.totalWutongye)
        }
        centerItem.onClick {
            startActivity<AnchorCenterActivity>()
        }
        orderItem.onClick {
            startActivity<OrderListActivity>()
        }
        wtbContain.onClick {
            startActivity<CashOutActivity>("total" to userInfo?.totalWutongbi)
        }
        messageBtn.onClick { startActivity<MessageActivity>() }
        seeItem.onClick { startActivity<SeeRecordActivity>() }
        likeItem.onClick { startActivity<LikeActivity>() }
    }


    /**
     * 获取个人信息
     */
    private fun getUerInfo(){
        OkGo.post<BaseResponse<UserInfo>>(Constant.PERSONAL_CENTER)
                .execute(object : JsonCallBack<BaseResponse<UserInfo>>(){
                    override fun onSuccess(response: Response<BaseResponse<UserInfo>>?) {
                        if(response?.body()?.res == 0){
                            setUi(response.body()?.data!!)
                        }else{
                            toast(response?.body()?.msg.toString())
                        }
                    }

                })
    }
}