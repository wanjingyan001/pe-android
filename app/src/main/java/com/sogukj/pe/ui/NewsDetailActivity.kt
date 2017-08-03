package com.sogukj.pe.ui

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.webkit.WebSettings
import android.webkit.WebSettings.LayoutAlgorithm
import com.framework.base.ToolbarActivity
import com.framework.util.Trace
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
    }


    fun setContent(table_id: Int, map: Map<String, Object>, data: NewsBean) {
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
    }

    fun set13(map: Map<String, Object>, data: NewsBean) {
        val content = map[NewsType._13.format_content.toString()] as String?
        val html = "<html>${head()}<body style='margin:20;'>" +
                "<div style='padding:10px;'>" +
                "${title(data.title)}" +
                "<h5 style='color:#999;font-size:12px;'>" + data.time + "  " + data.source + "</h5>" +
                "<span style='color:#333;font-size:16px;line-height:30px;'>" + content + "</span></div>" +
                "</body></html>"
        webview.loadDataWithBaseURL("about:blank", html, "text/html", "utf-8", null)
    }

    fun set12(map: Map<String, Object>, data: NewsBean) {
        setTitle("司法拍卖")
        val html = "<html>${head()}<body style='margin:20;'>" +
                "<div style='padding:10px;'>" +
                "${title(data.title)}" +
                row("委托法院拍卖时间", map[NewsType._12.auction_time.toString()]) +
                row("委托法院内容", map[NewsType._12.entrusted_court.toString()]) +
                row("内容", map[NewsType._12.content.toString()]) +
                "</span></div>" +
                "</body></html>"
        webview.loadDataWithBaseURL("about:blank", html, "text/html", "utf-8", null)
    }

    fun set11(map: Map<String, Object>, data: NewsBean) {
        setTitle("开庭公告")
        val html = "<html>${head()}<body style='margin:20;'>" +
                row("案由", map[NewsType._11.case_name.toString()]) +
                row("案号", map[NewsType._11.caseno.toString()]) +
                row("开庭日期", map[NewsType._11.court_date.toString()]) +
                row("排期日期", map[NewsType._11.schedu_date.toString()]) +
                row("承办部门", map[NewsType._11.undertake_department.toString()]) +
                row("审判长/主审人", map[NewsType._11.presiding_judge.toString()]) +
                row("上诉人", map[NewsType._11.appellant.toString()]) +
                row("被上诉人", map[NewsType._11.appellee.toString()]) +
                row("法院", map[NewsType._11.court.toString()]) +
                row("法庭", map[NewsType._11.courtroom.toString()]) +
                row("地区", map[NewsType._11.area.toString()]) +
                "</body></html>"
        webview.loadDataWithBaseURL("about:blank", html, "text/html", "utf-8", null)
    }

    fun set10(map: Map<String, Object>, data: NewsBean) {
        setTitle("经营异常")
        val html = "<html>${head()}<body style='margin:20;'>" +
                row("列入日期", map[NewsType._10.putDate.toString()]) +
                row("列入经营异常名录原因", map[NewsType._10.putReason.toString()]) +
                row("列入部门", map[NewsType._10.putDepartment.toString()]) +
                row("移出日期", map[NewsType._10.removeDate.toString()]) +
                row("移出原因", map[NewsType._10.removeReason.toString()]) +
                row("移出部门", map[NewsType._10.removeDepartment.toString()]) +
                "</body></html>"
        webview.loadDataWithBaseURL("about:blank", html, "text/html", "utf-8", null)
    }

    fun set9(map: Map<String, Object>, data: NewsBean) {
        setTitle("欠税公告")
        val html = "<html>${head()}<body style='margin:20;'>" +
                row("纳税人名称", map[NewsType._9._name.toString()]) +
                row("欠税税种", map[NewsType._9.taxCategory.toString()]) +
                row("证件号码", map[NewsType._9.personIdNumber.toString()]) +
                row("法人或负责人名称", map[NewsType._9.legalpersonName.toString()]) +
                row("经营地点", map[NewsType._9.location.toString()]) +
                row("当前新发生欠税余额", map[NewsType._9.newOwnTaxBalance.toString()]) +
                row("欠税余额", map[NewsType._9.ownTaxBalance.toString()]) +
                row("纳税人识别号", map[NewsType._9.taxIdNumber.toString()]) +
                row("类型", if (map[NewsType._9.type.toString()] as Int? == 1) "地税" else "国税") +
                row("发布时间", map[NewsType._9.time.toString()]) +
                row("税务机关", map[NewsType._9.tax_authority.toString()]) +
                "</body></html>"
        webview.loadDataWithBaseURL("about:blank", html, "text/html", "utf-8", null)
    }

    fun set8(map: Map<String, Object>, data: NewsBean) {
        setTitle("动产抵押")
        val html = "<html>${head()}<body style='margin:20;'>" +
                row("登记日期", map[NewsType._8.regDate.toString()]) +
                row("登记编号", map[NewsType._8.regNum.toString()]) +
                row("被担保债权种类", map[NewsType._8.type.toString()]) +
                row("被担保债权数额", map[NewsType._8.amount.toString()]) +
                row("登记机关", map[NewsType._8.regDepartment.toString()]) +
                row("债务人履行债务的期限", map[NewsType._8.term.toString()]) +
                row("担保范围", map[NewsType._8.scope.toString()]) +
                row("备注", map[NewsType._8.remark.toString()]) +
                row("概况种类", map[NewsType._8.overviewType.toString()]) +
                row("概况数额", map[NewsType._8.overviewAmount.toString()]) +
                row("概况担保的范围", map[NewsType._8.overviewScope.toString()]) +
                row("概况债务人履行债务的期限", map[NewsType._8.overviewTerm.toString()]) +
                row("概况备注", map[NewsType._8.overviewRemark.toString()]) +
                row("总抵押变更 json数据", map[NewsType._8.changeInfoList.toString()]) +
                row("json数据", map[NewsType._8.pawnInfoList.toString()]) +
                row("抵押人信息json数据", map[NewsType._8.peopleInfoList.toString()]) +
                "</body></html>"
        webview.loadDataWithBaseURL("about:blank", html, "text/html", "utf-8", null)
    }

    fun set7(map: Map<String, Object>, data: NewsBean) {
        setTitle("股权出质")
        val html = "<html>${head()}<body style='margin:20;'>" +
                row("登记编号", map[NewsType._7.regNumber.toString()]) +
                row("出质人", map[NewsType._7.pledgor.toString()]) +
                row("质权人", map[NewsType._7.pledgee.toString()]) +
                row("状态", map[NewsType._7.state.toString()]) +
                row("出质股权数额", map[NewsType._7.equityAmount.toString()]) +
                row("质权人证照/证件号码", map[NewsType._7.certifNumberR.toString()]) +
                row("股权出质设立登记日期", map[NewsType._7.regDate.toString()]) +
                "</body></html>"
        webview.loadDataWithBaseURL("about:blank", html, "text/html", "utf-8", null)
    }

    fun set6(map: Map<String, Object>, data: NewsBean) {
        setTitle("严重违法")
        val html = "<html>${head()}<body style='margin:20;'>" +
                row("列入日期", map[NewsType._6.putDate.toString()]) +
                row("列入原因", map[NewsType._6.putReason.toString()]) +
                row("决定列入部门(作出决定机关", map[NewsType._6.putDepartment.toString()]) +
                row("移除原因", map[NewsType._6.removeReason.toString()]) +
                row("决定移除部门", map[NewsType._6.removeDepartment.toString()]) +
                "</body></html>"
        webview.loadDataWithBaseURL("about:blank", html, "text/html", "utf-8", null)
    }

    fun set5(map: Map<String, Object>, data: NewsBean) {
        setTitle("行政处罚")
        val html = "<html>${head()}<body style='margin:20;'>" +
                row("行政处罚日期", map[NewsType._5.decisionDate.toString()]) +
                row("行政处罚决定书文号", map[NewsType._5.punishNumber.toString()]) +
                row("违法行为类型", map[NewsType._5.type.toString()]) +
                row("作出行政处罚决定机关名称", map[NewsType._5.departmentName.toString()]) +
                row("行政处罚内容", map[NewsType._5.content.toString()]) +
                "</body></html>"
        webview.loadDataWithBaseURL("about:blank", html, "text/html", "utf-8", null)
    }

    fun set4(map: Map<String, Object>, data: NewsBean) {
        setTitle("被执行人")
        val html = "<html>${head()}<body style='margin:20;'>" +
                row("立案时间", map[NewsType._4.caseCreateTime.toString()]) +
                row("执行标的", map[NewsType._4.execMoney.toString()]) +
                row("案号", map[NewsType._4.caseCode.toString()]) +
                row("执行法院", map[NewsType._4.execCourtName.toString()]) +
                "</body></html>"
        webview.loadDataWithBaseURL("about:blank", html, "text/html", "utf-8", null)
    }

    fun set3(map: Map<String, Object>, data: NewsBean) {
        setTitle("失信人")
        val html = "<html>${head()}<body style='margin:20;'>" +
                row("失信人名或公司名称", map[NewsType._3.iname.toString()]) +
                row("执行依据文号", map[NewsType._3.casecode.toString()]) +
                row("身份证号／组织机构代码", map[NewsType._3.cardnum.toString()]) +
                row("省份", map[NewsType._3.areaname.toString()]) +
                row("执行法院", map[NewsType._3.courtname.toString()]) +
                row("案号", map[NewsType._3.gistid.toString()]) +
                row("立案时间", map[NewsType._3.regdate.toString()]) +
                row("做出执行依据单位", map[NewsType._3.gistunit.toString()]) +
                row("法律生效文书确定的义务", map[NewsType._3.duty.toString()]) +
                row("被执行人的履行情况", map[NewsType._3.performance.toString()]) +
                row("发布时间", map[NewsType._3.publishdate.toString()]) +
                "</body></html>"
        webview.loadDataWithBaseURL("about:blank", html, "text/html", "utf-8", null)
    }

    fun set2(map: Map<String, Object>, data: NewsBean) {
        setTitle("法院公告")
        val html = "<html>${head()}<body style='margin:20;'>" +
                row("刊登日期", map[NewsType._2.publishdate.toString()]) +
                row("原告", map[NewsType._2.party1.toString()]) +
                row("当事人", map[NewsType._2.party2.toString()]) +
                row("公告类型名称", map[NewsType._2.bltntypename.toString()]) +
                row("法院名", map[NewsType._2.courtcode.toString()]) +
                row("案件内容", map[NewsType._2.content.toString()]) +
                "</body></html>"
        webview.loadDataWithBaseURL("about:blank", html, "text/html", "utf-8", null)
    }

    fun set1(map: Map<String, Object>, data: NewsBean) {
        setTitle("法律诉颂")
        val html = "<html>${head()}<body style='margin:20;'>" +
                row("提交时间", map[NewsType._1.submittime.toString()]) +
                row("标题", map[NewsType._1.title.toString()]) +
                row("案件类型", map[NewsType._1.casetype.toString()]) +
                row("案件号", map[NewsType._1.caseno.toString()]) +
                row("法院", map[NewsType._1.court.toString()]) +
                row("文书类型", map[NewsType._1.doctype.toString()]) +
                row("原文链接地址", map[NewsType._1.url.toString()]) +
                row("唯一标识符", map[NewsType._1.uuid.toString()]) +
                "</body></html>"
        webview.loadDataWithBaseURL("about:blank", html, "text/html", "utf-8", null)
    }

    var DF = SimpleDateFormat("yyyy-MM-dd HH:mm")
    internal val fontSize = 18
    fun head(): String = "<head>" +
            "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0, user-scalable=no\"> " +
            "<style>img{max-width: 100%; height:auto;} .reduce-font p{font-size:" + fontSize + "px!important;}</style>" +
            "</head>"

    fun label(label: String): String = "${label}:"
    fun value(value: Any?): String = if (null == value) "" else "<span style='color:#666;'>${value as String}</span>"
    fun row(label: String, value: Any?) = "<p style='color:#000;font-size:16px;line-height:30px;'>${label(label)}  ${value(value)}</p>"
    fun title(title: String?): String = if (null == title) "" else "<h1 style='color:#333;font-size:18px;'>${title}</h1>"


    companion object {
        fun start(ctx: Activity?, news: NewsBean) {
            val intent = Intent(ctx, NewsDetailActivity::class.java)
            intent.putExtra(Extras.DATA, news)
            ctx?.startActivity(intent)
        }
    }
}
