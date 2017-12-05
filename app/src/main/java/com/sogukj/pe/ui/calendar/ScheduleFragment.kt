package com.sogukj.pe.ui.calendar


import android.graphics.Color
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.view.ViewPager
import android.support.v7.widget.LinearLayoutManager
import android.text.TextUtils
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.framework.base.BaseFragment
import com.ldf.calendar.component.CalendarAttr
import com.ldf.calendar.component.CalendarViewAdapter
import com.ldf.calendar.interf.OnSelectDateListener
import com.ldf.calendar.model.CalendarDate
import com.ldf.calendar.view.MonthPager

import com.sogukj.pe.R
import com.sogukj.pe.bean.ScheduleBean
import com.sogukj.pe.util.Utils
import com.sogukj.pe.view.FlowLayout
import com.sogukj.pe.view.RecyclerAdapter
import com.sogukj.pe.view.RecyclerHolder
import kotlinx.android.synthetic.main.fragment_schedule.*
import org.jetbrains.anko.backgroundResource
import org.jetbrains.anko.collections.forEachByIndex
import org.jetbrains.anko.find
import org.jetbrains.anko.imageResource
import org.jetbrains.anko.support.v4.find
import org.jetbrains.anko.textColor


/**
 * A simple [Fragment] subclass.
 * Use the [ScheduleFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ScheduleFragment : BaseFragment() {
    override val containerViewId: Int
        get() = R.layout.fragment_schedule

    lateinit var adapter: RecyclerAdapter<List<ScheduleBean>>
    private var mParam1: String? = null
    private var mParam2: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (arguments != null) {
            mParam1 = arguments.getString(ARG_PARAM1)
            mParam2 = arguments.getString(ARG_PARAM2)
        }
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        calendar_view.setViewheight(Utils.dpToPx(context, 270))
        initCalendarView()
        initList()
    }


    private fun initCalendarView() {
        val dayView = CustomDayView(context, R.layout.custom_day)
        val calendarAdapter = CalendarViewAdapter(context, object : OnSelectDateListener {
            override fun onSelectDate(date: CalendarDate?) {
                //选中日期监听
            }

            override fun onSelectOtherMonth(offset: Int) {
                calendar_view.selectOtherMonth(offset)
            }

        }, CalendarAttr.CalendayType.MONTH, dayView)
        CalendarViewAdapter.weekArrayType = 1
        calendar_view.adapter = calendarAdapter
        calendar_view.currentItem = MonthPager.CURRENT_DAY_INDEX
        calendar_view.setPageTransformer(false) { page, position ->
            page?.alpha = Math.sqrt((1 - Math.abs(position)).toDouble()).toFloat()
        }
        calendar_view.addOnPageChangeListener(object : MonthPager.OnPageChangeListener {
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
            }

            override fun onPageSelected(position: Int) {

            }

            override fun onPageScrollStateChanged(state: Int) {
            }

        })

    }


    private fun initList() {
        adapter = RecyclerAdapter(context, { _adapter, parent, position ->
            val convertView = _adapter.getView(R.layout.item_schedule_list, parent)
            object : RecyclerHolder<List<ScheduleBean>>(convertView) {
                val contentLayout = convertView.find<LinearLayout>(R.id.scheduleContent)
                val dayTv = convertView.find<TextView>(R.id.dayTv)
                override fun setData(view: View, data: List<ScheduleBean>, position: Int) {
                    data[0].time?.let {
                        dayTv.text = Utils.getDayFromDate(it)
                    }
                    contentLayout.removeAllViews()
                    data.forEachIndexed { index, scheduleBean ->
                        val px = Utils.dpToPx(context, 15)
                        val inflate = LinearLayout(context)
                        val layout = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
                        layout.gravity = Gravity.CENTER_VERTICAL
                        inflate.layoutParams = layout
                        val timeTv = TextView(context)
                        timeTv.setPadding(px, px, 0, px)
                        timeTv.textColor = Color.parseColor("#adb1be")
                        val contentTv = TextView(context)
                        contentTv.setPadding(px, px, 0, px)
                        val params = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT)
                        params.weight = 1F
                        contentTv.maxLines = 1
                        contentTv.ellipsize = TextUtils.TruncateAt.END
                        contentTv.textColor = resources.getColor(R.color.text_1)
                        val finishBox = CheckBox(context)
                        finishBox.setPadding(Utils.dpToPx(context, 10), 0,
                                Utils.dpToPx(context, 10), 0)
                        val arrow = ImageView(context)
                        arrow.imageResource = R.drawable.ic_right
                        val params1 = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
                        params1.gravity = Gravity.CENTER_VERTICAL
                        params1.rightMargin = Utils.dpToPx(context, 20)
                        inflate.addView(timeTv)
                        inflate.addView(contentTv, params)
                        inflate.addView(finishBox)
                        inflate.addView(arrow, params1)
                        scheduleBean.time?.let {
                            timeTv.text = Utils.getTime(it)
                        }
                        contentTv.text = scheduleBean.title
                        finishBox.setOnCheckedChangeListener({ buttonView, isChecked ->

                        })
                        contentLayout.addView(inflate)
                        if (index != data.size - 1) {
                            val divide = View(context)
                            divide.backgroundResource = R.color.divider2
                            val params2 = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, Utils.dpToPx(context, 1))
                            params2.leftMargin = Utils.dpToPx(context, 68)
                            params2.rightMargin = Utils.dpToPx(context, 20)
                            contentLayout.addView(divide, params2)
                        }
                    }
                }
            }
        })
        adapter.onItemClick = { v, position ->
        }
        list.layoutManager = LinearLayoutManager(context)
        list.adapter = adapter
    }


    override fun onStart() {
        super.onStart()
        val list = ArrayList<List<ScheduleBean>>()
        for (i in 0..5) {
            val list2 = ArrayList<ScheduleBean>()
            for (j in 0..3) {
                val bean = ScheduleBean()
                bean.time = System.currentTimeMillis()
                bean.title = "${i}*${j}条数据"
                list2.add(bean)
            }
            list.add(list2)
        }
        adapter.dataList.addAll(list)
        adapter.notifyDataSetChanged()
    }


    companion object {
        private val ARG_PARAM1 = "param1"
        private val ARG_PARAM2 = "param2"

        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment ScheduleFragment.
         */
        fun newInstance(param1: String, param2: String): ScheduleFragment {
            val fragment = ScheduleFragment()
            val args = Bundle()
            args.putString(ARG_PARAM1, param1)
            args.putString(ARG_PARAM2, param2)
            fragment.arguments = args
            return fragment
        }
    }

}
