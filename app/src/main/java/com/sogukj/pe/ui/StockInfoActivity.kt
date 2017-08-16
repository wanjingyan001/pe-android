package com.sogukj.pe.ui

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import com.framework.base.ToolbarActivity
import com.sogukj.pe.util.Trace
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.bean.ProjectBean
import com.sogukj.service.SoguApi
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_stock_quote.*
import java.text.SimpleDateFormat

class StockInfoActivity : ToolbarActivity() {
    lateinit var project: ProjectBean
    val fmt = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        project = intent.getSerializableExtra(Extras.DATA) as ProjectBean
        setContentView(R.layout.activity_stock_quote)
        setBack(true)
        setTitle("股票行情")
        SoguApi.getService(application)
                .stockInfo(project.company_id!!)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ payload ->
                    if (payload.isOk) {
                        val data = payload.payload
                        data?.apply {
                            tv_name.text = stockname
                            tv_obj.text = stockcode.toString()
                            tv_price.text = hexm_curPrice
                            tv_zhangdie.text = "$hexm_float_price $hexm_float_rate"
                            tv_update_time.text = fmt.format(payload.timestamp)
                            tv_zhangting.text = tmaxprice
                            tv_dieting.text = tminprice
                            tv_open.text = topenprice
                            tv_close.text = pprice
                            tv_high.text = thighprice
                            tv_low.text = tlowprice
                            tv_shizhi.text = tvalue
                            tv_shizhi_liutong.text = flowvalue
                            tv_liang.text = tamount
                            tv_e.text = tamounttotal
                            tv_jin.text = tvaluep
                            tv_pe.text = fvaluep
                            tv_zhenfu.text = trange
                            tv_huanshou.text = tchange
                        }
                    } else
                        showToast(payload.message)
                }, { e ->
                    Trace.e(e)
                })
    }

    companion object {
        fun start(ctx: Activity?, project: ProjectBean) {
            val intent = Intent(ctx, StockInfoActivity::class.java)
            intent.putExtra(Extras.DATA, project)
            ctx?.startActivity(intent)
        }
    }
}
