package com.sogukj.pe.ui.weekly

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.framework.base.BaseFragment
import com.google.gson.JsonSyntaxException
import com.sogukj.pe.Extras

import com.sogukj.pe.R
import com.sogukj.pe.bean.UserBean
import com.sogukj.pe.bean.WeeklyThisBean
import com.sogukj.pe.ui.user.OrganizationActivity
import com.sogukj.pe.util.Trace
import com.sogukj.pe.view.CircleImageView
import com.sogukj.pe.view.MyListView
import com.sogukj.pe.view.WeeklyDotView
import com.sogukj.service.SoguApi
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.buchong_empty.*
import kotlinx.android.synthetic.main.buchong_full.*
import kotlinx.android.synthetic.main.fragment_weekly_this.*
import kotlinx.android.synthetic.main.send.*
import java.net.UnknownHostException

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

        var tag = arguments.getString(Extras.FLAG)
        if (tag == "MAIN") {
            spinner_this.visibility = View.GONE
        } else if (tag == "PERSONAL") {
            spinner_this.visibility = View.VISIBLE
        }

        baseActivity?.let {
            SoguApi.getService(it.application)
                    .getWeekly()
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeOn(Schedulers.io())
                    .subscribe({ payload ->
                        if (payload.isOk) {
                            payload.payload?.apply {
                                initView(this)
                            }
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
    }

    var childs = 0
    lateinit var week: WeeklyThisBean.Week

    fun initView(loaded: WeeklyThisBean) {

        if (spinner_this.visibility == View.VISIBLE) {
            childs = 1
        }

        loaded.automatic?.let {
            for (items in it.iterator()) {
                val item = inflate.inflate(R.layout.weekly_item, null) as LinearLayout
                val index = item.findViewById(R.id.tv_index) as TextView
                val weekday = item.findViewById(R.id.tv_week) as TextView
                val date = item.findViewById(R.id.tv_date) as TextView
                val event_list = item.findViewById(R.id.event_list) as MyListView

                var str = items.date!!.split("-")
                index.text = str[2].toInt().toString()
                date.text = "${str[0]}年${str[1]}月"
                weekday.text = items.week_day

                val adapter = WeeklyEventAdapter(context, items.data!!)
                event_list.adapter = adapter

                root.addView(item, childs++)
            }
        }
        if (loaded.week == null) {
            bu_chong_empty.visibility = View.VISIBLE
            buchong_full.visibility = View.GONE

            bu_chong_empty.setOnClickListener {
                val intent = Intent(context, WeeklyRecordActivity::class.java)
                intent.putExtra(Extras.FLAG, "ADD")
                startActivityForResult(intent, ADD)
            }
        } else {
            bu_chong_empty.visibility = View.GONE
            buchong_full.visibility = View.VISIBLE
            week = loaded.week as WeeklyThisBean.Week
            var time = buchong_full.findViewById(R.id.time) as TextView
            var times = buchong_full.findViewById(R.id.times) as TextView
            var info = buchong_full.findViewById(R.id.info) as TextView
            var buchong_edit = buchong_full.findViewById(R.id.buchong_edit) as ImageView

            var S_TIME = week.s_times?.split("-")
            var E_TIME = week.e_times?.split("-")

            time.text = week.time
            times.text = "${S_TIME?.get(1)}.${S_TIME?.get(2)}-${E_TIME?.get(1)}.${E_TIME?.get(2)}"
            info.text = week.info

            buchong_edit.setOnClickListener {
                val intent = Intent(context, WeeklyRecordActivity::class.java)
                intent.putExtra(Extras.FLAG, "EDIT")
                intent.putExtra(Extras.DATA, week)
                startActivityForResult(intent, EDIT)
            }
        }

        var beanObj = UserBean()
        beanObj.name = "添加"
        var list = ArrayList<UserBean>()
        list.add(beanObj)
        var send_adapter = MyAdapter(context, list)
        grid_send_to.adapter = send_adapter

        var list1 = ArrayList<UserBean>()
        var beanObj1 = UserBean()
        beanObj1.name = "添加"
        list1.add(beanObj1)
        var chaosong_adapter = MyAdapter(context, list1)
        grid_chaosong_to.adapter = chaosong_adapter

        grid_send_to.setOnItemClickListener { parent, view, position, id ->
            var already = ArrayList<UserBean>()
            var list = ArrayList<UserBean>(send_adapter.list)
            list.removeAt(list.size - 1)
            already.addAll(list)
            list = ArrayList<UserBean>(chaosong_adapter.list)
            list.removeAt(list.size - 1)
            already.addAll(list)

            var obj = parent.getItemAtPosition(position) as UserBean
            if (obj.name == "添加") {
                val intent = Intent(context, OrganizationActivity::class.java)
                intent.putExtra(Extras.FLAG, "WEEKLY")
                intent.putExtra(Extras.DATA, already)
                startActivityForResult(intent, SEND)
            }
        }

        grid_chaosong_to.setOnItemClickListener { parent, view, position, id ->
            var already = ArrayList<UserBean>()
            var list = ArrayList<UserBean>(send_adapter.list)
            list.removeAt(list.size - 1)
            already.addAll(list)
            list = ArrayList<UserBean>(chaosong_adapter.list)
            list.removeAt(list.size - 1)
            already.addAll(list)

            var obj = parent.getItemAtPosition(position) as UserBean
            if (obj.name == "添加") {
                val intent = Intent(context, OrganizationActivity::class.java)
                intent.putExtra(Extras.FLAG, "WEEKLY")
                intent.putExtra(Extras.DATA, already)
                startActivityForResult(intent, CHAO_SONG)
            }
        }

        btn_commit.setOnClickListener {

        }
    }

    var ADD = 0x005
    var EDIT = 0x006
    var SEND = 0x007
    var CHAO_SONG = 0x008

    class WeeklyEventAdapter(var context: Context, val list: ArrayList<WeeklyThisBean.Automatic.WeeklyData>) : BaseAdapter() {

        val EVENT = 0x001
        val LEAVE = 0x002

        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            var conView = convertView
            var item = list.get(position)

            if (getItemViewType(position) == EVENT) {
                // 会议，跟踪记录
                conView = LayoutInflater.from(context).inflate(R.layout.weekly_event, null) as LinearLayout
                var dot = conView.findViewById(R.id.dot) as WeeklyDotView
                var event = conView.findViewById(R.id.event) as TextView
                var AI = conView.findViewById(R.id.AI) as TextView
                var tag = conView.findViewById(R.id.tag) as TextView

                dot.setTime(item.time!!)
                event.text = item.title
                AI.visibility = if (item.is_collect == 1) View.VISIBLE else View.INVISIBLE
                tag.text = item.type_name
            } else {
                // 请假，出差
                conView = LayoutInflater.from(context).inflate(R.layout.weekly_leave, null) as LinearLayout
                var dot = conView.findViewById(R.id.dot) as WeeklyDotView
                var event = conView.findViewById(R.id.event) as TextView
                var AI = conView.findViewById(R.id.AI) as TextView
                var tv_start_time = conView.findViewById(R.id.tv_start_time) as TextView
                var tv_end_time = conView.findViewById(R.id.tv_end_time) as TextView
                var tag = conView.findViewById(R.id.tag) as TextView

                dot.setTime(item.time!!)
                event.text = item.title
                AI.visibility = if (item.is_collect == 1) View.VISIBLE else View.INVISIBLE
                tag.text = item.type_name
                tv_start_time.text = item.start_time
                tv_end_time.text = item.end_time
            }
            return conView
        }

        override fun getItemViewType(position: Int): Int {
            if (list.get(position).start_time.isNullOrEmpty()) {
                return EVENT
            }
            return LEAVE
        }

        override fun getViewTypeCount(): Int {
            return 0x003
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
    }

    class MyAdapter(var context: Context, val list: ArrayList<UserBean>) : BaseAdapter() {

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
            if (list.get(position).name == "添加") {
                viewHolder.icon?.setImageResource(R.drawable.send_add)
            } else {
                viewHolder.icon?.setChar(list.get(position).name?.first())
            }
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
            var icon: CircleImageView? = null
            var name: TextView? = null
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == ADD && resultCode == Activity.RESULT_OK) {//ADD
            bu_chong_empty.visibility = View.GONE
            buchong_full.visibility = View.VISIBLE

            week = data?.getSerializableExtra(Extras.DATA) as WeeklyThisBean.Week
            var time = buchong_full.findViewById(R.id.time) as TextView
            var times = buchong_full.findViewById(R.id.times) as TextView
            var info = buchong_full.findViewById(R.id.info) as TextView

            var S_TIME = week.s_times?.split("-")
            var E_TIME = week.e_times?.split("-")

            time.text = week.time
            times.text = "${S_TIME?.get(1)}.${S_TIME?.get(2)}-${E_TIME?.get(1)}.${E_TIME?.get(2)}"
            info.text = week.info
        } else if (requestCode == EDIT && resultCode == Activity.RESULT_OK) {//EDIT
            bu_chong_empty.visibility = View.GONE
            buchong_full.visibility = View.VISIBLE

            week = data?.getSerializableExtra(Extras.DATA) as WeeklyThisBean.Week
            var info = buchong_full.findViewById(R.id.info) as TextView
            info.text = week.info
        } else if (requestCode == SEND && resultCode == Activity.RESULT_OK) {//SEND
            var adapter = grid_send_to.adapter as MyAdapter
            var beanObj = data?.getSerializableExtra(Extras.DATA) as UserBean
            adapter.list.add(adapter.list.size - 1, beanObj)
            adapter.notifyDataSetChanged()
        } else if (requestCode == CHAO_SONG && resultCode == Activity.RESULT_OK) {//CHAO_SONG
            var adapter = grid_chaosong_to.adapter as MyAdapter
            var beanObj = data?.getSerializableExtra(Extras.DATA) as UserBean
            adapter.list.add(adapter.list.size - 1, beanObj)
            adapter.notifyDataSetChanged()
        }
    }

    companion object {

        fun newInstance(tag: String): WeeklyThisFragment {
            val fragment = WeeklyThisFragment()
            var args = Bundle()
            args.putString(Extras.FLAG, tag)
            fragment.setArguments(args)
            return fragment
        }
    }
}
