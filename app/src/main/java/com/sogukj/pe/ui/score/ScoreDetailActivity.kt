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
import kotlinx.android.synthetic.main.activity_score_detail.*


class ScoreDetailActivity : ToolbarActivity() {

    companion object {
        fun start(ctx: Activity?) {
            val intent = Intent(ctx, ScoreDetailActivity::class.java)
            ctx?.startActivity(intent)
        }
    }

    class Content(var name: String? = null, var score: String? = null) {
//        var name: String = "张三"
//        var score: String = "67.5"
    }

    //设置组视图的显示文字
    var group = arrayOf("创新能力1", "创新能力2", "创新能力3", "创新能力4")

    //子视图显示文字
    val childs = arrayOf(
            arrayOf(Content("姓名", "打分情况"), Content("1", "1"), Content("1", "1"), Content("1", "1"), Content("1", "1")),
            arrayOf(Content("姓名", "打分情况"), Content("1", "1"), Content("1", "1"), Content("1", "1"), Content("1", "1")),
            arrayOf(Content("姓名", "打分情况"), Content("1", "1"), Content("1", "1"), Content("1", "1"), Content("1", "1")),
            arrayOf(Content("姓名", "打分情况"), Content("1", "1"), Content("1", "1"), Content("1", "1"), Content("1", "1")))

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_score_detail)

        setBack(true)
        setTitle("打分详情页")
        toolbar?.setBackgroundColor(Color.WHITE)
        toolbar?.apply {
            val title = this.findViewById(R.id.toolbar_title) as TextView?
            title?.textColor = Color.parseColor("#282828")
            val back = this.findViewById(R.id.toolbar_back) as ImageView
            back?.visibility = View.VISIBLE
            back.setImageResource(R.drawable.grey_back)
        }

        score_list.setAdapter(MyExpAdapter(context, group, childs))
        score_list.setGroupIndicator(null)
        score_list.setOnChildClickListener(object : ExpandableListView.OnChildClickListener {
            override fun onChildClick(parent: ExpandableListView?, v: View?, groupPosition: Int, childPosition: Int, id: Long): Boolean {
                showToast("${groupPosition}+++${childPosition}")
                return false
            }
        })
    }

    class MyExpAdapter(val context: Context, val group: Array<String>, val childs: Array<Array<Content>>) : BaseExpandableListAdapter() {

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
                holder.name = view.findViewById(R.id.name) as TextView
                holder.score = view.findViewById(R.id.score) as TextView
                view.setTag(holder)
            } else {
                holder = view.getTag() as ChildHolder
            }

            holder.name?.text = childs[groupPosition][childPosition].name
            holder.score?.text = childs[groupPosition][childPosition].score

            if (childPosition == 0) {
                holder.name?.setBackgroundColor(Color.TRANSPARENT)
                holder.score?.setBackgroundColor(Color.TRANSPARENT)
            } else {
                holder.name?.setBackgroundColor(Color.WHITE)
                holder.score?.setBackgroundColor(Color.WHITE)
            }

            return view!!
        }

        class ChildHolder {
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
