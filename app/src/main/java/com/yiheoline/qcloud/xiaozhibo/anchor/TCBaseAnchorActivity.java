package com.yiheoline.qcloud.xiaozhibo.anchor;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.content.Intent;
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
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.model.HttpParams;
import com.lzy.okgo.model.Response;
import com.yiheoline.liteav.demo.lvb.liveroom.IMLVBLiveRoomListener;
import com.yiheoline.liteav.demo.lvb.liveroom.MLVBLiveRoom;
import com.yiheoline.liteav.demo.lvb.liveroom.MLVBLiveRoomImpl;
import com.yiheoline.liteav.demo.lvb.liveroom.roomutil.commondef.AnchorInfo;
import com.yiheoline.liteav.demo.lvb.liveroom.roomutil.commondef.AudienceInfo;
import com.yiheoline.liteav.demo.lvb.liveroom.roomutil.commondef.MLVBCommonDef;
import com.yiheoline.qcloud.xiaozhibo.Constant;
import com.yiheoline.qcloud.xiaozhibo.TCApplication;
import com.yiheoline.qcloud.xiaozhibo.TCGlobalConfig;
import com.yiheoline.qcloud.xiaozhibo.anim.AnimUtils;
import com.yiheoline.qcloud.xiaozhibo.anim.NumAnim;
import com.yiheoline.qcloud.xiaozhibo.bean.GiftBean;
import com.yiheoline.qcloud.xiaozhibo.bean.SendGiftBean;
import com.yiheoline.qcloud.xiaozhibo.bean.StartPlayBean;
import com.yiheoline.qcloud.xiaozhibo.bean.UpPlayInfoBean;
import com.yiheoline.qcloud.xiaozhibo.common.net.TCHTTPMgr;
import com.yiheoline.qcloud.xiaozhibo.common.report.TCELKReportMgr;
import com.yiheoline.qcloud.xiaozhibo.common.ui.ErrorDialogFragment;
import com.yiheoline.qcloud.xiaozhibo.common.utils.TCConstants;
import com.yiheoline.qcloud.xiaozhibo.common.utils.TCUtils;
import com.yiheoline.qcloud.xiaozhibo.common.widget.TCInputTextMsgDialog;
import com.yiheoline.qcloud.xiaozhibo.common.widget.TCSwipeAnimationController;
import com.yiheoline.qcloud.xiaozhibo.common.widget.danmaku.TCDanmuMgr;
import com.yiheoline.qcloud.xiaozhibo.common.widget.like.TCHeartLayout;
import com.yiheoline.qcloud.xiaozhibo.common.msg.TCChatEntity;
import com.yiheoline.qcloud.xiaozhibo.common.msg.TCChatMsgListAdapter;
import com.yiheoline.qcloud.xiaozhibo.common.msg.TCSimpleUserInfo;
import com.yiheoline.qcloud.xiaozhibo.http.BaseResponse;
import com.yiheoline.qcloud.xiaozhibo.http.JsonCallBack;
import com.yiheoline.qcloud.xiaozhibo.login.TCUserMgr;
import com.tencent.rtmp.TXLog;
import com.yiheoline.qcloud.xiaozhibo.utils.FastJsonUtil;
import com.yiheonline.qcloud.xiaozhibo.R;
import com.zhangyf.gift.RewardLayout;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import master.flame.danmaku.controller.IDanmakuView;


/**
 * Module:   TCBaseAnchorActivity
 * <p>
 * Function: ?????????????????????
 *
 * 1. MLVB ?????????????????????????????????????????????{@link TCBaseAnchorActivity#startPublish()}; ??????????????????????????????
 *
 * 2. ???????????????????????????????????????{@link TCBaseAnchorActivity#onRecvRoomTextMsg(String, String, String, String, String)}
 */
public class TCBaseAnchorActivity extends Activity implements IMLVBLiveRoomListener, View.OnClickListener, TCInputTextMsgDialog.OnTextSendListener {
    private static final String TAG = TCBaseAnchorActivity.class.getSimpleName();

    // ??????????????????
    private ListView                    mLvMessage;             // ????????????
    private TCInputTextMsgDialog        mInputTextMsgDialog;    // ???????????????
    private TCChatMsgListAdapter mChatMsgListAdapter;    // ???????????????Adapter
    private ArrayList<TCChatEntity>     mArrayListChatEntity;   // ????????????

    private ErrorDialogFragment mErrDlgFragment;        // ??????????????????
    private TCHeartLayout mHeartLayout;           // ?????????????????????

    protected TCSwipeAnimationController mTCSwipeAnimationController;  // ???????????????

    private String                      mTitle;                 // ????????????
    private String                      mCoverPicUrl;           // ???????????????
    private String                      mAvatarPicUrl;          // ??????????????????
    private String                      mNickName;              // ????????????
    private String                      mUserId;                // ????????????id
    protected long                      mTotalMemberCount = 0;  // ?????????????????????
    protected long                      mCurrentMemberCount = 0;// ??????????????????
    protected long                      mHeartCount = 0;        // ????????????

    private TCDanmuMgr mDanmuMgr;              // ???????????????

    protected MLVBLiveRoom              mLiveRoom;              // MLVB ?????????

    protected Handler mMainHandler = new Handler(Looper.getMainLooper());


    // ????????? Timer ?????????????????????
    private Timer                           mBroadcastTimer;        // ????????? Timer
    private BroadcastTimerTask              mBroadcastTimerTask;    // ????????????
    protected long                          mSecond = 0;            // ??????????????????????????????
    private long                            mStartPushPts;          // ?????????????????????????????? ELK ??????????????? ??????????????????
    private int noticeId = 0;//??????id
    private RewardLayout rewardLayout;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mStartPushPts = System.currentTimeMillis();
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        Intent intent = getIntent();
        mUserId = intent.getStringExtra(TCConstants.USER_ID);
        mTitle = intent.getStringExtra(TCConstants.ROOM_TITLE);
        mCoverPicUrl = intent.getStringExtra(TCConstants.COVER_PIC);
        mAvatarPicUrl = intent.getStringExtra(TCConstants.USER_HEADPIC);
        mNickName = intent.getStringExtra(TCConstants.USER_NICK);
        noticeId = getIntent().getIntExtra(TCConstants.NOTICE_ID,0);

        mArrayListChatEntity = new ArrayList<>();
        mErrDlgFragment = new ErrorDialogFragment();
        mLiveRoom = MLVBLiveRoom.sharedInstance(this);

        initView();
        if (TextUtils.isEmpty(mNickName)) {
            mNickName = mUserId;
        }
        mLiveRoom.setSelfProfile(mNickName, mAvatarPicUrl);
        startPublish();
        //?????????????????????
        rewardLayout = findViewById(R.id.giftContent);
        initGiftContent();
    }

    /**
     * ??????????????????????????? findViewById ????????????????????????
     * {@link TCCameraAnchorActivity}
     * ??????????????????id?????????????????? ???id?????????????????????id??????????????????
     */
    protected void initView() {
        RelativeLayout relativeLayout = (RelativeLayout) findViewById(R.id.rl_root);
        relativeLayout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return mTCSwipeAnimationController.processEvent(event);
            }
        });

        RelativeLayout controllLayer = (RelativeLayout) findViewById(R.id.anchor_rl_controllLayer);
        mTCSwipeAnimationController = new TCSwipeAnimationController(this);
        mTCSwipeAnimationController.setAnimationView(controllLayer);

        mLvMessage = (ListView) findViewById(R.id.im_msg_listview);
        mHeartLayout = (TCHeartLayout) findViewById(R.id.heart_layout);

        mInputTextMsgDialog = new TCInputTextMsgDialog(this, R.style.InputDialog);
        mInputTextMsgDialog.setmOnTextSendListener(this);

        mChatMsgListAdapter = new TCChatMsgListAdapter(this, mLvMessage, mArrayListChatEntity);
        mLvMessage.setAdapter(mChatMsgListAdapter);

        IDanmakuView danmakuView = (IDanmakuView) findViewById(R.id.anchor_danmaku_view);
        mDanmuMgr = new TCDanmuMgr(this);
        mDanmuMgr.setDanmakuView(danmakuView);
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
//                giftImage.setImageResource(bean.getGiftImg());
                Glide.with(TCBaseAnchorActivity.this).load(bean.getGiftImg()).into(giftImage);
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
//                giftImage.setImageResource(o.getGiftImg());
                Glide.with(TCBaseAnchorActivity.this).load(o.getGiftImg()).into(giftImage);
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
                Animation giftInAnim = AnimUtils.getInAnimation(TCBaseAnchorActivity.this);
                // ??????????????????
                Animation imgInAnim = AnimUtils.getInAnimation(TCBaseAnchorActivity.this);
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
                return AnimUtils.getOutAnimation(TCBaseAnchorActivity.this);
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

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_close:
                showExitInfoDialog("??????????????????????????????????????????", false);
                break;
            case R.id.btn_message_input:
                showInputMsgDialog();
                break;
            default:
                break;
        }
    }


    /**
     *     /////////////////////////////////////////////////////////////////////////////////
     *     //
     *     //                      Activity??????????????????
     *     //
     *     /////////////////////////////////////////////////////////////////////////////////
     */

    @Override
    public void onBackPressed() {
        showExitInfoDialog("??????????????????????????????????????????", false);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mDanmuMgr != null) {
            mDanmuMgr.resume();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mDanmuMgr != null) {
            mDanmuMgr.pause();
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopTimer();
        if (mDanmuMgr != null) {
            mDanmuMgr.destroy();
            mDanmuMgr = null;
        }
        stopPublish();
        long endPushPts = System.currentTimeMillis();
        long diff = (endPushPts - mStartPushPts) / 1000;
        TCELKReportMgr.getInstance().reportELK(TCConstants.ELK_ACTION_CAMERA_PUSH_DURATION, TCUserMgr.getInstance().getUserId(), diff, "?????????????????????", null);
    }

    /**
     *     /////////////////////////////////////////////////////////////////////////////////
     *     //
     *     //                      ???????????????????????????
     *     //
     *     /////////////////////////////////////////////////////////////////////////////////
     */
    protected void startPublish() {
        mLiveRoom.setListener(this);
        mLiveRoom.setCameraMuteImage(BitmapFactory.decodeResource(getResources(), R.drawable.pause_publish));
        String roomInfo = mTitle;
        try {
            roomInfo = new JSONObject()
                    .put("title", mTitle)
                    .put("frontcover", mCoverPicUrl)
                    .toString();
        } catch (JSONException e) {
            roomInfo = mTitle;
        }
        mLiveRoom.createRoom("", roomInfo, new IMLVBLiveRoomListener.CreateRoomCallback() {
            @Override
            public void onSuccess(String roomId) {
                Log.w(TAG, String.format("???????????????%s??????", roomId));
                onCreateRoomSuccess(roomId);
            }

            @Override
            public void onError(int errCode, String e) {
                Log.w(TAG, String.format("?????????????????????, code=%s,error=%s", errCode, e));
                showErrorAndQuit(errCode, "????????????????????????,Error:" + e);
            }
        });
    }

    /**
     * ?????????????????????
     * ????????????
     */
    protected void onCreateRoomSuccess(String roomId) {
        if(getIntent().hasExtra(TCConstants.NOTICE_ID)){
            startShow(roomId);
        }else{
            startShowSingle(roomId);
        }
    }

    /**
     * ??????????????????
     * ????????????
     */
    private void startShow(String roomId){
        StartPlayBean startPlayBean = new StartPlayBean();
        startPlayBean.setNoticeId(noticeId);
        startPlayBean.setRoomId(roomId);
        startPlayBean.setTitle(mTitle);
        OkGo.<BaseResponse<Integer>>post(Constant.START_LIVE)
                .upJson(FastJsonUtil.createJsonString(startPlayBean))
                .execute(new JsonCallBack<BaseResponse<Integer>>() {
                    @Override
                    public void onSuccess(Response<BaseResponse<Integer>> response) {
                        if(response.body().getRes() == 0){
                            startTimer();
                            TCApplication.Companion.setCurrentPlayId(response.body().data);
                        }else{
                            Toast.makeText(TCBaseAnchorActivity.this, "???????????????", Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    }
                });
    }

    /**
     * ????????????
     * ????????????
     */
    private void startShowSingle(String roomId){
        StartPlayBean startPlayBean = new StartPlayBean();
        startPlayBean.setCover(mCoverPicUrl);
        startPlayBean.setTitle(mTitle);
        startPlayBean.setRoomId(roomId);
        OkGo.<BaseResponse<Integer>>post(Constant.ARTIST_START)
                .upJson(FastJsonUtil.createJsonString(startPlayBean))
                .execute(new JsonCallBack<BaseResponse<Integer>>() {
                    @Override
                    public void onSuccess(Response<BaseResponse<Integer>> response) {
                        if(response.body().getRes() == 0){
                            startTimer();
                            TCApplication.Companion.setCurrentPlayId(response.body().data);
                        }else{
                            Toast.makeText(TCBaseAnchorActivity.this, "???????????????", Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    }
                });
    }



    protected void stopPublish() {
        mLiveRoom.exitRoom(new ExitRoomCallback() {
            @Override
            public void onSuccess() {
                Log.i(TAG, "exitRoom Success");
            }

            @Override
            public void onError(int errCode, String e) {
                Log.e(TAG, "exitRoom failed, errorCode = " + errCode + " errMessage = " + e);
            }
        });

        mLiveRoom.setListener(null);
    }

    /**
     *     /////////////////////////////////////////////////////////////////////////////////
     *     //
     *     //                      MLVB ????????????
     *     //
     *     /////////////////////////////////////////////////////////////////////////////////
     */
    @Override
    public void onAnchorEnter(AnchorInfo pusherInfo) {

    }

    @Override
    public void onAnchorExit(AnchorInfo pusherInfo) {

    }

    @Override
    public void onAudienceEnter(AudienceInfo audienceInfo) {

    }

    @Override
    public void onAudienceExit(AudienceInfo audienceInfo) {

    }

    @Override
    public void onRequestJoinAnchor(AnchorInfo pusherInfo, String reason) {

    }

    @Override
    public void onKickoutJoinAnchor() {

    }

    @Override
    public void onRequestRoomPK(AnchorInfo pusherInfo) {

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
                handleMemberJoinMsg(userInfo);
                break;
            case TCConstants.IMCMD_EXIT_LIVE:
                handleMemberQuitMsg(userInfo);
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
        TXLog.w(TAG, "room closed");
        showErrorAndQuit(0, "???????????????");
    }

    @Override
    public void onError(int errorCode, String errorMessage, Bundle extraInfo) {
        if (errorCode == MLVBCommonDef.LiveRoomErrorCode.ERROR_IM_FORCE_OFFLINE) {
            TCUtils.showKickOut(TCBaseAnchorActivity.this);
        } else {
            showErrorAndQuit(errorCode, errorMessage);
        }
    }

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
     *     //                      ??????????????????????????????
     *     //
     *     /////////////////////////////////////////////////////////////////////////////////
     */
    protected void handleTextMsg(TCSimpleUserInfo userInfo, String text) {
        TCChatEntity entity = new TCChatEntity();
        entity.setSenderName(userInfo.nickname);
        entity.setContent(text);
        entity.setType(TCConstants.TEXT_TYPE);
        notifyMsg(entity);
    }

    /**
     * ????????????????????????
     *
     * @param userInfo
     */
    protected void handleMemberJoinMsg(TCSimpleUserInfo userInfo) {
        mTotalMemberCount++;
        mCurrentMemberCount++;
        ((MLVBLiveRoomImpl)mLiveRoom).setTotalMemberCount(mTotalMemberCount);
        ((MLVBLiveRoomImpl)mLiveRoom).setCurrentMemberCount(mCurrentMemberCount);
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
     * ????????????????????????
     *
     * @param userInfo
     */
    protected void handleMemberQuitMsg(TCSimpleUserInfo userInfo) {
        if (mCurrentMemberCount > 0)
            mCurrentMemberCount--;
        else
            Log.d(TAG, "????????????????????????????????????????????????");
        ((MLVBLiveRoomImpl)mLiveRoom).setCurrentMemberCount(mCurrentMemberCount);
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
    protected void handlePraiseMsg(TCSimpleUserInfo userInfo) {
        TCChatEntity entity = new TCChatEntity();
        entity.setSenderName("??????");
        if (TextUtils.isEmpty(userInfo.nickname))
            entity.setContent(userInfo.userid + "????????????");
        else
            entity.setContent(userInfo.nickname + "????????????");

        mHeartLayout.addFavor();
        mHeartCount++;
        ((MLVBLiveRoomImpl)mLiveRoom).setHeartCount(mHeartCount);
        //todo?????????????????????
        entity.setType(TCConstants.PRAISE);
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
    protected void handleDanmuMsg(TCSimpleUserInfo userInfo, String text) {
        TCChatEntity entity = new TCChatEntity();
        entity.setSenderName(userInfo.nickname);
        entity.setContent(text);
        entity.setType(TCConstants.TEXT_TYPE);
        notifyMsg(entity);

        if (mDanmuMgr != null) {
            mDanmuMgr.addDanmu(userInfo.avatar, userInfo.nickname, text);
        }
    }


    /**
     *     /////////////////////////////////////////////////////////////////////////////////
     *     //
     *     //                      ??????????????????
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
        lp.width = (int) (display.getWidth()); //????????????
        mInputTextMsgDialog.getWindow().setAttributes(lp);
        mInputTextMsgDialog.setCancelable(true);
        mInputTextMsgDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        mInputTextMsgDialog.show();
    }


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

        TCChatEntity entity = new TCChatEntity();
        entity.setSenderName("???:");
        entity.setContent(msg);
        entity.setType(TCConstants.TEXT_TYPE);
        notifyMsg(entity);

        // ?????????????????????????????????
        if (danmuOpen) {
            if (mDanmuMgr != null) {
                mDanmuMgr.addDanmu(TCUserMgr.getInstance().getAvatar(), TCUserMgr.getInstance().getNickname(), msg);
            }
            mLiveRoom.sendRoomCustomMsg(String.valueOf(TCConstants.IMCMD_DANMU), msg, new SendRoomCustomMsgCallback() {
                @Override
                public void onError(int errCode, String errInfo) {
                    Log.w(TAG, "sendRoomDanmuMsg error: " + errInfo);
                }

                @Override
                public void onSuccess() {
                    Log.d(TAG, "sendRoomDanmuMsg success");
                }
            });
        } else {
            mLiveRoom.sendRoomTextMsg(msg, new SendRoomTextMsgCallback() {
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


    private void notifyMsg(final TCChatEntity entity) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mArrayListChatEntity.size() > 1000) {
                    while (mArrayListChatEntity.size() > 900) {
                        mArrayListChatEntity.remove(0);
                    }
                }
                mArrayListChatEntity.add(entity);
                mChatMsgListAdapter.notifyDataSetChanged();
            }
        });
    }


    /**
     *     /////////////////////////////////////////////////////////////////////////////////
     *     //
     *     //                      ????????????
     *     //
     *     /////////////////////////////////////////////////////////////////////////////////
     */

    /**
     * ???????????????????????????
     *
     * ???????????????????????????????????????????????????
     */
    protected void showPublishFinishDetailsDialog() {
        //?????????????????????detail
        FinishDetailDialogFragment dialogFragment = new FinishDetailDialogFragment();
        Bundle args = new Bundle();
        args.putString("time", TCUtils.formattedTime(mSecond));
        args.putString("heartCount", String.format(Locale.CHINA, "%d", mHeartCount));
        args.putString("totalMemberCount", String.format(Locale.CHINA, "%d", mTotalMemberCount));
        if(getIntent().hasExtra(TCConstants.NOTICE_ID)){
            upLoadPlayInfo(mHeartCount,mTotalMemberCount);
        }else{
            upLoadPlayInfoSingle();
        }
        dialogFragment.setArguments(args);
        dialogFragment.setCancelable(false);
        if (dialogFragment.isAdded())
            dialogFragment.dismiss();
        else
            dialogFragment.show(getFragmentManager(), "");
    }

    /**
     * ???????????? ??????????????????
     * ??????
     */
    private void upLoadPlayInfo(long likeCount,long totalCount){
        UpPlayInfoBean upPlayInfoBean = new UpPlayInfoBean();
        upPlayInfoBean.setLikeCount(likeCount);
        upPlayInfoBean.setWatchCount(totalCount);
        upPlayInfoBean.setNoticeId(noticeId);
        upPlayInfoBean.setTheaterLiveId(TCApplication.Companion.getCurrentPlayId());
        OkGo.<BaseResponse<String>>put(Constant.FINISH_PLAY)
                .upJson(FastJsonUtil.createJsonString(upPlayInfoBean))
                .execute(new JsonCallBack<BaseResponse<String>>() {
                    @Override
                    public void onSuccess(Response<BaseResponse<String>> response) {

                    }
                });
    }
    /**
     * ???????????? ??????????????????
     * ????????????
     */
    private void upLoadPlayInfoSingle(){
        UpPlayInfoBean upPlayInfoBean = new UpPlayInfoBean();
        upPlayInfoBean.setLikeCount(mHeartCount);
        upPlayInfoBean.setWatchCount(mTotalMemberCount);
        upPlayInfoBean.setArtistLiveId(TCApplication.Companion.getCurrentPlayId());
        upPlayInfoBean.setTheaterLiveId(TCApplication.Companion.getCurrentPlayId());
        OkGo.<BaseResponse<String>>put(Constant.ARTIST_FINISH)
                .upJson(FastJsonUtil.createJsonString(upPlayInfoBean))
                .execute(new JsonCallBack<BaseResponse<String>>() {
                    @Override
                    public void onSuccess(Response<BaseResponse<String>> response) {

                    }
                });
    }
    /**
     * ??????????????????
     *
     * @param msg     ????????????
     * @param isError true?????????????????????????????? false???????????????????????????????????????
     */
    public void showExitInfoDialog(String msg, Boolean isError) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.ConfirmDialogStyle);
        builder.setCancelable(true);
        builder.setTitle(msg);

        if (!isError) {
            builder.setPositiveButton("??????", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    stopPublish();
                    showPublishFinishDetailsDialog();
                }
            });
            builder.setNegativeButton("??????", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
        } else {
            //????????????????????????????????????????????????
            stopPublish();
            builder.setPositiveButton("??????", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    showPublishFinishDetailsDialog();
                }
            });
        }
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
        alertDialog.setCanceledOnTouchOutside(false);
    }

    /**
     * ???????????????????????????????????????
     *
     * @param errorCode
     * @param errorMsg
     */
    protected void showErrorAndQuit(int errorCode, String errorMsg) {
        stopTimer();
        stopPublish();
        if (!mErrDlgFragment.isAdded() && !this.isFinishing()) {
            Bundle args = new Bundle();
            args.putInt("errorCode", errorCode);
            args.putString("errorMsg", errorMsg);
            mErrDlgFragment.setArguments(args);
            mErrDlgFragment.setCancelable(false);
            FragmentTransaction transaction = getFragmentManager().beginTransaction();
            transaction.add(mErrDlgFragment, "loading");
            transaction.commitAllowingStateLoss();
        }
    }

    /**
     *     /////////////////////////////////////////////////////////////////////////////////
     *     //
     *     //                      ??????????????????
     *     //
     *     /////////////////////////////////////////////////////////////////////////////////
     */
    protected void onBroadcasterTimeUpdate(long second) {

    }

    /**
     * ?????????
     */
    private class BroadcastTimerTask extends TimerTask {
        public void run() {
            //Log.i(TAG, "timeTask ");
            ++mSecond;
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    onBroadcasterTimeUpdate(mSecond);
                }
            });
        }
    }

    private void startTimer() {
        //????????????
        if (mBroadcastTimer == null) {
            mBroadcastTimer = new Timer(true);
            mBroadcastTimerTask = new BroadcastTimerTask();
            mBroadcastTimer.schedule(mBroadcastTimerTask, 1000, 1000);
        }
    }

    private void stopTimer() {
        //????????????
        if (null != mBroadcastTimer) {
            mBroadcastTimerTask.cancel();
        }
    }

}
