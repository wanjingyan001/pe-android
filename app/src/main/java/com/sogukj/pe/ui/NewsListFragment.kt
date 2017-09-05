package com.sogukj.pe.ui

import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
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
import com.sogukj.pe.view.RecyclerAdapter
import com.sogukj.pe.view.RecyclerHolder
import com.sogukj.pe.bean.NewsBean
import com.sogukj.pe.util.Trace
import com.sogukj.pe.view.FlowLayout
import com.sogukj.service.SoguApi
import com.sogukj.util.Store
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_list_news.*
import org.jetbrains.anko.find
import org.jetbrains.anko.textColor
import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by qinfei on 17/7/18.
 */
class NewsListFragment : BaseFragment(), SupportEmptyView {
    override val containerViewId: Int
        get() = R.layout.fragment_list_news //To change initializer of created properties use File | Settings | File Templates.

    lateinit var adapter: RecyclerAdapter<NewsBean>
    var index: Int = 0
    var type: Int? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        index = arguments.getInt(Extras.INDEX)
        type = when (index) {
            0 -> null
            1 -> 3
            2 -> 1
            else -> null
        }
        Store.store.getRead(baseActivity!!)
    }

    fun isRead(data: NewsBean)
            = (Store.store.readList.contains("${KEY_NEWS}${data.data_id}"))

    fun read(data: NewsBean) {
        var readList = HashSet<String>();
        readList.addAll(Store.store.readList)
        readList.add("${KEY_NEWS}${data.data_id}")
        Store.store.setRead(baseActivity!!, readList)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = RecyclerAdapter<NewsBean>(baseActivity!!, { _adapter, parent, type ->
            NewsHolder(_adapter.getView(R.layout.item_main_news, parent))
        })
        adapter.onItemClick = { v, p ->
            val news = adapter.getItem(p);
            NewsDetailActivity.start(baseActivity, news)
            read(news)
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
            doRequest()
        }, 100)
    }

//    fun onItemClick(news: NewsBean) {
//        when (news.table_id) {
//            else -> NewsDetailActivity.start(baseActivity)
//        }
//    }

    override fun onStart() {
        super.onStart()
        doRequest()
    }


    var page = 1
    fun doRequest() {
        val user = Store.store.getUser(baseActivity!!)
        val userId = if (index == 0) null else user?.uid
        SoguApi.getService(baseActivity!!.application)
                .listNews(page = page, type = type, uid = userId)
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
    val KEY_NEWS = "news"

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
            tags.removeAllViews()
            data.tag?.split("#")
                    ?.forEach { str ->
                        if (!TextUtils.isEmpty(str)) {
                            val itemRes = when (str!!) {
                                "财务风险", "坏账增加", "经营风险",
                                "法律风险", "财务造假", "诉讼判决",
                                "违规违法"
                                -> R.layout.item_tag_news_1
                                "负面", "业绩不佳", "市场份额下降",
                                "企业风险", "系统风险", "操作风险",
                                "技术风险"
                                -> R.layout.item_tag_news_2
                                "股权转让", "人事变动", "内部重组"
                                    , "股权出售", "质押担保", "行业企业重大事件"
                                -> R.layout.item_tag_news_3
                                else -> R.layout.item_tag_news_4
                            }
                            val itemTag = View.inflate(baseActivity, itemRes, null)
                            val tvTag = itemTag.find<TextView>(R.id.tv_tag)
                            tvTag.text = str
                            tags.addView(itemTag)
                        }
                    }

            var isRead = isRead(data)
            if (isRead) {
                tv_summary.textColor = resources.getColor(R.color.text_3)
                tv_time.textColor = resources.getColor(R.color.text_3)
                tv_from.textColor = resources.getColor(R.color.text_3)
            } else {
                tv_summary.textColor = resources.getColor(R.color.text_1)
                tv_time.textColor = resources.getColor(R.color.text_2)
                tv_from.textColor = resources.getColor(R.color.text_2)
            }

        }

    }

    companion object {
        val TAG = NewsListFragment::class.java.simpleName

        fun newInstance(idx: Int): NewsListFragment {
            val fragment = NewsListFragment()
            val intent = Bundle()
            intent.putInt(Extras.INDEX, idx)
            fragment.arguments = intent
            return fragment
        }
    }
}