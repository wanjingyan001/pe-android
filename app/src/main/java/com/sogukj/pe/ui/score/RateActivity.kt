package com.sogukj.pe.ui.score

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.support.design.widget.TabLayout
import android.support.v4.view.ViewPager
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.framework.base.ToolbarActivity
import com.sogukj.pe.R
import com.sogukj.pe.view.ArrayPagerAdapter
import kotlinx.android.synthetic.main.activity_rate.*
import org.jetbrains.anko.textColor
import com.lcodecore.tkrefreshlayout.utils.DensityUtil
import android.support.v4.view.MarginLayoutParamsCompat.setMarginEnd
import android.support.v4.view.MarginLayoutParamsCompat.setMarginStart
import android.widget.LinearLayout
import com.sogukj.pe.util.Utils
import java.lang.reflect.AccessibleObject.setAccessible


class RateActivity : ToolbarActivity() {

    companion object {
        fun start(ctx: Context?) {
            val intent = Intent(ctx, RateActivity::class.java)
            ctx?.startActivity(intent)
        }
    }

    val TYPE_JOB = 1
    val TYPE_RATE = 2

    val fragments = arrayOf(
            RateFragment.newInstance(TYPE_RATE),
            RateFragment.newInstance(TYPE_JOB)
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_rate)

        setBack(true)
        setTitle("考核评分")
        toolbar?.setBackgroundColor(Color.WHITE)
        toolbar?.apply {
            val title = this.findViewById(R.id.toolbar_title) as TextView?
            title?.textColor = Color.parseColor("#282828")
            val back = this.findViewById(R.id.toolbar_back) as ImageView
            back?.visibility = View.VISIBLE
            back.setImageResource(R.drawable.grey_back)
        }

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

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            val tabStrip = tabs::class.java.getDeclaredField("mTabStrip")
            tabStrip.setAccessible(true)
            val ll_tab = tabStrip.get(tabs) as LinearLayout
            for (i in 0 until ll_tab.childCount) {
                val child = ll_tab.getChildAt(i)
                child.setPadding(0, 0, 0, 0)
                val params = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1f)
//                params.leftMargin = 170
//                params.rightMargin = 170
                child.layoutParams = params
//                child.invalidate()
            }
        }
    }
}