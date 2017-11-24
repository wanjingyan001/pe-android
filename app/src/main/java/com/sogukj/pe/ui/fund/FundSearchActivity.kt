package com.sogukj.pe.ui.fund

import android.content.Context
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.text.Html
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.TextView
import com.framework.base.ToolbarActivity
import com.google.gson.Gson
import com.lcodecore.tkrefreshlayout.RefreshListenerAdapter
import com.lcodecore.tkrefreshlayout.TwinklingRefreshLayout
import com.lcodecore.tkrefreshlayout.footer.BallPulseView
import com.lcodecore.tkrefreshlayout.header.progresslayout.ProgressLayout
import com.sogukj.pe.R
import com.sogukj.pe.bean.FundSmallBean
import com.sogukj.pe.ui.SupportEmptyView
import com.sogukj.pe.ui.user.UserActivity
import com.sogukj.pe.util.Trace
import com.sogukj.pe.view.RecyclerAdapter
import com.sogukj.pe.view.RecyclerHolder
import com.sogukj.service.SoguApi
import com.sogukj.util.Store
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_fund_search.*
import org.jetbrains.anko.find
import org.jetbrains.anko.imageResource
import java.util.*

class FundSearchActivity : ToolbarActivity(), View.OnClickListener {

    lateinit var historyAdapter: RecyclerAdapter<String>
    lateinit var adapter: RecyclerAdapter<FundSmallBean>
    private var page = 0
    private var currentNameOrder = FundSmallBean.FundDesc
    private var currentTimeOrder = FundSmallBean.RegTimeAsc
    private var searchStr: String = ""

    companion object {
        val TAG = FundSearchActivity::class.java.simpleName

        fun start(ctx: Context?) {
            ctx?.startActivity(Intent(ctx, FundSearchActivity::class.java))
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fund_search)
        initHistoryAdapter()
        initResultAdapter()
        tv_result_title.text = Html.fromHtml(getString(R.string.tv_title_result_news, 0))
        search_view.onTextChange = { text ->
            if (text.isEmpty()) {
                ll_history.visibility = View.VISIBLE
            } else {
                searchStr = search_view.search
                page = 0
                handler.postDelayed({ doSearch(searchStr) }, 500)
            }
        }
        run {
            ll_order_name_1.setOnClickListener(this)
            ll_order_time_1.setOnClickListener(this)
            iv_clear.setOnClickListener(this)
            search_view.tv_cancel.setOnClickListener(this)
        }
    }

    //历史记录列表初始化
    private fun initHistoryAdapter() {
        historyAdapter = RecyclerAdapter(this, { _adapter, parent, _ ->
            val convertView = _adapter.getView(R.layout.item_main_project_search, parent)
            object : RecyclerHolder<String>(convertView) {
                val tv1 = convertView.findViewById(R.id.tv1) as TextView
                override fun setData(view: View, data: String, position: Int) {
                    tv1.text = data
                }
            }
        })
        recycler_his.layoutManager = LinearLayoutManager(this)
        recycler_his.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))
        recycler_his.adapter = historyAdapter
        historyAdapter.onItemClick = { _, p ->
            //点击历史记录直接进行查询
            searchStr = historyAdapter.dataList[p]
            search_view.search = searchStr
            doSearch(searchStr)
        }
    }

    private fun initResultAdapter() {
        //查询结果列表和adapter的初始化
        adapter = RecyclerAdapter(this, { _adapter, parent, _ ->
            val convertView = _adapter.getView(R.layout.item_fund_main_list, parent)
            object : RecyclerHolder<FundSmallBean>(convertView) {
                val fundName = convertView.find<TextView>(R.id.fundName)
                val regTime = convertView.find<TextView>(R.id.regTime)
                override fun setData(view: View, data: FundSmallBean, position: Int) {
                    var label = data.fundName
                    if (!TextUtils.isEmpty(label) && !TextUtils.isEmpty(searchStr)) {
                        label = label.replaceFirst(searchStr, "<font color='#ff3300'>$searchStr</font>")
                    }
                    fundName.text = Html.fromHtml(label)
                    regTime.text = data.regTime
                }
            }
        })
        adapter.onItemClick = { _, position ->
            FundDetailActivity.start(this@FundSearchActivity, adapter.dataList[position])
        }
        recycler_result.layoutManager = LinearLayoutManager(this)
        recycler_result.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))
        recycler_result.adapter = adapter
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
                page = 0
                doSearch(searchStr)
            }

            override fun onLoadMore(refreshLayout: TwinklingRefreshLayout?) {
                ++page
                doSearch(searchStr)
            }

        })
    }

    override fun onStart() {
        super.onStart()
        val fundSearch = Store.store.getFundSearch(this)
        historyAdapter.dataList.addAll(fundSearch)
        historyAdapter.notifyDataSetChanged()
    }

    override fun onPause() {
        super.onPause()
        historyAdapter.dataList.clear()
        historyAdapter.notifyDataSetChanged()
    }

    fun doSearch(searchStr: String) {
        if (searchStr.isEmpty()) {
            adapter.dataList.clear()
            adapter.notifyDataSetChanged()
            ll_history.visibility = View.VISIBLE
            ll_result.visibility = View.GONE
            return
        }
        ll_history.visibility = View.GONE
        ll_result.visibility = View.VISIBLE
        val tmplist = LinkedList<String>()
        tmplist.add(searchStr)
        Store.store.saveFundSearch(this, tmplist)
        SoguApi.getService(application)
                .getAllFunds(page = page, sort = (currentNameOrder + currentTimeOrder),fuzzyQuery = searchStr)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ payload ->
                    if (payload.isOk) {
                        payload.apply {
                            tv_result_title.text = Html.fromHtml(getString(R.string.tv_title_result_news, total))
                        }
                        if (page == 0) {
                            adapter.dataList.clear()
                        }
                        payload.payload?.apply {
                            Log.d(FundMainActivity.TAG, Gson().toJson(this))
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
                    if (page == 0) {
                        refresh?.finishRefreshing()
                    } else {
                        refresh?.finishLoadmore()
                    }
                })
    }


    override fun onClick(p0: View?) {
        when (p0?.id) {
            R.id.ll_order_name_1 -> {
                if (currentNameOrder == FundSmallBean.FundDesc) {
                    currentNameOrder = FundSmallBean.FundAsc
                    iv_sort_name_1.imageResource = R.drawable.ic_up
                } else {
                    currentNameOrder = FundSmallBean.FundDesc
                    iv_sort_name_1.imageResource = R.drawable.ic_down
                }
                page = 0
                doSearch(searchStr)
            }
            R.id.ll_order_time_1 -> {
                if (currentTimeOrder == FundSmallBean.RegTimeAsc) {
                    currentTimeOrder = FundSmallBean.RegTimeDesc
                    iv_sort_time_1.imageResource = R.drawable.ic_down
                } else {
                    currentTimeOrder = FundSmallBean.RegTimeAsc
                    iv_sort_time_1.imageResource = R.drawable.ic_up
                }
                page = 0
                doSearch(searchStr)
            }
            R.id.tv_cancel -> {
                finish()
            }
            R.id.iv_clear->{
                Store.store.clearFundSearch(this)
                historyAdapter.dataList.clear()
                historyAdapter.notifyDataSetChanged()
            }
        }
    }


}
