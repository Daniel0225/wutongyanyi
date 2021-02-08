package com.yiheoline.qcloud.xiaozhibo.dialog

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.lzy.okgo.OkGo
import com.lzy.okgo.model.HttpParams
import com.lzy.okgo.model.Response
import com.yiheoline.qcloud.xiaozhibo.Constant
import com.yiheoline.qcloud.xiaozhibo.bean.CommentBean
import com.yiheoline.qcloud.xiaozhibo.http.BaseResponse
import com.yiheoline.qcloud.xiaozhibo.http.JsonCallBack
import com.yiheoline.qcloud.xiaozhibo.http.response.CommentListResponse
import com.yiheoline.qcloud.xiaozhibo.show.adapter.ShortCommentListAdapter
import com.yiheoline.qcloud.xiaozhibo.video.adapter.CommentListAdapter
import com.yiheonline.qcloud.xiaozhibo.R
import kotlinx.android.synthetic.main.fragment_item_list_dialog.*
import org.jetbrains.anko.sdk27.coroutines.onClick
import org.jetbrains.anko.support.v4.toast

class ListBottomSheetDialogFragment : BottomSheetDialogFragment(),InputDialog.SendCommentListener {
    var pageNum = 1
    var shortVideoId = ""
    var commentListAdapter:ShortCommentListAdapter? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //给dialog设置主题为透明背景 不然会有默认的白色背景
        setStyle(DialogFragment.STYLE_NO_FRAME, R.style.CustomBottomSheetDialogTheme)
        if(arguments != null){
            shortVideoId = arguments!!.getString(ARG_PARAM1,"")
        }
    }

    /**
     * 如果想要点击外部消失的话 重写此方法
     *
     * @param savedInstanceState
     * @return
     */
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        //设置点击外部可消失
        dialog.setCanceledOnTouchOutside(true)
        //设置使软键盘弹出的时候dialog不会被顶起
        val win = dialog.window
        val params = win.attributes
        win.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING)
        //这里设置dialog的进出动画
        win.setWindowAnimations(R.style.DialogBottomAnim)
        return dialog
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // 在这里将view的高度设置为精确高度，即可屏蔽向上滑动不占全屏的手势。
        //如果不设置高度的话 会默认向上滑动时dialog覆盖全屏
        val view = inflater.inflate(R.layout.fragment_item_list_dialog, container, false)
        view.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                getScreenHeight(activity) / 2)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        recyclerView.layoutManager = LinearLayoutManager(context)
        commentListAdapter = ShortCommentListAdapter(R.layout.short_recommend_item_layout, arrayListOf())
        recyclerView.adapter = commentListAdapter
        commentTextView.onClick {
            val inputDialog = InputDialog(activity!!,null,this@ListBottomSheetDialogFragment)
            val window = inputDialog.window
            val params = window.attributes
            //设置软键盘通常是可见的
            window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)
            inputDialog.show()
        }
        closeImageView.onClick { dialog?.dismiss() }

        commentListAdapter?.addChildClickViewIds(R.id.isLikeContain,R.id.replyBtn)
        commentListAdapter?.setOnItemChildClickListener { _, view, position ->
            when(view.id){
                R.id.isLikeContain ->{//评论点赞
                    var item = commentListAdapter!!.data[position]
                    if(item.isLike == null){
                        likeComment(item.commentId.toString())
                        item.isLike = "1"
                    }else{
                        cancelLikeComment(item.commentId.toString())
                        item.isLike = null
                    }
                    commentListAdapter?.notifyItemChanged(position)
                }

                R.id.replyBtn ->{
                    var commentBean = commentListAdapter!!.data[position]
                    val inputDialog = InputDialog(activity!!,commentBean,this@ListBottomSheetDialogFragment)
                    val window = inputDialog.window
                    val params = window.attributes
                    //设置软键盘通常是可见的
                    window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)
                    inputDialog.show()
                }
            }

        }
        getCommentList()
    }

    /**
     * 获取评论列表
     */
    private fun getCommentList(){
        var params = HttpParams()
        params.put("pageNum",pageNum)
        params.put("pageSize", Constant.PAGE_SIZE)
        params.put("shortVideoId", shortVideoId )
        OkGo.post<BaseResponse<CommentListResponse>>(Constant.SHORT_VIDEO_COMMENT)
                .params(params)
                .execute(object:JsonCallBack<BaseResponse<CommentListResponse>>(){
                    override fun onSuccess(response: Response<BaseResponse<CommentListResponse>>?) {
                        if(response?.body()?.res == 0){
                            if(pageNum == 1){
                                commentListAdapter?.setList(response.body()?.data?.list)
                            }else{
                                commentListAdapter?.addData(response.body().data.list)
                            }
                            numView.text = "${response.body()?.data?.total}条评论"
                        }else{

                        }
                    }

                })
    }
    /**
     * 发表视频评论
     */
    private fun sendShortVideoComment(content:String){
        var params = HttpParams()
        params.put("content",content)
        params.put("shortVideoId",shortVideoId)
        params.put("type",1)
        OkGo.post<BaseResponse<String>>(Constant.ADD_VIDEO_COMMENT)
                .params(params)
                .execute(object:JsonCallBack<BaseResponse<String>>(){
                    override fun onSuccess(response: Response<BaseResponse<String>>?) {
                        if(response?.body()?.res == 0){
                            toast("评论成功")
                            pageNum = 1
                            getCommentList()
                        }else{

                        }
                    }

                })
    }
    /**
     * 回复视频评论
     */
    private fun replyShortVideoComment(content:String,commentBean: CommentBean?){
        var params = HttpParams()
        params.put("content",content)
        params.put("shortVideoId",shortVideoId)
        params.put("type",2)
        params.put("commentId",commentBean?.commentId.toString())
        params.put("replyId",commentBean?.userId.toString())
        OkGo.post<BaseResponse<String>>(Constant.ADD_VIDEO_COMMENT)
                .params(params)
                .execute(object:JsonCallBack<BaseResponse<String>>(){
                    override fun onSuccess(response: Response<BaseResponse<String>>?) {
                        if(response?.body()?.res == 0){
                            toast("评论成功")
                            pageNum = 1
                            getCommentList()
                        }else{

                        }
                    }

                })
    }

    /**
     * 得到屏幕的高
     *
     * @param context
     * @return
     */
    private fun getScreenHeight(context: Context?): Int {
        val wm = context!!.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        return wm.defaultDisplay.height
    }

    companion object {

        private const val ARG_PARAM1 = "param1"
        private const val ARG_PARAM2 = "param2"

        @JvmStatic
        fun newInstance(param1: String?, param2: String?): ListBottomSheetDialogFragment {
            val fragment = ListBottomSheetDialogFragment()
            val args = Bundle()
            args.putString(ARG_PARAM1, param1)
            args.putString(ARG_PARAM2, param2)
            fragment.arguments = args
            return fragment
        }
    }

    /**
     * 点赞评论
     */
    private fun likeComment(commentId:String){
        var params = HttpParams()
        params.put("commentId",commentId)
        OkGo.post<BaseResponse<String>>(Constant.SHORT_COMMENT_LIKE)
                .params(params)
                .execute(object : JsonCallBack<BaseResponse<String>>(){
                    override fun onSuccess(response: Response<BaseResponse<String>>?) {

                    }

                })
    }

    /**
     * 取消点赞评论
     */
    private fun cancelLikeComment(commentId: String){
        var params = HttpParams()
        params.put("commentId",commentId)
        OkGo.post<BaseResponse<String>>(Constant.SHORT_COMMENT_LIKE_CANCEL)
                .params(params)
                .execute(object : JsonCallBack<BaseResponse<String>>(){
                    override fun onSuccess(response: Response<BaseResponse<String>>?) {

                    }

                })
    }

    override fun onSend(comment: String?,commentBean: CommentBean?) {
        if(commentBean == null){
            sendShortVideoComment(comment!!)
        }else{
            replyShortVideoComment(comment!!,commentBean)
        }

    }


}