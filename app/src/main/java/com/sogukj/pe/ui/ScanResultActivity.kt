package com.sogukj.pe.ui

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import com.framework.base.BaseActivity
import com.sogukj.pe.R
import kotlinx.android.synthetic.main.activity_scan_result.*

class ScanResultActivity : BaseActivity() {

    companion object {
        fun start(ctx: Activity?) {
            val intent = Intent(ctx, ScanResultActivity::class.java)
            ctx?.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scan_result)

        toolbar_back.setOnClickListener {
            finish()
        }

        cancel.setOnClickListener {
            finish()
        }
    }
}
