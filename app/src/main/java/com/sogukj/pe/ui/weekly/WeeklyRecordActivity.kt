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
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.bean.TimeItem
import kotlinx.android.synthetic.main.activity_weekly_record.*
import org.jetbrains.anko.textColor
import java.text.SimpleDateFormat
import java.util.*

class WeeklyRecordActivity : ToolbarActivity() {

    var format = SimpleDateFormat("yyyy-MM-dd")

    lateinit var startBean: TimeItem
    lateinit var endBean: TimeItem

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_weekly_record)

        var tag = intent.getStringExtra(Extras.FLAG)
        if (tag == "ADD") {
            title = "补充工作日程"

            var calendar = Calendar.getInstance()
            var year = calendar.get(Calendar.YEAR)
            var month = calendar.get(Calendar.MONTH) + 1
            var day = calendar.get(Calendar.DAY_OF_MONTH)

            startBean = TimeItem(year, month, day)
            endBean = TimeItem(year, month, day)

            tv_start_time.text = formatTime(startBean)
            tv_end_time.text = formatTime(endBean)

        } else if (tag == "EDIT") {
            title = "修改工作日程"

            // TODO
            var calendar = Calendar.getInstance()
            var year = calendar.get(Calendar.YEAR)
            var month = calendar.get(Calendar.MONTH) + 1
            var day = calendar.get(Calendar.DAY_OF_MONTH)

            startBean = TimeItem(year, month, day)
            endBean = TimeItem(year, month, day)

            tv_start_time.text = formatTime(startBean)
            tv_end_time.text = formatTime(endBean)
        }

        var selector = LayoutInflater.from(this).inflate(R.layout.time_selector, null)
        var dialog = AlertDialog.Builder(this).setView(selector).create()
        var date_picker = selector.findViewById(R.id.date) as DatePicker
        var time_picker = selector.findViewById(R.id.time) as TimePicker
        time_picker.setIs24HourView(true)
        time_picker.visibility = View.GONE

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
            setResult(Activity.RESULT_OK)
            finish()
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
