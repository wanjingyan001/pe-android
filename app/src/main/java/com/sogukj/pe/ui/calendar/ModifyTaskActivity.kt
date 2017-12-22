package com.sogukj.pe.ui.calendar

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.support.v7.widget.GridLayoutManager
import android.util.Log
import android.view.MenuItem
import android.view.View
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.Theme
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
import kotlinx.android.synthetic.main.activity_modify_task.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.properties.Delegates

/**
 * 添加/修改任务界面
 */
class ModifyTaskActivity : ToolbarActivity(), View.OnClickListener, AddPersonListener {
    var type: Long by Delegates.notNull()
    var name: String? = null
    var selectT: Int by Delegates.notNull()
    var data_id: Int? = null
    var companyId: Int? = null
    var time: Long? = null
    lateinit var adapter: CcPersonAdapter
    lateinit var exAdapter: ExecutiveAdapter
    val data = ArrayList<UserBean>()
    val data2 = ArrayList<UserBean>()
    override val menuId: Int
        get() = R.menu.modify_submit


    companion object {
        const val CREATE = 1L
        const val MODIFY = 2L
        const val Schedule = "Schedule"
        const val Task = "Task"
        fun startForCreate(ctx: Activity?, name: String) {
            val intent = Intent(ctx, ModifyTaskActivity::class.java)
            intent.putExtra(Extras.NAME, name)
            intent.putExtra(Extras.TYPE, CREATE)
            ctx?.startActivity(intent)
        }

        fun startForModify(ctx: Activity?, data_id: Int, name: String) {
            val intent = Intent(ctx, ModifyTaskActivity::class.java)
            intent.putExtra(Extras.TYPE, MODIFY)
            intent.putExtra(Extras.NAME, name)
            intent.putExtra(Extras.DATA, data_id)
            ctx?.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_modify_task)
        setBack(true)
        adapter = CcPersonAdapter(context, data)
        adapter.setListener(this)
        copyList.layoutManager = GridLayoutManager(this, 6)
        copyList.adapter = adapter

        exAdapter = ExecutiveAdapter(context, data2)
        exAdapter.setListener(this)
        executiveList.layoutManager = GridLayoutManager(context, 6)
        executiveList.adapter = exAdapter
        type = intent.getLongExtra(Extras.TYPE, -1)
        name = intent.getStringExtra(Extras.NAME)
        when (type) {
            CREATE -> {
                when (name) {
                    Task -> {
                        executiveLayout.visibility = View.VISIBLE
                        copyLayout.visibility = View.VISIBLE
                        line.visibility = View.VISIBLE
                        selectTypeLayout.visibility = View.GONE
                        selectT = 1
                        title = "添加任务"
                        missionTV.text = "任务描述"
                    }
                    Schedule -> {
                        executiveLayout.visibility = View.GONE
                        copyLayout.visibility = View.GONE
                        line.visibility = View.GONE
                        selectTypeLayout.visibility = View.GONE
                        selectT = 0
                        title = "添加日程"
                        missionTV.text = "日程描述"
                    }
                    else -> {
                        title = "添加"
                        selectTypeLayout.visibility = View.VISIBLE
                    }
                }
            }
            MODIFY -> {
                when (name) {
                    Task -> {
                        executiveLayout.visibility = View.VISIBLE
                        copyLayout.visibility = View.VISIBLE
                        line.visibility = View.VISIBLE
                        selectTypeLayout.visibility = View.GONE
                        selectT = 1
                        title = "修改任务"
                        missionTV.text = "任务描述"
                    }
                    Schedule -> {
                        executiveLayout.visibility = View.GONE
                        copyLayout.visibility = View.GONE
                        line.visibility = View.GONE
                        selectTypeLayout.visibility = View.GONE
                        selectT = 0
                        title = "修改日程"
                        missionTV.text = "日程描述"
                    }
                    else -> {
                        title = "修改"
                        selectTypeLayout.visibility = View.VISIBLE
                    }
                }
                data_id = intent.getIntExtra(Extras.DATA, -1)
                doRequest()
            }
        }
        selectType.setOnClickListener(this)
        relatedProject.setOnClickListener(this)
        startTime.setOnClickListener(this)
        deadline.setOnClickListener(this)
        remind.setOnClickListener(this)
    }

    fun doRequest() {
        data_id?.let {
            SoguApi.getService(application)
                    .showEditTask(it)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeOn(Schedulers.io())
                    .subscribe({ payload ->
                        if (payload.isOk) {
                            payload.payload?.let {
                                companyId = it.company_id
                                relatedProject.text = it.cName
                                missionDetails.setText(it.info)
                                start = SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(it.start_time)
                                endTime = SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(it.end_time)
                                startTime.text = Utils.getTime(start, "MM月dd日 E HH:mm")
                                deadline.text = Utils.getTime(endTime, "MM月dd日 E HH:mm")
                                remind.text = "截止前${(it.clock)?.div(60)}分钟"
                                it.executor?.forEach {
                                    val bean = UserBean()
                                    bean.uid = it.id
                                    bean.name = it.name
                                    bean.url = it.url
                                    exAdapter.addData(bean)
                                }
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
                    })
        }
    }


    private fun submitChange() {
        if (missionDetails.text.isEmpty()) {
            showToast("请填写日程描述")
            return
        }
        if (start == null || endTime == null) {
            showToast("请选择时间")
            return
        }
        if (start!!.time - endTime!!.time > 0) {
            showToast("开始时间不能大于结束时间")
            return
        }
        val reqBean = TaskModifyBean()
        val bean = reqBean.ae
        reqBean.data_id = data_id
        reqBean.type = selectT
        bean.info = missionDetails.text.toString()
        if (companyBean != null && companyBean?.id != null) {
            bean.company_id = companyBean?.id
        } else {
            bean.company_id = companyId
        }
        bean.start_time = Utils.getTime(start, "yyyy-MM-dd HH:mm:ss")
        bean.end_time = Utils.getTime(endTime, "yyyy-MM-dd HH:mm:ss")
        time?.let {
            bean.clock = (it / 1000).toInt()
        }
        val exusers = StringBuilder()
        if (data2.isNotEmpty()) {
            data2.forEach {
                if (it.user_id != null) {
                    exusers.append(it.user_id.toString() + ",")
                }
            }
        }
        if (exusers.isNotEmpty()) {
            bean.executor = exusers.toString().substring(0, exusers.length - 1)
        }
        val watchusers = StringBuilder()
        if (data.isNotEmpty()) {
            data.forEach {
                if (it.user_id != null) {
                    watchusers.append(it.user_id.toString() + ",")
                }
            }
        }
        if (watchusers.isNotEmpty()) {
            bean.watcher = watchusers.toString().substring(0, watchusers.length - 1)
        }
        Log.d("WJY", Gson().toJson(reqBean))
        SoguApi.getService(application)
                .aeCalendarInfo(reqBean)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ payload ->
                    if (payload.isOk) {
                        Utils.closeInput(this, missionDetails)
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
            R.id.selectType -> {
                MaterialDialog.Builder(this)
                        .theme(Theme.LIGHT)
                        .title("请选择类型")
                        .items(arrayListOf("日程", "任务"))
                        .itemsCallbackSingleChoice(-1) { dialog, itemView, which, text ->
                            if (text == "日程") {
                                selectT = 0
                                executiveLayout.visibility = View.GONE
                                copyLayout.visibility = View.GONE
                                line.visibility = View.GONE
                            } else if (text == "任务") {
                                selectT = 1
                                executiveLayout.visibility = View.VISIBLE
                                copyLayout.visibility = View.VISIBLE
                                line.visibility = View.VISIBLE
                            }
                            selectType.text = text
                            true
                        }
                        .positiveText("确定")
                        .negativeText("取消")
                        .show()
            }
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
                    time?.let {
                        if (it < 0) {
                            showToast("提醒时间不能大于开始时间")
                        } else {
                            remind.text = "开始前${it / 1000 / 60}分钟"
                        }
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

    var companyBean: CustomSealBean.ValueBean? = null
    var selectUser: Users? = null
    var selectUser2: Users? = null
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == Extras.REQUESTCODE && data != null) {
            when (resultCode) {
                Extras.RESULTCODE -> {
                    selectUser = data.getSerializableExtra(Extras.DATA) as Users
                    selectUser?.selectUsers.let {
                        adapter.addAllData(it)
                    }
                }
                Extras.RESULTCODE2 -> {
                    selectUser2 = data.getSerializableExtra(Extras.DATA) as Users
                    selectUser2?.selectUsers.let {
                        exAdapter.addAllData(it)
                    }
                }
                Activity.RESULT_OK -> {
                    companyBean = data.getSerializableExtra(Extras.DATA) as CustomSealBean.ValueBean
                    companyBean?.let {
                        relatedProject.text = it.name
                    }
                }
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


    override fun addPerson(tag: String) {
        when (tag) {
            "CcPersonAdapter" -> SelectUserActivity.startForResult(this, tag, selectUser)
            "ExecutiveAdapter" -> SelectUserActivity.startForResult(this, tag, selectUser2)
        }
    }
}
