package com.sogukj.pe.ui

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import com.framework.base.ToolbarActivity
import com.sogukj.pe.R
import com.sogukj.util.Store
import kotlinx.android.synthetic.main.activity_user.*

/**
 * Created by qinfei on 17/7/18.
 */

class UserActivity : ToolbarActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user)
        setTitle("个人信息")
        setBack(true)
    }

    override fun onStart() {
        super.onStart()
        val user = Store.store.getUserInfo(this)
        user?.apply {
            tv_name?.text = "用户$uid"
            tv_mobile?.text = phone
            tv_mail?.text = email
            tv_job?.text = depart_name
        }
    }

    companion object {
        fun start(ctx: Activity?) {
            ctx?.startActivity(Intent(ctx, UserActivity::class.java))
        }
    }
}
