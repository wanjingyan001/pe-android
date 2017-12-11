package com.sogukj.pe.ui.calendar

import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.View
import android.widget.CompoundButton
import com.framework.base.BaseFragment
import com.google.gson.Gson
import com.ldf.calendar.component.CalendarAttr
import com.ldf.calendar.component.CalendarViewAdapter
import com.ldf.calendar.interf.OnSelectDateListener
import com.ldf.calendar.model.CalendarDate
import com.ldf.calendar.view.Calendar
import com.ldf.calendar.view.MonthPager
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.bean.ScheduleBean
import com.sogukj.pe.bean.UserBean
import com.sogukj.pe.ui.user.OrganizationActivity
import com.sogukj.pe.util.Trace
import com.sogukj.pe.util.Utils
import com.sogukj.service.SoguApi
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_team_schedule.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

/**
 * A simple [Fragment] subclass.
 * Activities that contain this fragment must implement the
 * to handle interaction events.
 * Use the [TeamScheduleFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class TeamScheduleFragment : BaseFragment(), ScheduleItemClickListener {



    override val containerViewId: Int
        get() = R.layout.fragment_team_schedule
    lateinit var teamAdapter: TeamAdapter
    val data = ArrayList<ScheduleBean>()

    private var mParam1: String? = null
    private var mParam2: String? = null
    lateinit var monthSelect: MonthSelectListener
    var page = 1
    var filter: StringBuilder = StringBuilder("")
    lateinit var date: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (arguments != null) {
            mParam1 = arguments.getString(ARG_PARAM1)
            mParam2 = arguments.getString(ARG_PARAM2)
        }
        val format = SimpleDateFormat("yyyy年MM月").format(Date(System.currentTimeMillis()))
        date = format
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initCalendarView()
        initList()
        doRequest(page, SimpleDateFormat("yyyy-MM-dd").format(Date(System.currentTimeMillis())))
    }


    private fun initCalendarView() {
        calendar_view.setViewheight(Utils.dpToPx(context, 270))
        val dayView = CustomDayView(context, R.layout.custom_day)
        val calendarAdapter = CalendarViewAdapter(context, object : OnSelectDateListener {
            override fun onSelectDate(date: CalendarDate?) {
                //选中日期监听
                val calendar = java.util.Calendar.getInstance()
                calendar.set(date?.year!!, date.month - 1, date.day)
                doRequest(page, Utils.getTime(calendar.time.time, "yyyy-MM-dd"), filter.toString())
            }

            override fun onSelectOtherMonth(offset: Int) {
                calendar_view.selectOtherMonth(offset)
            }

        }, CalendarAttr.CalendayType.MONTH, dayView)
        CalendarViewAdapter.weekArrayType = 1
        calendar_view.adapter = calendarAdapter
        calendar_view.currentItem = MonthPager.CURRENT_DAY_INDEX
        calendar_view.setPageTransformer(false) { page, position ->
            page?.alpha = Math.sqrt((1 - Math.abs(position)).toDouble()).toFloat()
        }
        calendar_view.addOnPageChangeListener(object : MonthPager.OnPageChangeListener {
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
            }

            override fun onPageSelected(position: Int) {
                val currentCalendars = calendarAdapter.pagers
                if (currentCalendars[position % currentCalendars.size] is Calendar) {
                    val date = currentCalendars[position % currentCalendars.size].seedDate
                    monthSelect.onMonthSelect(date)
                    this@TeamScheduleFragment.date = "${date.year}年${date.month}月"
                }
            }

            override fun onPageScrollStateChanged(state: Int) {

            }

        })
        val map = HashMap<String, String>()
        map.put("2017-12-3", "0")
        map.put("2017-12-9", "0")
        map.put("2017-12-16", "0")
        map.put("2017-12-24", "0")
        map.put("2017-11-30", "0")
        calendarAdapter.setMarkData(map)
    }

    fun doRequest(page: Int, date: String, filter: String? = null) {
        SoguApi.getService(activity.application)
                .showSchedule(page, stat = 2, time = date, filter = filter)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ payload ->
                    if (payload.isOk) {
                        Log.d("WJY", Gson().toJson(payload.payload))
                        data.clear()
                        payload.payload?.let {
                            data.addAll(it)
                        }
                    } else {
                        showToast(payload.message)
                    }
                }, { e ->
                    Trace.e(e)
                },{
                    teamAdapter.notifyDataSetChanged()
                })
    }


    fun initList() {
        teamAdapter = TeamAdapter(context, data)
        teamList.layoutManager = LinearLayoutManager(context)
        teamList.adapter = teamAdapter
        teamAdapter.setListener(this)
    }


    fun setListener(listener: MonthSelectListener) {
        this.monthSelect = listener
    }


    override fun onItemClick(view: View, position: Int) {
        when (view.id) {
            R.id.selectTv -> {
                OrganizationActivity.startForResult(this)
            }
            R.id.teamItemLayout -> {

            }
        }
    }

    override fun finishCheck(buttonView: CompoundButton, isChecked: Boolean, position: Int) {

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == Extras.REQUESTCODE && resultCode == Extras.RESULTCODE && data != null) {
            val userBean = data.getSerializableExtra(Extras.DATA) as UserBean
            Log.d("WJY", "${userBean.name}====>${userBean.uid}")
            userBean.uid.let {
                filter = filter.append("${userBean.uid},")
                doRequest(page, date, filter.toString())
            }
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
         * @return A new instance of fragment TeamScheduleFragment.
         */
        fun newInstance(param1: String, param2: String): TeamScheduleFragment {
            val fragment = TeamScheduleFragment()
            val args = Bundle()
            args.putString(ARG_PARAM1, param1)
            args.putString(ARG_PARAM2, param2)
            fragment.arguments = args
            return fragment
        }
    }
}