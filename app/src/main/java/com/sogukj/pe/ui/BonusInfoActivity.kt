package com.sogukj.pe.ui

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import android.widget.TextView
import com.sogukj.pe.adapter.RecyclerAdapter
import com.sogukj.pe.adapter.RecyclerHolder
import com.framework.base.ToolbarActivity
import com.sogukj.pe.util.Trace
import com.lcodecore.tkrefreshlayout.RefreshListenerAdapter
import com.lcodecore.tkrefreshlayout.TwinklingRefreshLayout
import com.lcodecore.tkrefreshlayout.header.progresslayout.ProgressLayout
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.bean.BonusBean
import com.sogukj.pe.bean.ProjectBean
import com.sogukj.service.SoguApi
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_list_news.*
import java.text.SimpleDateFormat

class BonusInfoActivity : ToolbarActivity() {

    lateinit var adapter: RecyclerAdapter<BonusBean>
    lateinit var project: ProjectBean
    val df = SimpleDateFormat("yyyy-MM-dd")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_list_common)

        project = intent.getSerializableExtra(Extras.DATA) as ProjectBean
        setBack(true)
        setTitle("分红情况")

        adapter = RecyclerAdapter<BonusBean>(this, { _adapter, parent, type ->
            val convertView = _adapter.getView(R.layout.item_project_bonus_info, parent) as View
            object : RecyclerHolder<BonusBean>(convertView) {


                val tvTime = convertView.findViewById(R.id.tv_time) as TextView
                val tvBoardDate = convertView.findViewById(R.id.tv_boardDate) as TextView
                val tvShareholderDate = convertView.findViewById(R.id.tv_shareholderDate) as TextView
                val tvImplementationDate = convertView.findViewById(R.id.tv_implementationDate) as TextView
                val tvIntroduction = convertView.findViewById(R.id.tv_introduction) as TextView
                val tvAsharesDate = convertView.findViewById(R.id.tv_asharesDate) as TextView
                val tvAcuxiDate = convertView.findViewById(R.id.tv_acuxiDate) as TextView
                val tvAdividendDate = convertView.findViewById(R.id.tv_adividendDate) as TextView
                val tvProgress = convertView.findViewById(R.id.tv_progress) as TextView
                val tvDividendRate = convertView.findViewById(R.id.tv_dividendRate) as TextView


                override fun setData(view: View, data: BonusBean, position: Int) {
                    tvBoardDate.text = data.boardDate
                    tvShareholderDate.text = data.shareholderDate
                    tvImplementationDate.text = data.implementationDate
                    tvIntroduction.text = data.introduction
                    tvAsharesDate.text = data.asharesDate
                    tvAcuxiDate.text = data.acuxiDate
                    tvAdividendDate.text = data.adividendDate
                    tvProgress.text = data.progress
                    tvDividendRate.text = data.dividendRate
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
                .listBonusInfo(project.company_id!!)
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
            val intent = Intent(ctx, BonusInfoActivity::class.java)
            intent.putExtra(Extras.DATA, project)
            ctx?.startActivity(intent)
        }
    }
}
