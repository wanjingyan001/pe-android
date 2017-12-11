package com.sogukj.pe.ui.calendar


import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.View
import android.widget.CompoundButton
import com.framework.base.BaseFragment
import com.google.gson.Gson
import com.lcodecore.tkrefreshlayout.RefreshListenerAdapter
import com.lcodecore.tkrefreshlayout.TwinklingRefreshLayout
import com.lcodecore.tkrefreshlayout.footer.BallPulseView
import com.lcodecore.tkrefreshlayout.header.progresslayout.ProgressLayout
import com.sogukj.pe.R
import com.sogukj.pe.util.Trace
import com.sogukj.service.SoguApi
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_task.*
import kotlinx.android.synthetic.main.layout_empty.*
import org.jetbrains.anko.support.v4.find
import org.jetbrains.anko.textColor


/**
 * A simple [Fragment] subclass.
 * Use the [TaskFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class TaskFragment : BaseFragment(), View.OnClickListener, TaskFilterWindow.FilterItemClickListener, ScheduleItemClickListener {


    override val containerViewId: Int
        get() = R.layout.fragment_task
    lateinit var taskAdapter: TaskAdapter
    val data = ArrayList<Any>()

    private var mParam1: String? = null
    private var mParam2: String? = null
    var page = 1
    var range = "w"
    var isFinish = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (arguments != null) {
            mParam1 = arguments.getString(ARG_PARAM1)
            mParam2 = arguments.getString(ARG_PARAM2)
        }
    }


    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initAdapter()
        doRequest(page, range, isFinish)
        dateFilter.setOnClickListener(this)
        taskFilter.setOnClickListener(this)
    }


    private fun initAdapter() {
        taskAdapter = TaskAdapter(context, data)
        taskAdapter.setListener(this)

        taskList.layoutManager = LinearLayoutManager(context)
        taskList.adapter = taskAdapter
        val header = ProgressLayout(context)
        header.setColorSchemeColors(ContextCompat.getColor(context, R.color.color_main))
        refresh.setHeaderView(header)
        val footer = BallPulseView(context)
        footer.setAnimatingColor(ContextCompat.getColor(context, R.color.color_main))
        refresh.setBottomView(footer)
        refresh.setOverScrollRefreshShow(false)
        refresh.setEnableLoadmore(true)
        refresh.setOnRefreshListener(object : RefreshListenerAdapter() {
            override fun onRefresh(refreshLayout: TwinklingRefreshLayout?) {
                page = 1
                doRequest(page, range, isFinish)
            }

            override fun onLoadMore(refreshLayout: TwinklingRefreshLayout?) {
                ++page
                doRequest(page, range, isFinish)
            }

        })

    }

    fun doRequest(page: Int, range: String, isFinish: String) {
        SoguApi.getService(activity.application)
                .showTask(page = page, range = range, is_finish = isFinish)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ payload ->
                    if (payload.isOk) {
                        Log.d("WJY", Gson().toJson(payload.payload))
                        payload.payload.let {
                            data.clear()
                            val dayList = ArrayList<String>()
                            it?.forEachIndexed { index, taskItemBean ->
                                val day = taskItemBean.end_time.split(" ")[0]
                                if (!dayList.contains(day)) {
                                    dayList.add(day)
                                }
                            }
                            dayList.forEach {
                                data.add(TodoDay(it))
                                payload.payload?.forEachIndexed { index, taskItemBean ->
                                    val day = taskItemBean.end_time.split(" ")[0]
                                    if (day == it) {
                                        data.add(taskItemBean)
                                    }
                                }
                            }
                        }

                    } else {
                        showToast(payload.message)
                    }
                }, { e ->
                    Trace.e(e)
                }, {
                    if (data.size == 0) {
                        iv_empty.visibility = View.VISIBLE
                    } else {
                        iv_empty.visibility = View.GONE
                    }
                    refresh?.setEnableLoadmore((data.size - 1) % 20 == 0)
                    taskAdapter.notifyDataSetChanged()
                    if (page == 1) {
                        refresh?.finishRefreshing()
                    } else {
                        refresh?.finishLoadmore()
                    }
                })

    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.dateFilter -> {
                dateFilterTv.textColor = resources.getColor(R.color.color_main)
                val dateFilters = arrayListOf("一周内", "一月内", "一年内")
                val dateWindow = TaskFilterWindow(context, dateFilters, this, "date")
                dateWindow.setOnDismissListener {
                    dateFilterTv.textColor = resources.getColor(R.color.text_3)
                    arrow1.visibility = View.VISIBLE
                    arrow3.visibility = View.GONE
                }
                dateWindow.showAsDropDown(find(R.id.filterConditionLayout))
                arrow1.visibility = View.GONE
                arrow3.visibility = View.VISIBLE
            }
            R.id.taskFilter -> {
                taskFilterTv.textColor = resources.getColor(R.color.color_main)
                val taskFilters = arrayListOf("未完成任务", "已完成任务", "全部任务")
                val taskWindow = TaskFilterWindow(context, taskFilters, this, "task")
                taskWindow.setOnDismissListener {
                    taskFilterTv.textColor = resources.getColor(R.color.text_3)
                    arrow2.visibility = View.VISIBLE
                    arrow4.visibility = View.GONE
                }
                taskWindow.showAsDropDown(find(R.id.filterConditionLayout))
                arrow2.visibility = View.GONE
                arrow4.visibility = View.VISIBLE
            }

        }
    }

    override fun onItemClick(view: View, position: Int) {
        when (view.id) {
            R.id.taskItemLayout -> {
                val bean = data[position] as TaskItemBean
                TaskDetailActivity.start(activity, bean)
            }
        }
    }

    override fun finishCheck(buttonView: CompoundButton, isChecked: Boolean, position: Int) {
        val bean = data[position] as TaskItemBean
        bean.is_finish = if (isChecked) 1 else 0
        taskAdapter.notifyItemChanged(position)
    }

    override fun itemClick(view: View?, position: Int, filter: String?) {
        when (view?.tag) {
            "date" -> {
                dateFilterTv.text = filter
                when (filter) {
                    "一周内" -> {
                        range = "w"
                    }
                    "一月内" -> {
                        range = "m"
                    }
                    "一年内" -> {
                        range = "y"
                    }
                }

            }
            "task" -> {
                taskFilterTv.text = filter
                when (filter) {
                    "未完成任务" -> {
                        isFinish = "0"
                    }
                    "已完成任务" -> {
                        isFinish = "1"
                    }
                    "全部任务" -> {
                        isFinish = ""
                    }
                }
            }
        }
        doRequest(page, range, isFinish)
    }


    companion object {
        private val ARG_PARAM1 = "param1"
        private val ARG_PARAM2 = "param2"
        val TAG = TaskFragment::class.java.simpleName

        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment TaskFragment.
         */
        fun newInstance(param1: String, param2: String): TaskFragment {
            val fragment = TaskFragment()
            val args = Bundle()
            args.putString(ARG_PARAM1, param1)
            args.putString(ARG_PARAM2, param2)
            fragment.arguments = args
            return fragment
        }
    }

}