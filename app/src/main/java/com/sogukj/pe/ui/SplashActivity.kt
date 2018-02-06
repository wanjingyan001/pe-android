package com.sogukj.pe.ui

import android.content.Intent
import android.os.Bundle
import com.framework.base.BaseActivity
import com.sogukj.pe.R
import com.sogukj.pe.ui.main.MainActivity
import com.sogukj.util.Store
import me.leolin.shortcutbadger.ShortcutBadger

/**
 * Created by qinfei on 17/8/11.
 */
class SplashActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        ShortcutBadger.removeCount(this)
    }

    override fun onResume() {
        super.onResume()
        handler.postDelayed({
            if (!Store.store.checkLogin(this)) {
                LoginActivity.start(this)
            } else {
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }
        }, 500)
    }
}
