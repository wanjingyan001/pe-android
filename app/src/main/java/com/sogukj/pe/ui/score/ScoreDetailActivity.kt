package com.sogukj.pe.ui.score

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import com.framework.base.ToolbarActivity
import com.sogukj.pe.R
import org.jetbrains.anko.textColor
import android.view.LayoutInflater
import android.widget.*
import com.google.gson.JsonSyntaxException
import com.sogukj.pe.bean.EmployeeInteractBean
import com.sogukj.pe.util.Trace
import com.sogukj.service.SoguApi
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_score_detail.*
import java.net.UnknownHostException


class ScoreDetailActivity : ToolbarActivity() {

    companion object {
        fun start(ctx: Activity?) {
            val intent = Intent(ctx, ScoreDetailActivity::class.java)
            ctx?.startActivity(intent)
        }
    }

    //设置组视图的显示文字
    val group = ArrayList<String>()

    //子视图显示文字
    val childs = ArrayList<ArrayList<EmployeeInteractBean.EmployeeItem>>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_score_detail)

        setBack(true)
        setTitle("员工互评考核结果")
        toolbar?.setBackgroundColor(Color.WHITE)
        toolbar?.apply {
            val title = this.findViewById(R.id.toolbar_title) as TextView?
            title?.textColor = Color.parseColor("#282828")
            val back = this.findViewById(R.id.toolbar_back) as ImageView
            back?.visibility = View.VISIBLE
            back.setImageResource(R.drawable.grey_back)
        }

        score_list.setGroupIndicator(null)
        score_list.setOnChildClickListener(object : ExpandableListView.OnChildClickListener {
            override fun onChildClick(parent: ExpandableListView?, v: View?, groupPosition: Int, childPosition: Int, id: Long): Boolean {
                showToast("${groupPosition}+++${childPosition}")
                return false
            }
        })

        SoguApi.getService(application)
                .grade_info()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ payload ->
                    if (payload.isOk) {
                        payload.payload?.forEach {
                            group.add(it.title!!)
                            childs.add(it.data!!)
                        }
                        score_list.setAdapter(MyExpAdapter(context, group, childs))
                    } else
                        showToast(payload.message)
                }, { e ->
                    Trace.e(e)
                    when (e) {
                        is JsonSyntaxException -> showToast("后台数据出错")
                        is UnknownHostException -> showToast("网络出错")
                        else -> showToast("未知错误")
                    }
                })
    }

    class MyExpAdapter(val context: Context, val group: ArrayList<String>, val childs: ArrayList<ArrayList<EmployeeInteractBean.EmployeeItem>>) : BaseExpandableListAdapter() {

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
                view = LayoutInflater.from(context).inflate(R.layout.item_group, null)
                holder = GroupHolder()
                holder.title = view.findViewById(R.id.title) as TextView
                holder.direction = view.findViewById(R.id.direction) as ImageView
                view.setTag(holder)
            } else {
                holder = view.getTag() as GroupHolder
            }

            holder.title?.text = group[groupPosition]
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
            return childs[groupPosition].size
        }

        override fun getChild(groupPosition: Int, childPosition: Int): Any {
            return childs[groupPosition][childPosition]
        }

        override fun getGroupId(groupPosition: Int): Long {
            return groupPosition.toLong()
        }

        override fun getChildView(groupPosition: Int, childPosition: Int, isLastChild: Boolean, convertView: View?, parent: ViewGroup?): View {
            var view = convertView
            var holder: ChildHolder? = null
            if (view == null) {
                view = LayoutInflater.from(context).inflate(R.layout.item_child, null)
                holder = ChildHolder()
                holder.seq = view.findViewById(R.id.seq) as TextView
                holder.depart = view.findViewById(R.id.depart) as TextView
                holder.name = view.findViewById(R.id.name) as TextView
                holder.score = view.findViewById(R.id.score) as TextView
                view.setTag(holder)
            } else {
                holder = view.getTag() as ChildHolder
            }

            holder.seq?.text = "${childs[groupPosition][childPosition].sort}"
            holder.depart?.text = childs[groupPosition][childPosition].department
            holder.name?.text = childs[groupPosition][childPosition].name
            holder.score?.text = childs[groupPosition][childPosition].grade_case

            if (childPosition == 0) {
                holder.seq?.setBackgroundColor(Color.TRANSPARENT)
                holder.depart?.setBackgroundColor(Color.TRANSPARENT)
                holder.name?.setBackgroundColor(Color.TRANSPARENT)
                holder.score?.setBackgroundColor(Color.TRANSPARENT)
            } else {
                holder.seq?.setBackgroundColor(Color.WHITE)
                holder.depart?.setBackgroundColor(Color.WHITE)
                holder.name?.setBackgroundColor(Color.WHITE)
                holder.score?.setBackgroundColor(Color.WHITE)
            }

            return view!!
        }

        class ChildHolder {
            var seq: TextView? = null
            var depart: TextView? = null
            var name: TextView? = null
            var score: TextView? = null
        }

        override fun getChildId(groupPosition: Int, childPosition: Int): Long {
            return childPosition.toLong()
        }

        override fun getGroupCount(): Int {
            return group.size
        }
    }
}
