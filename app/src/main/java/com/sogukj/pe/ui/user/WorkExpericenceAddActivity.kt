package com.sogukj.pe.ui.user

import android.app.Activity
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.framework.base.ToolbarActivity
import com.sogukj.pe.R

class WorkExpericenceAddActivity : ToolbarActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_work_expericence_add)
        setBack(true)
        title = "添加工作经历"
    }

    companion object {
        fun start(ctx: Activity?) {
            val intent = Intent(ctx, WorkExpericenceAddActivity::class.java)
            ctx?.startActivity(intent)
        }
    }
}
