package com.sogukj.pe.ui

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.MenuItem
import com.bumptech.glide.Glide
import com.framework.base.ToolbarActivity
import com.sogukj.pe.R
import com.sogukj.pe.bean.UserBean
import com.sogukj.util.Store
import kotlinx.android.synthetic.main.activity_user_edit.*

/**
 * Created by qinfei on 17/7/18.
 */

class UserEditActivity : ToolbarActivity() {

    override val menuId: Int
        get() = R.menu.user_edit
    var user: UserBean = UserBean()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_edit)
        setTitle("个人信息")
        Store.store.getUser(this)?.apply {
            user = this
            if (!TextUtils.isEmpty(name))
                tv_name?.setText(name)
            if (!TextUtils.isEmpty(phone))
                tv_phone?.setText(phone)
            if (!TextUtils.isEmpty(email))
                tv_email?.setText(email)
            if (!TextUtils.isEmpty(depart_name))
                tv_job?.setText(depart_name)
            if (!TextUtils.isEmpty(url))
                Glide.with(this@UserEditActivity)
                        .load(url)
                        .into(iv_user)
        }

    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.action_save -> {
                doSave();return true;
            }
        }
        return false
    }

    fun doSave() {

    }

    companion object {
        fun start(ctx: Activity?) {
            ctx?.startActivity(Intent(ctx, UserEditActivity::class.java))
        }
    }
}
