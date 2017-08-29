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
import com.sogukj.pe.bean.NewsType
import com.sogukj.pe.util.Trace
import com.sogukj.service.SoguApi
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_news_detail.*
import kotlinx.android.synthetic.main.item_main_news.*
import org.jetbrains.anko.find


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
                        val itemTag = View.inflate(this, R.layout.item_tag_news, null)
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

    fun set13(map: Map<String, Object?>, data: NewsBean) {
        val title = if (TextUtils.isEmpty(data.title)) "" else "<h1 style='color:#333;font-size:18px;'>${data.title}</h1>"
        val head = "<head><meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0, user-scalable=no\"> " +
                "<style>img{max-width: 100%; height:auto;} .reduce-font p{font-size:" + fontSize + "px!important;}</style>" +
                "</head>"
        val content = map[NewsType._13.format_content.toString()] as String?
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
        appendLine("委托法院拍卖时间", map[NewsType._12.auction_time.toString()])
        appendLine("委托法院内容", map[NewsType._12.entrusted_court.toString()])
        appendLine("内容", map[NewsType._12.content.toString()])
    }

    fun set11(map: Map<String, Object?>, data: NewsBean) {
        setTitle("开庭公告")

        tv_title.visibility = View.GONE
        appendLine("案由", map[NewsType._11.case_name.toString()])
        appendLine("案号", map[NewsType._11.caseno.toString()])
        appendLine("开庭日期", map[NewsType._11.court_date.toString()])
        appendLine("排期日期", map[NewsType._11.schedu_date.toString()])
        appendLine("承办部门", map[NewsType._11.undertake_department.toString()])
        appendLine("审判长/主审人", map[NewsType._11.presiding_judge.toString()])
        appendLine("上诉人", map[NewsType._11.appellant.toString()])
        appendLine("被上诉人", map[NewsType._11.appellee.toString()])
        appendLine("法院", map[NewsType._11.court.toString()])
        appendLine("法庭", map[NewsType._11.courtroom.toString()])
        appendLine("地区", map[NewsType._11.area.toString()])
    }

    fun set10(map: Map<String, Object?>, data: NewsBean) {
        setTitle("经营异常")
        appendLine("列入日期", map[NewsType._10.putDate.toString()])
        appendLine("列入经营异常名录原因", map[NewsType._10.putReason.toString()])
        appendLine("列入部门", map[NewsType._10.putDepartment.toString()])
        appendLine("移出日期", map[NewsType._10.removeDate.toString()])
        appendLine("移出原因", map[NewsType._10.removeReason.toString()])
        appendLine("移出部门", map[NewsType._10.removeDepartment.toString()])
    }

    fun set9(map: Map<String, Object?>, data: NewsBean) {
        setTitle("欠税公告")
        appendLine("纳税人名称", map[NewsType._9._name.toString()])
        appendLine("欠税税种", map[NewsType._9.taxCategory.toString()])
        appendLine("证件号码", map[NewsType._9.personIdNumber.toString()])
        appendLine("法人或负责人名称", map[NewsType._9.legalpersonName.toString()])
        appendLine("经营地点", map[NewsType._9.location.toString()])
        appendLine("当前新发生欠税余额", map[NewsType._9.newOwnTaxBalance.toString()])
        appendLine("欠税余额", map[NewsType._9.ownTaxBalance.toString()])
        appendLine("纳税人识别号", map[NewsType._9.taxIdNumber.toString()])
        appendLine("类型", if (map[NewsType._9.type.toString()] as Int? == 1) "地税" else "国税")
        appendLine("发布时间", map[NewsType._9.time.toString()])
        appendLine("税务机关", map[NewsType._9.tax_authority.toString()])
    }

    fun set8(map: Map<String, Object?>, data: NewsBean) {
        setTitle("动产抵押")
        appendLine("登记日期", map[NewsType._8.regDate.toString()])
        appendLine("登记编号", map[NewsType._8.regNum.toString()])
        appendLine("被担保债权种类", map[NewsType._8.type.toString()])
        appendLine("被担保债权数额", map[NewsType._8.amount.toString()])
        appendLine("登记机关", map[NewsType._8.regDepartment.toString()])
        appendLine("债务人履行债务的期限", map[NewsType._8.term.toString()])
        appendLine("担保范围", map[NewsType._8.scope.toString()])
        appendLine("备注", map[NewsType._8.remark.toString()])
        appendLine("概况种类", map[NewsType._8.overviewType.toString()])
        appendLine("概况数额", map[NewsType._8.overviewAmount.toString()])
        appendLine("概况担保的范围", map[NewsType._8.overviewScope.toString()])
        appendLine("概况债务人履行债务的期限", map[NewsType._8.overviewTerm.toString()])
        appendLine("概况备注", map[NewsType._8.overviewRemark.toString()])
        appendLine("总抵押变更 json数据", map[NewsType._8.changeInfoList.toString()])
        appendLine("json数据", map[NewsType._8.pawnInfoList.toString()])
        appendLine("抵押人信息json数据", map[NewsType._8.peopleInfoList.toString()])
    }

    fun set7(map: Map<String, Object?>, data: NewsBean) {
        setTitle("股权出质")
        appendLine("登记编号", map[NewsType._7.regNumber.toString()])
        appendLine("出质人", map[NewsType._7.pledgor.toString()])
        appendLine("质权人", map[NewsType._7.pledgee.toString()])
        appendLine("状态", map[NewsType._7.state.toString()])
        appendLine("出质股权数额", map[NewsType._7.equityAmount.toString()])
        appendLine("质权人证照/证件号码", map[NewsType._7.certifNumberR.toString()])
        appendLine("股权出质设立登记日期", map[NewsType._7.regDate.toString()])
    }

    fun set6(map: Map<String, Object?>, data: NewsBean) {
        setTitle("严重违法")
        appendLine("列入日期", map[NewsType._6.putDate.toString()])
        appendLine("列入原因", map[NewsType._6.putReason.toString()])
        appendLine("决定列入部门(作出决定机关", map[NewsType._6.putDepartment.toString()])
        appendLine("移除原因", map[NewsType._6.removeReason.toString()])
        appendLine("决定移除部门", map[NewsType._6.removeDepartment.toString()])
    }

    fun set5(map: Map<String, Object?>, data: NewsBean) {
        setTitle("行政处罚")
        appendLine("行政处罚日期", map[NewsType._5.decisionDate.toString()])
        appendLine("行政处罚决定书文号", map[NewsType._5.punishNumber.toString()])
        appendLine("违法行为类型", map[NewsType._5.type.toString()])
        appendLine("作出行政处罚决定机关名称", map[NewsType._5.departmentName.toString()])
        appendLine("行政处罚内容", map[NewsType._5.content.toString()])
    }

    fun set4(map: Map<String, Object?>, data: NewsBean) {
        setTitle("被执行人")
        appendLine("立案时间", map[NewsType._4.caseCreateTime.toString()])
        appendLine("执行标的", map[NewsType._4.execMoney.toString()])
        appendLine("案号", map[NewsType._4.caseCode.toString()])
        appendLine("执行法院", map[NewsType._4.execCourtName.toString()])
    }

    fun set3(map: Map<String, Object?>, data: NewsBean) {
        setTitle("失信人")
        appendLine("失信人名或公司名称", map[NewsType._3.iname.toString()])
        appendLine("执行依据文号", map[NewsType._3.casecode.toString()])
        appendLine("身份证号／组织机构代码", map[NewsType._3.cardnum.toString()])
        appendLine("省份", map[NewsType._3.areaname.toString()])
        appendLine("执行法院", map[NewsType._3.courtname.toString()])
        appendLine("案号", map[NewsType._3.gistid.toString()])
        appendLine("立案时间", map[NewsType._3.regdate.toString()])
        appendLine("做出执行依据单位", map[NewsType._3.gistunit.toString()])
        appendLine("法律生效文书确定的义务", map[NewsType._3.duty.toString()])
        appendLine("被执行人的履行情况", map[NewsType._3.performance.toString()])
        appendLine("发布时间", map[NewsType._3.publishdate.toString()])
    }

    fun set2(map: Map<String, Object?>, data: NewsBean) {
        setTitle("法院公告")
        appendLine("刊登日期", map[NewsType._2.publishdate.toString()])
        appendLine("原告", map[NewsType._2.party1.toString()])
        appendLine("当事人", map[NewsType._2.party2.toString()])
        appendLine("公告类型名称", map[NewsType._2.bltntypename.toString()])
        appendLine("法院名", map[NewsType._2.courtcode.toString()])
        appendLine("案件内容", map[NewsType._2.content.toString()])
    }

    fun set1(map: Map<String, Object?>, data: NewsBean) {
        setTitle("法律诉颂")
        appendLine("提交时间", map[NewsType._1.submittime.toString()])
        appendLine("标题", map[NewsType._1.title.toString()])
        appendLine("案件类型", map[NewsType._1.casetype.toString()])
        appendLine("案件号", map[NewsType._1.caseno.toString()])
        appendLine("法院", map[NewsType._1.court.toString()])
        appendLine("文书类型", map[NewsType._1.doctype.toString()])
        appendLine("原文链接地址", map[NewsType._1.url.toString()])
        //                appendLine("唯一标识符", map[NewsType._1.uuid.toString()])
    }


    companion object {
        fun start(ctx: Activity?, news: NewsBean) {
            val intent = Intent(ctx, NewsDetailActivity::class.java)
            intent.putExtra(Extras.DATA, news)
            ctx?.startActivity(intent)
        }
    }
}
