package com.sogukj.pe.ui.main

import android.Manifest
import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Rect
import android.graphics.drawable.ColorDrawable
import android.media.ThumbnailUtils
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.content.FileProvider
import android.support.v4.view.ViewCompat
import com.framework.base.BaseActivity
import com.sogukj.pe.App
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.bean.NewsBean
import com.sogukj.pe.ui.LoginActivity
import com.sogukj.pe.ui.fund.FundMainFragment
import com.sogukj.pe.ui.news.NewsDetailActivity
import com.sogukj.pe.ui.project.MainProjectFragment
import com.sogukj.util.Store
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.widget.*
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.Theme
import com.ashokvarma.bottomnavigation.BottomNavigationBar
import com.ashokvarma.bottomnavigation.BottomNavigationItem
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.sogukj.pe.ui.TeamSelectFragment
import com.sogukj.pe.util.*
import com.sogukj.pe.view.MyProgressBar
import com.sogukj.service.SoguApi
import com.sogukj.util.XmlDb
import com.sougukj.initNavTextColor
import com.sougukj.initNavTextColor1
import com.sougukj.setVisible
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_main.*
import org.jetbrains.anko.dip
import org.jetbrains.anko.find
import org.jetbrains.anko.imageResource
import org.jetbrains.anko.info
import java.io.File
import java.util.*


/**
 * Created by qinfei on 17/7/18.
 */
class MainActivity : BaseActivity() {
    val fragments = Stack<Fragment>()
    private val mainMsg: MainMsgFragment by lazy { MainMsgFragment.newInstance() }
    private val teamSelect: TeamSelectFragment by lazy { TeamSelectFragment.newInstance() }
    private val mainHome: MainHomeFragment by lazy { MainHomeFragment.newInstance() }
    private val project: MainProjectFragment by lazy { MainProjectFragment.newInstance() }
    private val mainFund: FundMainFragment by lazy { FundMainFragment.newInstance() }

    lateinit var manager: FragmentManager
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val clazz = intent.getSerializableExtra("uPush.target") as Class<Activity>?
        clazz?.apply {
            val news = intent.getSerializableExtra(Extras.DATA) as NewsBean?
            if (null != news) NewsDetailActivity.start(this@MainActivity, news)
        }
        verifyPermissions(this)
        initFragments()
        initBottomNavBar()
        changeFragment(0)
        updateVersion()
        Glide.with(this)
                .load(Utils.defaultIc())
                .apply(RequestOptions().centerInside())
                .into(mainLogo)
        ViewCompat.setElevation(mainLogo,50f)
    }

    private fun initFragments() {
        manager = supportFragmentManager
        manager.beginTransaction()
                .add(R.id.container, mainMsg)
                .add(R.id.container, teamSelect)
                .add(R.id.container, mainHome)
                .add(R.id.container, project)
                .add(R.id.container, mainFund)
                .commit()

        fragments.add(mainMsg)
        fragments.add(teamSelect)
        fragments.add(mainHome)
        fragments.add(project)
        fragments.add(mainFund)
    }

    private fun initBottomNavBar() {
//        .addItem(BottomNavigationItem(R.drawable.ic_qb_sel12, "首页").setInactiveIconResource(R.drawable.ic_qb_nor2).initNavTextColor1())
        bottomBar.addItem(BottomNavigationItem(R.drawable.ic_qb_sel11, "消息").setInactiveIconResource(R.drawable.ic_qb_nor).initNavTextColor())
                .addItem(BottomNavigationItem(R.drawable.ic_qb_sel15, "通讯录").setInactiveIconResource(R.drawable.ic_qb_nor1).initNavTextColor())
                .addItem(BottomNavigationItem(R.drawable.ic_qb_selnull, "首页").setInactiveIconResource(R.drawable.ic_qb_nor2).initNavTextColor1())
                .addItem(BottomNavigationItem(R.drawable.ic_tab_main_proj_11, "项目").setInactiveIconResource(R.drawable.ic_tab_main_proj_0).initNavTextColor())
                .addItem(BottomNavigationItem(R.drawable.ic_main_fund22, "基金").setInactiveIconResource(R.drawable.ic_main_fund).initNavTextColor())
                .setMode(BottomNavigationBar.MODE_FIXED)
                .setBackgroundStyle(BottomNavigationBar.BACKGROUND_STYLE_STATIC)
                .setBarBackgroundColor(R.color.white)
                .setFirstSelectedPosition(0)
                .initialise()
        bottomBar.setTabSelectedListener(object : BottomNavigationBar.SimpleOnTabSelectedListener() {
            override fun onTabSelected(position: Int) {
                if (position == 2) {
                    mainLogo.setVisible(true)
                    val scalex = PropertyValuesHolder.ofFloat("scaleX", 0.7f,1f)
                    val scaley = PropertyValuesHolder.ofFloat("scaleY", 0.7f,1f)
                    val animator = ObjectAnimator.ofPropertyValuesHolder(mainLogo, scalex, scaley).setDuration(300)
                    animator.interpolator = DecelerateInterpolator()
                    animator.start()
                }else{
                    mainLogo.setVisible(false)
                }
                changeFragment(position)
            }
        })
    }

    /**
     * 切换Tab，切换到对应的Fragment
     */
    private fun changeFragment(position: Int) {
        when (position) {
            0, 3, 4 -> {
                StatusBarUtil.setColor(this, resources.getColor(R.color.colorPrimary), 0)
                StatusBarUtil.setDarkMode(this)
            }
            1 -> {
                StatusBarUtil.setColor(this, resources.getColor(R.color.color_blue_0888ff), 0)
                StatusBarUtil.setDarkMode(this)
            }
            2 -> {
                StatusBarUtil.setColor(this, resources.getColor(R.color.white), 0)
                StatusBarUtil.setLightMode(this)
            }
        }
        val ft = supportFragmentManager.beginTransaction()
        fragments.forEach { ft.hide(it) }
        ft.show(fragments[position])
        ft.commit()
    }


    val PERMISSIONS_STORAGE = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)

    private fun verifyPermissions(activity: Activity) {
        // Check if we have write permission
        val permission = ActivityCompat.checkSelfPermission(activity,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)

        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(activity, PERMISSIONS_STORAGE,
                    1)
        }
    }


    override fun onStart() {
        super.onStart()
        if (!Store.store.checkLogin(this)) {
            LoginActivity.start(this)
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
        //mDialog.show()
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
                                if (XmlDb.open(context).get("is_read", "").equals("no")) {
                                    mDialog.show()
                                    title.text = "新版功能介绍"
                                    icon.imageResource = R.drawable.update_icon1
                                    message.text = info.toString()
                                    update_info.visibility = View.GONE
                                    scrollView.visibility = View.VISIBLE
                                    update.visibility = View.VISIBLE
                                    prompt.visibility = View.GONE
                                    update.text = "我知道了"
                                    update.setOnClickListener {
                                        mDialog.dismiss()
                                    }
                                    XmlDb.open(context).set("is_read", "yes")
                                }
                                //mDialog.dismiss()
                                //enterNext()
                            } else {//1=>更新，2---强制更新
                                mDialog.show()
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
                                    XmlDb.open(context).set("is_read", "no")
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
                            //mDialog.dismiss()
                        }
                    } else {
                        //showToast(payload.message)
                        showCustomToast(R.drawable.icon_toast_fail, payload.message)
                        //enterNext()
                        title.text = "更新失败"
                        //mDialog.dismiss()
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

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        if(manager != null){
            bottomBar.selectTab(0)
        }
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
            uri = FileProvider.getUriForFile(context, FileUtil.getFileProvider(this), File(param))
            //uri = MyFileProvider.getUriForFile(new File(param));
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
        } else {
            uri = Uri.fromFile(File(param))
        }
        return uri
    }
}
