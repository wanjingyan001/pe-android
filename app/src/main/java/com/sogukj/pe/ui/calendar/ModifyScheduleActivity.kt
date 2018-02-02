package com.sogukj.pe.ui.calendar

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import com.bigkoo.pickerview.TimePickerView
import com.framework.base.ToolbarActivity
import com.google.gson.Gson
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.bean.CustomSealBean
import com.sogukj.pe.bean.UserBean
import com.sogukj.pe.util.Trace
import com.sogukj.pe.util.Utils
import com.sogukj.service.SoguApi
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_modify_schedule.*
import org.jetbrains.anko.find
import java.text.SimpleDateFormat
import java.util.*
import kotlin.properties.Delegates

class ModifyScheduleActivity : ToolbarActivity(), AddPersonListener, View.OnClickListener {


    var type: Long by Delegates.notNull()
    var data_id = 0
    var companyId: Int? = null
    var time: Long by Delegates.notNull()
    lateinit var adapter: CcPersonAdapter
    val data = ArrayList<UserBean>()

    companion object {
        const val CREATE = 1L
        const val MODIFY = 2L
        fun start(ctx: Activity?) {
            val intent = Intent(ctx, ModifyScheduleActivity::class.java)
            intent.putExtra(Extras.TYPE, CREATE)
            ctx?.startActivity(intent)
        }

        fun start(ctx: Activity?, data_id: Int) {
            val intent = Intent(ctx, ModifyScheduleActivity::class.java)
            intent.putExtra(Extras.TYPE, MODIFY)
            intent.putExtra(Extras.DATA, data_id)
            ctx?.startActivity(intent)
        }
    }

    override val menuId: Int
        get() = R.menu.modify_submit

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_modify_schedule)
        setBack(true)
        adapter = CcPersonAdapter(context, data)
        adapter.setListener(this)
//        copyList.layoutManager = GridLayoutManager(context, 6)
//        copyList.adapter = adapter
        type = intent.getLongExtra(Extras.TYPE, -1)
        if (type == CREATE) {
            title = "添加日程"
        } else {
            title = "修改日程"
            data_id = intent.getIntExtra(Extras.DATA, -1)
            doRequest()
        }
        relatedProject.setOnClickListener(this)
        deadline.setOnClickListener(this)
        startTime.setOnClickListener(this)
        remind.setOnClickListener(this)
    }

    fun doRequest() {
        if (data_id == 0) {
            return
        }
        SoguApi.getService(application)
                .showEditTask(data_id)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ payload ->
                    if (payload.isOk) {
                        payload.payload?.let {
                            Log.d("WJY", Gson().toJson(payload.payload));
                            companyId = it.company_id
                            relatedProject.text = it.cName
                            missionDetails.setText(it.info)
                            startTime.text = Utils.getTime(SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(it.start_time), "MM月dd日 E HH:mm")
                            deadline.text = Utils.getTime(SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(it.end_time), "MM月dd日 E HH:mm")
                            Log.d("WJY", "开始前:" + it.clock)
                            remind.text = "开始前${(it.clock)?.div(60)}分钟"
                            it.watcher?.forEach {
                                val bean = UserBean()
                                bean.uid = it.id
                                bean.name = it.name
                                bean.url = it.url
                                adapter.addData(bean)
                            }
                        }
                    } else {
                        showToast(payload.message)
                    }
                }, { e ->
                    Trace.e(e)
                })
    }

    private fun submitChange() {
        if (missionDetails.text.isEmpty()) {
            showToast("请填写日程描述")
            return
        }
        if (startTime.text.isEmpty() || deadline.text.isEmpty()) {
            showToast("请选择时间")
            return
        }
        if (start!!.time - endTime!!.time > 0) {
            showToast("开始时间不能大于结束时间")
            return
        }
        val reqBean = TaskModifyBean()
        val bean = reqBean.ae
        if (data_id != 0) {
            reqBean.data_id = data_id
        }
        reqBean.type = 1
        bean.info = missionDetails.text.toString()
        bean.start_time = Utils.getTime(start, "yyyy-MM-dd HH:mm:ss")
        bean.end_time = Utils.getTime(endTime, "yyyy-MM-dd HH:mm:ss")
        if (companyBean.id != null) {
            bean.company_id = companyBean.id
        } else {
            bean.company_id = companyId
        }
        bean.clock = (time / 1000).toInt()
        val watchusers = StringBuilder()
        data.forEach {
            watchusers.append(it.user_id.toString() + ",")
        }
        bean.watcher = watchusers.toString()
        Log.d("WJY", Gson().toJson(reqBean))
        SoguApi.getService(application)
                .aeCalendarInfo(reqBean)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ payload ->
                    if (payload.isOk) {
                        showToast("提交成功")
                        finish()
                    } else {
                        showToast(payload.message)
                    }
                }, { e ->
                    Trace.e(e)
                }, {}, {
                    showProgress("正在提交")
                })

    }

    var start: Date? = null
    var endTime: Date? = null
    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.relatedProject -> {
                CompanySelectActivity.start(this)
            }
            R.id.startTime -> {
                Utils.closeInput(context, missionDetails)
                val selectedDate = Calendar.getInstance()//系统当前时间
                val startDate = Calendar.getInstance()
                startDate.set(1949, 10, 1)
                val endDate = Calendar.getInstance()
                endDate.set(2020, 12, 31)
                val timePicker = TimePickerView.Builder(this, { date, view ->
                    start = date
                    startTime.text = Utils.getTime(date, "MM月dd日 E HH:mm")
                })
                        //年月日时分秒 的显示与否，不设置则默认全部显示
                        .setType(booleanArrayOf(true, true, true, true, true, false))
                        .setDividerColor(Color.DKGRAY)
                        .setContentSize(21)
                        .setDate(selectedDate)
                        .setCancelColor(resources.getColor(R.color.shareholder_text_gray))
                        .setRangDate(startDate, endDate)
                        .build()
                timePicker.show()
            }
            R.id.deadline -> {
                Utils.closeInput(context, missionDetails)
                val selectedDate = Calendar.getInstance()//系统当前时间
                val startDate = Calendar.getInstance()
                startDate.set(1949, 10, 1)
                val endDate = Calendar.getInstance()
                endDate.set(2020, 12, 31)
                val timePicker = TimePickerView.Builder(this, { date, view ->
                    endTime = date
                    deadline.text = Utils.getTime(date, "MM月dd日 E HH:mm")
                })
                        //年月日时分秒 的显示与否，不设置则默认全部显示
                        .setType(booleanArrayOf(true, true, true, true, true, false))
                        .setDividerColor(Color.DKGRAY)
                        .setContentSize(21)
                        .setDate(selectedDate)
                        .setCancelColor(resources.getColor(R.color.shareholder_text_gray))
                        .setRangDate(startDate, endDate)
                        .build()
                timePicker.show()

            }
            R.id.remind -> {
                //截止时间要在结束时间之前
                if (start == null) {
                    showToast("请选择开始时间")
                    return
                }
                if (endTime == null) {
                    showToast("请选择结束时间")
                    return
                }
                Utils.closeInput(context, missionDetails)
                val selectedDate = Calendar.getInstance()//系统当前时间
                val startDate = Calendar.getInstance()
                startDate.set(
                        selectedDate.get(Calendar.YEAR),
                        selectedDate.get(Calendar.MONTH) - 1,
                        selectedDate.get(Calendar.DAY_OF_MONTH),
                        selectedDate.get(Calendar.HOUR_OF_DAY),
                        selectedDate.get(Calendar.MINUTE)
                )
                val endDate = Calendar.getInstance()
                endDate.set(Utils.getTime(start, "yyyy").toInt(),
                        Utils.getTime(start, "MM").toInt() - 1,
                        Utils.getTime(start, "dd").toInt(),
                        Utils.getTime(start, "HH").toInt(),
                        Utils.getTime(start, "mm").toInt())
                val timePicker = TimePickerView.Builder(this, { date, view ->
                    time = start!!.time - date.time
                    if (time < 0) {
                        showToast("提醒时间不能大于开始时间")
                    } else {
                        remind.text = "开始前${time / 1000 / 60}分钟"
                    }
                })
                        //年月日时分秒 的显示与否，不设置则默认全部显示
                        .setType(booleanArrayOf(true, true, true, true, true, false))
                        .setDividerColor(Color.DKGRAY)
                        .setContentSize(21)
                        .setDate(selectedDate)
                        .setCancelColor(resources.getColor(R.color.shareholder_text_gray))
                        .setRangDate(startDate, endDate)
                        .build()
                timePicker.show()
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.modify_submit -> {
                submitChange()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    lateinit var companyBean: CustomSealBean.ValueBean
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == Extras.REQUESTCODE && data != null) {
            if (resultCode == Extras.RESULTCODE) {
                val userBean = data.getSerializableExtra(Extras.DATA) as UserBean
                adapter.addData(userBean)
            } else if (resultCode == Activity.RESULT_OK) {
                companyBean = data.getSerializableExtra(Extras.DATA) as CustomSealBean.ValueBean
                relatedProject.text = companyBean.name
            }
        }
    }


    override fun onPause() {
        super.onPause()
        Utils.closeInput(this, find(R.id.modify_schedule_main))
    }

    override fun addPerson(tag: String) {
    }
    override fun remove(tag: String, user: UserBean) {

    }
}
