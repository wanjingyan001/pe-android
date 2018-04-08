package com.sogukj.pe.ui.project

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import com.framework.base.ToolbarActivity
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.bean.ProjectBean
import com.sogukj.pe.util.Trace
import com.sogukj.pe.util.Utils
import com.sogukj.pe.view.CalendarDingDing
import com.sogukj.service.SoguApi
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_project_tc.*
import org.jetbrains.anko.backgroundResource
import org.jetbrains.anko.textColor
import java.util.*


class ProjectTCActivity : ToolbarActivity() {

    companion object {
        // isView  true表示历史记录，false表示要退出的项目
        fun start(ctx: Activity?, isView: Boolean, bean: ProjectBean) {
            val intent = Intent(ctx, ProjectTCActivity::class.java)
            intent.putExtra(Extras.TYPE, isView)
            intent.putExtra(Extras.DATA, bean)
            ctx?.startActivityForResult(intent, 0x001)
        }
    }

    var type = false
    lateinit var project: ProjectBean

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_project_tc)
        setBack(true)
//        if (type) {
//            title = "退出记录"
//        } else {
//            title = "退出"
//        }

        type = intent.getBooleanExtra(Extras.TYPE, false)
        project = intent.getSerializableExtra(Extras.DATA) as ProjectBean
        titleComp.text = project.name
        bianhao.text = "项目编号${project.number}"

        if (type) {
            title = "退出记录"
        } else {
            title = "退出"
        }

        if (type) {
            ll_btn.visibility = View.GONE
            var parent = quit_layout.parent as LinearLayout
            parent.layoutParams.height = Utils.dpToPx(context, 130)
            parent.requestLayout()
            quit_layout.visibility = View.GONE
            load()
        } else {
            part_quit.backgroundResource = R.drawable.tc_true
            part_quit.textColor = Color.parseColor("#1787fb")
            total_quit.backgroundResource = R.drawable.tc_false
            total_quit.textColor = Color.parseColor("#282828")

            part_blue.visibility = View.INVISIBLE
            tr_wtccb.visibility = View.VISIBLE
            tr_bc.visibility = View.VISIBLE
            tr_tzzt.visibility = View.GONE
            tr_bck.visibility = View.GONE
            mType = 1

            part_quit.setOnClickListener {
                part_quit.backgroundResource = R.drawable.tc_true
                part_quit.textColor = Color.parseColor("#1787fb")
                total_quit.backgroundResource = R.drawable.tc_false
                total_quit.textColor = Color.parseColor("#282828")

                tr_wtccb.visibility = View.VISIBLE
                tr_bc.visibility = View.VISIBLE
                tr_tzzt.visibility = View.GONE
                tr_bck.visibility = View.GONE

                mType = 1

                part_blue.visibility = View.INVISIBLE
            }

            total_quit.setOnClickListener {
                total_quit.backgroundResource = R.drawable.tc_true
                total_quit.textColor = Color.parseColor("#1787fb")
                part_quit.backgroundResource = R.drawable.tc_false
                part_quit.textColor = Color.parseColor("#282828")

                tr_wtccb.visibility = View.GONE
                tr_bc.visibility = View.GONE
                tr_tzzt.visibility = View.VISIBLE
                tr_bck.visibility = View.VISIBLE

                mType = 2

                if (project.quit == 1) {
                    part_blue.visibility = View.VISIBLE
                } else {
                    part_blue.visibility = View.INVISIBLE
                }
            }

            btn_commit.setOnClickListener {
                upload()
            }

            var startDD = CalendarDingDing(context)
            et_tzsj.setOnClickListener {
//                val timePicker = TimePickerView.Builder(this, { date, view ->
//                    et_tzsj.text = Utils.getYMD(date)
//                    startDate = date
//                    checkDays()
//                })
//                        //年月日时分秒 的显示与否，不设置则默认全部显示
//                        .setType(booleanArrayOf(true, true, true, false, false, false))
//                        .setDividerColor(Color.DKGRAY)
//                        .setContentSize(21)
//                        //.setDate(selectedDate)
//                        .setCancelColor(resources.getColor(R.color.shareholder_text_gray))
//                        .build()
//                timePicker.show()
                startDD.show(2, Date(), object : CalendarDingDing.onTimeClick {
                    override fun onClick(date: Date?) {
                        if(date != null){
                            et_tzsj.text = Utils.getYMD(date)
                            startDate = date
                            checkDays()
                        }
                    }
                })
            }

            var deadDD = CalendarDingDing(context)
            et_tcsj.setOnClickListener {
//                val timePicker = TimePickerView.Builder(this, { date, view ->
//                    et_tcsj.text = Utils.getYMD(date)
//                    endDate = date
//                    checkDays()
//                })
//                        //年月日时分秒 的显示与否，不设置则默认全部显示
//                        .setType(booleanArrayOf(true, true, true, false, false, false))
//                        .setDividerColor(Color.DKGRAY)
//                        .setContentSize(21)
//                        //.setDate(selectedDate)
//                        .setCancelColor(resources.getColor(R.color.shareholder_text_gray))
//                        .build()
//                timePicker.show()
                deadDD.show(2, Date(), object : CalendarDingDing.onTimeClick {
                    override fun onClick(date: Date?) {
                        if(date != null){
                            et_tcsj.text = Utils.getYMD(date)
                            endDate = date
                            checkDays()
                        }
                    }
                })
            }
        }
    }

    fun checkDays() {
        if (startDate == null || endDate == null) {
            return
        }
        val between = (endDate!!.getTime() - startDate!!.getTime()) / 1000//除以1000是为了转换成秒
        val day1 = between / (24 * 3600)
        //println("" + day1 + "天" + hour1 + "小时" + minute1 + "分" + second1 + "秒")
        et_tzts.text = "${day1}"

        val hour1 = between / 3600
        et_tzsc.text = "${hour1}"
    }

    var mType = 1
    var startDate: Date? = null
    var endDate: Date? = null

    fun upload() {
        var map = HashMap<String, Any>()
        var content = HashMap<String, Any>()

        if (et_cb.text.toString().isNullOrEmpty()) {
            showToast("成本不能为空")
            return
        }
        if (et_tcsr.text.toString().isNullOrEmpty()) {
            showToast("退出收入不能为空")
            return
        }
        if (et_fh.text.toString().isNullOrEmpty()) {
            showToast("分红不能为空")
            return
        }
        if (et_tcsr.text.toString().isNullOrEmpty()) {
            showToast("退出收益不能为空")
            return
        }
        if (et_tzsyl.text.toString().isNullOrEmpty()) {
            showToast("投资收益率不能为空")
            return
        }
        if (et_tzsj.text.toString().isNullOrEmpty()) {
            showToast("投资时间不能为空")
            return
        }
        if (et_tcsj.text.toString().isNullOrEmpty()) {
            showToast("退出时间不能为空")
            return
        }
        if (et_tzts.text.toString().isNullOrEmpty()) {
            showToast("投资天数不能为空")
            return
        }
        if (et_nhsyl.text.toString().isNullOrEmpty()) {
            showToast("年化收益率不能为空")
            return
        }
        if (et_tzsc.text.toString().isNullOrEmpty()) {
            showToast("投资时长不能为空")
            return
        }
        if (et_IRR.text.toString().isNullOrEmpty()) {
            showToast("IRR(内部收益率)不能为空")
            return
        }

        if (mType == 1) {
            if (et_wtccb.text.toString().isNullOrEmpty()) {
                showToast("未退出成本不能为空")
                return
            }
            content.put("company_id", project.company_id!!)
            content.put("type", mType)
            content.put("cost", et_cb.text.toString())
            content.put("income", et_tcsr.text.toString())
            content.put("profit", et_fh.text.toString())
            content.put("outIncome", et_tcsr.text.toString())
            content.put("investRate", et_tzsyl.text.toString())
            content.put("investTime", et_tzsj.text.toString())
            content.put("outTime", et_tcsj.text.toString())
            content.put("days", et_tzts.text.toString())
            content.put("annualRate", et_nhsyl.text.toString())
            content.put("investHour", et_tzsc.text.toString())
            content.put("IRR", et_IRR.text.toString())
            content.put("supply", et_bc.text.toString())
            content.put("surplus", et_wtccb.text.toString())
            content.put("summary", et_tczj.text.toString())
        } else if (mType == 2) {
            if (et_tzzt.text.toString().isNullOrEmpty()) {
                showToast("投资主体不能为空")
                return
            }
            content.put("company_id", project.company_id!!)
            content.put("type", mType)
            content.put("invest", et_tzzt.text.toString())
            content.put("cost", et_cb.text.toString())
            content.put("income", et_tcsr.text.toString())
            content.put("compensation", et_bck.text.toString())
            content.put("profit", et_fh.text.toString())
            content.put("outIncome", et_tcsr.text.toString())
            content.put("investRate", et_tzsyl.text.toString())
            content.put("investTime", et_tzsj.text.toString())
            content.put("outTime", et_tcsj.text.toString())
            content.put("days", et_tzts.text.toString())
            content.put("annualRate", et_nhsyl.text.toString())
            content.put("investHour", et_tzsc.text.toString())
            content.put("IRR", et_IRR.text.toString())
            content.put("summary", et_tczj.text.toString())
        }

        map.put("ae", content)

        //company_id	number		公司ID	非空
        //type	number		类型	非空（1=>部分退出，2=>全部退出）
        //invest	string		投资主体	type=2时非空，type=1时隐藏此字段
        //cost	string		成本	非空
        //income	string		退出收入	非空
        //compensation	string		补偿款	type=2时可空，type=1时隐藏此字段
        //profit	string		分红	非空
        //outIncome	string		退出收益	非空
        //investRate	string		投资收益率	非空
        //investTime	string		投资时间	非空(格式如2018-01-16)
        //outTime	string		退出时间	非空(格式如2018-01-16)
        //days	number		投资天数	非空
        //annualRate	string		年化收益率	非空
        //investHour	string		投资时长	非空
        //IRR	string		IRR	非空
        //supply	string		补充	type=1时可空，type=2隐藏此字段
        //surplus	string		未退出成本	type=1时非空，type=2隐藏此字段
        //summary	string		退出总结	可空

        SoguApi.getService(application)
                .addQuit(map)
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
                    showToast("暂无可用数据")
                })
    }

    fun load() {
        SoguApi.getService(application)
                .quitInfo(project.company_id!!)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ payload ->
                    if (payload.isOk) {
                        var quitBean = payload.payload!!
                        if (quitBean.type == 1) {
                            part_blue.visibility = View.VISIBLE

                            part_quit.backgroundResource = R.drawable.tc_true
                            part_quit.textColor = Color.parseColor("#1787fb")
                            total_quit.backgroundResource = R.drawable.tc_false
                            total_quit.textColor = Color.parseColor("#282828")

//                            tr_wtccb.visibility = View.VISIBLE
//                            tr_bc.visibility = View.GONE
//                            tr_tzzt.visibility = View.GONE
//                            tr_bck.visibility = View.VISIBLE
                            tr_wtccb.visibility = View.VISIBLE
                            tr_bc.visibility = View.VISIBLE
                            tr_tzzt.visibility = View.GONE
                            tr_bck.visibility = View.GONE

                            et_cb.setText(quitBean.cost)
                            et_tcsr.setText(quitBean.income)
                            et_fh.setText(quitBean.profit)
                            //et_tcsr.setText(quitBean.outIncome)
                            et_tcsy.setText(quitBean.outIncome)
                            et_tzsyl.setText(quitBean.investRate)
                            et_tzsj.setText(quitBean.investTime)
                            et_tcsj.setText(quitBean.outTime)
                            et_tzts.setText("${quitBean.days}")
                            et_nhsyl.setText(quitBean.annualRate)
                            et_tzsc.setText(quitBean.investHour)
                            et_IRR.setText(quitBean.IRR)
                            et_bc.setText(quitBean.supply)
                            et_wtccb.setText(quitBean.surplus)
                            et_tczj.setText(quitBean.summary)

                            et_cb.isFocusable = false
                            et_tcsr.isFocusable = false
                            et_fh.isFocusable = false
                            //et_tcsr.isFocusable = false
                            et_tcsy.isFocusable = false
                            et_tzsyl.isFocusable = false
                            et_tzsj.isFocusable = false
                            et_tcsj.isFocusable = false
                            et_tzts.isFocusable = false
                            et_nhsyl.isFocusable = false
                            et_tzsc.isFocusable = false
                            et_IRR.isFocusable = false
                            et_bc.isFocusable = false
                            et_wtccb.isFocusable = false
                            et_tczj.isFocusable = false
                        } else if (quitBean.type == 2) {
                            part_blue.visibility = View.INVISIBLE

                            total_quit.backgroundResource = R.drawable.tc_true
                            total_quit.textColor = Color.parseColor("#1787fb")
                            part_quit.backgroundResource = R.drawable.tc_false
                            part_quit.textColor = Color.parseColor("#282828")

//                            tr_wtccb.visibility = View.GONE
//                            tr_bc.visibility = View.VISIBLE
//                            tr_tzzt.visibility = View.VISIBLE
//                            tr_bck.visibility = View.GONE
                            tr_wtccb.visibility = View.GONE
                            tr_bc.visibility = View.GONE
                            tr_tzzt.visibility = View.VISIBLE
                            tr_bck.visibility = View.VISIBLE

                            et_tzzt.setText(quitBean.invest)
                            et_cb.setText(quitBean.cost)
                            et_tcsr.setText(quitBean.income)
                            et_bck.setText(quitBean.compensation)
                            et_fh.setText(quitBean.profit)
                            //et_tcsr.setText(quitBean.outIncome)
                            et_tcsy.setText(quitBean.outIncome)
                            et_tzsyl.setText(quitBean.investRate)
                            et_tzsj.setText(quitBean.investTime)
                            et_tcsj.setText(quitBean.outTime)
                            et_tzts.setText("${quitBean.days}")
                            et_nhsyl.setText(quitBean.annualRate)
                            et_tzsc.setText(quitBean.investHour)
                            et_IRR.setText(quitBean.IRR)
                            et_tczj.setText(quitBean.summary)

                            et_tzzt.isFocusable = false
                            et_cb.isFocusable = false
                            et_tcsr.isFocusable = false
                            et_bck.isFocusable = false
                            et_fh.isFocusable = false
                            //et_tcsr.isFocusable = false
                            et_tcsy.isFocusable = false
                            et_tzsyl.isFocusable = false
                            et_tzsj.isFocusable = false
                            et_tcsj.isFocusable = false
                            et_tzts.isFocusable = false
                            et_nhsyl.isFocusable = false
                            et_tzsc.isFocusable = false
                            et_IRR.isFocusable = false
                            et_tczj.isFocusable = false
                        }
                    } else
                        showToast(payload.message)
                }, { e ->
                    Trace.e(e)
                    showToast("暂无可用数据")
                })
    }
}
