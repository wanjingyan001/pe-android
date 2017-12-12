package com.sogukj.pe.ui.user

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.text.Spannable
import android.text.Spanned
import android.text.TextUtils
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.framework.base.ToolbarActivity
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.bean.DepartmentBean
import com.sogukj.pe.bean.UserBean
import com.sogukj.pe.util.Trace
import com.sogukj.pe.view.CircleImageView
import com.sogukj.pe.view.LinkSpan
import com.sogukj.service.SoguApi
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_organization.*
import org.jetbrains.anko.find


class OrganizationActivity : ToolbarActivity() {
    lateinit var departList: ArrayList<DepartmentBean>
    lateinit var alreadyList: ArrayList<UserBean>
    //    var departList = ArrayList<DepartmentBean>()
    var tag: String? = null

    companion object {
        fun start(ctx: Activity?) {
            val intent = Intent(ctx, OrganizationActivity::class.java)
            intent.putExtra(Extras.FLAG, "USER")

            ctx?.startActivity(intent)
        }

        fun startForResult(ctx: Activity?, tag: String) {
            val intent = Intent(ctx, OrganizationActivity::class.java)
            intent.putExtra(Extras.FLAG, "SelectUser")
            intent.putExtra(Extras.NAME, tag)
            ctx?.startActivityForResult(intent, Extras.REQUESTCODE)
        }

        fun startForResult(ctx: Fragment?) {
            val intent = Intent(ctx?.context, OrganizationActivity::class.java)
            intent.putExtra(Extras.FLAG, "SelectUser")
            ctx?.startActivityForResult(intent, Extras.REQUESTCODE)
        }
    }

    var flag: String? = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_organization)
//
        flag = intent.getStringExtra(Extras.FLAG)
        if (flag == "USER") {
            departList = intent.getSerializableExtra(Extras.DATA) as ArrayList<DepartmentBean>
            setBack(true)
            title = "公司组织架构"
            setData(departList)
        } else if (flag == "WEEKLY") {
            setBack(true)
            title = "请选择"
            departList = ArrayList<DepartmentBean>()
            alreadyList = intent.getSerializableExtra(Extras.DATA) as ArrayList<UserBean>

            SoguApi.getService(application)
                    .userDepart()
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeOn(Schedulers.io())
                    .subscribe({ payload ->
                        if (payload.isOk) {
                            departList.clear()
                            payload.payload?.apply {
                                for (list in this) {
                                    var LIST = list.clone()
                                    var dataList = LIST.data
                                    for (bean in alreadyList) {
                                        var databeanIndex: Int = 0
                                        while (databeanIndex < dataList!!.size) {
                                            if (bean.name == dataList[databeanIndex].name) {
                                                dataList.removeAt(databeanIndex)
                                                break
                                            }
                                            databeanIndex++
                                        }
                                    }
                                    if (dataList!!.size != 0) {
                                        departList.add(LIST)
                                    }
                                }
                            }
                            setData(departList)
                        } else
                            showToast(payload.message)
                    }, { e ->
                        Trace.e(e)
                        showToast("数据获取失败")
                    })
        } else if (flag == "SelectUser") {
            title = "请选择"
            setBack(true)
            tag = intent.getStringExtra(Extras.NAME)
            departList = ArrayList<DepartmentBean>()
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
                            setData(departList)
                        } else
                            showToast(payload.message)
                    }, { e ->
                        Trace.e(e)
                        showToast("数据获取失败")
                    })
        }


    }

    fun setData(departList: List<DepartmentBean>) {
        ll_jobs.removeAllViews()

        for (i in 0 until departList.size) {
            addGroup(departList[i])
        }
    }

    fun addGroup(departmentBean: DepartmentBean) {
        val group = View.inflate(this, R.layout.item_row_user_jobs, null);
        ll_jobs.addView(group)
        val tv_part = group.find<TextView>(R.id.tv_part)
        tv_part.text = departmentBean.de_name

        val list = departmentBean.data
        if (list != null && list.size > 0) {
            val tab_info = group.find<LinearLayout>(R.id.tab_info)
            for (i in 0..list.size - 1)
                addItem(list[i], tab_info)
        }


    }

    fun addItem(userBean: UserBean, tab_info: LinearLayout) {
        val item_content = View.inflate(this, R.layout.item_row_content_user_jobs, null)
        tab_info.addView(item_content)
        val iv_user = item_content.find<ImageView>(R.id.iv_user) as CircleImageView
        val tv_name = item_content.find<TextView>(R.id.tv_name)
        val tv_job = item_content.find<TextView>(R.id.tv_job)
        if (TextUtils.isEmpty(userBean.name)) {
            userBean.name = "--"
        }
        if (TextUtils.isEmpty(userBean.position)) {
            userBean.position = "--"
        }
        if (TextUtils.isEmpty(userBean.phone)) {
            userBean.phone = "--"
        }
        if (TextUtils.isEmpty(userBean.email)) {
            userBean.email = "--"
        }

        tv_name.text = userBean.name + "\n" + userBean.phone
        tv_job.text = userBean.position + "\n" + userBean.email
        val link = LinkSpan();
        if (!TextUtils.isEmpty(userBean.phone) && tv_name.text is Spannable) {
            val s = tv_name.text as Spannable
            s.setSpan(link, userBean.name.length, s.length, Spanned.SPAN_MARK_MARK)
        }
        if (userBean.name.isNotEmpty()) {
            val ch = userBean.name.first()
            iv_user.setChar(ch)
        }


        item_content.setOnClickListener {
            if (flag == "WEEKLY") {
                var intent = Intent()
                intent.putExtra(Extras.DATA, userBean)
                setResult(Activity.RESULT_OK, intent)
            } else if (flag == "SelectUser") {
                val intent = Intent()
                intent.putExtra(Extras.DATA, userBean)
                if (tag == "CcPersonAdapter") {
                    setResult(Extras.RESULTCODE, intent)
                } else {
                    setResult(Extras.RESULTCODE2, intent)
                }
                finish()
            }
        }

    }
}
