package com.sogukj.pe.ui.fund

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v4.graphics.drawable.RoundedBitmapDrawable
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.text.Html
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.transition.Transition
import com.framework.base.ToolbarActivity
import com.google.gson.Gson
import com.lcodecore.tkrefreshlayout.RefreshListenerAdapter
import com.lcodecore.tkrefreshlayout.TwinklingRefreshLayout
import com.lcodecore.tkrefreshlayout.footer.BallPulseView
import com.lcodecore.tkrefreshlayout.header.progresslayout.ProgressLayout
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.bean.FundSmallBean
import com.sogukj.pe.ui.SupportEmptyView
import com.sogukj.pe.util.Trace
import com.sogukj.pe.util.Utils
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
    private var offset = 0
    private var currentNameOrder = FundSmallBean.FundDesc
    private var currentTimeOrder = FundSmallBean.RegTimeAsc
    private var searchStr: String = ""

    companion object {
        val TAG = FundSearchActivity::class.java.simpleName

        fun start(ctx: Context?, type: Int) {
            ctx?.startActivity(Intent(ctx, FundSearchActivity::class.java).putExtra(Extras.TYPE, type))
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
                offset = 0
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
                val icon = convertView.find<ImageView>(R.id.imageIcon)
                override fun setData(view: View, data: FundSmallBean, position: Int) {
                    if (data.logo.isNullOrEmpty()) {
                        var bitmap = BitmapFactory.decodeResource(resources, R.drawable.default_icon)
                        var draw = RoundedBitmapDrawableFactory.create(resources, bitmap) as RoundedBitmapDrawable
                        draw.setCornerRadius(Utils.dpToPx(context, 4).toFloat())
                        icon.setBackgroundDrawable(draw)
                    } else {
                        Glide.with(context).asBitmap().load(data.logo).into(object : SimpleTarget<Bitmap>() {
                            override fun onResourceReady(bitmap: Bitmap?, glideAnimation: Transition<in Bitmap>?) {
                                var draw = RoundedBitmapDrawableFactory.create(resources, bitmap) as RoundedBitmapDrawable
                                draw.setCornerRadius(Utils.dpToPx(context, 4).toFloat())
                                icon.setBackgroundDrawable(draw)
                            }
                        })
                    }
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
        //recycler_result.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))
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
                offset = 0
                doSearch(searchStr)
            }

            override fun onLoadMore(refreshLayout: TwinklingRefreshLayout?) {
                offset = adapter.dataList.size
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
                .getAllFunds(offset = offset, sort = (currentNameOrder + currentTimeOrder), fuzzyQuery = searchStr, type = intent.getIntExtra(Extras.TYPE, 0))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ payload ->
                    if (payload.isOk) {
                        payload.apply {
                            //tv_result_title.text = Html.fromHtml(getString(R.string.tv_title_result_news, (total as Double).toInt()))
                            tv_result_title.text = Html.fromHtml(getString(R.string.tv_title_result_news, payload.payload?.size))
                        }
                        if (offset == 0) {
                            adapter.dataList.clear()
                        }
                        payload.payload?.apply {
                            Log.d(FundMainFragment.TAG, Gson().toJson(this))
                            adapter.dataList.addAll(this)
                        }
                    } else
                        showCustomToast(R.drawable.icon_toast_fail, payload.message)
                }, { e ->
                    Trace.e(e)
                    showCustomToast(R.drawable.icon_toast_common, "暂无可用数据")
                }, {
                    SupportEmptyView.checkEmpty(this, adapter)
                    refresh?.setEnableLoadmore(adapter.dataList.size % 20 == 0)
                    adapter.notifyDataSetChanged()
                    if (offset == 0) {
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
                offset = 0
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
                offset = 0
                doSearch(searchStr)
            }
            R.id.tv_cancel -> {
                finish()
            }
            R.id.iv_clear -> {
                Store.store.clearFundSearch(this)
                historyAdapter.dataList.clear()
                historyAdapter.notifyDataSetChanged()
            }
        }
    }


}
