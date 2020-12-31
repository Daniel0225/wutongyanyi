package com.yiheoline.qcloud.xiaozhibo

class Constant {
    companion object {
        //基础地址
//        private const val BASE_URL: String = "http://192.168.0.3:8085/"//
//      const val BASE_URL: String = "https://test.api.air.yihenst.com/"
        const val BASE_URL: String = "http://192.168.0.85:8085/"
        const val IMAGE_BASE = "http://test.img.yihenst.com/"
        const val MUSIC_BASE = "http://test.audio.yihenst.com"
        const val VIDEO_BASE = "http://test.video.yihenst.com/"
        const val FILE_BASE = "http://test.file.yihenst.com/"

        const val PAGE_SIZE = 10

        const val CODE_LOGIN = BASE_URL + "codeLogin"
        const val SEND_CODE = BASE_URL + "sendCode"
        const val SDK_INFO = BASE_URL + "mlvb/user/info"
        const val HOME_PAGE_LIST = BASE_URL + "notice/queryNoticeList"
        const val INSERT_NOTICE = BASE_URL + "notice/insertNotice"
        const val QUERY_CAT_LIST = BASE_URL + "queryCatList"
        const val UPLOAD_PIC = BASE_URL + "uploadPic"
        const val QUERY_TAG_LIST = BASE_URL + "queryPresetTagList"
        const val QUERY_MY_NOTICE_LIST = BASE_URL + "notice/queryMyNoticeList"
        const val NOTICE_INTENT = BASE_URL + "notice/intent"
        const val NOTICE_DETAIL = BASE_URL + "notice/noticeDetail"
        const val START_LIVE = BASE_URL + "live/theater/start"
        const val PLACE_ORDER = BASE_URL + "order/placeAnOrder"
        const val FINISH_PLAY = BASE_URL + "live/theater/finish"
        const val ONLINE_PLAY = BASE_URL + "live/theater/online/"
        const val VIDEO_QUERY_CHOICE = BASE_URL + "video/queryChoice"
        const val VIDEO_DETAIL = BASE_URL + "video/findVideoDetail"
        const val ORDER_PAYMENT = BASE_URL + "order/payment"
        const val VIDEO_LIKE = BASE_URL + "video/videoLike"
        const val UN_LIKE_VIDEO = BASE_URL + "video/cancelVideoLike"
        const val VIDEO_COMMENT_LIST = BASE_URL + "video/getVideoComment"
        const val COMMENT_LIKE = BASE_URL + "video/commentLike"
        const val COMMENT_UNLIKE = BASE_URL + "video/cancelCommentLike"
        const val QUERY_VIDEO_LIST = BASE_URL + "video/queryVideoList"
        const val VIDEO_COMMENT = BASE_URL + "video/videoComment"
        const val VIDEO_COLLECT = BASE_URL + "video/videoCollect"
        const val CANCEL_VIDEO_COLLECT = BASE_URL + "video/cancelVideoCollect"
        const val HEART_BEAT = BASE_URL + "live/theater/heartbeat"
        const val SEND_GIFT = BASE_URL + "live/theater/gift"

    }
}