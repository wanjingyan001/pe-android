package com.sogukj.pe.ui.fund

import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.View
import android.widget.TextView
import com.bumptech.glide.Glide
import com.framework.base.BaseFragment
import com.google.gson.Gson
import com.lcodecore.tkrefreshlayout.RefreshListenerAdapter
import com.lcodecore.tkrefreshlayout.TwinklingRefreshLayout
import com.lcodecore.tkrefreshlayout.footer.BallPulseView
import com.lcodecore.tkrefreshlayout.header.progresslayout.ProgressLayout
import com.sogukj.pe.R
import com.sogukj.pe.bean.FundSmallBean
import com.sogukj.pe.bean.FundSmallBean.Companion.FundAsc
import com.sogukj.pe.bean.FundSmallBean.Companion.FundDesc
import com.sogukj.pe.bean.FundSmallBean.Companion.RegTimeAsc
import com.sogukj.pe.bean.FundSmallBean.Companion.RegTimeDesc
import com.sogukj.pe.ui.SupportEmptyView
import com.sogukj.pe.ui.user.UserActivity
import com.sogukj.pe.util.Trace
import com.sogukj.pe.view.RecyclerAdapter
import com.sogukj.pe.view.RecyclerHolder
import com.sogukj.service.SoguApi
import com.sogukj.util.Store
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_fund_main.*
import kotlinx.android.synthetic.main.fund_mian_toolbar.*
import org.jetbrains.anko.find
import org.jetbrains.anko.imageResource

class FundMainFragment : BaseFragment(), View.OnClickListener {
    override val containerViewId: Int
        get() = R.layout.activity_fund_main


    lateinit var adapter: RecyclerAdapter<FundSmallBean>
    private var page = 0
    private var currentNameOrder = FundDesc
    private var currentTimeOrder = RegTimeAsc

    companion object {
        val TAG: String = FundMainFragment::class.java.simpleName

        fun newInstance(): FundMainFragment{
            val fragment = FundMainFragment()
            val args = Bundle()
            fragment.arguments = args
            return fragment
        }
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fundTitle.text = "基金"

        run {
            //列表和adapter的初始化
            adapter = RecyclerAdapter(context, { _adapter, parent, type ->
                val convertView = _adapter.getView(R.layout.item_fund_main_list, parent)
                object : RecyclerHolder<FundSmallBean>(convertView) {
                    val fundName = convertView.find<TextView>(R.id.fundName)
                    val regTime = convertView.find<TextView>(R.id.regTime)
                    override fun setData(view: View, data: FundSmallBean, position: Int) {
                        fundName.text = data.fundName
                        regTime.text = data.regTime
                    }
                }
            })
            adapter.onItemClick = { _, position ->
                FundDetailActivity.start(activity, adapter.dataList[position])
            }
            recycler_view.layoutManager = LinearLayoutManager(context)
            recycler_view.addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
            recycler_view.adapter = adapter
            val header = ProgressLayout(context)
            header.setColorSchemeColors(ContextCompat.getColor(context, R.color.color_main))
            refresh.setHeaderView(header)
            val footer = BallPulseView(context)
            footer.setAnimatingColor(ContextCompat.getColor(context, R.color.color_main))
            refresh.setBottomView(footer)
            refresh.setOverScrollRefreshShow(false)
            refresh.setEnableLoadmore(true)
            refresh.setOnRefreshListener(object : RefreshListenerAdapter() {
                override fun onRefresh(refreshLayout: TwinklingRefreshLayout?) {
                    page = 0
                    doRequest()
                }

                override fun onLoadMore(refreshLayout: TwinklingRefreshLayout?) {
                    ++page
                    doRequest()
                }

            })
        }

        run {
            ll_order_name_1.setOnClickListener(this)
            ll_order_time_1.setOnClickListener(this)
            iv_user.setOnClickListener(this)
            iv_search.setOnClickListener(this)
        }
    }


    override fun onStart() {
        super.onStart()
        run {
            //显示用户头像
            Store.store.getUser(context)?.apply {
                iv_user.mChar = name?.first()
                Glide.with(this@FundMainFragment)
                        .load(headImage())
                        .into(iv_user)
            }
        }
        doRequest()
    }

    /**
     * 获取基金公司列表
     */
    fun doRequest() {
        SoguApi.getService(activity.application)
                .getAllFunds(page = page, sort = (currentNameOrder + currentTimeOrder))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ payload ->
                    if (payload.isOk) {
                        if (page == 0) {
                            adapter.dataList.clear()
                        }
                        payload.payload?.apply {
                            Log.d(TAG, Gson().toJson(this))
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
                if (currentNameOrder == FundDesc) {
                    currentNameOrder = FundAsc
                    iv_sort_name_1.imageResource = R.drawable.ic_up
                } else {
                    currentNameOrder = FundDesc
                    iv_sort_name_1.imageResource = R.drawable.ic_down
                }
                page = 0
                doRequest()
            }
            R.id.ll_order_time_1 -> {
                if (currentTimeOrder == RegTimeAsc) {
                    currentTimeOrder = RegTimeDesc
                    iv_sort_time_1.imageResource = R.drawable.ic_down
                } else {
                    currentTimeOrder = RegTimeAsc
                    iv_sort_time_1.imageResource = R.drawable.ic_up
                }
                page = 0
                doRequest()
            }
            R.id.iv_user -> {
                UserActivity.start(activity)
            }
            R.id.iv_search -> {
                FundSearchActivity.start(activity)
            }
        }

    }
}
