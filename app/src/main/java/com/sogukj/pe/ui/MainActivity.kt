package com.sogukj.pe.ui

import android.os.Bundle
import com.framework.base.BaseActivity
import com.sogukj.pe.R
import com.sogukj.pe.util.Trace
import com.sogukj.service.SoguApi
import com.sogukj.util.Store
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
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
        val user = Store.store.getUser(this)
        val uToken = Store.store.getUToken(this)
        if (user?.uid != null && uToken != null) {
            SoguApi.getService(application)
                    .saveUser(uid = user.uid!!, advice_token = Store.store.getUToken(this))
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeOn(Schedulers.io())
                    .subscribe({ payload ->
                        Trace.i("pushToken", "${payload.isOk}")
                    }, { e -> Trace.e(e) })
        }
    }

    var checkId = R.id.rb_news;

    fun doCheck(checkedId: Int) {
        this.checkId = checkedId
        val fragment = when (checkId) {
            R.id.rb_news -> fragmentNews
            else -> fragmentProject
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
