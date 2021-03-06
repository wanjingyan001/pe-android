package com.sogukj.pe.ui.project

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.*
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.support.design.widget.AppBarLayout
import android.support.v4.graphics.drawable.RoundedBitmapDrawable
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.Toolbar
import android.text.Html
import android.text.TextUtils
import android.util.Log
import android.view.*
import android.widget.*
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
import com.sogukj.pe.bean.ProjectDetailBean
import com.sogukj.pe.ui.*
import com.sogukj.pe.ui.approve.ApproveListActivity
import com.sogukj.pe.ui.htdata.ProjectBookActivity
import com.sogukj.pe.ui.news.NegativeNewsActivity
import com.sogukj.pe.ui.news.NewsDetailActivity
import com.sogukj.pe.util.StatusBarUtil
import com.sogukj.pe.util.Trace
import com.sogukj.pe.util.Utils
import com.sogukj.pe.view.*
import com.sogukj.service.SoguApi
import com.sogukj.util.Store
import com.sogukj.util.XmlDb
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_project.*
import okhttp3.internal.Util
import org.jetbrains.anko.backgroundResource
import org.jetbrains.anko.find
import org.jetbrains.anko.textColor
import java.net.UnknownHostException

/**
 * Created by qinfei on 17/7/18.
 */
class ProjectActivity : ToolbarActivity(), View.OnClickListener {
    lateinit var project: ProjectBean
    var position = 0
    var type = 0

    override fun onBackPressed() {
        var intent = Intent()
        intent.putExtra(Extras.DATA, project)
        intent.putExtra(Extras.CODE, position)
        setResult(Activity.RESULT_CANCELED, intent)
        super.onBackPressed()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 0x001 && resultCode == Activity.RESULT_OK) {
            var step = data?.getIntExtra(Extras.TYPE, 0)
            if (step == 1) {

            } else if (step == 2) {
                //全部退出才会到下一个
                setResult(Activity.RESULT_OK)
                finish()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        project = intent.getSerializableExtra(Extras.DATA) as ProjectBean
        position = intent.getIntExtra(Extras.CODE, 0)
        type = intent.getIntExtra(Extras.TYPE, 0)
        setContentView(R.layout.activity_project)
        StatusBarUtil.setTranslucentForCoordinatorLayout(this, 0)
        setBack(true)
        toolbar?.apply {
            this.setBackgroundColor(resources.getColor(R.color.transparent))
        }
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

        divide2.visibility = View.VISIBLE

        //shangshi_layout.visibility = if (project.is_volatility == 0) View.GONE else View.VISIBLE

        val user = Store.store.getUser(this)
        SoguApi.getService(application)
                .projectPage(company_id = project.company_id!!)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ payload ->
                    if (payload.isOk) {
                        payload.payload?.counts?.apply {
                            refreshGrid(gl_changyonggongneng, get(0).value!!, Color.parseColor("#5785f3"))
                            refreshGrid(gl_qiyeshangchuan, get(1).value!!, Color.parseColor("#5785f3"))
                            refreshGrid(gl_xiangmudanganku, get(2).value!!, Color.parseColor("#5785f3"))
                            refreshGrid(gl_shangshi, get(3).value!!, Color.parseColor("#5785f3"))
                            refreshGrid(gl_qiyebeijin, get(4).value!!, Color.parseColor("#fe5f39"))
                            refreshGrid(gl_qiyefazhan, get(5).value!!, Color.parseColor("#5785f3"))
                            refreshGrid(gl_jinyinzhuankuang, get(6).value!!, Color.parseColor("#fe5f39"))
                            refreshGrid(gl_zhishichanquan, get(7).value!!, Color.parseColor("#5785f3"))
                            if (get(0).value!!.size == 0) {
                                changyonggongneng_layout.visibility = View.GONE
                            }
                            if (get(1).value!!.size == 0) {
                                qiyeshangchuan_layout.visibility = View.GONE
                            }
                            if (get(2).value!!.size == 0) {
                                xiangmudanganku_layout.visibility = View.GONE
                            }
                            if (get(3).value!!.size == 0) {
                                shangshi_layout.visibility = View.GONE
                            }
                            if (get(4).value!!.size == 0) {
                                qiyebeijin_layout.visibility = View.GONE
                            }
                            if (get(5).value!!.size == 0) {
                                qiyefazhan_layout.visibility = View.GONE
                            }
                            if (get(6).value!!.size == 0) {
                                jinyinzhuankuang_layout.visibility = View.GONE
                            }
                            if (get(7).value!!.size == 0) {
                                zhishichanquan_layout.visibility = View.GONE
                            }
                        }

                        if (payload.payload!!.fu == 0) {
                            neg_num.text = "运营良好"
                            neg_num.textColor = Color.parseColor("#ff27d2ab")
                            neg_num.backgroundResource = R.drawable.neg_yq_bg2
                            neg.setOnClickListener(null)
                        } else {
                            neg_num.text = "${payload.payload!!.fu}条"
                            neg_num.textColor = Color.parseColor("#ffff5858")
                            neg_num.backgroundResource = R.drawable.neg_yq_bg
                            neg.setOnClickListener {
                                ProjectNewsActivity.start(context, "负面讯息", 1, project.company_id!!)
                            }
                        }
                        neg_num.visibility = View.VISIBLE
                        yq_num.text = "${payload.payload!!.yu}条"
                        yq_num.visibility = View.VISIBLE
                        if (payload.payload!!.yu == 0) {
                            yuqing.setOnClickListener(null)
                        } else {
                            yuqing.setOnClickListener {
                                ProjectNewsActivity.start(context, "企业舆情", 2, project.company_id!!)
                            }
                        }

                    } else
                        showCustomToast(R.drawable.icon_toast_fail, payload.message)
                }, { e ->
                    Trace.e(e)
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

        AppBarLayout.addOnOffsetChangedListener(object : AppBarLayout.OnOffsetChangedListener {
            override fun onOffsetChanged(appBarLayout: AppBarLayout?, verticalOffset: Int) {
                var alpha = Math.abs(verticalOffset) * 1.0 / Utils.dpToPx(context, 60)
                down.alpha = 1 - alpha.toFloat()

                if (down.alpha < 0.05) {
                    toolbar_title.text = if (project.shortName.isNullOrEmpty()) project.name else project.shortName
                } else {
                    toolbar_title.text = ""
                }
            }
        })
    }

    fun doDel() {
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

    //"status": 1,
    //"module": 1
    //"count": "0"
    //module=1 右上角不需要数字
    //module=0 count=0 灰色
    fun refreshGrid(grid: GridView, value: ArrayList<ProjectDetailBean.DetailBean.DetailSmallBean>, color: Int = Color.RED) {
        var adapter = GridAdapter(context, value, color)
        grid.adapter = adapter
        adapter.notifyDataSetChanged()
        grid.verticalSpacing = Utils.dpToPx(context, 10)
    }

    inner class GridAdapter(var context: Context, val list: ArrayList<ProjectDetailBean.DetailBean.DetailSmallBean>, val color: Int = Color.RED) : BaseAdapter() {
        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            var view = convertView
            var bean = list.get(position)
            var holder: ViewHolder? = null
            if (view == null) {
                holder = ViewHolder()
                view = LayoutInflater.from(context).inflate(R.layout.project_detail_item, null)
                holder.icon = view.findViewById(R.id.icon) as ImageView
                holder.seq = view.findViewById(R.id.seq) as TextView
                holder.title = view.findViewById(R.id.title) as TextView
                view.tag = holder
            } else {
                holder = view.tag as ViewHolder
            }
            holder.title!!.text = bean.name
            holder.icon!!.setImageResource(IdToDrawable(bean.id!!))
            view!!.id = bean.id!!

            var count = bean.count
            if (bean.module == 1) {
                holder.seq!!.visibility = View.GONE
                if (bean.status == 1) {
                    holder.icon!!.clearColorFilter()
                    view.setOnClickListener(this@ProjectActivity)
                } else {
                    holder.icon!!.setColorFilter(colorGray, PorterDuff.Mode.SRC_ATOP)
                    view.setOnClickListener(null)
                }
            } else {
                if (count != null && count.toInt() > 0) {
                    holder.seq!!.setText(count)
                    holder.seq!!.textColor = Color.parseColor("#608cf8")
                    //holder.icon!!.clearColorFilter()
                    holder.icon!!.setColorFilter(Color.parseColor("#608cf8"), PorterDuff.Mode.SRC_ATOP)
                    view.setOnClickListener(this@ProjectActivity)
                } else {
                    holder.seq!!.visibility = View.GONE
                    holder.icon!!.setColorFilter(colorGray, PorterDuff.Mode.SRC_ATOP)
                    view.setOnClickListener(null)
                }
            }

            return view
        }

        override fun getItem(position: Int): Any {
            return list.get(position)
        }

        override fun getItemId(position: Int): Long {
            return position.toLong()
        }

        override fun getCount(): Int {
            return list.size
        }

        inner class ViewHolder {
            var icon: ImageView? = null
            var seq: TextView? = null
            var title: TextView? = null
        }
    }

    fun disable(view: TextView) {
        view.compoundDrawables[1]?.setColorFilter(colorGray, PorterDuff.Mode.SRC_ATOP)
        view.setOnClickListener(null)
    }

    fun IdToDrawable(id: Int): Int {
        return when (id) {
            1 -> R.drawable.ic_proj_gsxx//工商信息
            2 -> R.drawable.ic_proj_qygx//企业关系
            3 -> R.drawable.ic_proj_gudong//股东信息
            4 -> R.drawable.ic_proj_gqjg//股权结构
            5 -> R.drawable.ic_proj_zyry//主要人员
            6 -> R.drawable.ic_proj_dwtz//对外投资
            7 -> R.drawable.ic_proj_bgjl//变更记录
            8 -> R.drawable.ic_proj_qynb//企业年报
            9 -> R.drawable.ic_proj_fzjg//分支机构
            10 -> R.drawable.ic_proj_gsjj//公司简介
            11 -> R.drawable.ic_proj_rzls//融资历史
            12 -> R.drawable.ic_proj_tzsj//投资事件
            13 -> R.drawable.ic_proj_hxtd//核心团队
            14 -> R.drawable.ic_proj_qyyw//企业业务
            15 -> R.drawable.ic_proj_jpxx//竞品信息
            16 -> R.drawable.ic_proj_zpxx//招聘信息
            17 -> R.drawable.ic_proj_zqxx//债券信息
            18 -> R.drawable.ic_proj_swpj//税务评级
            19 -> R.drawable.ic_proj_gdxx//购地信息
            20 -> R.drawable.ic_proj_ztb//招投标
            21 -> R.drawable.ic_proj_zzzs//资质证书
            22 -> R.drawable.ic_proj_ccjc//抽查检查
            23 -> R.drawable.ic_proj_cpxx//产品信息
            24 -> R.drawable.ic_proj_xzxk//行政许可
            25 -> R.drawable.ic_proj_qsxx//清算信息
            26 -> R.drawable.ic_proj_gsyg//公司员工
            27 -> R.drawable.ic_proj_cwzl//财务总览
            28 -> R.drawable.ic_proj_lrb//利润表
            29 -> R.drawable.ic_proj_zcfzb//资产负债表
            30 -> R.drawable.ic_proj_xjllb//现金流量表
            31 -> R.drawable.ic_proj_jckxyxx//进出口信用信息
            32 -> R.drawable.ic_proj_sbxx//商标信息
            33 -> R.drawable.ic_proj_zlxx//专利信息
            34 -> R.drawable.ic_proj_zzq//著作权
            35 -> R.drawable.ic_proj_rzq//软著权
            36 -> R.drawable.ic_proj_wzba//网站备案
            37 -> R.drawable.ic_proj_qyzs//企业证书
            38 -> R.drawable.ic_proj_gphq//股票行情
            39 -> R.drawable.ic_proj_qyjs//企业简介
            40 -> R.drawable.ic_proj_ggxx//高管信息
            41 -> R.drawable.ic_proj_cgkg//参股控股
            42 -> R.drawable.ic_proj_ssgg//上市公告
            43 -> R.drawable.ic_proj_sdgd//十大股东
            44 -> R.drawable.ic_proj_sdlt//十大流通股东
            45 -> R.drawable.ic_proj_fxxg//发行相关
            46 -> R.drawable.ic_proj_gbjg//股本结构
            47 -> R.drawable.ic_proj_gbbd//股本变动
            48 -> R.drawable.ic_proj_fhqk//分红情况
            49 -> R.drawable.ic_proj_pgqk//配股情况
            50 -> R.drawable.ic_proj_cwsj//财务数据
            51 -> R.drawable.ic_proj_gdzx//股东征信
            52 -> R.drawable.ic_proj_xmws//项目文书
            53 -> R.drawable.ic_proj_xmgy//项目概要
            54 -> R.drawable.ic_proj_xmcb//储备信息
            55 -> R.drawable.ic_proj_gzjl//跟踪记录
            56 -> R.drawable.ic_proj_jdsj//尽调数据
            57 -> R.drawable.ic_proj_tjsj//投决数据
            58 -> R.drawable.ic_proj_thglsj//投后管理数据
            59 -> R.drawable.ic_proj_spls//审批历史
            60 -> R.drawable.qqxx//股权信息
            61 -> R.drawable.cwbb
            else -> 0
        }
    }

    val colorGray = Color.parseColor("#D9D9D9")
    override fun onClick(view: View) {
        if (view.id >= 1 && view.id <= 61) {
            SoguApi.getService(application)
                    .saveClick(view.id)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeOn(Schedulers.io())
                    .subscribe({ payload ->
                        if (payload.isOk) {
                        } else {
                        }
                    }, { e ->
                    })
        }
        when (view.id) {
            38 -> StockInfoActivity.start(this@ProjectActivity, project)//股票行情
            39 -> CompanyInfoActivity.start(this@ProjectActivity, project)//企业简介
            40 -> GaoGuanActivity.start(this@ProjectActivity, project)//高管信息
            41 -> CanGuActivity.start(this@ProjectActivity, project)//参股控股
            42 -> AnnouncementActivity.start(this@ProjectActivity, project)//上市公告
            43 -> ShiDaGuDongActivity.start(this@ProjectActivity, project)//十大股东
            44 -> ShiDaLiuTongGuDongActivity.start(this@ProjectActivity, project)//十大流通
            45 -> IssueRelatedActivity.start(this@ProjectActivity, project)//发行相关
            47 -> EquityChangeActivity.start(this@ProjectActivity, project)//股本变动
            48 -> BonusInfoActivity.start(this@ProjectActivity, project)//分红情况
            49 -> AllotmentListActivity.start(this@ProjectActivity, project)//配股情况
            46 -> GuBenJieGouActivity.start(this@ProjectActivity, project)//股本结构

            1 -> BizInfoActivity.start(this@ProjectActivity, project)//工商信息
            3 -> ShareHolderInfoActivity.start(this@ProjectActivity, project)//股东信息
            8 -> QiYeLianBaoActivity.start(this@ProjectActivity, project)//企业年报
            7 -> ChangeRecordActivity.start(this@ProjectActivity, project)//变更记录
            6 -> InvestmentActivity.start(this@ProjectActivity, project)//对外投资
            5 -> KeyPersonalActivity.start(this@ProjectActivity, project)//主要人员
        //4 -> EquityStructureActivity.start(this@ProjectActivity, project)//股权结构
            9 -> BranchListActivity.start(this@ProjectActivity, project)//分支机构
            10 -> CompanyInfo2Activity.start(this@ProjectActivity, project)//公司简介

            11 -> FinanceHistoryActivity.start(this@ProjectActivity, project)//融资历史
            12 -> InvestEventActivity.start(this@ProjectActivity, project)//投资事件
            13 -> CoreTeamActivity.start(this@ProjectActivity, project)//核心团队
            14 -> BusinessEventsActivity.start(this@ProjectActivity, project)//企业业务
            15 -> ProductInfoActivity.start(this@ProjectActivity, project)//竞品信息
            16 -> RecruitActivity.start(this@ProjectActivity, project)//招聘信息

            17 -> BondActivity.start(this@ProjectActivity, project)//债券信息
            18 -> TaxRateActivity.start(this@ProjectActivity, project)//税务评级
            19 -> LandPurchaseActivity.start(this@ProjectActivity, project)//购地信息
            20 -> BidsActivity.start(this@ProjectActivity, project)//招投标
            21 -> QualificationListActivity.start(this@ProjectActivity, project)//资质证书
            22 -> CheckListActivity.start(this@ProjectActivity, project)//抽查检查
            23 -> AppListActivity.start(this@ProjectActivity, project)//产品信息

            32 -> BrandListActivity.start(this@ProjectActivity, project)//商标信息
            33 -> PatentListActivity.start(this@ProjectActivity, project)//专利信息
            34 -> CopyrightListActivity.start(this@ProjectActivity, project, 1)//软著权
            35 -> CopyrightListActivity.start(this@ProjectActivity, project, 2)//著作权
            36 -> ICPListActivity.start(this@ProjectActivity, project)//网站备案

            52 -> ProjectBookActivity.start(this@ProjectActivity, project)//项目文书
            54 -> StoreProjectAddActivity.startView(this@ProjectActivity, project)//储备信息
        //51 -> ShareholderCreditActivity.start(this@ProjectActivity, project)//高管征信（股东征信）
            51 -> {
                XmlDb.open(context).set("INNER", "TRUE")
                var first = XmlDb.open(context).get("FIRST", "TRUE")
                if (first.equals("FALSE")) {
                    ShareholderCreditActivity.start(this@ProjectActivity, project)
                } else if (first.equals("TRUE")) {
                    ShareHolderDescActivity.start(this@ProjectActivity, project, "INNER")
                    XmlDb.open(context).set("FIRST", "FALSE")
                }
            }

        // 跟踪记录,尽调数据,投决数据,投后管理数据
            55 -> RecordTraceActivity.start(this@ProjectActivity, project)//跟踪记录
            56 -> SurveyDataActivity.start(this@ProjectActivity, project)//尽调数据
            57 -> InvestSuggestActivity.start(this@ProjectActivity, project)//投决数据
            58 -> ManageDataActivity.start(this@ProjectActivity, project)//投后管理

            59 -> ApproveListActivity.start(this@ProjectActivity, null, project.company_id)//审批历史

            60 -> EquityListActivity.start(this@ProjectActivity, project)

            61 -> FinanceListActivity.start(this@ProjectActivity, project)

            R.id.im -> {
                val inflate = LayoutInflater.from(this).inflate(R.layout.layout_input_dialog1, null)
                val dialog = MaterialDialog.Builder(this)
                        .customView(inflate, false)
                        .cancelable(true)
                        .build()
                dialog.window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                val veto = inflate.find<TextView>(R.id.veto_comment)
                val confirm = inflate.find<TextView>(R.id.confirm_comment)
                val title = inflate.find<TextView>(R.id.approval_comments_title)
                title.text = "是否加入该项目讨论组?"
                veto.text = "否"
                confirm.text = "是"
                veto.setOnClickListener {
                    if (dialog.isShowing) {
                        dialog.dismiss()
                    }
                }
                confirm.setOnClickListener {
                    if (dialog.isShowing) {
                        dialog.dismiss()
                    }
                    createOrJoin()
                }
                dialog.show()
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
