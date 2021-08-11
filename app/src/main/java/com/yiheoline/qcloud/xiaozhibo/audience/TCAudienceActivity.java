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
 * Function: 观众观看界面
 *
 *
 * 1. MLVB 观众开始和停止观看主播：{@link TCAudienceActivity#startPlay()} 和 {@link TCAudienceActivity#stopPlay()}
 *
 *
 * 3. 房间消息、弹幕、点赞处理
 *
 **/
public class TCAudienceActivity extends Activity implements IMLVBLiveRoomListener, View.OnClickListener, TCInputTextMsgDialog.OnTextSendListener {
    private static final String TAG = TCAudienceActivity.class.getSimpleName();
    //连麦间隔控制
    private static final long                   LINK_MIC_INTERVAL = 3 * 1000;

    private Handler                             mHandler = new Handler(Looper.getMainLooper());

    private String mixedPlayUrl;//直播地址
    private TXCloudVideoView                    mTXCloudVideoView;      // 观看大主播的 View
    private MLVBLiveRoom                        mLiveRoom;              // MLVB 组件


    // 消息相关
    private TCInputTextMsgDialog                mInputTextMsgDialog;    // 消息输入框
    private ListView                            mListViewMsg;           // 消息列表控件
    private ArrayList<TCChatEntity>             mArrayListChatEntity = new ArrayList<>(); // 消息列表集合
    private TCChatMsgListAdapter                mChatMsgListAdapter;    // 消息列表的Adapter

    private ImageView                           mIvAvatar;              // 主播头像控件
    private TextView                            mTvPusherName;          // 主播昵称控件
    private TextView                            mMemberCount;           // 当前观众数量控件

    private String                              mPusherAvatar;          // 主播头像连接地址
    private long                                mCurrentAudienceCount;  // 当前观众数量
    private long                                mHeartCount;            // 点赞数量

    private boolean                             mPlaying = false;       // 是否正在播放
    private String                              mPusherNickname;        // 主播昵称
    private String                              mPusherId;              // 主播id
    private String                              mGroupId = "";          // 房间id
    private String                              mUserId = "";           // 我的id
    private String                              mNickname = "";         // 我的昵称
    private String                              mAvatar = "";           // 我的头像
    private String                              mFileId = "";
    private String                              mTimeStamp = "";

    //头像列表控件
    private RecyclerView mUserAvatarList;
    private TCUserAvatarListAdapter             mAvatarListAdapter;

    //点赞动画
    private TCHeartLayout                       mHeartLayout;

    //点赞频率控制
    private TCFrequeControl                     mLikeFrequeControl;

    //弹幕
    private TCDanmuMgr                          mDanmuMgr;
    private IDanmakuView                        mDanmuView;

    //手势动画
    private RelativeLayout                      mControlLayer;
    private TCSwipeAnimationController          mTCSwipeAnimationController;
    private ImageView                           mBgImageView;

    //分享相关
    private String                              mCoverUrl = "";
    private String                              mTitle = ""; //标题

    // 麦上主播相关
    private List<AnchorInfo>                    mPusherList = new ArrayList<>();    // 麦上主播列表
    private TCVideoViewMgr                      mVideoViewMgr;                      // 主播对应的视频View管理类

    //美颜
    private BeautyPanel                         mBeautyControl;

    private ErrorDialogFragment                 mErrDlgFragment = new ErrorDialogFragment();
    private long                                mStartPlayPts;

    private RewardLayout rewardLayout;
    private int liveId;//直播场次id

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

        // 初始化 MLVB 组件
        mLiveRoom = MLVBLiveRoom.sharedInstance(this);

        initView();
        mBeautyControl.setBeautyManager(mLiveRoom.getBeautyManager());
        startPlay();

        //初始化礼物控件
        rewardLayout = findViewById(R.id.giftContent);
        initGiftContent();
        getGiftList();
        //在这里停留，让列表界面卡住几百毫秒，给sdk一点预加载的时间，形成秒开的视觉效果
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
        mMemberCount.setText(String.format(Locale.CHINA,"%d在线", mCurrentAudienceCount));
        mChatMsgListAdapter = new TCChatMsgListAdapter(this, mListViewMsg, mArrayListChatEntity);
        mListViewMsg.setAdapter(mChatMsgListAdapter);
        mDanmuView = (IDanmakuView) findViewById(R.id.anchor_danmaku_view);
        mDanmuView.setVisibility(View.VISIBLE);
        mDanmuMgr = new TCDanmuMgr(this);
        mDanmuMgr.setDanmakuView(mDanmuView);

        mBgImageView = (ImageView) findViewById(R.id.audience_background);
        mBgImageView.setScaleType(ImageView.ScaleType.CENTER_CROP);

        //美颜功能
        mBeautyControl = (BeautyPanel) findViewById(R.id.beauty_panel);

        TCUtils.blurBgPic(this, mBgImageView, mCoverUrl, R.drawable.bg);
    }

    /**
     * 初始化礼物控件
     */
    private void initGiftContent(){
        rewardLayout.setGiftAdapter(new RewardLayout.GiftAdapter<SendGiftBean>() {
            @Override
            public View onInit(View view, SendGiftBean bean) {
                ImageView giftImage = (ImageView) view.findViewById(R.id.iv_gift_img);
                final TextView giftNum = (TextView) view.findViewById(R.id.tv_gift_amount);
                TextView userName = (TextView) view.findViewById(R.id.tv_user_name);
                TextView giftName = (TextView) view.findViewById(R.id.tv_gift_name);

                // 初始化数据
                giftNum.setText("x" + bean.getTheSendGiftSize());
                bean.setTheGiftCount(bean.getTheSendGiftSize());
                Glide.with(TCAudienceActivity.this).load(bean.getGiftImg()).into(giftImage);
                userName.setText(bean.getUserName());
                giftName.setText("送出 " + bean.getGiftName());
                return view;
            }

            @Override
            public View onUpdate(View view, SendGiftBean o, SendGiftBean t) {
                ImageView giftImage = (ImageView) view.findViewById(R.id.iv_gift_img);
                TextView giftNum = (TextView) view.findViewById(R.id.tv_gift_amount);

                int showNum = (Integer) o.getTheGiftCount() + o.getTheSendGiftSize();
                // 刷新已存在的giftview界面数据
                giftNum.setText("x" + showNum);
                Glide.with(TCAudienceActivity.this).load(o.getGiftImg()).into(giftImage);
                // 数字刷新动画
                new NumAnim().start(giftNum);
                // 更新累计礼物数量
                o.setTheGiftCount(showNum);
                // 更新其它自定义字段
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
                // 整个giftview动画
                Animation giftInAnim = AnimUtils.getInAnimation(TCAudienceActivity.this);
                // 礼物图像动画
                Animation imgInAnim = AnimUtils.getInAnimation(TCAudienceActivity.this);
                // 首次连击动画
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
     * 送礼物 上报给服务器
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
     * 获取礼物列表
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
     *     //                      生命周期相关
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
        TCELKReportMgr.getInstance().reportELK(TCConstants.ELK_ACTION_LIVE_PLAY_DURATION, TCUserMgr.getInstance().getUserId(), diff, "直播播放时长", null);
    }

    /**
     *     /////////////////////////////////////////////////////////////////////////////////
     *     //
     *     //                      开始和停止播放相关
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
                showErrorAndQuit("加入房间失败，Error:" + errCode);
                TCELKReportMgr.getInstance().reportELK(TCConstants.ELK_ACTION_LIVE_PLAY, TCUserMgr.getInstance().getUserId(), -10001, "进入LiveRoom失败", null);
            }

            @Override
            public void onSuccess() {
                mBgImageView.setVisibility(View.GONE);
                mLiveRoom.sendRoomCustomMsg(String.valueOf(TCConstants.IMCMD_ENTER_LIVE), "", null);
                TCELKReportMgr.getInstance().reportELK(TCConstants.ELK_ACTION_LIVE_PLAY, TCUserMgr.getInstance().getUserId(), 10000, "进入LiveRoom成功", null);
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
     *     //                      MLVB 回调
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
                videoView.stopLoading(false); //推流成功，stopLoading 小主播隐藏踢人的button
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
        }); //开启远端视频渲染
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

        mLiveRoom.stopRemoteView(pusherInfo);//关闭远端视频渲染
        mVideoViewMgr.recycleVideoView(pusherInfo.userID);
    }

    /**
     * 收到观众进房通知
     *
     * @param audienceInfo 进房观众信息
     */
    @Override
    public void onAudienceEnter(AudienceInfo audienceInfo) {

    }

    /**
     * 收到观众退房通知
     *
     * @param audienceInfo 退房观众信息
     */
    @Override
    public void onAudienceExit(AudienceInfo audienceInfo) {

    }

    @Override
    public void onRequestJoinAnchor(AnchorInfo anchorInfo, String reason) {

    }

    @Override
    public void onKickoutJoinAnchor() {
        Toast.makeText(getApplicationContext(), "不好意思，您被主播踢开",Toast.LENGTH_LONG).show();
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
        showErrorAndQuit("直播已结束");
    }

    @Override
    public void onError(int errorCode, String errorMessage, Bundle extraInfo) {
        if (errorCode == MLVBCommonDef.LiveRoomErrorCode.ERROR_IM_FORCE_OFFLINE) { // IM 被强制下线。
            TCUtils.showKickOut(TCAudienceActivity.this);
        } else {
            showErrorAndQuit("视频流播放失败，Error:");
        }
    }

    /**
     * 警告回调
     *
     * @param warningCode 错误码 TRTCWarningCode
     * @param warningMsg  警告信息
     * @param extraInfo   额外信息，如警告发生的用户，一般不需要关注，默认是本地错误
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
     *     //                      接收到各类的消息的处理
     *     //
     *     /////////////////////////////////////////////////////////////////////////////////
     */

    /**
     * 观众进房消息
     *
     * @param userInfo
     */
    public void handleAudienceJoinMsg(TCSimpleUserInfo userInfo) {
        //更新头像列表 返回false表明已存在相同用户，将不会更新数据
        if (!mAvatarListAdapter.addItem(userInfo))
            return;

        mCurrentAudienceCount++;
        mMemberCount.setText(String.format(Locale.CHINA,"%d", mCurrentAudienceCount));

        //左下角显示用户加入消息
        TCChatEntity entity = new TCChatEntity();
        entity.setSenderName("通知");
        if (TextUtils.isEmpty(userInfo.nickname))
            entity.setContent(userInfo.userid + "加入直播");
        else
            entity.setContent(userInfo.nickname + "加入直播");
        entity.setType(TCConstants.MEMBER_ENTER);
        notifyMsg(entity);
    }

    /**
     * 观众退房消息
     *
     * @param userInfo
     */
    public void handleAudienceQuitMsg(TCSimpleUserInfo userInfo) {
        if(mCurrentAudienceCount > 0)
            mCurrentAudienceCount--;
        else
            Log.d(TAG, "接受多次退出请求，目前人数为负数");

        mMemberCount.setText(String.format(Locale.CHINA,"%d", mCurrentAudienceCount));

        mAvatarListAdapter.removeItem(userInfo.userid);

        TCChatEntity entity = new TCChatEntity();
        entity.setSenderName("通知");
        if (TextUtils.isEmpty(userInfo.nickname))
            entity.setContent(userInfo.userid + "退出直播");
        else
            entity.setContent(userInfo.nickname + "退出直播");
        entity.setType(TCConstants.MEMBER_EXIT);
        notifyMsg(entity);
    }

    /**
     * 收到点赞消息
     *
     * @param userInfo
     */
    public void handlePraiseMsg(TCSimpleUserInfo userInfo) {
        TCChatEntity entity = new TCChatEntity();

        entity.setSenderName("通知");
        if (TextUtils.isEmpty(userInfo.nickname))
            entity.setContent(userInfo.userid + "点了个赞");
        else
            entity.setContent(userInfo.nickname + "点了个赞");
        if (mHeartLayout != null) {
            mHeartLayout.addFavor();
        }
        mHeartCount++;

        entity.setType(TCConstants.MEMBER_ENTER);
        notifyMsg(entity);
    }

    /**
     * 收到礼物消息
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
     * 说到弹幕消息
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
     * 收到文本消息
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
     * 更新消息列表控件
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
     * 显示错误以及退出的弹窗
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

            //此处不使用用.show(...)的方式加载dialogfragment，避免IllegalStateException
            FragmentTransaction transaction = getFragmentManager().beginTransaction();
            transaction.add(mErrDlgFragment, "loading");
            transaction.commitAllowingStateLoss();
        }
    }


    /**
     *     /////////////////////////////////////////////////////////////////////////////////
     *     //
     *     //                       点击事件
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

                //点赞发送请求限制
                if (mLikeFrequeControl == null) {
                    mLikeFrequeControl = new TCFrequeControl();
                    mLikeFrequeControl.init(2, 1);
                }
                if (mLikeFrequeControl.canTrigger()) {
                    mHeartCount++;
                    mLiveRoom.setCustomInfo(MLVBCommonDef.CustomFieldOp.INC, "praise", 1, null);
                    //向ChatRoom发送点赞消息
                    mLiveRoom.sendRoomCustomMsg(String.valueOf(TCConstants.IMCMD_PRAISE), "", null);
                }
                break;
            case R.id.btn_message_input:
                showInputMsgDialog();
                break;
            case R.id.btn_gift://发送礼物
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
     *     //                       消息输入与发送相关
     *     //
     *     /////////////////////////////////////////////////////////////////////////////////
     */

    /**
     * 发消息弹出框
     */
    private void showInputMsgDialog() {
        WindowManager windowManager = getWindowManager();
        Display display = windowManager.getDefaultDisplay();
        WindowManager.LayoutParams lp = mInputTextMsgDialog.getWindow().getAttributes();

        lp.width = (display.getWidth()); //设置宽度
        mInputTextMsgDialog.getWindow().setAttributes(lp);
        mInputTextMsgDialog.setCancelable(true);
        mInputTextMsgDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        mInputTextMsgDialog.show();
    }
    /**
     * TextInputDialog发送回调
     * @param msg 文本信息
     * @param danmuOpen 是否打开弹幕
     */
    @Override
    public void onTextSend(String msg, boolean danmuOpen) {
        if (msg.length() == 0)
            return;
        try {
            byte[] byte_num = msg.getBytes("utf8");
            if (byte_num.length > 160) {
                Toast.makeText(this, "请输入内容", Toast.LENGTH_SHORT).show();
                return;
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return;
        }

        //消息回显
        TCChatEntity entity = new TCChatEntity();
        entity.setSenderName("我:");
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
     *     //                      弹窗消息
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
     *     //                      权限管理
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
     *     //                      ELK 数据上报（您可以不关心）
     *     //
     *     /////////////////////////////////////////////////////////////////////////////////
     */
    /**
     * 小直播ELK上报内容
     * @param event
     */
    private void report(int event) {
        switch (event) {
            case TXLiveConstants.PLAY_EVT_RCV_FIRST_I_FRAME :
                TCELKReportMgr.getInstance().reportELK(TCConstants.ELK_ACTION_LIVE_PLAY, TCUserMgr.getInstance().getUserId(), 0, "视频播放成功", null);
                break;
            case TXLiveConstants.PLAY_ERR_NET_DISCONNECT :
                TCELKReportMgr.getInstance().reportELK(TCConstants.ELK_ACTION_LIVE_PLAY, TCUserMgr.getInstance().getUserId(), -1, "网络断连,且经多次重连抢救无效,可以放弃治疗,更多重试请自行重启播放", null);
                break;
            case TXLiveConstants.PLAY_ERR_GET_RTMP_ACC_URL_FAIL :
                TCELKReportMgr.getInstance().reportELK(TCConstants.ELK_ACTION_LIVE_PLAY, TCUserMgr.getInstance().getUserId(), -2, "获取加速拉流地址失败", null);
                break;
            case TXLiveConstants.PLAY_ERR_FILE_NOT_FOUND :
                TCELKReportMgr.getInstance().reportELK(TCConstants.ELK_ACTION_LIVE_PLAY, TCUserMgr.getInstance().getUserId(), -3, "播放文件不存在", null);
                break;
            case TXLiveConstants.PLAY_ERR_HEVC_DECODE_FAIL :
                TCELKReportMgr.getInstance().reportELK(TCConstants.ELK_ACTION_LIVE_PLAY, TCUserMgr.getInstance().getUserId(), -4, "H265解码失败", null);
                break;
            case TXLiveConstants.PLAY_ERR_HLS_KEY :
                TCELKReportMgr.getInstance().reportELK(TCConstants.ELK_ACTION_LIVE_PLAY, TCUserMgr.getInstance().getUserId(), -5, "HLS解码Key获取失败", null);
                break;
            case TXLiveConstants.PLAY_ERR_GET_PLAYINFO_FAIL :
                TCELKReportMgr.getInstance().reportELK(TCConstants.ELK_ACTION_LIVE_PLAY, TCUserMgr.getInstance().getUserId(), -6, "获取点播文件信息失败", null);
                break;

        }
    }
}
