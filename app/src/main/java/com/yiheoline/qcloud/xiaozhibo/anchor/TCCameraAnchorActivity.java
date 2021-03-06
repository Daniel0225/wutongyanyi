package com.yiheoline.qcloud.xiaozhibo.anchor;

import android.animation.ObjectAnimator;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.tencent.liteav.audiosettingkit.AudioEffectPanel;
import com.tencent.liteav.demo.beauty.BeautyParams;
import com.tencent.liteav.demo.beauty.constant.BeautyConstants;
import com.tencent.liteav.demo.beauty.model.BeautyInfo;
import com.tencent.liteav.demo.beauty.model.ItemInfo;
import com.tencent.liteav.demo.beauty.model.TabInfo;
import com.tencent.liteav.demo.beauty.view.BeautyPanel;
import com.tencent.rtmp.ui.TXCloudVideoView;
import com.yiheoline.liteav.demo.lvb.liveroom.roomutil.commondef.AnchorInfo;
import com.yiheoline.qcloud.xiaozhibo.Constant;
import com.yiheoline.qcloud.xiaozhibo.TCApplication;
import com.yiheoline.qcloud.xiaozhibo.anchor.music.TCAudioControl;
import com.yiheoline.qcloud.xiaozhibo.common.msg.TCSimpleUserInfo;
import com.yiheoline.qcloud.xiaozhibo.common.report.TCELKReportMgr;
import com.yiheoline.qcloud.xiaozhibo.common.utils.TCConstants;
import com.yiheoline.qcloud.xiaozhibo.common.utils.TCUtils;
import com.yiheoline.qcloud.xiaozhibo.common.widget.TCUserAvatarListAdapter;
import com.yiheoline.qcloud.xiaozhibo.common.widget.video.TCVideoView;
import com.yiheoline.qcloud.xiaozhibo.common.widget.video.TCVideoViewMgr;
import com.yiheoline.qcloud.xiaozhibo.login.TCUserMgr;
import com.yiheonline.qcloud.xiaozhibo.R;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

/**
 * Module:   TCBaseAnchorActivity
 * <p>
 * Function: ?????????????????????
 * <p>
 *
 * 1. MLVB ???????????????????????????????????????{@link TCCameraAnchorActivity#onRequestJoinAnchor(AnchorInfo, String)}
 *
 * 2. MLVB ??????????????????????????????????????????{@link TCCameraAnchorActivity#onAnchorEnter(AnchorInfo)} {@link TCCameraAnchorActivity#onAnchorExit(AnchorInfo)}
 *
 * 3. ????????????????????? {@link TCAudioControl}
 *
 * 4. ????????????????????? {@link BeautyPanel}
 */
public class TCCameraAnchorActivity extends TCBaseAnchorActivity {
    private static final String TAG = TCCameraAnchorActivity.class.getSimpleName();

    private TXCloudVideoView                mTXCloudVideoView;      // ????????????????????? View
    private Button                          mFlashView;             // ???????????????

    // ????????????????????????
    private RecyclerView mUserAvatarList;        // ???????????????????????????
    private TCUserAvatarListAdapter mAvatarListAdapter;     // ??????????????? Adapter

    // ????????????
    private ImageView                       mHeadIcon;              // ????????????
    private ImageView                       mRecordBall;            // ??????????????????????????????
    private TextView                        mBroadcastTime;         // ?????????????????????
    private TextView                        mMemberCount;           // ????????????


    private AudioEffectPanel mPanelAudioControl;     // ????????????

    private BeautyPanel                     mBeautyControl;          // ????????????????????????
    private LinearLayout                    mLinearToolBar;

    // log??????
    private boolean                         mShowLog;               // ???????????? log ??????
    private boolean                         mFlashOn;               // ?????????????????????

    // ????????????
    private boolean                         mPendingRequest;        // ??????????????????????????????
    private TCVideoViewMgr mPlayerVideoViewList;   // ?????????????????????View
    private List<AnchorInfo>                mPusherList;            // ????????????????????????

    private ObjectAnimator                  mObjAnim;               // ??????



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.BeautyTheme);
        super.onCreate(savedInstanceState);
        TCELKReportMgr.getInstance().reportELK(TCConstants.ELK_ACTION_CAMERA_PUSH, TCUserMgr.getInstance().getUserId(), 0, "???????????????", null);
        mPusherList = new ArrayList<>();

        mBeautyControl.setBeautyManager(mLiveRoom.getBeautyManager());
        BeautyInfo beautyInfo = mBeautyControl.getDefaultBeautyInfo();
        beautyInfo.setBeautyBg(BeautyConstants.BEAUTY_BG_GRAY);
        mBeautyControl.setBeautyInfo(beautyInfo);


    }

    @Override
    protected void initView() {
        setContentView(R.layout.activity_camera_anchor);
        super.initView();
        mTXCloudVideoView = (TXCloudVideoView) findViewById(R.id.anchor_video_view);
        mTXCloudVideoView.setLogMargin(10, 10, 45, 55);

        mUserAvatarList = (RecyclerView) findViewById(R.id.anchor_rv_avatar);
        mAvatarListAdapter = new TCUserAvatarListAdapter(this, TCUserMgr.getInstance().getUserId());
        mUserAvatarList.setAdapter(mAvatarListAdapter);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        mUserAvatarList.setLayoutManager(linearLayoutManager);

        mFlashView = (Button) findViewById(R.id.anchor_btn_flash);

        mBroadcastTime = (TextView) findViewById(R.id.anchor_tv_broadcasting_time);
        mBroadcastTime.setText(String.format(Locale.US, "%s", "00:00:00"));
        mRecordBall = (ImageView) findViewById(R.id.anchor_iv_record_ball);

        mHeadIcon = (ImageView) findViewById(R.id.anchor_iv_head_icon);
        showHeadIcon(mHeadIcon, Constant.IMAGE_BASE+ TCApplication.Companion.getLoginInfo().getAvatar());
        mMemberCount = (TextView) findViewById(R.id.anchor_tv_member_counts);
        mMemberCount.setText("0");

        mLinearToolBar = (LinearLayout) findViewById(R.id.tool_bar);

        //AudioEffectPanel
        mPanelAudioControl = (AudioEffectPanel) findViewById(R.id.anchor_audio_control);
        mPanelAudioControl.setAudioEffectManager(mLiveRoom.getAudioEffectManager());
        mPanelAudioControl.setBackgroundColor(getResources().getColor(R.color.audio_gray_color));
        mPanelAudioControl.setOnAudioEffectPanelHideListener(new AudioEffectPanel.OnAudioEffectPanelHideListener() {
            @Override
            public void onClosePanel() {
                mPanelAudioControl.setVisibility(View.GONE);
                mLinearToolBar.setVisibility(View.VISIBLE);
            }
        });

        mBeautyControl = (BeautyPanel) findViewById(R.id.beauty_panel);
        mBeautyControl.setOnBeautyListener(new BeautyPanel.OnBeautyListener() {
            @Override
            public void onTabChange(TabInfo tabInfo, int position) {

            }

            @Override
            public boolean onClose() {
                mBeautyControl.setVisibility(View.GONE);
                mLinearToolBar.setVisibility(View.VISIBLE);
                return true;
            }

            @Override
            public boolean onClick(TabInfo tabInfo, int tabPosition, ItemInfo itemInfo, int itemPosition) {
                return false;
            }

            @Override
            public boolean onLevelChanged(TabInfo tabInfo, int tabPosition, ItemInfo itemInfo, int itemPosition, int beautyLevel) {
                return false;
            }
        });

        // ?????????????????????
        mPlayerVideoViewList = new TCVideoViewMgr(this, new TCVideoView.OnRoomViewListener() {
            @Override
            public void onKickUser(String userID) {
                if (userID != null) {
                    for (AnchorInfo item : mPusherList) {
                        if (userID.equalsIgnoreCase(item.userID)) {
                            onAnchorExit(item);
                            break;
                        }
                    }
                    mLiveRoom.kickoutJoinAnchor(userID);
                }
            }
        });
    }


    /**
     * ??????????????????
     *
     * @param view   view
     * @param avatar ????????????
     */
    private void showHeadIcon(ImageView view, String avatar) {
        TCUtils.showPicWithUrl(this, view, avatar, R.drawable.face);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();

        stopRecordAnimation();

        mPlayerVideoViewList.recycleVideoView();
        mPlayerVideoViewList = null;
        if (mMainHandler != null) {
            mMainHandler.removeCallbacksAndMessages(null);
        }
    }




    /**
     * /////////////////////////////////////////////////////////////////////////////////
     * //
     * //                      ???????????????????????????
     * //
     * /////////////////////////////////////////////////////////////////////////////////
     */

    @Override
    protected void startPublish() {
        mTXCloudVideoView.setVisibility(View.VISIBLE);

        // ???????????????????????????????????? View
        mLiveRoom.startLocalPreview(true, mTXCloudVideoView);
        // ??????????????????
        BeautyParams beautyParams = new BeautyParams();
        mLiveRoom.getBeautyManager().setBeautyStyle(beautyParams.mBeautyStyle);
        mLiveRoom.getBeautyManager().setBeautyLevel(beautyParams.mBeautyLevel);
        mLiveRoom.getBeautyManager().setWhitenessLevel(beautyParams.mWhiteLevel);
        mLiveRoom.getBeautyManager().setRuddyLevel(beautyParams.mRuddyLevel);
        // ??????????????????
        mLiveRoom.getBeautyManager().setFaceSlimLevel(beautyParams.mFaceSlimLevel);
        // ??????????????????
        mLiveRoom.getBeautyManager().setEyeScaleLevel(beautyParams.mBigEyeLevel);
        if (TCUtils.checkRecordPermission(this)) {
            super.startPublish();
        }
    }

    @Override
    protected void stopPublish() {
        super.stopPublish();
        if (mPanelAudioControl != null) {
            mPanelAudioControl.unInit();
            mPanelAudioControl = null;
        }
    }

    @Override
    protected void onCreateRoomSuccess(String roomId) {
        super.onCreateRoomSuccess(roomId);
        startRecordAnimation();
    }

    /**
     * /////////////////////////////////////////////////////////////////////////////////
     * //
     * //                      MLVB ????????????
     * //
     * /////////////////////////////////////////////////////////////////////////////////
     */
    @Override
    public void onAnchorEnter(final AnchorInfo pusherInfo) {
        if (pusherInfo == null || pusherInfo.userID == null) {
            return;
        }

        final TCVideoView videoView = mPlayerVideoViewList.applyVideoView(pusherInfo.userID);
        if (videoView == null) {
            return;
        }

        if (mPusherList != null) {
            boolean exist = false;
            for (AnchorInfo item : mPusherList) {
                if (pusherInfo.userID.equalsIgnoreCase(item.userID)) {
                    exist = true;
                    break;
                }
            }
            if (exist == false) {
                mPusherList.add(pusherInfo);
            }
        }

        videoView.startLoading();
        mLiveRoom.startRemoteView(pusherInfo, videoView.videoView, new PlayCallback() {
            @Override
            public void onBegin() {
                videoView.stopLoading(true); //???????????????stopLoading ???????????????????????????button
            }

            @Override
            public void onError(int errCode, String errInfo) {
                videoView.stopLoading(false);
                onDoAnchorExit(pusherInfo);
            }

            @Override
            public void onEvent(int event, Bundle param) {

            }
        }); //????????????????????????
    }

    @Override
    public void onAnchorExit(AnchorInfo pusherInfo) {
        onDoAnchorExit(pusherInfo);
    }

    private void onDoAnchorExit(AnchorInfo pusherInfo) {
        if (mPusherList != null) {
            Iterator<AnchorInfo> it = mPusherList.iterator();
            while (it.hasNext()) {
                AnchorInfo item = it.next();
                if (pusherInfo.userID.equalsIgnoreCase(item.userID)) {
                    it.remove();
                    break;
                }
            }
        }

        mLiveRoom.stopRemoteView(pusherInfo);//????????????????????????
        mPlayerVideoViewList.recycleVideoView(pusherInfo.userID);
    }

    @Override
    public void onRequestJoinAnchor(final AnchorInfo pusherInfo, String reason) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setCancelable(true)
                .setTitle("??????")
                .setMessage(pusherInfo.userName + "????????????????????????")
                .setPositiveButton("??????", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mLiveRoom.responseJoinAnchor(pusherInfo.userID, true, "");
                        dialog.dismiss();
                        mPendingRequest = false;
                    }
                })
                .setNegativeButton("??????", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mLiveRoom.responseJoinAnchor(pusherInfo.userID, false, "?????????????????????????????????");
                        dialog.dismiss();
                        mPendingRequest = false;
                    }
                });

        mMainHandler.post(new Runnable() {
            @Override
            public void run() {
                if (mPendingRequest == true) {
                    mLiveRoom.responseJoinAnchor(pusherInfo.userID, false, "??????????????????????????????????????????????????????");
                    return;
                }

                if (mPusherList.size() >= 3) {
                    mLiveRoom.responseJoinAnchor(pusherInfo.userID, false, "???????????????????????????????????????");
                    return;
                }

                final AlertDialog alertDialog = builder.create();
                alertDialog.setCancelable(false);
                alertDialog.setCanceledOnTouchOutside(false);
                alertDialog.show();

                mPendingRequest = true;

                mMainHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        alertDialog.dismiss();
                        mPendingRequest = false;
                    }
                }, 10000);
            }
        });
    }

    /**
     * /////////////////////////////////////////////////////////////////////////////////
     * //
     * //                      ????????????????????????
     * //
     * /////////////////////////////////////////////////////////////////////////////////
     */

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (null != mPanelAudioControl && mPanelAudioControl.getVisibility() != View.GONE && ev.getRawY() < mPanelAudioControl.getTop()) {
            mPanelAudioControl.setVisibility(View.GONE);
            mPanelAudioControl.hideAudioPanel();
            mLinearToolBar.setVisibility(View.VISIBLE);
        }
        return super.dispatchTouchEvent(ev);
    }


    /**
     *     /////////////////////////////////////////////////////////////////////////////////
     *     //
     *     //                      ???????????????????????????
     *     //
     *     /////////////////////////////////////////////////////////////////////////////////
     */
    /**
     * ???????????????????????????
     */
    private void startRecordAnimation() {
        mObjAnim = ObjectAnimator.ofFloat(mRecordBall, "alpha", 1f, 0f, 1f);
        mObjAnim.setDuration(1000);
        mObjAnim.setRepeatCount(-1);
        mObjAnim.start();
    }

    /**
     * ???????????????????????????
     */
    private void stopRecordAnimation() {
        if (null != mObjAnim)
            mObjAnim.cancel();
    }

    @Override
    protected void onBroadcasterTimeUpdate(long second) {
        super.onBroadcasterTimeUpdate(second);
        if (!mTCSwipeAnimationController.isMoving())
            mBroadcastTime.setText(TCUtils.formattedTime(second));
    }

    /**
     * /////////////////////////////////////////////////////////////////////////////////
     * //
     * //                      ?????????????????????????????????
     * //
     * /////////////////////////////////////////////////////////////////////////////////
     */
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.switch_cam:
                if (mLiveRoom != null) {
                    mLiveRoom.switchCamera();
                }
                break;
            case R.id.anchor_btn_flash:
                if (mLiveRoom == null || !mLiveRoom.enableTorch(!mFlashOn)) {
                    Toast.makeText(getApplicationContext(), "?????????????????????", Toast.LENGTH_SHORT).show();
                    return;
                }
                mFlashOn = !mFlashOn;
                mFlashView.setBackgroundDrawable(mFlashOn ?
                        getResources().getDrawable(R.drawable.flash_on) :
                        getResources().getDrawable(R.drawable.flash_off));

                break;
            case R.id.beauty_btn:
                if (mBeautyControl.isShown()) {
                    mBeautyControl.setVisibility(View.GONE);
                    mLinearToolBar.setVisibility(View.VISIBLE);
                } else {
                    mBeautyControl.setVisibility(View.VISIBLE);
                    mLinearToolBar.setVisibility(View.GONE);
                }
                break;
            case R.id.btn_close:
                showExitInfoDialog("??????????????????????????????????????????", false);
                break;
            case R.id.btn_audio_ctrl:
                if (mPanelAudioControl.isShown()) {
                    mPanelAudioControl.setVisibility(View.GONE);
                    mPanelAudioControl.hideAudioPanel();
                    mLinearToolBar.setVisibility(View.VISIBLE);
                } else {
                    mPanelAudioControl.setVisibility(View.VISIBLE);
                    mPanelAudioControl.showAudioPanel();
                    mLinearToolBar.setVisibility(View.GONE);
                }
                break;
            default:
                super.onClick(v);
                break;
        }
    }


    @Override
    protected void showErrorAndQuit(int errorCode, String errorMsg) {
        stopRecordAnimation();
        super.showErrorAndQuit(errorCode, errorMsg);
    }


    /**
     * /////////////////////////////////////////////////////////////////////////////////
     * //
     * //                      ?????????????????????????????????
     * //
     * /////////////////////////////////////////////////////////////////////////////////
     */
    @Override
    protected void handleMemberJoinMsg(TCSimpleUserInfo userInfo) {
        //?????????????????? ??????false???????????????????????????????????????????????????
        if (mAvatarListAdapter.addItem(userInfo))
            super.handleMemberJoinMsg(userInfo);
        mMemberCount.setText(String.format(Locale.CHINA, "%d", mCurrentMemberCount));
    }

    @Override
    protected void handleMemberQuitMsg(TCSimpleUserInfo userInfo) {
        mAvatarListAdapter.removeItem(userInfo.userid);
        super.handleMemberQuitMsg(userInfo);
        mMemberCount.setText(String.format(Locale.CHINA, "%d", mCurrentMemberCount));
    }


    /**
     * /////////////////////////////////////////////////////////////////////////////////
     * //
     * //                      ????????????
     * //
     * /////////////////////////////////////////////////////////////////////////////////
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 100:
                for (int ret : grantResults) {
                    if (ret != PackageManager.PERMISSION_GRANTED) {
                        showErrorAndQuit(-1314, "??????????????????");
                        return;
                    }
                }
                this.startPublish();
                break;
            default:
                break;
        }
    }
}
