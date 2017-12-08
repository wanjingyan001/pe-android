package com.sogukj.pe.ui.weekly

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.framework.base.BaseFragment
import com.sogukj.pe.R
import com.sogukj.pe.bean.TimeItem
import kotlinx.android.synthetic.main.fragment_weekly_wait_to_watch.*
import com.sogukj.pe.bean.WeeklyWatchBean
import com.sogukj.pe.view.*
import org.jetbrains.anko.support.v4.startActivityForResult
import org.jetbrains.anko.textColor
import java.text.SimpleDateFormat
import java.util.*

/**
 * A simple [Fragment] subclass.
 */
class WeeklyWaitToWatchFragment : BaseFragment() {

    override val containerViewId: Int
        get() = R.layout.fragment_weekly_wait_to_watch

    lateinit var adapter: RecyclerAdapter<WeeklyWatchBean>
    var format = SimpleDateFormat("yyyy-MM-dd")

    lateinit var startBean: TimeItem
    lateinit var endBean: TimeItem
    var currentClick = 0

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        var mItems = resources.getStringArray(R.array.spinner)
        val arr_adapter = ArrayAdapter<String>(context, R.layout.spinner_item, mItems)
        arr_adapter.setDropDownViewResource(R.layout.spinner_dropdown)
        spinner.adapter = arr_adapter
        spinner.setOnItemSelectedListener(object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View,
                                        pos: Int, id: Long) {
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
            }
        })


        adapter = RecyclerAdapter<WeeklyWatchBean>(context, { _adapter, parent, type ->
            val convertView = _adapter.getView(R.layout.item_wait_watch, parent) as LinearLayout
            object : RecyclerHolder<WeeklyWatchBean>(convertView) {
                val tv_title = convertView.findViewById(R.id.title_date) as TextView
                val grid = convertView.findViewById(R.id.grid_list) as MyGridView
                override fun setData(view: View, data: WeeklyWatchBean, position: Int) {
                    tv_title.text = data.date
                    data.list?.let {
                        var adapter = MyAdapter(context, it)
                        adapter.sort()
                        grid.adapter = adapter

                        grid.setOnItemClickListener(object : AdapterView.OnItemClickListener {
                            override fun onItemClick(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {

                                (grid.adapter.getItem(position) as WeeklyWatchBean.BeanObj).click = true
                                grid.setTag("CLICK")

                                val intent = Intent(context, PersonalWeeklyActivity::class.java)
                                startActivityForResult(intent, 0x011)
                            }
                        })
                    }
                }
            }
        })
        adapter.onItemClick = { v, p ->
        }
        val layoutManager = LinearLayoutManager(context)
        layoutManager.orientation = LinearLayoutManager.VERTICAL
        list.layoutManager = layoutManager
        list.adapter = adapter


        var bean = WeeklyWatchBean()
        bean.date = "本周"
        var obj = WeeklyWatchBean.BeanObj()
        obj.icon = R.drawable.bg
        obj.name = "名利里"
        obj.click = false
        bean.list.add(obj)
        bean.list.add(obj)
        bean.list.add(obj)
        bean.list.add(obj)
        bean.list.add(obj)
        adapter.dataList.add(bean)
        adapter.dataList.add(bean)
        adapter.dataList.add(bean)
        adapter.dataList.add(bean)
        adapter.dataList.add(bean)

        var bean1 = WeeklyWatchBean()
        bean1.date = "本周"
        var obj1 = WeeklyWatchBean.BeanObj()
        obj1.icon = R.drawable.week_y
        obj1.name = "名利里"
        obj1.click = false
        bean1.list.add(obj1)
        bean1.list.add(obj1)
        adapter.dataList.add(bean1)

        adapter.notifyDataSetChanged()




        currentClick == 0
        total.setClick(true)
        unread.setClick(false)
        readed.setClick(false)
        total.setOnClickListener {
            if (currentClick == 0) {
                return@setOnClickListener
            }
            currentClick = 0
            total.setClick(true)
            unread.setClick(false)
            readed.setClick(false)
        }
        unread.setOnClickListener {
            if (currentClick == 1) {
                return@setOnClickListener
            }
            currentClick = 1
            total.setClick(false)
            unread.setClick(true)
            readed.setClick(false)
        }
        readed.setOnClickListener {
            if (currentClick == 2) {
                return@setOnClickListener
            }
            currentClick = 2
            total.setClick(false)
            unread.setClick(false)
            readed.setClick(true)
        }


        var calendar = Calendar.getInstance()
        var year = calendar.get(Calendar.YEAR)
        var month = calendar.get(Calendar.MONTH) + 1
        var day = calendar.get(Calendar.DAY_OF_MONTH)

        startBean = TimeItem(year, month, day)
        endBean = TimeItem(year, month, day)

        start.text = formatTime(startBean)
        end.text = formatTime(endBean)

        var selector = LayoutInflater.from(context).inflate(R.layout.time_selector, null)
        var dialog = AlertDialog.Builder(context).setView(selector).create()
        var date_picker = selector.findViewById(R.id.date) as DatePicker
        var time_picker = selector.findViewById(R.id.time) as TimePicker
        time_picker.setIs24HourView(true)
        time_picker.visibility = View.GONE

        start.setOnClickListener {
            dialog.setButton(DialogInterface.BUTTON_POSITIVE, "确定", object : DialogInterface.OnClickListener {
                override fun onClick(p0: DialogInterface?, p1: Int) {
                    start.text = formatTime(startBean)
                }
            })
            dialog.show()

            date_picker.init(startBean.year, startBean.month - 1, startBean.day, object : DatePicker.OnDateChangedListener {
                override fun onDateChanged(p0: DatePicker?, p1: Int, p2: Int, p3: Int) {
                    startBean.year = p1
                    startBean.month = p2 + 1
                    startBean.day = p3
                }
            })
        }

        end.setOnClickListener {
            dialog.setButton(DialogInterface.BUTTON_POSITIVE, "确定", object : DialogInterface.OnClickListener {
                override fun onClick(p0: DialogInterface?, p1: Int) {
                    end.text = formatTime(endBean)
                }
            })
            dialog.show()

            date_picker.init(endBean.year, endBean.month - 1, endBean.day, object : DatePicker.OnDateChangedListener {
                override fun onDateChanged(p0: DatePicker?, p1: Int, p2: Int, p3: Int) {
                    endBean.year = p1
                    endBean.month = p2 + 1
                    endBean.day = p3
                }
            })
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 0x011) {
            var grid = root.findViewWithTag("CLICK") as GridView
            (grid.adapter as MyAdapter).notifyDataSetChanged()
            grid.setTag("")
        }
    }

    /**
     * month是已经调整过的month
     */
    fun formatTime(time: TimeItem): String {
        var month_str = "${time.month}"
        if (time.month < 10) {
            month_str = "0${time.month}"
        }
        var day_str = "${time.day}"
        if (time.day < 10) {
            day_str = "0${time.day}"
        }
        return "${time.year}-${month_str}-${day_str}";
    }

    class MyAdapter(var context: Context, val list: ArrayList<WeeklyWatchBean.BeanObj>) : BaseAdapter() {

        // click=true放前面
        fun sort() {

        }

        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            var viewHolder: ViewHolder
            var conView = convertView
            if (conView == null) {
                viewHolder = ViewHolder()
                conView = LayoutInflater.from(context).inflate(R.layout.watch_item, null) as LinearLayout
                viewHolder.icon = conView.findViewById(R.id.icon) as CircleImageView
                viewHolder.name = conView.findViewById(R.id.name) as TextView
                conView.setTag(viewHolder)
            } else {
                viewHolder = conView.getTag() as ViewHolder
            }
            viewHolder.icon?.setImageResource(list.get(position).icon)
            viewHolder.name?.text = list.get(position).name
            if (list.get(position).click) {
                viewHolder.icon?.alpha = 0.8f
                viewHolder.name?.textColor = Color.parseColor("#A0A4AA")
            } else {
                viewHolder.icon?.alpha = 1f
                viewHolder.name?.textColor = Color.parseColor("#282828")
            }
            return conView
        }

        override fun getItem(position: Int): WeeklyWatchBean.BeanObj {
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
}
