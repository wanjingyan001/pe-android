package com.sogukj.pe.ui.calendar

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.view.ViewPager
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import com.framework.base.ToolbarActivity
import com.ldf.calendar.model.CalendarDate
import com.sogukj.pe.R
import com.sogukj.pe.util.Utils
import kotlinx.android.synthetic.main.activity_calendar_mian.*
import org.jetbrains.anko.find
import java.text.SimpleDateFormat
import java.util.*
import kotlin.Comparator

class CalendarMainActivity : ToolbarActivity(), MonthSelectListener, ViewPager.OnPageChangeListener {

    companion object {
        fun start(ctx: Activity?) {
            ctx?.startActivity(Intent(ctx, CalendarMainActivity::class.java))
        }
    }

    override val menuId: Int
        get() = R.menu.calendar_menu
    val fragments = ArrayList<Fragment>()
    val titles = arrayListOf("周工作安排", "日历", "任务", "项目事项", "团队日程")
    private lateinit var arrangeFragment: ArrangeListFragment
    private lateinit var scheduleFragment: ScheduleFragment
    private lateinit var teamScheduleFragment: TeamScheduleFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_calendar_mian)
        setBack(true)
        arrangeFragment = ArrangeListFragment.newInstance("", "")
        scheduleFragment = ScheduleFragment.newInstance("", "")
        teamScheduleFragment = TeamScheduleFragment.newInstance("", "")
        scheduleFragment.monthSelect = this
        teamScheduleFragment.monthSelect = this
        fragments.add(arrangeFragment)
        fragments.add(scheduleFragment)
        fragments.add(TaskFragment.newInstance("", ""))
        fragments.add(ProjectMattersFragment.newInstance("", ""))
        fragments.add(teamScheduleFragment)
        initPager()
        title = SimpleDateFormat("yyyy年MM月").format(Date(System.currentTimeMillis()))
        addSchedule.visibility = View.GONE
        addSchedule.setOnClickListener {
            ModifyTaskActivity.startForCreate(this, ModifyTaskActivity.Schedule)
        }
    }

    private fun initPager() {
        val adapter = ContentAdapter(supportFragmentManager, fragments, titles)
        contentPager.adapter = adapter
        tabLayout.setupWithViewPager(contentPager)
        for (i in 0 until titles.size){
            val tab = tabLayout.getTabAt(i)
            tab?.let {
                it.setCustomView(R.layout.layout_calendar_main_tab)
                if (i == 0){
                    it.customView!!.isSelected = true
                }
                it.customView!!.find<TextView>(R.id.indicatorTv).text = titles[i]
            }
        }
        contentPager.addOnPageChangeListener(this)
        contentPager.offscreenPageLimit = 3
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
            0 -> {
                mMenu.getItem(0).isVisible = true
                addSchedule.visibility = View.GONE
                title = scheduleFragment.date
                addSchedule.setOnClickListener {
                    ModifyTaskActivity.startForCreate(this, ModifyTaskActivity.Schedule)
                }
            }
            1 -> {
                mMenu.getItem(0).isVisible = false
                title = scheduleFragment.date
                addSchedule.visibility = View.VISIBLE
                addSchedule.setOnClickListener {
                    ModifyTaskActivity.startForCreate(this, ModifyTaskActivity.Schedule)
                }
            }
            2 -> {
                mMenu.getItem(0).isVisible = false
                title = "日历"
                addSchedule.visibility = View.VISIBLE
                addSchedule.setOnClickListener {
                    ModifyTaskActivity.startForCreate(this, ModifyTaskActivity.Task)
                }
            }
            3 -> {
                mMenu.getItem(0).isVisible = false
                title = "日历"
                addSchedule.visibility = View.GONE
                addSchedule.setOnClickListener {
                    ModifyTaskActivity.startForCreate(this, "")
                }
            }
            4 -> {
                mMenu.getItem(0).isVisible = false
                title = teamScheduleFragment.date
                addSchedule.visibility = View.GONE
                addSchedule.setOnClickListener {
                    ModifyTaskActivity.startForCreate(this, ModifyTaskActivity.Schedule)
                }
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.calendar_menu -> {
                ArrangeEditActivity.start(this, arrangeFragment.getWeeklyData(), arrangeFragment.offset.toString())
            }
        }
        return super.onOptionsItemSelected(item)
    }
}
