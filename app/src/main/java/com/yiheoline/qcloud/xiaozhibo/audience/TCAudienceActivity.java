package com.yiheoline.qcloud.xiaozhibo.audience;

import android.app.Activity;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.model.Response;
import com.tencent.liteav.demo.beauty.view.BeautyPanel;
import com.tencent.liteav.demo.beauty.BeautyParams;
import com.yiheoline.liteav.demo.lvb.liveroom.IMLVBLiveRoomListener;
import com.yiheoline.liteav.demo.lvb.liveroom.MLVBLiveRoom;
import com.yiheoline.liteav.demo.lvb.liveroom.roomutil.commondef.AnchorInfo;
import com.yiheoline.liteav.demo.lvb.liveroom.roomutil.commondef.AudienceInfo;
import com.yiheoline.liteav.demo.lvb.liveroom.roomutil.commondef.MLVBCommonDef;
import com.yiheoline.qcloud.xiaozhibo.Constant;
import com.yiheoline.qcloud.xiaozhibo.TCApplication;
import com.yiheoline.qcloud.xiaozhibo.TCGlobalConfig;
import com.yiheoline.qcloud.xiaozhibo.anchor.TCBaseAnchorActivity;
import com.yiheoline.qcloud.xiaozhibo.anim.AnimUtils;
import com.yiheoline.qcloud.xiaozhibo.anim.NumAnim;
import com.yiheoline.qcloud.xiaozhibo.bean.GiftBean;
import com.yiheoline.qcloud.xiaozhibo.bean.GiftUpBean;
import com.yiheoline.qcloud.xiaozhibo.bean.SendGiftBean;
import com.yiheoline.qcloud.xiaozhibo.common.report.TCELKReportMgr;
import com.yiheoline.qcloud.xiaozhibo.common.ui.ErrorDialogFragment;
import com.yiheoline.qcloud.xiaozhibo.common.utils.TCConstants;
import com.yiheoline.qcloud.xiaozhibo.common.utils.TCUtils;
import com.yiheoline.qcloud.xiaozhibo.common.widget.TCInputTextMsgDialog;
import com.yiheoline.qcloud.xiaozhibo.common.widget.TCSwipeAnimationController;
import com.yiheoline.qcloud.xiaozhibo.common.widget.TCUserAvatarListAdapter;
import com.yiheoline.qcloud.xiaozhibo.common.widget.video.TCVideoView;
import com.yiheoline.qcloud.xiaozhibo.common.widget.video.TCVideoViewMgr;
import com.yiheoline.qcloud.xiaozhibo.common.widget.danmaku.TCDanmuMgr;
import com.yiheoline.qcloud.xiaozhibo.common.widget.like.TCHeartLayout;
import com.yiheoline.qcloud.xiaozhibo.common.msg.TCChatEntity;
import com.yiheoline.qcloud.xiaozhibo.common.msg.TCChatMsgListAdapter;
import com.yiheoline.qcloud.xiaozhibo.common.msg.TCSimpleUserInfo;
import com.yiheoline.qcloud.xiaozhibo.dialog.GiftDialog;
import com.yiheoline.qcloud.xiaozhibo.http.BaseResponse;
import com.yiheoline.qcloud.xiaozhibo.http.JsonCallBack;
import com.yiheoline.qcloud.xiaozhibo.login.TCUserMgr;
import com.yiheoline.qcloud.xiaozhibo.main.videolist.ui.TCVideoListFragment;
import com.tencent.rtmp.TXLiveConstants;
import com.tencent.rtmp.TXLog;
import com.tencent.rtmp.ui.TXCloudVideoView;
import com.yiheoline.qcloud.xiaozhibo.utils.FastJsonUtil;
import com.yiheonline.qcloud.xiaozhibo.R;
import com.zhangyf.gift.RewardLayout;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ThreadPoolExecutor;

import master.flame.danmaku.controller.IDanmakuView;

/**
 * Module:   TCAudienceActivity
 *
 * Function: ??????????????????
 *
 *
 * 1. MLVB ????????????????????????????????????{@link TCAudienceActivity#startPlay()} ??? {@link TCAudienceActivity#stopPlay()}
 *
 *
 * 3. ????????????????????????????????????
 *
 **/
public class TCAudienceActivity extends Activity implements IMLVBLiveRoomListener, View.OnClickListener, TCInputTextMsgDialog.OnTextSendListener {
    private static final String TAG = TCAudienceActivity.class.getSimpleName();
    //??????????????????
    private static final long                   LINK_MIC_INTERVAL = 3 * 1000;

    private Handler                             mHandler = new Handler(Looper.getMainLooper());

    private String mixedPlayUrl;//????????????
    private TXCloudVideoView                    mTXCloudVideoView;      // ?????????????????? View
    private MLVBLiveRoom                        mLiveRoom;              // MLVB ??????


    // ????????????
    private TCInputTextMsgDialog                mInputTextMsgDialog;    // ???????????????
    private ListView                            mListViewMsg;           // ??????????????????
    private ArrayList<TCChatEntity>             mArrayListChatEntity = new ArrayList<>(); // ??????????????????
    private TCChatMsgListAdapter                mChatMsgListAdapter;    // ???????????????Adapter

    private ImageView                           mIvAvatar;              // ??????????????????
    private TextView                            mTvPusherName;          // ??????????????????
    private TextView                            mMemberCount;           // ????????????????????????

    private String                              mPusherAvatar;          // ????????????????????????
    private long                                mCurrentAudienceCount;  // ??????????????????
    private long                                mHeartCount;            // ????????????

    private boolean                             mPlaying = false;       // ??????????????????
    private String                              mPusherNickname;        // ????????????
    private String                              mPusherId;              // ??????id
    private String                              mGroupId = "";          // ??????id
    private String                              mUserId = "";           // ??????id
    private String                              mNickname = "";         // ????????????
    private String                              mAvatar = "";           // ????????????
    private String                              mFileId = "";
    private String                              mTimeStamp = "";

    //??????????????????
    private RecyclerView mUserAvatarList;
    private TCUserAvatarListAdapter             mAvatarListAdapter;

    //????????????
    private TCHeartLayout                       mHeartLayout;

    //??????????????????
    private TCFrequeControl                     mLikeFrequeControl;

    //??????
    private TCDanmuMgr                          mDanmuMgr;
    private IDanmakuView                        mDanmuView;

    //????????????
    private RelativeLayout                      mControlLayer;
    private TCSwipeAnimationController          mTCSwipeAnimationController;
    private ImageView                           mBgImageView;

    //????????????
    private String                              mCoverUrl = "";
    private String                              mTitle = ""; //??????

    // ??????????????????
    private List<AnchorInfo>                    mPusherList = new ArrayList<>();    // ??????????????????
    private TCVideoViewMgr                      mVideoViewMgr;                      // ?????????????????????View?????????

    //??????
    private BeautyPanel                         mBeautyControl;

    private ErrorDialogFragment                 mErrDlgFragment = new ErrorDialogFragment();
    private long                                mStartPlayPts;

    private RewardLayout rewardLayout;
    private int liveId;//????????????id

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mStartPlayPts = System.currentTimeMillis();
        setTheme(R.style.BeautyTheme);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.activity_audience);

        Intent intent = getIntent();
        mixedPlayUrl = intent.getStringExtra(TCConstants.PLAY_URL);
        mPusherId = intent.getStringExtra(TCConstants.PUSHER_ID);
        mGroupId = intent.getStringExtra(TCConstants.GROUP_ID);
        mPusherNickname = intent.getStringExtra(TCConstants.PUSHER_NAME);
        mPusherAvatar = intent.getStringExtra(TCConstants.PUSHER_AVATAR);
        mHeartCount = Long.decode(intent.getStringExtra(TCConstants.HEART_COUNT));
        String count = intent.getStringExtra(TCConstants.MEMBER_COUNT);
        if(count.isEmpty()){
            mCurrentAudienceCount = 0L;
        }else{
            mCurrentAudienceCount = Long.decode(intent.getStringExtra(TCConstants.MEMBER_COUNT));
        }
        mFileId = intent.getStringExtra(TCConstants.FILE_ID);
        mTimeStamp = intent.getStringExtra(TCConstants.TIMESTAMP);
        mTitle = intent.getStringExtra(TCConstants.ROOM_TITLE);
        mUserId = TCApplication.Companion.getLoginInfo().getUserId();
        mNickname = TCApplication.Companion.getLoginInfo().getNickname();
        mAvatar = Constant.IMAGE_BASE+TCApplication.Companion.getLoginInfo().getAvatar();
        mCoverUrl = getIntent().getStringExtra(TCConstants.COVER_PIC);
        liveId = intent.getIntExtra("LIVE_ID",0);
        mVideoViewMgr = new TCVideoViewMgr(this, null);

        if (TextUtils.isEmpty(mNickname)) {
            mNickname = mUserId;
        }

        // ????????? MLVB ??????
        mLiveRoom = MLVBLiveRoom.sharedInstance(this);

        initView();
        mBeautyControl.setBeautyManager(mLiveRoom.getBeautyManager());
        startPlay();

        //?????????????????????
        rewardLayout = findViewById(R.id.giftContent);
        initGiftContent();
        getGiftList();
        //?????????????????????????????????????????????????????????sdk??????????????????????????????????????????????????????
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }


    private void initView() {
        RelativeLayout relativeLayout = (RelativeLayout) findViewById(R.id.audience_play_root);
        relativeLayout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return mTCSwipeAnimationController.processEvent(event);
            }
        });

        mControlLayer = (RelativeLayout) findViewById(R.id.anchor_rl_controllLayer);
        mTCSwipeAnimationController = new TCSwipeAnimationController(this);
        mTCSwipeAnimationController.setAnimationView(mControlLayer);

        mTXCloudVideoView = (TXCloudVideoView) findViewById(R.id.anchor_video_view);
        mTXCloudVideoView.setLogMargin(10, 10, 45, 55);
        mListViewMsg = (ListView) findViewById(R.id.im_msg_listview);
        mListViewMsg.setVisibility(View.VISIBLE);
        mHeartLayout = (TCHeartLayout) findViewById(R.id.heart_layout);
        mTvPusherName = (TextView) findViewById(R.id.anchor_tv_broadcasting_time);
        mTvPusherName.setText(TCUtils.getLimitString(mPusherNickname, 10));

        findViewById(R.id.anchor_iv_record_ball).setVisibility(View.GONE);

        mUserAvatarList = (RecyclerView) findViewById(R.id.anchor_rv_avatar);
        mUserAvatarList.setVisibility(View.VISIBLE);
        mAvatarListAdapter = new TCUserAvatarListAdapter(this, mPusherId);
        mUserAvatarList.setAdapter(mAvatarListAdapter);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        mUserAvatarList.setLayoutManager(linearLayoutManager);

        mInputTextMsgDialog = new TCInputTextMsgDialog(this, R.style.InputDialog);
        mInputTextMsgDialog.setmOnTextSendListener(this);

        mIvAvatar = (ImageView) findViewById(R.id.anchor_iv_head_icon);
        TCUtils.showPicWithUrl(this, mIvAvatar, mPusherAvatar, R.drawable.face);
        mMemberCount = (TextView) findViewById(R.id.anchor_tv_member_counts);

        mCurrentAudienceCount++;
        mMemberCount.setText(String.format(Locale.CHINA,"%d??????", mCurrentAudienceCount));
        mChatMsgListAdapter = new TCChatMsgListAdapter(this, mListViewMsg, mArrayListChatEntity);
        mListViewMsg.setAdapter(mChatMsgListAdapter);
        mDanmuView = (IDanmakuView) findViewById(R.id.anchor_danmaku_view);
        mDanmuView.setVisibility(View.VISIBLE);
        mDanmuMgr = new TCDanmuMgr(this);
        mDanmuMgr.setDanmakuView(mDanmuView);

        mBgImageView = (ImageView) findViewById(R.id.audience_background);
        mBgImageView.setScaleType(ImageView.ScaleType.CENTER_CROP);

        //????????????
        mBeautyControl = (BeautyPanel) findViewById(R.id.beauty_panel);

        TCUtils.blurBgPic(this, mBgImageView, mCoverUrl, R.drawable.bg);
    }

    /**
     * ?????????????????????
     */
    private void initGiftContent(){
        rewardLayout.setGiftAdapter(new RewardLayout.GiftAdapter<SendGiftBean>() {
            @Override
            public View onInit(View view, SendGiftBean bean) {
                ImageView giftImage = (ImageView) view.findViewById(R.id.iv_gift_img);
                final TextView giftNum = (TextView) view.findViewById(R.id.tv_gift_amount);
                TextView userName = (TextView) view.findViewById(R.id.tv_user_name);
                TextView giftName = (TextView) view.findViewById(R.id.tv_gift_name);

                // ???????????????
                giftNum.setText("x" + bean.getTheSendGiftSize());
                bean.setTheGiftCount(bean.getTheSendGiftSize());
                Glide.with(TCAudienceActivity.this).load(bean.getGiftImg()).into(giftImage);
                userName.setText(bean.getUserName());
                giftName.setText("?????? " + bean.getGiftName());
                return view;
            }

            @Override
            public View onUpdate(View view, SendGiftBean o, SendGiftBean t) {
                ImageView giftImage = (ImageView) view.findViewById(R.id.iv_gift_img);
                TextView giftNum = (TextView) view.findViewById(R.id.tv_gift_amount);

                int showNum = (Integer) o.getTheGiftCount() + o.getTheSendGiftSize();
                // ??????????????????giftview????????????
                giftNum.setText("x" + showNum);
                Glide.with(TCAudienceActivity.this).load(o.getGiftImg()).into(giftImage);
                // ??????????????????
                new NumAnim().start(giftNum);
                // ????????????????????????
                o.setTheGiftCount(showNum);
                // ???????????????????????????
//              o.setUserName(t.getUserName());
                return view;
            }

            @Override
            public void onKickEnd(SendGiftBean bean) {
                Log.e("zyfff", "onKickEnd:" + bean.getTheGiftId() + "," + bean.getGiftName() + "," + bean.getUserName() + "," + bean.getTheGiftCount());
            }

            @Override
            public void onComboEnd(SendGiftBean bean) {
//                Log.e("zyfff","onComboEnd:"+bean.getTheGiftId()+","+bean.getGiftName()+","+bean.getUserName()+","+bean.getTheGiftCount());
            }

            @Override
            public void addAnim(final View view) {
                final TextView textView = (TextView) view.findViewById(R.id.tv_gift_amount);
                ImageView img = (ImageView) view.findViewById(R.id.iv_gift_img);
                // ??????giftview??????
                Animation giftInAnim = AnimUtils.getInAnimation(TCAudienceActivity.this);
                // ??????????????????
                Animation imgInAnim = AnimUtils.getInAnimation(TCAudienceActivity.this);
                // ??????????????????
                final NumAnim comboAnim = new NumAnim();
                imgInAnim.setStartTime(500);
                imgInAnim.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {
                        textView.setVisibility(View.GONE);
                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        textView.setVisibility(View.VISIBLE);
                        comboAnim.start(textView);
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }
                });
                view.startAnimation(giftInAnim);
                img.startAnimation(imgInAnim);
            }

            @Override
            public AnimationSet outAnim() {
                return AnimUtils.getOutAnimation(TCAudienceActivity.this);
            }

            @Override
            public boolean checkUnique(SendGiftBean o, SendGiftBean t) {
                return o.getTheGiftId() == t.getTheGiftId() && o.getTheUserId() == t.getTheUserId();
            }


            @Override
            public SendGiftBean generateBean(SendGiftBean bean) {
                try {
                    return (SendGiftBean) bean.clone();
                } catch (CloneNotSupportedException e) {
                    e.printStackTrace();
                }
                return null;
            }
        });
    }

    /**
     * ????????? ??????????????????
     */
    private void sendGift(int position){
        GiftBean giftBean = giftBeanList.get(position);
        GiftUpBean giftUpBean = new GiftUpBean(giftBean.getId(),liveId);
        OkGo.<BaseResponse<String>>post(Constant.SEND_GIFT)
                .upJson(FastJsonUtil.createJsonString(giftUpBean))
                .execute(new JsonCallBack<BaseResponse<String>>() {
                    @Override
                    public void onSuccess(Response<BaseResponse<String>> response) {
                        if(response.body().getRes() == 0){
                            mLiveRoom.sendRoomCustomMsg(String.valueOf(TCConstants.IMCMD_GIFT),FastJsonUtil.createJsonString(giftBean) , null);
                        }else{
                            Toast.makeText(TCAudienceActivity.this,response.body().getMsg(),Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }
    private List<GiftBean> giftBeanList;
    /**
     * ??????????????????
     */
    private void getGiftList(){
        OkGo.<BaseResponse<List<GiftBean>>>get(Constant.GIFT_ALL)
                .execute(new JsonCallBack<BaseResponse<List<GiftBean>>>() {
                    @Override
                    public void onSuccess(Response<BaseResponse<List<GiftBean>>> response) {
                        if(response.body().getRes() == 0){
                            giftBeanList = response.body().data;
                        }else{

                        }
                    }
                });
    }

    /**
     *     /////////////////////////////////////////////////////////////////////////////////
     *     //
     *     //                      ??????????????????
     *     //
     *     /////////////////////////////////////////////////////////////////////////////////
     */
    @Override
    protected void onResume() {
        super.onResume();
        if (mDanmuMgr != null) {
            mDanmuMgr.resume();
        }
        if (rewardLayout != null) {
            rewardLayout.onResume();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (rewardLayout != null) {
            rewardLayout.onPause();
        }
        if (mDanmuMgr != null) {
            mDanmuMgr.pause();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (rewardLayout != null) {
            rewardLayout.onDestroy();
        }
        if (mDanmuMgr != null) {
            mDanmuMgr.destroy();
            mDanmuMgr = null;
        }

        stopPlay();

        mVideoViewMgr.recycleVideoView();
        mVideoViewMgr = null;

        hideNoticeToast();


        long endPushPts = System.currentTimeMillis();
        long diff = (endPushPts - mStartPlayPts) / 1000 ;
        TCELKReportMgr.getInstance().reportELK(TCConstants.ELK_ACTION_LIVE_PLAY_DURATION, TCUserMgr.getInstance().getUserId(), diff, "??????????????????", null);
    }

    /**
     *     /////////////////////////////////////////////////////////////////////////////////
     *     //
     *     //                      ???????????????????????????
     *     //
     *     /////////////////////////////////////////////////////////////////////////////////
     */
    private void startPlay() {
        if (mPlaying) return;
        mLiveRoom.setSelfProfile(mNickname, mAvatar);
        mLiveRoom.setListener(this);
        mLiveRoom.enterRoom(mGroupId,mixedPlayUrl, mTXCloudVideoView, new IMLVBLiveRoomListener.EnterRoomCallback() {
            @Override
            public void onError(int errCode, String errInfo) {
                showErrorAndQuit("?????????????????????Error:" + errCode);
                TCELKReportMgr.getInstance().reportELK(TCConstants.ELK_ACTION_LIVE_PLAY, TCUserMgr.getInstance().getUserId(), -10001, "??????LiveRoom??????", null);
            }

            @Override
            public void onSuccess() {
                mBgImageView.setVisibility(View.GONE);
                mLiveRoom.sendRoomCustomMsg(String.valueOf(TCConstants.IMCMD_ENTER_LIVE), "", null);
                TCELKReportMgr.getInstance().reportELK(TCConstants.ELK_ACTION_LIVE_PLAY, TCUserMgr.getInstance().getUserId(), 10000, "??????LiveRoom??????", null);
            }
        });
        mPlaying = true;
    }

    private void stopPlay() {
        if (mPlaying && mLiveRoom != null) {
            mLiveRoom.sendRoomCustomMsg(String.valueOf(TCConstants.IMCMD_EXIT_LIVE), "", null);
            mLiveRoom.exitRoom(new IMLVBLiveRoomListener.ExitRoomCallback() {
                @Override
                public void onError(int errCode, String errInfo) {
                    TXLog.w(TAG, "exit room error : "+errInfo);
                }

                @Override
                public void onSuccess() {
                    TXLog.d(TAG, "exit room success ");
                }
            });
            mPlaying = false;
            mLiveRoom.setListener(null);
        }
    }


    /**
     *     /////////////////////////////////////////////////////////////////////////////////
     *     //
     *     //                      MLVB ??????
     *     //
     *     /////////////////////////////////////////////////////////////////////////////////
     */

    @Override
    public void onAnchorEnter(final AnchorInfo pusherInfo) {
        if (pusherInfo == null || pusherInfo.userID == null) {
            return;
        }

        final TCVideoView videoView = mVideoViewMgr.applyVideoView(pusherInfo.userID);
        if (videoView == null) {
            return;
        }

        if (mPusherList != null) {
            boolean exist = false;
            for (AnchorInfo item: mPusherList) {
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
        mLiveRoom.startRemoteView(pusherInfo, videoView.videoView, new IMLVBLiveRoomListener.PlayCallback() {
            @Override
            public void onBegin() {
                videoView.stopLoading(false); //???????????????stopLoading ????????????????????????button
            }

            @Override
            public void onError(int errCode, String errInfo) {
                videoView.stopLoading(false);
                onDoAnchorExit(pusherInfo);
            }

            @Override
            public void onEvent(int event, Bundle param) {
                report(event);
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
        mVideoViewMgr.recycleVideoView(pusherInfo.userID);
    }

    /**
     * ????????????????????????
     *
     * @param audienceInfo ??????????????????
     */
    @Override
    public void onAudienceEnter(AudienceInfo audienceInfo) {

    }

    /**
     * ????????????????????????
     *
     * @param audienceInfo ??????????????????
     */
    @Override
    public void onAudienceExit(AudienceInfo audienceInfo) {

    }

    @Override
    public void onRequestJoinAnchor(AnchorInfo anchorInfo, String reason) {

    }

    @Override
    public void onKickoutJoinAnchor() {
        Toast.makeText(getApplicationContext(), "?????????????????????????????????",Toast.LENGTH_LONG).show();
    }

    @Override
    public void onRequestRoomPK(AnchorInfo anchorInfo) {

    }

    @Override
    public void onQuitRoomPK(AnchorInfo anchorInfo) {

    }

    @Override
    public void onRecvRoomTextMsg(String roomID, String userID, String userName, String userAvatar, String message) {
        TCSimpleUserInfo userInfo = new TCSimpleUserInfo(userID, userName, userAvatar);
        handleTextMsg(userInfo, message);
    }

    @Override
    public void onRecvRoomCustomMsg(String roomID, String userID, String userName, String userAvatar, String cmd, String message) {
        TCSimpleUserInfo userInfo = new TCSimpleUserInfo(userID, userName, userAvatar);
        int type = Integer.valueOf(cmd);
        switch (type) {
            case TCConstants.IMCMD_ENTER_LIVE:
                handleAudienceJoinMsg(userInfo);
                break;
            case TCConstants.IMCMD_EXIT_LIVE:
                handleAudienceQuitMsg(userInfo);
                break;
            case TCConstants.IMCMD_PRAISE:
                handlePraiseMsg(userInfo);
                break;
            case TCConstants.IMCMD_PAILN_TEXT:
                handleTextMsg(userInfo, message);
                break;
            case TCConstants.IMCMD_DANMU:
                handleDanmuMsg(userInfo, message);
                break;
            case TCConstants.IMCMD_GIFT:
                handleGiftMsg(userInfo,message);
                break;
            default:
                break;
        }
    }



    @Override
    public void onRoomDestroy(String roomID) {
        showErrorAndQuit("???????????????");
    }

    @Override
    public void onError(int errorCode, String errorMessage, Bundle extraInfo) {
        if (errorCode == MLVBCommonDef.LiveRoomErrorCode.ERROR_IM_FORCE_OFFLINE) { // IM ??????????????????
            TCUtils.showKickOut(TCAudienceActivity.this);
        } else {
            showErrorAndQuit("????????????????????????Error:");
        }
    }

    /**
     * ????????????
     *
     * @param warningCode ????????? TRTCWarningCode
     * @param warningMsg  ????????????
     * @param extraInfo   ???????????????????????????????????????????????????????????????????????????????????????
     */
    @Override
    public void onWarning(int warningCode, String warningMsg, Bundle extraInfo) {

    }

    @Override
    public void onDebugLog(String log) {
        Log.d(TAG, log);
    }


    /**
     *     /////////////////////////////////////////////////////////////////////////////////
     *     //
     *     //                      ?????????????????????????????????
     *     //
     *     /////////////////////////////////////////////////////////////////////////////////
     */

    /**
     * ??????????????????
     *
     * @param userInfo
     */
    public void handleAudienceJoinMsg(TCSimpleUserInfo userInfo) {
        //?????????????????? ??????false???????????????????????????????????????????????????
        if (!mAvatarListAdapter.addItem(userInfo))
            return;

        mCurrentAudienceCount++;
        mMemberCount.setText(String.format(Locale.CHINA,"%d", mCurrentAudienceCount));

        //?????????????????????????????????
        TCChatEntity entity = new TCChatEntity();
        entity.setSenderName("??????");
        if (TextUtils.isEmpty(userInfo.nickname))
            entity.setContent(userInfo.userid + "????????????");
        else
            entity.setContent(userInfo.nickname + "????????????");
        entity.setType(TCConstants.MEMBER_ENTER);
        notifyMsg(entity);
    }

    /**
     * ??????????????????
     *
     * @param userInfo
     */
    public void handleAudienceQuitMsg(TCSimpleUserInfo userInfo) {
        if(mCurrentAudienceCount > 0)
            mCurrentAudienceCount--;
        else
            Log.d(TAG, "????????????????????????????????????????????????");

        mMemberCount.setText(String.format(Locale.CHINA,"%d", mCurrentAudienceCount));

        mAvatarListAdapter.removeItem(userInfo.userid);

        TCChatEntity entity = new TCChatEntity();
        entity.setSenderName("??????");
        if (TextUtils.isEmpty(userInfo.nickname))
            entity.setContent(userInfo.userid + "????????????");
        else
            entity.setContent(userInfo.nickname + "????????????");
        entity.setType(TCConstants.MEMBER_EXIT);
        notifyMsg(entity);
    }

    /**
     * ??????????????????
     *
     * @param userInfo
     */
    public void handlePraiseMsg(TCSimpleUserInfo userInfo) {
        TCChatEntity entity = new TCChatEntity();

        entity.setSenderName("??????");
        if (TextUtils.isEmpty(userInfo.nickname))
            entity.setContent(userInfo.userid + "????????????");
        else
            entity.setContent(userInfo.nickname + "????????????");
        if (mHeartLayout != null) {
            mHeartLayout.addFavor();
        }
        mHeartCount++;

        entity.setType(TCConstants.MEMBER_ENTER);
        notifyMsg(entity);
    }

    /**
     * ??????????????????
     */
    public void handleGiftMsg(TCSimpleUserInfo userInfo,String message){
        String nickName = "";
        if (TextUtils.isEmpty(userInfo.nickname))
            nickName = userInfo.userid ;
        else
            nickName = userInfo.nickname;
        GiftBean giftBean = FastJsonUtil.getObject(message,GiftBean.class);
        SendGiftBean sendGiftBean = new SendGiftBean(Integer.parseInt(userInfo.userid),giftBean.getId(),nickName,
                giftBean.getName(),Constant.IMAGE_BASE+giftBean.getGiftLogo(),2700);
        rewardLayout.put(sendGiftBean);
    }

    /**
     * ??????????????????
     *
     * @param userInfo
     * @param text
     */
    public void handleDanmuMsg(TCSimpleUserInfo userInfo, String text) {
        handleTextMsg(userInfo, text);
        if (mDanmuMgr != null) {
            mDanmuMgr.addDanmu(userInfo.avatar, userInfo.nickname, text);
        }
    }

    /**
     * ??????????????????
     *
     * @param userInfo
     * @param text
     */
    public void handleTextMsg(TCSimpleUserInfo userInfo, String text) {
        TCChatEntity entity = new TCChatEntity();
        entity.setSenderName(userInfo.nickname);
        entity.setContent(text);
        entity.setType(TCConstants.TEXT_TYPE);

        notifyMsg(entity);
    }


    /**
     * ????????????????????????
     *
     * @param entity
     */
    private void notifyMsg(final TCChatEntity entity) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                if (mArrayListChatEntity.size() > 1000)
                {
                    while (mArrayListChatEntity.size() > 900)
                    {
                        mArrayListChatEntity.remove(0);
                    }
                }

                mArrayListChatEntity.add(entity);
                mChatMsgListAdapter.notifyDataSetChanged();
            }
        });
    }

    /**
     * ?????????????????????????????????
     *
     * @param errorMsg
     */
    private void showErrorAndQuit(String errorMsg) {
        stopPlay();

        Intent rstData = new Intent();
        rstData.putExtra(TCConstants.ACTIVITY_RESULT,errorMsg);
        setResult(TCVideoListFragment.START_LIVE_PLAY,rstData);

        if (!mErrDlgFragment.isAdded() && !this.isFinishing()) {
            Bundle args = new Bundle();
            args.putString("errorMsg", errorMsg);
            mErrDlgFragment.setArguments(args);
            mErrDlgFragment.setCancelable(false);

            //??????????????????.show(...)???????????????dialogfragment?????????IllegalStateException
            FragmentTransaction transaction = getFragmentManager().beginTransaction();
            transaction.add(mErrDlgFragment, "loading");
            transaction.commitAllowingStateLoss();
        }
    }


    /**
     *     /////////////////////////////////////////////////////////////////////////////////
     *     //
     *     //                       ????????????
     *     //
     *     /////////////////////////////////////////////////////////////////////////////////
     */
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_back:
                Intent rstData = new Intent();
                long memberCount = mCurrentAudienceCount - 1;
                rstData.putExtra(TCConstants.MEMBER_COUNT, memberCount>=0 ? memberCount:0);
                rstData.putExtra(TCConstants.HEART_COUNT, mHeartCount);
                rstData.putExtra(TCConstants.PUSHER_ID, mPusherId);
                setResult(0,rstData);
                stopPlay();
                finish();
                break;
            case R.id.btn_share:
                if (mHeartLayout != null) {
                    mHeartLayout.addFavor();
                }

                //????????????????????????
                if (mLikeFrequeControl == null) {
                    mLikeFrequeControl = new TCFrequeControl();
                    mLikeFrequeControl.init(2, 1);
                }
                if (mLikeFrequeControl.canTrigger()) {
                    mHeartCount++;
                    mLiveRoom.setCustomInfo(MLVBCommonDef.CustomFieldOp.INC, "praise", 1, null);
                    //???ChatRoom??????????????????
                    mLiveRoom.sendRoomCustomMsg(String.valueOf(TCConstants.IMCMD_PRAISE), "", null);
                }
                break;
            case R.id.btn_message_input:
                showInputMsgDialog();
                break;
            case R.id.btn_gift://????????????
                showGiftDialog();
                break;
            default:
                break;
        }
    }

    private void showGiftDialog() {

        GiftDialog.INSTANCE.onCreateDialog(TCAudienceActivity.this, giftBeanList,
                new GiftDialog.SelectGiftListener() {
                    @Override
                    public void select(int position) {
                        sendGift(position);
                    }
                });
    }


    /**
     *     /////////////////////////////////////////////////////////////////////////////////
     *     //
     *     //                       ???????????????????????????
     *     //
     *     /////////////////////////////////////////////////////////////////////////////////
     */

    /**
     * ??????????????????
     */
    private void showInputMsgDialog() {
        WindowManager windowManager = getWindowManager();
        Display display = windowManager.getDefaultDisplay();
        WindowManager.LayoutParams lp = mInputTextMsgDialog.getWindow().getAttributes();

        lp.width = (display.getWidth()); //????????????
        mInputTextMsgDialog.getWindow().setAttributes(lp);
        mInputTextMsgDialog.setCancelable(true);
        mInputTextMsgDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        mInputTextMsgDialog.show();
    }
    /**
     * TextInputDialog????????????
     * @param msg ????????????
     * @param danmuOpen ??????????????????
     */
    @Override
    public void onTextSend(String msg, boolean danmuOpen) {
        if (msg.length() == 0)
            return;
        try {
            byte[] byte_num = msg.getBytes("utf8");
            if (byte_num.length > 160) {
                Toast.makeText(this, "???????????????", Toast.LENGTH_SHORT).show();
                return;
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return;
        }

        //????????????
        TCChatEntity entity = new TCChatEntity();
        entity.setSenderName("???:");
        entity.setContent(msg);
        entity.setType(TCConstants.TEXT_TYPE);
        notifyMsg(entity);

        if (danmuOpen) {
            if (mDanmuMgr != null) {
                mDanmuMgr.addDanmu(mAvatar, mNickname, msg);
            }
            mLiveRoom.sendRoomCustomMsg(String.valueOf(TCConstants.IMCMD_DANMU), msg, new IMLVBLiveRoomListener.SendRoomCustomMsgCallback() {
                @Override
                public void onError(int errCode, String errInfo) {
                    Log.w(TAG, "sendRoomDanmuMsg error: "+errInfo);
                }

                @Override
                public void onSuccess() {
                    Log.d(TAG, "sendRoomDanmuMsg success");
                }
            });
        } else {
            mLiveRoom.sendRoomTextMsg(msg, new IMLVBLiveRoomListener.SendRoomTextMsgCallback() {
                @Override
                public void onError(int errCode, String errInfo) {
                    Log.d(TAG, "sendRoomTextMsg error:");
                }

                @Override
                public void onSuccess() {
                    Log.d(TAG, "sendRoomTextMsg success:");
                }
            });
        }
    }



    /**
     *     /////////////////////////////////////////////////////////////////////////////////
     *     //
     *     //                      ????????????
     *     //
     *     /////////////////////////////////////////////////////////////////////////////////
     */

    private Toast mNoticeToast;
    private Timer mNoticeTimer;

    private void showNoticeToast(String text) {
        if (mNoticeToast == null) {
            mNoticeToast = Toast.makeText(getApplicationContext(), text, Toast.LENGTH_LONG);
        }

        if (mNoticeTimer == null) {
            mNoticeTimer = new  Timer();
        }

        mNoticeToast.setText(text);
        mNoticeTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                mNoticeToast.show();
            }
        }, 0, 3000);

    }

    private void hideNoticeToast() {
        if (mNoticeToast != null) {
            mNoticeToast.cancel();
            mNoticeToast = null;
        }
        if (mNoticeTimer != null) {
            mNoticeTimer.cancel();
            mNoticeTimer = null;
        }
    }

    /**
     *     /////////////////////////////////////////////////////////////////////////////////
     *     //
     *     //                      ????????????
     *     //
     *     /////////////////////////////////////////////////////////////////////////////////
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 100:
                for (int ret : grantResults) {
                    if (ret != PackageManager.PERMISSION_GRANTED) {
                        return;
                    }
                }
                break;
            default:
                break;
        }
    }

    /**
     *     /////////////////////////////////////////////////////////////////////////////////
     *     //
     *     //                      ELK ????????????????????????????????????
     *     //
     *     /////////////////////////////////////////////////////////////////////////////////
     */
    /**
     * ?????????ELK????????????
     * @param event
     */
    private void report(int event) {
        switch (event) {
            case TXLiveConstants.PLAY_EVT_RCV_FIRST_I_FRAME :
                TCELKReportMgr.getInstance().reportELK(TCConstants.ELK_ACTION_LIVE_PLAY, TCUserMgr.getInstance().getUserId(), 0, "??????????????????", null);
                break;
            case TXLiveConstants.PLAY_ERR_NET_DISCONNECT :
                TCELKReportMgr.getInstance().reportELK(TCConstants.ELK_ACTION_LIVE_PLAY, TCUserMgr.getInstance().getUserId(), -1, "????????????,??????????????????????????????,??????????????????,?????????????????????????????????", null);
                break;
            case TXLiveConstants.PLAY_ERR_GET_RTMP_ACC_URL_FAIL :
                TCELKReportMgr.getInstance().reportELK(TCConstants.ELK_ACTION_LIVE_PLAY, TCUserMgr.getInstance().getUserId(), -2, "??????????????????????????????", null);
                break;
            case TXLiveConstants.PLAY_ERR_FILE_NOT_FOUND :
                TCELKReportMgr.getInstance().reportELK(TCConstants.ELK_ACTION_LIVE_PLAY, TCUserMgr.getInstance().getUserId(), -3, "?????????????????????", null);
                break;
            case TXLiveConstants.PLAY_ERR_HEVC_DECODE_FAIL :
                TCELKReportMgr.getInstance().reportELK(TCConstants.ELK_ACTION_LIVE_PLAY, TCUserMgr.getInstance().getUserId(), -4, "H265????????????", null);
                break;
            case TXLiveConstants.PLAY_ERR_HLS_KEY :
                TCELKReportMgr.getInstance().reportELK(TCConstants.ELK_ACTION_LIVE_PLAY, TCUserMgr.getInstance().getUserId(), -5, "HLS??????Key????????????", null);
                break;
            case TXLiveConstants.PLAY_ERR_GET_PLAYINFO_FAIL :
                TCELKReportMgr.getInstance().reportELK(TCConstants.ELK_ACTION_LIVE_PLAY, TCUserMgr.getInstance().getUserId(), -6, "??????????????????????????????", null);
                break;

        }
    }
}
