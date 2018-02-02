package com.sogukj.pe.ui.weekly


import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.LinearLayout
import android.widget.TextView
import com.bigkoo.pickerview.TimePickerView
import com.framework.base.BaseFragment
import com.google.gson.JsonSyntaxException
import com.lcodecore.tkrefreshlayout.RefreshListenerAdapter
import com.lcodecore.tkrefreshlayout.TwinklingRefreshLayout
import com.lcodecore.tkrefreshlayout.footer.BallPulseView
import com.lcodecore.tkrefreshlayout.header.progresslayout.ProgressLayout
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.bean.WeeklySendBean
import com.sogukj.pe.util.Trace
import com.sogukj.pe.view.MyGridView
import com.sogukj.pe.view.RecyclerAdapter
import com.sogukj.pe.view.RecyclerHolder
import com.sogukj.service.SoguApi
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_weekly_isend.*
import kotlinx.android.synthetic.main.layout_network_error.*
import java.net.UnknownHostException
import java.text.SimpleDateFormat
import java.util.*


/**
 * A simple [Fragment] subclass.
 */
class WeeklyISendFragment : BaseFragment() {

    override val containerViewId: Int
        get() = R.layout.fragment_weekly_isend

    lateinit var adapter: RecyclerAdapter<WeeklySendBean>
    var format = SimpleDateFormat("yyyy-MM-dd")

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = RecyclerAdapter(context, { _adapter, parent, type ->
            val convertView = _adapter.getView(R.layout.item_week_send, parent) as LinearLayout
            object : RecyclerHolder<WeeklySendBean>(convertView) {
                val tv_title = convertView.findViewById(R.id.title_date) as TextView
                val grid = convertView.findViewById(R.id.grid_list) as MyGridView
                override fun setData(view: View, data: WeeklySendBean, position: Int) {
                    tv_title.text = data.date
                    data.data?.let {
                        var adapter = MyAdapter(context, it)
                        grid.adapter = adapter
                        adapter.notifyDataSetChanged()
                        grid.setOnItemClickListener { parent, view, position, id ->
                            val sendBeanObj = it[position]
                            val intent = Intent(context, PersonalWeeklyActivity::class.java)
                            intent.putExtra(Extras.ID, sendBeanObj.week_id)
                            intent.putExtra(Extras.NAME, "My")
                            intent.putExtra(Extras.TIME1, sendBeanObj.start_time)
                            intent.putExtra(Extras.TIME2, sendBeanObj.end_time)
                            activity.startActivity(intent)
                        }
                    }
                }
            }
        })
        adapter.onItemClick = { v, p ->
        }
        val layoutManager = LinearLayoutManager(context)
        layoutManager.orientation = LinearLayoutManager.VERTICAL
        list.layoutManager = layoutManager
        list.adapter = adapter

        var calendar = Calendar.getInstance()

        start.text = ""//formatTime(startBean)
        end.text = ""//formatTime(endBean)

        start.setOnClickListener {
            if (start.text != "") {
                calendar.time = format.parse(start.text.toString())
            } else {
                calendar = Calendar.getInstance()
            }
            val timePicker = TimePickerView.Builder(context, { date, view ->
                if (end.text.trim() == "") {
                    start.text = format.format(date)
                    return@Builder
                }

                var startBean = date
                var endBean = format.parse(end.text.toString())
                if (startBean.compareTo(endBean) > 0) {
                    showToast("日期选择错误")
                    return@Builder
                }

                start.text = format.format(date)

                adapter.dataList.clear()
                adapter.notifyDataSetChanged()
                doRequest()
            })
                    //年月日时分秒 的显示与否，不设置则默认全部显示
                    .setType(booleanArrayOf(true, true, true, false, false, false))
                    .setDividerColor(Color.DKGRAY)
                    .setContentSize(20)
                    .setDate(calendar)
                    .setCancelColor(resources.getColor(R.color.shareholder_text_gray))
                    .build()
            timePicker.show()
        }

        end.setOnClickListener {
            if (end.text != "") {
                calendar.time = format.parse(end.text.toString())
            } else {
                calendar = Calendar.getInstance()
            }
            val timePicker = TimePickerView.Builder(context, { date, view ->
                if (start.text.trim() == "") {
                    end.text = format.format(date)
                    return@Builder
                }

                var startBean = format.parse(start.text.toString())
                var endBean = date
                if (startBean.compareTo(endBean) > 0) {
                    showToast("日期选择错误")
                    return@Builder
                }

                end.text = format.format(date)

                adapter.dataList.clear()
                adapter.notifyDataSetChanged()
                doRequest()
            })
                    //年月日时分秒 的显示与否，不设置则默认全部显示
                    .setType(booleanArrayOf(true, true, true, false, false, false))
                    .setDividerColor(Color.DKGRAY)
                    .setContentSize(20)
                    .setDate(calendar)
                    .setCancelColor(resources.getColor(R.color.shareholder_text_gray))
                    .build()
            timePicker.show()
        }

        val header = ProgressLayout(baseActivity)
        header.setColorSchemeColors(ContextCompat.getColor(baseActivity, R.color.color_main))
        refresh.setHeaderView(header)
        val footer = BallPulseView(baseActivity)
        footer.setAnimatingColor(ContextCompat.getColor(baseActivity, R.color.color_main))
        refresh.setBottomView(footer)
        refresh.setOverScrollRefreshShow(false)
        refresh.setOnRefreshListener(object : RefreshListenerAdapter() {
            override fun onRefresh(refreshLayout: TwinklingRefreshLayout?) {
                page = 1
                doRequest()
            }

            override fun onLoadMore(refreshLayout: TwinklingRefreshLayout?) {
                ++page
                doRequest()
            }

        })
        refresh.setAutoLoadMore(true)

        doRequest()
    }

    var page = 1
    var pageSize = 5

    fun doRequest() {
        SoguApi.getService(baseActivity!!.application)
                .send(page, pageSize, start.text.toString(), end.text.toString())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ payload ->
                    if (payload.isOk) {
                        jsSendLayout.visibility = View.VISIBLE
                        networkErrorLayout.visibility = View.GONE
                        if (page == 1) {
                            adapter.dataList.clear()
                        }
                        payload.payload?.apply {
                            adapter.dataList.addAll(this)
                            //adapter.notifyDataSetChanged()
                        }
                    } else
                        showToast(payload.message)
                }, { e ->
                    Trace.e(e)
                    ToastError(e)
                    when (e) {
                        is JsonSyntaxException -> showToast("后台数据出错")
                        is UnknownHostException -> {
                            showToast("网络出错")
                            jsSendLayout.visibility = View.GONE
                            networkErrorLayout.visibility = View.VISIBLE
                            resetRefresh.setOnClickListener{
                                doRequest()
                            }
                        }
                        else -> showToast("未知错误")
                    }
                }, {
                    refresh.setEnableLoadmore(adapter.dataList.size % pageSize == 0)
                    adapter.notifyDataSetChanged()
                    if (page == 1)
                        refresh.finishRefreshing()
                    else
                        refresh.finishLoadmore()
                })
    }

    class MyAdapter(var context: Context, val list: ArrayList<WeeklySendBean.WeeklySendBeanObj>) : BaseAdapter() {


        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            var conView = convertView
            if (conView == null) {
                conView = LayoutInflater.from(context).inflate(R.layout.senditem, null)
            }
            (conView as TextView).text = list.get(position).week
            return conView
        }

        override fun getItem(position: Int): Any {
            return list.get(position)
        }

        override fun getItemId(position: Int): Long {
            return position.toLong()
        }

        override fun getCount(): Int {
            return list.size
        }
    }
}
