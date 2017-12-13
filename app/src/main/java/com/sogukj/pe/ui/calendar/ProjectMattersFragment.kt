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
import android.widget.CompoundButton
import com.framework.base.BaseFragment
import com.google.gson.Gson
import com.lcodecore.tkrefreshlayout.RefreshListenerAdapter
import com.lcodecore.tkrefreshlayout.TwinklingRefreshLayout
import com.lcodecore.tkrefreshlayout.footer.BallPulseView
import com.lcodecore.tkrefreshlayout.header.progresslayout.ProgressLayout
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.bean.CustomSealBean
import com.sogukj.pe.bean.ScheduleBean
import com.sogukj.pe.util.Trace
import com.sogukj.pe.util.Utils
import com.sogukj.service.SoguApi
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_project_matters.*
import kotlinx.android.synthetic.main.layout_empty.*
import org.jetbrains.anko.support.v4.find
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


/**
 * A simple [Fragment] subclass.
 * Use the [ProjectMattersFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ProjectMattersFragment : BaseFragment(), View.OnClickListener, ScheduleItemClickListener {


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
        projectAdapter.setListener(this)
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


        doRequest(page, date)
        window = CalendarWindow(context, { date ->
            page = 1
            val calendar = java.util.Calendar.getInstance()
            calendar.set(date?.year!!, date.month - 1, date.day)
            this.date = Utils.getTime(calendar.time.time, "yyyy-MM-dd")
            doRequest(page, this.date, companyId)
        })
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
                            val dayList = ArrayList<String>()
                            val companyList = ArrayList<ProjectMatterCompany>()
                            val infoList = ArrayList<ScheduleBean>()
                            it.forEachIndexed { index, projectMattersBean ->
                                var companyid = ""
                                projectMattersBean.item?.forEachIndexed { position, scheduleBean ->
                                    val day = scheduleBean.start_time?.split(" ")?.get(0)
                                    if (!dayList.contains(day) && day != null) {
                                        dayList.add(day)
                                    }
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
                            dayList.forEachIndexed { index, s ->
                                val md = ProjectMatterMD(s)
                                data.add(md)
                                companyList.forEachIndexed { position, name ->
                                    val infos = ArrayList<ScheduleBean>()
                                    data.add(name)
                                    infoList.forEachIndexed { i, scheduleBean ->
                                        val day = scheduleBean.start_time?.split(" ")?.get(0)
                                        if (day.equals(s) && name.companyId == scheduleBean.company_id.toString()) {
                                            data.add(scheduleBean)
                                            infos.add(scheduleBean)
                                        }
                                    }
                                }
                            }
                            projectAdapter.notifyDataSetChanged()
                        }

                    } else {
                        showToast(payload.message)
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

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.matters_img1 -> {
                //跳转公司列表
                startActivityForResult(Intent(context, CompanySelectActivity::class.java), Extras.REQUESTCODE)
            }
            R.id.matters_img2 -> {
                //选择日期
                window.showAtLocation(find(R.id.project_matter_main), Gravity.BOTTOM, 0, 0)
            }
        }
    }

    override fun onItemClick(view: View, position: Int) {
    }

    override fun finishCheck(buttonView: CompoundButton, isChecked: Boolean, position: Int) {
        val bean = data[position] as ScheduleBean
        bean.id?.let { finishTask(it) }
    }

    fun finishTask(id: Int) {
        SoguApi.getService(activity.application)
                .finishTask(id)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ payload ->
                    if (payload.isOk) {
                        showToast("日程标记成功")
                    } else {
                        showToast(payload.message)
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
