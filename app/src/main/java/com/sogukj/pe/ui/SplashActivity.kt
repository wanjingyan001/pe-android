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
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Build
import android.support.v4.content.FileProvider
import android.view.View
import android.widget.*
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.Theme
import com.sogukj.pe.util.DownloadUtil
import com.sogukj.pe.util.OpenFileUtil
import kotlinx.android.synthetic.main.activity_splash.*
import java.util.*
import me.leolin.shortcutbadger.ShortcutBadger
import org.jetbrains.anko.find
import org.jetbrains.anko.imageResource
import java.io.File

/**
 * Created by qinfei on 17/8/11.
 */
class SplashActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        ShortcutBadger.removeCount(this)
//        val dialog = MaterialDialog.Builder(this)
//                .customView(R.layout.dialog_updated, false)
//                .theme(Theme.LIGHT)
//                .cancelable(false)
//                .build()
//        dialog.window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
//        val icon = dialog.find<ImageView>(R.id.updated_icon)
//        val bar = dialog.find<ProgressBar>(R.id.update_progress)
//        val title = dialog.find<TextView>(R.id.update_title)
//        val info = dialog.find<TextView>(R.id.update_info)
//        val scrollView = dialog.find<ScrollView>(R.id.update_message_layout)
//        val message = dialog.find<TextView>(R.id.update_message)
//        val update = dialog.find<Button>(R.id.update)
//        val prompt = dialog.find<TextView>(R.id.update_prompt)
//        update.setOnClickListener {
//            icon.imageResource = R.drawable.update_icon1
//            bar.visibility = View.VISIBLE
//            title.text = "新功能介绍"
//            info.visibility = View.GONE
//            scrollView.visibility = View.VISIBLE
//            update.visibility = View.GONE
//            prompt.visibility = View.GONE
//        }
//        dialog.show()
    }

    override fun onResume() {
        super.onResume()
//        tv_progress.text = "正在检测更新"
//        SoguApi.getService(application)
//                .getVersion()
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribeOn(Schedulers.io())
//                .subscribe({ payload ->
//                    if (payload.isOk) {
//                        var newVersion = payload.payload?.version
//                        var info = payload.payload?.info
//                        var url = payload.payload?.app_url
//                        var force = payload.payload?.force//1=>更新，0=>不更新，2---强制更新
//
//                        if (!url.isNullOrEmpty()) {
//                            if (force == 0) {
//                                enterNext()
//                            } else if (force == 1) {
//                                MaterialDialog.Builder(context)
//                                        .theme(Theme.LIGHT)
//                                        .title("发现新的版本，是否需要更新")
//                                        .content(info.toString())
//                                        .positiveText("确认")
//                                        .negativeText("取消")
//                                        .onPositive { dialog, which ->
//                                            update(url!!)
//                                        }
//                                        .onNegative { dialog, which ->
//                                            enterNext()
//                                        }
//                                        .show()
//                            } else if (force == 2) {
//                                MaterialDialog.Builder(context)
//                                        .theme(Theme.LIGHT)
//                                        .title("发现新的版本，是否需要更新")
//                                        .content(info.toString())
//                                        .positiveText("确认")
//                                        .onPositive { dialog, which ->
//                                            update(url!!)
//                                        }
//                                        .show()
//                            }
//                        } else {
//                            enterNext()
//                        }
//                    } else {
//                        showToast(payload.message)
//                        enterNext()
//                    }
//                }, { e ->
//                    Trace.e(e)
//                    //showToast("更新失败，进入首页")
//                    enterNext()
//                })
        handler.postDelayed({
            if (!Store.store.checkLogin(this)) {
                LoginActivity.start(this)
            } else {
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }
        }, 500)
    }

    fun update(url: String) {
        pb_progress.visibility = View.VISIBLE
        tv_progress.text = "开始下载"
        val fileName = url.substring(url.lastIndexOf("/") + 1)
        DownloadUtil.getInstance().download(url, externalCacheDir.toString(), fileName, object : DownloadUtil.OnDownloadListener {
            override fun onDownloadSuccess(path: String?) {
                tv_progress.text = "下载完成"
                var intent = Intent()
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                intent.action = Intent.ACTION_VIEW
                //val uri = Uri.fromFile(File(path))
                val uri = transform(path!!, intent)
                intent.setDataAndType(uri, "application/vnd.android.package-archive")
                startActivity(intent)
            }

            override fun onDownloading(progress: Int) {
                tv_progress.text = "已下载" + progress + "%"
                pb_progress.progress = progress
            }

            override fun onDownloadFailed() {
                //showToast("下载失败")
                tv_progress.text = "下载失败"
            }
        })
    }

    private fun transform(param: String, intent: Intent): Uri {
        val uri: Uri
        // 判断版本大于等于7.0
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            uri = FileProvider.getUriForFile(context, "com.sogukj.pe.fileProvider", File(param))
            //uri = MyFileProvider.getUriForFile(new File(param));
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
        } else {
            uri = Uri.fromFile(File(param))
        }
        return uri
    }

    fun enterNext() {
        tv_progress.text = "正在进入首页"
        handler.postDelayed({
            if (!Store.store.checkLogin(this)) {
                LoginActivity.start(this)
                finish()
            } else {
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }
        }, 500)
    }
}
