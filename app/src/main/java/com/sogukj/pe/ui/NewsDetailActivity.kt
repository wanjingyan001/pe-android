package com.sogukj.pe.ui

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.webkit.WebSettings
import android.webkit.WebSettings.LayoutAlgorithm
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
import java.text.SimpleDateFormat


class NewsDetailActivity : ToolbarActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_news_detail)
        setTitle("资讯详情")
        setBack(true)

        val webSettings = webview.settings
        webSettings.savePassword = false
        webSettings.javaScriptEnabled = true;
        webSettings.displayZoomControls = false;
        webSettings.builtInZoomControls = false;
        webSettings.setSupportZoom(false);
        webSettings.domStorageEnabled = true;

        val appCacheDir = this.getApplicationContext().getDir("cache", Context.MODE_PRIVATE).getPath();
        webSettings.setAppCachePath(appCacheDir);
        webSettings.setAllowFileAccess(true);
        webSettings.setAppCacheEnabled(true);
        webSettings.setCacheMode(WebSettings.LOAD_DEFAULT);
        webSettings.setDefaultTextEncodingName("utf-8");

        webSettings.setLoadsImagesAutomatically(true)
//        webSettings.setBlockNetworkImage(true)
//      webSettings.setUseWideViewPort(true);
//      webSettings.setLoadWithOverviewMode(true);
//        webSettings.setLayoutAlgorithm(LayoutAlgorithm.NARROW_COLUMNS)
        webSettings.setLayoutAlgorithm(LayoutAlgorithm.SINGLE_COLUMN);


        val news = intent.getSerializableExtra(Extras.DATA) as NewsBean?
        news?.apply {
            //            setSubview(this)
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
                        map?.apply { setContent(table_id, this, data) }
                    } else
                        showToast(payload.message)
                }, {})
    }

    fun setContent(table_id: Int, map: Map<String, Object>, data: NewsBean) {
        when (table_id) {
            13 -> set13(map, data)
        }
    }

    fun setSubview(data: NewsBean) {
        tv_summary.text = data.title
        tv_time.text = data.time
        tv_from.text = data.source
    }

    fun set13(map: Map<String, Object>, data: NewsBean) {
        val text = map[NewsType._13.format_content.toString()] as String?
        text?.apply {
            val html = getHtmlData(this, data, 18)
            webview.loadDataWithBaseURL("about:blank", html, "text/html", "utf-8", null)
        }
    }

    internal var DF = SimpleDateFormat("yyyy-MM-dd HH:mm")

    private fun getHtmlData(content: String, news: NewsBean, fontSize: Int): String {
        val head = "<head>" +
                "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0, user-scalable=no\"> " +
                "<style>img{max-width: 100%; height:auto;} .reduce-font p{font-size:" + fontSize + "px!important;}</style>" +
                "</head>"

//        val strTime = strDate.substring(0, 16).replace('T', ' ')
//        var time = strTime
//        try {
//            val date = DF.parse(time)
//            var ms = date.time
//            ms += (8 * 60 * 60 * 1000).toLong()
//            time = DF.format(ms)
//        } catch (e: Exception) {
//            Trace.e(TAG, "", e)
//        }

        return "<html>$head<body style='margin:0;'>" +
                "<div style='padding:10px;'><h1 style='color:#333;font-size:18px;'>" + news.title + "</h1>" +
                "<h5 style='color:#999;font-size:12px;'>" + news.time + "  " + news.source + "</h5>" +
                "<span style='color:#333;font-size:16px;line-height:30px;'>" + content + "</span></div>" +
                "</body></html>"
    }

    companion object {
        fun start(ctx: Activity?, news: NewsBean) {
            val intent = Intent(ctx, NewsDetailActivity::class.java)
            intent.putExtra(Extras.DATA, news)
            ctx?.startActivity(intent)
        }
    }
}
