package com.sogukj.pe.ui.weekly

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.TypedValue
import android.view.Gravity
import android.widget.TextView
import com.framework.base.ToolbarActivity
import com.sogukj.pe.R
import com.sogukj.pe.util.Utils
import kotlinx.android.synthetic.main.activity_weekly.*
import org.jetbrains.anko.textColor

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

        supportFragmentManager.beginTransaction()
                .replace(R.id.container, fragments[current])
                .commit()

        clicked(weekly, true)
        clicked(wait_to_watch, false)
        clicked(send, false)
        
        weekly.setOnClickListener {
            replace(0)
        }

        wait_to_watch.setOnClickListener {
            replace(1)
        }

        send.setOnClickListener {
            replace(2)
        }
    }

    var current = 0;

    fun replace(checkedId: Int) {
        if (checkedId == current) {
            return
        }
        var fragment = Fragment()
        if (checkedId == 0) {
            fragment = fragments[0]
            current = 0
            clicked(weekly, true)
            clicked(wait_to_watch, false)
            clicked(send, false)
        } else if (checkedId == 1) {
            fragment = fragments[1]
            current = 1
            clicked(weekly, false)
            clicked(wait_to_watch, true)
            clicked(send, false)
        } else if (checkedId == 2) {
            fragment = fragments[2]
            current = 2
            clicked(weekly, false)
            clicked(wait_to_watch, false)
            clicked(send, true)
        }
        supportFragmentManager.beginTransaction()
                .replace(R.id.container, fragment)
                .commit()
    }

    fun clicked(view: TextView, flag: Boolean) {
        if (flag) {
            view.textColor = Color.parseColor("#282828")
            view.setBackgroundResource(R.drawable.weekly_selected)
        } else {
            view.textColor = Color.parseColor("#c7c7c7")
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
