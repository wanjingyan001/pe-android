package com.sogukj.pe.ui.user

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import com.framework.base.ToolbarActivity
import com.sogukj.pe.R
import kotlinx.android.synthetic.main.activity_user_resume.*

class UserResumeActivity : ToolbarActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_resume)
        setBack(true)
        title = "个人简历"

        tv_add_work_expericence.setOnClickListener {
            WorkExpericenceAddActivity.start(this)
        }
        tv_add_education.setOnClickListener {
            EducationActivity.start(this)
        }
    }

    companion object {
        fun start(ctx: Activity?) {
            val intent = Intent(ctx, UserResumeActivity::class.java)
            ctx?.startActivity(intent)
        }
    }
}
