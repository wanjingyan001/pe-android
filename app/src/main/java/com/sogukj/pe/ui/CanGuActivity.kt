package com.sogukj.pe.ui

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.text.Html
import android.view.View
import android.widget.TextView
import com.framework.base.ToolbarActivity
import com.lcodecore.tkrefreshlayout.RefreshListenerAdapter
import com.lcodecore.tkrefreshlayout.TwinklingRefreshLayout
import com.lcodecore.tkrefreshlayout.header.progresslayout.ProgressLayout
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.adapter.RecyclerAdapter
import com.sogukj.pe.adapter.RecyclerHolder
import com.sogukj.pe.bean.CanGuBean
import com.sogukj.pe.bean.ProjectBean
import com.sogukj.pe.util.Trace
import com.sogukj.service.SoguApi
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_list_common.*

class CanGuActivity : ToolbarActivity(),SupportEmptyView {

    lateinit var adapter: RecyclerAdapter<CanGuBean>
    lateinit var project: ProjectBean
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_list_common)

        project = intent.getSerializableExtra(Extras.DATA) as ProjectBean
        setBack(true)
        setTitle("参股控股")
        adapter = RecyclerAdapter<CanGuBean>(this, { _adapter, parent, type ->
            val convertView = _adapter.getView(R.layout.item_project_cangukonggu, parent) as View
            object : RecyclerHolder<CanGuBean>(convertView) {
                var tvName = convertView.findViewById(R.id.tv_name) as TextView
                var tvRelation = convertView.findViewById(R.id.tv_relation) as TextView
                var tvPercent = convertView.findViewById(R.id.tv_percent) as TextView
                var tvTouzijine = convertView.findViewById(R.id.tv_touzijine) as TextView
                var tvJinlirun = convertView.findViewById(R.id.tv_jinlirun) as TextView
                var tvIsMerge = convertView.findViewById(R.id.tv_is_merge) as TextView
                var tvBis = convertView.findViewById(R.id.tv_bis) as TextView


                override fun setData(view: View, data: CanGuBean, position: Int) {
                    tvName.text = data.name
                    tvRelation.text = Html.fromHtml(getString(R.string.tv_project_cangu_relation, data.relationship))
                    tvPercent.text = Html.fromHtml(getString(R.string.tv_project_cangu_percent, String.format("%.2f", data.participationRatio)))
                    tvTouzijine.text = Html.fromHtml(getString(R.string.tv_project_cangu_touzijine, data.investmentAmount))
                    tvJinlirun.text = Html.fromHtml(getString(R.string.tv_project_cangu_jinlirun, data.profit))
                    tvIsMerge.text = Html.fromHtml(getString(R.string.tv_project_cangu_is_merge, data.reportMerge))
                    tvBis.text = Html.fromHtml(getString(R.string.tv_project_cangu_bis, data.mainBusiness))
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
                .cangu(project.company_id!!)
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
                    SupportEmptyView.checkEmpty(this,adapter)
                    adapter.notifyDataSetChanged()
                    refresh?.finishRefreshing()
                })
    }


    companion object {
        fun start(ctx: Activity?, project: ProjectBean) {
            val intent = Intent(ctx, CanGuActivity::class.java)
            intent.putExtra(Extras.DATA, project)
            ctx?.startActivity(intent)
        }
    }
}
