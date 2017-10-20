package com.sogukj.pe.ui.approve

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import com.framework.base.ToolbarActivity
import com.google.gson.Gson
import com.sogukj.pe.R
import java.text.SimpleDateFormat

class ApproveActivity : ToolbarActivity() {

    val fmt = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
    val gson = Gson()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_approve)
        setBack(true)
        setTitle("审批")
    }

    companion object {
        fun start(ctx: Activity?) {
            val intent = Intent(ctx, ApproveActivity::class.java)
            ctx?.startActivity(intent)
        }
    }
}
