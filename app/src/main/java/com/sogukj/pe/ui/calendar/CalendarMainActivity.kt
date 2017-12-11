package com.sogukj.pe.ui.calendar

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.view.ViewPager
import com.framework.base.ToolbarActivity
import com.ldf.calendar.model.CalendarDate
import com.sogukj.pe.R
import com.sogukj.pe.util.Utils
import kotlinx.android.synthetic.main.activity_calendar_mian.*
import java.text.SimpleDateFormat
import java.util.*

class CalendarMainActivity : ToolbarActivity(), MonthSelectListener, ViewPager.OnPageChangeListener {

    companion object {
        fun start(ctx: Activity?) {
            ctx?.startActivity(Intent(ctx, CalendarMainActivity::class.java))
        }
    }

    val fragments = ArrayList<Fragment>()
    val titles = arrayListOf("日程", "任务", "项目事项", "团队日程")
    private lateinit var scheduleFragment: ScheduleFragment
    private lateinit var teamScheduleFragment: TeamScheduleFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_calendar_mian)

        setBack(true)
        scheduleFragment = ScheduleFragment.newInstance("", "")
        teamScheduleFragment = TeamScheduleFragment.newInstance("", "")
        scheduleFragment.monthSelect = this
        teamScheduleFragment.monthSelect = this
        fragments.add(scheduleFragment)
        fragments.add(TaskFragment.newInstance("", ""))
        fragments.add(ProjectMattersFragment.newInstance("", ""))
        fragments.add(teamScheduleFragment)
        val adapter = ContentAdapter(supportFragmentManager, fragments, titles)
        contentPager.adapter = adapter
        tabLayout.setupWithViewPager(contentPager)
        Utils.setUpIndicatorWidth(tabLayout, 18, 18, this)

        contentPager.addOnPageChangeListener(this)
        contentPager.currentItem = 0
        title = SimpleDateFormat("yyyy年MM月").format(Date(System.currentTimeMillis()))
    }

    override fun onMonthSelect(date: CalendarDate) {
        title = "${date.year}年${date.month}月"
    }

    override fun onPageScrollStateChanged(state: Int) {

    }

    override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {

    }

    override fun onPageSelected(position: Int) {
        when (position) {
            1, 2 -> {
                title = "日历"
            }
            0 -> {
                title = scheduleFragment.date
            }
            3 -> {
                title = teamScheduleFragment.date
            }
        }
    }
}
