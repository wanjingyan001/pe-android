package com.sogukj.pe.ui.news

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import com.framework.base.ToolbarActivity
import com.sogukj.pe.R
import com.sogukj.pe.ui.IM.TeamSelectActivity
import com.sogukj.pe.ui.main.MainMsgFragment

class NewsListActivity : ToolbarActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_news_list)
        supportFragmentManager
                .beginTransaction()
                .replace(R.id.fl_container, MainMsgFragment.newInstance())
                .commit()
    }


    override val menuId: Int
        get() = R.menu.menu_to_address

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.to_address -> {
                TeamSelectActivity.Companion.startForResult(this)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    companion object {
        fun start(ctx: Activity?) {
            val intent = Intent(ctx, NewsListActivity::class.java)
            ctx?.startActivity(intent)
        }
    }
}
