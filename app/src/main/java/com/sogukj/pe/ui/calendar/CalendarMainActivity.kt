package com.sogukj.pe.ui.calendar

import android.app.Activity
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.app.Fragment
import com.framework.base.ToolbarActivity
import com.sogukj.pe.R
import com.sogukj.pe.util.Utils
import kotlinx.android.synthetic.main.activity_calendar_mian.*

class CalendarMainActivity : ToolbarActivity() {
    companion object {
        fun start(ctx: Activity?) {
            ctx?.startActivity(Intent(ctx, CalendarMainActivity::class.java))
        }
    }

    val fragments = ArrayList<Fragment>()
    val titles = arrayListOf("日程", "任务", "项目事项", "团队日程")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_calendar_mian)
        title = "日历"
        setBack(true)
        fragments.add(ScheduleFragment.newInstance("", ""))
        fragments.add(TaskFragment.newInstance("", ""))
        fragments.add(ProjectMattersFragment.newInstance("", ""))
        fragments.add(TeamScheduleFragment.newInstance("", ""))
        val adapter = ContentAdapter(supportFragmentManager, fragments, titles)
        contentPager.adapter = adapter
        tabLayout.setupWithViewPager(contentPager)
        Utils.setUpIndicatorWidth(tabLayout, 18, 18, this)
    }
}
