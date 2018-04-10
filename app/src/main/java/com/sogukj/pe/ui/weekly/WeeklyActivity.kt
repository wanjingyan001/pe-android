package com.sogukj.pe.ui.weekly

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.support.design.widget.TabLayout
import android.support.v4.view.ViewPager
import android.widget.LinearLayout
import android.widget.TextView
import com.framework.base.ToolbarActivity
import com.sogukj.pe.R
import com.sogukj.pe.view.ArrayPagerAdapter
import kotlinx.android.synthetic.main.activity_weekly.*
import org.jetbrains.anko.textColor


class WeeklyActivity : ToolbarActivity() {

    val fragments = arrayOf(
            WeeklyThisFragment.newInstance("MAIN", null, null, null, 1000000),
            WeeklyWaitToWatchFragment(),
            WeeklyISendFragment()
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_weekly)

        title = "工作概览"
        toolbar?.setBackgroundColor(Color.TRANSPARENT)
        setBack(true)

        var adapter = ArrayPagerAdapter(supportFragmentManager, fragments)
        view_pager.adapter = adapter
        view_pager.offscreenPageLimit = fragments.size

        view_pager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) {
            }

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
            }

            override fun onPageSelected(position: Int) {
                replace(position)
            }
        })

        clicked(weekly, true)
        clicked(wait_to_watch, false)
        clicked(send, false)

        weekly.setOnClickListener {
            replace(0)
            view_pager.setCurrentItem(0, true)
        }

        wait_to_watch.setOnClickListener {
            replace(1)
            view_pager.setCurrentItem(1, true)
        }

        send.setOnClickListener {
            replace(2)
            view_pager.setCurrentItem(2, true)
        }
    }

    fun replace(checkedId: Int) {
        if (checkedId == 0) {
            clicked(weekly, true)
            clicked(wait_to_watch, false)
            clicked(send, false)
        } else if (checkedId == 1) {
            clicked(weekly, false)
            clicked(wait_to_watch, true)
            clicked(send, false)
        } else if (checkedId == 2) {
            clicked(weekly, false)
            clicked(wait_to_watch, false)
            clicked(send, true)
        }
    }

    fun clicked(view: TextView, flag: Boolean) {
        if (flag) {
            view.textColor = Color.parseColor("#FF282828")
            view.setBackgroundResource(R.drawable.weekly_selected)
        } else {
            view.textColor = Color.parseColor("#FFc7c7c7")
            view.setBackgroundResource(R.drawable.weekly_unselected)
        }
    }

    companion object {
        fun start(ctx: Activity?) {
            val intent = Intent(ctx, WeeklyActivity::class.java)
            ctx?.startActivity(intent)
        }
    }
}
