package com.sogukj.pe.ui.weekly

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.support.v4.app.FragmentManager
import android.util.Log
import android.widget.TextView
import com.framework.base.ToolbarActivity
import com.sogukj.pe.R
import kotlinx.android.synthetic.main.activity_weekly.*
import org.jetbrains.anko.textColor


class WeeklyActivity : ToolbarActivity() {

    val fragments = arrayOf(
            WeeklyThisFragment.newInstance("MAIN", null, null, null, 1000000),
            WeeklyWaitToWatchFragment(),
            WeeklyISendFragment()
    )

    lateinit var manager: FragmentManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_weekly)

        title = "工作概览"
        toolbar?.setBackgroundColor(Color.TRANSPARENT)
        setBack(true)

        manager = supportFragmentManager
        manager.beginTransaction().add(R.id.container, fragments[0]).commit()

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

    fun switchContent(from: Int, to: Int) {
        if (!fragments[to].isAdded) { // 先判断是否被add过
            manager.beginTransaction().hide(fragments[from])
                    .add(R.id.container, fragments[to]).commit() // 隐藏当前的fragment，add下一个到Activity中
        } else {
            manager.beginTransaction().hide(fragments[from]).show(fragments[to]).commit() // 隐藏当前的fragment，显示下一个
        }
    }

    var current = 0

    fun replace(checkedId: Int) {
        if (checkedId == current) {
            return
        }
        switchContent(current, checkedId)
        current = checkedId
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
