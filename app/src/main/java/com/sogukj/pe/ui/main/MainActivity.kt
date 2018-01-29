package com.sogukj.pe.ui.main

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.graphics.Rect
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.widget.RadioButton
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
import com.sogukj.pe.util.Utils
import com.sogukj.pe.view.ArrayPagerAdapter


/**
 * Created by qinfei on 17/7/18.
 */
class MainActivity : BaseActivity() {

    val fgProj = MainProjectFragment.newInstance()
    //    val fgMsg = MainMsgFragment.newInstance()
    val fgMsg = MainNewsFragment.newInstance()
    val fgHome = MainHomeFragment.newInstance()
    val fgFund = FundMainFragment.newInstance()
    val fgMine = UserFragment.newInstance()

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
        val fragments = arrayOf(
                fgMsg,
                fgProj,
                fgHome,
                fgFund,
                fgMine
        )
        var adapter = ArrayPagerAdapter(supportFragmentManager, fragments)
        viewpager.adapter = adapter
        viewpager.offscreenPageLimit = fragments.size
        viewpager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener{
            override fun onPageScrollStateChanged(state: Int) {
            }

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
            }

            override fun onPageSelected(position: Int) {
                viewpager.setCurrentItem(position, false)
            }
        })
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
        viewpager.setCurrentItem(currentId, false)
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

    override fun onResume() {
        super.onResume()
        App.INSTANCE.resetPush(true)
    }
}
