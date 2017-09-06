package com.sogukj.pe.ui

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Html
import android.text.TextUtils
import android.view.View
import android.webkit.WebSettings
import android.webkit.WebSettings.LayoutAlgorithm
import android.widget.TextView
import com.framework.base.ToolbarActivity
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.bean.NewsBean
import com.sogukj.pe.util.Trace
import com.sogukj.service.SoguApi
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_news_detail.*
import kotlinx.android.synthetic.main.item_main_news.*
import org.jetbrains.anko.find

/**
 * Created by qinfei on 17/8/11.
 */
class NewsDetailActivity : ToolbarActivity() {
    fun appendLine(k: String, v: Any?) {
        buff.append("$k：<font color='#666666'>${if (v == null) "" else v}</font><br/>")
    }

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
            if (table_id == 13) {
                webview.visibility = View.VISIBLE
                scroll_plain.visibility = View.GONE
            } else {
                webview.visibility = View.GONE
                scroll_plain.visibility = View.VISIBLE
                setSubview(this)
            }
            if (null != table_id && null != data_id)
                doRequest(table_id!!, data_id!!, this);
        }
    }

    fun doRequest(table_id: Int, data_id: Int, data: NewsBean) {
        Trace.i(TAG, "tableId:dataId=${table_id}:${data_id}")
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

    fun setSubview(data: NewsBean) {
        tv_summary.text = data.title
        tv_time.text = data.time
        tv_from.text = data.source
        tags.removeAllViews()
        data.tag?.split("#")
                ?.forEach { str ->
                    if (!TextUtils.isEmpty(str)) {
                        val itemRes = when (str!!) {
                            "财务风险", "坏账增加", "经营风险",
                            "法律风险", "财务造假", "诉讼判决",
                            "违规违法"
                            -> R.layout.item_tag_news_1
                            "负面", "业绩不佳", "市场份额下降",
                            "企业风险", "系统风险", "操作风险",
                            "技术风险"
                            -> R.layout.item_tag_news_2
                            "股权转让", "人事变动", "内部重组"
                                , "股权出售", "质押担保", "行业企业重大事件"
                            -> R.layout.item_tag_news_3
                            else -> R.layout.item_tag_news_4
                        }
                        val itemTag = View.inflate(this, itemRes, null)
                        val tvTag = itemTag.find<TextView>(R.id.tv_tag)
                        tvTag.text = str
                        tags.addView(itemTag)
                    }
                }
    }


    val buff = StringBuffer()
    fun setContent(table_id: Int, map: Map<String, Object?>, data: NewsBean) {
        buff.setLength(0)
        when (table_id) {
            1 -> set1(map, data)
            2 -> set2(map, data)
            3 -> set3(map, data)
            4 -> set4(map, data)
            5 -> set5(map, data)
            6 -> set6(map, data)
            6 -> set6(map, data)
            7 -> set7(map, data)
            8 -> set8(map, data)
            9 -> set9(map, data)
            10 -> set10(map, data)
            11 -> set11(map, data)
            12 -> set12(map, data)
            13 -> set13(map, data)
        }

        tv_content.text = Html.fromHtml(buff.toString())
    }

    internal val fontSize = 18


    fun set1(map: Map<String, Object?>, data: NewsBean) {
        setTitle("法律诉讼")
        val content = map["content"]?.toString()
        if (!content.isNullOrEmpty()) {
            webview.visibility = View.VISIBLE
            scroll_plain.visibility = View.GONE
            val head = "<head><meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0, user-scalable=no\"> " +
                    "<style>img{max-width: 100%; height:auto;} .reduce-font p{font-size:" + fontSize + "px!important;}</style>" +
                    "</head>"
            val html = "<html>${head}<body style='margin:0px;'>" +
                    "<span style='color:#333;font-size:16px;line-height:30px;'>$content</span></div>" +
                    "</body></html>"
            webview.loadDataWithBaseURL("about:blank", html, "text/html", "utf-8", null)
        } else {
            webview.visibility = View.GONE
            scroll_plain.visibility = View.VISIBLE
            setSubview(data)

            appendLine("提交时间", map["submittime"])
            appendLine("标题", map["title"])
            appendLine("案件类型", map["casetype"])
            appendLine("案件号", map["caseno"])
            appendLine("法院", map["court"])
            appendLine("文书类型", map["doctype"])
            appendLine("原文链接地址", map["url"])
        }
        //                appendLine("唯一标识符", map["uuid"])
    }

    fun set13(map: Map<String, Object?>, data: NewsBean) {
        val title = if (TextUtils.isEmpty(data.title)) "" else "<h1 style='color:#333;font-size:18px;'>${data.title}</h1>"
        val head = "<head><meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0, user-scalable=no\"> " +
                "<style>img{max-width: 100%; height:auto;} .reduce-font p{font-size:" + fontSize + "px!important;}</style>" +
                "</head>"
        val content = map["format_content"] as String?
        val html = "<html>${head}<body style='margin:0px;'>" +
                "<div style='padding:10px;'>${title}" +
                "<h5 style='color:#999;font-size:12px;'>${data.time}    ${data.source}</h5>" +
                "<span style='color:#333;font-size:16px;line-height:30px;'>$content</span></div>" +
                "</body></html>"
        webview.loadDataWithBaseURL("about:blank", html, "text/html", "utf-8", null)
    }

    fun set12(map: Map<String, Object?>, data: NewsBean) {
        setTitle("司法拍卖")
        tv_title.text = "${data.title}"
        tv_title.visibility = View.VISIBLE
        appendLine("委托法院拍卖时间", map["auction_time"])
        appendLine("委托法院内容", map["entrusted_court"])
        appendLine("内容", map["content"])
    }

    fun set11(map: Map<String, Object?>, data: NewsBean) {
        setTitle("开庭公告")

        tv_title.visibility = View.GONE
        appendLine("案由", map["case_name"])
        appendLine("案号", map["caseno"])
        appendLine("开庭日期", map["court_date"])
        appendLine("排期日期", map["schedu_date"])
        appendLine("承办部门", map["undertake_department"])
        appendLine("审判长/主审人", map["presiding_judge"])
        appendLine("上诉人", map["appellant"])
        appendLine("被上诉人", map["appellee"])
        appendLine("法院", map["court"])
        appendLine("法庭", map["courtroom"])
        appendLine("地区", map["area"])
    }

    fun set10(map: Map<String, Object?>, data: NewsBean) {
        setTitle("经营异常")
        appendLine("列入日期", map["putDate"])
        appendLine("列入经营异常名录原因", map["putReason"])
        appendLine("列入部门", map["putDepartment"])
        appendLine("移出日期", map["removeDate"])
        appendLine("移出原因", map["removeReason"])
        appendLine("移出部门", map["removeDepartment"])
    }

    fun set9(map: Map<String, Object?>, data: NewsBean) {
        setTitle("欠税公告")
        appendLine("纳税人名称", map["name"])
        appendLine("欠税税种", map["taxCategory"])
        appendLine("证件号码", map["personIdNumber"])
        appendLine("法人或负责人名称", map["legalpersonName"])
        appendLine("经营地点", map["location"])
        appendLine("当前新发生欠税余额", map["recentOwnTaxBalance"])
        appendLine("欠税余额", map["ownTaxBalance"])
        appendLine("纳税人识别号", map["taxIdNumber"])
        appendLine("类型", map["type"])
        appendLine("发布时间", map["time"])
        appendLine("税务机关", map["tax_authority"])
    }

    fun set8(map: Map<String, Object?>, data: NewsBean) {
        setTitle("动产抵押")
        appendLine("登记日期", map["regDate"])
        appendLine("登记编号", map["regNum"])
        appendLine("被担保债权种类", map["type"])
        appendLine("被担保债权数额", map["amount"])
        appendLine("登记机关", map["regDepartment"])
        appendLine("债务人履行债务的期限", map["term"])
        appendLine("担保范围", map["scope"])
        appendLine("备注", map["remark"])
        appendLine("概况种类", map["overviewType"])
        appendLine("概况数额", map["overviewAmount"])
        appendLine("概况担保的范围", map["overviewScope"])
        appendLine("概况债务人履行债务的期限", map["overviewTerm"])
        appendLine("概况备注", map["overviewRemark"])
//        appendLine("总抵押变更 json数据", map["changeInfoList"])
//        appendLine("抵押物信息", map["pawnInfoList"])
//        appendLine("抵押人信息", map["peopleInfoList"])
        appendLine("抵押物信息", "")
        val json1 = map["pawnInfoList"]
        json1?.apply {

        }
        appendLine("抵押人信息", "")
        val json2 = map["peopleInfoList"]
        json2.apply {

        }
    }

    fun set7(map: Map<String, Object?>, data: NewsBean) {
        setTitle("股权出质")
        appendLine("登记编号", map["regNumber"])
        appendLine("出质人", map["pledgor"])
        appendLine("质权人", map["pledgee"])
        appendLine("状态", map["state"])
        appendLine("出质股权数额", map["equityAmount"])
        appendLine("质权人证照/证件号码", map["certifNumberR"])
        appendLine("股权出质设立登记日期", map["regDate"])
    }

    fun set6(map: Map<String, Object?>, data: NewsBean) {
        setTitle("严重违法")
        appendLine("列入日期", map["putDate"])
        appendLine("列入原因", map["putReason"])
        appendLine("决定列入部门(作出决定机关", map["putDepartment"])
        appendLine("移除原因", map["removeReason"])
        appendLine("决定移除部门", map["removeDepartment"])
    }

    fun set5(map: Map<String, Object?>, data: NewsBean) {
        setTitle("行政处罚")
        appendLine("行政处罚日期", map["decisionDate"])
        appendLine("行政处罚决定书文号", map["punishNumber"])
        appendLine("违法行为类型", map["type"])
        appendLine("作出行政处罚决定机关名称", map["departmentName"])
        appendLine("行政处罚内容", map["content"])
    }

    fun set4(map: Map<String, Object?>, data: NewsBean) {
        setTitle("被执行人")
        appendLine("立案时间", map["caseCreateTime"])
        appendLine("执行标的", map["execMoney"])
        appendLine("案号", map["caseCode"])
        appendLine("执行法院", map["execCourtName"])
    }

    fun set3(map: Map<String, Object?>, data: NewsBean) {
        setTitle("失信人")
        appendLine("失信人名或公司名称", map["iname"])
        appendLine("执行依据文号", map["casecode"])
        appendLine("身份证号／组织机构代码", map["cardnum"])
        appendLine("省份", map["areaname"])
        appendLine("执行法院", map["courtname"])
        appendLine("案号", map["gistid"])
        appendLine("立案时间", map["regdate"])
        appendLine("做出执行依据单位", map["gistunit"])
        appendLine("法律生效文书确定的义务", map["duty"])
        appendLine("被执行人的履行情况", map["performance"])
        appendLine("发布时间", map["publishdate"])
    }

    fun set2(map: Map<String, Object?>, data: NewsBean) {
        setTitle("法院公告")
        appendLine("刊登日期", map["publishdate"])
        appendLine("原告", map["party1"])
        appendLine("当事人", map["party2"])
        appendLine("公告类型名称", map["bltntypename"])
        appendLine("法院名", map["courtcode"])
        appendLine("案件内容", map["content"])
    }


    companion object {
        fun start(ctx: Activity?, news: NewsBean) {
            val intent = Intent(ctx, NewsDetailActivity::class.java)
            intent.putExtra(Extras.DATA, news)
            ctx?.startActivity(intent)
        }
    }
}
