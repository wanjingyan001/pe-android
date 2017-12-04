package com.sogukj.pe.ui.project

import android.app.Activity
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.widget.DatePicker
import android.widget.TimePicker
import com.framework.base.ToolbarActivity
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.bean.ProjectBean
import com.sogukj.pe.bean.RecordInfoBean
import com.sogukj.pe.util.DateUtils
import com.sogukj.pe.util.Trace
import com.sogukj.service.SoguApi
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_record_detail.*
import java.net.UnknownHostException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.HashMap

class RecordDetailActivity : ToolbarActivity() {

    lateinit var project: ProjectBean
    lateinit var item: RecordInfoBean.ListBean
    val gson = Gson()
    var type: String = ""
    var format = SimpleDateFormat("yyyy.MM.dd HH:mm")

    lateinit var startBean: TimeBean
    lateinit var endBean: TimeBean

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_record_detail)

        type = intent.getStringExtra(Extras.TYPE)
        if (type.equals("ADD")) {
            setTitle("新增记录")

            var calendar = Calendar.getInstance()
            var year = calendar.get(Calendar.YEAR)
            var month = calendar.get(Calendar.MONTH) + 1
            var day = calendar.get(Calendar.DAY_OF_MONTH)
            var hour = calendar.get(Calendar.HOUR_OF_DAY)
            var minute = calendar.get(Calendar.MINUTE)

            startBean = TimeBean(year, month, day, hour, minute)
            endBean = TimeBean(year, month, day, hour, minute)

            tv_start_time.text = formatTime(startBean)
            tv_end_time.text = formatTime(endBean)

            var selector = LayoutInflater.from(this).inflate(R.layout.time_selector, null)
            var dialog = AlertDialog.Builder(this).setView(selector).create()
            var date_picker = selector.findViewById(R.id.date) as DatePicker
            var time_picker = selector.findViewById(R.id.time) as TimePicker
            time_picker.setIs24HourView(true)

            tv_start_time.setOnClickListener {
                dialog.setButton(DialogInterface.BUTTON_POSITIVE, "确定", object : DialogInterface.OnClickListener {
                    override fun onClick(p0: DialogInterface?, p1: Int) {
                        tv_start_time.text = formatTime(startBean)
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
                time_picker.currentHour = startBean.hour
                time_picker.currentMinute = startBean.minute
                time_picker.setOnTimeChangedListener(object : TimePicker.OnTimeChangedListener {
                    override fun onTimeChanged(p0: TimePicker?, p1: Int, p2: Int) {
                        startBean.hour = p1
                        startBean.minute = p2
                    }
                })
            }

            tv_end_time.setOnClickListener {
                dialog.setButton(DialogInterface.BUTTON_POSITIVE, "确定", object : DialogInterface.OnClickListener {
                    override fun onClick(p0: DialogInterface?, p1: Int) {
                        tv_end_time.text = formatTime(endBean)
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
                time_picker.currentHour = endBean.hour
                time_picker.currentMinute = endBean.minute
                time_picker.setOnTimeChangedListener(object : TimePicker.OnTimeChangedListener {
                    override fun onTimeChanged(p0: TimePicker?, p1: Int, p2: Int) {
                        endBean.hour = p1
                        endBean.minute = p2
                    }
                })
            }
        } else if (type.equals("VIEW")) {
            setTitle("记录详情")

            tv_start_time.setOnClickListener(null)
            tv_end_time.setOnClickListener(null)
            et_visiter.isFocusable = false
            et_visiter.isEnabled = false
            et_des.isFocusable = false
            et_des.isEnabled = false
            cb_important.isEnabled = false

            item = intent.getSerializableExtra(Extras.DATA2) as RecordInfoBean.ListBean
            tv_start_time.text = DateUtils.timet("${item.start_time}")
            tv_end_time.text = DateUtils.timet("${item.end_time}")
            et_visiter.setText(item.visits)
            et_des.setText(item.des)
            cb_important.isChecked = if (item.important == 0) false else true
        }

        project = intent.getSerializableExtra(Extras.DATA) as ProjectBean
        company_name.text = project.name

        setBack(true)
    }

    /**
     * month是已经调整过的month
     */
    fun formatTime(time: TimeBean): String {
        var month_str = "${time.month}"
        if (time.month < 10) {
            month_str = "0${time.month}"
        }
        var day_str = "${time.day}"
        if (time.day < 10) {
            day_str = "0${time.day}"
        }
        var hour_str = "${time.hour}"
        if (time.hour < 10) {
            hour_str = "0${time.hour}"
        }
        var minute_str = "${time.minute}"
        if (time.minute < 10) {
            minute_str = "0${time.minute}"
        }
        return "${time.year}.${month_str}.${day_str} ${hour_str}:${minute_str}";
    }

    override val menuId: Int
        get() = R.menu.menu_mark

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val flag = super.onCreateOptionsMenu(menu)
        val menuMark = menu.findItem(R.id.action_mark) as MenuItem
        if (type.equals("ADD")) {
            menuMark?.title = "提交"
        } else if (type.equals("VIEW")) {
            menuMark?.title = ""
        }
        return flag
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.action_mark -> {
                if (type.equals("ADD")) {
                    project.company_id?.let {
                        upload(it)
                    }
                }
            }
        }
        return false
    }

    var paramsMap = HashMap<String, Any>()

    fun upload(it: Int) {
        var isImportant = 0
        if (cb_important.isChecked) {
            isImportant = 1
        }

        var start_Date = format.parse("${startBean.year}.${startBean.month}.${startBean.day} ${startBean.hour}:${startBean.minute}")
        start_Date.time /= 1000
        var end_Date = format.parse("${endBean.year}.${endBean.month}.${endBean.day} ${endBean.hour}:${endBean.minute}")
        end_Date.time /= 1000
        if (end_Date.time < start_Date.time) {
            showToast("日期错误")
            return
        }
        if (et_des.text.toString().trim() == "") {
            showToast("跟踪情况描述不能为空")
            return
        }

        var paramsObj = HashMap<String, Any>()
        paramsObj.put("company_id", it)
        paramsObj.put("start_time", start_Date.time)
        paramsObj.put("end_time", end_Date.time)
        paramsObj.put("visits", et_visiter.text.toString())
        paramsObj.put("des", et_des.text.toString())
        paramsObj.put("important", isImportant)
        paramsMap.put("ae", paramsObj)

        SoguApi.getService(application)
                .addRecord(paramsMap)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ payload ->
                    if (payload.isOk) {
                        setResult(Activity.RESULT_OK)
                        finish()
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

    companion object {
        fun startAdd(ctx: Activity?, project: ProjectBean) {
            val intent = Intent(ctx, RecordDetailActivity::class.java)
            intent.putExtra(Extras.DATA, project)
            intent.putExtra(Extras.TYPE, "ADD")
            ctx?.startActivityForResult(intent, 0x001)
        }

        fun startView(ctx: Activity?, project: ProjectBean, item: RecordInfoBean.ListBean) {
            val intent = Intent(ctx, RecordDetailActivity::class.java)
            intent.putExtra(Extras.DATA, project)
            intent.putExtra(Extras.DATA2, item)
            intent.putExtra(Extras.TYPE, "VIEW")
            ctx?.startActivityForResult(intent, 0x002)
        }
    }

    class TimeBean(year: Int, month: Int, day: Int, hour: Int, minute: Int) {
        var year = year
        var month = month
        var day = day
        var hour = hour
        var minute = minute
    }

    override fun onBackPressed() {
        super.onBackPressed()
        if (type.equals("VIEW")) {
            setResult(Activity.RESULT_CANCELED)
            finish()
        }
    }
}
