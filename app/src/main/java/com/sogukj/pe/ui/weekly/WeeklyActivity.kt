package com.sogukj.pe.ui.weekly

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import com.framework.base.ToolbarActivity
import com.sogukj.pe.R
import com.sogukj.pe.view.ArrayPagerAdapter
import kotlinx.android.synthetic.main.activity_weekly.*

class WeeklyActivity : ToolbarActivity() {

    val fragments = arrayOf(
            WeeklyThisFragment(),
            WeeklyWaitToWatchFragment(),
            WeeklyISendFragment()
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_weekly)

        title = "工作概览"
        toolbar?.setBackgroundColor(Color.TRANSPARENT)
        setBack(true)

        //

        //R.layout.weeklylabel
        //R.layout.weekly_event  事件和跟踪
        //R.layout.weekly_leave  请假 出差
        var adapter = ArrayPagerAdapter(supportFragmentManager, fragments)
        view_pager.adapter = adapter
    }

    companion object {
        fun start(ctx: Activity?) {
            val intent = Intent(ctx, WeeklyActivity::class.java)
            ctx?.startActivity(intent)
        }
    }
}
