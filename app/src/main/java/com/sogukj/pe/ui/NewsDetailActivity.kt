package com.sogukj.pe.ui

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import com.framework.base.ToolbarActivity
import com.sogukj.pe.R

class NewsDetailActivity : ToolbarActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_news_detail)
        setTitle("资讯详情")
        setBack(true)
    }
    companion object {
        fun start(ctx: Activity?) {
            ctx?.startActivity(Intent(ctx, NewsDetailActivity::class.java))
        }
    }
}
