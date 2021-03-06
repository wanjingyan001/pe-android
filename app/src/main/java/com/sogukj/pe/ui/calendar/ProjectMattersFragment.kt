package com.sogukj.pe.ui.calendar


import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.Gravity
import android.view.View
import com.framework.base.BaseFragment
import com.google.gson.Gson
import com.lcodecore.tkrefreshlayout.RefreshListenerAdapter
import com.lcodecore.tkrefreshlayout.TwinklingRefreshLayout
import com.lcodecore.tkrefreshlayout.footer.BallPulseView
import com.lcodecore.tkrefreshlayout.header.progresslayout.ProgressLayout
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.bean.CustomSealBean
import com.sogukj.pe.bean.ProjectMatterCompany
import com.sogukj.pe.bean.ScheduleBean
import com.sogukj.pe.ui.approve.SealApproveActivity
import com.sogukj.pe.ui.approve.SignApproveActivity
import com.sogukj.pe.ui.project.ProjectActivity
import com.sogukj.pe.ui.project.RecordTraceActivity
import com.sogukj.pe.util.Trace
import com.sogukj.pe.util.Utils
import com.sogukj.service.SoguApi
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_project_matters.*
import kotlinx.android.synthetic.main.item_project_matters_list.*
import kotlinx.android.synthetic.main.layout_empty.*
import org.jetbrains.anko.support.v4.find
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap


/**
 * A simple [Fragment] subclass.
 * Use the [ProjectMattersFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ProjectMattersFragment : BaseFragment(), ScheduleItemClickListener {


    override val containerViewId: Int
        get() = R.layout.fragment_project_matters
    private lateinit var projectAdapter: ProjectAdapter
    private val data = ArrayList<Any>()
    private lateinit var window: CalendarWindow
    var page = 1
    var date = SimpleDateFormat("yyyy-MM-dd").format(Date(System.currentTimeMillis()))
    var companyId: String? = null

    private var mParam1: String? = null
    private var mParam2: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (arguments != null) {
            mParam1 = arguments.getString(ARG_PARAM1)
            mParam2 = arguments.getString(ARG_PARAM2)
        }
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        projectAdapter = ProjectAdapter(context, data)
        projectAdapter.setItemClickListener(this)
        projectList.layoutManager = LinearLayoutManager(context)
        projectList.adapter = projectAdapter
        val header = ProgressLayout(context)
        header.setColorSchemeColors(ContextCompat.getColor(context, R.color.color_main))
        refresh.setHeaderView(header)
        val footer = BallPulseView(context)
        footer.setAnimatingColor(ContextCompat.getColor(context, R.color.color_main))
        refresh.setBottomView(footer)
        refresh.setOverScrollRefreshShow(false)
        refresh.setEnableLoadmore(true)
        refresh.setOnRefreshListener(object : RefreshListenerAdapter() {
            override fun onRefresh(refreshLayout: TwinklingRefreshLayout?) {
                page = 1
                doRequest(page, date, companyId)
            }

            override fun onLoadMore(refreshLayout: TwinklingRefreshLayout?) {
                ++page
                doRequest(page, date, companyId)
            }

        })

        window = CalendarWindow(context, { date ->
            page = 1
            val calendar = java.util.Calendar.getInstance()
            calendar.set(date?.year!!, date.month - 1, date.day)
            MDTime.text = Utils.getTime(calendar.time, "MM月dd日")
            this.date = Utils.getTime(calendar.time.time, "yyyy-MM-dd")
            doRequest(page, this.date, companyId)
        })
        MDTime.text = Utils.getTime(System.currentTimeMillis(), "MM月dd日")
        matters_img1.setOnClickListener {
            //跳转公司列表
            startActivityForResult(Intent(context, CompanySelectActivity::class.java), Extras.REQUESTCODE)
        }
        matters_img2.setOnClickListener {
            //选择日期
            window.showAtLocation(find(R.id.project_matter_main), Gravity.BOTTOM, 0, 0)
        }
    }

    override fun onResume() {
        super.onResume()
        doRequest(page, date)
    }


    fun doRequest(page: Int, date: String, companyId: String? = null) {
        SoguApi.getService(activity.application)
                .ShowMatterSchedule(page, time = date, company_id = companyId)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ payload ->
                    if (payload.isOk) {
                        if (page == 1) {
                            data.clear()
                        }
                        Log.d("WJY", Gson().toJson(payload.payload))
                        payload.payload?.let {
                            val companyList = ArrayList<ProjectMatterCompany>()
                            val infoList = ArrayList<ScheduleBean>()
                            it.forEachIndexed { index, projectMattersBean ->
                                var companyid = ""
                                projectMattersBean.item?.forEachIndexed { position, scheduleBean ->
                                    if (!infoList.contains(scheduleBean)) {
                                        infoList.add(scheduleBean)
                                    }
                                    companyid = scheduleBean.company_id.toString()
                                }
                                projectMattersBean.cName?.let {
                                    val matterCompany = ProjectMatterCompany(it, companyid)
                                    if (!companyList.contains(matterCompany)) {
                                        companyList.add(matterCompany)
                                    }
                                }
                            }
                            val map = HashMap<String, List<ScheduleBean>>()
                            companyList.forEachIndexed { position, name ->
                                val infos = ArrayList<ScheduleBean>()
                                data.add(name)
                                infoList.forEachIndexed { i, scheduleBean ->
                                    val day = scheduleBean.start_time?.split(" ")?.get(0)
                                    if (name.companyId == scheduleBean.company_id.toString()) {
                                        data.add(scheduleBean)
                                        infos.add(scheduleBean)
                                    }
                                }
                                map.put(name.companyName, infos)
                            }
                            projectAdapter.notifyDataSetChanged()
                        }

                    } else {
                        showCustomToast(R.drawable.icon_toast_fail, payload.message)
                    }
                }, { e ->
                    Trace.e(e)
                }, {
                    if (data.size == 0) {
                        iv_empty.visibility = View.VISIBLE
                    } else {
                        iv_empty.visibility = View.GONE
                    }
                    refresh?.setEnableLoadmore((data.size - 1) % 20 == 0)
                    projectAdapter.notifyDataSetChanged()
                    if (page == 1) {
                        refresh?.finishRefreshing()
                    } else {
                        refresh?.finishLoadmore()
                    }
                })
    }



    override fun onItemClick(view: View, position: Int) {
        val bean = data[position] as ScheduleBean

        when (bean.type) {
            0 -> {
                //日程
                TaskDetailActivity.start(activity, bean.data_id!!, bean.title!!, ModifyTaskActivity.Schedule)
            }
            1 -> {
                //任务
                TaskDetailActivity.start(activity, bean.data_id!!, bean.title!!, ModifyTaskActivity.Task)
            }
            2 -> {
                //会议
            }
            3 -> {
                //用印审批
                SealApproveActivity.start(activity, bean.data_id!!, "用印审批")
            }
            4 -> {
                //签字审批
                SignApproveActivity.start(activity, bean.data_id!!, "签字审批")
            }
            5 -> {
                //跟踪记录
                getCompanyDetail(bean.data_id!!, 5)
            }
            6 -> {
                //项目
                getCompanyDetail(bean.data_id!!, 6)
            }
            7 -> {
                //请假
            }
            8 -> {
                // 出差
            }
        }
    }

    fun getCompanyDetail(cId: Int, type: Int) {
        SoguApi.getService(activity.application)
                .singleCompany(cId)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ payload ->
                    if (payload.isOk) {
                        payload.payload?.let {
                            when (type) {
                                5 -> {
                                    RecordTraceActivity.start(activity, it)
                                }
                                6 -> {
                                    ProjectActivity.start(activity, it)
                                }
                            }
                        }
                    } else {
                        showCustomToast(R.drawable.icon_toast_fail, payload.message)
                    }
                }, { e ->
                    Trace.e(e)
                })
    }

    override fun finishCheck(isChecked: Boolean, position: Int) {
        val bean = data[position] as ScheduleBean
        bean.id?.let { finishTask(it, isChecked) }
    }

    fun finishTask(id: Int, isChecked: Boolean) {
        SoguApi.getService(activity.application)
                .finishTask(id)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ payload ->
                    if (payload.isOk) {
                        payload.payload?.let {
                            if (it == 1) {
                                showCustomToast(R.drawable.icon_toast_success, "您完成了该日程")
                            } else {
                                showCustomToast(R.drawable.icon_toast_success, "您重新打开了该日程")
                            }
                        }
                    } else {
                        showCustomToast(R.drawable.icon_toast_fail, payload.message)
                    }
                }, { e ->
                    Trace.e(e)
                })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == Extras.REQUESTCODE && resultCode == Activity.RESULT_OK && data != null) {
            val bean = data.getSerializableExtra(Extras.DATA) as CustomSealBean.ValueBean
            page = 1
            companyId = bean.id.toString()
            doRequest(page, date, companyId)
        }
    }


    companion object {
        private val ARG_PARAM1 = "param1"
        private val ARG_PARAM2 = "param2"

        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment ProjectMattersFragment.
         */
        fun newInstance(param1: String, param2: String): ProjectMattersFragment {
            val fragment = ProjectMattersFragment()
            val args = Bundle()
            args.putString(ARG_PARAM1, param1)
            args.putString(ARG_PARAM2, param2)
            fragment.arguments = args
            return fragment
        }
    }
}
