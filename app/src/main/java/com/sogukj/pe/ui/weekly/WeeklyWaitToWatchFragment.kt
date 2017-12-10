package com.sogukj.pe.ui.weekly

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.framework.base.BaseFragment
import com.google.gson.JsonSyntaxException
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.bean.ReceiveSpinnerBean
import com.sogukj.pe.bean.TimeItem
import kotlinx.android.synthetic.main.fragment_weekly_wait_to_watch.*
import com.sogukj.pe.bean.WeeklyWatchBean
import com.sogukj.pe.util.Trace
import com.sogukj.pe.view.*
import com.sogukj.service.SoguApi
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import org.jetbrains.anko.textColor
import java.net.UnknownHostException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

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

    val loadedData = ArrayList<WeeklyWatchBean>()
    lateinit var spinner_data: ArrayList<ReceiveSpinnerBean>
    var selected_depart_id: Long = 0

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val arr_adapter = ArrayAdapter<String>(context, R.layout.spinner_item)
        arr_adapter.setDropDownViewResource(R.layout.spinner_dropdown)
        spinner.adapter = arr_adapter
        spinner.setSelection(0, true)
        spinner.setOnItemSelectedListener(object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View,
                                        pos: Int, id: Long) {
                selected_depart_id = parent.selectedItemId
                Log.e("IIIIIDDDDDD", "${selected_depart_id}")
                var selected_depart = spinner_data.get(selected_depart_id.toInt()).name
                Log.e("IIIIIDDDDDD", selected_depart)
                Log.e("IIIIIDDDDDD", "${spinner_data.get(selected_depart_id.toInt()).id ?: 100000}")

                adapter.dataList.clear()
                adapter.notifyDataSetChanged()
                doRequest()
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
                    data.data?.let {
                        var adapter = MyAdapter(context, it)
                        adapter.sort()
                        grid.adapter = adapter

                        grid.setOnItemClickListener(object : AdapterView.OnItemClickListener {
                            override fun onItemClick(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {

                                (grid.adapter.getItem(position) as WeeklyWatchBean.BeanObj).is_read = 1
                                grid.setTag("CLICK")

                                val intent = Intent(context, PersonalWeeklyActivity::class.java)
                                intent.putExtra(Extras.DATA, grid.adapter.getItem(position) as WeeklyWatchBean.BeanObj)
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
        list.addItemDecoration(SpaceItemDecoration(20))
        list.adapter = adapter

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

            //sort()

            adapter.dataList.clear()
            adapter.notifyDataSetChanged()

            doRequest()
        }
        unread.setOnClickListener {
            if (currentClick == 1) {
                return@setOnClickListener
            }
            currentClick = 1
            total.setClick(false)
            unread.setClick(true)
            readed.setClick(false)

            //sort()

            adapter.dataList.clear()
            adapter.notifyDataSetChanged()

            doRequest()
        }
        readed.setOnClickListener {
            if (currentClick == 2) {
                return@setOnClickListener
            }
            currentClick = 2
            total.setClick(false)
            unread.setClick(false)
            readed.setClick(true)

            //sort()

            adapter.dataList.clear()
            adapter.notifyDataSetChanged()

            doRequest()
        }


        var calendar = Calendar.getInstance()
        var year = calendar.get(Calendar.YEAR)
        var month = calendar.get(Calendar.MONTH) + 1
        var day = calendar.get(Calendar.DAY_OF_MONTH)

        startBean = TimeItem(year, month, day)
        endBean = TimeItem(year, month, day)

//        start.text = formatTime(startBean)
//        end.text = formatTime(endBean)
        start.text = ""
        end.text = ""

        var selector = LayoutInflater.from(context).inflate(R.layout.time_selector, null)
        var dialog = AlertDialog.Builder(context).setView(selector).create()
        var date_picker = selector.findViewById(R.id.date) as DatePicker
        var time_picker = selector.findViewById(R.id.time) as TimePicker
        time_picker.setIs24HourView(true)
        time_picker.visibility = View.GONE

        start.setOnClickListener {
            dialog.setButton(DialogInterface.BUTTON_POSITIVE, "确定", object : DialogInterface.OnClickListener {
                override fun onClick(p0: DialogInterface?, p1: Int) {

                    if (end.text.trim() == "") {
                        start.text = formatTime(startBean)
                        return
                    }

                    if (startBean.compare(endBean) == 1) {
                        showToast("日期选择错误")
                        return
                    }

                    start.text = formatTime(startBean)

                    adapter.dataList.clear()
                    adapter.notifyDataSetChanged()
                    doRequest()
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

                    if (start.text.trim() == "") {
                        end.text = formatTime(endBean)
                        return
                    }

                    if (startBean.compare(endBean) == 1) {
                        showToast("日期选择错误")
                        return
                    }

                    end.text = formatTime(endBean)

                    adapter.dataList.clear()
                    adapter.notifyDataSetChanged()
                    doRequest()
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

        currentClick = 0
        selected_depart_id = 0

        SoguApi.getService(baseActivity!!.application)
                .getDepartment()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ payload ->
                    if (payload.isOk) {
                        payload.payload?.apply {
                            spinner_data = this
                            var total = ReceiveSpinnerBean()
                            total.id = null
                            total.name = "全部"
                            spinner_data.add(0, total)

                            for (item in spinner_data) {
                                arr_adapter.add(item.name)
                            }
                            arr_adapter.notifyDataSetChanged()
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

    fun doRequest() {
        var is_read: Int? = null
        if (currentClick == 1) {
            is_read = 0
        } else if (currentClick == 2) {
            is_read = 1
        }

        var de_id = spinner_data.get(selected_depart_id.toInt()).id

        var start_time: String? = null
        var end_time: String? = null
        if (start.text.trim() != "") {
            start_time = formatTime(startBean)
            end_time = formatTime(endBean)
        }

        SoguApi.getService(baseActivity!!.application)
                .receive(is_read, de_id, start_time, end_time)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ payload ->
                    if (payload.isOk) {
                        payload.payload?.apply {
                            adapter.dataList.addAll(this)
                            adapter.notifyDataSetChanged()
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

    private fun sort() {
        if (currentClick == 0) {
            adapter.dataList = loadedData
            adapter.notifyDataSetChanged()
        } else {
            // 未读-1-false，已读-2-true
            var flag = if (currentClick == 1) 0 else 1
            var obj_list = ArrayList<WeeklyWatchBean>()
            for (i in 0 until loadedData.size) {
                var objs = ArrayList<WeeklyWatchBean.BeanObj>()
                for (j in 0 until loadedData[i].data.size) {
                    if (loadedData[i].data[j].is_read == flag) {
                        objs.add(loadedData[i].data[j])
                    }
                }
                if (objs.size != 0) {
                    var bean = WeeklyWatchBean()
                    bean.date = loadedData[i].date
                    bean.data.addAll(objs)
                    obj_list.add(bean)
                }
            }
            adapter.dataList = obj_list
            adapter.notifyDataSetChanged()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 0x011) {
            var grid = root.findViewWithTag("CLICK") as GridView
            (grid.adapter as MyAdapter).sort()
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
            for (i in 0 until list.size) {
                if (list[i].is_read == 0) {
                    for (j in (i + 1) until list.size) {
                        if (list[j].is_read == 1) {
                            var tmp = list[i]
                            list[i] = list[j]
                            list[j] = tmp
                            break
                        }
                    }
                    break
                }
            }
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
            viewHolder.icon?.setChar(list.get(position).name?.first())
            viewHolder.name?.text = list.get(position).name
            if (list.get(position).is_read == 1) {
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
            var icon: CircleImageView? = null
            var name: TextView? = null
        }
    }
}
