package com.sogukj.pe.ui

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.text.TextUtils
import com.framework.base.BaseActivity
import com.framework.util.ViewUtil
import com.sogukj.pe.R
import com.sogukj.pe.util.LoginTimer
import kotlinx.android.synthetic.main.activity_login.*
import java.util.*
/**
 * Created by qinfei on 17/7/18.
 */
class LoginActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        et_name.requestFocus()
        tv_code.setOnClickListener {
            doSendCode();
        }
        btn_login.setOnClickListener {
            doLogin();
        }
    }

    private fun doSendCode() {
        if (TextUtils.isEmpty(et_name.text) && et_name.text.length < 11) {
            et_name.requestFocus()
            ViewUtil.showKeyboard(et_name)
            return
        }
        Timer().scheduleAtFixedRate(LoginTimer(60, Handler(), tv_code), 0, 1000);

    }

    private fun doLogin() {
        if (TextUtils.isEmpty(et_name.text) && et_name.text.length < 11) {
            et_name.requestFocus()
            ViewUtil.showKeyboard(et_name)
            return
        }
        if (TextUtils.isEmpty(et_pwd.text)) {
            et_pwd.requestFocus()
            ViewUtil.showKeyboard(et_pwd)
            return
        }
    }

    companion object {
        fun start(ctx: Activity?) {
            ctx?.startActivity(Intent(ctx, LoginActivity::class.java))
        }
    }
}
