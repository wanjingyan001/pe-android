package com.sogukj.pe.ui

import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import android.widget.TextView
import com.sogukj.pe.adapter.RecyclerAdapter
import com.sogukj.pe.adapter.RecyclerHolder
import com.framework.base.BaseFragment
import com.sogukj.pe.util.Trace
import com.lcodecore.tkrefreshlayout.RefreshListenerAdapter
import com.lcodecore.tkrefreshlayout.TwinklingRefreshLayout
import com.lcodecore.tkrefreshlayout.footer.BallPulseView
import com.lcodecore.tkrefreshlayout.header.progresslayout.ProgressLayout
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.bean.NewsBean
import com.sogukj.pe.bean.ProjectBean
import com.sogukj.pe.view.FlowLayout
import com.sogukj.service.SoguApi
import com.sogukj.util.Store
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_list_news.*
import org.jetbrains.anko.find
import java.text.SimpleDateFormat

/**
 * Created by qinfei on 17/7/18.
 */
class ProjectNewsFragment : BaseFragment() {
    override val containerViewId: Int
        get() = R.layout.fragment_list_project_news //To change initializer of created properties use File | Settings | File Templates.

    lateinit var adapter: RecyclerAdapter<NewsBean>
    var index: Int = 0
    var type: Int? = null
    lateinit var project: ProjectBean
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        project = arguments.getSerializable(Extras.DATA) as ProjectBean
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
        val user = Store.store.getUser(baseActivity!!)
        val userId = if (index == 0) null else user?.uid;
        SoguApi.getService(baseActivity!!.application)
                .listNews(page = page, type = 1, company_id = project.company_id)
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
        val tv_summary: TextView
        val tv_time: TextView
        val tv_from: TextView
        val tags: FlowLayout

        init {
            tv_summary = view.find(R.id.tv_summary)
            tv_time = view.find(R.id.tv_time)
            tv_from = view.find(R.id.tv_from)
            tags = view.find(R.id.tags)
        }

        override fun setData(view: View, data: NewsBean, position: Int) {
            tv_summary.text = data.title
            tv_time.text = data.time
            tv_from.text = data.source

        }

    }

    companion object {
        val TAG = NewsListFragment::class.java.simpleName

        fun newInstance(project: ProjectBean): NewsListFragment {
            val fragment = NewsListFragment()
            val intent = Bundle()
            intent.putSerializable(Extras.DATA, project)
            fragment.arguments = intent
            return fragment
        }
    }
}