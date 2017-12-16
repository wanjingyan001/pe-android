package com.sogukj.pe.ui.score

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.support.design.widget.TabLayout
import android.support.v4.app.Fragment
import android.support.v4.view.ViewPager
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.framework.base.ToolbarActivity
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.util.Utils
import com.sogukj.pe.view.ArrayPagerAdapter
import kotlinx.android.synthetic.main.activity_judge.*
import org.jetbrains.anko.textColor


class JudgeActivity : ToolbarActivity(), JudgeFragment.judgeInterface {

    companion object {
        fun start(ctx: Activity?, type: Int? = null) {
            val intent = Intent(ctx, JudgeActivity::class.java)
            intent.putExtra(Extras.FLAG, type)
            ctx?.startActivity(intent)
        }
    }

    val TYPE_WAIT = 1
    val TYPE_END = 2
    val TYPE_EMPLOYEE = 3
    val TYPE_MANAGE = 4

    lateinit var fragments: Array<JudgeFragment>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_judge)

        var type = intent.getIntExtra(Extras.FLAG, 0)

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

        fragments = arrayOf(
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

        toolbar_menu.setOnClickListener {
            if (canClick) {
                //0-未完成，1-已完成
                TotalScoreActivity.start(context, 0)
            }
        }
    }

    var canClick = false

    override fun onStart() {
        super.onStart()
//            var mTabStrip_C = tabs::class.java.getDeclaredField("mTabStrip")
//            mTabStrip_C.setAccessible(true)
//            var mTabStrip = mTabStrip_C.get(tabs) as LinearLayout
//            var dp20 = Utils.dpToPx(context, 20)
//            for (i in 0..mTabStrip.childCount) {
//                var tabView = mTabStrip.getChildAt(i) as View
//
//                //拿到tabView的mTextView属性  tab的字数不固定一定用反射取mTextView
//                var mTextViewField = tabView::class.java.getDeclaredField("mTextView")
//                mTextViewField.setAccessible(true);
//
//                var mTextView = mTextViewField.get(tabView) as TextView
//
//                var params = tabView.getLayoutParams() as LinearLayout.LayoutParams
//                params.width = dp20
//                tabView.setLayoutParams(params)
//
//                tabView.invalidate()
//            }
//        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {

            var width = tabs.width / tabs.childCount
            var indicator_width = Utils.dpToPx(context, 20)

            //获取TabStrip
            var mTabStrip_Filed = tabs::class.java.getDeclaredField("mTabStrip")
            mTabStrip_Filed.setAccessible(true)
            var mTabStrip = mTabStrip_Filed.get(tabs) as LinearLayout

            //
            var left_F = mTabStrip::class.java.getDeclaredField("mIndicatorLeft")
            left_F.isAccessible = true
            left_F.set(mTabStrip, (width - indicator_width) / 2)
            var right_F = mTabStrip::class.java.getDeclaredField("mIndicatorRight")
            right_F.isAccessible = true
            right_F.set(mTabStrip, (width + indicator_width) / 2)
            mTabStrip.invalidate()

            // 获取有参函数
//            val method1 = mTabStrip::class.java.getDeclaredMethod("setIndicatorPosition", Int::class.java, Int::class.java)
//            method1.setAccessible(true)
//            var left = (width - indicator_width) / 2
//            var right = (width + indicator_width) / 2
//            method1.invoke(mTabStrip, left, right)
//
//            tabs.invalidate()
        }
    }

    override fun judgeFinish() {
        var type = intent.getIntExtra(Extras.FLAG, 0)
        if (view_pager.currentItem == 0 && type == TYPE_EMPLOYEE) {
            toolbar_menu.text = "我的分数"
        } else if (view_pager.currentItem == 0 && type == TYPE_MANAGE) {
            toolbar_menu.text = "查看分数"
        }
        canClick = true
    }
}
