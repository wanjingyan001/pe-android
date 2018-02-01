package com.sogukj.pe.ui.user

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.text.Html
import android.text.Spannable
import android.text.Spanned
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.framework.base.ToolbarActivity
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.bean.DepartmentBean
import com.sogukj.pe.bean.UserBean
import com.sogukj.pe.util.Trace
import com.sogukj.pe.view.*
import com.sogukj.service.SoguApi
import com.sogukj.util.Store
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_organization.*
import org.jetbrains.anko.backgroundColor
import org.jetbrains.anko.find
import org.jetbrains.anko.textColor


class OrganizationActivity : ToolbarActivity() {
    lateinit var departList: ArrayList<DepartmentBean>
    lateinit var alreadyList: ArrayList<UserBean>
    //    var departList = ArrayList<DepartmentBean>()
    var tag: String? = null
    val mine = Store.store.getUser(this)

    companion object {
        fun start(ctx: Activity?, departList: ArrayList<DepartmentBean>) {
            val intent = Intent(ctx, OrganizationActivity::class.java)
            intent.putExtra(Extras.DATA, departList)
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
    lateinit var orgAdapter: RecyclerAdapter<String>

    private fun initHistoryAdapter() {
        orgAdapter = RecyclerAdapter(this, { _adapter, parent, _ ->
            val convertView = _adapter.getView(R.layout.item_main_project_search, parent)
            object : RecyclerHolder<String>(convertView) {
                val tv1 = convertView.findViewById(R.id.tv1) as TextView
                override fun setData(view: View, data: String, position: Int) {
                    tv1.text = data
                }
            }
        })
        recycler_result.layoutManager = LinearLayoutManager(this)
        recycler_result.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))
        recycler_result.adapter = orgAdapter
        orgAdapter.onItemClick = { _, p ->
            //点击历史记录直接进行查询
//            searchStr = historyAdapter.dataList[p]
//            search_view.search = searchStr
//            doSearch(searchStr)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_organization)

        toolbar?.apply {
            val menu = this.findViewById(R.id.toolbar_menu) as ImageView
            menu.visibility = View.VISIBLE
            menu.setImageResource(R.drawable.org_sz)
        }
        search.root.backgroundColor = Color.WHITE
        (search.tv_cancel as TextView).textColor = Color.parseColor("#a0a4aa")
        search.et_search.hint = "搜索"
        search.onTextChange = { text ->
            if (text.isEmpty()) {
                ll_result.visibility = View.VISIBLE
                tv_result_title.text = Html.fromHtml(getString(R.string.tv_title_result_news, 0))
            } else {
                var searchStr = search.search
                ll_result.visibility = View.VISIBLE

                var tmp = ArrayList<String>()
                for (list in departList) {
                    for (item in list.data!!) {
                        if (item.name.contains(searchStr)) {
                            tmp.add(item.name)
                        }
                    }
                }
                orgAdapter.dataList.clear()
                orgAdapter.dataList.addAll(tmp)
                orgAdapter.notifyDataSetChanged()
                tv_result_title.text = Html.fromHtml(getString(R.string.tv_title_result_news, tmp.size))
            }
        }
        search.tv_cancel.setOnClickListener {
            search.et_search.setText("")
            ll_result.visibility = View.GONE
        }

        initHistoryAdapter()

        flag = intent.getStringExtra(Extras.FLAG)
        if (flag == "USER") {
            departList = intent.getSerializableExtra(Extras.DATA) as ArrayList<DepartmentBean>
            setBack(true)
            title = "通讯录"
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

        employee_list.setOnChildClickListener(object : ExpandableListView.OnChildClickListener {
            override fun onChildClick(parent: ExpandableListView?, v: View?, groupPosition: Int, childPosition: Int, id: Long): Boolean {
                var userBean = departList.get(groupPosition).data?.get(childPosition)
                if (flag == "WEEKLY") {
                    var intent = Intent()
                    intent.putExtra(Extras.DATA, userBean)
                    setResult(Activity.RESULT_OK, intent)
                    finish()
                } else if (flag == "SelectUser") {
                    val intent = Intent()
                    intent.putExtra(Extras.DATA, userBean)
                    if (tag == "CcPersonAdapter") {
                        setResult(Extras.RESULTCODE, intent)
                    } else {
                        setResult(Extras.RESULTCODE2, intent)
                    }
                    finish()
                } else {
                    userBean?.user_id?.let {
                        doRequest(it, userBean!!)
                    }
                }
                return false
            }
        })
    }

    fun setData(departList: List<DepartmentBean>) {
//        ll_jobs.removeAllViews()
//
//        for (i in 0 until departList.size) {
//            addGroup(departList[i])
//        }
        val group = ArrayList<String>()
        val childs = ArrayList<ArrayList<UserBean>>()
        for (bean in departList) {
            group.add(bean.de_name)
            childs.add(bean.data!!)
        }
        var adapter = MyExpAdapter(context, group, childs, flag!!)
        employee_list.setAdapter(adapter)

//        var adapter1 = MyExpAdapter.MyListAdapter(context, childs, flag!!)
//        changyong.adapter = adapter1
    }

//    fun addGroup(departmentBean: DepartmentBean) {
//        val group = View.inflate(this, R.layout.item_row_user_jobs, null);
//        ll_jobs.addView(group)
//        val tv_part = group.find<TextView>(R.id.tv_part)
//        tv_part.text = departmentBean.de_name
//
//        val list = departmentBean.data
//        if (list != null && list.size > 0) {
//            val tab_info = group.find<LinearLayout>(R.id.tab_info)
//            for (i in 0..list.size - 1)
//                addItem(list[i], tab_info)
//        }
//
//
//    }

//    fun addItem(userBean: UserBean, tab_info: LinearLayout) {
//        val item_content = View.inflate(this, R.layout.item_row_content_user_jobs, null)
//        tab_info.addView(item_content)
//        val iv_user = item_content.find<ImageView>(R.id.iv_user) as CircleImageView
//        val tv_name = item_content.find<TextView>(R.id.tv_name)
//        val tv_job = item_content.find<TextView>(R.id.tv_job)
//        val select_box = item_content.find<ImageView>(R.id.select_box)
//        if (TextUtils.isEmpty(userBean.name)) {
//            userBean.name = "--"
//        }
//        if (TextUtils.isEmpty(userBean.position)) {
//            userBean.position = "--"
//        }
//        if (TextUtils.isEmpty(userBean.phone)) {
//            userBean.phone = "--"
//        }
//        if (TextUtils.isEmpty(userBean.email)) {
//            userBean.email = "--"
//        }
//
//        tv_name.text = userBean.name + "\n" + userBean.phone
//        tv_job.text = userBean.position + "\n" + userBean.email
//        val link = LinkSpan();
//        if (!TextUtils.isEmpty(userBean.phone) && tv_name.text is Spannable) {
//            val s = tv_name.text as Spannable
//            s.setSpan(link, userBean.name.length, s.length, Spanned.SPAN_MARK_MARK)
//        }
//        if (userBean.name.isNotEmpty()) {
//            val ch = userBean.name.first()
//            iv_user.setChar(ch)
//        }
//
//
//        if (flag == "SelectUser") {
//            select_box.visibility = View.VISIBLE
//            select_box.setOnClickListener { v ->
//                select_box.isSelected = !v.isSelected
//            }
//        } else {
//            select_box.visibility = View.GONE
//        }
//
//
//        item_content.setOnClickListener {
//            if (flag == "WEEKLY") {
//                var intent = Intent()
//                intent.putExtra(Extras.DATA, userBean)
//                setResult(Activity.RESULT_OK, intent)
//                finish()
//            } else if (flag == "SelectUser") {
//                val intent = Intent()
//                intent.putExtra(Extras.DATA, userBean)
//                if (tag == "CcPersonAdapter") {
//                    setResult(Extras.RESULTCODE, intent)
//                } else {
//                    setResult(Extras.RESULTCODE2, intent)
//                }
//                finish()
//            } else {
//                userBean.user_id?.let {
//                    doRequest(it, userBean)
//                }
//            }
//        }
//    }

    class MyExpAdapter(val context: Context, val group: ArrayList<String>,
                       val childs: ArrayList<ArrayList<UserBean>>, val type: String) : BaseExpandableListAdapter() {

        override fun getGroup(groupPosition: Int): Any {
            return group[groupPosition]
        }

        override fun isChildSelectable(groupPosition: Int, childPosition: Int): Boolean {
            return true
        }

        override fun hasStableIds(): Boolean {
            return true
        }

        override fun getGroupView(groupPosition: Int, isExpanded: Boolean, convertView: View?, parent: ViewGroup?): View {
            var view = convertView
            var holder: GroupHolder? = null
            if (view == null) {
                view = LayoutInflater.from(context).inflate(R.layout.item_group_org, null)
                holder = GroupHolder()
                holder.title = view.findViewById(R.id.title) as TextView
                holder.direction = view.findViewById(R.id.direction) as ImageView
                view.setTag(holder)
            } else {
                holder = view.getTag() as GroupHolder
            }

            holder.title?.text = "${group[groupPosition]}（${childs[groupPosition].size}）"
            if (isExpanded) {
                holder.direction?.setBackgroundResource(R.drawable.up)
            } else {
                holder.direction?.setBackgroundResource(R.drawable.down)
            }

            return view!!
        }

        class GroupHolder {
            var title: TextView? = null
            var direction: ImageView? = null
        }

        override fun getChildrenCount(groupPosition: Int): Int {
            //return childs[groupPosition].size
            return 1
        }

        override fun getChild(groupPosition: Int, childPosition: Int): Any {
            return childs[groupPosition][childPosition]
        }

        override fun getGroupId(groupPosition: Int): Long {
            return groupPosition.toLong()
        }

        override fun getChildView(groupPosition: Int, childPosition: Int, isLastChild: Boolean, convertView: View?, parent: ViewGroup?): View {
            var view = convertView
            var holder: FatherHolder? = null
            if (view == null) {
                view = LayoutInflater.from(context).inflate(R.layout.item_child_org, null)
                holder = FatherHolder()
                holder.list = view.findViewById(R.id.listview) as MyListView
                view.setTag(holder)
            } else {
                holder = view.getTag() as FatherHolder
            }

            holder.list?.adapter = MyListAdapter(context, childs.get(groupPosition), type)

            return view!!
        }

        class FatherHolder {
            var list: MyListView? = null
        }

        override fun getChildId(groupPosition: Int, childPosition: Int): Long {
            return childPosition.toLong()
        }

        override fun getGroupCount(): Int {
            return group.size
        }

        class MyListAdapter(val context: Context, val datalist: ArrayList<UserBean>, val type: String) : BaseAdapter() {
            override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
                var view = convertView
                var holder: ChildHolder? = null
                if (view == null) {
                    view = LayoutInflater.from(context).inflate(R.layout.item_row_content_user_jobs, null)
                    holder = ChildHolder()
                    holder.iv_user = view.find<ImageView>(R.id.iv_user) as CircleImageView
                    holder.tv_name = view.find<TextView>(R.id.tv_name)
                    holder.tv_job = view.find<TextView>(R.id.tv_job)
                    holder.select_box = view.find<ImageView>(R.id.select_box)
                    view.setTag(holder)
                } else {
                    holder = view.getTag() as ChildHolder
                }

                var userBean = datalist.get(position)
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

                holder.tv_name?.text = userBean.name + "\n" + userBean.phone
                holder.tv_job?.text = userBean.position + "\n" + userBean.email
                val link = LinkSpan()
                if (!TextUtils.isEmpty(userBean.phone) && holder.tv_name?.text is Spannable) {
                    val s = holder.tv_name?.text as Spannable
                    s.setSpan(link, userBean.name.length, s.length, Spanned.SPAN_MARK_MARK)
                }
                if (userBean.name.isNotEmpty()) {
                    val ch = userBean.name.first()
                    holder.iv_user?.setChar(ch)
                }


                if (type == "SelectUser") {
                    var select_box = holder.select_box
                    select_box?.visibility = View.VISIBLE
                    select_box?.setOnClickListener { v ->
                        select_box?.isSelected = !v.isSelected
                    }
                } else {
                    holder.select_box?.visibility = View.GONE
                }

                return view!!
            }

            override fun getItem(position: Int): Any {
                return datalist.get(position)
            }

            override fun getItemId(position: Int): Long {
                return position.toLong()
            }

            override fun getCount(): Int {
                return datalist.size
            }

            class ChildHolder {
                var iv_user: CircleImageView? = null
                var tv_name: TextView? = null
                var tv_job: TextView? = null
                var select_box: ImageView? = null
            }
        }
    }

    fun doRequest(uId: Int, userBean: UserBean) {
        SoguApi.getService(application)
                .getPersonalResume(user_id = uId)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ payload ->
                    if (payload.isOk) {
                        UserResumeActivity.start(this, userBean)
                    } else {
                        showToast(payload.message)
                    }
                }, { e ->
                    Trace.e(e)
                })
    }
}
