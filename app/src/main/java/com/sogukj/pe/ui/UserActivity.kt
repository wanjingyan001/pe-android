package com.sogukj.pe.ui

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import com.framework.base.ToolbarActivity
import com.sogukj.pe.R
import com.sogukj.pe.bean.DepartmentBean
import com.sogukj.service.SoguApi
import com.sogukj.util.Store
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_user.*

/**
 * Created by qinfei on 17/7/18.
 */

class UserActivity : ToolbarActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user)
        setTitle("个人信息")
        setBack(true)
        SoguApi.getService(application)
                .userInfo()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ payload ->
                    if (payload.isOk) {
                        val departList = payload.payload
                        departList?.apply { setData(this) }
                    } else
                        showToast(payload.message)
                }, { e ->
                    showToast("数据获取失败")
                })
    }

    fun setData(departList: List<DepartmentBean>) {

    }

    override fun onStart() {
        super.onStart()
        val user = Store.store.getUser(this)
        user?.apply {
            tv_name?.text = "用户$uid"
            tv_mobile?.text = phone
            tv_mail?.text = email
            tv_job?.text = depart_name
        }
    }

    companion object {
        fun start(ctx: Activity?) {
            ctx?.startActivity(Intent(ctx, UserActivity::class.java))
        }
    }
}
