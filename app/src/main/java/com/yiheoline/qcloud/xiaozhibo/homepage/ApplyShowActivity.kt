package com.yiheoline.qcloud.xiaozhibo.homepage

import android.content.Intent
import android.nfc.Tag
import android.text.TextUtils
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
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
import com.bilibili.boxing_impl.ui.BoxingViewActivity
import com.bumptech.glide.Glide
import com.chad.library.adapter.base.BaseQuickAdapter
import com.dyhdyh.widget.loadingbar2.LoadingBar
import com.library.flowlayout.FlowLayoutManager
import com.library.flowlayout.SpaceItemDecoration
import com.lzy.okgo.OkGo
import com.lzy.okgo.model.HttpParams
import com.lzy.okgo.model.Response
import com.lzy.okgo.request.base.Request
import com.yiheoline.qcloud.xiaozhibo.Constant
import com.yiheoline.qcloud.xiaozhibo.base.BaseActivity
import com.yiheoline.qcloud.xiaozhibo.bean.ApplyNoticeBean
import com.yiheoline.qcloud.xiaozhibo.bean.TagBean
import com.yiheoline.qcloud.xiaozhibo.bean.TypeBean
import com.yiheoline.qcloud.xiaozhibo.bean.UpImageBean
import com.yiheoline.qcloud.xiaozhibo.homepage.adapter.AddPicListAdapter
import com.yiheoline.qcloud.xiaozhibo.homepage.adapter.TagListAdapter
import com.yiheoline.qcloud.xiaozhibo.http.BaseResponse
import com.yiheoline.qcloud.xiaozhibo.http.JsonCallBack
import com.yiheoline.qcloud.xiaozhibo.utils.BoxingGlideLoader
import com.yiheoline.qcloud.xiaozhibo.utils.FastJsonUtil
import com.yiheoline.qcloud.xiaozhibo.utils.FileUtils
import com.yiheoline.qcloud.xiaozhibo.utils.TimeUtil
import com.yiheonline.qcloud.xiaozhibo.R
import kotlinx.android.synthetic.main.activity_apply_show.*
import kotlinx.android.synthetic.main.toolbar_layout.*
import org.jetbrains.anko.sdk27.coroutines.onClick
import org.jetbrains.anko.toast
import top.zibin.luban.Luban
import top.zibin.luban.OnCompressListener
import java.io.File
import java.time.Duration

class ApplyShowActivity : BaseActivity() {
    var typeList : MutableList<TypeBean>? = null
    var tags = arrayListOf<TagBean>()
    var adapter : AddPicListAdapter? = null
    var tagAdapter : TagListAdapter? = null
    var cover = ""
    var imageList = arrayListOf("empty")
    var upImageType = 4
    override fun getLayoutId(): Int {
        return R.layout.activity_apply_show
    }

    override fun initView() {
        super.initView()
        backView.onClick { finish() }
        titleView.text = "申请演出直播"

        BoxingMediaLoader.getInstance().init(BoxingGlideLoader()) // 需要实现IBoxingMediaLoader
        //初始化选择图片RV
        var manager = LinearLayoutManager(this)
        manager.orientation = LinearLayoutManager.HORIZONTAL
        recyclerView.layoutManager = manager
        adapter = AddPicListAdapter(R.layout.upload_pic_item_layout, imageList)
        recyclerView.adapter = adapter
        adapter?.setOnItemClickListener { adapter, view, position ->
            upImageType = 5
            if (position == adapter?.data!!.size - 1) {
                //选择图片
                if (imageList.size == 4) {
                    Toast.makeText(mContext, "最多上传三张图片", Toast.LENGTH_SHORT).show()
                    return@setOnItemClickListener
                }
                var config = BoxingConfig(BoxingConfig.Mode.MULTI_IMG)
                config.needCamera(R.mipmap.ic_boxing_camera_white)
                config.withMaxCount(4 - imageList.size)
                Boxing.of(config).withIntent(this, BoxingActivity::class.java).start(this,200)
            } else {
                //查看大图
                Boxing.get().withIntent(
                        mContext,
                        BoxingViewActivity::class.java,
                        getUirData(imageList)
                ).start(
                        mContext as ApplyShowActivity,
                        BoxingConfig.ViewMode.PREVIEW
                )
            }
        }
        adapter?.setOnItemChildClickListener { _, _, position ->
            imageList!!.removeAt(position)
            adapter?.notifyDataSetChanged()
        }

        //初始化添加标签RV
        tagRecyclerView.layoutManager = FlowLayoutManager()
        tagRecyclerView?.addItemDecoration((SpaceItemDecoration(20)))
        tagAdapter = TagListAdapter(R.layout.tag_item_layout, tags)
        tagRecyclerView.adapter = tagAdapter
        tagAdapter?.setOnItemClickListener { _, _, position ->
            tags[position].isChecked = !tags[position].isChecked
            tagAdapter?.notifyDataSetChanged()
        }
        //选择类型
        typeInputView.onClick {
            if(typeList == null){
                toast("无分类数据")
                getTypeList()
            }else{
                var tempList = arrayListOf<String>()
                for (item in typeList!!){
                    tempList.add(item.name)
                }
                var optionsPickView = OptionsPickerBuilder(this@ApplyShowActivity,
                        OnOptionsSelectListener { options1, _, _, _ ->
                            typeInputView.text = typeList!![options1].name
                            typeInputView.tag = typeList!![options1].catId//记录选择的位置下标 当做id
                        }).build<String>()
                optionsPickView.setPicker(tempList)
                optionsPickView.show()
            }

        }
        //选择时间
        startTimeInputView.onClick {
            var types = arrayOf(true, true, true, true, true, false)
            var timePicker = TimePickerBuilder(this@ApplyShowActivity,
                    OnTimeSelectListener { date, _ ->
                        startTimeInputView.text = TimeUtil.getYearMonthAndDayWithHour(date.time)
                    })
                    .setType(types.toBooleanArray())
                    .build()
            timePicker.show()
        }
        //提交申请
        insertNoticeBtn.onClick {
            var title = titleInputView.text.toString()
            var type = typeInputView.text.toString()
            var price = priceInputView.text.toString()
            var startTime = startTimeInputView.text.toString()
            var duration = durationInputView.text.toString()
            var detail = detailInput.text.toString()
            when{
                title.isEmpty() -> toast("请输入标题")
                type.isEmpty() -> toast("请选择类型")
                price.isEmpty() -> toast("请输入价格")
                startTime.isEmpty() -> toast("请选择开始时间")
                duration.isEmpty() -> toast("请输入直播时长")
                detail.isEmpty() -> toast("请输入演出详情描述")
            else -> {
                insertNotice(title,typeInputView.tag.toString(),price,startTime,duration,detail)
            }
            }
        }

        //添加封面
        addCover.onClick {
            upImageType = 4
            var config = BoxingConfig(BoxingConfig.Mode.SINGLE_IMG)
            config.needCamera(R.mipmap.ic_boxing_camera_white)
            Boxing.of(config).withIntent(this@ApplyShowActivity, BoxingActivity::class.java)
                    .start(this@ApplyShowActivity, 200)
        }

        addCustomBtn.onClick {
            var intent = Intent(this@ApplyShowActivity,CustomTagActivity::class.java)
            startActivityForResult(intent,2000)
        }
    }

    override fun initData() {
        super.initData()
        getTypeList()
        getTag()
    }

    private fun getUirData(list : List<String>) : ArrayList<ImageMedia>{
        var uriDataList = arrayListOf<ImageMedia>()
        for (item in list){
            if(item != "empty"){
                var realUrl = "${Constant.IMAGE_BASE}/${item}"
                var baseMedia = ImageMedia("1",realUrl)
                uriDataList.add(baseMedia)
            }
        }
        return uriDataList
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode == RESULT_OK){
            when(requestCode){
                200 ->{
                    var medias = Boxing.getResult(data)
                    if(medias != null){
                        lubanImage(medias)
                    }
                }
                2000 ->{
                    var tagName = data?.getStringExtra("tagName")
                    var tagBean = TagBean()
                    tagBean.name = tagName
                    tagBean.tagId = 0
                    tagBean.isChecked = true
                    tags.add(tagBean)
                    tagAdapter?.notifyDataSetChanged()
                }
            }
        }
    }

    /**
     * 提交申请
     */
    private fun insertNotice(title:String,catId:String,price:String,liveTime:String,duration: String,detail:String){
        var applyNoticeBean = ApplyNoticeBean()
        applyNoticeBean.catId = catId.toInt()
        applyNoticeBean.cover = cover
        applyNoticeBean.detail = detail
        applyNoticeBean.duration = duration.toInt()
        applyNoticeBean.liveTime = liveTime
        applyNoticeBean.imageDetail = getImagePath(imageList)
        applyNoticeBean.price = price.toDouble()
        applyNoticeBean.title = title
        var tagList = arrayListOf<TagBean>()
        for (item in tags){
            if(item.isChecked){
                tagList.add(item)
            }
        }
        applyNoticeBean.tagList = tagList
        OkGo.post<BaseResponse<String>>(Constant.INSERT_NOTICE)
                .upJson(FastJsonUtil.createJsonString(applyNoticeBean))
                .execute(object : JsonCallBack<BaseResponse<String>>(){
                    override fun onStart(request: Request<BaseResponse<String>, out Request<Any, Request<*, *>>>?) {
                        super.onStart(request)
                        LoadingBar.dialog(mContext).extras(arrayOf("提交中")).show()
                    }

                    override fun onFinish() {
                        super.onFinish()
                        LoadingBar.dialog(mContext).cancel()
                    }
                    override fun onSuccess(response: Response<BaseResponse<String>>?) {
                        if(response?.body()?.res == 0){
                            toast("申请已提交")
                            finish()
                        }else{
                            toast(response?.body()?.msg+"")
                        }
                    }

                })
    }
    /**
     * 获取分类集合
     */
    private fun getTypeList(){
        OkGo.get<BaseResponse<List<TypeBean>>>(Constant.QUERY_CAT_LIST)
                .execute(object : JsonCallBack<BaseResponse<List<TypeBean>>>(){
                    override fun onSuccess(response: Response<BaseResponse<List<TypeBean>>>?) {
                        if(response?.body()?.res == 0){
                            typeList = response.body()?.data!! as MutableList<TypeBean>
                        }else{
                            toast(response?.body()?.msg.toString())
                        }
                    }

                })
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
        params.put("type",upImageType)
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
                            if(upImageType == 4){
                                cover = response?.body()?.data?.filename!!
                                Glide.with(this@ApplyShowActivity)
                                        .load("${Constant.IMAGE_BASE}/$cover")
                                        .into(coverImage)
                            }else{
                                imageList.add(0,response.body()?.data!!.filename)
                                adapter?.notifyDataSetChanged()
                            }

                        }else{
                            toast(response?.body()?.msg+"")
                        }
                    }

                })
    }
    /**
     * 获取默认标签
     */
    private fun getTag(){
        OkGo.get<BaseResponse<List<TagBean>>>(Constant.QUERY_TAG_LIST)
                .execute(object : JsonCallBack<BaseResponse<List<TagBean>>>(){
                    override fun onSuccess(response: Response<BaseResponse<List<TagBean>>>?) {
                        if(response?.body()?.res == 0){
                            tagAdapter?.setList(response.body()?.data!! as ArrayList<TagBean>)
                        }else{
                            toast(response?.body()?.msg+"")
                        }
                    }

                })
    }
    /**
     * 拼接地址
     */
    private fun getImagePath(list : ArrayList<String>):String{
        var stringBuffer = StringBuffer()
        for (item in list){
            if(item != "empty"){
                stringBuffer.append(item)
                stringBuffer.append(",")
            }
        }
        if(stringBuffer.isNotEmpty()){
            stringBuffer.deleteCharAt(stringBuffer.lastIndexOf(","))
        }
        return stringBuffer.toString()
    }
}