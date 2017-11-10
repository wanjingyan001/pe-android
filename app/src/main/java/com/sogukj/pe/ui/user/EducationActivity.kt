package com.sogukj.pe.ui.user

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import com.framework.base.ToolbarActivity
import com.sogukj.pe.R

class EducationActivity : ToolbarActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_education)
        setBack(true)
        title = "添加教育经历"
    }

    companion object {
        fun start(ctx: Activity?) {
            val intent = Intent(ctx, EducationActivity::class.java)
            ctx?.startActivity(intent)
        }
    }
}
