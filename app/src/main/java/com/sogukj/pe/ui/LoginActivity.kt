package com.sogukj.pe.ui

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.text.TextUtils
import android.util.Log
import android.view.View
import com.framework.base.ActivityHelper
import com.framework.base.BaseActivity
import com.jakewharton.rxbinding2.widget.RxTextView
import com.netease.nim.uikit.api.NimUIKit
import com.netease.nimlib.sdk.RequestCallback
import com.netease.nimlib.sdk.auth.LoginInfo
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.bean.UserBean
import com.sogukj.pe.ui.main.MainActivity
import com.sogukj.pe.util.LoginTimer
import com.sogukj.pe.util.Utils
import com.sogukj.service.SoguApi
import com.sogukj.util.Store
import com.sogukj.util.XmlDb
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.functions.Consumer
import io.reactivex.functions.Function
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_login.*
import org.jetbrains.anko.backgroundResource
import org.jetbrains.anko.imageResource
import org.jetbrains.anko.sdk25.coroutines.textChangedListener
import java.util.*

/**
 * Created by qinfei on 17/7/18.
 */
class LoginActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        when (Utils.getEnvironment()) {
            "civc" -> {
                login_logo.imageResource = R.drawable.img_logo_login_zd
            }
            "ht" -> {
                login_logo.imageResource = R.drawable.img_logo_login_ht
            }
            "kk" ->{
                login_logo.imageResource = R.drawable.img_logo_login_kk
            }
            "yge"->{
                login_logo.imageResource = R.drawable.img_logo_login_yge
            }
            else -> {
                login_logo.imageResource = R.drawable.img_logo_login
            }
        }
        et_name.requestFocus()
        tv_code.setOnClickListener {
            doSendCode();
        }
//        btn_login.setOnClickListener {
//            doLogin();
//        }
        btn_login.isEnabled = false
        btn_login.backgroundResource = R.drawable.bg_btn_login_1
        var observable_List = ArrayList<Observable<CharSequence>>()
        observable_List.add(RxTextView.textChanges(et_name))
        observable_List.add(RxTextView.textChanges(et_pwd))
        Observable.combineLatest(observable_List, object : Function<Array<Any>, Boolean> {
            override fun apply(str: Array<Any>): Boolean {
                return (str[0] as CharSequence).isNullOrEmpty() || (str[1] as CharSequence).isNullOrEmpty()//只要有一个是null或空就反悔true
            }
        }).subscribe(object : Consumer<Boolean> {
            override fun accept(t: Boolean) {
                if (!t) {
                    btn_login.isEnabled = true
                    btn_login.backgroundResource = R.drawable.bg_btn_login
                    btn_login.setOnClickListener {
                        doLogin();
                    }
                } else {
                    btn_login.isEnabled = false
                    btn_login.backgroundResource = R.drawable.bg_btn_login_1
                }
            }
        })
        val extra = intent.getBooleanExtra(Extras.FLAG, false)
        if (extra) {
            showCustomToast(R.drawable.icon_toast_common, "您的帐号已在其他设备登陆，您已被迫下线")
            //showToast("您的帐号已在其他设备登陆，您已被迫下线")
        }
        et_name.textChangedListener {
            onTextChanged { charSequence, i1, i2, i3 ->
                if (!charSequence.isNullOrEmpty()) {
                    delete1.visibility = View.VISIBLE
                    delete1.setOnClickListener {
                        et_name.setText("")
                    }
                }else{
                    delete1.visibility = View.GONE
                }
            }
        }
        et_pwd.textChangedListener {
            onTextChanged { charSequence, i1, i2, i3 ->
                if (!charSequence.isNullOrEmpty()) {
                    delete2.visibility = View.VISIBLE
                    delete2.setOnClickListener {
                        et_pwd.setText("")
                    }
                }else{
                    delete2.visibility = View.GONE
                }
            }
        }
    }

    private fun doSendCode() {
        if (TextUtils.isEmpty(et_name.text) || !Utils.isMobile(et_name.text.trim())) {
            et_name.setText("")
            et_name.requestFocus()
            showCustomToast(R.drawable.icon_toast_common, "请输入正确的手机号")
            //showToast("请输入正确的手机号")
            return
        }
        Timer().scheduleAtFixedRate(LoginTimer(60, Handler(), tv_code), 0, 1000);
        val phone = et_name.text.toString()

        //test
        var user = UserBean()
        user.phone = phone
        Store.store.setUser(context, user)

        SoguApi.getService(application)
                .sendVerifyCode(phone)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ payload ->
                    if (payload.isOk) {
                        showCustomToast(R.drawable.icon_toast_common, "验证码已经发送，请查收")
                        //showToast("验证码已经发送，请查收")
                        et_pwd.setFocusable(true);//设置输入框可聚集
                        et_pwd.setFocusableInTouchMode(true);//设置触摸聚焦
                        et_pwd.requestFocus();//请求焦点
                        et_pwd.findFocus();//获取焦点
                    } else
                        showCustomToast(R.drawable.icon_toast_fail, payload.message)
                }, { e ->
                    showCustomToast(R.drawable.icon_toast_fail, "验证码发送失败")
                    //showToast("验证码发送失败")
                })


    }

    private fun doLogin() {
        val user = UserBean()
        et_name?.text?.toString()?.apply {
            user.phone = this
        }

        if (TextUtils.isEmpty(et_name.text) || !Utils.isMobile(et_name.text.trim())) {
            et_name.setText("")
            et_name.requestFocus()
            showCustomToast(R.drawable.icon_toast_common, "请输入正确的手机号")
            //showToast("请输入正确的手机号")
            return
        }
        if (TextUtils.isEmpty(et_pwd.text) || et_pwd.text.trim().length < 4) {
            et_pwd.setText("")
            et_pwd.requestFocus()
            showCustomToast(R.drawable.icon_toast_common, "请输入正确的验证码")
            //showToast("请输入正确的验证码")
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
                        showCustomToast(R.drawable.icon_toast_success, "登录成功")
                        //showToast("登录成功")
                        payload?.payload?.apply {
                            Store.store.setUser(this@LoginActivity, this)
                            Log.d("WJY", "$accid==>$token")
                            ifNotNull(accid, token, { accid, token ->
                                IMLogin(accid, token)
                            })
                        }
                        startActivity(Intent(context, MainActivity::class.java))
                        finish()
                    } else
                        showCustomToast(R.drawable.icon_toast_fail, payload.message)
                }, { e ->
                    showCustomToast(R.drawable.icon_toast_fail, "登录失败")
                    //showToast("登录失败")
                })
    }

    /**
     * 网易云信IM登录
     */
    private fun IMLogin(account: String, token: String) {
        val loginInfo = LoginInfo(account, token)
        NimUIKit.login(loginInfo, object : RequestCallback<LoginInfo> {
            override fun onSuccess(p0: LoginInfo?) {
                Log.d("WJY", "登录成功:${p0?.account}===>${p0?.token}")
                val xmlDb = XmlDb.open(this@LoginActivity)
                xmlDb.set(Extras.NIMACCOUNT, account)
                xmlDb.set(Extras.NIMTOKEN, token)
            }

            override fun onFailed(p0: Int) {
                if (p0 == 302 || p0 == 404) {
                    showCustomToast(R.drawable.icon_toast_fail, "帐号或密码错误")
                    //showToast("帐号或密码错误")
                } else {
                    showCustomToast(R.drawable.icon_toast_fail, "登录失败")
                    //showToast("登录失败")
                }
            }

            override fun onException(p0: Throwable?) {
                showCustomToast(R.drawable.icon_toast_common, "无效输入")
                //showToast("无效输入")
            }
        })
    }

    private fun <T1, T2> ifNotNull(value1: T1?, value2: T2?, bothNotNull: (T1, T2) -> (Unit)) {
        if (value1 != null && value2 != null) {
            bothNotNull(value1, value2)
        }
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
