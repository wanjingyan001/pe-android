package com.sogukj.pe.ui.fund


import android.graphics.Bitmap
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.support.v4.graphics.drawable.RoundedBitmapDrawable
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.SpannableString
import android.text.Spanned
import android.text.style.AbsoluteSizeSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.animation.GlideAnimation
import com.bumptech.glide.request.target.SimpleTarget
import com.framework.base.BaseFragment
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
import com.sogukj.pe.view.ProgressView
import com.sogukj.pe.view.RecyclerAdapter
import com.sogukj.pe.view.RecyclerHolder
import com.sogukj.service.SoguApi
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_fund_list.*
import org.jetbrains.anko.find


/**
 * A simple [Fragment] subclass.
 */
class FundListFragment : BaseFragment() {
    override val containerViewId: Int
        get() = R.layout.fragment_fund_list

    companion object {
        val TAG = FundListFragment::class.java.simpleName
        const val TYPE_CB = 4//
        const val TYPE_LX = 1
        const val TYPE_YT = 2
        const val TYPE_GZ = 3
        const val TYPE_DY = 6
        const val TYPE_TC = 5//
        const val TYPE_CX = 7//

        fun newInstance(type: Int): FundListFragment {
            val fragment = FundListFragment()
            val intent = Bundle()
            intent.putInt(Extras.TYPE, type)
            fragment.arguments = intent
            return fragment
        }
    }

    fun getRecycleView(): RecyclerView {
        return recycler_view
    }

    lateinit var adapter: RecyclerAdapter<FundSmallBean>
    private var page = 0
    private var currentNameOrder = FundSmallBean.FundDesc
    private var currentTimeOrder = FundSmallBean.RegTimeAsc

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //列表和adapter的初始化
        adapter = RecyclerAdapter(context, { _adapter, parent, type ->
            val convertView = _adapter.getView(R.layout.item_fund_main_list, parent)
            object : RecyclerHolder<FundSmallBean>(convertView) {
                val icon = convertView.find<ImageView>(R.id.imageIcon)
                val fundName = convertView.find<TextView>(R.id.fundName)
                val regTime = convertView.find<TextView>(R.id.regTime)
                val ytc = convertView.find<TextView>(R.id.ytc)
                val total = convertView.find<TextView>(R.id.total)
                val progress = convertView.find<ProgressView>(R.id.progess)
                override fun setData(view: View, data: FundSmallBean, position: Int) {
                    Glide.with(context).load(data.url).asBitmap().into(object : SimpleTarget<Bitmap>() {
                        override fun onResourceReady(bitmap: Bitmap?, glideAnimation: GlideAnimation<in Bitmap>?) {
                            var draw = RoundedBitmapDrawableFactory.create(resources, bitmap) as RoundedBitmapDrawable
                            draw.setCornerRadius(Utils.dpToPx(context, 4).toFloat())
                            icon.setBackgroundDrawable(draw)
                        }
                    })
                    fundName.text = data.fundName
                    regTime.text = data.regTime

                    var spannableString = SpannableString("${data.ytc} 万")
                    var sizeSpan1 = AbsoluteSizeSpan(16, false)
                    var sizeSpan2 = AbsoluteSizeSpan(9, false)
                    spannableString.setSpan(sizeSpan1, 0, data.ytc.length, Spanned.SPAN_INCLUSIVE_EXCLUSIVE)
                    spannableString.setSpan(sizeSpan2, data.ytc.length, spannableString.length, Spanned.SPAN_INCLUSIVE_EXCLUSIVE)
                    ytc.text = spannableString

                    total.text = "总额：${data.total}万"

                    try {
                        progress.setPercent(data.ytc.toInt() * 100 / data.total.toInt())
                    } catch (e: Exception) {
                        progress.setPercent(0)
                    }
                }
            }
        })
        adapter.onItemClick = { _, position ->
            FundDetailActivity.start(activity, adapter.dataList[position])
        }
        recycler_view.layoutManager = LinearLayoutManager(context)
        //recycler_view.addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
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

    override fun onStart() {
        super.onStart()
        doRequest()
    }

    /**
     * 获取基金公司列表
     */
    fun doRequest() {
        //非空（1=>储备，2=>存续，3=>退出）
        var type = arguments.getInt(Extras.TYPE)
        if (type == TYPE_CB) {
            type = 1
        } else if (type == TYPE_CX) {
            type = 2
        } else if (type == TYPE_TC) {
            type = 3
        }
        SoguApi.getService(activity.application)
                .getAllFunds(page = page, sort = (currentNameOrder + currentTimeOrder), type = type)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ payload ->
                    if (payload.isOk) {
                        if (page == 0) {
                            adapter.dataList.clear()
                        }
                        payload.payload?.apply {
                            Log.d(FundMainFragment.TAG, Gson().toJson(this))
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

}
