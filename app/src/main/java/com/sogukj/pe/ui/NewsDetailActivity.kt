package com.sogukj.pe.ui

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import com.framework.base.ToolbarActivity
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.bean.NewsBean
import com.sogukj.pe.bean.NewsType
import com.sogukj.service.SoguApi
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_news_detail.*
import kotlinx.android.synthetic.main.item_main_news.*

class NewsDetailActivity : ToolbarActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_news_detail)
        setTitle("资讯详情")
        setBack(true)

        val news = intent.getSerializableExtra(Extras.DATA) as NewsBean?
        news?.apply {
            setSubview(this)
            if (null != table_id && null != data_id)
                doRequest(table_id!!, data_id!!, this);
        }
    }

    fun doRequest(table_id: Int, data_id: Int, data: NewsBean) {
        SoguApi.getService(application)
                .newsInfo(table_id, data_id)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ payload ->
                    if (payload.isOk) {
                        val map = payload.payload
                        map?.apply { setContent(table_id, this) }
                    } else
                        showToast(payload.message)
                }, {})
    }

    fun setContent(table_id: Int, map: Map<String, Object>) {
        when (table_id) {
            13 -> set13(map)
        }
    }

    fun setSubview(data: NewsBean) {
        tv_summary.text = data.title
        tv_time.text = data.time
        tv_from.text = data.source
    }

    fun set13(map: Map<String, Object>) {
        tv_content.text = map[NewsType._13.format_content.toString()] as String?
    }

    companion object {
        fun start(ctx: Activity?, news: NewsBean) {
            val intent = Intent(ctx, NewsDetailActivity::class.java)
            intent.putExtra(Extras.DATA, news)
            ctx?.startActivity(intent)
        }
    }
}
