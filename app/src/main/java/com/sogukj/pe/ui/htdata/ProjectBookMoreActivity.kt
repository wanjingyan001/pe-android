package com.sogukj.pe.ui.htdata

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.text.TextUtils
import android.view.View
import android.widget.TextView
import com.framework.base.ToolbarActivity
import com.lcodecore.tkrefreshlayout.RefreshListenerAdapter
import com.lcodecore.tkrefreshlayout.TwinklingRefreshLayout
import com.lcodecore.tkrefreshlayout.footer.BallPulseView
import com.lcodecore.tkrefreshlayout.header.progresslayout.ProgressLayout
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.bean.ProjectBean
import com.sogukj.pe.bean.ProjectBookBean
import com.sogukj.pe.ui.SupportEmptyView
import com.sogukj.pe.util.DownloadUtil
import com.sogukj.pe.util.OpenFileUtil
import com.sogukj.pe.util.Trace
import com.sogukj.pe.view.RecyclerAdapter
import com.sogukj.pe.view.RecyclerHolder
import com.sogukj.service.SoguApi
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_list_common.*
import java.text.SimpleDateFormat

/**
 * Created by qinfei on 17/8/11.
 */
class ProjectBookMoreActivity : ToolbarActivity() {

    lateinit var adapter: RecyclerAdapter<ProjectBookBean>
    lateinit var project: ProjectBean
    var type = 1
    val df = SimpleDateFormat("yyyy-MM-dd")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        type = intent.getIntExtra(Extras.TYPE, 1)
        project = intent.getSerializableExtra(Extras.DATA) as ProjectBean
        setContentView(R.layout.activity_list_common)
        title = "项目文书"
        setBack(true)
        adapter = RecyclerAdapter<ProjectBookBean>(this, { _adapter, parent, type ->
            val convertView = _adapter.getView(R.layout.item_project_book, parent) as View
            object : RecyclerHolder<ProjectBookBean>(convertView) {
                val tvSummary = convertView.findViewById(R.id.tv_summary) as TextView
                val tvDate = convertView.findViewById(R.id.tv_date) as TextView
                val tvTime = convertView.findViewById(R.id.tv_time) as TextView
                val tvType = convertView.findViewById(R.id.tv_type) as TextView
                val tvName = convertView.findViewById(R.id.tv_name) as TextView
                override fun setData(view: View, data: ProjectBookBean, position: Int) {
                    tvSummary.text = data?.doc_title
                    val strTime = data?.add_time
                    tvTime.visibility = View.GONE
                    if (!TextUtils.isEmpty(strTime)) {
                        val strs = strTime!!.trim().split(" ")
                        if (!TextUtils.isEmpty(strs.getOrNull(1))) {
                            tvTime.visibility = View.VISIBLE
                        }
                        tvDate.text = strs.getOrNull(0)
                        tvTime.text = strs.getOrNull(1)
                    }
                    tvType.text = data?.name
                    if (data?.name.isNullOrEmpty()) {
                        tvType.visibility = View.INVISIBLE
                    }
                    tvName.text = data.submitter
                }

            }
        })
        adapter.onItemClick = { v, p ->
            val data = adapter.getItem(p);
            download(data.url!!, data.doc_title!!)
//            if (!TextUtils.isEmpty(data.url)) {
//                val intent = Intent(Intent.ACTION_VIEW)
//                intent.data = Uri.parse(data.url)
//                startActivity(intent)
//            }
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
        refresh.setEnableLoadmore(false)
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

    fun download(url: String, fileName: String) {
        showToast("开始下载")
        DownloadUtil.getInstance().download(url, externalCacheDir.toString(), fileName, object : DownloadUtil.OnDownloadListener {
            override fun onDownloadSuccess(path: String?) {
                var intent = OpenFileUtil.openFile(path)
                if (intent == null) {
                    showToast("文件类型不合格")
                } else {
                    startActivity(intent)
                }
            }

            override fun onDownloading(progress: Int) {
            }

            override fun onDownloadFailed() {
                showToast("下载失败")
            }
        })
    }

    var page = 1
    fun doRequest() {
        SoguApi.getService(application)
                .projectBookSearch(project.company_id!!, page = page, type = 1, status = type)
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
                        showToast(payload.message)
                }, { e ->
                    Trace.e(e)
                    showToast("暂无可用数据")
                }, {
                    SupportEmptyView.checkEmpty(this, adapter)
                    refresh?.setEnableLoadmore(adapter.dataList.size % 20 == 0)
                    adapter.notifyDataSetChanged()
                    if (page == 1)
                        refresh?.finishRefreshing()
                    else
                        refresh?.finishLoadmore()
                })
    }

    companion object {
        fun start(ctx: Activity?, project: ProjectBean, type: Int) {
            val intent = Intent(ctx, ProjectBookMoreActivity::class.java)
            intent.putExtra(Extras.DATA, project)
            intent.putExtra(Extras.TYPE, type)
            ctx?.startActivity(intent)
        }
    }
}