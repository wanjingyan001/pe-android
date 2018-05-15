package com.sogukj.pe.ui.calendar

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.text.Spannable
import android.text.Spanned
import android.text.TextUtils
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.framework.base.ToolbarActivity
import com.google.gson.Gson
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.bean.DepartmentBean
import com.sogukj.pe.bean.UserBean
import com.sogukj.pe.bean.Users
import com.sogukj.pe.util.Trace
import com.sogukj.pe.view.CircleImageView
import com.sogukj.pe.view.LinkSpan
import com.sogukj.service.SoguApi
import com.sogukj.util.Store
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_select_user.*
import org.jetbrains.anko.find

class SelectUserActivity : ToolbarActivity() {
    override val menuId: Int
        get() = R.menu.menu_confirm

    var flag: String? = null
    var data: Users? = null
    var tag: String? = null
    val mine = Store.store.getUser(this)
//    val selectUser = Users()

    companion object {
        fun start(ctx: Fragment?, users: Users?) {
            val intent = Intent(ctx?.context, SelectUserActivity::class.java)
            intent.putExtra(Extras.DATA, users)
            intent.putExtra(Extras.FLAG, "SelectUser")
            ctx?.startActivityForResult(intent, Extras.REQUESTCODE)
        }

        fun startForResult(ctx: Activity?, tag: String, users: Users?) {
            val intent = Intent(ctx, SelectUserActivity::class.java)
            intent.putExtra(Extras.FLAG, "SelectUser")
            intent.putExtra(Extras.NAME, tag)
            intent.putExtra(Extras.DATA, users)
            ctx?.startActivityForResult(intent, Extras.REQUESTCODE)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_select_user)
        title = "请选择"
        setBack(true)
        flag = intent.getStringExtra(Extras.FLAG)
        tag = intent.getStringExtra(Extras.NAME)
        data = intent.getSerializableExtra(Extras.DATA) as Users?
        if (data == null){
            data = Users()
        }
        val departList = ArrayList<DepartmentBean>()
        SoguApi.getService(application)
                .userDepart()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ payload ->
                    if (payload.isOk) {
                        payload.payload?.forEach {
                            departList.add(it)
                        }
                        setData(departList)
                    } else
                        showCustomToast(R.drawable.icon_toast_fail, payload.message)
                }, { e ->
                    Trace.e(e)
                    showCustomToast(R.drawable.icon_toast_fail, "数据获取失败")
                })
    }


    fun setData(departList: List<DepartmentBean>) {
        ll_jobs.removeAllViews()

        for (i in 0 until departList.size) {
            addGroup(departList[i])
        }
    }

    fun addGroup(departmentBean: DepartmentBean) {
        val group = View.inflate(this, R.layout.item_row_user_jobs, null)
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
        if (userBean.user_id != mine?.uid) {
            tab_info.addView(item_content)
        }
        val iv_user = item_content.find<ImageView>(R.id.iv_user) as CircleImageView
        val tv_name = item_content.find<TextView>(R.id.tv_name)
        val tv_job = item_content.find<TextView>(R.id.tv_job)
        val select_box = item_content.find<ImageView>(R.id.select_box)
        select_box.tag = userBean.user_id
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
        val link = LinkSpan()
        if (!TextUtils.isEmpty(userBean.phone) && tv_name.text is Spannable) {
            val s = tv_name.text as Spannable
            s.setSpan(link, userBean.name.length, s.length, Spanned.SPAN_MARK_MARK)
        }
        if (userBean.name.isNotEmpty()) {
            val ch = userBean.name.first()
            iv_user.setChar(ch)
        }

        item_content.setOnClickListener {
            select_box.isSelected = !select_box.isSelected
            data?.let {
                if (select_box.isSelected) {
                    if (!it.selectUsers.contains(userBean)) {
                        it.selectUsers.add(userBean)
                    }
                } else {
                    val iterator = it.selectUsers.iterator()
                    while (iterator.hasNext()) {
                        val next = iterator.next()
                        if (next.user_id == userBean.user_id){
                            iterator.remove()
                        }
                    }
                }
            }

        }

        data?.let {
            it.selectUsers.forEachIndexed { index, user ->
                if (user.user_id == userBean.user_id){
                    select_box.isSelected  = true
                    return
                }else{
                    select_box.isSelected  = false
                }
            }
        }

    }


    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.action_confirm -> {
                //确认选择
                val intent = Intent()
                Log.d("WJY",Gson().toJson(data))
                intent.putExtra(Extras.DATA, data)
                when (tag) {
                    "CcPersonAdapter" -> setResult(Extras.RESULTCODE, intent)
                    "ExecutiveAdapter" -> setResult(Extras.RESULTCODE2, intent)
                    else -> setResult(Activity.RESULT_OK, intent)
                }
                finish()
            }
        }
        return super.onOptionsItemSelected(item)
    }
}
