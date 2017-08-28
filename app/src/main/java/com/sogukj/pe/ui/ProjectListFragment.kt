package com.sogukj.pe.ui

import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import android.widget.TextView
import com.framework.base.BaseFragment
import com.lcodecore.tkrefreshlayout.RefreshListenerAdapter
import com.lcodecore.tkrefreshlayout.TwinklingRefreshLayout
import com.lcodecore.tkrefreshlayout.footer.BallPulseView
import com.lcodecore.tkrefreshlayout.header.progresslayout.ProgressLayout
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.adapter.RecyclerAdapter
import com.sogukj.pe.adapter.RecyclerHolder
import com.sogukj.pe.bean.ProjectBean
import com.sogukj.pe.util.Trace
import com.sogukj.service.SoguApi
import com.sogukj.util.Store
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_list_project.*
import org.jetbrains.anko.find
import java.text.SimpleDateFormat

/**
 * Created by qinfei on 17/7/18.
 */

class ProjectListFragment : BaseFragment() {
    override val containerViewId: Int
        get() = R.layout.fragment_list_project//To change initializer of created properties use File | Settings | File Templates.
    lateinit var adapter: RecyclerAdapter<ProjectBean>
    var index: Int = 0
    var type: Int? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        index = arguments.getInt(Extras.INDEX)
        type = when (index) {
            0 -> 2;2 -> 1;else -> 3
        };
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        ll_header2.visibility = if (index == 2) View.VISIBLE else View.GONE
        ll_header.visibility = if (index != 2) View.VISIBLE else View.GONE
        adapter = RecyclerAdapter<ProjectBean>(baseActivity!!, { _adapter, parent, type ->
            if (index != 2)
                ProjectHolder(_adapter.getView(R.layout.item_main_project, parent))
            else
                ProjectHolder(_adapter.getView(R.layout.item_main_project_2, parent))
        })
        adapter.onItemClick = { v, p ->
            val project = adapter.getItem(p);
            ProjectActivity.start(baseActivity, project)
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

    override fun onStart() {
        super.onStart()
        doRequest()
    }

    val fmt = SimpleDateFormat("MM/dd HH:mm")
    var page = 1

    fun doRequest() {
        val user = Store.store.getUser(baseActivity!!)
        if (null != user)
            SoguApi.getService(baseActivity!!.application)
                    .listProject(page = page, type = type, user_id = user!!.uid)
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

    inner class ProjectHolder(view: View)
        : RecyclerHolder<ProjectBean>(view) {

        val tv1: TextView
        val tv2: TextView
        val tv3: TextView

        init {
            tv1 = view.find(R.id.tv1)
            tv2 = view.find(R.id.tv2)
            tv3 = view.find(R.id.tv3)
        }

        override fun setData(view: View, data: ProjectBean, position: Int) {
            tv1.text = data.name
            tv2.text = if (type == 2) data.status else data.state
            tv3.text = data.add_time
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