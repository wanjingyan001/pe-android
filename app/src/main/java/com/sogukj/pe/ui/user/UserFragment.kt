package com.sogukj.pe.ui.user

import android.annotation.TargetApi
import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import com.bumptech.glide.Glide
import com.framework.base.ToolbarFragment
import com.sogukj.pe.R
import com.sogukj.pe.bean.DepartmentBean
import com.sogukj.pe.bean.UserBean
import com.sogukj.pe.ui.main.MainActivity
import com.sogukj.pe.ui.project.ProjectFocusActivity
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

class UserFragment : ToolbarFragment(), View.OnClickListener {
    override val containerViewId: Int
        get() = R.layout.activity_user


    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        toolbar_title.text = "个人中心"
        SoguApi.getService(activity.application)
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

        val user = Store.store.getUser(context)
        updateUser(user)
        if (null != user?.uid) {
            SoguApi.getService(activity.application)
                    .userInfo(user.uid!!)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeOn(Schedulers.io())
                    .subscribe({ payload ->
                        if (payload.isOk) {
                            val user = payload.payload
                            user?.apply { Store.store.setUser(context, this) }
                            updateUser(user)
                        } else showToast(payload.message)
                    }, { e ->
                        Trace.e(e)
                        showToast("提交失败")
                    })
        }


        ll_user.setOnClickListener {
            UserEditActivity.start(activity, departList)
        }
        structure.setOnClickListener {
            OrganizationActivity.start(activity, departList)
        }
        setting.setOnClickListener {
            SettingActivity.start(context)
        }
        focus_layout.setOnClickListener {
            ProjectFocusActivity.start(activity)
        }
        tv_1.setOnClickListener(this)
        tv_11.setOnClickListener(this)
        tv_2.setOnClickListener(this)
        tv_22.setOnClickListener(this)
        tv_3.setOnClickListener(this)
        tv_33.setOnClickListener(this)
        tv_4.setOnClickListener(this)
        tv_44.setOnClickListener(this)
        tv_5.setOnClickListener(this)
        tv_55.setOnClickListener(this)
        toolbar_menu.setOnClickListener {
            user?.let {
                CardActivity.start(activity, it)
            }
        }

    }

    val departList = ArrayList<DepartmentBean>()


    fun getBelongBean(userId: Int) {
        SoguApi.getService(activity.application)
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
        val user = Store.store.getUser(context)
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
            Glide.with(this@UserFragment)
                    .load(user.headImage())
                    .into(iv_user)
    }

    override fun onClick(view: View) {
        val activity = activity as MainActivity
        activity.doCheck(R.id.rb_project)
//        activity.rg_tab_main.check(R.id.rb_project)
        when (view.id) {
            R.id.tv_1, R.id.tv_11 -> {
                activity.fgProj.setCurrentItem(0)
            }
            R.id.tv_2, R.id.tv_22 -> {
                activity.fgProj.setCurrentItem(1)
            }
            R.id.tv_3, R.id.tv_33 -> {
                activity.fgProj.setCurrentItem(2)
            }
            R.id.tv_4, R.id.tv_44 -> {
                activity.fgProj.setCurrentItem(3)
            }
            R.id.tv_5, R.id.tv_55 -> {
                activity.fgProj.setCurrentItem(4)
            }
        }
    }


    companion object {
        fun start(ctx: Activity?) {
            ctx?.startActivity(Intent(ctx, UserFragment::class.java))
        }

        fun newInstance(): UserFragment {
            val fragment = UserFragment()
            val args = Bundle()
            fragment.arguments = args
            return fragment
        }
    }
}