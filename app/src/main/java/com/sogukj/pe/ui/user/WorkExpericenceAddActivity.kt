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
import com.sogukj.pe.bean.WorkEducationBean
import com.sogukj.pe.bean.WorkReqBean
import com.sogukj.pe.util.Trace
import com.sogukj.pe.util.Utils
import com.sogukj.service.SoguApi
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_user_resume.*
import kotlinx.android.synthetic.main.activity_work_expericence_add.*
import kotlinx.android.synthetic.main.layout_shareholder_toolbar.*
import java.text.SimpleDateFormat
import java.util.*

class WorkExpericenceAddActivity : BaseActivity(), View.OnClickListener {

    companion object {
        fun start(ctx: Activity?) {
            val intent = Intent(ctx, WorkExpericenceAddActivity::class.java)
            ctx?.startActivityForResult(intent, Extras.REQUESTCODE)
        }

        fun start(ctx: Activity?, workeducation: WorkEducationBean) {
            val intent = Intent(ctx, WorkExpericenceAddActivity::class.java)
            intent.putExtra(Extras.DATA, workeducation)
            ctx?.startActivityForResult(intent, Extras.REQUESTCODE)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_work_expericence_add)
        Utils.setWindowStatusBarColor(this, R.color.white)
        val workEducationBean = intent.getParcelableExtra<WorkEducationBean?>(Extras.DATA)
        workEducationBean?.let {
            setData(it)
        }
        if (workEducationBean==null){
            toolbar_title.text = "添加工作经历"
        }else{
            toolbar_title.text = "修改工作经历"
        }
        addTv.text = "保存"
        addTv.setOnClickListener(this)
        back.setOnClickListener(this)
        tv_start_date.setOnClickListener(this)
        tv_date_end.setOnClickListener(this)
        tv_industry.setOnClickListener(this)
        tv_workers.setOnClickListener(this)
        tv_nature.setOnClickListener(this)
    }

    fun setData(workEducationBean: WorkEducationBean) {
        tv_start_date.text = workEducationBean.employDate
        tv_date_end.text = workEducationBean.leaveDate
        tv_company.setText(workEducationBean.company)
        tv_skill.setText(workEducationBean.responsibility)
        tv_desc.setText(workEducationBean.jobInfo)
        tv_industry.text = workEducationBean.trade_name
        tv_depart.setText(workEducationBean.department)
        tv_workers.text = workEducationBean.companyScale
        tv_nature.text = workEducationBean.companyProperty
    }

    override fun onClick(v: View?) {
        val selectedDate = Calendar.getInstance()//系统当前时间
        val startDate = Calendar.getInstance()
        startDate.set(1949, 10, 1)
        val endDate = Calendar.getInstance()
        endDate.set(selectedDate.get(Calendar.YEAR), selectedDate.get(Calendar.MONTH) + 1, selectedDate.get(Calendar.DAY_OF_MONTH))
        when (v?.id) {
            R.id.back -> {
                onBackPressed()
            }
            R.id.addTv -> {
                //保存
                if (!tv_start_date.text.isNotEmpty()) {
                    showToast("请选择入职时间")
                    return
                }
                if (!tv_date_end.text.isNotEmpty()) {
                    showToast("请选择离职时间")
                    return
                }
                if (!tv_company.text.isNotEmpty()) {
                    showToast("请填写公司名称")
                    return
                }
                if (!tv_skill.text.isNotEmpty()) {
                    showToast("请填写职能")
                    return
                }
                if (!tv_desc.text.isNotEmpty()) {
                    showToast("请填写工作描述")
                    return
                }
                if (!tv_industry.text.isNotEmpty()) {
                    showToast("请选择行业")
                    return
                }
                if (!tv_depart.text.isNotEmpty()) {
                    showToast("请填写部门")
                    return
                }
                if (!tv_workers.text.isNotEmpty()) {
                    showToast("请选择公司规模")
                    return
                }
                if (!tv_nature.text.isNotEmpty()) {
                    showToast("请选择公司性质")
                    return
                }
                val reqBean = WorkReqBean()
                val workeducation = WorkEducationBean()
                workeducation.employDate = tv_start_date.text.toString()
                workeducation.leaveDate = tv_date_end.text.toString()
                workeducation.company = tv_company.text.toString()
                workeducation.responsibility = tv_skill.text.toString()
                workeducation.jobInfo = tv_desc.text.toString()
                workeducation.trade = industry.id!!
                workeducation.department = tv_depart.text.toString()
                workeducation.companyScale = tv_workers.text.toString()
                workeducation.companyProperty = tv_nature.text.toString()
                reqBean.ae = workeducation
                reqBean.type = 2
                doRequest(reqBean)
            }
            R.id.tv_start_date -> {
                //入职时间
                val timePicker = TimePickerView.Builder(this, { date, view ->
                    tv_start_date.text = Utils.getTime(date)
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
                    tv_date_end.text = Utils.getTime(date)
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


    fun doRequest(reqBean: WorkReqBean) {
        SoguApi.getService(application)
                .addWorkExperience(reqBean)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ payload ->
                    if (payload.isOk) {
                        payload.payload?.apply {
                            val intent = Intent()
                            intent.putExtra(Extras.LIST, this)
                            setResult(Extras.RESULTCODE, intent)
                            finish()
                        }
                    } else {
                        showToast(payload.message)
                    }
                }, { e ->
                    Trace.e(e)
                }, {}, {
                    showProgress("正在保存,请稍后")
                })
    }

    lateinit var industry: Industry.Children

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == Extras.REQUESTCODE && resultCode == Extras.RESULTCODE && data != null) {
            industry = data.getSerializableExtra(Extras.DATA) as Industry.Children
            industry.let {
                tv_industry.text = it.name
            }
        }
    }
}
