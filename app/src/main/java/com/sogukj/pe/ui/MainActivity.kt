package com.sogukj.pe.ui

import android.app.Activity
import android.os.Bundle
import com.framework.base.BaseActivity
import com.sogukj.pe.App
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.bean.NewsBean
import com.sogukj.util.Store
import kotlinx.android.synthetic.main.activity_main.*

/**
 * Created by qinfei on 17/7/18.
 */
class MainActivity : BaseActivity() {

    val fgProj = MainProjectFragment.newInstance()
    val fgMsg = MainMsgFragment.newInstance()
    val fgHome = MainHomeFragment.newInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val clazz = intent.getSerializableExtra("uPush.target") as Class<Activity>?
        clazz?.apply {
            val news = intent.getSerializableExtra(Extras.DATA) as NewsBean?
            if (null != news) NewsDetailActivity.start(this@MainActivity, news)
        }

        App.INSTANCE.resetPush(true)
    }

    var checkId = R.id.rb_home

    fun doCheck(checkedId: Int) {
        this.checkId = checkedId
        val fragment = when (checkId) {
            R.id.rb_msg -> fgMsg
            R.id.rb_project -> fgProj
            else -> fgHome
        }
        supportFragmentManager.beginTransaction()
                .replace(R.id.container, fragment)
                .commit()
    }

    override fun onStart() {
        super.onStart()
        if (!Store.store.checkLogin(this)) {
            LoginActivity.start(this)
        } else {
            doCheck(checkId)
            rg_tab_main.setOnCheckedChangeListener { group, checkedId ->
                doCheck(checkedId)
            }
        }
    }
}
