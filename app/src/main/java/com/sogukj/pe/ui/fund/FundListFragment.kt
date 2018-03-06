package com.sogukj.pe.ui.fund


import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.CountDownTimer
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.support.v4.graphics.drawable.RoundedBitmapDrawable
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.SpannableString
import android.text.Spanned
import android.text.style.RelativeSizeSpan
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.transition.Transition
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
import com.sogukj.util.XmlDb
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_fund_list.*
import kotlinx.android.synthetic.main.layout_loading.*
import org.jetbrains.anko.find
import java.text.DecimalFormat


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

    var mType = 0

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mType = arguments.getInt(Extras.TYPE)

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

                    fundName.text = data.fundName
                    if (data.regTime.isNullOrEmpty() || data.regTime.equals("--")) {
                        regTime.text = "- -/- -/- -"
                    } else {
                        regTime.text = data.regTime
                    }

//                    var spannableString = SpannableString("${data.invest} 万")
//                    var sizeSpan1 = RelativeSizeSpan(1f)
//                    var sizeSpan2 = RelativeSizeSpan(0.5f)
//                    spannableString.setSpan(sizeSpan1, 0, data.invest.length, Spanned.SPAN_INCLUSIVE_EXCLUSIVE)
//                    spannableString.setSpan(sizeSpan2, data.invest.length, spannableString.length, Spanned.SPAN_INCLUSIVE_EXCLUSIVE)

                    try {
                        var fund3 = FundCountDownColor(1000, 10, ytc, data.invest.toInt())
                        fund3.start()
                        var fund1 = FundCountDown(1000, 10, total, data.total.toInt())
                        fund1.start()
                    } catch (e: Exception) {
                        var spannable1 = SpannableString("- -  万")
                        var sizeSpan11 = RelativeSizeSpan(1f)
                        var sizeSpan21 = RelativeSizeSpan(0.5f)
                        spannable1.setSpan(sizeSpan11, 0, 3, Spanned.SPAN_INCLUSIVE_EXCLUSIVE)
                        spannable1.setSpan(sizeSpan21, 3, spannable1.length, Spanned.SPAN_INCLUSIVE_EXCLUSIVE)
                        ytc.text = spannable1
                        total.text = "总额：- - 万"
                    }
                    try {
                        var fund2 = FundCountDown(1000, 10, progress, data.invest.toInt() * 100 / data.total.toInt())
                        fund2.start()
                    } catch (e: Exception) {
                        progress.setPercent(0)
                    }
                }
            }
        })
        adapter.onItemClick = { _, position ->
            XmlDb.open(context).set(Extras.TYPE, mType.toString())
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

    class FundCountDownColor(var millisInFuture: Long, var countDownInterval: Long, var view: View, var data: Int) : CountDownTimer(millisInFuture, countDownInterval) {
        override fun onFinish() {
            var tmpdata = ""//data原始数据，tmpdata表示是否需要转化为"亿"来显示
            var spannableString: SpannableString? = null
            if (data >= 10000) {
                val df = DecimalFormat("#.00")
                tmpdata = df.format(data * 1.0 / 10000)
                spannableString = SpannableString("${tmpdata} 亿")
            } else {
                tmpdata = "${data}"
                spannableString = SpannableString("${tmpdata} 万")
            }
            var sizeSpan1 = RelativeSizeSpan(1f)
            var sizeSpan2 = RelativeSizeSpan(0.5f)
            spannableString.setSpan(sizeSpan1, 0, tmpdata.length, Spanned.SPAN_INCLUSIVE_EXCLUSIVE)
            spannableString.setSpan(sizeSpan2, tmpdata.length, spannableString.length, Spanned.SPAN_INCLUSIVE_EXCLUSIVE)
            (view as TextView).text = spannableString
//            var spannableString = SpannableString("${data} 万")
//            var sizeSpan1 = RelativeSizeSpan(1f)
//            var sizeSpan2 = RelativeSizeSpan(0.5f)
//            spannableString.setSpan(sizeSpan1, 0, data.toString().length, Spanned.SPAN_INCLUSIVE_EXCLUSIVE)
//            spannableString.setSpan(sizeSpan2, data.toString().length, spannableString.length, Spanned.SPAN_INCLUSIVE_EXCLUSIVE)
//            (view as TextView).text = spannableString
        }

        override fun onTick(millisUntilFinished: Long) {
            var tmp = millisInFuture - millisUntilFinished
            var data = data * tmp.toInt() / 1000

            var tmpdata = ""
            var spannableString: SpannableString? = null
            if (data >= 10000) {
                val df = DecimalFormat("#.00")
                tmpdata = df.format(data * 1.0 / 10000)
                spannableString = SpannableString("${tmpdata} 亿")
            } else {
                tmpdata = "${data}"
                spannableString = SpannableString("${tmpdata} 万")
            }
            var sizeSpan1 = RelativeSizeSpan(1f)
            var sizeSpan2 = RelativeSizeSpan(0.5f)
            spannableString.setSpan(sizeSpan1, 0, tmpdata.length, Spanned.SPAN_INCLUSIVE_EXCLUSIVE)
            spannableString.setSpan(sizeSpan2, tmpdata.length, spannableString.length, Spanned.SPAN_INCLUSIVE_EXCLUSIVE)
            (view as TextView).text = spannableString
//            var tmp = millisInFuture - millisUntilFinished
//            var tmpData = data * tmp.toInt() / 1000
//            var spannableString = SpannableString("${tmpData} 万")
//            var sizeSpan1 = RelativeSizeSpan(1f)
//            var sizeSpan2 = RelativeSizeSpan(0.5f)
//            spannableString.setSpan(sizeSpan1, 0, tmpData.toString().length, Spanned.SPAN_INCLUSIVE_EXCLUSIVE)
//            spannableString.setSpan(sizeSpan2, tmpData.toString().length, spannableString.length, Spanned.SPAN_INCLUSIVE_EXCLUSIVE)
//            (view as TextView).text = spannableString
        }
    }

    class FundCountDown(var millisInFuture: Long, var countDownInterval: Long, var view: View, var data: Int) : CountDownTimer(millisInFuture, countDownInterval) {
        override fun onFinish() {
            if (view is TextView) {
                //(view as TextView).text = "总额：${data}万"
                (view as TextView).text = transform(data)
            } else {
                (view as ProgressView).setPercent(data)
            }
        }

        override fun onTick(millisUntilFinished: Long) {
            var tmp = millisInFuture - millisUntilFinished
            if (view is TextView) {
                //(view as TextView).text = "总额：${data * tmp.toInt() / 1000}万"
                (view as TextView).text = transform(data * tmp.toInt() / 1000)
            } else {
                (view as ProgressView).setPercent(data * tmp.toInt() / 1000)
            }
        }

        private fun transform(data: Int): String {
            if (data >= 10000) {
                val df = DecimalFormat("#.00")
                return "总额：${df.format(data * 1.0 / 10000)}亿"
            } else {
                return "总额：${data}万"
            }
        }
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
                    iv_loading?.visibility = View.GONE
                }, { e ->
                    Trace.e(e)
                    //showToast("暂无可用数据")
                    iv_loading?.visibility = View.GONE
                    SupportEmptyView.checkEmpty(this, adapter)
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
