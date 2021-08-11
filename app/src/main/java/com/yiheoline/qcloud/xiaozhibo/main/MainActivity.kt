package com.yiheoline.qcloud.xiaozhibo.main

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.view.MenuItem
import android.view.View
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.ViewPager.OnPageChangeListener
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.lzy.okgo.OkGo
import com.lzy.okgo.callback.FileCallback
import com.lzy.okgo.model.Progress
import com.lzy.okgo.model.Response
import com.lzy.okgo.request.base.Request
import com.tencent.mmkv.MMKV
import com.yiheoline.liteav.demo.lvb.liveroom.IMLVBLiveRoomListener
import com.yiheoline.liteav.demo.lvb.liveroom.MLVBLiveRoom
import com.yiheoline.liteav.demo.lvb.liveroom.roomutil.commondef.AnchorInfo
import com.yiheoline.liteav.demo.lvb.liveroom.roomutil.commondef.AudienceInfo
import com.yiheoline.liteav.demo.lvb.liveroom.roomutil.commondef.MLVBCommonDef
import com.yiheoline.qcloud.xiaozhibo.Constant
import com.yiheoline.qcloud.xiaozhibo.base.BaseActivity
import com.yiheoline.qcloud.xiaozhibo.bean.VersionInfo
import com.yiheoline.qcloud.xiaozhibo.common.utils.TCUtils
import com.yiheoline.qcloud.xiaozhibo.dialog.UpdateDialog
import com.yiheoline.qcloud.xiaozhibo.homepage.HomePageFragment
import com.yiheoline.qcloud.xiaozhibo.http.BaseResponse
import com.yiheoline.qcloud.xiaozhibo.http.JsonCallBack
import com.yiheoline.qcloud.xiaozhibo.login.TCUserMgr
import com.yiheoline.qcloud.xiaozhibo.main.MainActivity
import com.yiheoline.qcloud.xiaozhibo.profile.ProfileFragment
import com.yiheoline.qcloud.xiaozhibo.show.ShowFragment
import com.yiheoline.qcloud.xiaozhibo.utils.TimeUtil
import com.yiheoline.qcloud.xiaozhibo.video.VideoFragment
import com.yiheoline.qcloud.xiaozhibo.widgets.NoAnimationViewPager
import com.yiheonline.qcloud.xiaozhibo.BuildConfig
import com.yiheonline.qcloud.xiaozhibo.R
import org.jetbrains.anko.toast
import java.io.File
import java.util.*

/**
 * Module:   TCMainActivity
 *
 * Function: 主界面：直播列表、回放列表、个人信息页
 *
 */
class MainActivity : BaseActivity(), BottomNavigationView.OnNavigationItemSelectedListener {
    private var bottomNavigationView: BottomNavigationView? = null
    private var viewPager: NoAnimationViewPager? = null
    private val fragments: MutableList<Fragment?> = ArrayList()

    override fun initView() {
        super.initView()
        bottomNavigationView = findViewById<View>(R.id.bottomNavigationView) as BottomNavigationView
        viewPager = findViewById<View>(R.id.viewPager) as NoAnimationViewPager
        initViews()
        if (Build.VERSION.SDK_INT >= 23) {
            val REQUEST_CODE_CONTACT = 101
            val permissions = arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA)
            //验证是否许可权限
            for (str in permissions) {
                if (checkSelfPermission(str) != PackageManager.PERMISSION_GRANTED) {
                    //申请权限
                    requestPermissions(permissions, REQUEST_CODE_CONTACT)
                    return
                }
            }
        }

        getUpdateInfo()
    }

    private fun initViews() {
        if (!isTaskRoot) {
            if (intent != null) {
                val action = intent.action
                if (intent.hasCategory(Intent.CATEGORY_LAUNCHER) && Intent.ACTION_MAIN === action) {
                    finish()
                }
            }
        }
        bottomNavigationView!!.inflateMenu(R.menu.home_page_tab)
        bottomNavigationView!!.setOnNavigationItemSelectedListener(this)
        bottomNavigationView!!.itemIconTintList = null
        fragments.add(HomePageFragment.newInstance("1", "2"))
        fragments.add(VideoFragment.newInstance("1", "2"))
        fragments.add(ShowFragment.newInstance("1", "2"))
        fragments.add(ProfileFragment())
        viewPager!!.adapter = ViewPageAdapter(supportFragmentManager)
        viewPager!!.setOnPageChangeListener(object : OnPageChangeListener {
            override fun onPageScrolled(i: Int, v: Float, i1: Int) {}
            override fun onPageSelected(i: Int) {
                bottomNavigationView!!.menu.getItem(i).isChecked = true
            }

            override fun onPageScrollStateChanged(i: Int) {}
        })
    }

    override fun getLayoutId(): Int {
        return R.layout.activity_main
    }

    internal inner class ViewPageAdapter(fm: FragmentManager?) : FragmentPagerAdapter(fm!!) {
        override fun getItem(i: Int): Fragment {
            return fragments[i]!!
        }

        override fun getCount(): Int {
            return fragments.size
        }
    }

    override fun onResume() {
        super.onResume()
        // 如果您的 App 没有做判断是否账号重复的登录的逻辑
        // 那么 MLVB 会监听是否有同一个人登录，所以在 resume 的时候需要重新设置 MLVB Room 的监听
        MLVBLiveRoom.sharedInstance(this).setListener(object : IMLVBLiveRoomListener {
            override fun onError(errCode: Int, errMsg: String, extraInfo: Bundle) {
                if (errCode == MLVBCommonDef.LiveRoomErrorCode.ERROR_IM_FORCE_OFFLINE) {
                    TCUtils.showKickOut(this@MainActivity)
                }
            }

            override fun onWarning(warningCode: Int, warningMsg: String, extraInfo: Bundle) {}
            override fun onDebugLog(log: String) {}
            override fun onRoomDestroy(roomID: String) {}
            override fun onAnchorEnter(anchorInfo: AnchorInfo) {}
            override fun onAnchorExit(anchorInfo: AnchorInfo) {}
            override fun onAudienceEnter(audienceInfo: AudienceInfo) {}
            override fun onAudienceExit(audienceInfo: AudienceInfo) {}
            override fun onRequestJoinAnchor(anchorInfo: AnchorInfo, reason: String) {}
            override fun onKickoutJoinAnchor() {}
            override fun onRequestRoomPK(anchorInfo: AnchorInfo) {}
            override fun onQuitRoomPK(anchorInfo: AnchorInfo) {}
            override fun onRecvRoomTextMsg(roomID: String, userID: String, userName: String, userAvatar: String, message: String) {}
            override fun onRecvRoomCustomMsg(roomID: String, userID: String, userName: String, userAvatar: String, cmd: String, message: String) {}
        })
    }

    override fun onStart() {
        super.onStart()
        if (TextUtils.isEmpty(TCUserMgr.getInstance().userToken)) {
            if (TCUtils.isNetworkAvailable(this) && TCUserMgr.getInstance().hasUser()) {
                TCUserMgr.getInstance().autoLogin(null)
            }
        }
    }

    override fun onNavigationItemSelected(menuItem: MenuItem): Boolean {
        when (menuItem.itemId) {
            R.id.navigation_home -> viewPager!!.currentItem = 0
            R.id.navigation_play -> viewPager!!.currentItem = 1
            R.id.navigation_news -> viewPager!!.currentItem = 2
            R.id.navigation_profile -> viewPager!!.currentItem = 3
        }
        return false
    }

    /**
     * 获取APP升级信息
     */
    private fun getUpdateInfo(){
        OkGo.get<BaseResponse<VersionInfo>>(Constant.APP_UPDATE)
                .execute(object : JsonCallBack<BaseResponse<VersionInfo>>(){
                    override fun onSuccess(response: Response<BaseResponse<VersionInfo>>?) {
                        if(response?.body()?.data != null && response?.body()?.res == 0){
                            updateInfo(response.body().data)
                        }
                    }
                })
    }

    var isDownload = false

    private fun updateInfo(versionInfo: VersionInfo){
        if(BuildConfig.VERSION_CODE <= versionInfo.minVersionCode){//低於設置的最低運行版本  則必須強制升級
            versionInfo.forceFlag = 1
        }
        if(BuildConfig.VERSION_CODE < versionInfo.versionCode){
            if(versionInfo.forceFlag == 1 || TimeUtil.isAnotherDay(MMKV.defaultMMKV().decodeLong("cancelUpdate",0))){
                UpdateDialog.showDialog(mContext!!,versionInfo,object : UpdateDialog.UpdateSelectListener{
                    override fun onUpdate(isUpdate: Boolean) {
                        if(isUpdate){
                            if(!isDownload){
                                toast("开始下载更新，请稍候")
                                downLoadApk(Constant.FILE_BASE+versionInfo.downloadUrl)
                            }
                        }else{
                            if(versionInfo.forceFlag == 1){
                                toast("您需要更新版本才能继续使用")
                                finish()
                            }else{
                                MMKV.defaultMMKV().encode("cancelUpdate",System.currentTimeMillis())
                            }
                        }
                    }

                })
            }

        }
    }

    private fun downLoadApk(url : String){
        OkGo.get<File>(url)
                .execute(object : FileCallback("$externalCacheDir/onLine","onLine.apk"){
                    override fun onStart(request: Request<File, out Request<Any, Request<*, *>>>?) {
                        super.onStart(request)
                        isDownload = true
                    }

                    override fun onFinish() {
                        isDownload = false
                    }

                    override fun onError(response: Response<File>?) {
                        toast("下载更新出错，请重试")
                    }

                    override fun downloadProgress(progress: Progress?) {
                        UpdateDialog.setProgress((progress?.fraction!! * 100).toInt())
                    }

                    override fun onSuccess(response: Response<File>?) {
                        installApk()
                    }

                })
    }

    private fun installApk(){

        try {
            var intent = Intent(Intent.ACTION_VIEW)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            var file = File("$externalCacheDir/onLine","onLine.apk")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                var apkUri = FileProvider.getUriForFile(mContext!!,"com.tencent.qcloud.xiaozhibo.fileprovider",file)
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                intent.setDataAndType(apkUri,"application/vnd.android.package-archive")
            }else{
                intent.setDataAndType(Uri.fromFile(file),"application/vnd.android.package-archive")
            }
            startActivity(intent)
        }catch (e:IllegalArgumentException){

        }
    }
}