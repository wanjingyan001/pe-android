package com.sogukj.pe.ui

import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import android.widget.TextView
import com.framework.adapter.RecyclerAdapter
import com.framework.base.BaseFragment
import com.lcodecore.tkrefreshlayout.RefreshListenerAdapter
import com.lcodecore.tkrefreshlayout.TwinklingRefreshLayout
import com.lcodecore.tkrefreshlayout.footer.BallPulseView
import com.lcodecore.tkrefreshlayout.header.progresslayout.ProgressLayout
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.bean.NewsBean
import kotlinx.android.synthetic.main.fragment_list_news.*
import org.jetbrains.anko.find
import java.text.SimpleDateFormat

/**
 * Created by qinfei on 17/7/18.
 */

class ProjectListFragment : BaseFragment() {
    override val containerViewId: Int
        get() = R.layout.fragment_list_project//To change initializer of created properties use File | Settings | File Templates.

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val adapter = RecyclerAdapter<NewsBean>(baseActivity!!, { _adapter, parent, type ->
            NewsHolder(_adapter.getView(R.layout.item_main_project, parent))
        })
        adapter.onItemClick = { v, p ->
            ProjectActivity.start(baseActivity)
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
                handler.postDelayed({
                    adapter.dataList.apply {
                        clear()
                        for (i in 0..10) {
                            add(NewsBean())
                        }
                    }
                    adapter.notifyDataSetChanged()
                    refresh?.finishRefreshing()
                }, 500)
            }

            override fun onLoadMore(refreshLayout: TwinklingRefreshLayout?) {
                handler.postDelayed({
                    adapter.dataList.apply {
                        for (i in 0..10) {
                            add(NewsBean())
                        }
                    }
                    adapter.notifyDataSetChanged()
                    refresh?.finishLoadmore()
                }, 500)
            }

        })
        refresh.setAutoLoadMore(true)
        handler.postDelayed({
            refresh?.startRefresh()
        }, 200)
    }

    val fmt = SimpleDateFormat("MM/dd HH:mm")

    inner class NewsHolder(view: View)
        : RecyclerAdapter.SimpleViewHolder<NewsBean>(view) {
        val tv1: TextView
        val tv2: TextView
        val tv3: TextView

        init {
            tv1 = view.find(R.id.tv1)
            tv2 = view.find(R.id.tv2)
            tv3 = view.find(R.id.tv3)
        }

        override fun setData(view: View, data: NewsBean, position: Int) {
            tv1.text = "搜股(北京)科技有限公司"
            tv2.text = "A轮"
            tv3.text = fmt.format(System.currentTimeMillis())
        }

    }

    companion object {
        val TAG = ProjectListFragment::class.java.simpleName

        fun newInstance(idx: Int): ProjectListFragment {
            val fragment = ProjectListFragment()
            val intent = Bundle()
            intent.putInt(Extras.INDEX, idx)
            fragment.arguments = intent
            return fragment
        }
    }
}