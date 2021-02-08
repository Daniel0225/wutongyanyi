package com.yiheoline.qcloud.xiaozhibo.homepage

import android.app.Activity
import android.content.Intent
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.core.widget.addTextChangedListener
import androidx.databinding.adapters.TextViewBindingAdapter
import com.yiheoline.qcloud.xiaozhibo.base.BaseActivity
import com.yiheonline.qcloud.xiaozhibo.R
import kotlinx.android.synthetic.main.activity_custom_tag.*
import kotlinx.android.synthetic.main.toolbar_layout.*
import org.jetbrains.anko.sdk27.coroutines.onClick
import org.jetbrains.anko.textColor
import org.jetbrains.anko.toast

class CustomTagActivity : BaseActivity() {

    override fun getLayoutId(): Int {
        return R.layout.activity_custom_tag
    }

    override fun initView() {
        super.initView()
        backView.onClick { finish() }
        rightBtn.visibility = View.VISIBLE
        rightBtn.text = "保存"
        rightBtn.textColor = resources.getColor(R.color.colorAccent)
        rightBtn.onClick {
            var tagName = inputView.text.toString()
            if(tagName.isNotEmpty()){
                var intent = Intent()
                intent.putExtra("tagName",inputView.text.toString())
                setResult(Activity.RESULT_OK,intent)
                finish()
            }else{
                toast("标签不能为空")
            }
        }

        inputView.addTextChangedListener(object : TextWatcher{
            override fun afterTextChanged(editable: Editable?) {
                var length = editable?.length
                numberView.text = "$length/9"
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

        })
    }
}