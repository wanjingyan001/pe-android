package com.sogukj.pe.ui.user

import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.framework.base.ToolbarActivity
import com.sogukj.pe.R

class UserActivity : ToolbarActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user2)
        supportFragmentManager
                .beginTransaction()
                .replace(R.id.fl_container, UserFragment.newInstance())
                .commit()
    }

    companion object {
        fun start(ctx: Context) {
            val intent = Intent(ctx, UserActivity::class.java)
            ctx?.startActivity(intent)
        }
    }
}
