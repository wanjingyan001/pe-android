package com.sogukj.pe.ui

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.bumptech.glide.Glide
import com.framework.base.ToolbarActivity
import com.sogukj.pe.R
import com.sogukj.pe.bean.DepartmentBean
import com.sogukj.pe.bean.UserBean
import com.sogukj.service.SoguApi
import com.sogukj.util.Store
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_user.*
import org.jetbrains.anko.find
import org.jetbrains.anko.sdk25.coroutines.onClick

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
                .userDepart()
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

        iv_user.onClick {
            UserEditActivity.start(this@UserActivity)
        }
    }

    fun setData(departList: List<DepartmentBean>) {
        ll_jobs.removeAllViews()

        for (i in 0..departList.size - 1) {
            addGroup(departList[i]);
        }
    }

    fun addGroup(departmentBean: DepartmentBean) {
        val group = View.inflate(this, R.layout.item_row_user_jobs, null);
        ll_jobs.addView(group)
        val tv_part = group.find<TextView>(R.id.tv_part)
        tv_part.text = departmentBean.de_name

        val list = departmentBean.data
        if (list != null && list.size > 0) {
//            val list_view=group.find<ListView>(R.id.list_view)
            val tab_info = group.find<LinearLayout>(R.id.tab_info)
            for (i in 0..list.size - 1)
                addItem(list[i], tab_info);
        }


    }

    fun addItem(userBean: UserBean, tab_info: LinearLayout) {
        val item_content = View.inflate(this, R.layout.item_row_content_user_jobs, null)
        tab_info.addView(item_content)
        val iv_head = item_content.find<ImageView>(R.id.iv_head)
        val tv_name = item_content.find<TextView>(R.id.tv_name)
        val tv_job = item_content.find<TextView>(R.id.tv_job)
        tv_name.text = userBean.name + "\n" + userBean.phone
        tv_job.text = userBean.position + "\n" + userBean.email
    }

    override fun onStart() {
        super.onStart()
        val user = Store.store.getUser(this)
        user?.apply {
            tv_name?.text = "用户$uid"
            tv_mobile?.text = phone
            if (!TextUtils.isEmpty(email))
                tv_mail?.text = email
            if (!TextUtils.isEmpty(depart_name))
                tv_job?.text = depart_name
            if (!TextUtils.isEmpty(url))
                Glide.with(this@UserActivity)
                        .load(url)
                        .into(iv_user)
        }
    }

    companion object {
        fun start(ctx: Activity?) {
            ctx?.startActivity(Intent(ctx, UserActivity::class.java))
        }
    }
}
