package com.sogukj.pe.ui.score

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.support.design.widget.TabLayout
import android.support.v4.view.ViewPager
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.framework.base.ToolbarActivity
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.view.ArrayPagerAdapter
import kotlinx.android.synthetic.main.activity_rate.*
import kotlinx.android.synthetic.main.item_rate.view.*
import org.jetbrains.anko.textColor

class JudgeActivity : ToolbarActivity() {

    companion object {
        fun start(ctx: Activity?, type: Int) {
            val intent = Intent(ctx, RateActivity::class.java)
            intent.putExtra(Extras.TYPE, type)
            ctx?.startActivity(intent)
        }
    }

    val TYPE_WAIT = 1
    val TYPE_END = 2
    val TYPE_EMPLOYEE = 3
    val TYPE_MANAGE = 4

    lateinit var fragments: Array<JudgeFragment>
    var type = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_judge)

        type = intent.getIntExtra(Extras.TYPE, 0)

        setBack(true)
        if (type == TYPE_EMPLOYEE) {
            setTitle("岗位胜任力评价")
        } else if (type == TYPE_MANAGE) {
            setTitle("年终考核评价中心")
        }
        toolbar?.setBackgroundColor(Color.WHITE)
        toolbar?.apply {
            val title = this.findViewById(R.id.toolbar_title) as TextView?
            title?.textColor = Color.parseColor("#282828")
            val back = this.findViewById(R.id.toolbar_back) as ImageView
            back?.visibility = View.VISIBLE
            back.setImageResource(R.drawable.grey_back)
        }

        val fragments = arrayOf(
                JudgeFragment.newInstance(TYPE_WAIT, type),
                JudgeFragment.newInstance(TYPE_END, type)
        )

        var adapter = ArrayPagerAdapter(supportFragmentManager, fragments)
        view_pager.adapter = adapter
        view_pager.offscreenPageLimit = fragments.size

        tabs?.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabReselected(tab: TabLayout.Tab?) {

            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {

            }

            override fun onTabSelected(tab: TabLayout.Tab) {
                view_pager?.currentItem = tab.position
            }

        })
        view_pager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) {
            }

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
            }

            override fun onPageSelected(position: Int) {
                tabs?.getTabAt(position)?.select()
            }

        })
    }
}
