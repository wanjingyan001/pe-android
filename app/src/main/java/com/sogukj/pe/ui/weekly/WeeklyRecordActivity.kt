package com.sogukj.pe.ui.weekly

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import com.bigkoo.pickerview.TimePickerView
import com.framework.base.BaseActivity
import com.google.gson.JsonSyntaxException
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.bean.WeeklyThisBean
import com.sogukj.pe.util.Trace
import com.sogukj.pe.util.Utils
import com.sogukj.service.SoguApi
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_weekly_record.*
import kotlinx.android.synthetic.main.layout_shareholder_toolbar.*
import java.net.UnknownHostException
import java.text.SimpleDateFormat
import java.util.*

class WeeklyRecordActivity : BaseActivity() {

    var format = SimpleDateFormat("yyyy-MM-dd")

    lateinit var week: WeeklyThisBean.Week

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_weekly_record)
        Utils.setWindowStatusBarColor(this, R.color.white)

        var calendar = Calendar.getInstance()

        var tag = intent.getStringExtra(Extras.FLAG)
        if (tag == "ADD") {
            toolbar_title.text = "补充工作日程"

            tv_start_time.text = format.format(calendar.time)
            tv_end_time.text = format.format(calendar.time)

            week = intent.getSerializableExtra(Extras.DATA) as WeeklyThisBean.Week

            tv_start_time.text = week.start_time
            tv_end_time.text = week.end_time

        } else if (tag == "EDIT") {
            toolbar_title.text  = "修改工作日程"
            week = intent.getSerializableExtra(Extras.DATA) as WeeklyThisBean.Week

            tv_start_time.text = week.start_time
            tv_end_time.text = week.end_time
            et_des.setText(week.info)
        }
        addTv.visibility = View.GONE
        back.setOnClickListener { finish() }
        tv_start_time.setOnClickListener {
            if (tag == "EDIT" || tag == "ADD") {
                return@setOnClickListener
            }

            calendar.time = format.parse(tv_start_time.text.toString())
            val timePicker = TimePickerView.Builder(this, { date, view ->
                tv_start_time.text = format.format(date)
            })
                    //年月日时分秒 的显示与否，不设置则默认全部显示
                    .setType(booleanArrayOf(true, true, true, true, true, false))
                    .setDividerColor(Color.DKGRAY)
                    .setContentSize(15)
                    .setDate(calendar)
                    .setCancelColor(resources.getColor(R.color.shareholder_text_gray))
                    .build()
            timePicker.show()
        }

        tv_end_time.setOnClickListener {
            if (tag == "EDIT" || tag == "ADD") {
                return@setOnClickListener
            }
            calendar.time = format.parse(tv_end_time.text.toString())
            val timePicker = TimePickerView.Builder(this, { date, view ->
                tv_end_time.text = format.format(date)
            })
                    //年月日时分秒 的显示与否，不设置则默认全部显示
                    .setType(booleanArrayOf(true, true, true, true, true, false))
                    .setDividerColor(Color.DKGRAY)
                    .setContentSize(15)
                    .setDate(calendar)
                    .setCancelColor(resources.getColor(R.color.shareholder_text_gray))
                    .build()
            timePicker.show()
        }

//        toolbar?.setBackgroundColor(Color.WHITE)
//        toolbar?.apply {
//            val title = this.findViewById(R.id.toolbar_title) as TextView?
//            title?.textColor = Color.parseColor("#282828")
//            val back = this.findViewById(R.id.toolbar_back) as ImageView
//            back?.visibility = View.VISIBLE
//            back.setBackgroundResource(R.drawable.grey_back)
//        }
//        setBack(true)

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
}
