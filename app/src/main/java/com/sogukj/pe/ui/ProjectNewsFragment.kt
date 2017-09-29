package com.sogukj.pe.ui

import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.text.Html
import android.text.TextUtils
import android.view.View
import android.widget.TextView
import com.framework.base.BaseFragment
import com.lcodecore.tkrefreshlayout.RefreshListenerAdapter
import com.lcodecore.tkrefreshlayout.TwinklingRefreshLayout
import com.lcodecore.tkrefreshlayout.footer.BallPulseView
import com.lcodecore.tkrefreshlayout.header.progresslayout.ProgressLayout
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.bean.NewsBean
import com.sogukj.pe.bean.ProjectBean
import com.sogukj.pe.util.Trace
import com.sogukj.pe.view.FlowLayout
import com.sogukj.pe.view.RecyclerAdapter
import com.sogukj.pe.view.RecyclerHolder
import com.sogukj.service.SoguApi
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_list_project_news.*
import java.text.SimpleDateFormat

/**
 * Created by qinfei on 17/7/18.
 */
class ProjectNewsFragment : BaseFragment(), SupportEmptyView {
    override val containerViewId: Int
        get() = R.layout.fragment_list_project_news //To change initializer of created properties use File | Settings | File Templates.

    lateinit var adapter: RecyclerAdapter<NewsBean>
    var type: Int = 1
    lateinit var project: ProjectBean
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        project = arguments.getSerializable(Extras.DATA) as ProjectBean
        type = arguments.getInt(Extras.TYPE)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        adapter = RecyclerAdapter<NewsBean>(baseActivity!!, { _adapter, parent, type ->
            NewsHolder(_adapter.getView(R.layout.item_main_news, parent))
        })
        adapter.onItemClick = { v, p ->
            val news = adapter.getItem(p);
            NewsDetailActivity.start(baseActivity, news)
        }
        val layoutManager = LinearLayoutManager(baseActivity)
        layoutManager.orientation = LinearLayoutManager.VERTICAL
        recycler_view.addItemDecoration(DividerItemDecoration(baseActivity, DividerItemDecoration.VERTICAL))
        recycler_view.layoutManager = layoutManager
        recycler_view.adapter = adapter

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
        handler.postDelayed({
            refresh?.startRefresh()
        }, 100)
    }

//    fun onItemClick(news: NewsBean) {
//        when (news.table_id) {
//            else -> NewsDetailActivity.start(baseActivity)
//        }
//    }

    var page = 1
    fun doRequest() {
        SoguApi.getService(baseActivity!!.application)
                .listNews(pageSize = 20, page = page, type = type, company_id = project.company_id)
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

    val fmt = SimpleDateFormat("yyyy/MM/dd HH:mm")

    inner class NewsHolder(view: View)
        : RecyclerHolder<NewsBean>(view) {
        val tv_summary = convertView.findViewById(R.id.tv_summary) as TextView
        val tv_time = convertView.findViewById(R.id.tv_time) as TextView
        val tv_from = convertView.findViewById(R.id.tv_from) as TextView
        val tags = convertView.findViewById(R.id.tags) as FlowLayout
        val tv_date = convertView.findViewById(R.id.tv_date) as TextView


        override fun setData(view: View, data: NewsBean, position: Int) {
            var label = data.title
//            if (!TextUtils.isEmpty(label) && !TextUtils.isEmpty(key)) {
//                label = label!!.replaceFirst(key, "<font color='#ff3300'>${key}</font>")
//            }
            tv_summary.text = Html.fromHtml(label)
            val strTime = data.time
            tv_time.visibility = View.GONE
            if (!TextUtils.isEmpty(strTime)) {
                val strs = strTime!!.trim().split(" ")
                if (!TextUtils.isEmpty(strs.getOrNull(1))) {
                    tv_time.visibility = View.VISIBLE
                }
                tv_date.text = strs
                        .getOrNull(0)
                tv_time.text = strs
                        .getOrNull(1)
            }
            tv_from.text = data.source
            data.setTags(baseActivity!!, tags)
        }

    }

    companion object {
        val TAG = NewsListFragment::class.java.simpleName

        fun newInstance(project: ProjectBean, type: Int = 1): ProjectNewsFragment {
            val fragment = ProjectNewsFragment()
            val intent = Bundle()
            intent.putSerializable(Extras.DATA, project)
            intent.putInt(Extras.TYPE, type)
            fragment.arguments = intent
            return fragment
        }
    }
}