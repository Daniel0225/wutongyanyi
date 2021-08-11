package com.yiheoline.qcloud.xiaozhibo.base

import android.content.Context
import android.content.pm.ActivityInfo
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import anet.channel.util.Utils.context
import com.umeng.message.PushAgent
import org.jetbrains.anko.AnkoLogger
import com.yiheoline.qcloud.xiaozhibo.utils.StatusBarUtil

abstract class BaseActivity : AppCompatActivity(),AnkoLogger {
    var savedInstanceState : Bundle? = null
    var mContext : Context? = null
    override fun onCreate(savedInstanceState1: Bundle?) {
        super.onCreate(savedInstanceState1)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        mContext = this
        savedInstanceState = savedInstanceState1
        setContentView(getLayoutId())
        PushAgent.getInstance(context).onAppStart()
        StatusBarUtil.setRootViewFitsSystemWindows(this,getFitSystemWindows())
        StatusBarUtil.setTranslucentStatus(this)
        if(!StatusBarUtil.setStatusBarDarkTheme(this,true)){
            StatusBarUtil.setStatusBarColor(this,0x55000000)
        }
        initData()
        initView()
        initToolbar()
        initListener()
    }

    /**
     * 获取布局文件
     */
    abstract fun getLayoutId() : Int

    /**
     * 初始化视图
     */
    open protected fun initView(){

    }

    /**
     * 获取fitSystemWindows
     */
    open protected fun getFitSystemWindows() : Boolean{
        return true
    }

    /**
     * 初始TollBar
     */
    open protected fun initToolbar(){

    }

    /**
     * 初始化监听
     */
    open protected fun initListener(){

    }

    /**
     * 初始化数据
     */
    open protected fun initData(){

    }

    override fun onResume() {
        super.onResume()
//        MobclickAgent.onResume(this)
    }

    override fun onPause() {
        super.onPause()
//        MobclickAgent.onPause(this)
    }
}