package com.sogukj.pe.ui.news

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import com.framework.base.ToolbarActivity
import com.sogukj.pe.R

class MainNewsActivity : ToolbarActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_news)
        supportFragmentManager
                .beginTransaction()
                .replace(R.id.fl_container, MainNewsFragment.newInstance())
                .commit()
    }

    companion object {
        fun start(ctx: Activity?) {
            val intent = Intent(ctx, MainNewsActivity::class.java)
            ctx?.startActivity(intent)
        }
    }
}
