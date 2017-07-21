package com.sogukj.pe.ui

import android.os.Bundle
import com.framework.base.BaseActivity
import com.sogukj.pe.R
import com.sogukj.util.Store
import kotlinx.android.synthetic.main.activity_main.*

/**
 * Created by qinfei on 17/7/18.
 */
class MainActivity : BaseActivity() {

    val fragmentNews = MainNewsFragment.newInstance()
    val fragmentProject = MainProjectFragment.newInstance()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        supportFragmentManager.beginTransaction()
                .replace(R.id.container, fragmentNews)
                .commit()
        rg_tab_main.setOnCheckedChangeListener { group, checkedId ->
            val fragment = when (checkedId) {
                R.id.rb_project -> fragmentProject
                else -> fragmentNews
            }
            supportFragmentManager.beginTransaction()
                    .replace(R.id.container, fragment)
                    .commit()
        }

    }

    override fun onResume() {
        super.onResume()
        if (!Store.store.checkLogin(this)) {
            LoginActivity.start(this)
        }
    }
}
