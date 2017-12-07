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
import com.sogukj.pe.bean.WeeklyItemBean
import com.sogukj.pe.bean.WeeklyWatchBean
import com.sogukj.pe.view.CircleImageView
import com.sogukj.pe.view.MyListView
import kotlinx.android.synthetic.main.buchong_empty.*
import kotlinx.android.synthetic.main.buchong_full.*
import kotlinx.android.synthetic.main.fragment_schedule.*
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

        var mItems = resources.getStringArray(R.array.spinner_this)
        val arr_adapter = ArrayAdapter<String>(context, R.layout.spinner_item, mItems)
        arr_adapter.setDropDownViewResource(R.layout.spinner_dropdown)
        spinner_this.adapter = arr_adapter
        spinner_this.setOnItemSelectedListener(object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View,
                                        pos: Int, id: Long) {
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
            }
        })
        childs = 1

        initView()
    }

    var childs = 0

    fun initView() {
        val data = ArrayList<WeeklyItemBean>()
        data.add(WeeklyItemBean())
        data.add(WeeklyItemBean())
        data.add(WeeklyItemBean())

        val item = inflate.inflate(R.layout.weekly_item, null) as LinearLayout
        val event_list = item.findViewById(R.id.event_list) as MyListView
        val adapter = WeeklyEventAdapter(context, data)
        event_list.adapter = adapter
        root.addView(item, childs++)

        val item1 = inflate.inflate(R.layout.weekly_item, null) as LinearLayout
        val event_list1 = item1.findViewById(R.id.event_list) as MyListView
        val adapter1 = WeeklyEventAdapter(context, data)
        event_list1.adapter = adapter1
        root.addView(item1, childs++)

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

    class WeeklyEventAdapter(var context: Context, val list: ArrayList<WeeklyItemBean>) : BaseAdapter() {

        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            var viewHolder: ViewHolder
            var conView = convertView
            if (conView == null) {
                viewHolder = ViewHolder()
                conView = LayoutInflater.from(context).inflate(R.layout.weekly_event, null) as LinearLayout
                //conView = LayoutInflater.from(context).inflate(R.layout.weekly_leave, null) as LinearLayout
//                viewHolder.icon = conView.findViewById(R.id.icon) as CircleImageView
//                viewHolder.name = conView.findViewById(R.id.name) as TextView
                conView.setTag(viewHolder)
            } else {
                viewHolder = conView.getTag() as ViewHolder
            }
//            viewHolder.icon?.setImageResource(list.get(position).icon)
//            viewHolder.name?.text = list.get(position).name
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
