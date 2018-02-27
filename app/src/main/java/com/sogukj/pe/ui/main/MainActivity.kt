package com.sogukj.pe.ui.main

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.Rect
import android.graphics.drawable.ColorDrawable
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
import android.view.View
import android.widget.*
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.Theme
import com.sogukj.pe.util.DownloadUtil
import com.sogukj.pe.util.Trace
import com.sogukj.pe.util.Utils
import com.sogukj.pe.view.ArrayPagerAdapter
import com.sogukj.service.SoguApi
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
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
        val bar = mDialog.find<ProgressBar>(R.id.update_progress)
        val title = mDialog.find<TextView>(R.id.update_title)
        val update_info = mDialog.find<TextView>(R.id.update_info)
        val scrollView = mDialog.find<ScrollView>(R.id.update_message_layout)
        val message = mDialog.find<TextView>(R.id.update_message)
        val update = mDialog.find<Button>(R.id.update)
        val cancelBtn = mDialog.find<Button>(R.id.cancel)
        val prompt = mDialog.find<TextView>(R.id.update_prompt)
        mDialog.show()
        title.text = "正在检测更新"
        update_info.visibility = View.INVISIBLE
        scrollView.visibility = View.INVISIBLE
        update.visibility = View.INVISIBLE
        cancelBtn.visibility = View.INVISIBLE
        prompt.visibility = View.INVISIBLE

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
                                mDialog.dismiss()
                                //enterNext()
                            } else if (force == 1) {
                                update_info.visibility = View.VISIBLE
                                scrollView.visibility = View.VISIBLE
                                update.visibility = View.VISIBLE
                                cancelBtn.visibility = View.VISIBLE//TODO
                                title.text = "当前版本过低(" + Utils.getVersionName(context) + ")"
                                update_info.text = "请立即升级到最新版(" + newVersion + ")"
                                icon.imageResource = R.drawable.update_icon1
                                scrollView.visibility = View.VISIBLE
                                message.text = info.toString()
                                update.setOnClickListener {
                                    update.visibility = View.GONE
                                    cancelBtn.visibility = View.GONE
                                    update(url!!, bar, title, mDialog)
                                }
                                cancelBtn.setOnClickListener {
                                    mDialog.dismiss()
                                }
                            } else if (force == 2) {
                                update_info.visibility = View.VISIBLE
                                scrollView.visibility = View.VISIBLE
                                update.visibility = View.VISIBLE
                                cancelBtn.visibility = View.GONE//TODO
                                title.text = "当前版本过低(" + Utils.getVersionName(context) + ")"
                                update_info.text = "请立即升级到最新版(" + newVersion + ")"
                                icon.imageResource = R.drawable.update_icon1
                                scrollView.visibility = View.VISIBLE
                                message.text = info.toString()
                                update.setOnClickListener {
                                    update.visibility = View.GONE
                                    update(url!!, bar, title, mDialog)
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

    override fun onResume() {
        super.onResume()
        App.INSTANCE.resetPush(true)
    }

    fun update(url: String, bar: ProgressBar, title: TextView, dialog: MaterialDialog) {
        bar.visibility = View.VISIBLE
        title.text = "开始下载"
        val fileName = url.substring(url.lastIndexOf("/") + 1)
        DownloadUtil.getInstance().download(url, externalCacheDir.toString(), fileName, object : DownloadUtil.OnDownloadListener {
            override fun onDownloadSuccess(path: String?) {
                title.text = "下载完成"
                dialog.dismiss()
                var intent = Intent()
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                intent.action = Intent.ACTION_VIEW
                //val uri = Uri.fromFile(File(path))
                val uri = transform(path!!, intent)
                intent.setDataAndType(uri, "application/vnd.android.package-archive")
                startActivity(intent)
            }

            override fun onDownloading(progress: Int) {
                title.text = "已下载" + progress + "%"
                bar.progress = progress
            }

            override fun onDownloadFailed() {
                //showToast("下载失败")
                title.text = "下载失败"
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
