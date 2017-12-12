package com.sogukj.pe.ui.user

import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.framework.base.BaseActivity
import com.sogukj.pe.R
import com.sogukj.pe.util.Utils
import kotlinx.android.synthetic.main.activity_setting.*

class SettingActivity : BaseActivity() {

    companion object {
        fun start(ctx: Context?) {
            ctx?.startActivity(Intent(ctx, SettingActivity::class.java))
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setting)
        Utils.setWindowStatusBarColor(this, R.color.white)

        toolbar_back.setOnClickListener {
            onBackPressed()
        }
    }
}
