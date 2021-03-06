package com.yiheoline.liteav.demo.lvb.liveroom;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.util.Log;
import android.view.View;

import androidx.annotation.Nullable;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.model.Response;
import com.tencent.imsdk.TIMUserProfile;
import com.tencent.imsdk.TIMValueCallBack;
import com.tencent.liteav.audio.TXAudioEffectManager;
import com.tencent.liteav.basic.log.TXCLog;
import com.tencent.liteav.beauty.TXBeautyManager;
import com.yiheoline.liteav.demo.lvb.liveroom.roomutil.commondef.AnchorInfo;
import com.yiheoline.liteav.demo.lvb.liveroom.roomutil.commondef.AudienceInfo;
import com.yiheoline.liteav.demo.lvb.liveroom.roomutil.commondef.LoginInfo;
import com.yiheoline.liteav.demo.lvb.liveroom.roomutil.commondef.MLVBCommonDef;
import com.yiheoline.liteav.demo.lvb.liveroom.roomutil.commondef.RoomInfo;
import com.yiheoline.liteav.demo.lvb.liveroom.roomutil.http.HttpRequests;
import com.yiheoline.liteav.demo.lvb.liveroom.roomutil.http.HttpResponse;
import com.yiheoline.liteav.demo.lvb.liveroom.roomutil.im.IMMessageMgr;
import com.tencent.rtmp.ITXLivePlayListener;
import com.tencent.rtmp.ITXLivePushListener;
import com.tencent.rtmp.TXLiveConstants;
import com.tencent.rtmp.TXLivePlayConfig;
import com.tencent.rtmp.TXLivePlayer;
import com.tencent.rtmp.TXLivePushConfig;
import com.tencent.rtmp.TXLivePusher;
import com.tencent.rtmp.ui.TXCloudVideoView;
import com.yiheoline.qcloud.xiaozhibo.Constant;
import com.yiheoline.qcloud.xiaozhibo.TCApplication;
import com.yiheoline.qcloud.xiaozhibo.bean.HeartBean;
import com.yiheoline.qcloud.xiaozhibo.http.BaseResponse;
import com.yiheoline.qcloud.xiaozhibo.http.JsonCallBack;
import com.yiheoline.qcloud.xiaozhibo.http.response.CreateRoomResponse;
import com.yiheoline.qcloud.xiaozhibo.utils.FastJsonUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

public class MLVBLiveRoomImpl extends MLVBLiveRoom implements HttpRequests.HeartBeatCallback, IMMessageMgr.IMMessageListener {

    protected static final String TAG = MLVBLiveRoomImpl.class.getName();
    protected static final int              LIVEROOM_ROLE_NONE      = 0;
    protected static final int              LIVEROOM_ROLE_PUSHER    = 1;
    protected static final int              LIVEROOM_ROLE_PLAYER    = 2;

    protected static MLVBLiveRoomImpl       mInstance = null;
    protected static final String           mServerDomain = "https://liveroom.qcloud.com/weapp/live_room"; //RoomService????????????

    protected Context                       mAppContext = null;
    protected IMLVBLiveRoomListener         mListener = null;
    protected int                           mSelfRoleType           = LIVEROOM_ROLE_NONE;

    protected boolean                       mJoinPusher             = false;

    protected boolean                       mBackground             = false;

    protected TXLivePlayer                  mTXLivePlayer;

    protected TXLivePlayConfig              mTXLivePlayConfig;
    protected Handler                       mListenerHandler = null;
    protected HttpRequests                  mHttpRequest = null;           //HTTP CGI????????????
    protected IMMessageMgr                  mIMMessageMgr;          //IM SDK??????
    protected LoginInfo mSelfAccountInfo;
    protected StreamMixturer                mStreamMixturer; //?????????
    protected HeartBeatThread               mHeartBeatThread;       //??????
    protected String                        mCurrRoomID;
    protected int                           mRoomStatusCode = 0;
    protected ArrayList<RoomInfo>           mRoomList = new ArrayList<>();
    protected TXLivePusher mTXLivePusher;
    protected TXLivePushListenerImpl        mTXLivePushListener;
    protected String                        mSelfPushUrl;
    protected String                        mSelfAccelerateURL;
    protected HashMap<String, PlayerItem>   mPlayers = new LinkedHashMap<>();
    protected HashMap<String, AnchorInfo>   mPushers = new LinkedHashMap<>();
    private IMLVBLiveRoomListener.RequestJoinAnchorCallback mJoinAnchorCallback;
    private Runnable                        mJoinAnchorTimeoutTask;
    private IMLVBLiveRoomListener.RequestRoomPKCallback mRequestPKCallback = null;
    private Runnable                        mRequestPKTimeoutTask = null;
    private AnchorInfo                      mPKAnchorInfo = null;

    //????????????????????????
    private static final int                MAX_MEMBER_SIZE = 20;
    //?????????????????????????????????????????????????????????????????????????????????
    private static final int                REFRESH_AUDIENCE_INTERVAL_MS = 2000;
    private long                            mLastEnterAudienceTimeMS = 0;
    private long                            mLastExitAudienceTimeMS = 0;
    //????????????
    private LinkedHashMap<String/*userID*/, AudienceInfo> mAudiences = null;


    private static final int                LIVEROOM_CAMERA_PREVIEW = 0;
    private static final int                LIVEROOM_SCREEN_PREVIEW = 1;
    private int                             mPreviewType = LIVEROOM_CAMERA_PREVIEW;

    protected boolean                       mScreenAutoEnable       = true;
    private boolean                         mHasAddAnchor = false;

    private static final int                STREAM_MIX_MODE_JOIN_ANCHOR = 0;
    private static final int                STREAM_MIX_MODE_PK = 1;
    private int                             mMixMode = STREAM_MIX_MODE_JOIN_ANCHOR;

    private long                            mTimeDiff = 0; //????????????????????????????????????????????????PK??????????????????

    private long mTotalMemberCount = 0;  // ?????????????????????
    private long mCurrentMemberCount = 0;// ??????????????????
    private long mHeartCount = 0;        // ????????????

    public static MLVBLiveRoom sharedInstance(Context context) {
        synchronized (MLVBLiveRoomImpl.class) {
            if (mInstance == null) {
                mInstance = new MLVBLiveRoomImpl(context);
            }
            return mInstance;
        }
    }

    public static void destroySharedInstance() {
        synchronized (MLVBLiveRoomImpl.class) {
            if (mInstance != null) {
                mInstance.destroy();
                mInstance = null;
            }
        }
    }

    /**
     * ??????????????????
     *
     * ??????????????? IMLVBLiveRoomListener ?????? MLVBLiveRoom ?????????????????????
     *
     * @param listener ????????????
     * @note ???????????? Main Thread????????????????????????????????????????????????????????? {@link MLVBLiveRoom#setListenerHandler(Handler)}
     */
    @Override
    public void setListener(IMLVBLiveRoomListener listener) {
        TXCLog.i(TAG, "API -> setListener");
        mListener = listener;
    }

    /**
     * ???????????????????????????
     *
     * @param listenerHandler ??????
     */
    @Override
    public void setListenerHandler(Handler listenerHandler) {
        TXCLog.i(TAG, "API -> setListenerHandler");
        if (listenerHandler != null) {
            mListenerHandler = listenerHandler;
        } else {
            mListenerHandler = new Handler(mAppContext.getMainLooper());
        }
    }

    /**
     * ??????
     *
     * @param loginInfo ????????????
     * @param callback  ??????????????????
     * @see {@link IMLVBLiveRoomListener.LoginCallback}
     */
    @Override
    public void login(final LoginInfo loginInfo, final IMLVBLiveRoomListener.LoginCallback callback) {
        TXCLog.i(TAG, "API -> login:" + loginInfo.sdkAppID + ":" + loginInfo.userID + ":" + loginInfo.userName + ":" + loginInfo.userSig);
        mSelfAccountInfo = loginInfo;

        if (mHttpRequest != null) {
            mHttpRequest.cancelAllRequests();
        }
        mHttpRequest = new HttpRequests(mServerDomain);
        mHttpRequest.setHeartBeatCallback(this);

        if (mIMMessageMgr == null) {
            mIMMessageMgr = new IMMessageMgr(mAppContext);
            mIMMessageMgr.setIMMessageListener(this);
        }

        //RoomService??????
        mHttpRequest.login(loginInfo.sdkAppID, loginInfo.userID, loginInfo.userSig, "Android", new HttpRequests.OnResponseCallback<HttpResponse.LoginResponse>() {
            @Override
            public void onResponse(final int retcode, final String retmsg, final HttpResponse.LoginResponse data) {
                if (retcode == 0) {
                    mTimeDiff = System.currentTimeMillis() - data.timestamp;
                    // ?????????IM SDK???????????????login
                    IMMessageMgr imMessageMgr = mIMMessageMgr;
                    if (imMessageMgr != null) {
                        imMessageMgr.initialize(mSelfAccountInfo.userID, mSelfAccountInfo.userSig, (int) mSelfAccountInfo.sdkAppID, new IMMessageMgr.Callback() {
                            @Override
                            public void onError(final int code, final String errInfo) {
                                String msg = "[IM] ???????????????[" + errInfo + ":" + code + "]";
                                TXCLog.e(TAG, msg);
                                callbackOnThread(mListener, msg);
                                callbackOnThread(callback, "onError", code, msg);
                            }

                            @Override
                            public void onSuccess(Object... args) {
                                //??????IM???????????????
                                String msg = String.format("[LiveRoom] ????????????, userID {%s}, userName {%s} " + "sdkAppID {%s}", mSelfAccountInfo.userID, mSelfAccountInfo.userName, mSelfAccountInfo.sdkAppID);
                                IMMessageMgr imMessageMgr = mIMMessageMgr;
                                if (imMessageMgr != null) {
                                    imMessageMgr.setSelfProfile(loginInfo.userName, loginInfo.userAvatar);
                                }
                                TXCLog.d(TAG, msg);
                                callbackOnThread(mListener, "onDebugLog", msg);
                                callbackOnThread(callback, "onSuccess");
                            }
                        });
                    }
                } else {
                    String msg = "[LiveRoom] RoomService????????????[" + retmsg + ":" + retcode + "]";
                    TXCLog.e(TAG, msg);
                    callbackOnThread(mListener, "onDebugLog", msg);
                    callbackOnThread(callback, "onError", retcode, msg);
                }
            }
        });
    }

    /**
     * ????????????
     */
    @Override
    public void logout() {
        TXCLog.i(TAG, "API -> logout");
        callbackOnThread(mListener, "onDebugLog", "[LiveRoom] ??????");
        if (mHttpRequest != null) {
            mHttpRequest.logout(new HttpRequests.OnResponseCallback<HttpResponse>() {
                @Override
                public void onResponse(int retcode, String retmsg, HttpResponse data) {
                    mHttpRequest.cancelAllRequests();
                }
            });
        }

        if (mIMMessageMgr != null) {
            mIMMessageMgr.setIMMessageListener(null);
            mIMMessageMgr.unInitialize();
            mIMMessageMgr = null;
        }

        mHeartBeatThread.stopHeartbeat();
    }

    /**
     * ??????????????????
     *
     * @param userName  ??????
     * @param avatarURL ????????????
     */
    @Override
    public void setSelfProfile(String userName, String avatarURL) {
        if (mSelfAccountInfo != null) {
            mSelfAccountInfo.userName = userName;
            mSelfAccountInfo.userAvatar = avatarURL;
        }
        IMMessageMgr imMessageMgr = mIMMessageMgr;
        if (imMessageMgr != null) {
            imMessageMgr.setSelfProfile(userName, avatarURL);
        }
    }

    /**
     * ??????????????????
     *
     * ??????????????????????????????????????????????????? index ??? count ??????????????????????????????????????????
     * - index = 0 & count = 10 ????????????????????????10????????????
     * - index = 11 & count = 10 ????????????????????????10????????????
     *
     * @param index    ????????????????????????0???????????????
     * @param count    ????????????????????????????????????
     * @param callback ????????????????????????????????????
     */
    @Override
    public void getRoomList(int index, int count, final IMLVBLiveRoomListener.GetRoomListCallback callback) {
        TXCLog.i(TAG, "API -> getRoomList:" + index + ":" + count);
        if (mHttpRequest == null && callback != null) {
            callbackOnThread(callback, "onError", MLVBCommonDef.LiveRoomErrorCode.ERROR_NOT_LOGIN, "[LiveRoom] getRoomList??????[Http ??????????????????????????????login]");
            return;
        }

        mHttpRequest.getRoomList(index, count, new HttpRequests.OnResponseCallback<HttpResponse.RoomList>() {
            @Override
            public void onResponse(final int retcode, final String retmsg, HttpResponse.RoomList data) {
                if (retcode != HttpResponse.CODE_OK || data == null || data.rooms == null){
                    callbackOnThread(callback, "onError", retcode, "[LiveRoom] getRoomList ??????[" + retmsg + "]");
                }else {
                    final ArrayList<RoomInfo> arrayList = new ArrayList<>(data.rooms.size());
                    arrayList.addAll(data.rooms);
                    mRoomList = arrayList;
                    callbackOnThread(callback, "onSuccess", arrayList);
                }
            }
        });
    }

    /**
     * ??????????????????
     *
     * ?????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????
     *
     * @param callback ????????????????????????????????????
     * @note ???????????????????????????30??????????????????????????? UI ?????????????????????????????????????????????????????????????????????????????????????????????????????????
     */
    @Override
    public void getAudienceList(final IMLVBLiveRoomListener.GetAudienceListCallback callback) {
        TXCLog.i(TAG, "API -> getAudienceList");
        if (mCurrRoomID == null || mCurrRoomID.length() > 0) {
            callbackOnThread(callback, "onError", MLVBCommonDef.LiveRoomErrorCode.ERROR_NOT_IN_ROOM, "[LiveRoom] getAudienceList ??????[???????????????]");
            return;
        }
        if (mAudiences != null) {
            final ArrayList<AudienceInfo> audienceList = new ArrayList<>();
            for (Map.Entry<String, AudienceInfo> item : mAudiences.entrySet()) {
                audienceList.add(item.getValue());
            }
            callbackOnThread(callback, "onSuccess", audienceList);
        } else {
            IMMessageMgr imMessageMgr = mIMMessageMgr;
            if (imMessageMgr != null) {
                imMessageMgr.getGroupMembers(mCurrRoomID, MAX_MEMBER_SIZE, new TIMValueCallBack<List<TIMUserProfile>>() {
                    @Override
                    public void onError(final int i, final String s) {
                        callbackOnThread(callback, "onError", i, "[IM] ?????????????????????[" + s + "]");
                    }

                    @Override
                    public void onSuccess(List<TIMUserProfile> timUserProfiles) {
                        for (TIMUserProfile userProfile : timUserProfiles) {
                            AudienceInfo audienceInfo = new AudienceInfo();
                            audienceInfo.userID = userProfile.getIdentifier();
                            audienceInfo.userName = userProfile.getNickName();
                            audienceInfo.userAvatar = userProfile.getFaceUrl();
                            mAudiences.put(userProfile.getIdentifier(), audienceInfo);
                        }

                        final ArrayList<AudienceInfo> audienceList = new ArrayList<>();
                        for (Map.Entry<String, AudienceInfo> item : mAudiences.entrySet()) {
                            audienceList.add(item.getValue());
                        }
                        callbackOnThread(callback, "onSuccess", audienceList);
                    }
                });
            }
        }
    }

    /**
     * ??????????????????????????????
     *
     * ???????????????????????????????????????
     * 1.?????????????????? startLocalPreview() ?????????????????????????????????????????????????????????
     * 2.?????????????????? createRoom ??????????????????????????????????????????????????? {@link IMLVBLiveRoomListener.CreateRoomCallback} ??????????????????
     *
     * @param roomID   ?????????????????????????????????????????? userID ??????????????? roomID??????????????????????????????????????????room ID ???????????????????????????????????????
     * @param roomInfo ????????????????????????????????????????????????????????????????????????????????????????????? JSON ???????????????????????????
     * @param callback ???????????????????????????
     */
    @Override
    public void createRoom(final String roomID, final String roomInfo, final IMLVBLiveRoomListener.CreateRoomCallback callback) {
        TXCLog.i(TAG, "API -> createRoom:" + roomID + ":" + roomInfo);
        mSelfRoleType = LIVEROOM_ROLE_PUSHER;

        if (mSelfAccountInfo == null) return;
        //1. ??????????????????startLocalPreview?????????????????????
        mHttpRequest.getPushUrl(mSelfAccountInfo.userID, roomID, new HttpRequests.OnResponseCallback<HttpResponse.PushUrl>() {
            @Override
            public void onResponse(int retcode, String retmsg, HttpResponse.PushUrl data) {
                if (retcode == HttpResponse.CODE_OK && data != null && data.pushURL != null) {
                    final String pushURL = data.pushURL;
                    mSelfPushUrl = data.pushURL;
                    mSelfAccelerateURL = data.accelerateURL;

                    //3.????????????
                    startPushStream(pushURL, TXLiveConstants.VIDEO_QUALITY_HIGH_DEFINITION, new StandardCallback() {
                        @Override
                        public void onError(int errCode, String errInfo) {
                            callbackOnThread(callback, "onError", errCode, errInfo);
                        }

                        @Override
                        public void onSuccess() {
                            //???????????????????????????????????????PUSH_EVT_PUSH_BEGIN?????????onSuccess?????????????????????????????????????????????????????????????????????
                            if (mCurrRoomID != null && mCurrRoomID.length() > 0) {
                                return;
                            }

                            if (mTXLivePusher != null) {
                                TXLivePushConfig config = mTXLivePusher.getConfig();
                                config.setVideoEncodeGop(2);
                                mTXLivePusher.setConfig(config);
                            }

                            mBackground = false;
                            //4.?????????????????????CGI:create_room?????????roomID???roomSig
                            doCreateRoom(roomID, roomInfo, new StandardCallback() {
                                @Override
                                public void onError(int errCode, String errInfo) {
                                    callbackOnThread(callback, "onError", errCode, errInfo);
                                }

                                @Override
                                public void onSuccess() {

                                    //5.??????CGI???add_pusher???????????????
                                    addAnchor(mCurrRoomID, pushURL, new StandardCallback() {
                                        @Override
                                        public void onError(int errCode, String errInfo) {
                                            callbackOnThread(callback, "onError", errCode, errInfo);
                                        }

                                        @Override
                                        public void onSuccess() {
                                            //6.??????IM???
                                            createIMGroup(mCurrRoomID, mCurrRoomID, new StandardCallback() {
                                                @Override
                                                public void onError(int errCode, String errInfo) {
                                                    if (errCode == 10025) {
                                                        //?????? ID ????????????????????????????????????????????????????????????
                                                        Log.w(TAG, "[IM] ?????? " + mCurrRoomID + " ????????????????????????????????????????????????????????????");
                                                        mJoinPusher = true;
                                                        mHeartBeatThread.startHeartbeat(); //????????????
                                                        mStreamMixturer.setMainVideoStream(pushURL);
                                                        callbackOnThread(callback, "onSuccess", mCurrRoomID);
                                                    } else {
                                                        callbackOnThread(callback, "onError", errCode, errInfo);
                                                    }
                                                }

                                                @Override
                                                public void onSuccess() {
                                                    mJoinPusher = true;
                                                    mHeartBeatThread.startHeartbeat(); //????????????
                                                    mStreamMixturer.setMainVideoStream(pushURL);
                                                    callbackOnThread(callback, "onSuccess", mCurrRoomID);
                                                }
                                            });
                                        }
                                    });
                                }
                            });
                        }
                    });

                }
                else {
                    callbackOnThread(callback, "onError", retcode, "[LiveRoom] ??????????????????[????????????????????????]");
                }
            }
        });
    }

    /**
     * ??????????????????????????????
     *
     * ?????????????????????????????????????????????
     * 1.?????????????????? getRoomList() ????????????????????????????????????????????? {@link IMLVBLiveRoomListener.GetRoomListCallback} ???????????????????????????
     * 2.???????????????????????????????????????????????? enterRoom() ??????????????????
     *
     * @param roomID   ????????????
     * @param view     ???????????????????????????
     * @param callback ???????????????????????????
     */
    @Override
    public void enterRoom(final String roomID,final String mixedPlayUrl, final TXCloudVideoView view, final IMLVBLiveRoomListener.EnterRoomCallback callback) {
        TXCLog.i(TAG, "API -> enterRoom:" + roomID);
        if (roomID == null || roomID.length() == 0) {
            callbackOnThread(callback, "onError", MLVBCommonDef.LiveRoomErrorCode.ERROR_PARAMETERS_INVALID, "[LiveRoom] ????????????[???????????????]");
            return;
        }
        mSelfRoleType = LIVEROOM_ROLE_PLAYER;
        mCurrRoomID = roomID;

        //1.IM??????
        jionIMGroup(roomID, new StandardCallback() {
            @Override
            public void onError(int errCode, String errInfo) {
                callbackOnThread(callback, "onError", errCode, errInfo);
            }

            @Override
            public void onSuccess() {
                //2.??????????????????CDN???
                Handler handler = new Handler(mAppContext.getMainLooper());
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (view != null) {
                            view.setVisibility(View.VISIBLE);
                        }
//                        String mixedPlayUrl = getMixedPlayUrlByRoomID(roomID);
                        if (mixedPlayUrl != null && mixedPlayUrl.length() > 0) {
                            int playType = getPlayType(mixedPlayUrl);
                            mTXLivePlayer.setPlayerView(view);
                            mTXLivePlayer.startPlay(mixedPlayUrl, playType);

                            if (mHttpRequest != null) {
                                String userInfo = "";
                                try {
                                    userInfo = new JSONObject()
                                            .put("userName", mSelfAccountInfo.userName)
                                            .put("userAvatar", mSelfAccountInfo.userAvatar)
                                            .toString();
                                } catch (JSONException e) {
                                    userInfo = "";
                                }
                                mHttpRequest.addAudience(roomID, mSelfAccountInfo.userID, userInfo, null);
                            }
                            callbackOnThread(callback, "onSuccess");
                        } else {
                            callbackOnThread(callback, "onError", MLVBCommonDef.LiveRoomErrorCode.ERROR_PLAY, "[LiveRoom] ?????????CDN????????????");
                        }
                    }
                });
            }
        });
    }

    /**
     * ????????????
     *
     * @param callback ???????????????????????????
     */
    @Override
    public void exitRoom(IMLVBLiveRoomListener.ExitRoomCallback callback) {
        TXCLog.i(TAG, "API -> exitRoom");
        //1. ????????????
        mHeartBeatThread.stopHeartbeat();

        // ?????? BGM
        stopBGM();

        if (mSelfRoleType == LIVEROOM_ROLE_PUSHER) {
            //2. ?????????????????????????????????
            IMMessageMgr imMessageMgr = mIMMessageMgr;
            if (imMessageMgr != null) {
                imMessageMgr.destroyGroup(mCurrRoomID, new IMMessageMgr.Callback() {
                    @Override
                    public void onError(int code, String errInfo) {
                        TXCLog.e(TAG, "[IM] ???????????????:" + code + ":" + errInfo);
                    }

                    @Override
                    public void onSuccess(Object... args) {
                        TXCLog.d(TAG, "[IM] ???????????????");
                    }
                });
            }

        } else {
            //???????????????????????????
//            notifyPusherChange();

            //2. ??????IM???quitGroup
            IMMessageMgr imMessageMgr = mIMMessageMgr;
            if (imMessageMgr != null) {
                imMessageMgr.quitGroup(mCurrRoomID, new IMMessageMgr.Callback() {
                    @Override
                    public void onError(int code, String errInfo) {
                        TXCLog.e(TAG, "[IM] ????????????:" + code + ":" + errInfo);
                    }

                    @Override
                    public void onSuccess(Object... args) {
                        TXCLog.d(TAG, "[IM] ????????????");
                    }
                });
            }
        }

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                //3. ??????????????????
                if (mPreviewType == LIVEROOM_CAMERA_PREVIEW) {
                    stopLocalPreview();
                } else {
                    stopScreenCapture();
                }
                unInitLivePusher();

                //4. ??????????????????????????????
                cleanPlayers();

                //5. ?????????????????????
                if (mTXLivePlayer != null) {
                    mTXLivePlayer.stopPlay(true);
                    mTXLivePlayer.setPlayerView(null);
                }

                quitRoomPK(null);
            }
        };

        if (Looper.myLooper() != mAppContext.getMainLooper()) {
            Handler handler = new Handler(mAppContext.getMainLooper());
            handler.post(runnable);
        } else {
            runnable.run();
        }

        //6. ?????????????????????CGI:delete_pusher??????????????????????????????????????????
        if (mHasAddAnchor) {
            mHasAddAnchor = false;
            mHttpRequest.delPusher(mCurrRoomID, mSelfAccountInfo.userID, new HttpRequests.OnResponseCallback<HttpResponse>() {
                @Override
                public void onResponse(int retcode, String retmsg, HttpResponse data) {
                    if (retcode == HttpResponse.CODE_OK) {
                        TXCLog.d(TAG, "????????????");
                        callbackOnThread(mListener, "onDebugLog", "[LiveRoom] ????????????");
                    } else {
                        callbackOnThread(mListener, "onDebugLog", String.format("[LiveRoom] ???????????????%s(%d)", retmsg, retcode));
                        TXCLog.e(TAG, String.format("??????????????????%s(%d)", retmsg, retcode));
                    }
                }
            });
        }


        if (mSelfRoleType == LIVEROOM_ROLE_PLAYER && mHttpRequest != null) {
            mHttpRequest.delAudience(mCurrRoomID, mSelfAccountInfo.userID, null);
        }

        mJoinPusher = false;
        mSelfRoleType = LIVEROOM_ROLE_NONE;
        mCurrRoomID   = "";
        mPushers.clear();

        mStreamMixturer.resetMergeState();

        callbackOnThread(callback, "onSuccess");
    }

    /**
     * ?????????????????????
     *
     * ??????????????????????????????????????????????????????????????????????????????????????????????????????????????????
     *
     * @param op ?????????????????????????????? {@link MLVBCommonDef.CustomFieldOp}
     * @param key ????????????
     * @param value ??????
     *
     * @note op ??? {@link MLVBCommonDef.CustomFieldOp#SET} ??????value ????????? String ?????? Integer ??????
     *       op ??? {@link MLVBCommonDef.CustomFieldOp#INC} ??????value ??? Integer ??????
     *       op ??? {@link MLVBCommonDef.CustomFieldOp#DEC} ??????value ??? Integer ??????
     */
    public void setCustomInfo(final MLVBCommonDef.CustomFieldOp op, final String key, final Object value, final IMLVBLiveRoomListener.SetCustomInfoCallback callback) {
        TXCLog.i(TAG, "API -> setCustomInfo:" + op + ":" + key);
        if ((op == MLVBCommonDef.CustomFieldOp.SET && !((value instanceof String) || (value instanceof Integer)))
                || (op == MLVBCommonDef.CustomFieldOp.INC && !(value instanceof Integer))
                || (op == MLVBCommonDef.CustomFieldOp.DEC && !(value instanceof Integer))) {
            String msg = "[LiveRoom] setCustomInfo??????[op???value???????????????]";
            callbackOnThread(callback, "onError", MLVBCommonDef.LiveRoomErrorCode.ERROR_PARAMETERS_INVALID, msg);
            return;
        }
        String strOp = "";
        if (op == MLVBCommonDef.CustomFieldOp.SET) {
            strOp = "set";
        } else if (op == MLVBCommonDef.CustomFieldOp.INC) {
            strOp = "inc";
        } else if (op == MLVBCommonDef.CustomFieldOp.DEC) {
            strOp = "dec";
        }
        mHttpRequest.setCustomInfo(mCurrRoomID, key, strOp, value, new HttpRequests.OnResponseCallback<HttpResponse>() {
            @Override
            public void onResponse(final int retcode, @Nullable final String retmsg, @Nullable HttpResponse data) {
                if (retcode == HttpResponse.CODE_OK) {
                    callbackOnThread(callback, "onSuccess");
                } else {
                    callbackOnThread(callback, "onError", retcode, "[LiveRoom] setCustomInfo??????[" + retmsg + ":" + retcode + "]");
                }
            }
        });
    }

    /**
     * ?????????????????????
     *
     * @param callback ???????????????????????????
     */
    public void getCustomInfo(final IMLVBLiveRoomListener.GetCustomInfoCallback callback) {
        TXCLog.i(TAG, "API -> getCustomInfo");
        mHttpRequest.getCustomInfo(mCurrRoomID, new HttpRequests.OnResponseCallback<HttpResponse.GetCustomInfoResponse>() {
            @Override
            public void onResponse(final int retcode, @Nullable final String retmsg, @Nullable HttpResponse.GetCustomInfoResponse data) {
                if (retcode == HttpResponse.CODE_OK) {
                    final Map<String, Object> customList = new HashMap<>();
                    if (data.custom != null && data.custom.size() > 0) {
                        customList.putAll(data.custom);
                    }
                    callbackOnThread(callback, "onGetCustomInfo", customList);
                } else {
                    callbackOnThread(callback, "onError", retcode, "[LiveRoom] getCustomInfo??????[" + retmsg + ":" + retcode + "]");
                }
            }
        });
    }

    @Override
    public void initMlvb(LoginInfo loginInfo) {
        MLVBLiveRoom liveRoom = MLVBLiveRoom.sharedInstance(mAppContext);
        liveRoom.login(loginInfo, new IMLVBLiveRoomListener.LoginCallback() {
            @Override
            public void onError(int errCode, String errInfo) {
                Log.i(TAG, "onError: errorCode = " + errInfo + " info = " + errInfo);
            }

            @Override
            public void onSuccess() {
                Log.i(TAG, "onSuccess: ");
            }
        });
    }

    /**
     * ??????????????????
     *
     * ????????????????????????????????????????????????????????????????????????
     * 1. ?????????????????? requestJoinAnchor() ??????????????????????????????
     * 2. ????????????????????? {@link IMLVBLiveRoomListener#onRequestJoinAnchor(AnchorInfo, String)} ??????????????????
     * 3. ?????????????????? responseJoinAnchor() ??????????????????????????????????????????
     * 4. ????????????????????? {@link IMLVBLiveRoomListener.RequestJoinAnchorCallback} ???????????????????????????????????????????????????
     * 5. ????????????????????????????????????????????? startLocalPreview() ?????????????????????????????? App ?????????????????????????????????????????????????????? UI ?????????
     * 6. ???????????????????????? joinAnchor() ???????????????????????????
     * 7. ??????????????????????????????????????????????????????????????? {@link IMLVBLiveRoomListener#onAnchorEnter(AnchorInfo)} ?????????
     * 8. ???????????????????????? startRemoteView() ?????????????????????????????????????????????
     * 9. ????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????? onAnchorEnter() ????????????????????????startRemoteView????????????????????????????????????
     *
     * @param reason   ????????????
     * @param callback
     * @see {@link IMLVBLiveRoomListener#onRequestJoinAnchor(AnchorInfo, String)}
     */
    @Override
    public void requestJoinAnchor(String reason, final IMLVBLiveRoomListener.RequestJoinAnchorCallback callback) {
        TXCLog.i(TAG, "API -> requestJoinAnchor:" + reason);
        try {
            CommonJson<JoinAnchorRequest> request = new CommonJson<>();
            request.cmd = "linkmic";
            request.data = new JoinAnchorRequest();
            request.data.type = "request";
            request.data.roomID = mCurrRoomID;
            request.data.userID = mSelfAccountInfo.userID;
            request.data.userName = mSelfAccountInfo.userName;
            request.data.userAvatar = mSelfAccountInfo.userAvatar;
            request.data.reason = reason;
            request.data.timestamp = System.currentTimeMillis() - mTimeDiff;

            mJoinAnchorCallback = callback;

            if (mJoinAnchorTimeoutTask == null) {
                mJoinAnchorTimeoutTask = new Runnable() {
                    @Override
                    public void run() {
                        callbackOnThread(new Runnable() {
                            @Override
                            public void run() {
                                IMLVBLiveRoomListener.RequestJoinAnchorCallback reqJoinCallback = mJoinAnchorCallback;
                                if (reqJoinCallback != null) {
                                    reqJoinCallback.onTimeOut();
                                    mJoinAnchorCallback = null;
                                }
                            }
                        });
                    }
                };
            }

            mListenerHandler.removeCallbacks(mJoinAnchorTimeoutTask);
            //10????????????????????????/???????????????????????????????????????
            mListenerHandler.postDelayed(mJoinAnchorTimeoutTask, 10 * 1000);

            String content = new Gson().toJson(request, new TypeToken<CommonJson<JoinAnchorRequest>>(){}.getType());
            String toUserID = getRoomCreator(mCurrRoomID);
            IMMessageMgr imMessageMgr = mIMMessageMgr;
            if (imMessageMgr != null) {
                imMessageMgr.sendC2CCustomMessage(toUserID, content, new IMMessageMgr.Callback() {
                    @Override
                    public void onError(final int code, final String errInfo) {
                        callbackOnThread(new Runnable() {
                            @Override
                            public void run() {
                                IMLVBLiveRoomListener.RequestJoinAnchorCallback reqJoinCallback = mJoinAnchorCallback;
                                if (reqJoinCallback != null) {
                                    reqJoinCallback.onError(code, "[IM] ??????????????????[" + errInfo + ":" + code + "]");
                                }
                            }
                        });
                    }

                    @Override
                    public void onSuccess(Object... args) {

                    }
                });
            }

        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * ????????????????????????
     *
     * ??????????????? {@link IMLVBLiveRoomListener#onRequestJoinAnchor(AnchorInfo, String)} ?????????????????????????????????????????????????????????????????????
     *
     * @param userID ??????ID
     * @param agree  true????????????false?????????
     * @param reason ??????/???????????????????????????
     *
     * @return 0?????????????????????0???????????????
     */
    @Override
    public int responseJoinAnchor(String userID, boolean agree, String reason) {
        TXCLog.i(TAG, "API -> responseJoinAnchor:" + userID + ":" + agree + ":" + reason);
        if (mPlayers.size() > 0 && mMixMode == STREAM_MIX_MODE_PK) {
            TXCLog.e(TAG, "?????????PK?????????????????????PK??????????????????");
            return -1;
        }
        try {
            if (agree) {
                mMixMode = STREAM_MIX_MODE_JOIN_ANCHOR;
            }
            CommonJson<JoinAnchorResponse> response = new CommonJson<>();
            response.cmd = "linkmic";
            response.data = new JoinAnchorResponse();
            response.data.type = "response";
            response.data.result = agree?"accept":"reject";
            response.data.reason = reason;
            response.data.roomID = mCurrRoomID;
            response.data.timestamp = System.currentTimeMillis() - mTimeDiff;
            String content = new Gson().toJson(response, new TypeToken<CommonJson<JoinAnchorResponse>>(){}.getType());
            IMMessageMgr imMessageMgr = mIMMessageMgr;
            if (imMessageMgr != null) {
                imMessageMgr.sendC2CCustomMessage(userID, content, new IMMessageMgr.Callback() {
                    @Override
                    public void onError(final int code, final String errInfo) {

                    }

                    @Override
                    public void onSuccess(Object... args) {

                    }
                });
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * ????????????????????????
     *
     * ???????????????????????????????????????????????????????????? {@link IMLVBLiveRoomListener#onAnchorEnter(AnchorInfo)} ??????
     *
     * @param callback ???????????????????????????
     */
    @Override
    public void joinAnchor(final IMLVBLiveRoomListener.JoinAnchorCallback callback) {
        TXCLog.i(TAG, "API -> joinAnchor");
        if (mCurrRoomID == null || mCurrRoomID.length() == 0) {
            callbackOnThread(callback, "onError", MLVBCommonDef.LiveRoomErrorCode.ERROR_NOT_IN_ROOM, "[LiveRoom] ????????????????????????[?????????????????????????????????????????????]");
            return;
        }

        //1.??????????????????startLocalPreview

        //2. ??????CGI:get_pushers???????????????????????????????????????????????????
        updateAnchors(true, new UpdateAnchorsCallback() {
            @Override
            public void onUpdateAnchors(int retcode, List<AnchorInfo> addAnchors, List<AnchorInfo> delAnchors, HashMap<String, AnchorInfo> mergedAnchors, AnchorInfo roomCreator) {
                //3.?????????????????????????????????????????????????????????
                if (retcode == 0) {
                    String accelerateURL = roomCreator.accelerateURL;
                    if (accelerateURL != null && accelerateURL.length() > 0) {
                        mTXLivePlayer.stopPlay(true);
                        mTXLivePlayer.startPlay(accelerateURL, TXLivePlayer.PLAY_TYPE_LIVE_RTMP_ACC);
                    } else {
                        TXCLog.e(TAG, "???????????????????????????????????????");
                    }
                }
            }
        });

        //4. ??????CGI:get_push_url??????????????????????????????pushUrl
        mHttpRequest.getPushUrl(mSelfAccountInfo.userID, mCurrRoomID, new HttpRequests.OnResponseCallback<HttpResponse.PushUrl>() {
            @Override
            public void onResponse(int retcode, String retmsg, final HttpResponse.PushUrl data) {
                if (retcode == HttpResponse.CODE_OK && data != null && data.pushURL != null) {
                    mSelfPushUrl = data.pushURL;
                    mSelfAccelerateURL = data.accelerateURL;
                    //5. ????????????
                    startPushStream(data.pushURL, TXLiveConstants.VIDEO_QUALITY_LINKMIC_SUB_PUBLISHER, new StandardCallback() {
                        @Override
                        public void onError(final int code, final String info) {
                            callbackOnThread(callback, "onError", code, info);
                        }

                        @Override
                        public void onSuccess() {
                            mBackground = false;
                            //6. ?????????????????????CGI:add_pusher????????????????????????????????????
                            addAnchor(mCurrRoomID, data.pushURL, new StandardCallback() {
                                @Override
                                public void onError(final int code, final String info) {
                                    callbackOnThread(callback, "onError", code, info);
                                }

                                @Override
                                public void onSuccess() {
                                    mJoinPusher = true;
                                    mHeartBeatThread.startHeartbeat();// ????????????
                                    callbackOnThread(callback, "onSuccess");

                                    //???????????????????????????
                                    notifyPusherChange();
                                }
                            });
                        }
                    });
                } else {
                    callbackOnThread(callback, "onError", retcode, "[LiveRoom] ????????????????????????[" + retmsg + ":" + retcode + "]");
                }
            }
        });
    }

    /**
     * ??????????????????
     *
     * ???????????????????????????????????????????????????????????? {@link IMLVBLiveRoomListener#onAnchorExit(AnchorInfo)} ??????
     *
     * @param callback ???????????????????????????
     */
    @Override
    public void quitJoinAnchor(final IMLVBLiveRoomListener.QuitAnchorCallback callback) {
        TXCLog.i(TAG, "API -> quitJoinAnchor");
        Handler handler = new Handler(mAppContext.getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                //1. ??????????????????
                if (mPreviewType == LIVEROOM_CAMERA_PREVIEW) {
                    stopLocalPreview();
                } else {
                    stopScreenCapture();
                }
                unInitLivePusher();

                //2. ??????????????????????????????
                cleanPlayers();

                //3. ?????????????????????????????????????????????????????????
                mTXLivePlayer.stopPlay(true);
                if (!mBackground) {
                    String mixedPlayUrl = getMixedPlayUrlByRoomID(mCurrRoomID);
                    if (mixedPlayUrl != null && mixedPlayUrl.length() > 0) {
                        int playType = getPlayType(mixedPlayUrl);
                        mTXLivePlayer.startPlay(mixedPlayUrl, playType);
                    }
                }
            }
        });

        //4. ????????????
        mHeartBeatThread.stopHeartbeat();

        //5. ??????CGI:delete_pusher????????????????????????????????????
        if (mHasAddAnchor) {
            mHasAddAnchor = false;
            mHttpRequest.delPusher(mCurrRoomID, mSelfAccountInfo.userID, new HttpRequests.OnResponseCallback<HttpResponse>() {
                @Override
                public void onResponse(int retcode, String retmsg, HttpResponse data) {
                    if (retcode == HttpResponse.CODE_OK) {
                        TXCLog.d(TAG, "??????????????????");
                        callbackOnThread(mListener, "onDebugLog", "[LiveRoom] ??????????????????");
                    } else {
                        TXCLog.e(TAG, String.format("?????????????????????%s(%d)", retmsg, retcode));
                        callbackOnThread(mListener, "onDebugLog", String.format("[LiveRoom] ?????????????????????%s(%d)", retmsg, retcode));
                    }

                    //???????????????????????????
                    notifyPusherChange();
                }
            });
        }

        mJoinPusher = false;

        mPushers.clear();

        callbackOnThread(callback, "onSuccess");
    }

    /**
     * ????????????????????????
     *
     * ???????????????????????????????????????????????????????????????????????? {@link IMLVBLiveRoomListener#onKickoutJoinAnchor()} ????????????
     *
     * @param userID ????????????ID
     * @see {@link IMLVBLiveRoomListener#onKickoutJoinAnchor()}
     */
    @Override
    public void kickoutJoinAnchor(String userID) {
        TXCLog.i(TAG, "API -> kickoutJoinAnchor:" + userID);
        try {
            CommonJson<KickoutResponse> response = new CommonJson<>();
            response.cmd = "linkmic";
            response.data = new KickoutResponse();
            response.data.type = "kickout";
            response.data.roomID = mCurrRoomID;
            response.data.timestamp = System.currentTimeMillis() - mTimeDiff;
            String content = new Gson().toJson(response, new TypeToken<CommonJson<KickoutResponse>>(){}.getType());
            IMMessageMgr imMessageMgr = mIMMessageMgr;
            if (imMessageMgr != null) {
                imMessageMgr.sendC2CCustomMessage(userID, content, new IMMessageMgr.Callback() {
                    @Override
                    public void onError(final int code, final String errInfo) {

                    }

                    @Override
                    public void onSuccess(Object... args) {

                    }
                });
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * ????????????PK
     *
     * ???????????????????????????????????? PK????????????????????????????????? A ??? B???????????????????????? PK ???????????????
     * 1. ????????? A????????? requestRoomPK() ????????? B ?????????????????????
     * 2. ????????? B???????????? {@link IMLVBLiveRoomListener#onRequestRoomPK(AnchorInfo)} ???????????????
     * 3. ????????? B????????? responseRoomPK() ???????????????????????? A ??? PK ?????????
     * 4. ????????? B???????????????????????? A ?????????????????????????????? startRemoteView() ??????????????? A ??????????????????
     * 5. ????????? A???????????? {@link IMLVBLiveRoomListener.RequestRoomPKCallback} ???????????????????????????????????????????????????
     * 6. ????????? A?????????????????????????????????????????? startRemoteView() ???????????? B ??????????????????
     *
     * @param userID   ???????????????ID
     * @param callback ????????????PK???????????????
     * @see {@link IMLVBLiveRoomListener#onRequestRoomPK(AnchorInfo)}
     */
    @Override
    public void requestRoomPK(String userID, final IMLVBLiveRoomListener.RequestRoomPKCallback callback) {
        TXCLog.i(TAG, "API -> requestRoomPK:" + userID);
        try {
            CommonJson<PKRequest> request = new CommonJson<>();
            request.cmd = "pk";
            request.data = new PKRequest();
            request.data.type = "request";
            request.data.action = "start";
            request.data.roomID = mCurrRoomID;
            request.data.userID = mSelfAccountInfo.userID;
            request.data.userName = mSelfAccountInfo.userName;
            request.data.userAvatar = mSelfAccountInfo.userAvatar;
            request.data.accelerateURL = mSelfAccelerateURL;
            request.data.timestamp = System.currentTimeMillis() - mTimeDiff;

            mRequestPKCallback = callback;

            if (mRequestPKTimeoutTask == null) {
                mRequestPKTimeoutTask = new Runnable() {
                    @Override
                    public void run() {
                        callbackOnThread(new Runnable() {
                            @Override
                            public void run() {
                                if (mRequestPKCallback != null) {
                                    mRequestPKCallback.onTimeOut();
                                    mRequestPKCallback = null;
                                }
                            }
                        });
                    }
                };
            }

            mListenerHandler.removeCallbacks(mRequestPKTimeoutTask);
            //10????????????????????????/?????? PK ???????????????????????????
            mListenerHandler.postDelayed(mRequestPKTimeoutTask, 10 * 1000);

            mPKAnchorInfo = new AnchorInfo(userID, "", "", "");

            String content = new Gson().toJson(request, new TypeToken<CommonJson<PKRequest>>(){}.getType());
            IMMessageMgr imMessageMgr = mIMMessageMgr;
            if (imMessageMgr != null) {
                imMessageMgr.sendC2CCustomMessage(userID, content, new IMMessageMgr.Callback() {
                    @Override
                    public void onError(final int code, final String errInfo) {
                        callbackOnThread(callback, "onError", code, "[IM] ??????PK??????[" + errInfo + ":" + code + "]");
                    }

                    @Override
                    public void onSuccess(Object... args) {

                    }
                });
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * ???????????? PK ??????
     *
     * ????????????????????????????????? PK ??????????????? PK ???????????????????????? {@link IMLVBLiveRoomListener.RequestRoomPKCallback} ???????????????
     *
     * @param userID ?????? PK ??????????????? ID
     * @param agree  true????????????false?????????
     * @param reason ??????/??????PK???????????????
     *
     * @return 0?????????????????????0???????????????
     */
    @Override
    public int responseRoomPK(String userID, boolean agree, String reason) {
        TXCLog.i(TAG, "API -> responseRoomPK:" + userID + ":" + agree + ":" + reason);
        if (mPlayers.size() > 0 && mMixMode == STREAM_MIX_MODE_JOIN_ANCHOR) {
            TXCLog.e(TAG, "??????????????????????????????????????????????????????PK");
            return -1;
        }
        try {
            if (agree) {
                mMixMode = STREAM_MIX_MODE_PK;
            }
            CommonJson<PKResponse> response = new CommonJson<>();
            response.cmd = "pk";
            response.data = new PKResponse();
            response.data.type = "response";
            response.data.result = agree?"accept":"reject";
            response.data.reason= reason;
            response.data.roomID = mCurrRoomID;
            response.data.accelerateURL = mSelfAccelerateURL;
            response.data.timestamp = System.currentTimeMillis() - mTimeDiff;

            String content = new Gson().toJson(response, new TypeToken<CommonJson<PKResponse>>(){}.getType());
            IMMessageMgr imMessageMgr = mIMMessageMgr;
            if (imMessageMgr != null) {
                imMessageMgr.sendC2CCustomMessage(userID, content, new IMMessageMgr.Callback() {
                    @Override
                    public void onError(final int code, final String errInfo) {

                    }

                    @Override
                    public void onSuccess(Object... args) {

                    }
                });
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * ???????????? PK
     *
     * ????????????????????????????????????????????? PK ???????????????????????????????????? {@link IMLVBLiveRoomListener#onQuitRoomPK(AnchorInfo)} ???????????????
     *
     * @param callback ???????????? PK ???????????????
     */
    @Override
    public void quitRoomPK(final IMLVBLiveRoomListener.QuitRoomPKCallback callback) {
        TXCLog.i(TAG, "API -> quitRoomPK");
        try {
            if (mPKAnchorInfo != null && mPKAnchorInfo.userID != null && mPKAnchorInfo.userID.length() > 0) {
                CommonJson<PKRequest> request = new CommonJson<>();
                request.cmd = "pk";
                request.data = new PKRequest();
                request.data.type = "request";
                request.data.action = "stop";
                request.data.roomID = mCurrRoomID;
                request.data.userID = mSelfAccountInfo.userID;
                request.data.userName = mSelfAccountInfo.userName;
                request.data.userAvatar = mSelfAccountInfo.userAvatar;
                request.data.accelerateURL = "";
                request.data.timestamp = System.currentTimeMillis() - mTimeDiff;

                String content = new Gson().toJson(request, new TypeToken<CommonJson<PKRequest>>() {}.getType());
                IMMessageMgr imMessageMgr = mIMMessageMgr;
                if (imMessageMgr != null) {
                    imMessageMgr.sendC2CCustomMessage(mPKAnchorInfo.userID, content, new IMMessageMgr.Callback() {
                        @Override
                        public void onError(final int code, final String errInfo) {
                            callbackOnThread(callback, "onError", code, "[IM] ??????PK??????[" + errInfo + ":" + code + "]");
                        }

                        @Override
                        public void onSuccess(Object... args) {
                            callbackOnThread(callback, "onSuccess");
                        }
                    });
                }
            } else {
                TXCLog.e(TAG, "???????????? PK ?????????????????????????????????????????? PK");
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * ?????????????????????????????????
     *
     * @param frontCamera YES?????????????????????NO?????????????????????
     * @param view        ???????????????????????????
     */
    @Override
    public void startLocalPreview(boolean frontCamera, TXCloudVideoView view) {
        TXCLog.i(TAG, "API -> startLocalPreview:" + frontCamera);
        initLivePusher(frontCamera);
        if (mTXLivePusher != null) {
            if (view != null) {
                view.setVisibility(View.VISIBLE);
            }
            mTXLivePusher.startCameraPreview(view);
        }
        mPreviewType = LIVEROOM_CAMERA_PREVIEW;
    }

    /**
     * ?????????????????????????????????
     */
    @Override
    public void stopLocalPreview() {
        TXCLog.i(TAG, "API -> stopLocalPreview");
        if (mTXLivePusher != null) {
            mTXLivePusher.stopCameraPreview(false);
        }
//        unInitLivePusher();
    }

    /**
     * ??????????????????????????????
     *
     * @param anchorInfo ?????????????????????
     * @param view       ???????????????????????????
     * @param callback   ??????????????????
     *
     * @note ??? onUserVideoAvailable ??????????????????????????????
     */
    @Override
    public void startRemoteView(final AnchorInfo anchorInfo, final TXCloudVideoView view, final IMLVBLiveRoomListener.PlayCallback callback) {
        TXCLog.i(TAG, "API -> startRemoteView:" + anchorInfo.userID + ":" + anchorInfo.accelerateURL);
        //?????????????????????
        Handler handler = new Handler(mAppContext.getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (mPlayers.containsKey(anchorInfo.userID)) {
                    PlayerItem pusherPlayer = mPlayers.get(anchorInfo.userID);
                    if (pusherPlayer.player.isPlaying()) {
                        //???????????????
                        return;
                    } else {
                        pusherPlayer = mPlayers.remove(anchorInfo.userID);
                        pusherPlayer.destroy();
                    }
                }

                if (mSelfRoleType == LIVEROOM_ROLE_PUSHER) {
                    if (mPlayers.size() == 0) {
                        if (mTXLivePusher != null) {
                            if (mMixMode == STREAM_MIX_MODE_PK) {
                                //PK
                                mTXLivePusher.setVideoQuality(TXLiveConstants.VIDEO_QUALITY_LINKMIC_MAIN_PUBLISHER, true, true);
                                TXLivePushConfig config = mTXLivePusher.getConfig();
                                config.setVideoResolution(TXLiveConstants.VIDEO_RESOLUTION_TYPE_360_640);
                                config.setAutoAdjustBitrate(false);
                                config.setVideoBitrate(800);
                                mTXLivePusher.setConfig(config);
                            } else {
                                //?????????????????????Quality???VIDEO_QUALITY_LINKMIC_MAIN_PUBLISHER
                                mTXLivePusher.setVideoQuality(TXLiveConstants.VIDEO_QUALITY_LINKMIC_MAIN_PUBLISHER, true, false);
                            }
                        }
                    }
                }

                final TXLivePlayer player = new TXLivePlayer(mAppContext);

                view.setVisibility(View.VISIBLE);
                player.setPlayerView(view);
                player.enableHardwareDecode(true);
                player.setRenderMode(TXLiveConstants.RENDER_MODE_FULL_FILL_SCREEN);

                PlayerItem anchorPlayer = new PlayerItem(view, anchorInfo, player);
                mPlayers.put(anchorInfo.userID, anchorPlayer);

                player.setPlayListener(new ITXLivePlayListener() {
                    @Override
                    public void onPlayEvent(final int event, final Bundle param) {
                        if (event == TXLiveConstants.PLAY_EVT_PLAY_BEGIN) {
                            if (mSelfRoleType == LIVEROOM_ROLE_PUSHER) {
                                //??????????????????
                                if (mMixMode == STREAM_MIX_MODE_PK) {
                                    mStreamMixturer.addPKVideoStream(anchorInfo.accelerateURL);
                                } else {
                                    mStreamMixturer.addSubVideoStream(anchorInfo.accelerateURL);
                                }
                            }
                            callbackOnThread(callback, "onBegin");
                        }
                        else if (event == TXLiveConstants.PLAY_EVT_PLAY_END || event == TXLiveConstants.PLAY_ERR_NET_DISCONNECT){
                            callbackOnThread(callback, "onError", event, "[LivePlayer] ????????????[" + param.getString(TXLiveConstants.EVT_DESCRIPTION) + "]");

                            //????????????
//                        if (mPlayers.containsKey(anchorInfo.userID)) {
//                            PlayerItem item = mPlayers.remove(anchorInfo.userID);
//                            if (item != null) {
//                                item.destroy();
//                            }
//                        }
                        }
                        else {
                            callbackOnThread(callback, "onEvent", event, param);
                        }
                    }

                    @Override
                    public void onNetStatus(Bundle status) {

                    }
                });

                int result = player.startPlay(anchorInfo.accelerateURL, TXLivePlayer.PLAY_TYPE_LIVE_RTMP_ACC);
                if (result != 0){
                    TXCLog.e(TAG, String.format("[BaseRoom] ???????????? {%s} ?????? {%s} ??????", anchorInfo.userID, anchorInfo.accelerateURL));
                }
            }
        });
    }

    /**
     * ??????????????????????????????
     *
     * @param anchorInfo ?????????????????????
     */
    @Override
    public void stopRemoteView(final AnchorInfo anchorInfo) {
        TXCLog.i(TAG, "API -> stopRemoteView:" + anchorInfo.userID);
        if (anchorInfo == null || anchorInfo.userID == null) {
            return;
        }
        Handler handler = new Handler(mAppContext.getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (mPlayers.containsKey(anchorInfo.userID)){
                    PlayerItem pusherPlayer = mPlayers.remove(anchorInfo.userID);
                    pusherPlayer.destroy();
                }

                if (mPushers.containsKey(anchorInfo.userID)) {
                    mPushers.remove(anchorInfo.userID);
                }

                if (mSelfRoleType == LIVEROOM_ROLE_PUSHER) {
                    //????????????????????????
                    if (mMixMode == STREAM_MIX_MODE_PK) {
                        mStreamMixturer.delPKVideoStream(anchorInfo.accelerateURL);
                    } else {
                        mStreamMixturer.delSubVideoStream(anchorInfo.accelerateURL);
                    }
                    if (mPlayers.size() == 0) {
                        //????????????????????????????????????????????????
                        if (mTXLivePusher != null) {
                            mTXLivePusher.setVideoQuality(TXLiveConstants.VIDEO_QUALITY_HIGH_DEFINITION, false, false);
                            TXLivePushConfig config = mTXLivePusher.getConfig();
                            config.setVideoEncodeGop(2);
                            mTXLivePusher.setConfig(config);
                        }
                    }
                }
            }
        });
    }

    /**
     * ???????????????
     *
     */
    public synchronized void startScreenCapture() {
        TXCLog.i(TAG, "API -> startScreenCapture");
        initLivePusher(true);
        if (mTXLivePusher != null) {
            mTXLivePusher.startScreenCapture();
        }
        mPreviewType = LIVEROOM_SCREEN_PREVIEW;
    }

    /**
     * ???????????????
     *
     */
    public synchronized void stopScreenCapture() {
        TXCLog.i(TAG, "API -> stopScreenCapture");
        if (mTXLivePusher != null) {
            mTXLivePusher.stopScreenCapture();
        }
    }

    /**
     * ????????????????????????
     *
     * @param mute true:?????? false:??????
     */
    @Override
    public void muteLocalAudio(boolean mute) {
        if (mTXLivePusher != null) {
            mTXLivePusher.setMute(mute);
        }
    }

    /**
     * ?????????????????????????????????
     *
     * @param notify ????????????
     */
    public void setBGMNofify(TXLivePusher.OnBGMNotify notify) {
        if (mTXLivePusher != null) {
            mTXLivePusher.setBGMNofify(notify);
        }
    }

    /**
     * ??????????????????????????????
     *
     * @param userID ?????????????????????
     * @param mute   true:?????? false:?????????
     */
    @Override
    public void muteRemoteAudio(String userID, boolean mute) {
        if (mPlayers.containsKey(userID)){
            PlayerItem pusherPlayer = mPlayers.get(userID);
            pusherPlayer.player.setMute(mute);
        } else if (userID == getRoomCreator(mCurrRoomID)) {
            //?????????
            mTXLivePlayer.setMute(mute);
        }
    }

    /**
     * ????????????????????????????????????
     *
     * @param mute true:?????? false:?????????
     */
    @Override
    public void muteAllRemoteAudio(boolean mute) {
        for (Map.Entry<String, PlayerItem> entry : mPlayers.entrySet()) {
            entry.getValue().player.setMute(mute);
        }
        if (mTXLivePlayer != null && mTXLivePlayer.isPlaying()) {
            mTXLivePlayer.setMute(mute);
        }
    }

    /**
     * ???????????????
     */
    @Override
    public void switchCamera() {
        if (mTXLivePusher != null) {
            mTXLivePusher.switchCamera();
        }
    }

    /**
     * ???????????????????????????????????????
     *
     * @param distance ???????????? 1 - 5 ?????????1???????????????????????????????????????????????????5?????????????????????????????????????????????????????????????????????5?????????5????????????????????????????????????
     *
     * @return false??????????????????true???????????????
     */
    @Override
    public boolean setZoom(int distance) {
        if (mTXLivePusher != null) {
            return mTXLivePusher.setZoom(distance);
        }
        return false;
    }

    /**
     * ???????????????
     *
     * @param enable true????????????false?????????
     *
     * @return false??????????????????true???????????????
     */
    @Override
    public boolean enableTorch(boolean enable) {
        if (mTXLivePusher != null) {
            return mTXLivePusher.turnOnFlashLight(enable);
        }
        return false;
    }

    /**
     * ??????????????????????????????????????????????????????
     *
     * ??????????????????????????????????????? App ?????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????
     *
     * @param bitmap ??????
     */
    @Override
    public void setCameraMuteImage(Bitmap bitmap) {
        if (mTXLivePusher != null) {
            TXLivePushConfig config = mTXLivePusher.getConfig();
            config.setPauseImg(bitmap);
            config.setPauseFlag(TXLiveConstants.PAUSE_FLAG_PAUSE_VIDEO | TXLiveConstants.PAUSE_FLAG_PAUSE_AUDIO);
            mTXLivePusher.setConfig(config);
        }
    }

    /**
     * ??????????????????????????????????????????????????????
     *
     * ??????????????????????????????????????? App ?????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????
     *
     * @param id ???????????????????????????????????????
     */
    @Override
    public void setCameraMuteImage(int id) {
        Bitmap bitmap = BitmapFactory.decodeResource(mAppContext.getResources(), id);
        if (mTXLivePusher != null) {
            TXLivePushConfig config = mTXLivePusher.getConfig();
            config.setPauseImg(bitmap);
            config.setPauseFlag(TXLiveConstants.PAUSE_FLAG_PAUSE_VIDEO | TXLiveConstants.PAUSE_FLAG_PAUSE_AUDIO);
            mTXLivePusher.setConfig(config);
        }
    }

    @Override
    public TXBeautyManager getBeautyManager() {
        if (mTXLivePusher == null) {
            mTXLivePusher = new TXLivePusher(mAppContext);
        }
        return mTXLivePusher.getBeautyManager();
    }

    /**
     * ??????????????????????????????????????????
     *
     * @param beautyStyle    ????????????????????????????????????0 ????????????1????????????2?????????
     * @param beautyLevel    ??????????????????????????? 0 - 9??? 0 ??????????????? 1 - 9???????????????????????????
     * @param whitenessLevel ??????????????????????????? 0 - 9??? 0 ??????????????? 1 - 9???????????????????????????
     * @param ruddinessLevel ??????????????????????????? 0 - 9??? 0 ??????????????? 1 - 9???????????????????????????
     */
    @Override
    public boolean setBeautyStyle(int beautyStyle, int beautyLevel, int whitenessLevel, int ruddinessLevel) {
        if (mTXLivePusher != null) {
            return mTXLivePusher.setBeautyFilter(beautyStyle, beautyLevel, whitenessLevel, ruddinessLevel);
        }
        return false;
    }

    /**
     * ??????????????????????????????
     *
     * @param image ??????????????????????????????????????????????????????????????? png ???????????????
     */
    @Override
    public void setFilter(Bitmap image) {
        if (mTXLivePusher != null) {
            mTXLivePusher.setFilter(image);
        }
    }

    /**
     * ??????????????????
     *
     * @param concentration ???0???1?????????????????????????????????????????????0.5
     */
    @Override
    public void setFilterConcentration(float concentration) {
        if (mTXLivePusher != null) {
            mTXLivePusher.setSpecialRatio(concentration);
        }
    }

    /**
     * ???????????????height ???????????????sdk ?????????????????????????????????????????? height
     *
     * @param image      ???????????? null ??????????????????
     * @param x          ???????????????????????? X ??????????????????[0,1]
     * @param y          ???????????????????????? Y ??????????????????[0,1]
     * @param width      ??????????????????????????????[0,1]
     */
    @Override
    public void setWatermark(Bitmap image, float x, float y, float width) {
        if (mTXLivePusher != null) {
            TXLivePushConfig config = mTXLivePusher.getConfig();
            config.setWatermark(image, x, y, width);
            mTXLivePusher.setConfig(config);
        }
    }

    /**
     * ??????????????????
     *
     * @param filePath ????????????????????????
     */
    @Override
    public void setMotionTmpl(String filePath) {
        if (mTXLivePusher != null) {
            mTXLivePusher.setMotionTmpl(filePath);
        }
    }

    /**
     * ??????????????????
     *
     * ??????????????????jpg/png???????????????mp4/3gp???Android?????????????????????
     *
     * @param file ??????????????????????????????????????????
     *             1.??????????????????assets?????????path??????????????????
     *             2.path?????????????????????
     *
     * @return false??????????????????true???????????????
     * @note API??????18
     */
    @Override
    public boolean setGreenScreenFile(String file) {
        if (mTXLivePusher != null) {
            return mTXLivePusher.setGreenScreenFile(file);
        }
        return false;
    }

    /**
     * ??????????????????
     *
     * @param level ????????????????????? 0 ~ 9????????????0??????????????????????????????????????????0
     */
    @Override
    public void setEyeScaleLevel(int level) {
        if (mTXLivePusher != null) {
            mTXLivePusher.setEyeScaleLevel(level);
        }
    }

    /**
     * ??????V???????????????????????????????????????????????????????????????
     *
     * @param level V????????????????????? 0 ~ 9????????????????????????????????????????????????0
     */
    @Override
    public void setFaceVLevel(int level) {
        if (mTXLivePusher != null) {
            mTXLivePusher.setFaceVLevel(level);
        }
    }

    /**
     * ??????????????????
     *
     * @param level ????????????????????? 0 ~ 9????????????0??????????????????????????????????????????0
     */
    @Override
    public void setFaceSlimLevel(int level) {
        if (mTXLivePusher != null) {
            mTXLivePusher.setFaceSlimLevel(level);
        }
    }

    /**
     * ????????????????????????????????????????????????????????????????????????
     *
     * @param level ???????????????????????? 0 ~ 9??? ?????????????????????????????????????????????0
     */
    @Override
    public void setFaceShortLevel(int level) {
        if (mTXLivePusher != null) {
            mTXLivePusher.setFaceShortLevel(level);
        }
    }

    /**
     * ???????????????????????????????????????????????????????????????????????????????????????
     *
     * @param chinLevel ??????????????????????????????????????? -9 ~ 9????????????????????????????????????????????????0
     */
    @Override
    public void setChinLevel(int chinLevel) {
        if (mTXLivePusher != null) {
            mTXLivePusher.setChinLevel(chinLevel);
        }
    }

    /**
     * ????????????????????????????????????????????????????????????????????????
     *
     * @param noseSlimLevel ???????????????????????? 0 ~ 9????????????????????????????????????????????????0
     */
    @Override
    public void setNoseSlimLevel(int noseSlimLevel) {
        if (mTXLivePusher != null) {
            mTXLivePusher.setNoseSlimLevel(noseSlimLevel);
        }
    }

    /**
     * ????????????
     *
     * @param value ????????????????????????????????????????????????????????????????????????????????????-1 - 1???
     *              ???????????????????????????-1??????????????????????????????????????????1???????????????0?????????????????????
     */
    public void setExposureCompensation(float value) {
        if (mTXLivePusher != null) {
            mTXLivePusher.setExposureCompensation(value);
        }
    }

    /**
     * ??????????????????
     *
     * @param message ????????????
     * @param callback ???????????????????????????
     * @see {@link IMLVBLiveRoomListener#onRecvRoomTextMsg(String, String, String, String, String)}
     */
    @Override
    public void sendRoomTextMsg(String message, final IMLVBLiveRoomListener.SendRoomTextMsgCallback callback) {
        IMMessageMgr imMessageMgr = mIMMessageMgr;
        if (imMessageMgr != null) {
            imMessageMgr.sendGroupTextMessage(mSelfAccountInfo.userName, mSelfAccountInfo.userAvatar, message, new IMMessageMgr.Callback() {
                @Override
                public void onError(final int code, final String errInfo) {
                    String msg = "[IM] ??????????????????[" + errInfo + ":" + code + "]";
                    TXCLog.e(TAG, msg);
                    callbackOnThread(callback, "onError", code, msg);
                }

                @Override
                public void onSuccess(Object... args) {
                    callbackOnThread(callback, "onSuccess");
                }
            });
        }
    }

    /**
     * ???????????????????????????
     *
     * @param cmd     ????????????????????????????????????????????????????????????????????????
     * @param message ????????????
     * @param callback ???????????????????????????
     * @see {@link IMLVBLiveRoomListener#onRecvRoomCustomMsg(String, String, String, String, String, String)}
     */
    @Override
    public void sendRoomCustomMsg(String cmd, String message, final IMLVBLiveRoomListener.SendRoomCustomMsgCallback callback) {
        CommonJson<CustomMessage> customMessage = new CommonJson<>();
        customMessage.cmd = "CustomCmdMsg";
        customMessage.data = new CustomMessage();
        customMessage.data.userName = mSelfAccountInfo.userName;
        customMessage.data.userAvatar = mSelfAccountInfo.userAvatar;
        customMessage.data.cmd = cmd;
        customMessage.data.msg = message ;
        final String content = new Gson().toJson(customMessage, new TypeToken<CommonJson<CustomMessage>>(){}.getType());
        IMMessageMgr imMessageMgr = mIMMessageMgr;
        if (imMessageMgr != null) {
            imMessageMgr.sendGroupCustomMessage(content, new IMMessageMgr.Callback() {
                @Override
                public void onError(int code, String errInfo) {
                    String msg = "[IM] ???????????????????????????[" + errInfo + ":" + code + "]";
                    TXCLog.e(TAG, msg);
                    callbackOnThread(callback, "onError", code, msg);
                }

                @Override
                public void onSuccess(Object... args) {
                    callbackOnThread(callback, "onSuccess");
                }
            });
        }
    }

    /**
     * ??????????????????
     *
     * @param path ????????????????????????
     * @return true??????????????????false???????????????
     */
    @Override
    public boolean playBGM(String path) {
        if (mTXLivePusher != null) {
            return mTXLivePusher.playBGM(path);
        }
        return false;
    }

    /**
     * ????????????????????????
     */
    @Override
    public void stopBGM() {
        if (mTXLivePusher != null) {
            mTXLivePusher.stopBGM();
        }
    }

    /**
     * ????????????????????????
     */
    @Override
    public void pauseBGM() {
        if (mTXLivePusher != null) {
            mTXLivePusher.pauseBGM();
        }
    }

    /**
     * ????????????????????????
     */
    @Override
    public void resumeBGM() {
        if (mTXLivePusher != null) {
            mTXLivePusher.resumeBGM();
        }
    }

    /**
     * ???????????????????????????
     *
     * @param path ??????????????????????????? path ?????????????????????????????????????????? music ??????
     * @return ????????????????????????????????????????????????-1
     */
    @Override
    public int getBGMDuration(String path) {
        if (mTXLivePusher != null) {
            return mTXLivePusher.getMusicDuration(path);
        }
        return 0;
    }

    /**
     * ??????????????????????????????????????????????????????????????????????????????????????????????????????
     *
     * @param volume : ???????????????100??????????????????????????????0 - 200
     */
    @Override
    public void setMicVolumeOnMixing(int volume) {
        if (mTXLivePusher != null) {
            mTXLivePusher.setMicVolume(volume/100.0f);
        }
    }

    /**
     * ?????????????????????????????????????????????????????????????????????????????????????????????????????????
     *
     * @param volume ???????????????100??????????????????????????????0 - 200?????????????????????????????????????????????????????????
     */
    @Override
    public void setBGMVolume(int volume) {
        if (mTXLivePusher != null) {
            mTXLivePusher.setBGMVolume(volume/100.0f);
        }
    }

    /**
     * ??????????????????
     *
     * @param reverbType ?????????????????????
     *                      {@link TXLiveConstants#REVERB_TYPE_0 } (????????????)
     *                      {@link TXLiveConstants#REVERB_TYPE_1 } (KTV)
     *                      {@link TXLiveConstants#REVERB_TYPE_2 } (?????????)
     *                      {@link TXLiveConstants#REVERB_TYPE_3 } (?????????)
     *                      {@link TXLiveConstants#REVERB_TYPE_4 } (??????)
     *                      {@link TXLiveConstants#REVERB_TYPE_5 } (??????)
     *                      {@link TXLiveConstants#REVERB_TYPE_6 } (??????)
     */
    @Override
    public void setReverbType(int reverbType) {
        if (mTXLivePusher != null) {
            mTXLivePusher.setReverb(reverbType);
        }
    }

    /**
     * ??????????????????
     *
     * @param voiceChangerType ????????????????????? TXVoiceChangerType
     */
    @Override
    public void setVoiceChangerType(int voiceChangerType) {
        if (mTXLivePusher != null) {
            mTXLivePusher.setVoiceChangerType(voiceChangerType);
        }
    }

    /**
     * ??????????????????????????????
     *
     * ???????????????????????????,?????????????????????????????????????????????????????????????????????
     *
     * @param pitch ?????????0??????????????????????????? -1 - 1???
     */
    public void setBGMPitch(float pitch) {
        if (mTXLivePusher != null) {
            mTXLivePusher.setBGMPitch(pitch);
        }
    }

    /**
     * ?????????????????????????????????
     *
     * @note ?????????????????????????????????????????????????????????????????????????????? BGM ????????????????????????
     *       ?????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????
     *
     * @param position ????????????????????????????????????ms???
     *
     * @return ?????????????????????true????????????false????????????
     */
    public boolean setBGMPosition(int position) {
        if (mTXLivePusher != null) {
            return mTXLivePusher.setBGMPosition(position);
        }
        return false;
    }

    protected MLVBLiveRoomImpl(Context context) {
        if (context == null) {
            throw new InvalidParameterException("MLVBLiveRoom??????????????????context???????????????");
        }
        mAppContext = context.getApplicationContext();
        mListenerHandler = new Handler(mAppContext.getMainLooper());
        mStreamMixturer = new StreamMixturer();
        mHeartBeatThread = new HeartBeatThread();

        mTXLivePlayConfig = new TXLivePlayConfig();
        mTXLivePlayer = new TXLivePlayer(context);
        mTXLivePlayConfig.setAutoAdjustCacheTime(true);
        mTXLivePlayConfig.setMaxAutoAdjustCacheTime(2.0f);
        mTXLivePlayConfig.setMinAutoAdjustCacheTime(2.0f);
        mTXLivePlayer.setConfig(mTXLivePlayConfig);
        mTXLivePlayer.setRenderMode(TXLiveConstants.RENDER_MODE_FULL_FILL_SCREEN);
        mTXLivePlayer.setPlayListener(new ITXLivePlayListener() {
            @Override
            public void onPlayEvent(final int event, final Bundle param) {
                if (event == TXLiveConstants.PLAY_ERR_NET_DISCONNECT) {
                    String msg = "[LivePlayer] ????????????[" + param.getString(TXLiveConstants.EVT_DESCRIPTION) + "]";
                    TXCLog.e(TAG, msg);
                    callbackOnThread(mListener, "onDebugLog", msg);
                    callbackOnThread(mListener, "onError", event, msg, param);
                } else if (event == TXLiveConstants.PLAY_EVT_CHANGE_RESOLUTION) {
                    int width = param.getInt(TXLiveConstants.EVT_PARAM1, 0);
                    int height = param.getInt(TXLiveConstants.EVT_PARAM2, 0);
                    if (width > 0 && height > 0) {
                        float ratio = (float) height / width;
                        //pc???????????????????????????4:5?????????????????????????????????????????????????????????????????????????????????????????????????????????
                        if (ratio > 1.3f) {
                            mTXLivePlayer.setRenderMode(TXLiveConstants.RENDER_MODE_FULL_FILL_SCREEN);
                        } else {
                            mTXLivePlayer.setRenderMode(TXLiveConstants.RENDER_MODE_ADJUST_RESOLUTION);
                        }
                    }
                }
            }

            @Override
            public void onNetStatus(Bundle status) {

            }
        });
    }

    private void destroy() {
        mHeartBeatThread.stopHeartbeat();
    }



    protected void startPushStream(final String url, final int videoQuality, final StandardCallback callback){
        //????????????????????????
        Handler handler = new Handler(mAppContext.getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (mTXLivePusher != null && mTXLivePushListener != null) {
                    mTXLivePushListener.setCallback(callback);
                    mTXLivePusher.setVideoQuality(videoQuality, false, false);
                    int ret = mTXLivePusher.startPusher(url);
                    if (ret == -5) {
                        String msg = "[LiveRoom] ????????????[license ????????????]";
                        TXCLog.e(TAG, msg);
                        if (callback != null) callback.onError(MLVBCommonDef.LiveRoomErrorCode.ERROR_LICENSE_INVALID, msg);
                    }
                } else {
                    String msg = "[LiveRoom] ????????????[TXLivePusher????????????????????????????????????startLocalPreview]";
                    TXCLog.e(TAG, msg);
                    if (callback != null) callback.onError(MLVBCommonDef.LiveRoomErrorCode.ERROR_PUSH, msg);
                }
            }
        });
    }

    protected void doCreateRoom(final String roomID, String roomInfo, final StandardCallback callback){
        mHttpRequest.createRoom(roomID, mSelfAccountInfo.userID, roomInfo,
                new HttpRequests.OnResponseCallback<HttpResponse.CreateRoom>() {
                    @Override
                    public void onResponse(int retcode, String retmsg, HttpResponse.CreateRoom data) {
                        if (retcode != HttpResponse.CODE_OK || data == null || data.roomID == null) {
                            String msg = "[LiveRoom] ??????????????????[" + retmsg + ":" + retcode + "]";
                            TXCLog.e(TAG, msg);
                            callback.onError(retcode, msg);
                        } else {
                            TXCLog.d(TAG, "[LiveRoom] ??????????????? ID[" + data.roomID + "] ?????? ");
                            mCurrRoomID = data.roomID;
                            callback.onSuccess();
                        }
                    }//onResponse
                });
    }

    protected void addAnchor(final String roomID, final String pushURL, final StandardCallback callback) {
        mHasAddAnchor = true;
        mHttpRequest.addPusher(roomID,
                mSelfAccountInfo.userID,
                mSelfAccountInfo.userName,
                mSelfAccountInfo.userAvatar,
                pushURL, new HttpRequests.OnResponseCallback<HttpResponse>() {
                    @Override
                    public void onResponse(int retcode, String retmsg, HttpResponse data) {
                        if (retcode == HttpResponse.CODE_OK) {
                            TXCLog.d(TAG, "[LiveRoom] add pusher ??????");
                            callback.onSuccess();
                        } else {
                            String msg = "[LiveRoom] add pusher ??????[" + retmsg + ":" + retcode + "]";
                            TXCLog.e(TAG, msg);
                            callback.onError(retcode, msg);
                        }
                    }
                });
    }

    protected void createIMGroup(final String groupId, final String groupName, final StandardCallback callback) {
        IMMessageMgr imMessageMgr = mIMMessageMgr;
        if (imMessageMgr != null) {
            imMessageMgr.createGroup(groupId, "AVChatRoom", groupName, new IMMessageMgr.Callback() {
                @Override
                public void onError(int code, String errInfo) {
                    String msg = "[IM] ???????????????[" + errInfo + ":" + code + "]";
                    TXCLog.e(TAG, "msg");
                    callback.onError(code, msg);
                }

                @Override
                public void onSuccess(Object... args) {
                    callback.onSuccess();
                }
            });
        }
    }

    protected void jionIMGroup(final String roomID, final StandardCallback callback){
        IMMessageMgr imMessageMgr = mIMMessageMgr;
        if (imMessageMgr != null) {
            imMessageMgr.jionGroup(roomID, new IMMessageMgr.Callback() {
                @Override
                public void onError(int code, String errInfo) {
                    String msg = "[IM] ????????????[" + errInfo + ":" + code + "]";
                    TXCLog.e(TAG, msg);
                    callback.onError(code, msg);
                }

                @Override
                public void onSuccess(Object... args) {
                    callback.onSuccess();
                }
            });
        }
    }

    private void notifyPusherChange() {
        //???????????????????????????
        CommonJson<AnchorInfo> msg = new CommonJson<>();
        msg.cmd = "notifyPusherChange";
        msg.data = new AnchorInfo();
        msg.data.userID = mSelfAccountInfo.userID;
        String content = new Gson().toJson(msg, new TypeToken<CommonJson<AnchorInfo>>(){}.getType());
        IMMessageMgr imMessageMgr = mIMMessageMgr;
        if (imMessageMgr != null) {
            imMessageMgr.sendGroupCustomMessage(content, new IMMessageMgr.Callback() {
                @Override
                public void onError(int code, String errInfo) {
                    TXCLog.e(TAG, "[IM] ????????????????????????????????????[" + errInfo + ":" + code + "]");
                }

                @Override
                public void onSuccess(Object... args) {
                    TXCLog.d(TAG, "????????????????????????????????????");
                }
            });
        }
    }

    protected void cleanPlayers() {
        synchronized (this) {
            for (Map.Entry<String, PlayerItem> entry : mPlayers.entrySet()) {
                entry.getValue().destroy();
            }
            mPlayers.clear();
        }
    }

    protected void updateAnchors(final boolean excludeRoomCreator, final UpdateAnchorsCallback callback){
        mHttpRequest.getPushers(mCurrRoomID, new HttpRequests.OnResponseCallback<HttpResponse.PusherList>() {
            @Override
            public void onResponse(final int retcode, String retmsg, final HttpResponse.PusherList data) {
                callbackOnThread(new Runnable() {
                    @Override
                    public void run() {
                        if (retcode == HttpResponse.CODE_OK) {
                            if (data != null) {
                                mRoomStatusCode = data.roomStatusCode;
                            }
                            parsePushers(excludeRoomCreator, data, callback);
                        } else {
                            TXCLog.e(TAG, "????????????????????????");
                            callbackOnThread(mListener, "onDebugLog", "[LiveRoom] ????????????????????????");
                            if (callback != null) {
                                callback.onUpdateAnchors(-1, null, null, null, null);
                            }
                        }
                    }
                });
            }
        });
    }

    protected void parsePushers(final boolean excludeRoomCreator, final HttpResponse.PusherList data, UpdateAnchorsCallback callback) {
        if (data != null && data.pushers != null && data.pushers.size() > 0) {
            AnchorInfo roomCreator = new AnchorInfo();
            List<AnchorInfo> anchorList = data.pushers;
            if (excludeRoomCreator) {
                if (anchorList != null && anchorList.size() > 0) {
                    Iterator<AnchorInfo> it = anchorList.iterator();
                    while (it.hasNext()) {
                        AnchorInfo anchor = it.next();
                        // ???????????????????????????????????????????????????????????????
                        if (anchor.userID != null) {
                            if (anchor.userID.equalsIgnoreCase(getRoomCreator(mCurrRoomID))) {
                                roomCreator = anchor;
                                it.remove();
                                break;
                            }
                        }
                    }
                }
            }

            final List<AnchorInfo> addAnchors = new ArrayList<>();
            final List<AnchorInfo> delAnchors = new ArrayList<>();
            HashMap<String, AnchorInfo> mergedAnchors = new HashMap<String, AnchorInfo>();

            //?????????????????????????????????????????????????????????
            mergerAnchors(anchorList, addAnchors, delAnchors, mergedAnchors);

            //????????????(????????????????????????)???????????????
            callbackOnThread(new Runnable() {
                @Override
                public void run() {
                    IMLVBLiveRoomListener listener = mListener;
                    if (listener != null) {
                        for (AnchorInfo member : addAnchors) {
                            listener.onDebugLog(String.format("[LiveRoom] onPusherJoin, UserID {%s} PlayUrl {%s}", member.userID, member.accelerateURL));
                            listener.onAnchorEnter(member);
                        }
                        for (AnchorInfo member : delAnchors) {
                            listener.onDebugLog(String.format("[LiveRoom] onPusherQuit, UserID {%s} PlayUrl {%s}", member.userID, member.accelerateURL));
                            listener.onAnchorExit(member);
                        }
                    }
                }
            });

//            //??????
//            mixtureStream(addAnchors, delAnchors);

//            if (mSelfRoleType == LIVEROOM_ROLE_PUSHER) {
//                if (mPushers.size() == 0 && mergedAnchors.size() > 0) {
//                    //???????????????????????????Quality???VIDEO_QUALITY_LINKMIC_MAIN_PUBLISHER
//                    if (mTXLivePusher != null) {
//                        mTXLivePusher.setVideoQuality(TXLiveConstants.VIDEO_QUALITY_LINKMIC_MAIN_PUBLISHER, true, false);
//                    }
//                }
//                if (mPushers.size() > 0 && mergedAnchors.size() == 0) {
//                    //???????????????????????????Quality???VIDEO_QUALITY_HIGH_DEFINITION
//                    if (mTXLivePusher != null) {
//                        mTXLivePusher.setVideoQuality(TXLiveConstants.VIDEO_QUALITY_HIGH_DEFINITION, false, false);
//                        TXLivePushConfig config = mTXLivePusher.getConfig();
//                        config.setVideoEncodeGop(5);
//                        mTXLivePusher.setConfig(config);
//                    }
//                }
//            }


            callbackOnThread(mListener, "onDebugLog", String.format("[LiveRoom] ?????????????????? new(%d), remove(%d)", addAnchors.size(), delAnchors.size()));

            if (callback != null) {
                callback.onUpdateAnchors(0, addAnchors, delAnchors, mergedAnchors, roomCreator);
            }

            mPushers = mergedAnchors;
        }
        else {
            TXCLog.e(TAG, "?????????????????????????????????");
            if (callback != null) {
                callback.onUpdateAnchors(-1, null, null, null, null);
            }
        }
    }

    protected void mergerAnchors(List<AnchorInfo> anchors, List<AnchorInfo> addAnchors, List<AnchorInfo> delAnchors, HashMap<String, AnchorInfo> mergedAnchors){
        if (anchors == null) {
            //?????????????????????????????????????????????????????????
            if (delAnchors != null) {
                delAnchors.clear();
                for (Map.Entry<String, AnchorInfo> entry : mPushers.entrySet()) {
                    delAnchors.add(entry.getValue());
                }
            }
            mPushers.clear();
            return;
        }

        for (AnchorInfo member : anchors) {
            if (member.userID != null && (!member.userID.equals(mSelfAccountInfo.userID))){
                if (!mPushers.containsKey(member.userID)) {
                    if (addAnchors != null) {
                        addAnchors.add(member);
                    }
                }
                mergedAnchors.put(member.userID, member);
            }
        }

        if (delAnchors != null) {
            for (Map.Entry<String, AnchorInfo> entry : mPushers.entrySet()) {
                if (!mergedAnchors.containsKey(entry.getKey())) {
                    delAnchors.add(entry.getValue());
                }
            }
        }
    }

    protected void mixtureStream(List<AnchorInfo> addAnchors, List<AnchorInfo> delAnchors) {
        for (AnchorInfo member : addAnchors) {
            mStreamMixturer.addSubVideoStream(member.accelerateURL);
        }
        for (AnchorInfo member : delAnchors) {
            mStreamMixturer.delSubVideoStream(member.accelerateURL);
        }
    }

    protected String getMixedPlayUrlByRoomID(String roomID) {
        for (RoomInfo item : mRoomList) {
            if (item.roomID != null && item.roomID.equalsIgnoreCase(roomID)) {
                return item.mixedPlayURL;
            }
        }
        return null;
    }

    protected int getPlayType(String playUrl) {
        int playType = TXLivePlayer.PLAY_TYPE_LIVE_RTMP;
        if (playUrl.startsWith("rtmp://")) {
            playType = TXLivePlayer.PLAY_TYPE_LIVE_RTMP;
        } else if ((playUrl.startsWith("http://") || playUrl.startsWith("https://")) && playUrl.contains(".flv")) {
            playType = TXLivePlayer.PLAY_TYPE_LIVE_FLV;
        }
        return playType;
    }

    protected String getRoomCreator(String roomID) {
        for (RoomInfo item: mRoomList) {
            if (roomID.equalsIgnoreCase(item.roomID)) {
                return item.roomCreator;
            }
        }
        return null;
    }


    protected void initLivePusher(boolean frontCamera) {
        if (mTXLivePusher == null) {
            mTXLivePusher = new TXLivePusher(mAppContext);
        }
        TXLivePushConfig config = new TXLivePushConfig();
        config.setFrontCamera(frontCamera);
        config.enableScreenCaptureAutoRotate(mScreenAutoEnable);// ???????????????????????????
        config.setPauseFlag(TXLiveConstants.PAUSE_FLAG_PAUSE_VIDEO | TXLiveConstants.PAUSE_FLAG_PAUSE_AUDIO);
        mTXLivePusher.setConfig(config);
        mTXLivePusher.setBeautyFilter(TXLiveConstants.BEAUTY_STYLE_SMOOTH, 5, 3, 2);
        mTXLivePushListener = new TXLivePushListenerImpl();
        mTXLivePusher.setPushListener(mTXLivePushListener);
    }

    protected void unInitLivePusher() {
        if (mTXLivePusher != null) {
            mSelfPushUrl = "";
            mTXLivePushListener = null;
            mTXLivePusher.setPushListener(null);
            if (mPreviewType == LIVEROOM_CAMERA_PREVIEW) {
                mTXLivePusher.stopCameraPreview(true);
            } else {
                mTXLivePusher.stopScreenCapture();
            }
            mTXLivePusher.stopPusher();
            mTXLivePusher = null;
        }
    }

    private void onRecvLinkMicMessage(final String message) {
        try {
            final JoinAnchorRequest request = new Gson().fromJson(message, JoinAnchorRequest.class);
            if (request != null && request.type.equalsIgnoreCase("request")) {
                if (isCmdTimeOut(request.timestamp)) {
                    TXCLog.e(TAG, "[LiveRoom] ????????????????????????");
                    return;
                }
                if (request.roomID.equalsIgnoreCase(mCurrRoomID)) {
                    if (mPushers.containsKey(request.userID)) {
                        //????????????????????????
                        return;
                    }
                    if (mListener == null) {
                        TXCLog.w(TAG, "no deal with link mic request message. listener = null. msg = " + message);
                        return;
                    }
                    AnchorInfo info = new AnchorInfo(request.userID, request.userName, request.userAvatar, "");
                    callbackOnThread(mListener, "onDebugLog", String.format("[LiveRoom] ??????????????????, UserID {%s} UserName {%s}", request.userID, request.userName));
                    callbackOnThread(mListener, "onRequestJoinAnchor", info, request.reason);
                }
                return;
            }

            final JoinAnchorResponse response = new Gson().fromJson(message, JoinAnchorResponse.class);
            if (response != null && response.type.equalsIgnoreCase("response")) {
                if (isCmdTimeOut(response.timestamp)) {
                    TXCLog.e(TAG, "[LiveRoom] ??????????????????????????????");
                    return;
                }
                if (mJoinAnchorCallback == null) {
                    TXCLog.w(TAG, "no deal with join anchor response message. mJoinAnchorCallback = null. msg = " + message);
                    return;
                }
                if (response.roomID.equalsIgnoreCase(mCurrRoomID)) {
                    String result = response.result;
                    if (result != null) {
                        if (result.equalsIgnoreCase("accept")) {
                            callbackOnThread(new Runnable() {
                                @Override
                                public void run() {
                                    IMLVBLiveRoomListener.RequestJoinAnchorCallback reqJoinCallback = mJoinAnchorCallback;
                                    if (reqJoinCallback != null) {
                                        reqJoinCallback.onAccept();
                                        mJoinAnchorCallback = null;
                                    }
                                    mListenerHandler.removeCallbacks(mJoinAnchorTimeoutTask);
                                }
                            });
                            return;
                        } else if (result.equalsIgnoreCase("reject")) {
                            callbackOnThread(new Runnable() {
                                @Override
                                public void run() {
                                    IMLVBLiveRoomListener.RequestJoinAnchorCallback reqJoinCallback = mJoinAnchorCallback;
                                    if (reqJoinCallback != null) {
                                        reqJoinCallback.onReject(response.reason);
                                        mJoinAnchorCallback = null;
                                    }
                                    mListenerHandler.removeCallbacks(mJoinAnchorTimeoutTask);
                                }
                            });
                            return;
                        }
                    }
                    callbackOnThread(new Runnable() {
                        @Override
                        public void run() {
                            IMLVBLiveRoomListener.RequestJoinAnchorCallback reqJoinCallback = mJoinAnchorCallback;
                            if (reqJoinCallback != null) {
                                reqJoinCallback.onError(MLVBCommonDef.LiveRoomErrorCode.ERROR_PARAMETERS_INVALID, "[LiveRoom] ???????????????????????????[" + message + "]");
                                mJoinAnchorCallback = null;
                            }
                            mListenerHandler.removeCallbacks(mJoinAnchorTimeoutTask);
                        }
                    });
                }
                return;
            }

            KickoutResponse kickreq = new Gson().fromJson(message, KickoutResponse.class);
            if (kickreq != null && kickreq.type.equalsIgnoreCase("kickout")) {
                if (isCmdTimeOut(kickreq.timestamp)) {
                    TXCLog.e(TAG, "[LiveRoom] ????????????????????????");
                    return;
                }
                if (kickreq.roomID.equalsIgnoreCase(mCurrRoomID)) {
                    if (mListener == null) {
                        TXCLog.w(TAG, "no deal with kickout message. listener = null. msg = " + message);
                        return;
                    }
                    callbackOnThread(mListener, "onDebugLog", "[LiveRoom] ?????????????????????");
                    callbackOnThread(mListener, "onKickoutJoinAnchor");
                }
                return;
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void onRecvPKMessage(final String message) {
        try {
            final PKRequest request = new Gson().fromJson(message, PKRequest.class);
            if (request != null && request.type.equalsIgnoreCase("request")) {
                if (mListener == null) {
                    TXCLog.w(TAG, "can not deal with PK reqeust. mListener = null");
                    return;
                }
                if (request.action.equalsIgnoreCase("start")) {
                    if (mPKAnchorInfo == null) {
                        mPKAnchorInfo = new AnchorInfo(request.userID, request.userName, request.userAvatar, request.accelerateURL);
                    }
                    AnchorInfo info = new AnchorInfo(request.userID, request.userName, request.userAvatar, request.accelerateURL);
                    callbackOnThread(mListener, "onDebugLog", String.format("[LiveRoom] ??????PK??????, UserID {%s} UserName {%s}", request.userID, request.userName));
                    callbackOnThread(mListener, "onRequestRoomPK", info);
                } else if (request.action.equalsIgnoreCase("stop")) {
                    callbackOnThread(mListener, "onDebugLog", String.format("[LiveRoom] ??????????????????PK, UserID {%s} UserName {%s}", request.userID, request.userName));
                    AnchorInfo anchorInfo = new AnchorInfo(request.userID, request.userName, request.userAvatar, request.accelerateURL);
                    callbackOnThread(mListener, "onQuitRoomPK", anchorInfo);
                }
                return;
            }

            final PKResponse response = new Gson().fromJson(message, PKResponse.class);
            if (response != null && response.type.equalsIgnoreCase("response")) {
                if (mRequestPKCallback == null) {
                    TXCLog.w(TAG, "can not deal with PK response. mRequestPKCallback = null");
                    return;
                }
                String result = response.result;
                if (result != null) {
                    if (result.equalsIgnoreCase("accept")) {
                        mMixMode = STREAM_MIX_MODE_PK;
                        mPKAnchorInfo.accelerateURL = response.accelerateURL;
                        callbackOnThread(new Runnable() {
                            @Override
                            public void run() {
                                mRequestPKCallback.onAccept(mPKAnchorInfo);
                                mRequestPKCallback = null;
                                mListenerHandler.removeCallbacks(mRequestPKTimeoutTask);
                            }
                        });
                        return;
                    }
                    else if (result.equalsIgnoreCase("reject")) {
                        callbackOnThread(new Runnable() {
                            @Override
                            public void run() {
                                mRequestPKCallback.onReject(response.reason);
                                mRequestPKCallback = null;
                                mListenerHandler.removeCallbacks(mRequestPKTimeoutTask);
                            }
                        });
                        return;
                    }
                }
                callbackOnThread(new Runnable() {
                    @Override
                    public void run() {
                        String msg = "[LiveRoom] ??????????????? PK ??????[" + message + "]";
                        TXCLog.e(TAG, msg);
                        mRequestPKCallback.onError(MLVBCommonDef.LiveRoomErrorCode.ERROR_PARAMETERS_INVALID, msg);
                        mRequestPKCallback = null;
                        mListenerHandler.removeCallbacks(mRequestPKTimeoutTask);
                    }
                });
                return;
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean isCmdTimeOut(long remoteSendTimeMS) {
        long localSendTimeMS = remoteSendTimeMS + mTimeDiff;
        if (System.currentTimeMillis() > (localSendTimeMS + 10000)) {
            //???????????????????????????????????????10???????????????????????????
            return true;
        }
        return false;
    }

    //////////////////////////////////////////
    //
    // HttpRequests.HeartBeatCallback
    //
    //////////////////////////////////////////

    @Override
    public void onHeartBeatResponse(String data) {
        Gson gson = new Gson();
        HttpResponse.PusherList pusherList = gson.fromJson(data, HttpResponse.PusherList.class);
        if (data.contains("roomStatusCode")) {
            mRoomStatusCode = pusherList.roomStatusCode;
        }
        parsePushers(true, pusherList, null);
    }

    //////////////////////////////////////////
    //
    // IMMessageMgr.IMMessageListener
    //
    //////////////////////////////////////////
    /**
     * IM????????????
     */
    @Override
    public void onConnected() {
        TXCLog.d(TAG, "[IM] online");
        callbackOnThread(mListener, "onDebugLog", "[IM] online");
    }

    /**
     * IM????????????
     */
    @Override
    public void onDisconnected() {
        TXCLog.e(TAG, "[IM] offline");
        callbackOnThread(mListener, "onDebugLog", "[IM] offline");
    }

    /**
     * IM????????????????????????????????????
     */
    @Override
    public void onPusherChanged() {
        if (mBackground == false) {
            if (mSelfRoleType == LIVEROOM_ROLE_PUSHER || mJoinPusher) {
                //??????????????????????????????????????????????????????
                TXCLog.d(TAG, "?????? IM ????????????????????????");
                callbackOnThread(mListener, "onDebugLog", "[LiveRoom] updateAnchors called");
                updateAnchors(true, null);
            }
        }
    }

    /**
     * ?????????????????????
     *
     * @param groupID
     * @param senderID
     * @param userName
     * @param headPic
     * @param message
     */
    @Override
    public void onGroupTextMessage(final String groupID, final String senderID, final String userName, final String headPic, final String message) {
        callbackOnThread(mListener, "onRecvRoomTextMsg", groupID, senderID, userName, headPic, message);
    }

    /**
     * ???????????????????????????
     *
     * @param groupID
     * @param senderID
     * @param message
     */
    @Override
    public void onGroupCustomMessage(final String groupID, final String senderID, String message) {
        final CustomMessage customMessage =  new Gson().fromJson(message, CustomMessage.class);
        callbackOnThread(mListener, "onRecvRoomCustomMsg", groupID, senderID, customMessage.userName, customMessage.userAvatar, customMessage.cmd, customMessage.msg);
    }

    /**
     * ??????????????????C2C??????
     *
     * @param sendID
     * @param cmd
     * @param message
     */
    @Override
    public void onC2CCustomMessage(String sendID, String cmd, String message) {
        if (cmd.equalsIgnoreCase("linkmic")) {
            onRecvLinkMicMessage(message);
        }
        else if (cmd.equalsIgnoreCase("pk")) {
            onRecvPKMessage(message);
        }
    }

    /**
     * IM??????????????????
     *
     * @param groupID
     */
    @Override
    public void onGroupDestroyed(String groupID) {
        callbackOnThread(mListener, "onDebugLog", "[LiveRoom] onGroupDestroyed called , group id is " + groupID);
        callbackOnThread(mListener, "onRoomDestroy", mCurrRoomID);
    }

    /**
     * ????????????
     *
     * @param log
     */
    @Override
    public void onDebugLog(String log) {
        TXCLog.d(TAG, log);
        callbackOnThread(mListener, "onDebugLog", log);
    }

    @Override
    public void onGroupMemberEnter(String groupID, ArrayList<TIMUserProfile> users) {
        long nowTime = System.currentTimeMillis();
        if ((nowTime - mLastEnterAudienceTimeMS) > REFRESH_AUDIENCE_INTERVAL_MS) {
            mLastEnterAudienceTimeMS = nowTime;
            int memberCount = 0;
            for (TIMUserProfile userProfile : users) {
                if (memberCount < MAX_MEMBER_SIZE) {
                    //???????????? MAX_MEMBER_SIZE
                    memberCount++;
                    final AudienceInfo audienceInfo = new AudienceInfo();
                    audienceInfo.userID = userProfile.getIdentifier();
                    audienceInfo.userName = userProfile.getNickName();
                    audienceInfo.userAvatar = userProfile.getFaceUrl();
                    if (mAudiences != null) {
                        mAudiences.put(userProfile.getIdentifier(), audienceInfo);
                    }
                    TXCLog.e(TAG, "???????????????.userID:" + audienceInfo.userID + ", nickname:" + audienceInfo.userName + ", userAvatar:" + audienceInfo.userAvatar);
                    callbackOnThread(mListener, "onAudienceEnter", audienceInfo);
                }
            }
        }
    }

    @Override
    public void onGroupMemberExit(String groupID, ArrayList<TIMUserProfile> users) {
        long nowTime = System.currentTimeMillis();
        if ((nowTime - mLastExitAudienceTimeMS) > REFRESH_AUDIENCE_INTERVAL_MS) {
            mLastExitAudienceTimeMS = nowTime;
            int memberCount = 0;
            for (TIMUserProfile userProfile : users) {
                if (memberCount < MAX_MEMBER_SIZE) {
                    //???????????? MAX_MEMBER_SIZE
                    memberCount++;
                    final AudienceInfo audienceInfo = new AudienceInfo();
                    audienceInfo.userID = userProfile.getIdentifier();
                    audienceInfo.userName = userProfile.getNickName();
                    audienceInfo.userAvatar = userProfile.getFaceUrl();
                    TXCLog.e(TAG, "????????????.userID:" + audienceInfo.userID + ", nickname:" + audienceInfo.userName + ", userAvatar:" + audienceInfo.userAvatar);
                    callbackOnThread(mListener, "onAudienceExit", audienceInfo);
                }
            }
        }
    }

    @Override
    public void onForceOffline() {
        callbackOnThread(mListener, "onError", MLVBCommonDef.LiveRoomErrorCode.ERROR_IM_FORCE_OFFLINE, "[LiveRoom] IM ???????????????[?????????????????????????????????]", new Bundle());
    }

    private class StreamMixturer {
        private String              mMainStreamId = "";
        private String              mPKStreamId   = "";
        private Vector<String> mSubStreamIds = new java.util.Vector<String>();
        private int                 mMainStreamWidth = 540;
        private int                 mMainStreamHeight = 960;

        public StreamMixturer() {

        }

        public void setMainVideoStream(String  streamUrl) {
            mMainStreamId = getStreamIDByStreamUrl(streamUrl);

            Log.e(TAG, "MergeVideoStream: setMainVideoStream " + mMainStreamId);
        }

        public void setMainVideoStreamResolution(int width, int height) {
            if (width > 0 && height > 0) {
                mMainStreamWidth = width;
                mMainStreamHeight = height;
            }
        }

        public void addSubVideoStream(String  streamUrl) {
            if (mSubStreamIds.size() > 3) {
                return;
            }

            String streamId = getStreamIDByStreamUrl(streamUrl);

            Log.e(TAG, "MergeVideoStream: addSubVideoStream " + streamId);

            if (streamId == null || streamId.length() == 0) {
                return;
            }

            for (String item: mSubStreamIds) {
                if (item.equalsIgnoreCase(streamId)) {
                    return;
                }
            }

            mSubStreamIds.add(streamId);
            sendStreamMergeRequest(5);
        }

        public void delSubVideoStream(String  streamUrl) {
            String streamId = getStreamIDByStreamUrl(streamUrl);

            Log.e(TAG, "MergeVideoStream: delSubVideoStream " + streamId);

            boolean bExist = false;
            for (String item: mSubStreamIds) {
                if (item.equalsIgnoreCase(streamId)) {
                    bExist = true;
                    break;
                }
            }

            if (bExist == true) {
                mSubStreamIds.remove(streamId);
                sendStreamMergeRequest(1);
            }
        }

        public void addPKVideoStream(String streamUrl) {
            mPKStreamId = getStreamIDByStreamUrl(streamUrl);
            if (mMainStreamId == null || mMainStreamId.length() == 0 || mPKStreamId == null || mPKStreamId.length() == 0) {
                return;
            }

            Log.e(TAG, "MergeVideoStream: addPKVideoStream " + mPKStreamId);

            final JSONObject requestParam = createPKRequestParam();
            if (requestParam == null) {
                return;
            }

            internalSendRequest(5, true, requestParam);
        }

        public void delPKVideoStream(String streamUrl) {
            mPKStreamId = null;
            if (mMainStreamId == null || mMainStreamId.length() == 0) {
                return;
            }

            String streamId = getStreamIDByStreamUrl(streamUrl);
            Log.e(TAG, "MergeVideoStream: delPKStream");

            final JSONObject requestParam = createPKRequestParam();
            if (requestParam == null) {
                return;
            }

            internalSendRequest(1, true, requestParam);
        }

        public void resetMergeState() {
            Log.e(TAG, "MergeVideoStream: resetMergeState");

            mSubStreamIds.clear();
            mMainStreamId = null;
            mPKStreamId   = null;
            mMainStreamWidth = 540;
            mMainStreamHeight = 960;
        }

        private void sendStreamMergeRequest(final int retryCount) {
            if (mMainStreamId == null || mMainStreamId.length() == 0) {
                return;
            }

            final JSONObject requestParam = createRequestParam();
            if (requestParam == null) {
                return;
            }

            internalSendRequest(retryCount, true, requestParam);
        }

        private void internalSendRequest(final int retryIndex, final boolean runImmediately, final JSONObject requestParam) {
            new Thread() {
                @Override
                public void run() {
                    if (runImmediately == false) {
                        try {
                            sleep(2000, 0);
                        }
                        catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    String streamsInfo = "mainStream: " + mMainStreamId;
                    for (int i = 0; i < mSubStreamIds.size(); ++i) {
                        streamsInfo = streamsInfo + " subStream" + i + ": " + mSubStreamIds.get(i);
                    }

                    Log.e(TAG, "MergeVideoStream: send request, " + streamsInfo + " retryIndex: " + retryIndex + "    " + requestParam.toString());
                    if (mHttpRequest != null) {
                        mHttpRequest.mergeStream(mCurrRoomID, mSelfAccountInfo.userID, requestParam, new HttpRequests.OnResponseCallback<HttpResponse.MergeStream>() {
                            @Override
                            public void onResponse(int retcode, String strMessage, HttpResponse.MergeStream result) {
                                Log.e(TAG, "MergeVideoStream: recv response, message = " + (result != null ? "[code = " + result.code + " msg = " + result.message + " merge_code = " + result.merge_code + "]" : "null"));

                                if (result != null && result.code == 0 && result.merge_code == 0) {
                                    return;
                                }
                                else {
                                    int tempRetryIndex = retryIndex - 1;
                                    if (tempRetryIndex > 0) {
                                        internalSendRequest(tempRetryIndex, false, requestParam);
                                    }
                                }
                            }
                        });
                    }
                }
            }.start();
        }

        private JSONObject createRequestParam() {

            JSONObject requestParam = null;

            try {
                // input_stream_list
                JSONArray inputStreamList = new JSONArray();

                // ?????????
                {
                    JSONObject layoutParam = new JSONObject();
                    layoutParam.put("image_layer", 1);

                    JSONObject mainStream = new JSONObject();
                    mainStream.put("input_stream_id", mMainStreamId);
                    mainStream.put("layout_params", layoutParam);

                    inputStreamList.put(mainStream);
                }

                int subWidth  = 160;
                int subHeight = 240;
                int offsetHeight = 90;
                if (mMainStreamWidth < 540 || mMainStreamHeight < 960) {
                    subWidth  = 120;
                    subHeight = 180;
                    offsetHeight = 60;
                }
                int subLocationX = mMainStreamWidth - subWidth;
                int subLocationY = mMainStreamHeight - subHeight - offsetHeight;

                // ?????????
                int layerIndex = 0;
                for (String item : mSubStreamIds) {
                    JSONObject layoutParam = new JSONObject();
                    layoutParam.put("image_layer", layerIndex + 2);
                    layoutParam.put("image_width", subWidth);
                    layoutParam.put("image_height", subHeight);
                    layoutParam.put("location_x", subLocationX);
                    layoutParam.put("location_y", subLocationY - layerIndex * subHeight);

                    JSONObject subStream = new JSONObject();
                    subStream.put("input_stream_id", item);
                    subStream.put("layout_params", layoutParam);

                    inputStreamList.put(subStream);
                    ++layerIndex;
                }

                // para
                JSONObject para = new JSONObject();
                para.put("app_id", "");
                para.put("interface", "mix_streamv2.start_mix_stream_advanced");
                para.put("mix_stream_session_id", mMainStreamId);
                para.put("output_stream_id", mMainStreamId);
                para.put("input_stream_list", inputStreamList);

                // interface
                JSONObject interfaceObj = new JSONObject();
                interfaceObj.put("interfaceName", "Mix_StreamV2");
                interfaceObj.put("para", para);

                // requestParam
                requestParam = new JSONObject();
                requestParam.put("timestamp", System.currentTimeMillis() / 1000);
                requestParam.put("eventId", System.currentTimeMillis() / 1000);
                requestParam.put("interface", interfaceObj);
            }
            catch (Exception e) {
                e.printStackTrace();
            }

            return requestParam;
        }

        private JSONObject createPKRequestParam() {

            if (mMainStreamId == null || mMainStreamId.length() == 0) {
                return null;
            }

            JSONObject requestParam = null;

            try {
                // input_stream_list
                JSONArray inputStreamList = new JSONArray();

                if (mPKStreamId != null && mPKStreamId.length() > 0){
                    // ??????
                    {
                        JSONObject layoutParam = new JSONObject();
                        layoutParam.put("image_layer", 1);
                        layoutParam.put("input_type", 3);
                        layoutParam.put("image_width", 720);
                        layoutParam.put("image_height", 640);

                        JSONObject canvasStream = new JSONObject();
                        canvasStream.put("input_stream_id", mMainStreamId);
                        canvasStream.put("layout_params", layoutParam);

                        inputStreamList.put(canvasStream);
                    }

                    // mainStream
                    {
                        JSONObject layoutParam = new JSONObject();
                        layoutParam.put("image_layer", 2);
                        layoutParam.put("image_width", 360);
                        layoutParam.put("image_height", 640);
                        layoutParam.put("location_x", 0);
                        layoutParam.put("location_y", 0);

                        JSONObject mainStream = new JSONObject();
                        mainStream.put("input_stream_id", mMainStreamId);
                        mainStream.put("layout_params", layoutParam);

                        inputStreamList.put(mainStream);
                    }

                    // subStream
                    {
                        JSONObject layoutParam = new JSONObject();
                        layoutParam.put("image_layer", 3);
                        layoutParam.put("image_width", 360);
                        layoutParam.put("image_height", 640);
                        layoutParam.put("location_x", 360);
                        layoutParam.put("location_y", 0);

                        JSONObject mainStream = new JSONObject();
                        mainStream.put("input_stream_id", mPKStreamId);
                        mainStream.put("layout_params", layoutParam);

                        inputStreamList.put(mainStream);
                    }
                }
                else {
                    JSONObject layoutParam = new JSONObject();
                    layoutParam.put("image_layer", 1);

                    JSONObject canvasStream = new JSONObject();
                    canvasStream.put("input_stream_id", mMainStreamId);
                    canvasStream.put("layout_params", layoutParam);

                    inputStreamList.put(canvasStream);
                }

                // para
                JSONObject para = new JSONObject();
                para.put("app_id", "");
                para.put("interface", "mix_streamv2.start_mix_stream_advanced");
                para.put("mix_stream_session_id", mMainStreamId);
                para.put("output_stream_id", mMainStreamId);
                para.put("input_stream_list", inputStreamList);

                // interface
                JSONObject interfaceObj = new JSONObject();
                interfaceObj.put("interfaceName", "Mix_StreamV2");
                interfaceObj.put("para", para);

                // requestParam
                requestParam = new JSONObject();
                requestParam.put("timestamp", System.currentTimeMillis() / 1000);
                requestParam.put("eventId", System.currentTimeMillis() / 1000);
                requestParam.put("interface", interfaceObj);
            }
            catch (Exception e) {
                e.printStackTrace();
            }

            return requestParam;
        }

        private String getStreamIDByStreamUrl(String strStreamUrl) {
            if (strStreamUrl == null || strStreamUrl.length() == 0) {
                return null;
            }

            //?????????????????????rtmp://8888.livepush.myqcloud.com/path/8888_test_12345_test?txSecret=aaaa&txTime=bbbb
            //?????????????????????rtmp://8888.liveplay.myqcloud.com/path/8888_test_12345_test
            //            http://8888.liveplay.myqcloud.com/path/8888_test_12345_test.flv
            //            http://8888.liveplay.myqcloud.com/path/8888_test_12345_test.m3u8


            String subString = strStreamUrl;

            {
                //1 ??????????????? ??????????????????
                int index = subString.indexOf("?");
                if (index != -1) {
                    subString = subString.substring(0, index);
                }
                if (subString == null || subString.length() == 0) {
                    return null;
                }
            }

            {
                //2 ?????????????????? / ???????????????
                int index = subString.lastIndexOf("/");
                if (index != -1) {
                    subString = subString.substring(index + 1);
                }

                if (subString == null || subString.length() == 0) {
                    return null;
                }
            }

            {
                //3 ??????????????? . ???????????????
                int index = subString.indexOf(".");
                if (index != -1) {
                    subString = subString.substring(0, index);
                }
                if (subString == null || subString.length() == 0) {
                    return null;
                }
            }

            return subString;
        }
    }

    protected class HeartBeatThread {
        private Handler handler;
        private HeartBean heartBean;

        public HeartBeatThread() {
        }

        private Runnable heartBeatRunnable = new Runnable() {
            @Override
            public void run() {
                Handler localHander = handler;
                if (localHander == null) {
                    return;
                }
                if (mSelfAccountInfo != null && mSelfAccountInfo.userID != null && mSelfAccountInfo.userID.length() > 0 && mCurrRoomID != null && mCurrRoomID.length() > 0) {
                    if (mHttpRequest != null) {
                        mHttpRequest.heartBeat(mSelfAccountInfo.userID, mCurrRoomID, mRoomStatusCode);
                        if(heartBean == null){
                            heartBean = new HeartBean();
                        }
                        heartBean.setLikeCount(mHeartCount);
                        heartBean.setWatchCount(mTotalMemberCount);
                        heartBean.setLiveId(TCApplication.Companion.getCurrentPlayId());
                        OkGo.<BaseResponse<String>>put(Constant.HEART_BEAT)
                                .upJson(FastJsonUtil.createJsonString(heartBean))
                                .execute(new JsonCallBack<BaseResponse<String>>() {
                                    @Override
                                    public void onSuccess(Response<BaseResponse<String>> response) {

                                    }
                                });
                    }
                    localHander.postDelayed(heartBeatRunnable, 5000);
                }
            }
        };

        public void startHeartbeat(){
            synchronized (this) {
                if (handler != null && handler.getLooper() != null) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                        handler.getLooper().quitSafely();
                    } else {
                        handler.getLooper().quit();
                    }
                }
                HandlerThread thread = new HandlerThread("HeartBeatThread");
                thread.start();
                handler = new Handler(thread.getLooper());
                handler.postDelayed(heartBeatRunnable, 1000);
            }
        }

        public void stopHeartbeat(){
            synchronized (this) {
                if (handler != null) {
                    handler.removeCallbacks(heartBeatRunnable);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                        handler.getLooper().quitSafely();
                    } else {
                        handler.getLooper().quit();
                    }
                    handler = null;
                }
            }
        }
    }

    private class TXLivePushListenerImpl implements ITXLivePushListener {
        private StandardCallback mCallback = null;

        public void setCallback(StandardCallback callback) {
            mCallback = callback;
        }

        @Override
        public void onPushEvent(final int event, final Bundle param) {
            if (event == TXLiveConstants.PUSH_EVT_PUSH_BEGIN) {
                TXCLog.d(TAG, "????????????");
                callbackOnThread(mCallback, "onSuccess");
            } else if (event == TXLiveConstants.PUSH_ERR_OPEN_CAMERA_FAIL) {
                String msg = "[LivePusher] ????????????[?????????????????????]";
                TXCLog.e(TAG, msg);
                callbackOnThread(mCallback, "onError", event, msg);
            } else if (event == TXLiveConstants.PUSH_ERR_OPEN_MIC_FAIL) {
                String msg = "[LivePusher] ????????????[?????????????????????]";
                TXCLog.e(TAG, msg);
                callbackOnThread(mCallback, "onError", event, msg);
            } else if (event == TXLiveConstants.PUSH_ERR_NET_DISCONNECT || event == TXLiveConstants.PUSH_ERR_INVALID_ADDRESS) {
                String msg = "[LivePusher] ????????????[????????????]";
                TXCLog.e(TAG,msg);
                callbackOnThread(mCallback, "onError", event, msg);
            } else if (event == TXLiveConstants.PUSH_ERR_SCREEN_CAPTURE_START_FAILED) {
                String msg = "[LivePusher] ????????????[??????????????????]";
                TXCLog.e(TAG,msg);
                callbackOnThread(mCallback, "onError", event, msg);
            }
        }

        @Override
        public void onNetStatus(Bundle status) {

        }
    }

    @Override
    public TXAudioEffectManager getAudioEffectManager() {
        if (mTXLivePusher == null) {
            mTXLivePusher = new TXLivePusher(mAppContext);
        }
        return mTXLivePusher.getAudioEffectManager();
    }

    private void callbackOnThread(final Object object, final String methodName, final Object... args) {
        if (object == null || methodName == null || methodName.length() == 0) {
            return;
        }
        mListenerHandler.post(new Runnable() {
            @Override
            public void run() {
                Class objClass = object.getClass();
                while (objClass != null) {
                    Method[] methods = objClass.getDeclaredMethods();
                    for (Method method : methods) {
                        if (method.getName() == methodName) {
                            try {
                                method.invoke(object, args);
                            } catch (IllegalAccessException e) {
                                e.printStackTrace();
                            } catch (InvocationTargetException e) {
                                e.printStackTrace();
                            }
                            return;
                        }
                    }
                    objClass = objClass.getSuperclass();
                }
            }
        });
    }

    private void callbackOnThread(final Runnable runnable) {
        if (runnable == null) {
            return;
        }
        mListenerHandler.post(new Runnable() {
            @Override
            public void run() {
                runnable.run();
            }
        });
    }

    private  class PlayerItem {
        public TXCloudVideoView view;
        public AnchorInfo       anchorInfo;
        public TXLivePlayer     player;

        public PlayerItem(TXCloudVideoView view, AnchorInfo anchorInfo, TXLivePlayer player) {
            this.view = view;
            this.anchorInfo = anchorInfo;
            this.player = player;
        }

        public void resume(){
            this.player.resume();
        }

        public void pause(){
            this.player.pause();
        }

        public void destroy(){
            this.player.stopPlay(true);
            this.view.onDestroy();
        }
    }

    protected class CommonJson<T> {
        public String cmd;
        public T      data;
        public CommonJson() {
        }
    }

    private class JoinAnchorRequest {
        public String type;
        public String roomID;
        public String userID;
        public String userName;
        public String userAvatar;
        public String reason;
        public long timestamp;
    }

    private class JoinAnchorResponse {
        public String type;
        public String roomID;
        public String result;
        public String reason;
        public long timestamp;
    }

    private class KickoutResponse {
        public String type;
        public String roomID;
        public long timestamp;
    }

    private class PKRequest {
        public String type;
        public String action;
        public String roomID;
        public String userID;
        public String userName;
        public String userAvatar;
        public String accelerateURL;
        public long timestamp;
    }

    private class PKResponse {
        public String type;
        public String roomID;
        public String result;
        public String reason;
        public String accelerateURL;
        public long timestamp;
    }

    protected class CustomMessage{
        public String userName;
        public String userAvatar;
        public String cmd;
        public String msg;
    }

    public interface StandardCallback {
        /**
         * @param errCode ?????????
         * @param errInfo ????????????
         */
        void onError(int errCode, String errInfo);

        void onSuccess();
    }

    protected interface UpdateAnchorsCallback {
        void onUpdateAnchors(int errcode, List<AnchorInfo> addAnchors, List<AnchorInfo> delAnchors, HashMap<String, AnchorInfo> mergedAnchors, AnchorInfo roomCreator);
    }

    public long getTotalMemberCount() {
        return mTotalMemberCount;
    }

    public void setTotalMemberCount(long mTotalMemberCount) {
        this.mTotalMemberCount = mTotalMemberCount;
    }

    public long getCurrentMemberCount() {
        return mCurrentMemberCount;
    }

    public void setCurrentMemberCount(long mCurrentMemberCount) {
        this.mCurrentMemberCount = mCurrentMemberCount;
    }

    public long getHeartCount() {
        return mHeartCount;
    }

    public void setHeartCount(long mHeartCount) {
        this.mHeartCount = mHeartCount;
    }
}
