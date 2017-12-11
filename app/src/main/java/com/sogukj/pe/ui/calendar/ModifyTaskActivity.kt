package com.sogukj.pe.ui.calendar

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.support.v7.widget.GridLayoutManager
import android.view.MenuItem
import android.view.View
import com.bigkoo.pickerview.TimePickerView
import com.framework.base.ToolbarActivity
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.bean.UserBean
import com.sogukj.pe.ui.user.OrganizationActivity
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
    var data_id: Int by Delegates.notNull()
    var companyId: Int? = null
    var time: Int? = null
    lateinit var adapter: CcPersonAdapter
    lateinit var exAdapter: ExecutiveAdapter
    val data = ArrayList<UserBean>()
    val data2 = ArrayList<UserBean>()
    override val menuId: Int
        get() = R.menu.modify_submit


    companion object {
        const val CREATE = 1L
        const val MODIFY = 2L
        fun start(ctx: Activity?) {
            val intent = Intent(ctx, ModifyTaskActivity::class.java)
            intent.putExtra(Extras.TYPE, CREATE)
            ctx?.startActivity(intent)
        }

        fun start(ctx: Activity?, data_id: Int) {
            val intent = Intent(ctx, ModifyTaskActivity::class.java)
            intent.putExtra(Extras.TYPE, MODIFY)
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
        copyList.layoutManager = GridLayoutManager(context, 6)
        copyList.adapter = adapter

        exAdapter = ExecutiveAdapter(context, data2)
        exAdapter.setListener(this)
        executiveList.layoutManager = GridLayoutManager(context, 6)
        executiveList.adapter = exAdapter
        type = intent.getLongExtra(Extras.TYPE, -1)
        if (type == CREATE) {
            title = "创建任务"
        } else {
            title = "修改任务"
            data_id = intent.getIntExtra(Extras.DATA, -1)
            doRequest()
        }
        startTime.setOnClickListener(this)
        deadline.setOnClickListener(this)
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
                            companyId = it.company_id
                            associatedEdt.setText(it.cName)
                            missionDetails.setText(it.info)
                            startTime.text = it.start_time
                            deadline.text = it.end_time

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


    private fun submitChange() {
        val reqBean = TaskModifyBean()
        val bean = reqBean.ae
        reqBean.data_id = data_id
        reqBean.type = 1
        bean.info = missionDetails.text.toString()
        val parse = SimpleDateFormat("MM月dd日 E HH:mm").parse(deadline.text.toString())
        bean.end_time = Utils.getTime(parse, "yyyy-MM-dd HH:mm:ss")
        bean.company_id = companyId
        val parse2 = SimpleDateFormat("MM月dd日 E HH:mm").parse(startTime.text.toString())
        bean.start_time = Utils.getTime(parse2, "yyyy-MM-dd HH:mm:ss")
        bean.clock = time?.times(60)
        val exusers = StringBuilder()
        data2.forEach {
            exusers.append(it.user_id.toString() + ",")
        }
        bean.executor = exusers.toString()
        val watchusers = StringBuilder()
        data.forEach {
            watchusers.append(it.user_id.toString() + ",")
        }
        bean.watcher = watchusers.toString()
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


    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.startTime -> {
                val selectedDate = Calendar.getInstance()//系统当前时间
                val startDate = Calendar.getInstance()
                startDate.set(1949, 10, 1)
                val endDate = Calendar.getInstance()
                endDate.set(2020, 12, 31)
                val timePicker = TimePickerView.Builder(this, { date, view ->
                    startTime.text = Utils.getTime(date, "MM月dd日 E HH:mm")
                })
                        //年月日时分秒 的显示与否，不设置则默认全部显示
                        .setType(booleanArrayOf(false, true, true, true, true, false))
                        .setDividerColor(Color.DKGRAY)
                        .setContentSize(21)
                        .setDate(selectedDate)
                        .setCancelColor(resources.getColor(R.color.shareholder_text_gray))
                        .setRangDate(startDate, endDate)
                        .build()
                timePicker.show()
            }
            R.id.deadline -> {
                val selectedDate = Calendar.getInstance()//系统当前时间
                val startDate = Calendar.getInstance()
                startDate.set(1949, 10, 1)
                val endDate = Calendar.getInstance()
                endDate.set(2020, 12, 31)
                val timePicker = TimePickerView.Builder(this, { date, view ->
                    deadline.text = Utils.getTime(date, "MM月dd日 E HH:mm")
                })
                        //年月日时分秒 的显示与否，不设置则默认全部显示
                        .setType(booleanArrayOf(false, true, true, true, true, false))
                        .setDividerColor(Color.DKGRAY)
                        .setContentSize(21)
                        .setDate(selectedDate)
                        .setCancelColor(resources.getColor(R.color.shareholder_text_gray))
                        .setRangDate(startDate, endDate)
                        .build()
                timePicker.show()

            }
            R.id.remind -> {
                val selectedDate = Calendar.getInstance()//系统当前时间
                val startDate = Calendar.getInstance()
                startDate.set(selectedDate.get(Calendar.YEAR), selectedDate.get(Calendar.MONTH) + 1, selectedDate.get(Calendar.DAY_OF_MONTH))
                val endDate = Calendar.getInstance()
                endDate.set(selectedDate.get(Calendar.YEAR), selectedDate.get(Calendar.MONTH) + 1, selectedDate.get(Calendar.DAY_OF_MONTH))
                val timePicker = TimePickerView.Builder(this, { date, view ->
                    val hour = Utils.getTime(date, "HH")
                    val minute = Utils.getTime(date, "mm")
                    time = hour.toInt() * 60 + minute.toInt()
                    remind.text = "截止前${time}分钟"
                })
                        //年月日时分秒 的显示与否，不设置则默认全部显示
                        .setType(booleanArrayOf(false, false, false, true, true, false))
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == Extras.REQUESTCODE && data != null) {
            val userBean = data.getSerializableExtra(Extras.DATA) as UserBean
            if (resultCode == Extras.RESULTCODE) {
                adapter.addData(userBean)
            } else {
                exAdapter.addData(userBean)
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
        OrganizationActivity.startForResult(this@ModifyTaskActivity, tag)
    }
}
