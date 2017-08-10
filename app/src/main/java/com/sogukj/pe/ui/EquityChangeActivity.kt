package com.sogukj.pe.ui

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import android.widget.TextView
import com.framework.adapter.RecyclerAdapter
import com.framework.adapter.RecyclerHolder
import com.framework.base.ToolbarActivity
import com.framework.util.Trace
import com.lcodecore.tkrefreshlayout.RefreshListenerAdapter
import com.lcodecore.tkrefreshlayout.TwinklingRefreshLayout
import com.lcodecore.tkrefreshlayout.header.progresslayout.ProgressLayout
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.bean.EquityChangeBean
import com.sogukj.pe.bean.ProjectBean
import com.sogukj.service.SoguApi
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_list_news.*
import java.text.SimpleDateFormat

class EquityChangeActivity : ToolbarActivity() {

    lateinit var adapter: RecyclerAdapter<EquityChangeBean>
    lateinit var project: ProjectBean
    val df = SimpleDateFormat("yyyy-MM-dd")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_list_common)

        project = intent.getSerializableExtra(Extras.DATA) as ProjectBean
        setBack(true)
        setTitle("股本变动")
        adapter = RecyclerAdapter<EquityChangeBean>(this, { _adapter, parent, type ->
            val convertView = _adapter.getView(R.layout.item_project_equity_change, parent) as View
            object : RecyclerHolder<EquityChangeBean>(convertView) {

                var tvTime = convertView.findViewById(R.id.tv_time) as TextView
                var tvTotal = convertView.findViewById(R.id.tv_total) as TextView
                var tvCirculate = convertView.findViewById(R.id.tv_circulate) as TextView
                var tvLimitSales = convertView.findViewById(R.id.tv_limit_sales) as TextView
                var tvReason = convertView.findViewById(R.id.tv_reason) as TextView

                override fun setData(view: View, data: EquityChangeBean, position: Int) {
                    tvTime.text = df.format(data.changeDate)
                    tvTotal.text = data.afterAll
                    tvCirculate.text = data.afterNoLimit
                    tvLimitSales.text = data.afterLimit
                    tvReason.text = data.changeReason
                }

            }
        })
        adapter.onItemClick = { v, p ->
        }
        val layoutManager = LinearLayoutManager(this)
        layoutManager.orientation = LinearLayoutManager.VERTICAL
        recycler_view.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))
        recycler_view.layoutManager = layoutManager
        recycler_view.adapter = adapter

        val header = ProgressLayout(this)
        header.setColorSchemeColors(ContextCompat.getColor(this, R.color.color_main))
        refresh.setHeaderView(header)
//        val footer = BallPulseView(this)
//        footer.setAnimatingColor(ContextCompat.getColor(this, R.color.color_main))
//        refresh.setBottomView(footer)
        refresh.setOverScrollRefreshShow(false)
        refresh.setEnableLoadmore(false)
        refresh.setOnRefreshListener(object : RefreshListenerAdapter() {
            override fun onRefresh(refreshLayout: TwinklingRefreshLayout?) {
                doRequest()
            }

            override fun onLoadMore(refreshLayout: TwinklingRefreshLayout?) {
                super.onLoadMore(refreshLayout)
            }

        })
        refresh.setAutoLoadMore(false)
        handler.postDelayed({
            doRequest()
        }, 100)
    }

    fun doRequest() {
        SoguApi.getService(application)
                .equityChange(project.company_id!!)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ payload ->
                    adapter.dataList.clear()
                    if (payload.isOk) {
                        payload.payload?.apply {
                            adapter.dataList.addAll(this)
                        }
                    } else {
                        showToast(payload.message)
                    }
                }, { e ->
                    Trace.e(e)
                }, {
                    adapter.notifyDataSetChanged()
                    refresh?.finishRefreshing()
                })
    }

    companion object {
        fun start(ctx: Activity?, project: ProjectBean) {
            val intent = Intent(ctx, EquityChangeActivity::class.java)
            intent.putExtra(Extras.DATA, project)
            ctx?.startActivity(intent)
        }
    }
}
