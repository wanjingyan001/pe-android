package com.sogukj.pe.ui.user

import android.annotation.TargetApi
import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.view.MenuItem
import com.bumptech.glide.Glide
import com.framework.base.ToolbarActivity
import com.sogukj.pe.R
import com.sogukj.pe.bean.DepartmentBean
import com.sogukj.pe.bean.UserBean
import com.sogukj.pe.util.Trace
import com.sogukj.service.SoguApi
import com.sogukj.util.Store
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_user.*

@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
/**
 * Created by qinfei on 17/7/18.
 */

class UserActivity : ToolbarActivity() {
    override val menuId: Int
        get() = R.menu.menu_logout

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.action_logout -> {
                val user = Store.store.getUser(this@UserActivity)
                user?.let {
                    CardActivity.start(this, it)
                }
            }
        }
        return false
    }

    val departList = ArrayList<DepartmentBean>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user)
        title = "个人中心"
        setBack(true)
        SoguApi.getService(application)
                .userDepart()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ payload ->
                    if (payload.isOk) {
                        departList.clear()
                        payload.payload?.forEach {
                            departList.add(it)
                        }
                    } else
                        showToast(payload.message)
                }, { e ->
                    Trace.e(e)
                    showToast("数据获取失败")
                })

        val user = Store.store.getUser(this)
        updateUser(user)
        if (null != user?.uid) {
            SoguApi.getService(application)
                    .userInfo(user.uid!!)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeOn(Schedulers.io())
                    .subscribe({ payload ->
                        if (payload.isOk) {
                            val user = payload.payload
                            user?.apply { Store.store.setUser(this@UserActivity, this) }
                            updateUser(user)
                        } else showToast(payload.message)
                    }, { e ->
                        Trace.e(e)
                        showToast("提交失败")
                    })
        }


        ll_user.setOnClickListener {
            UserEditActivity.start(this@UserActivity, departList)
        }
        structure.setOnClickListener {
            OrganizationActivity.start(this, departList)
        }
        setting.setOnClickListener {
            SettingActivity.start(this)
        }

    }


    fun getBelongBean(userId: Int) {
        SoguApi.getService(application)
                .getBelongProject(userId)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ payload ->
                    if (payload.isOk) {
                        payload.payload?.let {
                            tv_1.text = it.dy.toString()
                            tv_2.text = it.cb.toString()
                            tv_3.text = it.lx.toString()
                            tv_4.text = it.yt.toString()
                            tv_5.text = it.tc.toString()
                            tv_6.text = it.gz.toString()
                        }
                    } else {
                        showToast(payload.message)
                    }
                })
    }


    override fun onResume() {
        super.onResume()
        val user = Store.store.getUser(this)
        updateUser(user)
        user?.uid?.let { getBelongBean(it) }
    }

    private fun updateUser(user: UserBean?) {
        if (null == user) return
        tv_name?.text = user.name
        tv_position?.text = user.position
        val ch = user.name.first()
        iv_user.setChar(ch)
        if (!TextUtils.isEmpty(user.email))
            tv_mail?.text = user.email
//        if (!TextUtils.isEmpty(user.depart_name))
//            tv_job?.text = user.position
        if (!TextUtils.isEmpty(user.url))
            Glide.with(this@UserActivity)
                    .load(user.headImage())
                    .into(iv_user)
    }


    companion object {
        fun start(ctx: Activity?) {
            ctx?.startActivity(Intent(ctx, UserActivity::class.java))
        }
    }
}
