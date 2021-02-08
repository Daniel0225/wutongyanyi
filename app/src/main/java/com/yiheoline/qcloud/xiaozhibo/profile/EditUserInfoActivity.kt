package com.yiheoline.qcloud.xiaozhibo.profile

import android.content.Intent
import android.text.TextUtils
import android.view.View
import com.bigkoo.pickerview.builder.OptionsPickerBuilder
import com.bigkoo.pickerview.builder.TimePickerBuilder
import com.bigkoo.pickerview.listener.OnOptionsSelectListener
import com.bigkoo.pickerview.listener.OnTimeSelectListener
import com.bilibili.boxing.Boxing
import com.bilibili.boxing.BoxingMediaLoader
import com.bilibili.boxing.model.config.BoxingConfig
import com.bilibili.boxing.model.entity.BaseMedia
import com.bilibili.boxing.model.entity.impl.ImageMedia
import com.bilibili.boxing_impl.ui.BoxingActivity
import com.bumptech.glide.Glide
import com.dyhdyh.widget.loadingbar2.LoadingBar
import com.lzy.okgo.OkGo
import com.lzy.okgo.model.HttpParams
import com.lzy.okgo.model.Response
import com.yiheoline.qcloud.xiaozhibo.Constant
import com.yiheoline.qcloud.xiaozhibo.TCApplication
import com.yiheoline.qcloud.xiaozhibo.base.BaseActivity
import com.yiheoline.qcloud.xiaozhibo.bean.InfoChangeBean
import com.yiheoline.qcloud.xiaozhibo.bean.UpImageBean
import com.yiheoline.qcloud.xiaozhibo.bean.UserInfo
import com.yiheoline.qcloud.xiaozhibo.dialog.AddressPickerDialog
import com.yiheoline.qcloud.xiaozhibo.dialog.CommentDialog
import com.yiheoline.qcloud.xiaozhibo.http.BaseResponse
import com.yiheoline.qcloud.xiaozhibo.http.JsonCallBack
import com.yiheoline.qcloud.xiaozhibo.utils.BoxingGlideLoader
import com.yiheoline.qcloud.xiaozhibo.utils.FileUtils
import com.yiheoline.qcloud.xiaozhibo.utils.TimeUtil
import com.yiheonline.qcloud.xiaozhibo.R
import kotlinx.android.synthetic.main.activity_edit_user_info2.*
import kotlinx.android.synthetic.main.toolbar_layout.*
import org.jetbrains.anko.sdk27.coroutines.onClick
import org.jetbrains.anko.toast
import top.zibin.luban.Luban
import top.zibin.luban.OnCompressListener
import java.io.File

class EditUserInfoActivity : BaseActivity() {
    private var upType = 0
    private var userInfo: UserInfo? = null
    var typeList = arrayOf("男","女")
    override fun getLayoutId(): Int {
        return R.layout.activity_edit_user_info2
    }

    override fun initView() {
        super.initView()
        BoxingMediaLoader.getInstance().init(BoxingGlideLoader()) // 需要实现IBoxingMediaLoader
        backView.onClick { finish() }
        titleView.text = "编辑资料"
        userInfo = intent.getSerializableExtra("userInfo") as UserInfo

        if(userInfo!!.avatar != null && userInfo!!.avatar.isNotEmpty())
        Glide.with(this).load("${Constant.IMAGE_BASE}${userInfo?.avatar}")
                .into(userHeader)

        nickView.text = userInfo?.nickname
        sexView.text = typeList[userInfo?.gender!!]
        if(userInfo!!.birthday != null)
            birthdayView.text = TimeUtil.getYearMonthAndDay(userInfo?.birthday!!.toLong())
        addressView.text = userInfo?.residence
        workTypeView.text = userInfo?.profession
        heightView.text = userInfo?.height
        weightView.text = userInfo?.weight
        descView.text = userInfo?.introduction
        Glide.with(this@EditUserInfoActivity)
                .load("${Constant.IMAGE_BASE}${userInfo?.cover}")
                .into(mainBgImageView)

        if(userInfo!!.type == 1){
            descContain.visibility = View.GONE
            bgContain.visibility = View.GONE
        }
    }

    override fun initListener() {
        super.initListener()
        headerView.onClick {
            upType = 0
            var config = BoxingConfig(BoxingConfig.Mode.SINGLE_IMG)
            config.needCamera(R.mipmap.ic_boxing_camera_white)
            Boxing.of(config).withIntent(this@EditUserInfoActivity, BoxingActivity::class.java)
                    .start(this@EditUserInfoActivity, 200)
        }

        chooseSex.onClick {
            var optionsPickView = OptionsPickerBuilder(this@EditUserInfoActivity,
                    OnOptionsSelectListener { options1, _, _, _ ->
                        sexView.text = typeList[options1]
                        sexView.tag = options1//记录选择的位置下标 当做id
                        changeInfo(InfoChangeBean("gender",options1.toString()))
                    }).build<String>()
            optionsPickView.setPicker(typeList.toMutableList())
            optionsPickView.show()
        }

        chooseBirthday.onClick {
            var types = arrayOf(true, true, true, false, false, false)
            var timePicker = TimePickerBuilder(this@EditUserInfoActivity,
                    OnTimeSelectListener { date, v ->
                        birthdayView.text = TimeUtil.getYearMonthAndDay(date.time)
                        changeInfo(InfoChangeBean("birthday",birthdayView.text.toString()))
                    })
                    .setType(types.toBooleanArray())
                    .build()
            timePicker.show()
        }

        mainBgImageView.onClick {
            upType = 1
            var config = BoxingConfig(BoxingConfig.Mode.SINGLE_IMG)
            config.needCamera(R.mipmap.ic_boxing_camera_white)
            Boxing.of(config).withIntent(this@EditUserInfoActivity, BoxingActivity::class.java)
                    .start(this@EditUserInfoActivity, 200)
        }

        nickContain.onClick {
            CommentDialog.onCreateDialog(this@EditUserInfoActivity,"好的昵称更容易让人记住哦",object : CommentDialog.PublishCommentListener{
                override fun publish(commentContent: String) {
                    changeInfo(InfoChangeBean("nickname",commentContent))
                }

            })
        }
        chooseAddress.onClick {
            AddressPickerDialog.onCreateDialog(this@EditUserInfoActivity,object : AddressPickerDialog.OnAddressPickListener{
                override fun select(address: String, provinceCode: String, cityCode: String, areaCode: String) {
                    changeInfo(InfoChangeBean("residence",address))
                }

            })
        }
        workTypeContain.onClick {
            var workTypes = resources.getStringArray(R.array.workTypes)
            var optionsPickerView = OptionsPickerBuilder(this@EditUserInfoActivity,
            OnOptionsSelectListener { options1, _, _, _ ->
                var workType = workTypes[options1]
                changeInfo(InfoChangeBean("profession",workType))
            }).build<String>()
            optionsPickerView.setPicker(workTypes.toMutableList())
            optionsPickerView.show()

        }
        heightContain.onClick {
            var heights = resources.getStringArray(R.array.heights)
            var optionsPickerView = OptionsPickerBuilder(this@EditUserInfoActivity,
                    OnOptionsSelectListener { options1, _, _, _ ->
                        var height = heights[options1]
                        changeInfo(InfoChangeBean("height",height.replace("cm","")))
                    }).build<String>()
            optionsPickerView.setPicker(heights.toMutableList())
            optionsPickerView.show()
        }
        weightContain.onClick {
            var weights = resources.getStringArray(R.array.weights)
            var optionsPickerView = OptionsPickerBuilder(this@EditUserInfoActivity,
                    OnOptionsSelectListener { options1, _, _, _ ->
                        var weight = weights[options1]
                        changeInfo(InfoChangeBean("weight",weight.replace("kg","")))
                    }).build<String>()
            optionsPickerView.setPicker(weights.toMutableList())
            optionsPickerView.show()
        }
        descContain.onClick {
            CommentDialog.onCreateDialog(this@EditUserInfoActivity,"向大家介绍一下自己吧",object : CommentDialog.PublishCommentListener{
                override fun publish(commentContent: String) {
                    changeInfo(InfoChangeBean("introduction",commentContent))
                }

            })
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode == RESULT_OK){
            var medias = Boxing.getResult(data)
            if(medias != null){
                lubanImage(medias)
            }
        }
    }

    private fun lubanImage(medias : ArrayList<BaseMedia>){
        LoadingBar.dialog(mContext).extras(arrayOf("正在上传")).show()
        for(i in medias.indices){
            Luban.with(mContext).load(medias[i].path).ignoreBy(100).setTargetDir(FileUtils.getCachePath(this))
                    .filter {  !(TextUtils.isEmpty(it) || it.toLowerCase().endsWith(".gif")) }
                    .setCompressListener(object : OnCompressListener {
                        override fun onError(e: Throwable?) {
                        }

                        override fun onStart() {
                        }

                        override fun onSuccess(file: File?) {
                            var baseMedia = medias[0]
                            var imageMedia = baseMedia as ImageMedia
                            imageMedia.compressPath = file?.path

                            upLoadImage(imageMedia.compressPath,i == medias.size - 1)
                        }

                    }).launch()
        }
    }

    /**
     * 上传图片
     */
    private fun upLoadImage(imagePath:String,isEnd : Boolean){
        var params = HttpParams()
        params.put("file", File(imagePath))
        params.put("type",1)
        OkGo.post<BaseResponse<UpImageBean>>(Constant.UPLOAD_PIC)
                .params(params)
                .execute(object : JsonCallBack<BaseResponse<UpImageBean>>(){

                    override fun onFinish() {
                        super.onFinish()
                        if(isEnd){
                            LoadingBar.dialog(mContext).cancel()
                        }
                    }
                    override fun onError(response: Response<BaseResponse<UpImageBean>>?) {
                        super.onError(response)
                        toast("图片上传失败，请检查网络后重试")
                    }
                    override fun onSuccess(response: Response<BaseResponse<UpImageBean>>?) {
                        if(response?.body()?.res == 0){
                            if(upType == 0){
                                var avatar = response?.body()?.data?.filename!!
                                Glide.with(this@EditUserInfoActivity)
                                        .load("${Constant.IMAGE_BASE}$avatar")
                                        .into(userHeader)
                                changeInfo(InfoChangeBean("avatar",avatar))
                            }else{
                                var cover = response?.body()?.data?.filename!!
                                Glide.with(this@EditUserInfoActivity)
                                        .load("${Constant.IMAGE_BASE}$cover")
                                        .into(mainBgImageView)
                                changeInfo(InfoChangeBean("cover",cover))
                            }

                        }else{
                            toast(response?.body()?.msg+"")
                        }
                    }

                })
    }


    /**
     *修改个人资料
     */
    private fun changeInfo(changeBean: InfoChangeBean){
        var params = HttpParams()
        params.put(changeBean.key,changeBean.value)
        params.put("userId",TCApplication.loginInfo?.userId)
        OkGo.post<BaseResponse<String>>(Constant.UPDATE_USER_INFO)
                .params(params)
                .execute(object:JsonCallBack<BaseResponse<String>>(){
                    override fun onSuccess(response: Response<BaseResponse<String>>?) {
                        if(response?.body()?.res == 0){
                            toast("修改成功")
                            when(changeBean.key){
                                "nickname" -> nickView.text = changeBean.value
                                "residence" -> addressView.text = changeBean.value
                                "profession" -> workTypeView.text = changeBean.value
                                "height" -> heightView.text = changeBean.value
                                "weight" -> weightView.text = changeBean.value
                                "introduction" ->descView.text = changeBean.value
                            }
                        }else{
                            toast(response?.body()?.msg.toString())
                        }
                    }

                })
    }
}