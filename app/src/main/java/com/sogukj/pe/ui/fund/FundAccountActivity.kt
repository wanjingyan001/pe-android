package com.sogukj.pe.ui.fund

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.widget.TextView
import com.framework.base.ToolbarActivity
import com.google.gson.Gson
import com.sogukj.pe.R
import com.sogukj.pe.bean.FundSmallBean
import com.sogukj.service.SoguApi
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_fund_account.*
import org.jetbrains.anko.find

class FundAccountActivity : ToolbarActivity() {
    private lateinit var adapter: FundAccountAdapter
    private var map = HashMap<String, String>()

    companion object {
        val TAG = FundAccountActivity::class.java.simpleName
        val DATA = "DATA"
        fun start(ctx: Context?, bean: FundSmallBean) {
            val intent = Intent(ctx, FundAccountActivity::class.java);
            intent.putExtra(DATA, bean)
            ctx?.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fund_account)
        val bean = intent.getSerializableExtra(DATA) as FundSmallBean
        setBack(true)
        title = "基金台账"
        find<TextView>(R.id.cardCompanyName).text = bean.fundName
        initAdapter()
        doRequest(bean.id)
    }

    private fun initAdapter() {
        adapter = FundAccountAdapter(this, map)
        val recyclerView = find<RecyclerView>(R.id.accountList)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))
        recyclerView.adapter = adapter
    }

    fun doRequest(fundId: Int) {
        SoguApi.getService(application)
                .getFundAccount(fundId)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ payload ->
                    if (payload.isOk) {
                        payload.payload?.apply {
                            Log.d(TAG, Gson().toJson(this))
                            map.put("退出项目收益", quitIncome)
                            map.put("PE项目投资金额", investmentAmount)
                            map.put("PE已上市项目浮盈/亏", profitLoss)
                            map.put("定增项目浮盈/亏", fixProfit)
                            map.put("估值", valuations)
                            adapter.setData(map)
                            val con = contributeSize.toFloat()
                            val act = actualSize.toFloat()
                            fund_histogram.setData(floatArrayOf(fundSize.toFloat(), con, act))
                            fundPie.setDatas(floatArrayOf(RaiseFunds.toFloat(), freeFunds.toFloat()))
                            progressChart.setData(floatArrayOf(quitAll.toFloat(),quitNum.toFloat(),investedNum.toFloat()))
                            fund_pie2.setColor(intArrayOf(R.color.fund_deep_blue,R.color.fund_light_blue))
                            fund_pie2.setDatas(floatArrayOf(investedMoney.toFloat(),fundSize.toFloat()))
                        }
                    } else {
                        showToast(payload.message)
                    }
                })
    }
}
