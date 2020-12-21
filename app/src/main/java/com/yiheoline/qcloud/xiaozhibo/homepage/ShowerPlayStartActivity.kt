package com.yiheoline.qcloud.xiaozhibo.homepage

import android.content.Intent
import com.yiheoline.liteav.demo.lvb.liveroom.MLVBLiveRoom
import com.yiheoline.qcloud.xiaozhibo.TCApplication
import com.yiheoline.qcloud.xiaozhibo.anchor.TCCameraAnchorActivity
import com.yiheoline.qcloud.xiaozhibo.base.BaseActivity
import com.yiheoline.qcloud.xiaozhibo.common.utils.TCConstants
import com.yiheonline.qcloud.xiaozhibo.R
import kotlinx.android.synthetic.main.activity_shower_play_start.*
import org.jetbrains.anko.sdk27.coroutines.onClick
import org.jetbrains.anko.toast

class ShowerPlayStartActivity : BaseActivity() {
    var mLiveRoom: MLVBLiveRoom = MLVBLiveRoom.sharedInstance(this);
    override fun getLayoutId(): Int {
        return R.layout.activity_shower_play_start
    }

    override fun initView() {
        super.initView()
//        mTXCloudVideoView.setLogMargin(10f, 10f, 45f, 55f)

        // 打开本地预览，传入预览的 View
        mLiveRoom.startLocalPreview(true, mTXCloudVideoView)

        startPlayBtn.onClick {
            var title = titleInputView.text.toString()
            if(title.isEmpty()){
                toast("请输入标题")
                return@onClick
            }
            var intent = Intent(mContext, TCCameraAnchorActivity::class.java)
            intent.putExtra(TCConstants.ROOM_TITLE,title)
            intent.putExtra(TCConstants.USER_ID,TCApplication.userId)
            startActivity(intent)
            finish()
        }

        backView.onClick {
            finish()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mLiveRoom.stopLocalPreview()
    }
}