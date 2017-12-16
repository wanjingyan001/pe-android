package com.sogukj.pe.ui.user

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import com.framework.base.BaseActivity
import com.sogukj.pe.R
import com.sogukj.pe.util.Utils
import com.sogukj.service.SoguApi
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_feed_back.*

class FeedBackActivity : BaseActivity() {

    companion object {
        fun start(ctx: Activity?) {
            ctx?.startActivity(Intent(ctx, FeedBackActivity::class.java))
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_feed_back)
        Utils.setWindowStatusBarColor(this, R.color.white)
        toolbar_title.text = "意见反馈"
        toolbar_back.setOnClickListener {
            onBackPressed()
        }

        submitFeed.setOnClickListener {
                submit()
        }
    }


    fun submit() {
        if (feedEdt.text.toString().isEmpty()) {
            showToast("请填写意见建议")
            return
        }
        if (contactInfoEdt.text.toString().isEmpty()) {
            showToast("请填写联系方式")
            return
        }
        SoguApi.getService(application)
                .addFeedback(feedEdt.text.toString(),
                        contactEdt.text.toString(),
                        contactInfoEdt.text.toString())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ payload ->
                    if (payload.isOk) {
                        showToast("提交成功")
                        finish()
                    } else {
                        showToast(payload.message)
                    }
                })
    }
}
