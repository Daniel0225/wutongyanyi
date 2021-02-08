package com.yiheoline.qcloud.xiaozhibo.profile

import android.content.Intent
import android.text.TextUtils
import android.view.View
import com.bilibili.boxing.Boxing
import com.bilibili.boxing.BoxingMediaLoader
import com.bilibili.boxing.model.config.BoxingConfig
import com.bilibili.boxing.model.entity.BaseMedia
import com.bilibili.boxing.model.entity.impl.ImageMedia
import com.bilibili.boxing_impl.ui.BoxingActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.dyhdyh.widget.loadingbar2.LoadingBar
import com.lzy.okgo.OkGo
import com.lzy.okgo.model.HttpParams
import com.lzy.okgo.model.Response
import com.yiheoline.qcloud.xiaozhibo.Constant
import com.yiheoline.qcloud.xiaozhibo.base.BaseActivity
import com.yiheoline.qcloud.xiaozhibo.bean.UpImageBean
import com.yiheoline.qcloud.xiaozhibo.http.BaseResponse
import com.yiheoline.qcloud.xiaozhibo.http.JsonCallBack
import com.yiheoline.qcloud.xiaozhibo.utils.BoxingGlideLoader
import com.yiheoline.qcloud.xiaozhibo.utils.FileUtils
import com.yiheoline.qcloud.xiaozhibo.utils.GlideRoundTransform
import com.yiheonline.qcloud.xiaozhibo.R
import kotlinx.android.synthetic.main.activity_company_auth.*
import kotlinx.android.synthetic.main.toolbar_layout.*
import org.jetbrains.anko.sdk27.coroutines.onClick
import org.jetbrains.anko.toast
import top.zibin.luban.Luban
import top.zibin.luban.OnCompressListener
import java.io.File

class CompanyAuthActivity : BaseActivity() {

    private var authType = 1
    private var chooseImagePosition = 0
    private var idCardBack = ""
    private var idCardFront = ""
    private var license = ""
    private var honor = ""
    private var warmStrings = arrayListOf<String>("请输入公司名","请输入姓名")
    override fun getLayoutId(): Int {
        return R.layout.activity_company_auth
    }

    override fun initView() {
        super.initView()
        BoxingMediaLoader.getInstance().init(BoxingGlideLoader()) // 需要实现IBoxingMediaLoader
        backView.onClick { finish() }
        titleView.text = "身份认证"

    }

    override fun initListener() {
        super.initListener()
        companyBtn.onClick {
            exchangeType(0)
        }
        personBtn.onClick {
            exchangeType(1)
        }
        companyLabel.onClick { exchangeType(0) }
        personLabel.onClick { exchangeType(1) }

        confirmBtn.onClick {
            var companyName = nameInputView.text.toString()
            var cardNum = cardNumInputView.text.toString()
            var workType = workTypeInputView.text.toString()
            when{
                companyName.isEmpty() -> toast(warmStrings[authType])
                authType == 2 && cardNum.isEmpty() -> toast("请填写身份证号")
                authType == 2 && workType.isEmpty() -> toast("请填写职业类型")
                idCardBack.isEmpty() -> toast("请上传身份证背面图片")
                idCardFront.isEmpty() ->toast("请上传身份证正面图片")
                authType == 1 && license.isEmpty() ->toast("请上传营业执照")
                else ->{
                    commitApply(companyName,cardNum,workType)
                }
            }
        }

        imageView1.onClick {
            chooseImagePosition = 0
            selectImage()
        }
        imageView2.onClick {
            chooseImagePosition = 1
            selectImage()
        }
        imageView3.onClick {
            chooseImagePosition = 2
            selectImage()
        }
        imageView4.onClick {
            chooseImagePosition = 3
            selectImage()
        }
    }

    /**
     * 调用图片选择
     */
    private fun selectImage(){
        var config = BoxingConfig(BoxingConfig.Mode.SINGLE_IMG)
        config.needCamera(R.mipmap.ic_boxing_camera_white)
        Boxing.of(config).withIntent(this@CompanyAuthActivity, BoxingActivity::class.java)
                .start(this@CompanyAuthActivity, 200)
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

    //压缩图片 并且上传
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
        params.put("type",3)
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
                            var imagePath = response?.body()?.data?.filename!!
                            when(chooseImagePosition){
                                0 ->{
                                    license = imagePath
                                    Glide.with(this@CompanyAuthActivity)
                                            .load("${Constant.IMAGE_BASE}/$imagePath")
                                            .transform(CenterCrop(this@CompanyAuthActivity),
                                                    GlideRoundTransform(this@CompanyAuthActivity,5))
                                            .into(imageView1)
                                }
                                1 ->{
                                    idCardFront = imagePath
                                    Glide.with(this@CompanyAuthActivity)
                                            .load("${Constant.IMAGE_BASE}/$imagePath")
                                            .transform(CenterCrop(this@CompanyAuthActivity),
                                                    GlideRoundTransform(this@CompanyAuthActivity,5))
                                            .into(imageView2)
                                }
                                2 ->{
                                    idCardBack = imagePath
                                    Glide.with(this@CompanyAuthActivity)
                                            .load("${Constant.IMAGE_BASE}/$imagePath")
                                            .transform(CenterCrop(this@CompanyAuthActivity),
                                                    GlideRoundTransform(this@CompanyAuthActivity,5))
                                            .into(imageView3)
                                }
                                3 ->{
                                    honor = imagePath
                                    Glide.with(this@CompanyAuthActivity)
                                            .load("${Constant.IMAGE_BASE}/$imagePath")
                                            .transform(CenterCrop(this@CompanyAuthActivity),
                                                    GlideRoundTransform(this@CompanyAuthActivity,5))
                                            .into(imageView4)
                                }
                            }

                        }else{
                            toast(response?.body()?.msg+"")
                        }
                    }

                })
    }

    /**
     * 提交认证申请
     */
    private fun commitApply(companyName:String,cardNum:String,profession:String){
        var params = HttpParams()
        params.put("authType",authType)
        params.put("idCardBack",idCardBack)
        params.put("idCardFront",idCardFront)
        params.put("honor",honor)
        if(authType == 2){
            params.put("trueName",companyName.toString())
            params.put("profession",profession)
            params.put("idCard",cardNum)
        }else{
            params.put("companyName",companyName)
            params.put("license",license)
        }
        OkGo.post<BaseResponse<String>>(Constant.AUTH)
                .params(params)
                .execute(object:JsonCallBack<BaseResponse<String>>(){
                    override fun onSuccess(response: Response<BaseResponse<String>>?) {
                        if(response?.body()?.res == 0){
                            toast("认证申请已提交")
                            finish()
                        }else{
                            toast(response?.body()?.msg.toString())
                        }
                    }

                })
    }

    /**
     * 切换认证类型
     */
    private fun exchangeType(type:Int){
        authType = type
        if(type == 0){
            companyBtn.setImageResource(R.mipmap.xuanzhong)
            personBtn.setImageResource(R.mipmap.tuoyuan)
            imageLabel4.text = "上传公司荣誉照片"
            imageLabel2.text = "上传法人身份证正面"
            imageLabel3.text = "上传法人身份证背面"
            cardNumContain.visibility = View.GONE
            workTypeContain.visibility = View.GONE
            label1.text = "公司名称："
            licenseContain.visibility = View.VISIBLE
        }else{
            companyBtn.setImageResource(R.mipmap.tuoyuan)
            personBtn.setImageResource(R.mipmap.xuanzhong)
            imageLabel4.text = "上传个人荣誉照片"
            imageLabel2.text = "上传手持身份证正面照"
            imageLabel3.text = "上传手持身份证背面照"
            cardNumContain.visibility = View.VISIBLE
            workTypeContain.visibility = View.VISIBLE
            label1.text = "真实姓名："
            licenseContain.visibility = View.GONE
        }
    }


}