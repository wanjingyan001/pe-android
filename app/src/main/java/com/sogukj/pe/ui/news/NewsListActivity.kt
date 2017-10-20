package com.sogukj.pe.ui.news

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import com.framework.base.ToolbarActivity
import com.sogukj.pe.R

class NewsListActivity : ToolbarActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_news_list)
        supportFragmentManager
                .beginTransaction()
                .replace(R.id.fl_container, MainNewsFragment.newInstance())
                .commit()
    }

    companion object {
        fun start(ctx: Activity?) {
            val intent = Intent(ctx, NewsListActivity::class.java)
            ctx?.startActivity(intent)
        }
    }
}
