package com.sogukj.pe.ui

import android.content.Intent
import android.os.Bundle
import com.framework.base.BaseActivity
import com.sogukj.pe.R
import com.sogukj.pe.ui.main.MainActivity
import com.sogukj.pe.util.Trace
import com.sogukj.service.SoguApi
import com.sogukj.util.Store
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import android.content.pm.PackageInfo
import android.content.pm.PackageManager.NameNotFoundException
import android.view.View
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.Theme
import com.sogukj.pe.util.DownloadUtil
import com.sogukj.pe.util.OpenFileUtil
import java.util.*
import me.leolin.shortcutbadger.ShortcutBadger

/**
 * Created by qinfei on 17/8/11.
 */
class SplashActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        ShortcutBadger.removeCount(this)
    }

    override fun onResume() {
        super.onResume()
        SoguApi.getService(application)
                .getVersion()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ payload ->
                    if (payload.isOk) {
                        var newVersion = payload.payload?.version
                        var info = payload.payload?.info
                        var url = payload.payload?.app_url
                        var force = payload.payload?.force//1=>是，0=>否

                        if (!url.isNullOrEmpty()) {
                            //要更新
                            if (force == 1) {
                                update(url!!)
                            } else {
                                MaterialDialog.Builder(context)
                                        .theme(Theme.LIGHT)
                                        .title("发现新的版本，是否需要更新")
                                        .content(info.toString())
                                        .positiveText("确认")
                                        .negativeText("取消")
                                        .onPositive { dialog, which ->
                                            update(url!!)
                                        }
                                        .onNegative { dialog, which ->
                                            enterNext()
                                        }
                                        .show()
                            }
                        } else {
                            enterNext()
                        }
                    } else {
                        showToast(payload.message)
                        enterNext()
                    }
                }, { e ->
                    Trace.e(e)
                    //showToast("更新失败，进入首页")
                    enterNext()
                })
//        handler.postDelayed({
//            if (!Store.store.checkLogin(this)) {
//                LoginActivity.start(this)
//            } else {
//                startActivity(Intent(this, MainActivity::class.java))
//                finish()
//            }
//        }, 500)
    }

    fun update(url: String) {
        val fileName = url.substring(url.lastIndexOf("/") + 1)
        DownloadUtil.getInstance().download(url, externalCacheDir.toString(), fileName, object : DownloadUtil.OnDownloadListener {
            override fun onDownloadSuccess(path: String?) {
                var intent = OpenFileUtil.openFile(context, path)
                if (intent == null) {
                    showToast("apk出错")
                } else {
                    startActivity(intent)
                }
            }

            override fun onDownloading(progress: Int) {
            }

            override fun onDownloadFailed() {
                showToast("下载失败")
            }
        })
    }

    fun enterNext() {
        handler.postDelayed({
            if (!Store.store.checkLogin(this)) {
                LoginActivity.start(this)
            } else {
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }
        }, 500)
    }
}
