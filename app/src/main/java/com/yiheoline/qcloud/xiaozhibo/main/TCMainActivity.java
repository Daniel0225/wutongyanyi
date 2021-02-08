package com.yiheoline.qcloud.xiaozhibo.main;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.yiheoline.liteav.demo.lvb.liveroom.IMLVBLiveRoomListener;
import com.yiheoline.liteav.demo.lvb.liveroom.MLVBLiveRoom;
import com.yiheoline.liteav.demo.lvb.liveroom.roomutil.commondef.AnchorInfo;
import com.yiheoline.liteav.demo.lvb.liveroom.roomutil.commondef.AudienceInfo;
import com.yiheoline.liteav.demo.lvb.liveroom.roomutil.commondef.MLVBCommonDef;
import com.yiheoline.qcloud.xiaozhibo.base.BaseActivity;
import com.yiheoline.qcloud.xiaozhibo.common.utils.TCUtils;
import com.yiheoline.qcloud.xiaozhibo.homepage.HomePageFragment;
import com.yiheoline.qcloud.xiaozhibo.login.TCUserMgr;
import com.yiheoline.qcloud.xiaozhibo.profile.ProfileFragment;
import com.yiheoline.qcloud.xiaozhibo.show.ShowFragment;
import com.yiheoline.qcloud.xiaozhibo.video.VideoFragment;
import com.yiheoline.qcloud.xiaozhibo.widgets.NoAnimationViewPager;
import com.yiheonline.qcloud.xiaozhibo.R;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 *  Module:   TCMainActivity
 *
 *  Function: 主界面：直播列表、回放列表、个人信息页
 *
 */
public class TCMainActivity extends BaseActivity implements BottomNavigationView.OnNavigationItemSelectedListener {
    private static final String TAG = TCMainActivity.class.getSimpleName();
    private BottomNavigationView bottomNavigationView;
    private NoAnimationViewPager viewPager;
    private List<Fragment> fragments = new ArrayList();

    @Override
    protected void initView() {
        super.initView();
        bottomNavigationView = (BottomNavigationView)findViewById(R.id.bottomNavigationView);
        viewPager = (NoAnimationViewPager)findViewById(R.id.viewPager);
        initViews();

        if (Build.VERSION.SDK_INT >= 23) {
            int REQUEST_CODE_CONTACT = 101;
            String[] permissions = {Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.CAMERA};
            //验证是否许可权限
            for (String str : permissions) {
                if (this.checkSelfPermission(str) != PackageManager.PERMISSION_GRANTED) {
                    //申请权限
                    this.requestPermissions(permissions, REQUEST_CODE_CONTACT);
                    return;
                }
            }
        }
    }

    private void initViews(){
        if(!isTaskRoot()){
            if(getIntent() != null){
                String action = getIntent().getAction();
                if(getIntent().hasCategory(Intent.CATEGORY_LAUNCHER) && Intent.ACTION_MAIN == action){
                    finish();
                }
            }
        }
        bottomNavigationView.inflateMenu(R.menu.home_page_tab);
        bottomNavigationView.setOnNavigationItemSelectedListener(this);
        bottomNavigationView.setItemIconTintList(null);
        fragments.add(HomePageFragment.newInstance("1","2"));
        fragments.add(VideoFragment.newInstance("1","2"));
        fragments.add(ShowFragment.newInstance("1","2"));
        fragments.add(new ProfileFragment());
        viewPager.setAdapter(new ViewPageAdapter(getSupportFragmentManager()));
        viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener(){

            @Override
            public void onPageScrolled(int i, float v, int i1) {

            }

            @Override
            public void onPageSelected(int i) {
                bottomNavigationView.getMenu().getItem(i).setChecked(true);
            }

            @Override
            public void onPageScrollStateChanged(int i) {

            }
        });
    }

    @Override
    public int getLayoutId() {
        return R.layout.activity_main;
    }

    @NotNull
    @Override
    public String getLoggerTag() {
        return null;
    }

    class ViewPageAdapter extends FragmentPagerAdapter {
        public ViewPageAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int i) {
            return fragments.get(i);
        }

        @Override
        public int getCount() {
            return fragments.size();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // 如果您的 App 没有做判断是否账号重复的登录的逻辑
        // 那么 MLVB 会监听是否有同一个人登录，所以在 resume 的时候需要重新设置 MLVB Room 的监听
        MLVBLiveRoom.sharedInstance(this).setListener(new IMLVBLiveRoomListener() {
            @Override
            public void onError(int errCode, String errMsg, Bundle extraInfo) {
                if (errCode == MLVBCommonDef.LiveRoomErrorCode.ERROR_IM_FORCE_OFFLINE) {
                    TCUtils.showKickOut(TCMainActivity.this);
                }
            }

            @Override
            public void onWarning(int warningCode, String warningMsg, Bundle extraInfo) {

            }

            @Override
            public void onDebugLog(String log) {

            }

            @Override
            public void onRoomDestroy(String roomID) {

            }

            @Override
            public void onAnchorEnter(AnchorInfo anchorInfo) {

            }

            @Override
            public void onAnchorExit(AnchorInfo anchorInfo) {

            }

            @Override
            public void onAudienceEnter(AudienceInfo audienceInfo) {

            }

            @Override
            public void onAudienceExit(AudienceInfo audienceInfo) {

            }

            @Override
            public void onRequestJoinAnchor(AnchorInfo anchorInfo, String reason) {

            }

            @Override
            public void onKickoutJoinAnchor() {

            }

            @Override
            public void onRequestRoomPK(AnchorInfo anchorInfo) {

            }

            @Override
            public void onQuitRoomPK(AnchorInfo anchorInfo) {

            }

            @Override
            public void onRecvRoomTextMsg(String roomID, String userID, String userName, String userAvatar, String message) {

            }

            @Override
            public void onRecvRoomCustomMsg(String roomID, String userID, String userName, String userAvatar, String cmd, String message) {

            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (TextUtils.isEmpty(TCUserMgr.getInstance().getUserToken())) {
            if (TCUtils.isNetworkAvailable(this) && TCUserMgr.getInstance().hasUser()) {
                TCUserMgr.getInstance().autoLogin(null);
            }
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        switch (menuItem.getItemId()){
            case R.id.navigation_home:
                viewPager.setCurrentItem(0);
                break;
            case R.id.navigation_play:
                viewPager.setCurrentItem(1);
                break;
            case R.id.navigation_news:
                viewPager.setCurrentItem(2);
                break;
            case R.id.navigation_profile:
                viewPager.setCurrentItem(3);
                break;
        }
        return false;
    }

}
