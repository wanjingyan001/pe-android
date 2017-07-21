package com.sogukj.pe.ui

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.text.TextUtils
import com.framework.base.ActivityHelper
import com.framework.base.BaseActivity
import com.framework.util.Utils
import com.sogukj.pe.R
import com.sogukj.pe.bean.UserBean
import com.sogukj.pe.util.LoginTimer
import com.sogukj.service.SoguApi
import com.sogukj.util.Store
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
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
        if (TextUtils.isEmpty(et_name.text) || !Utils.isMobile(et_name.text.trim())) {
            et_name.setText("")
            et_name.requestFocus()
            return
        }
        Timer().scheduleAtFixedRate(LoginTimer(60, Handler(), tv_code), 0, 1000);
        val phone = et_name.text.toString()
        SoguApi.getService(application)
                .sendVerifyCode(phone)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ payload ->
                    if (payload.isOk)
                        showToast("验证码已经发送，请查收")
                    else
                        showToast(payload.message)
                }, { e ->
                    showToast("验证码发送失败")
                })


    }

    private fun doLogin() {
        val user = UserBean()
        user.phone = et_name?.text?.toString()

        if (TextUtils.isEmpty(et_name.text) || !Utils.isMobile(et_name.text.trim())) {
            et_name.setText("")
            et_name.requestFocus()
            return
        }
        if (TextUtils.isEmpty(et_pwd.text) || et_pwd.text.trim().length < 4) {
            et_pwd.setText("")
            et_pwd.requestFocus()
            return
        }
        val phone = et_name.text.toString()
        val code = et_pwd.text.toString()
        SoguApi.getService(application)
                .login(phone, code)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ payload ->
                    if (payload.isOk) {
                        showToast("登录成功")
                        payload?.payload?.apply {
                            Store.store.setUser(this@LoginActivity, this)
                        }
                        finish()
                    } else
                        showToast(payload.message)
                }, { e ->
                    showToast("登录失败")
                    user.uid = "$phone"
                    Store.store.setUser(this@LoginActivity, user)
                })
    }

    override fun onBackPressed() {
        ActivityHelper.exit()
        super.onBackPressed()
    }

    companion object {
        fun start(ctx: Activity?) {
            ctx?.startActivity(Intent(ctx, LoginActivity::class.java))
        }
    }
}
