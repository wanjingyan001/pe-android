package com.sogukj.pe.ui.project

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.support.v4.graphics.drawable.RoundedBitmapDrawable
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.Toolbar
import android.text.Html
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.Theme
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.transition.Transition
import com.framework.base.ToolbarActivity
import com.google.gson.JsonSyntaxException
import com.netease.nim.uikit.api.NimUIKit
import com.sogukj.pe.App
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.bean.NewsBean
import com.sogukj.pe.bean.ProjectBean
import com.sogukj.pe.ui.*
import com.sogukj.pe.ui.approve.ApproveListActivity
import com.sogukj.pe.ui.htdata.ProjectBookActivity
import com.sogukj.pe.ui.news.NegativeNewsActivity
import com.sogukj.pe.ui.news.NewsDetailActivity
import com.sogukj.pe.util.Trace
import com.sogukj.pe.util.Utils
import com.sogukj.pe.view.*
import com.sogukj.service.SoguApi
import com.sogukj.util.Store
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_project.*
import org.jetbrains.anko.find
import org.jetbrains.anko.textColor
import java.net.UnknownHostException

/**
 * Created by qinfei on 17/7/18.
 */
class ProjectActivity : ToolbarActivity(), View.OnClickListener {
    lateinit var adapterNeg: RecyclerAdapter<NewsBean>
    lateinit var adapterYuqin: RecyclerAdapter<NewsBean>
    lateinit var project: ProjectBean
    var position = 0
    var type = 0

    override fun onBackPressed() {
        var intent = Intent()
        intent.putExtra(Extras.DATA, project)
        intent.putExtra(Extras.CODE, position)
        setResult(Activity.RESULT_CANCELED, intent)
        super.onBackPressed()
        Log.e("onBackPressed", "onBackPressed")
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 0x001 && resultCode == Activity.RESULT_OK) {
            setResult(Activity.RESULT_OK)
            finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        project = intent.getSerializableExtra(Extras.DATA) as ProjectBean
        position = intent.getIntExtra(Extras.CODE, 0)
        type = intent.getIntExtra(Extras.TYPE, 0)
        setContentView(R.layout.activity_project)
        setBack(true)
        toolbar?.apply {
            this.setBackgroundColor(resources.getColor(R.color.transparent))
        }
        //setTitle(project.name)
        if (project.logo.isNullOrEmpty()) {
            imgIcon.setImageResource(R.drawable.default_icon)
        } else {
            Glide.with(context).load(project.logo).into(imgIcon)
        }
        companyTitle.text = project.name
        //const val TYPE_CB = 4
        //const val TYPE_LX = 1
        //const val TYPE_YT = 2
        //const val TYPE_GZ = 3
        //const val TYPE_DY = 6
        //const val TYPE_TC = 7
        if (type == ProjectListFragment.TYPE_DY) {
            proj_stage.text = "储 备"
            //edit.visibility = View.GONE
            history.visibility = View.GONE
        } else if (type == ProjectListFragment.TYPE_CB) {
            proj_stage.text = "立 项"
            history.visibility = View.GONE
        } else if (type == ProjectListFragment.TYPE_LX) {
            proj_stage.text = "投 决"
            edit.visibility = View.GONE
            history.visibility = View.GONE
        } else if (type == ProjectListFragment.TYPE_YT) {
            proj_stage.text = "退 出"
            edit.visibility = View.GONE
            history.visibility = View.GONE
            delete.visibility = View.GONE
            if (project.quit == 1) {
                history.visibility = View.VISIBLE
            }
        } else if (type == ProjectListFragment.TYPE_TC) {
            proj_stage.visibility = View.GONE
            edit.visibility = View.GONE
            delete.visibility = View.GONE
        }
        proj_stage.setOnClickListener {
            if (type == ProjectListFragment.TYPE_TC) {
                //退出项目已经不需要退出了
            } else if (type == ProjectListFragment.TYPE_YT) {
                //进入新增的退出模块
                ProjectTCActivity.start(context, false, project)
            } else {
                doAdd()
            }
        }
        delete.setOnClickListener {
            doDel()
        }
        edit.setOnClickListener {
            if (type == ProjectListFragment.TYPE_CB) {
                StoreProjectAddActivity.startEdit(context, project)
            } else if (type == ProjectListFragment.TYPE_DY) {
                ProjectAddActivity.startEdit(context, project)
            }
        }
        history.setOnClickListener {
            ProjectTcHistoryActivity.start(context, project)
        }

//        if (project.type == 6) {
//            divide1.visibility = View.VISIBLE
//            divide2.visibility = View.GONE
//        } else {
//            divide1.visibility = View.GONE
//            divide2.visibility = View.VISIBLE
//        }
        divide1.visibility = View.VISIBLE
        divide2.visibility = View.VISIBLE

        ll_shangshi.visibility = if (project.is_volatility == 0) View.GONE else View.VISIBLE
//        adapterNeg = ListAdapter<NewsBean> { NewsHolder() }
        adapterNeg = RecyclerAdapter(context, { adapter, parent, type ->
            NewsHolder(adapter.getView(R.layout.item_main_news, parent))
        })
        adapterYuqin = RecyclerAdapter(context, { adapter, parent, type ->
            NewsHolder(adapter.getView(R.layout.item_main_news, parent))
        })
        list_negative.layoutManager = LinearLayoutManager(this)
        list_negative.isNestedScrollingEnabled = false
        list_negative.adapter = adapterNeg
        list_yuqin.layoutManager = LinearLayoutManager(this)
        list_yuqin.adapter = adapterYuqin
        list_yuqin.isNestedScrollingEnabled = false

        tv_more.setOnClickListener {
            NegativeNewsActivity.start(this, project, 1)
        }
        tv_more_yq.setOnClickListener {
            NegativeNewsActivity.start(this, project, 2)
        }
        adapterNeg.onItemClick = { view, position ->
            val data = adapterNeg.dataList[position]
            NewsDetailActivity.start(this, data)
        }
        adapterYuqin.onItemClick = { view, position ->
            val data = adapterYuqin.dataList[position]
            NewsDetailActivity.start(this, data)
        }
        disable(tv_cwsj)
//        disable(tv_gdzx)
        disable(tv_xmzy)
        val user = Store.store.getUser(this)
        SoguApi.getService(application)
                .projectPage(pageSize = 20, page = 1, company_id = project.company_id!!, uid = user?.uid)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ payload ->
                    if (payload.isOk) {
                        payload.payload?.counts?.apply {
                            refresh(gl_shangshi, this, Color.parseColor("#5785f3"))
                            refresh(gl_qiyebeijin, this, Color.parseColor("#fe5f39"))
                            refresh(gl_qiyefazhan, this, Color.parseColor("#5785f3"))
                            refresh(gl_jinyinzhuankuang, this, Color.parseColor("#fe5f39"))
                            refresh(gl_zhishichanquan, this, Color.parseColor("#5785f3"))
                            refreshView()
                        }
                        payload.payload?.yuQing?.let { list ->
                            adapterYuqin.dataList.clear()
                            payload.payload?.let {
                                adapterYuqin.dataList.addAll(list)
                            }
                            adapterYuqin.notifyDataSetChanged()
                        }
                        payload.payload?.fuMian?.let { list ->
                            adapterNeg.dataList.clear()
                            payload.payload?.let {
                                adapterNeg.dataList.addAll(list)
                            }
                            adapterNeg.notifyDataSetChanged()
                        }
                    } else
                        showCustomToast(R.drawable.icon_toast_fail, payload.message)
                }, { e ->
                    Trace.e(e)
                    tv_more_yq.visibility = View.GONE
                }, {
                    if (adapterNeg.dataList.size <= 3)
                        tv_more.visibility = View.GONE
                    else
                        tv_more.visibility = View.VISIBLE

                    if (adapterYuqin.dataList.size <= 3)
                        tv_more_yq.visibility = View.GONE
                    else
                        tv_more_yq.visibility = View.VISIBLE
                })

        is_business = project.is_business
        is_ability = project.is_ability
        if (is_business == 1) {
            btn_yes.setBackgroundResource(R.drawable.bg_rectangle_blue)
            btn_yes.textColor = Color.parseColor("#ffffff")

            btn_no.setBackgroundResource(R.drawable.bg_rectangle_white)
            btn_no.textColor = Color.parseColor("#282828")
        } else if (is_business == 2) {
            btn_no.setBackgroundResource(R.drawable.bg_rectangle_blue)
            btn_no.textColor = Color.parseColor("#ffffff")

            btn_yes.setBackgroundResource(R.drawable.bg_rectangle_white)
            btn_yes.textColor = Color.parseColor("#282828")
        } else if (is_business == null) {
            btn_yes.setBackgroundResource(R.drawable.bg_rectangle_white)
            btn_yes.textColor = Color.parseColor("#282828")

            btn_no.setBackgroundResource(R.drawable.bg_rectangle_white)
            btn_no.textColor = Color.parseColor("#282828")
        }

        if (is_ability == 1) {
            btn_you.setBackgroundResource(R.drawable.bg_rectangle_blue)
            btn_you.textColor = Color.parseColor("#ffffff")

            btn_wu.setBackgroundResource(R.drawable.bg_rectangle_white)
            btn_wu.textColor = Color.parseColor("#282828")
        } else if (is_ability == 2) {
            btn_wu.setBackgroundResource(R.drawable.bg_rectangle_blue)
            btn_wu.textColor = Color.parseColor("#ffffff")

            btn_you.setBackgroundResource(R.drawable.bg_rectangle_white)
            btn_you.textColor = Color.parseColor("#282828")
        } else if (is_ability == null) {
            btn_you.setBackgroundResource(R.drawable.bg_rectangle_white)
            btn_you.textColor = Color.parseColor("#282828")

            btn_wu.setBackgroundResource(R.drawable.bg_rectangle_white)
            btn_wu.textColor = Color.parseColor("#282828")
        }

        btn_yes.setOnClickListener {
            if (is_business == 1) {
                return@setOnClickListener
            }
            btn_yes.setBackgroundResource(R.drawable.bg_rectangle_blue)
            btn_yes.textColor = Color.parseColor("#ffffff")

            btn_no.setBackgroundResource(R.drawable.bg_rectangle_white)
            btn_no.textColor = Color.parseColor("#282828")

            is_business = 1

            manager_assess()
        }

        btn_no.setOnClickListener {
            if (is_business == 2) {
                return@setOnClickListener
            }
            btn_no.setBackgroundResource(R.drawable.bg_rectangle_blue)
            btn_no.textColor = Color.parseColor("#ffffff")

            btn_yes.setBackgroundResource(R.drawable.bg_rectangle_white)
            btn_yes.textColor = Color.parseColor("#282828")

            is_business = 2

            manager_assess()
        }

        btn_you.setOnClickListener {
            if (is_ability == 1) {
                return@setOnClickListener
            }
            btn_you.setBackgroundResource(R.drawable.bg_rectangle_blue)
            btn_you.textColor = Color.parseColor("#ffffff")

            btn_wu.setBackgroundResource(R.drawable.bg_rectangle_white)
            btn_wu.textColor = Color.parseColor("#282828")

            is_ability = 1

            manager_assess()
        }

        btn_wu.setOnClickListener {
            if (is_ability == 2) {
                return@setOnClickListener
            }
            btn_wu.setBackgroundResource(R.drawable.bg_rectangle_blue)
            btn_wu.textColor = Color.parseColor("#ffffff")

            btn_you.setBackgroundResource(R.drawable.bg_rectangle_white)
            btn_you.textColor = Color.parseColor("#282828")

            is_ability = 2

            manager_assess()
        }
        im.setOnClickListener(this)
    }

    fun doDel() {
//        MaterialDialog.Builder(this)
//                .theme(Theme.LIGHT)
//                .title("是否删除该项目")
//                .positiveText("确定")
//                .negativeText("取消")
//                .onPositive { dialog, which ->
//                    SoguApi.getService(application)
//                            .delProject(project.company_id!!)
//                            .observeOn(AndroidSchedulers.mainThread())
//                            .subscribeOn(Schedulers.io())
//                            .subscribe({ payload ->
//                                if (payload.isOk) {
//                                    showCustomToast(R.drawable.icon_toast_success, "删除成功")
//                                    setResult(Activity.RESULT_OK)
//                                    finish()
//                                } else
//                                    showCustomToast(R.drawable.icon_toast_fail, payload.message)
//                            }, { e ->
//                                Trace.e(e)
//                                showCustomToast(R.drawable.icon_toast_fail, "删除失败")
//                            })
//                }
//                .show()
        val inflate = LayoutInflater.from(this).inflate(R.layout.layout_input_dialog1, null)
        val dialog = MaterialDialog.Builder(this)
                .customView(inflate, false)
                .cancelable(true)
                .build()
        dialog.window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        val veto = inflate.find<TextView>(R.id.veto_comment)
        val confirm = inflate.find<TextView>(R.id.confirm_comment)
        val title = inflate.find<TextView>(R.id.approval_comments_title)
        title.text = "是否删除该项目?"
        veto.text = "取消"
        confirm.text = "确定"
        veto.setOnClickListener {
            if (dialog.isShowing) {
                dialog.dismiss()
            }
        }
        confirm.setOnClickListener {
            if (dialog.isShowing) {
                dialog.dismiss()
            }
            SoguApi.getService(application)
                    .delProject(project.company_id!!)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeOn(Schedulers.io())
                    .subscribe({ payload ->
                        if (payload.isOk) {
                            showCustomToast(R.drawable.icon_toast_success, "删除成功")
                            setResult(Activity.RESULT_OK)
                            finish()
                        } else
                            showCustomToast(R.drawable.icon_toast_fail, payload.message)
                    }, { e ->
                        Trace.e(e)
                        showCustomToast(R.drawable.icon_toast_fail, "删除失败")
                    })
        }
        dialog.show()
    }

    fun doAdd() {
        var titleStr = ""
        if (type == ProjectListFragment.TYPE_DY) {
            titleStr = "是否添加到储备"
        } else if (type == ProjectListFragment.TYPE_CB) {
            titleStr = "是否添加到立项"
        } else if (type == ProjectListFragment.TYPE_LX) {
            titleStr = "是否添加到已投"
        } else if (type == ProjectListFragment.TYPE_YT) {
            titleStr = "是否添加到退出"
        }
        val inflate = LayoutInflater.from(this).inflate(R.layout.layout_input_dialog1, null)
        val dialog = MaterialDialog.Builder(this)
                .customView(inflate, false)
                .cancelable(true)
                .build()
        dialog.window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        val veto = inflate.find<TextView>(R.id.veto_comment)
        val confirm = inflate.find<TextView>(R.id.confirm_comment)
        val title = inflate.find<TextView>(R.id.approval_comments_title)
        title.text = titleStr
        veto.text = "取消"
        confirm.text = "确定"
        veto.setOnClickListener {
            if (dialog.isShowing) {
                dialog.dismiss()
            }
        }
        confirm.setOnClickListener {
            if (dialog.isShowing) {
                dialog.dismiss()
            }
            val status = if (type == ProjectListFragment.TYPE_DY) 1 else if (type == ProjectListFragment.TYPE_CB) 2
            else if (type == ProjectListFragment.TYPE_LX) 3 else return@setOnClickListener
            SoguApi.getService(application)
                    .changeStatus(project.company_id!!, status)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeOn(Schedulers.io())
                    .subscribe({ payload ->
                        if (payload.isOk) {
                            if (type == ProjectListFragment.TYPE_DY) {
                                showCustomToast(R.drawable.icon_toast_success, "成功添加到储备")
                            } else if (type == ProjectListFragment.TYPE_CB) {
                                showCustomToast(R.drawable.icon_toast_success, "成功添加到立项")
                            } else if (type == ProjectListFragment.TYPE_LX) {
                                showCustomToast(R.drawable.icon_toast_success, "成功添加到已投")
                            } else if (type == ProjectListFragment.TYPE_YT) {
                                showCustomToast(R.drawable.icon_toast_success, "成功添加到退出")
                            }
                            setResult(Activity.RESULT_OK)
                            finish()
                        } else {
                            showCustomToast(R.drawable.icon_toast_fail, payload.message)
                        }
                    }, { e ->
                        Trace.e(e)
                        if (type == ProjectListFragment.TYPE_DY) {
                            showCustomToast(R.drawable.icon_toast_fail, "添加到储备失败")
                        } else if (type == ProjectListFragment.TYPE_CB) {
                            showCustomToast(R.drawable.icon_toast_fail, "添加到立项失败")
                        } else if (type == ProjectListFragment.TYPE_LX) {
                            showCustomToast(R.drawable.icon_toast_fail, "添加到已投失败")
                        } else if (type == ProjectListFragment.TYPE_YT) {
                            showCustomToast(R.drawable.icon_toast_fail, "添加到退出失败")
                        }
                    })
        }
        dialog.show()
    }

    private fun createOrJoin() {
        val user = Store.store.getUser(this)
        val accid = user?.accid ?: ""
        SoguApi.getService(application)
                .createJoinGroup(accid, project.company_id.toString())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ payload ->
                    if (payload.isOk) {
                        payload.payload?.let {
                            NimUIKit.startTeamSession(this, it.toString())
                        }
                    } else {
                        showCustomToast(R.drawable.icon_toast_fail, payload.message)
                    }
                }, { e ->
                    Trace.e(e)
                })
    }

    var is_business: Int? = null//非空(1=>有价值 ,2=>无价值)
    var is_ability: Int? = null//非空(1=>有能力,2=>无能力)

    fun <T1, T2, T3> ifNotNull(value1: T1?, value2: T2?, value3: T3?, bothNotNull: (T1, T2, T3) -> (Unit)) {
        if (value1 != null && value2 != null && value3 != null) {
            bothNotNull(value1, value2, value3)
        }
    }

    fun manager_assess() {
        var id = project.company_id

        ifNotNull(is_business, is_ability, id, { is_business, is_ability, id ->
            SoguApi.getService(application)
                    .assess(id, is_business, is_ability)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeOn(Schedulers.io())
                    .subscribe({ payload ->
                        if (payload.isOk) {
                            Log.e("success", "success")
                            project.is_ability = is_ability
                            project.is_business = is_business
                        } else
                            showCustomToast(R.drawable.icon_toast_fail, payload.message)
                    }, { e ->
                        Trace.e(e)
                        ToastError(e)
                    })
        })
    }

    fun refresh(grid: android.support.v7.widget.GridLayout, data: Map<String, Int?>, color: Int = Color.RED) {
        val size = grid.childCount
        for (i in 0 until size) {
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
                    child.setOnClickListener(this)
//                    child.setOnClickListener(this::onClick)
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

    fun refreshView() {
        val size = gl_htdata.childCount
        for (i in 0 until size) {
            if (gl_htdata.getChildAt(i) != null) {
                val child = gl_htdata.getChildAt(i) as TipsView
                child.setOnClickListener(this)
            }
        }
    }

    fun disable(view: TextView) {
        view.compoundDrawables[1]?.setColorFilter(colorGray, PorterDuff.Mode.SRC_ATOP)
        view.setOnClickListener(null)
    }

    val colorGray = Color.parseColor("#D9D9D9")
    override fun onClick(view: View) {
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
            R.id.tv_xmcb -> StoreProjectAddActivity.startView(this@ProjectActivity, project)
            R.id.tv_gdzx -> ShareholderCreditActivity.start(this@ProjectActivity, project)

        // 跟踪记录,尽调数据,投决数据,投后管理数据
            R.id.tv_xmgz -> RecordTraceActivity.start(this@ProjectActivity, project)
            R.id.tv_xmjd -> SurveyDataActivity.start(this@ProjectActivity, project)
            R.id.tv_xmtj -> InvestSuggestActivity.start(this@ProjectActivity, project)
            R.id.tv_xmthgl -> ManageDataActivity.start(this@ProjectActivity, project)

            R.id.tv_spls -> ApproveListActivity.start(this@ProjectActivity, null, project.company_id)

            R.id.im -> {
                createOrJoin()
            }
        }
    }

//    override val menuId: Int
//        get() = R.menu.menu_mark
//
//    override fun onCreateOptionsMenu(menu: Menu): Boolean {
//        val flag = super.onCreateOptionsMenu(menu)
//        val menuMark = menu.findItem(R.id.action_mark) as MenuItem
//        if (null != project) {
//            when (project?.is_focus) {
//                1 -> menuMark?.title = ("已关注")
//                else -> menuMark?.title = ("关注")
//            }
//        }
//        return flag
//    }
//
//    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
//        when (item?.itemId) {
//            R.id.action_mark -> {
//                toggleMark(item!!)
//            }
//        }
//        return false
//    }
//
//    fun toggleMark(item: MenuItem) {
//        val user = Store.store.getUser(this)
//        if (null == user) return
//        val mark = if (project.is_focus == 1) 0 else 1
//        SoguApi.getService(application)
//                .mark(uid = user!!.uid!!, company_id = project.company_id!!, type = mark)
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribeOn(Schedulers.io())
//                .subscribe({ payload ->
//                    if (payload.isOk) {
//                        project.is_focus = mark
//                        item.title = if (mark == 1) "已关注" else "关注"
//                    }
//                }, { e ->
//                    Trace.e(e)
//                })
//
//    }

    inner class NewsHolder(convertView: View) : RecyclerHolder<NewsBean>(convertView) {
        val tv_summary: TextView = convertView.find(R.id.tv_summary)
        val tv_time: TextView = convertView.find(R.id.tv_time)
        val tv_from: TextView = convertView.find(R.id.tv_from)
        val tags: FlowLayout = convertView.find(R.id.tags)
        val tv_date: TextView = convertView.find(R.id.tv_date)
        val icon: ImageView = convertView.find(R.id.imageIcon)
        override fun setData(view: View, data: NewsBean, position: Int) {
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
//                tv_date.text = strs.getOrNull(0)
                try {
                    tv_date.text = Utils.formatDate2(strTime)
                }catch (e:Exception){
                    e.printStackTrace()
                }
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

            if (data?.url.isNullOrEmpty()) {
                var bitmap = BitmapFactory.decodeResource(resources, R.drawable.default_icon)
                var draw = RoundedBitmapDrawableFactory.create(resources, bitmap) as RoundedBitmapDrawable
                draw.setCornerRadius(Utils.dpToPx(context, 4).toFloat())
                icon.setBackgroundDrawable(draw)
            } else {
                Glide.with(context).asBitmap().load(data?.url).into(object : SimpleTarget<Bitmap>() {
                    override fun onResourceReady(bitmap: Bitmap?, glideAnimation: Transition<in Bitmap>?) {
                        var draw = RoundedBitmapDrawableFactory.create(resources, bitmap) as RoundedBitmapDrawable
                        draw.setCornerRadius(Utils.dpToPx(context, 4).toFloat())
                        icon.setBackgroundDrawable(draw)
                    }
                })
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
