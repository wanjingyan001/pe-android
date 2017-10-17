package com.sogukj.pe.ui

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Bundle
import android.text.Html
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.GridLayout
import android.widget.TextView
import com.framework.base.ToolbarActivity
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.bean.NewsBean
import com.sogukj.pe.bean.ProjectBean
import com.sogukj.pe.util.Trace
import com.sogukj.pe.view.FlowLayout
import com.sogukj.pe.view.ListAdapter
import com.sogukj.pe.view.ListHolder
import com.sogukj.pe.view.TipsView
import com.sogukj.service.SoguApi
import com.sogukj.util.Store
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_project.*
import org.jetbrains.anko.find

/**
 * Created by qinfei on 17/7/18.
 */
class ProjectActivity : ToolbarActivity() {
    lateinit var adapterNeg: ListAdapter<NewsBean>
    lateinit var adapterYuqin: ListAdapter<NewsBean>
    lateinit var project: ProjectBean
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        project = intent.getSerializableExtra(Extras.DATA) as ProjectBean
        setContentView(R.layout.activity_project)
        setBack(true)
        setTitle(project.name)

        ll_shangshi.visibility = if (project.is_volatility == 0) View.GONE else View.VISIBLE
        adapterNeg = ListAdapter<NewsBean> { NewsHolder() }
        adapterYuqin = ListAdapter<NewsBean> { NewsHolder() }
        list_negative.adapter = adapterNeg
        list_yuqin.adapter = adapterYuqin
        tv_more.setOnClickListener {
            NegativeNewsActivity.start(this, project, 1)
        }
        tv_more_yq.setOnClickListener {
            NegativeNewsActivity.start(this, project, 2)
        }
        list_negative.setOnItemClickListener { parent, view, position, id ->
            val data = adapterNeg.dataList[position]
            NewsDetailActivity.start(this, data)
        }
        list_yuqin.setOnItemClickListener { parent, view, position, id ->
            val data = adapterYuqin.dataList[position]
            NewsDetailActivity.start(this, data)
        }
        SoguApi.getService(application)
                .listNews(pageSize = 3, page = 1, type = 1, company_id = project.company_id)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ payload ->
                    if (payload.isOk) {
                        adapterNeg.dataList.clear()
                        payload.payload?.apply {
                            adapterNeg.dataList.addAll(this)
                        }
                        adapterNeg.notifyDataSetChanged()
                    } else
                        showToast(payload.message)
                }, { e ->
                    Trace.e(e)
                    tv_more.visibility = View.GONE
                    showToast("暂无可用数据")
                }, {
                    if (adapterNeg.dataList.size < 3)
                        tv_more.visibility = View.GONE
                    else
                        tv_more.visibility = View.VISIBLE
                })
        SoguApi.getService(application)
                .listNews(pageSize = 3, page = 1, type = 2, company_id = project.company_id)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ payload ->
                    if (payload.isOk) {
                        adapterYuqin.dataList.clear()
                        payload.payload?.apply {
                            adapterYuqin.dataList.addAll(this)
                        }
                        adapterYuqin.notifyDataSetChanged()
                    } else
                        showToast(payload.message)
                }, { e ->
                    Trace.e(e)
                    tv_more_yq.visibility = View.GONE
                    showToast("暂无可用数据")
                }, {
                    if (adapterYuqin.dataList.size < 3)
                        tv_more_yq.visibility = View.GONE
                    else
                        tv_more_yq.visibility = View.VISIBLE
                })
        val user = Store.store.getUser(this)
        SoguApi.getService(application)
                .projectPage(pageSize = 3, page = 1, company_id = project.company_id!!, uid = user?.uid)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ payload ->
                    if (payload.isOk) {
                        payload.payload
                                ?.counts
                                ?.apply {
                                    refresh(gl_shangshi, this, Color.parseColor("#5785f3"))
                                    refresh(gl_qiyebeijin, this, Color.parseColor("#fe5f39"))
                                    refresh(gl_qiyefazhan, this, Color.parseColor("#5785f3"))
                                    refresh(gl_jinyinzhuankuang, this, Color.parseColor("#fe5f39"))
                                    refresh(gl_zhishichanquan, this, Color.parseColor("#5785f3"))
                                }
                    } else
                        showToast(payload.message)
                }, { e ->
                    Trace.e(e)
                    tv_more_yq.visibility = View.GONE
                })
    }

    fun refresh(grid: GridLayout, data: Map<String, Int?>, color: Int = Color.RED) {
        tv_xmws.setOnClickListener(this::onClick)
        val size = grid.childCount
        for (i in 0..size - 1) {
            val child = grid.getChildAt(i) as TipsView
            val icon = child.compoundDrawables[1]
            val tag = child.getTag()
            if (null != tag && tag is String) {
                var count: Int? = null
                if (data.containsKey(tag.toLowerCase())) {
                    count = data[tag.toLowerCase()]
                } else {
                    count = data[tag]
                }
                if (count != null && count > 0) {
                    icon?.clearColorFilter()
                    child.display(count, color)
                    child.setOnClickListener(this::onClick)
                } else {
                    icon?.setColorFilter(colorGray, PorterDuff.Mode.SRC_ATOP)
                    child.setOnClickListener(null)
                }
            } else {
                icon?.setColorFilter(colorGray, PorterDuff.Mode.SRC_ATOP)
                child.setOnClickListener(null)
            }
        }
    }

    val colorGray = Color.parseColor("#D9D9D9")
    fun onClick(view: View) {
        when (view.id) {
            R.id.tv_stock -> StockInfoActivity.start(this@ProjectActivity, project)
            R.id.tv_company -> CompanyInfoActivity.start(this@ProjectActivity, project)
            R.id.tv_gaoguan -> GaoGuanActivity.start(this@ProjectActivity, project)
            R.id.tv_cangukonggu -> CanGuActivity.start(this@ProjectActivity, project)
            R.id.tv_shangshigonggao -> AnnouncementActivity.start(this@ProjectActivity, project)
            R.id.tv_shidagudong -> ShiDaGuDongActivity.start(this@ProjectActivity, project)
            R.id.tv_shidaliutong -> ShiDaLiuTongGuDongActivity.start(this@ProjectActivity, project)
            R.id.tv_faxinxiangguan -> IssueRelatedActivity.start(this@ProjectActivity, project)
            R.id.tv_gubenbiandong -> EquityChangeActivity.start(this@ProjectActivity, project)
            R.id.tv_fenhong -> BonusInfoActivity.start(this@ProjectActivity, project)
            R.id.tv_peigu -> AllotmentListActivity.start(this@ProjectActivity, project)
            R.id.tv_gubenjiegou -> GuBenJieGouActivity.start(this@ProjectActivity, project)

            R.id.tv_bizinfo -> BizInfoActivity.start(this@ProjectActivity, project)
            R.id.tv_shareholder_info -> ShareHolderInfoActivity.start(this@ProjectActivity, project)
            R.id.tv_qiyelianbao -> QiYeLianBaoActivity.start(this@ProjectActivity, project)
            R.id.tv_change_record -> ChangeRecordActivity.start(this@ProjectActivity, project)
            R.id.tv_investment -> InvestmentActivity.start(this@ProjectActivity, project)
            R.id.tv_key_personal -> KeyPersonalActivity.start(this@ProjectActivity, project)
            R.id.tv_equity_structure -> EquityStructureActivity.start(this@ProjectActivity, project)
            R.id.tv_branch -> BranchListActivity.start(this@ProjectActivity, project)
            R.id.tv_gsjj -> CompanyInfo2Activity.start(this@ProjectActivity, project)

            R.id.tv_rongzilishi -> FinanceHistoryActivity.start(this@ProjectActivity, project)
            R.id.tv_touzishijian -> InvestEventActivity.start(this@ProjectActivity, project)
            R.id.tv_hexintuandui -> CoreTeamActivity.start(this@ProjectActivity, project)
            R.id.tv_qiyeyewu -> BusinessEventsActivity.start(this@ProjectActivity, project)
            R.id.tv_jinpinxinxi -> ProductInfoActivity.start(this@ProjectActivity, project)
            R.id.tv_zhaopinxinxi -> RecruitActivity.start(this@ProjectActivity, project)

            R.id.tv_zhaopinxinxi -> RecruitActivity.start(this@ProjectActivity, project)
            R.id.tv_zhaiquanxinxi -> BondActivity.start(this@ProjectActivity, project)
            R.id.tv_shuiwupinji -> TaxRateActivity.start(this@ProjectActivity, project)
            R.id.tv_goudixinxi -> LandPurchaseActivity.start(this@ProjectActivity, project)
            R.id.tv_zhaotoubiao -> BidsActivity.start(this@ProjectActivity, project)
            R.id.tv_zizhizhenshu -> QualificationListActivity.start(this@ProjectActivity, project)
            R.id.tv_chouchajiancha -> CheckListActivity.start(this@ProjectActivity, project)
            R.id.tv_chanpinxinxi -> AppListActivity.start(this@ProjectActivity, project)

            R.id.tv_shangbiao -> BrandListActivity.start(this@ProjectActivity, project)
            R.id.tv_zhuanli -> PatentListActivity.start(this@ProjectActivity, project)
            R.id.tv_ranzhuquan -> CopyrightListActivity.start(this@ProjectActivity, project, 1)
            R.id.tv_zhuzuoquan -> CopyrightListActivity.start(this@ProjectActivity, project, 2)
            R.id.tv_wangzhanbeian -> ICPListActivity.start(this@ProjectActivity, project)

            R.id.tv_xmws -> ProjectBookActivity.start(this@ProjectActivity, project)
        }
    }

    override val menuId: Int
        get() = R.menu.menu_mark

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val flag = super.onCreateOptionsMenu(menu)
        val menuMark = menu.findItem(R.id.action_mark) as MenuItem
        if (null != project) {
            when (project?.is_focus) {
                1 -> menuMark?.title = ("已关注")
                else -> menuMark?.title = ("关注")
            }
        }
        return flag
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.action_mark -> {
                toggleMark(item!!)
            }
        }
        return false
    }

    fun toggleMark(item: MenuItem) {
        val user = Store.store.getUser(this)
        if (null == user) return
        val mark = if (project.is_focus == 1) 0 else 1
        SoguApi.getService(application)
                .mark(uid = user!!.uid!!, company_id = project.company_id!!, type = mark)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ payload ->
                    if (payload.isOk) {
                        project.is_focus = mark
                        item.title = if (mark == 1) "已关注" else "关注"
                    }
                }, { e ->
                    Trace.e(e)
                })

    }

    inner class NewsHolder
        : ListHolder<NewsBean> {
        lateinit var tv_summary: TextView
        lateinit var tv_time: TextView
        lateinit var tv_from: TextView
        lateinit var tags: FlowLayout
        lateinit var tv_date: TextView
        override fun createView(inflater: LayoutInflater): View {
            val convertView = inflater.inflate(R.layout.item_main_news, null)
            tv_summary = convertView.find(R.id.tv_summary)
            tv_time = convertView.find(R.id.tv_time)
            tv_from = convertView.find(R.id.tv_from)
            tags = convertView.find(R.id.tags)
            tv_date = convertView.findViewById(R.id.tv_date) as TextView
            return convertView
        }

        override fun showData(convertView: View, position: Int, data: NewsBean?) {
            var label = data?.title
//                if (!TextUtils.isEmpty(label) && !TextUtils.isEmpty(key)) {
//                    label = label!!.replaceFirst(key, "<font color='#ff3300'>${key}</font>")
//                }
            tv_summary.text = Html.fromHtml(label)
            val strTime = data?.time
            tv_time.visibility = View.GONE
            if (!TextUtils.isEmpty(strTime)) {
                val strs = strTime!!.trim().split(" ")
                if (!TextUtils.isEmpty(strs.getOrNull(1))) {
                    tv_time.visibility = View.VISIBLE
                }
                tv_date.text = strs.getOrNull(0)
                tv_time.text = strs.getOrNull(1)
            }
            tv_from.text = data?.source
            tags.removeAllViews()
            data?.tag?.split("#")
                    ?.forEach { str ->
                        if (!TextUtils.isEmpty(str)) {
                            val itemTag = View.inflate(this@ProjectActivity, R.layout.item_tag_news, null)
                            val tvTag = itemTag.find<TextView>(R.id.tv_tag)
                            tvTag.text = str
                            tags.addView(itemTag)
                            data.setTags(this@ProjectActivity, tags)
                        }
                    }
        }

    }

    companion object {
        fun start(ctx: Activity?, project: ProjectBean) {
            val intent = Intent(ctx, ProjectActivity::class.java)
            intent.putExtra(Extras.DATA, project)
            ctx?.startActivity(intent)
        }
    }
}
