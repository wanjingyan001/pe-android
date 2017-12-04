package com.sogukj.pe.ui.user

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.Theme
import com.bigkoo.pickerview.TimePickerView
import com.framework.base.BaseActivity
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.bean.Industry
import com.sogukj.pe.util.Utils
import kotlinx.android.synthetic.main.activity_user_resume.*
import kotlinx.android.synthetic.main.activity_work_expericence_add.*
import kotlinx.android.synthetic.main.layout_shareholder_toolbar.*
import java.text.SimpleDateFormat
import java.util.*

class WorkExpericenceAddActivity : BaseActivity(), View.OnClickListener {

    companion object {
        fun start(ctx: Activity?) {
            val intent = Intent(ctx, WorkExpericenceAddActivity::class.java)
            ctx?.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_work_expericence_add)
        Utils.setWindowStatusBarColor(this, R.color.white)
        toolbar_title.text = "添加工作经历"
        addTv.text = "保存"
        addTv.setOnClickListener(this)
        tv_start_date.setOnClickListener(this)
        tv_date_end.setOnClickListener(this)
        tv_industry.setOnClickListener(this)
        tv_workers.setOnClickListener(this)
        tv_nature.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        val selectedDate = Calendar.getInstance()//系统当前时间
        val startDate = Calendar.getInstance()
        startDate.set(1949, 10, 1)
        val endDate = Calendar.getInstance()
        endDate.set(selectedDate.get(Calendar.YEAR), selectedDate.get(Calendar.MONTH) + 1, selectedDate.get(Calendar.DAY_OF_MONTH))
        when (v?.id) {
            R.id.addTv -> {
                //保存
            }
            R.id.tv_start_date -> {
                //入职时间
                val timePicker = TimePickerView.Builder(this, { date, view ->
                    tv_start_date.text = getTime(date)
                })
                        //年月日时分秒 的显示与否，不设置则默认全部显示
                        .setType(booleanArrayOf(true, true, true, false, false, false))
                        .setDividerColor(Color.DKGRAY)
                        .setContentSize(21)
                        .setDate(selectedDate)
                        .setCancelColor(resources.getColor(R.color.shareholder_text_gray))
                        .setRangDate(startDate, endDate)
                        .build()
                timePicker.show()
            }
            R.id.tv_date_end -> {
                //离职时间
                val timePicker = TimePickerView.Builder(this, { date, view ->
                    tv_date_end.text = getTime(date)
                })
                        //年月日时分秒 的显示与否，不设置则默认全部显示
                        .setType(booleanArrayOf(true, true, true, false, false, false))
                        .setDividerColor(Color.DKGRAY)
                        .setContentSize(21)
                        .setDate(selectedDate)
                        .setCancelColor(resources.getColor(R.color.shareholder_text_gray))
                        .setRangDate(startDate, endDate)
                        .build()
                timePicker.show()
            }
            R.id.tv_industry -> {
                //行业
                IndustryActivity.start(this)
            }
            R.id.tv_workers -> {
                //公司规模
                MaterialDialog.Builder(this)
                        .title("选择公司规模")
                        .theme(Theme.LIGHT)
                        .items(resources.getStringArray(R.array.workers).toList())
                        .itemsCallbackSingleChoice(0) { dialog, itemView, which, text ->
                            tv_workers.text = text
                            true
                        }
                        .positiveText("确定")
                        .negativeText("取消")
                        .show()
            }
            R.id.tv_nature -> {
                //公司性质
                MaterialDialog.Builder(this)
                        .title("选择公司性质")
                        .theme(Theme.LIGHT)
                        .items(resources.getStringArray(R.array.BusinessNature).toList())
                        .itemsCallbackSingleChoice(0) { dialog, itemView, which, text ->
                            tv_nature.text = text
                            true
                        }
                        .positiveText("确定")
                        .negativeText("取消")
                        .show()
            }
        }
    }


    @SuppressLint("SimpleDateFormat")
    private fun getTime(date: Date): String {//可根据需要自行截取数据显示
        val format = SimpleDateFormat("yyyy/MM")
        return format.format(date)
    }


    override fun startActivityForResult(intent: Intent?, requestCode: Int) {
        super.startActivityForResult(intent, requestCode)
        if (requestCode == Extras.REQUESTCODE && intent != null) {
            val industry = intent.getSerializableExtra(Extras.DATA) as Industry.Children?
            industry?.let {
                tv_industry.text = it.name
            }
        }
    }
}
