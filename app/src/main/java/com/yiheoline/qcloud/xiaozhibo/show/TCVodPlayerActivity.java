package com.yiheoline.qcloud.xiaozhibo.show;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.bumptech.glide.Glide;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.model.HttpParams;
import com.lzy.okgo.model.Response;
import com.tencent.liteav.basic.log.TXCLog;
import com.tencent.qcloud.ugckit.UGCKitConstants;
import com.tencent.qcloud.ugckit.utils.BitmapUtils;
import com.tencent.qcloud.ugckit.utils.LogReport;
import com.tencent.qcloud.ugckit.utils.TCUserMgr;
import com.tencent.qcloud.ugckit.utils.TelephonyUtil;
import com.tencent.qcloud.ugckit.utils.ToastUtil;
import com.tencent.rtmp.ITXVodPlayListener;
import com.tencent.rtmp.TXLiveConstants;
import com.tencent.rtmp.TXLog;
import com.tencent.rtmp.TXVodPlayConfig;
import com.tencent.rtmp.TXVodPlayer;
import com.tencent.rtmp.ui.TXCloudVideoView;
import com.yiheoline.qcloud.xiaozhibo.Constant;
import com.yiheoline.qcloud.xiaozhibo.bean.PlayerInfo;
import com.yiheoline.qcloud.xiaozhibo.bean.ShortVideoBean;
import com.yiheoline.qcloud.xiaozhibo.dialog.ListBottomSheetDialogFragment;
import com.yiheoline.qcloud.xiaozhibo.http.BaseResponse;
import com.yiheoline.qcloud.xiaozhibo.http.JsonCallBack;
import com.yiheoline.qcloud.xiaozhibo.login.TCLoginActivity;
import com.yiheoline.qcloud.xiaozhibo.profile.ShowerInfoActivity;
import com.yiheoline.qcloud.xiaozhibo.utils.TimeUtil;
import com.yiheonline.qcloud.xiaozhibo.R;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import fr.castorflex.android.verticalviewpager.VerticalViewPager;

public class TCVodPlayerActivity extends AppCompatActivity implements ITXVodPlayListener, TelephonyUtil.OnTelephoneListener {
    private static final String TAG = "TCVodPlayerActivity";
    private VerticalViewPager mVerticalViewPager;
    private MyPagerAdapter mPagerAdapter;
    private TXCloudVideoView mTXCloudVideoView;
    private TextView mTvBack;
    private ImageView mIvCover;
    // ?????????id ?????????????????? ?????????????????? ??????URL??? ??????URL
    private List<ShortVideoBean> mTCLiveInfoList;
    private int mInitTCLiveInfoPosition;
    private int mCurrentPosition;
    private int jumpType = 0;//????????????????????? 0 ???????????????  1?????????????????????  ??????????????????????????????????????????
    /**
     * SDK?????????????????????
     */
    private TXVodPlayer mTXVodPlayer;
    private Context context;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);
        context = this;
        initDatas();
        initViews();
        initPlayerSDK();

        TelephonyUtil.getInstance().setOnTelephoneListener(this);
        TelephonyUtil.getInstance().initPhoneListener();

        //?????????????????????????????????????????????????????????sdk??????????????????????????????????????????????????????
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void initDatas() {
        Intent intent = getIntent();
        mTCLiveInfoList = (List<ShortVideoBean>) intent.getSerializableExtra(UGCKitConstants.TCLIVE_INFO_LIST);
        mInitTCLiveInfoPosition = intent.getIntExtra(UGCKitConstants.TCLIVE_INFO_POSITION, 0);
        jumpType = intent.getIntExtra("jumpType",0);
    }

    private void initViews() {
        mTXCloudVideoView = (TXCloudVideoView) findViewById(R.id.player_cloud_view);
        mIvCover = (ImageView) findViewById(R.id.player_iv_cover);
        mTvBack = (TextView) findViewById(R.id.player_tv_back);
        mTvBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        mVerticalViewPager = (VerticalViewPager) findViewById(R.id.vertical_view_pager);
        mVerticalViewPager.setOffscreenPageLimit(2);
        mVerticalViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                TXLog.d(TAG, "mVerticalViewPager, onPageScrolled position = " + position);
            }

            @Override
            public void onPageSelected(int position) {
                TXLog.d(TAG, "dingying mVerticalViewPager, onPageSelected position = " + position);
                mCurrentPosition = position;
                // ??????????????????????????????????????????????????????seek???0
                TXLog.d(TAG, "??????????????????????????????????????????mTXVodPlayer = " + mTXVodPlayer);
                if (mTXVodPlayer != null) {
                    mTXVodPlayer.seek(0);
                    mTXVodPlayer.pause();
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });

        mVerticalViewPager.setPageTransformer(false, new ViewPager.PageTransformer() {
            @Override
            public void transformPage(View page, float position) {
                TXLog.d(TAG, "dingying mVerticalViewPager, transformPage pisition = " + position + " mCurrentPosition" + mCurrentPosition);
                if (position != 0) {
                    return;
                }

                ViewGroup viewGroup = (ViewGroup) page;
                mIvCover = (ImageView) viewGroup.findViewById(R.id.player_iv_cover);
                mTXCloudVideoView = (TXCloudVideoView) viewGroup.findViewById(R.id.player_cloud_view);


                PlayerInfo playerInfo = mPagerAdapter.findPlayerInfo(mCurrentPosition);
                if (playerInfo != null) {
                    playerInfo.vodPlayer.resume();
                    mTXVodPlayer = playerInfo.vodPlayer;
                }
            }
        });

        mPagerAdapter = new MyPagerAdapter();
        mVerticalViewPager.setAdapter(mPagerAdapter);
    }

    private boolean isLogin() {
        if (!TCUserMgr.getInstance().hasUser()) {
            Intent intent = new Intent(this, TCLoginActivity.class);
            startActivity(intent);
            return false;
        }
        return true;
    }

    class MyPagerAdapter extends PagerAdapter {

        ArrayList<PlayerInfo> playerInfoList = new ArrayList<>();

        protected PlayerInfo instantiatePlayerInfo(int position) {
            TXCLog.d(TAG, "instantiatePlayerInfo " + position);

            PlayerInfo playerInfo = new PlayerInfo();
            TXVodPlayer vodPlayer = new TXVodPlayer(TCVodPlayerActivity.this);
            vodPlayer.setRenderRotation(TXLiveConstants.RENDER_ROTATION_PORTRAIT);
            //FIXBUG:FULL_SCREEN ?????????????????????ADJUST_RESOLUTION??????
            vodPlayer.setRenderMode(TXLiveConstants.RENDER_MODE_ADJUST_RESOLUTION);
            vodPlayer.setVodListener(TCVodPlayerActivity.this);
            TXVodPlayConfig config = new TXVodPlayConfig();

            File sdcardDir = getExternalFilesDir(null);
            if (sdcardDir != null) {
                config.setCacheFolderPath(sdcardDir.getAbsolutePath() + "/txcache");
            }
            config.setMaxCacheItems(5);
            vodPlayer.setConfig(config);
            vodPlayer.setAutoPlay(false);

            ShortVideoBean shortVideoBean = mTCLiveInfoList.get(position);
            playerInfo.playURL = shortVideoBean.getVideoPath();
            playerInfo.vodPlayer = vodPlayer;
            playerInfo.reviewstatus = 1;
            playerInfo.pos = position;
            playerInfoList.add(playerInfo);

            return playerInfo;
        }

        protected void destroyPlayerInfo(int position) {
            while (true) {
                PlayerInfo playerInfo = findPlayerInfo(position);
                if (playerInfo == null) {
                    break;
                }
                playerInfo.vodPlayer.stopPlay(true);
                playerInfoList.remove(playerInfo);

                TXCLog.d(TAG, "destroyPlayerInfo " + position);
            }
        }

        public PlayerInfo findPlayerInfo(int position) {
            for (int i = 0; i < playerInfoList.size(); i++) {
                PlayerInfo playerInfo = playerInfoList.get(i);
                if (playerInfo.pos == position) {
                    return playerInfo;
                }
            }
            return null;
        }

        public PlayerInfo findPlayerInfo(TXVodPlayer player) {
            for (int i = 0; i < playerInfoList.size(); i++) {
                PlayerInfo playerInfo = playerInfoList.get(i);
                if (playerInfo.vodPlayer == player) {
                    return playerInfo;
                }
            }
            return null;
        }

        public void onDestroy() {
            for (PlayerInfo playerInfo : playerInfoList) {
                playerInfo.vodPlayer.stopPlay(true);
            }
            playerInfoList.clear();
        }

        @Override
        public int getCount() {
            return mTCLiveInfoList.size();
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            TXCLog.i(TAG, " dingying MyPagerAdapter instantiateItem, position = " + position);
            ShortVideoBean shortVideoBean = mTCLiveInfoList.get(position);

            View view = LayoutInflater.from(container.getContext()).inflate(R.layout.view_player_content, null);
            view.setId(position);

            // ??????
            ImageView coverImageView = (ImageView) view.findViewById(R.id.player_iv_cover);
            BitmapUtils.blurBgPic(TCVodPlayerActivity.this, coverImageView, Constant.IMAGE_BASE+shortVideoBean.getCover(), R.drawable.bg);
            // ??????
            CircleImageView ivAvatar = (CircleImageView) view.findViewById(R.id.player_civ_avatar);
            Glide.with(TCVodPlayerActivity.this).load(Constant.IMAGE_BASE+shortVideoBean.getAvatar()).error(R.drawable.face).into(ivAvatar);
            ivAvatar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(jumpType == 0){
                        Intent intent = new Intent(TCVodPlayerActivity.this, ShowerInfoActivity.class);
                        intent.putExtra("userId",shortVideoBean.getUserId());
                        startActivity(intent);
                    }else{
                        finish();
                    }
                }
            });
            // ??????
            TextView tvName = (TextView) view.findViewById(R.id.player_tv_publisher_name);
            tvName.setText(shortVideoBean.getNickname() + " ?? " + TimeUtil.getMonthAndDay(Long.parseLong(shortVideoBean.getCreateTime())));

            TextView titleView = (TextView) view.findViewById(R.id.titleView);
            titleView.setText(shortVideoBean.getTitle());

            TextView likeNumView = (TextView)view.findViewById(R.id.likeNumView);
            likeNumView.setText(String.valueOf(shortVideoBean.getLikes()));
            ImageView likeImageView = (ImageView) view.findViewById(R.id.likeImageView);
            if(shortVideoBean.getIsLike() == null){
                likeImageView.setImageResource(R.mipmap.video_unlike);
            }else{
                likeImageView.setImageResource(R.mipmap.video_like);
            }
            likeImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(shortVideoBean.getIsLike() == null){
                        shortVideoBean.setIsLike("1");
                        like(shortVideoBean.getShortVideoId());
                        likeImageView.setImageResource(R.mipmap.video_like);
                    }else{
                        shortVideoBean.setIsLike(null);
                        unLike(shortVideoBean.getShortVideoId());
                        likeImageView.setImageResource(R.mipmap.video_unlike);
                    }

                }
            });
            //??????
            ImageView commentView = (ImageView)view.findViewById(R.id.commentView);
            commentView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ListBottomSheetDialogFragment dialogFragment =
                            ListBottomSheetDialogFragment.newInstance(String.valueOf(shortVideoBean.getShortVideoId()),"");
                    dialogFragment.setCancelable(false);
                    dialogFragment.show(getSupportFragmentManager(),null);
                }
            });
            ImageView followView = (ImageView)view.findViewById(R.id.followBtn);
            if(shortVideoBean.getIsFollow() == null){
                followView.setImageResource(R.mipmap.guanzhu);
            }else{
                followView.setImageResource(R.mipmap.followed);
            }
            followView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    followView.setVisibility(View.GONE);
                    follow(String.valueOf(shortVideoBean.getUserId()));
                }
            });
            // ?????????player
            TXCloudVideoView playView = (TXCloudVideoView) view.findViewById(R.id.player_cloud_view);
            PlayerInfo playerInfo = instantiatePlayerInfo(position);
            playerInfo.playerView = playView;
            playerInfo.vodPlayer.setPlayerView(playView);

            if (playerInfo.reviewstatus == TCVideoInfo.REVIEW_STATUS_NORMAL) {
                playerInfo.vodPlayer.startPlay(playerInfo.playURL);
            } else if (playerInfo.reviewstatus == TCVideoInfo.REVIEW_STATUS_NOT_REVIEW) { // ?????????
            } else if (playerInfo.reviewstatus == TCVideoInfo.REVIEW_STATUS_PORN) {       // ??????

            }
            container.addView(view);
            return view;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            TXCLog.i(TAG, "MyPagerAdapter destroyItem, position = " + position);

            destroyPlayerInfo(position);

            container.removeView((View) object);
        }
    }

    private void initPlayerSDK() {
        mVerticalViewPager.setCurrentItem(mInitTCLiveInfoPosition);
    }

    private void restartPlay() {
        if (mTXVodPlayer != null) {
            mTXVodPlayer.resume();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mTXCloudVideoView != null) {
            mTXCloudVideoView.onResume();
        }
        if (mTXVodPlayer != null) {
            mTXVodPlayer.resume();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mTXCloudVideoView != null) {
            mTXCloudVideoView.onPause();
        }
        if (mTXVodPlayer != null) {
            mTXVodPlayer.pause();
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mTXCloudVideoView != null) {
            mTXCloudVideoView.onDestroy();
            mTXCloudVideoView = null;
        }

        mPagerAdapter.onDestroy();
        stopPlay(true);
        mTXVodPlayer = null;

        TelephonyUtil.getInstance().uninitPhoneListener();
    }

    protected void stopPlay(boolean clearLastFrame) {
        if (mTXVodPlayer != null) {
            mTXVodPlayer.stopPlay(clearLastFrame);
        }
    }

    @Override
    public void onPlayEvent(TXVodPlayer player, int event, Bundle param) {
        if (event == TXLiveConstants.PLAY_EVT_CHANGE_RESOLUTION) {
            int width = param.getInt(TXLiveConstants.EVT_PARAM1);
            int height = param.getInt(TXLiveConstants.EVT_PARAM2);
            //FIXBUG:????????????????????????????????????????????????
        } else if (event == TXLiveConstants.PLAY_EVT_PLAY_END) {
            restartPlay();
        } else if (event == TXLiveConstants.PLAY_EVT_RCV_FIRST_I_FRAME) {// ??????I????????????????????????

            PlayerInfo playerInfo = mPagerAdapter.findPlayerInfo(player);
            if (playerInfo != null) {
                playerInfo.isBegin = true;
            }
            if (mTXVodPlayer == player) {
                TXLog.i(TAG, "onPlayEvent, event I FRAME, player = " + player);
                mIvCover.setVisibility(View.GONE);

                LogReport.getInstance().reportVodPlaySucc(event);
            }
        } else if (event == TXLiveConstants.PLAY_EVT_VOD_PLAY_PREPARED) {
            if (mTXVodPlayer == player) {
                TXLog.i(TAG, "onPlayEvent, event prepared, player = " + player);
                mTXVodPlayer.resume();
            }
        } else if (event == TXLiveConstants.PLAY_EVT_PLAY_BEGIN) {
            PlayerInfo playerInfo = mPagerAdapter.findPlayerInfo(player);
            if (playerInfo != null && playerInfo.isBegin) {
                mIvCover.setVisibility(View.GONE);
                TXCLog.i(TAG, "onPlayEvent, event begin, cover remove");
            }
        } else if (event < 0) {
            if (mTXVodPlayer == player) {
                TXLog.i(TAG, "onPlayEvent, event prepared, player = " + player);

                LogReport.getInstance().reportVodPlayFail(event);
            }

            ToastUtil.toastShortMessage("event:" + event);
        }
    }

    @Override
    public void onNetStatus(TXVodPlayer player, Bundle status) {

    }

    @Override
    public void onRinging() {
        if (mTXVodPlayer != null) {
            mTXVodPlayer.setMute(true);
        }
    }

    @Override
    public void onOffhook() {
        if (mTXVodPlayer != null) {
            mTXVodPlayer.setMute(true);
        }
    }

    @Override
    public void onIdle() {
        if (mTXVodPlayer != null) {
            mTXVodPlayer.setMute(false);
        }
    }

    /**
     * ???????????????
     */
    private void like(int shortVideoId){
        HttpParams httpParams = new HttpParams();
        httpParams.put("shortVideoId",shortVideoId);
        OkGo.<String>post(Constant.SHORT_VIDEO_LIKE)
                .params(httpParams)
                .execute(new JsonCallBack<String>() {
                    @Override
                    public void onSuccess(Response<String> response) {

                    }
                });
    }
    /**
     * ?????????????????????
     */
    private void unLike(int shortVideoId){
        HttpParams httpParams = new HttpParams();
        httpParams.put("shortVideoId",shortVideoId);
        OkGo.<String>post(Constant.CANCEL_SHORT_VIDEO_LIKE)
                .params(httpParams)
                .execute(new JsonCallBack<String>() {
                    @Override
                    public void onSuccess(Response<String> response) {

                    }
                });
    }
    /**
     * ??????
     */
    private void follow(String targetUserId){
        HttpParams httpParams = new HttpParams();
        httpParams.put("targetUserId",targetUserId);
        OkGo.<BaseResponse<String>>post(Constant.USER_FOLLOW)
                .params(httpParams)
                .execute(new JsonCallBack<BaseResponse<String>>() {
                    @Override
                    public void onSuccess(Response<BaseResponse<String>> response) {

                    }
                });
    }
}
