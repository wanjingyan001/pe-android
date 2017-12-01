package com.sogukj.pe.ui.user

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import com.framework.base.BaseActivity
import com.framework.base.ToolbarActivity
import com.sogukj.pe.R
import com.sogukj.pe.bean.UserBean
import com.sogukj.pe.util.Utils
import com.sogukj.util.Store
import kotlinx.android.synthetic.main.activity_user_resume.*
import kotlinx.android.synthetic.main.layout_shareholder_toolbar.*

class UserResumeActivity : BaseActivity() {
    var user: UserBean? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_resume)
        Utils.setWindowStatusBarColor(this, R.color.white)
        toolbar_title.text = "个人简历"
        addTv.text = "保存"


        user = Store.store.getUser(this)
        setData()

        back.setOnClickListener { finish() }
        addTv.setOnClickListener {  }
        tv_add_work_expericence.setOnClickListener {
            WorkExpericenceAddActivity.start(this)
        }
        tv_add_education.setOnClickListener {
            EducationActivity.start(this)
        }
    }

    fun setData(){
        user?.let {
            tv_name.setText(user?.name)
        }

    }

    companion object {
        fun start(ctx: Activity?) {
            val intent = Intent(ctx, UserResumeActivity::class.java)
            ctx?.startActivity(intent)
        }
    }
}
