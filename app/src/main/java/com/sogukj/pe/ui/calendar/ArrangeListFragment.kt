package com.sogukj.pe.ui.calendar


import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.support.constraint.ConstraintLayout
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.framework.base.BaseFragment
import com.google.gson.Gson
import com.lcodecore.tkrefreshlayout.RefreshListenerAdapter
import com.lcodecore.tkrefreshlayout.TwinklingRefreshLayout
import com.lcodecore.tkrefreshlayout.footer.BallPulseView
import com.lcodecore.tkrefreshlayout.header.progresslayout.ProgressLayout

import com.sogukj.pe.R
import com.sogukj.pe.bean.WeeklyArrangeBean
import com.sogukj.pe.util.Trace
import com.sogukj.pe.util.Utils
import com.sogukj.pe.view.ArrangeFooterView
import com.sogukj.pe.view.ArrangeHeaderView
import com.sogukj.pe.view.RecyclerAdapter
import com.sogukj.pe.view.RecyclerHolder
import com.sogukj.service.SoguApi
import com.sougukj.isNullOrEmpty
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_arrange_list.*
import org.jetbrains.anko.backgroundColor
import org.jetbrains.anko.backgroundResource
import org.jetbrains.anko.find
import org.jetbrains.anko.support.v4.ctx
import kotlin.properties.Delegates


/**
 * A simple [Fragment] subclass.
 * Use the [ArrangeListFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ArrangeListFragment : BaseFragment() {
    override val containerViewId: Int
        get() = R.layout.fragment_arrange_list
    private var mParam1: String? = null
    private var mParam2: String? = null
    private lateinit var arrangeAdapter: ArrangeAdapter
    var offset: Int = 0
    lateinit var inflate: View
    var isRefresh = false
    var isLoadMore = false
    var isNextWeekly = false
    var isLastWeekly = false
    private var isUpwards = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (arguments != null) {
            mParam1 = arguments.getString(ARG_PARAM1)
            mParam2 = arguments.getString(ARG_PARAM2)
        }
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        arrangeAdapter = ArrangeAdapter()
        recycler_view.layoutManager = LinearLayoutManager(context)
        recycler_view.adapter = arrangeAdapter
        val arrHeader = ArrangeHeaderView(context)
        refresh.setHeaderView(arrHeader)
        val arrFooter = ArrangeFooterView(context)
        refresh.setBottomView(arrFooter)
        refresh.setEnableLoadmore(true)
        refresh.setAutoLoadMore(false)
        refresh.setOnRefreshListener(object : RefreshListenerAdapter() {
            override fun onRefresh(refreshLayout: TwinklingRefreshLayout?) {
                if (!isRefresh) {
                    offset += 1
                    isRefresh = true
                    if (offset > 0) {
                        isNextWeekly = true
                        isLastWeekly = false
                    }
                    doRequest()
                }
            }

            override fun onLoadMore(refreshLayout: TwinklingRefreshLayout?) {
                if (!isLoadMore) {
                    offset -= 1
                    isLoadMore = true
                    if (offset < 0) {
                        isNextWeekly = false
                        isLastWeekly = true
                    }
                    doRequest()
                }
            }

        })
        backImg.setOnClickListener {
            offset = 0
            isNextWeekly = false
            isLastWeekly = false

            doRequest()
        }
    }

    fun getWeeklyData(): ArrayList<WeeklyArrangeBean> {
        val list = arrangeAdapter.dataList
        val newList = ArrayList<WeeklyArrangeBean>()
        list.forEach {
            if (it is WeeklyArrangeBean)
                newList.add(it)
        }
        return newList
    }


    override fun onResume() {
        super.onResume()
        doRequest()
    }

    private fun doRequest() {
        SoguApi.getService(baseActivity!!.application)
                .getWeeklyWorkList(offset)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ payload ->
                    if (payload.isOk) {
                        arrangeAdapter.dataList.clear()
                        arrangeAdapter.dataList.add("")
                        payload.payload?.forEach {
                            arrangeAdapter.dataList.add(it)
                        }
                    }
                }, { e ->
                    Trace.e(e)
                }, {
                    if (isRefresh) {
                        refresh.finishRefreshing()
                        isRefresh = false
                    }
                    if (isLoadMore) {
                        recycler_view.run {
                            smoothScrollToPosition(0)
                            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                                override fun onScrolled(recyclerView: RecyclerView?, dx: Int, dy: Int) {
                                    super.onScrolled(recyclerView, dx, dy)
                                    scrollBy(0, -Utils.dpToPx(ctx, 50))
                                    handler.postDelayed({ removeOnScrollListener(this) }, 1000)
                                }
                            })
                        }
                        refresh.finishLoadmore()
                        isLoadMore = false
                    }
                    //arrangeAdapter.notifyDataSetChanged()无效
                    recycler_view.adapter = arrangeAdapter

                    if (isNextWeekly) {
                        backImg.visibility = View.VISIBLE
                        if (isUpwards) {
                            val animator = ObjectAnimator.ofFloat(backImg, "rotation", 0f, 180f)
                            animator.duration = 500
                            animator.start()
                            isUpwards = false
                        }
                    }
                    if (isLastWeekly) {
                        backImg.visibility = View.VISIBLE
                        if (!isUpwards) {
                            val animator = ObjectAnimator.ofFloat(backImg, "rotation", 180f, 0f)
                            animator.duration = 500
                            animator.start()
                            isUpwards = true
                        }
                    }
                })
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
         * @return A new instance of fragment ArrangeListFragment.
         */
        fun newInstance(param1: String, param2: String): ArrangeListFragment {
            val fragment = ArrangeListFragment()
            val args = Bundle()
            args.putString(ARG_PARAM1, param1)
            args.putString(ARG_PARAM2, param2)
            fragment.arguments = args
            return fragment
        }
    }

    inner class ArrangeAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
        var dataList = mutableListOf<Any>()
        override fun onBindViewHolder(holder: RecyclerView.ViewHolder?, position: Int) {
            if (holder is HeadHolder) {
                holder.setData(holder.itemView)
            } else if (holder is ArrangeHolder) {
                val bean = dataList[position] as WeeklyArrangeBean
                holder.setData(holder.itemView, bean, position)
            }
        }

        override fun getItemCount() = dataList.size

        override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): RecyclerView.ViewHolder {
            return when (viewType) {
                0 -> HeadHolder(LayoutInflater.from(context).inflate(R.layout.layout_arrange_weekly_header, parent, false))
                else -> ArrangeHolder(LayoutInflater.from(context).inflate(R.layout.item_arrange_weekly, parent, false))
            }
        }

        override fun getItemViewType(position: Int): Int {
            return if (position == 0) {
                0
            } else
                1
        }

        inner class ArrangeHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val weekly = itemView.find<TextView>(R.id.weekly)
            val dayOfMonth = itemView.find<TextView>(R.id.dayOfMonth)
            val content = itemView.find<TextView>(R.id.arrangement_content)
            val attendTv = itemView.find<TextView>(R.id.attend)
            val participateTv = itemView.find<TextView>(R.id.participate)
            val address = itemView.find<TextView>(R.id.arrange_address)
            val emptyLayout = itemView.find<LinearLayout>(R.id.empty_layout)
            val dayOfWeeklyLayout = itemView.find<LinearLayout>(R.id.day_of_weekly_layout)
            val addressIcon = itemView.find<ImageView>(R.id.address_icon)
            val contentLayout = itemView.find<ConstraintLayout>(R.id.contentLayout)
            fun setData(view: View, data: WeeklyArrangeBean, position: Int) {
                weekly.text = data.weekday
                dayOfMonth.text = data.date?.substring(5, data.date?.length!!)
                if (data.reasons.isNullOrEmpty() && data.place.isNullOrEmpty() && data.attendee.isNullOrEmpty() && data.participant.isNullOrEmpty()) {
                    emptyLayout.visibility = View.VISIBLE
                    contentLayout.visibility = View.GONE
                } else {
                    emptyLayout.visibility = View.GONE
                    contentLayout.visibility = View.VISIBLE
                    content.text = data.reasons
                    data.attendee?.let {
                        if (it.isNotEmpty()) {
                            val builder = StringBuilder()
                            val list = if (it.size >= 3) {
                                it.subList(0, 2)
                            } else {
                                it
                            }
                            list.forEach { person ->
                                builder.append(person.name)
                                builder.append(",")
                            }
                            val attend = builder.toString()
                            var substring = attend.substring(0, attend.length - 1)
                            if (it.size > 2) {
                                substring += "..."
                            }
                            attendTv.text = "出席:$substring"
                        } else {
                            attendTv.text = "暂无出席人员"
                        }
                    }
                    data.participant?.let {
                        if (it.isNotEmpty()) {
                            val builder = StringBuilder()
                            val list = if (it.size >= 3) {
                                it.subList(0, 2)
                            } else {
                                it
                            }
                            list.forEach { person ->
                                builder.append(person.name)
                                builder.append(",")
                            }
                            val attend = builder.toString()
                            var substring = attend.substring(0, attend.length - 1)
                            if (it.size > 2) {
                                substring += "..."
                            }
                            participateTv.text = "参加:$substring"
                        } else {
                            participateTv.text = "暂无参加人员"
                        }
                    }
                    if (data.place.isNullOrEmpty()) {
                        addressIcon.isEnabled = false
                        address.hint = "暂无地址信息"
                    } else {
                        addressIcon.isEnabled = true
                        address.text = data.place
                    }
                    view.setOnClickListener {
                        ArrangeDetailActivity.start(activity, data)
                    }
                }
            }
        }

        inner class HeadHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            private val weeklyTv = itemView.find<TextView>(R.id.weeklyTv)
            fun setData(itemView: View) {
                when (offset) {
                    -1 -> {
                        itemView.backgroundResource = R.drawable.bg_last_week
                        weeklyTv.text = "上周"
                    }
                    0 -> {
                        itemView.backgroundResource = R.drawable.bg_this_week
                        weeklyTv.text = "本周"
                        backImg.visibility = View.GONE
                    }
                    1 -> {
                        itemView.backgroundResource = R.drawable.bg_next_week
                        weeklyTv.text = "下周"
                    }
                    else -> {
                        itemView.background = resources.getDrawable(R.color.white)
                        val bean = arrangeAdapter.dataList[1] as WeeklyArrangeBean
                        val firstTime = bean.date
                        val bean1 = arrangeAdapter.dataList[7] as WeeklyArrangeBean
                        val lastTime = bean1.date
                        weeklyTv.text = "${firstTime?.substring(5, firstTime.length)}~${lastTime?.substring(5, lastTime.length)}"
                        itemView.backgroundColor = Color.parseColor("#f7f9fc")
                    }
                }
            }
        }
    }
}
