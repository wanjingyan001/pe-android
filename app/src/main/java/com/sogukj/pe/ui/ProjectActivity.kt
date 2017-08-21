package com.sogukj.pe.ui

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import com.framework.base.ToolbarActivity
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.adapter.ListAdapter
import com.sogukj.pe.adapter.ListHolder
import com.sogukj.pe.bean.NewsBean
import com.sogukj.pe.bean.ProjectBean
import com.sogukj.pe.util.Trace
import com.sogukj.pe.view.FlowLayout
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
        setContentView(R.layout.activity_project)
        setBack(true)
        project = intent.getSerializableExtra(Extras.DATA) as ProjectBean
        setTitle(project.name)

        adapterNeg = ListAdapter<NewsBean> { NewsHolder() }
        adapterYuqin = ListAdapter<NewsBean> { NewsHolder() }
        list_negative.adapter = adapterNeg
        list_yuqin.adapter = adapterYuqin
        tv_more.setOnClickListener {
            NegativeNewsActivity.start(this, project)
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
                    showToast("暂无可用数据")
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
                    showToast("暂无可用数据")
                })
    }

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
        override fun createView(inflater: LayoutInflater): View {
            val view = inflater.inflate(R.layout.item_main_news, null)
            tv_summary = view.find(R.id.tv_summary)
            tv_time = view.find(R.id.tv_time)
            tv_from = view.find(R.id.tv_from)
            tags = view.find(R.id.tags)
            return view
        }

        override fun showData(convertView: View, position: Int, data: NewsBean?) {
            tv_summary.text = data?.title
            tv_time.text = data?.time
            tv_from.text = data?.source

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
