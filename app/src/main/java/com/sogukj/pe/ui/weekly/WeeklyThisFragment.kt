package com.sogukj.pe.ui.weekly

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.framework.base.BaseFragment
import com.sogukj.pe.Extras

import com.sogukj.pe.R
import com.sogukj.pe.bean.WeeklyWatchBean
import com.sogukj.pe.view.CircleImageView
import kotlinx.android.synthetic.main.buchong_empty.*
import kotlinx.android.synthetic.main.buchong_full.*
import kotlinx.android.synthetic.main.fragment_weekly_this.*
import kotlinx.android.synthetic.main.send.*

/**
 * A simple [Fragment] subclass.
 */
class WeeklyThisFragment : BaseFragment() {

    override val containerViewId: Int
        get() = R.layout.fragment_weekly_this

    lateinit var inflate: LayoutInflater

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        inflate = LayoutInflater.from(context)
        initView()
    }

    fun initView() {
        val item = inflate.inflate(R.layout.weekly_item, null) as LinearLayout

        val ll_event = inflate.inflate(R.layout.weekly_event, null) as LinearLayout
        val ll_leave = inflate.inflate(R.layout.weekly_leave, null) as LinearLayout

        item.addView(ll_event)
        item.addView(ll_leave)

        root.addView(item, 0)

        val item1 = inflate.inflate(R.layout.weekly_item, null) as LinearLayout

        val ll_event1 = inflate.inflate(R.layout.weekly_event, null) as LinearLayout
        //跟踪 bg_genzong FFFF5858
        //出差 bg_chuchai FF56B9F6
        val ll_leave1 = inflate.inflate(R.layout.weekly_leave, null) as LinearLayout

        item1.addView(ll_event1)
        item1.addView(ll_leave1)

        root.addView(item1, 1)

        //showToast("${root.childCount}")

        bu_chong_empty.setOnClickListener {
            val intent = Intent(context, WeeklyRecordActivity::class.java)
            intent.putExtra(Extras.FLAG, "ADD")
            startActivityForResult(intent, 0x001)
        }
        bu_chong_empty.visibility = View.VISIBLE
        buchong_full.visibility = View.GONE

        buchong_edit.setOnClickListener {
            val intent = Intent(context, WeeklyRecordActivity::class.java)
            intent.putExtra(Extras.FLAG, "EDIT")
            startActivityForResult(intent, 0x002)
        }

        // TODO test
        var beanObj = WeeklyWatchBean.BeanObj()
        beanObj.icon = R.drawable.bg
        beanObj.name = "张三"
        var list = ArrayList<WeeklyWatchBean.BeanObj>()
        list.add(beanObj)
        list.add(beanObj)
        list.add(beanObj)
        var send_adapter = MyAdapter(context, list)
        grid_send_to.adapter = send_adapter

        var list1 = ArrayList<WeeklyWatchBean.BeanObj>()
        list1.add(beanObj)
        list1.add(beanObj)
        list1.add(beanObj)
        list1.add(beanObj)
        list1.add(beanObj)
        var beanObj1 = WeeklyWatchBean.BeanObj()
        beanObj1.icon = R.drawable.send_add
        beanObj1.name = "添加"
        list1.add(beanObj1)
        var chaosong_adapter = MyAdapter(context, list1)
        grid_chaosong_to.adapter = chaosong_adapter
    }

    class MyAdapter(var context: Context, val list: ArrayList<WeeklyWatchBean.BeanObj>) : BaseAdapter() {

        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            var viewHolder: ViewHolder
            var conView = convertView
            if (conView == null) {
                viewHolder = ViewHolder()
                conView = LayoutInflater.from(context).inflate(R.layout.send_item, null) as LinearLayout
                viewHolder.icon = conView.findViewById(R.id.icon) as CircleImageView
                viewHolder.name = conView.findViewById(R.id.name) as TextView
                conView.setTag(viewHolder)
            } else {
                viewHolder = conView.getTag() as ViewHolder
            }
            viewHolder.icon?.setImageResource(list.get(position).icon)
            viewHolder.name?.text = list.get(position).name
            return conView
        }

        override fun getItem(position: Int): Any {
            return list.get(position)
        }

        override fun getItemId(position: Int): Long {
            return position.toLong()
        }

        override fun getCount(): Int {
            return list.size
        }

        class ViewHolder {
            var icon: ImageView? = null
            var name: TextView? = null
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Log.e("onActivityResult", "onActivityResult")
        if (requestCode == 0x001 && resultCode == Activity.RESULT_OK) {//ADD
            bu_chong_empty.visibility = View.GONE
            buchong_full.visibility = View.VISIBLE
        } else if (requestCode == 0x002 && resultCode == Activity.RESULT_OK) {//EDIT
            bu_chong_empty.visibility = View.GONE
            buchong_full.visibility = View.VISIBLE
        }
    }
}
