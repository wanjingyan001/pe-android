package com.sogukj.pe.ui

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.framework.base.ToolbarActivity
import com.lcodecore.tkrefreshlayout.RefreshListenerAdapter
import com.lcodecore.tkrefreshlayout.TwinklingRefreshLayout
import com.lcodecore.tkrefreshlayout.footer.BallPulseView
import com.lcodecore.tkrefreshlayout.header.progresslayout.ProgressLayout
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.view.RecyclerAdapter
import com.sogukj.pe.view.RecyclerHolder
import com.sogukj.pe.bean.BrandBean
import com.sogukj.pe.bean.ProjectBean
import com.sogukj.pe.util.Trace
import com.sogukj.service.SoguApi
import com.sogukj.pe.util.Utils
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_list_common.*
import java.text.SimpleDateFormat

/**
 * Created by qinfei on 17/8/11.
 */
class BrandListActivity : ToolbarActivity(),SupportEmptyView {

    lateinit var adapter: RecyclerAdapter<BrandBean>
    lateinit var project: ProjectBean
    val df = SimpleDateFormat("yyyy-MM-dd")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_list_common)

        project = intent.getSerializableExtra(Extras.DATA) as ProjectBean
        setBack(true)
        setTitle("商标信息")

        adapter = RecyclerAdapter<BrandBean>(this, { _adapter, parent, type ->
            val convertView = _adapter.getView(R.layout.item_project_brand, parent) as View
            object : RecyclerHolder<BrandBean>(convertView) {

                val ivLogo = convertView.findViewById(R.id.iv_logo) as ImageView
                val tvTmName = convertView.findViewById(R.id.tv_tmName) as TextView
                val tvRegNo = convertView.findViewById(R.id.tv_regNo) as TextView
                val tvAppdate = convertView.findViewById(R.id.tv_appdate) as TextView
                val tvStatus = convertView.findViewById(R.id.tv_status) as TextView
                val tvIntCls = convertView.findViewById(R.id.tv_intCls) as TextView
                override fun setData(view: View, data: BrandBean, position: Int) {
                    tvTmName.text = "${data.tmName}"
                    tvRegNo.text = "注册号:${data.regNo}"
                    tvAppdate.text = "申请时间:${data.appdate}"
                    tvStatus.text = "申请状态:${data.status}"
                    tvIntCls.text = "类别:${data.intCls}"
                    Glide.with(this@BrandListActivity)
                            .load(data.tmPic)
                            .apply(RequestOptions().error(Utils.defaultHeadImg()))
                            .into(ivLogo)
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
        val footer = BallPulseView(this)
        footer.setAnimatingColor(ContextCompat.getColor(this, R.color.color_main))
        refresh.setBottomView(footer)
        refresh.setOverScrollRefreshShow(false)
        refresh.setEnableLoadmore(true)
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
        handler.postDelayed({
            doRequest()
        }, 100)
    }

    var page = 1
    fun doRequest() {
        SoguApi.getService(application)
                .listBrand(project.company_id!!, page = page)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ payload ->
                    if (payload.isOk) {
                        if (page == 1)
                            adapter.dataList.clear()
                        payload.payload?.apply {
                            adapter.dataList.addAll(this)
                        }
                    } else
                        showCustomToast(R.drawable.icon_toast_fail, payload.message)
                }, { e ->
                    Trace.e(e)
                    showCustomToast(R.drawable.icon_toast_common, "暂无可用数据")
                }, {
                    SupportEmptyView.checkEmpty(this,adapter)
                    refresh?.setEnableLoadmore(adapter.dataList.size % 20 == 0)
                    adapter.notifyDataSetChanged()
                    if (page == 1)
                        refresh?.finishRefreshing()
                    else
                        refresh?.finishLoadmore()
                })
    }

    companion object {
        fun start(ctx: Activity?, project: ProjectBean) {
            val intent = Intent(ctx, BrandListActivity::class.java)
            intent.putExtra(Extras.DATA, project)
            ctx?.startActivity(intent)
        }
    }
}
