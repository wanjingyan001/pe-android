package com.sogukj.pe.ui.user

import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.Theme
import com.framework.base.BaseActivity
import com.netease.nimlib.sdk.NIMClient
import com.netease.nimlib.sdk.auth.AuthService
import com.sogukj.pe.App
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.ui.LoginActivity
import com.sogukj.pe.util.Utils
import com.sogukj.util.Store
import com.sogukj.util.XmlDb
import kotlinx.android.synthetic.main.activity_setting.*

class SettingActivity : BaseActivity() {

    companion object {
        fun start(ctx: Context?) {
            ctx?.startActivity(Intent(ctx, SettingActivity::class.java))
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setting)
        Utils.setWindowStatusBarColor(this, R.color.white)
        toolbar_title.text = "设置中心"
        toolbar_back.setOnClickListener {
            onBackPressed()
        }

        feedBack.setOnClickListener {
            FeedBackActivity.start(this)
        }
        loginOut.setOnClickListener {
            MaterialDialog.Builder(this@SettingActivity)
                    .theme(Theme.LIGHT)
                    .title("提示")
                    .content("确定要退出此帐号?")
                    .onPositive { materialDialog, dialogAction ->
                        App.INSTANCE.resetPush(false)
                        IMLogout()
                        Store.store.clearUser(this)
                        LoginActivity.start(this)
                        finish()
                    }
                    .positiveText("确定")
                    .negativeText("取消")
                    .show()
        }

    }

    /**
     * 网易云信IM注销
     */
    private fun IMLogout(){
        val xmlDb = XmlDb.open(this)
        xmlDb.set(Extras.NIMACCOUNT,"")
        xmlDb.set(Extras.NIMTOKEN,"")
        NIMClient.getService(AuthService::class.java).logout()
    }
}
