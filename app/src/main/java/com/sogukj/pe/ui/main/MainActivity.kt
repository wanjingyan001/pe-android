package com.sogukj.pe.ui.main

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.Rect
import android.graphics.drawable.ColorDrawable
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.app.FragmentManager
import android.support.v4.content.FileProvider
import com.framework.base.BaseActivity
import com.sogukj.pe.App
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.bean.NewsBean
import com.sogukj.pe.ui.LoginActivity
import com.sogukj.pe.ui.fund.FundMainFragment
import com.sogukj.pe.ui.news.MainNewsFragment
import com.sogukj.pe.ui.news.NewsDetailActivity
import com.sogukj.pe.ui.project.MainProjectFragment
import com.sogukj.pe.ui.user.UserFragment
import com.sogukj.util.Store
import kotlinx.android.synthetic.main.activity_main.*
import android.support.v4.view.ViewCompat.getMinimumHeight
import android.support.v4.view.ViewCompat.getMinimumWidth
import android.support.v4.view.ViewPager
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.view.View
import android.widget.*
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.Theme
import com.sogukj.pe.util.DownloadUtil
import com.sogukj.pe.util.Trace
import com.sogukj.pe.util.Utils
import com.sogukj.pe.view.ArrayPagerAdapter
import com.sogukj.pe.view.MyProgressBar
import com.sogukj.service.SoguApi
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.item_comment_list.*
import org.jetbrains.anko.find
import org.jetbrains.anko.imageResource
import java.io.File


/**
 * Created by qinfei on 17/7/18.
 */
class MainActivity : BaseActivity() {

//    val fgProj = MainProjectFragment.newInstance()
//    //    val fgMsg = MainMsgFragment.newInstance()
//    val fgMsg = MainNewsFragment.newInstance()
//    val fgHome = MainHomeFragment.newInstance()
//    val fgFund = FundMainFragment.newInstance()
//    val fgMine = UserFragment.newInstance()

    val fragments = arrayOf(
            MainNewsFragment.newInstance(),
            //    val fgMsg = MainMsgFragment.newInstance()
            MainProjectFragment.newInstance(),
            MainHomeFragment.newInstance(),
            FundMainFragment.newInstance(),
            UserFragment.newInstance()
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val clazz = intent.getSerializableExtra("uPush.target") as Class<Activity>?
        clazz?.apply {
            val news = intent.getSerializableExtra(Extras.DATA) as NewsBean?
            if (null != news) NewsDetailActivity.start(this@MainActivity, news)
        }

        verifyPermissions(this)
        val dm = resources.displayMetrics
        val dp = dm.density
        val w = dm.widthPixels
        val h = dm.heightPixels

        //
//        val fragments = arrayOf(
//                fgMsg,
//                fgProj,
//                fgHome,
//                fgFund,
//                fgMine
//        )
//        var adapter = ArrayPagerAdapter(supportFragmentManager, fragments)
//        viewpager.adapter = adapter
//        viewpager.offscreenPageLimit = fragments.size
//        viewpager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
//            override fun onPageScrollStateChanged(state: Int) {
//            }
//
//            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
//            }
//
//            override fun onPageSelected(position: Int) {
//                viewpager.setCurrentItem(position, false)
//                var checkId = 0
//                when (position) {
//                    0 -> checkId = R.id.rb_msg
//                    1 -> checkId = R.id.rb_project
//                    2 -> checkId = R.id.rb_home
//                    3 -> checkId = R.id.rb_fund
//                    4 -> checkId = R.id.rb_my
//                }
//                rg_tab_main.check(checkId)
//            }
//        })

        manager = supportFragmentManager
        manager.beginTransaction().add(R.id.container, fragments[2]).commit()

        updateVersion()
    }

    lateinit var manager: FragmentManager

    fun switchContent(from: Int, to: Int) {
        if (!fragments[to].isAdded) { // 先判断是否被add过
            manager.beginTransaction().hide(fragments[from])
                    .add(R.id.container, fragments[to]).commit() // 隐藏当前的fragment，add下一个到Activity中
        } else {
            manager.beginTransaction().hide(fragments[from]).show(fragments[to]).commit() // 隐藏当前的fragment，显示下一个
        }
    }

    val PERMISSIONS_STORAGE = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)

    fun verifyPermissions(activity: Activity) {
        // Check if we have write permission
        val permission = ActivityCompat.checkSelfPermission(activity,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)

        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(activity, PERMISSIONS_STORAGE,
                    1)

        }

    }

    var checkId = R.id.rb_home
    var current = 2

    fun doCheck(checkedId: Int) {
        this.checkId = checkedId
//        rg_tab_main.check(checkId)
//        val fragment = when (checkId) {
//            R.id.rb_msg -> fgMsg
//            R.id.rb_project -> fgProj
//            R.id.rb_fund -> fgFund
//            R.id.rb_my -> fgMine
//            else -> fgHome
//        }
//        supportFragmentManager.beginTransaction()
//                .replace(R.id.container, fragment)
//                .commit()
        var currentId = 2
        when (checkId) {
            R.id.rb_msg -> currentId = 0
            R.id.rb_project -> currentId = 1
            R.id.rb_home -> currentId = 2
            R.id.rb_fund -> currentId = 3
            R.id.rb_my -> currentId = 4
        }
        //viewpager.setCurrentItem(currentId, false)
        if (currentId == current) {
            return
        }
        switchContent(current, currentId)
        current = currentId
    }

    override fun onStart() {
        super.onStart()
        if (!Store.store.checkLogin(this)) {
            LoginActivity.start(this)
        } else {
            doCheck(checkId)

            var rb_home = rg_tab_main.getChildAt(2) as RadioButton
            var draws = rb_home.compoundDrawables
            // top = draws[1]
            //获取drawables
            var r = Rect(0, 0, Utils.dpToPx(context, 57), Utils.dpToPx(context, 57))
            //定义一个Rect边界
            draws[1].setBounds(r)
            //给drawable设置边界
            rb_home.setCompoundDrawables(null, draws[1], null, null)

            rg_tab_main.setOnCheckedChangeListener { group, checkedId ->
                doCheck(checkedId)
            }
        }
    }

    private fun updateVersion() {
        val mDialog = MaterialDialog.Builder(this)
                .customView(R.layout.dialog_updated, false)
                .theme(Theme.LIGHT)
                .cancelable(false)
                .build()
        mDialog.window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        val icon = mDialog.find<ImageView>(R.id.updated_icon)
        val bar = mDialog.find<MyProgressBar>(R.id.update_progress)
        val title = mDialog.find<TextView>(R.id.update_title)
        val update_info = mDialog.find<TextView>(R.id.update_info)
        val scrollView = mDialog.find<ScrollView>(R.id.update_message_layout)
        val message = mDialog.find<TextView>(R.id.update_message)
        val update = mDialog.find<Button>(R.id.update)
        val prompt = mDialog.find<TextView>(R.id.update_prompt)
        mDialog.show()
        title.text = "正在检测更新"
        update_info.visibility = View.GONE
        scrollView.visibility = View.GONE
        update.visibility = View.GONE
        prompt.visibility = View.VISIBLE
        if (isWifi()) {
            prompt.text = "当前处于Wi-Fi网络，请放心下载"
        } else {
            var hint = SpannableString("当前处于移动网络，下载将消耗流量")
            hint.setSpan(ForegroundColorSpan(Color.parseColor("#FF6174")), 9, hint.length, Spanned.SPAN_INCLUSIVE_EXCLUSIVE)
            prompt.text = hint
        }

        SoguApi.getService(application)
                .getVersion()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ payload ->
                    if (payload.isOk) {
                        var newVersion = payload.payload?.version
                        var info = payload.payload?.info
                        var url = payload.payload?.app_url
                        var force = payload.payload?.force//1=>更新，0=>不更新，2---强制更新

                        if (!url.isNullOrEmpty()) {
                            if (force == 0) {
                                title.text = "不用更新"
                                Thread.sleep(500)
                                mDialog.dismiss()
                                //enterNext()
                            } else {//1=>更新，2---强制更新
                                if (force == 1) {
                                    mDialog.setCanceledOnTouchOutside(true)
                                } else {
                                    mDialog.setCanceledOnTouchOutside(false)
                                }
                                update_info.visibility = View.VISIBLE
                                update.visibility = View.VISIBLE
                                title.text = "当前版本过低(" + Utils.getVersionName(context) + ")"
                                update_info.text = "请立即升级到最新版(" + newVersion + ")"
                                scrollView.visibility = View.GONE
                                message.text = info.toString()
                                update.setOnClickListener {
                                    icon.imageResource = R.drawable.update_icon1
                                    update.visibility = View.GONE
                                    title.text = "新版功能介绍"
                                    update_info.visibility = View.GONE
                                    scrollView.visibility = View.VISIBLE
                                    prompt.visibility = View.GONE
                                    update(url!!, bar, update, mDialog, prompt, force!!)
                                }
                            }
                        } else {
                            //enterNext()
                            title.text = "更新失败"
                            mDialog.dismiss()
                        }
                    } else {
                        showToast(payload.message)
                        //enterNext()
                        title.text = "更新失败"
                        mDialog.dismiss()
                    }
                }, { e ->
                    Trace.e(e)
                    title.text = "更新失败"
                    //showToast("更新失败，进入首页")
                    //enterNext()
                    mDialog.dismiss()
                })
    }

    private fun isWifi(): Boolean {
        var connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        var activeNetInfo = connectivityManager.getActiveNetworkInfo()
        if (activeNetInfo != null && activeNetInfo.getType() == ConnectivityManager.TYPE_WIFI) {
            return true
        }
        return false
    }

    override fun onResume() {
        super.onResume()
        App.INSTANCE.resetPush(true)
    }

    fun update(url: String, bar: MyProgressBar, update: Button, dialog: MaterialDialog, prompt: TextView, force: Int) {
        bar.visibility = View.VISIBLE
        //title.text = "开始下载"
        val fileName = url.substring(url.lastIndexOf("/") + 1)
        DownloadUtil.getInstance().download(url, externalCacheDir.toString(), fileName, object : DownloadUtil.OnDownloadListener {
            override fun onDownloadSuccess(path: String?) {
                //title.text = "下载完成"
                //dialog.dismiss()
                if (force == 1) {
                    dialog.setCanceledOnTouchOutside(true)
                } else {
                    dialog.setCanceledOnTouchOutside(false)
                }
                bar.visibility = View.GONE
                update.visibility = View.VISIBLE
                prompt.visibility = View.VISIBLE
                update.text = "立刻安装"
                if (isWifi()) {
                    prompt.text = "已在Wi-Fi网络下完成下载"
                } else {
                    prompt.text = "已在移动网络下完成下载"
                }
                update.setOnClickListener {
                    dialog.dismiss()
                    var intent = Intent()
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    intent.action = Intent.ACTION_VIEW
                    //val uri = Uri.fromFile(File(path))
                    val uri = transform(path!!, intent)
                    intent.setDataAndType(uri, "application/vnd.android.package-archive")
                    startActivity(intent)
                }
            }

            override fun onDownloading(progress: Int) {
                //title.text = "已下载" + progress + "%"
                //bar.progress = progress
                bar.setProgress(progress)
                dialog.setCanceledOnTouchOutside(false)
                dialog.setCancelable(false)
            }

            override fun onDownloadFailed() {
                //showToast("下载失败")
                //title.text = "下载失败"
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
}
