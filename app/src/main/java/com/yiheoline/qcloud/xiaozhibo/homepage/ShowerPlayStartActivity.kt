package com.yiheoline.qcloud.xiaozhibo.homepage

import android.content.Intent
import android.text.TextUtils
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
import com.yiheoline.liteav.demo.lvb.liveroom.MLVBLiveRoom
import com.yiheoline.qcloud.xiaozhibo.Constant
import com.yiheoline.qcloud.xiaozhibo.TCApplication
import com.yiheoline.qcloud.xiaozhibo.anchor.TCCameraAnchorActivity
import com.yiheoline.qcloud.xiaozhibo.base.BaseActivity
import com.yiheoline.qcloud.xiaozhibo.bean.UpImageBean
import com.yiheoline.qcloud.xiaozhibo.common.utils.TCConstants
import com.yiheoline.qcloud.xiaozhibo.http.BaseResponse
import com.yiheoline.qcloud.xiaozhibo.http.JsonCallBack
import com.yiheoline.qcloud.xiaozhibo.utils.BoxingGlideLoader
import com.yiheoline.qcloud.xiaozhibo.utils.FileUtils
import com.yiheonline.qcloud.xiaozhibo.R
import kotlinx.android.synthetic.main.activity_apply_show.*
import kotlinx.android.synthetic.main.activity_shower_play_start.*
import kotlinx.android.synthetic.main.activity_shower_play_start.addCover
import kotlinx.android.synthetic.main.activity_shower_play_start.coverImage
import kotlinx.android.synthetic.main.activity_shower_play_start.titleInputView
import org.jetbrains.anko.sdk27.coroutines.onClick
import org.jetbrains.anko.startActivity
import org.jetbrains.anko.toast
import top.zibin.luban.Luban
import top.zibin.luban.OnCompressListener
import java.io.File

class ShowerPlayStartActivity : BaseActivity() {
    var cover = ""
    var mLiveRoom: MLVBLiveRoom = MLVBLiveRoom.sharedInstance(this);
    override fun getLayoutId(): Int {
        return R.layout.activity_shower_play_start
    }

    override fun initView() {
        super.initView()
        mTXCloudVideoView.setLogMargin(10f, 10f, 45f, 55f)
        BoxingMediaLoader.getInstance().init(BoxingGlideLoader()) // 需要实现IBoxingMediaLoader
        // 打开本地预览，传入预览的 View
        mLiveRoom.startLocalPreview(true, mTXCloudVideoView)

        startPlayBtn.onClick {
            var title = titleInputView.text.toString()
            if(title.isEmpty()){
                toast("请输入标题")
                return@onClick
            }
            var intent = Intent(mContext, TCCameraAnchorActivity::class.java)
            intent.putExtra(TCConstants.ROOM_TITLE,title)
            intent.putExtra(TCConstants.USER_ID,TCApplication.loginInfo?.userId)
            startActivity(intent)
            finish()
        }

        backView.onClick {
            finish()
        }
        changeCamera.onClick {
            mLiveRoom.switchCamera()
        }

        addCover.onClick {
            var config = BoxingConfig(BoxingConfig.Mode.SINGLE_IMG)
            config.needCamera(R.mipmap.ic_boxing_camera_white)
            Boxing.of(config).withIntent(this@ShowerPlayStartActivity, BoxingActivity::class.java)
                    .start(this@ShowerPlayStartActivity, 200)
        }

        startPlayBtn.onClick {
            var title = titleInputView.text.toString()
            when{
                title.isEmpty() ->toast("请填写标题")
                cover.isEmpty() ->toast("请上传封面")
                else ->{
                    var intent = Intent(mContext, TCCameraAnchorActivity::class.java)
                    intent.putExtra(TCConstants.ROOM_TITLE,title)
                    intent.putExtra(TCConstants.USER_NICK,TCApplication.loginInfo?.nickname)
                    intent.putExtra(TCConstants.COVER_PIC,cover)
                    intent.putExtra(TCConstants.USER_HEADPIC,Constant.IMAGE_BASE+TCApplication.loginInfo?.avatar)
                    intent.putExtra(TCConstants.USER_ID, TCApplication.loginInfo?.userId)
                    startActivity(intent)
                    finish()
                }
            }
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
        params.put("type",4)
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
                            cover = response.body()?.data?.filename!!
                            Glide.with(this@ShowerPlayStartActivity)
                                    .load("${Constant.IMAGE_BASE}/$cover")
                                    .into(coverImage)
                        }else{
                            toast(response?.body()?.msg+"")
                        }
                    }

                })
    }

    override fun onDestroy() {
        super.onDestroy()
        mLiveRoom.stopLocalPreview()
    }
}