package com.sogukj.pe.ui.weekly

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.GradientDrawable.RECTANGLE
import android.os.Bundle
import android.support.v4.app.FragmentManager
import android.widget.TextView
import com.framework.base.BaseActivity
import com.sogukj.pe.R
import com.sogukj.pe.util.Utils
import kotlinx.android.synthetic.main.activity_personal_weekly.*
import org.jetbrains.anko.textColor

class PersonalWeeklyActivity : BaseActivity() {

    val fragments = arrayOf(
            WeeklyThisFragment(),
            RecordBuChongFragment()
    )

    lateinit var manager: FragmentManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_personal_weekly)

        manager = supportFragmentManager
        manager.beginTransaction().add(R.id.container, fragments[0]).commit()

        back.setOnClickListener {
            finish()
        }

        clicked(weekly, true)
        clicked(record_buchong, false)

        weekly.setOnClickListener {
            replace(0)
        }

        record_buchong.setOnClickListener {
            replace(1)
        }

        back.setOnClickListener {
            finish()
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
            clicked(record_buchong, false)
        } else if (checkedId == 1) {
            clicked(weekly, false)
            clicked(record_buchong, true)
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
}
