package com.sogukj.pe.ui.weekly

import android.app.Activity
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.DatePicker
import android.widget.ImageView
import android.widget.TextView
import android.widget.TimePicker
import com.framework.base.ToolbarActivity
import com.google.gson.JsonSyntaxException
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.bean.TimeItem
import com.sogukj.pe.bean.WeeklyThisBean
import com.sogukj.pe.util.Trace
import com.sogukj.service.SoguApi
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_weekly_record.*
import org.jetbrains.anko.textColor
import java.net.UnknownHostException
import java.text.SimpleDateFormat
import java.util.*

class WeeklyRecordActivity : ToolbarActivity() {

    var format = SimpleDateFormat("yyyy-MM-dd")

    lateinit var startBean: TimeItem
    lateinit var endBean: TimeItem
    lateinit var week: WeeklyThisBean.Week

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_weekly_record)

        var tag = intent.getStringExtra(Extras.FLAG)
        if (tag == "ADD") {
            title = "补充工作日程"

//            var calendar = Calendar.getInstance()
//            var year = calendar.get(Calendar.YEAR)
//            var month = calendar.get(Calendar.MONTH) + 1
//            var day = calendar.get(Calendar.DAY_OF_MONTH)
//
//            startBean = TimeItem(year, month, day)
//            endBean = TimeItem(year, month, day)
//
//            tv_start_time.text = formatTime(startBean)
//            tv_end_time.text = formatTime(endBean)

            week = intent.getSerializableExtra(Extras.DATA) as WeeklyThisBean.Week

            tv_start_time.text = week.start_time
            tv_end_time.text = week.end_time

        } else if (tag == "EDIT") {
            title = "修改工作日程"
            week = intent.getSerializableExtra(Extras.DATA) as WeeklyThisBean.Week

            tv_start_time.text = week.start_time
            tv_end_time.text = week.end_time
            et_des.setText(week.info)
        }

        var selector = LayoutInflater.from(this).inflate(R.layout.time_selector, null)
        var dialog = AlertDialog.Builder(this).setView(selector).create()
        var date_picker = selector.findViewById(R.id.date) as DatePicker
        var time_picker = selector.findViewById(R.id.time) as TimePicker
        time_picker.setIs24HourView(true)
        time_picker.visibility = View.GONE

        tv_start_time.setOnClickListener {
            if (tag == "EDIT" || tag == "ADD") {
                return@setOnClickListener
            }
            dialog.setButton(DialogInterface.BUTTON_POSITIVE, "确定", object : DialogInterface.OnClickListener {
                override fun onClick(p0: DialogInterface?, p1: Int) {
                    if (startBean.compare(endBean) == 1) {
                        showToast("日期选择错误")
                        return
                    }
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
        }

        tv_end_time.setOnClickListener {
            if (tag == "EDIT" || tag == "ADD") {
                return@setOnClickListener
            }
            dialog.setButton(DialogInterface.BUTTON_POSITIVE, "确定", object : DialogInterface.OnClickListener {
                override fun onClick(p0: DialogInterface?, p1: Int) {
                    if (startBean.compare(endBean) == 1) {
                        showToast("日期选择错误")
                        return
                    }
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
        }

        toolbar?.setBackgroundColor(Color.WHITE)
        toolbar?.apply {
            val title = this.findViewById(R.id.toolbar_title) as TextView?
            title?.textColor = Color.parseColor("#282828")
            val back = this.findViewById(R.id.toolbar_back) as ImageView
            back?.visibility = View.VISIBLE
            back.setBackgroundResource(R.drawable.grey_back)
        }
        setBack(true)

        btn_commit.setOnClickListener {
            var weekly_id: Int? = null
            if (tag == "EDIT") {
                weekly_id = week.week_id
            }
            if (et_des.text.toString().trim() == "") {
                showToast("工作内容不能为空")
                return@setOnClickListener
            }
            SoguApi.getService(application)
                    .addEditReport(tv_start_time.text.toString(), tv_end_time.text.toString(), et_des.text.toString(), weekly_id)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeOn(Schedulers.io())
                    .subscribe({ payload ->
                        if (payload.isOk) {
                            payload.payload?.apply {
                                var df = SimpleDateFormat("HH:mm")
                                if (tag == "EDIT") {
                                    var intent = Intent()
                                    //week.time = df.format(Date())
                                    week.info = et_des.text.toString()
                                    intent.putExtra(Extras.DATA, week)
                                    setResult(Activity.RESULT_OK, intent)
                                    finish()
                                } else {
//                                    week = WeeklyThisBean.Week()
                                    week.time = df.format(Date())
//                                    week.s_times = tv_start_time.text.toString()
//                                    week.e_times = tv_end_time.text.toString()
                                    week.info = et_des.text.toString()
                                    // 应该是int，确实double
                                    week.week_id = this.toString().split(".")[0].toInt()

                                    var intent = Intent()
                                    intent.putExtra(Extras.DATA, week)
                                    setResult(Activity.RESULT_OK, intent)
                                    finish()
                                }
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
}
