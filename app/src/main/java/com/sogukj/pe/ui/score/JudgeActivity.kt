package com.sogukj.pe.ui.score

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.support.design.widget.TabLayout
import android.support.v4.view.ViewPager
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.framework.base.ToolbarActivity
import com.google.gson.JsonSyntaxException
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.util.Trace
import com.sogukj.pe.util.Utils
import com.sogukj.pe.view.ArrayPagerAdapter
import com.sogukj.service.SoguApi
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_judge.*
import org.jetbrains.anko.textColor
import java.net.UnknownHostException


class JudgeActivity : ToolbarActivity(), JudgeFragment.judgeInterface {

    companion object {
        //type---员工或者领导，type1专门是领导的  岗位胜任力或者关键绩效
        //type=TYPE_MANAGE---   type1= TYPE_GANGWEI   TYPE_JIXIAO
        //type=TYPE_EMPLOYEE    type1= NORMAL, FK, TZ
        fun start(ctx: Context?, type: Int, type1: Int) {
            val intent = Intent(ctx, JudgeActivity::class.java)
            intent.putExtra(Extras.TYPE, type)
            intent.putExtra(Extras.TYPE1, type1)
            ctx?.startActivity(intent)
        }
    }

    val TYPE_WAIT = 1
    val TYPE_END = 2
    val TYPE_EMPLOYEE = 3
    val TYPE_MANAGE = 4

    val TYPE_GANGWEI = 18
    val TYPE_JIXIAO = 19

    val NORMAL = 100
    val FK = 101
    val TZ = 102

    var fragments = arrayOf<JudgeFragment>()
    var type = 0
    var type1 = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_judge)

        type = intent.getIntExtra(Extras.TYPE, 0)
        type1 = intent.getIntExtra(Extras.TYPE1, 0)

        setBack(true)
        if (type == TYPE_MANAGE && type1 == TYPE_GANGWEI) {
            setTitle("岗位胜任力评价")
        } else if (type == TYPE_MANAGE && type1 == TYPE_JIXIAO) {
            setTitle("关键绩效考核")
        } else if (type == TYPE_EMPLOYEE) {
            setTitle("岗位胜任力评价")
        }
        toolbar?.setBackgroundColor(Color.WHITE)
        toolbar?.apply {
            val title = this.findViewById(R.id.toolbar_title) as TextView?
            title?.textColor = Color.parseColor("#282828")
            val back = this.findViewById(R.id.toolbar_back) as ImageView
            back?.visibility = View.VISIBLE
            back.setImageResource(R.drawable.grey_back)
        }

        // 1=>进入绩效考核列表页面，2=>进入岗位胜任力列表 3=>进入风控部填写页，4=>进入投资部填写页----之前风控部投资部填写页已完成，所以只有1，2来两种可能
        if (type == TYPE_MANAGE) {
            if (type1 == TYPE_GANGWEI) {
                pageType = 2
            } else if (type1 == TYPE_JIXIAO) {
                pageType = 1
            }
        } else if (type == TYPE_EMPLOYEE) {
            if (type1 == FK) {
                pageType = 3
            } else if (type1 == TZ) {
                pageType = 4
            } else if (type1 == NORMAL) {
                pageType = 2
            }
        }

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

        //TODO
        var type = intent.getIntExtra(Extras.FLAG, 0)
        if (view_pager.currentItem == 0 && type == TYPE_EMPLOYEE) {
            toolbar_menu.text = "我的分数"
        } else if (view_pager.currentItem == 0 && type == TYPE_MANAGE) {
            toolbar_menu.text = "查看分数"
        }
        canClick = true
        //TODO

        toolbar_menu.setOnClickListener {
            if (canClick) {
                if (type == TYPE_EMPLOYEE) {
                    TotalScoreActivity.start(context)
                } else if (type == TYPE_MANAGE) {
                    //ScoreListActivity.start(context)
                }
            }
        }
    }

    var pageType = 0

    override fun onResume() {
        super.onResume()
        SoguApi.getService(application)
                .check(pageType)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ payload ->
                    if (payload.isOk) {
                        payload.payload?.apply {
                            if (pageType == 1) {
                                fragments = arrayOf(
                                        //type员工领导
                                        JudgeFragment.newInstance(TYPE_WAIT, type, type1, ready_grade!!),
                                        JudgeFragment.newInstance(TYPE_END, type, type1, finish_grade!!)
                                )
                                var adapter = ArrayPagerAdapter(supportFragmentManager, fragments)
                                view_pager.adapter = adapter
                            } else if (pageType == 2) {
//                                if (fragments == null || fragments.size == 0) {
//                                    fragments = arrayOf(
//                                            JudgeFragment.newInstance(TYPE_WAIT, type, type1, ready_grade!!),
//                                            JudgeFragment.newInstance(TYPE_END, type, type1, finish_grade!!)
//                                    )
//                                    var adapter = ArrayPagerAdapter(supportFragmentManager, fragments)
//                                    view_pager.adapter = adapter
//                                } else {
//                                    val intent = Bundle()
//                                    intent.putSerializable(Extras.DATA, ready_grade!!)
//                                    fragments[0].arguments = intent
//                                    fragments[0].adapter.dataList.clear()
//                                    fragments[0].adapter.dataList = ready_grade!!
//                                    fragments[0].adapter.notifyDataSetChanged()
//
//                                    val intent2 = Bundle()
//                                    intent2.putSerializable(Extras.DATA, finish_grade!!)
//                                    fragments[1].arguments = intent
//                                    fragments[1].adapter.dataList.clear()
//                                    fragments[1].adapter.dataList = ready_grade!!
//                                    fragments[1].adapter.notifyDataSetChanged()
//                                }

                                view_pager.adapter = null
                                fragments = arrayOf<JudgeFragment>()
                                fragments = arrayOf(
                                        JudgeFragment.newInstance(TYPE_WAIT, type, type1, ready_grade!!),
                                        JudgeFragment.newInstance(TYPE_END, type, type1, finish_grade!!)
                                )
                                var adapter = ArrayPagerAdapter(supportFragmentManager, fragments)
                                view_pager.adapter = adapter
                            } else {
                                fragments = arrayOf(
                                )
                                var adapter = ArrayPagerAdapter(supportFragmentManager, fragments)
                                view_pager.adapter = adapter
                            }
                        }
                    } else
                        showToast(payload.message)
                }, { e ->
                    Trace.e(e)
                    when (e) {
                        is JsonSyntaxException -> showToast("后台数据出错")
                        is UnknownHostException -> showToast("网络出错")
                        else -> showToast("未知错误")
                    }
                })
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
