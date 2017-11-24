package com.sogukj.pe.ui.fund

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import com.framework.base.ToolbarActivity
import com.google.gson.Gson
import com.sogukj.pe.R
import com.sogukj.pe.bean.FundSmallBean
import com.sogukj.pe.view.RecyclerAdapter
import com.sogukj.pe.view.RecyclerHolder
import com.sogukj.service.SoguApi
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import org.jetbrains.anko.find

class FundAccountActivity : ToolbarActivity() {
    private lateinit var adapter: RecyclerAdapter<Map<String, String>>
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
        title = "基金台账"
        find<TextView>(R.id.companyName).text = bean.fundName
        initAdapter()
        doRequest(bean.id)
    }

    fun initAdapter() {

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
                            adapter.dataList.add(map)
                            adapter.notifyDataSetChanged()
                        }
                    } else {
                        showToast(payload.message)
                    }
                })
    }
}
