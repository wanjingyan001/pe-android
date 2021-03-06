package com.sogukj.pe.ui.score

import android.graphics.Color
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.View
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.Theme
import com.bumptech.glide.Glide
import com.framework.base.BaseFragment
import com.google.gson.JsonSyntaxException
import com.sogukj.pe.Extras

import com.sogukj.pe.R
import com.sogukj.pe.bean.GradeCheckBean
import com.sogukj.pe.bean.InvestManageItem
import com.sogukj.pe.bean.PFBZ
import com.sogukj.pe.bean.TouZiUpload
import com.sogukj.pe.util.MyGlideUrl
import com.sogukj.pe.util.Trace
import com.sogukj.pe.util.Utils
import com.sogukj.pe.view.RecyclerAdapter
import com.sogukj.pe.view.RecyclerHolder
import com.sogukj.pe.view.SpaceItemDecoration
import com.sogukj.service.SoguApi
import io.reactivex.Observable
import io.reactivex.functions.Function
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.functions.Consumer
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_invest_manage.*
import kotlinx.android.synthetic.main.header.*
import java.net.UnknownHostException


/**
 * A simple [Fragment] subclass.投资部关键绩效
 */
class InvestManageFragment : BaseFragment() {

    override val containerViewId: Int
        get() = R.layout.fragment_invest_manage

    companion object {
        //isShow  = false 打分界面，true展示界面
        fun newInstance(check_person: GradeCheckBean.ScoreItem, isShow: Boolean): InvestManageFragment {
            val fragment = InvestManageFragment()
            val intent = Bundle()
            intent.putBoolean(Extras.FLAG, isShow)
            intent.putSerializable(Extras.DATA, check_person)
            fragment.arguments = intent
            return fragment
        }
    }

    lateinit var invest_adapter: RecyclerAdapter<InvestManageItem>

    lateinit var person: GradeCheckBean.ScoreItem
    var isShown = false

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        invest_adapter = RecyclerAdapter<InvestManageItem>(context, { _adapter, parent, t ->
            ProjectHolder(_adapter.getView(R.layout.item_rate_title, parent))
        })
        val layoutManager = LinearLayoutManager(context)
        layoutManager.orientation = LinearLayoutManager.VERTICAL
        invest_list_fragment.layoutManager = layoutManager
        //invest_list.addItemDecoration(SpaceItemDecoration(Utils.dpToPx(context, 30)))
        invest_list_fragment.adapter = invest_adapter

        person = arguments.getSerializable(Extras.DATA) as GradeCheckBean.ScoreItem
        person?.let {
            if(it.url.isNullOrEmpty()){
                val ch = it.name?.first()
                icon.setChar(ch)
            } else {
                Glide.with(context).load(MyGlideUrl(it.url)).into(icon)
            }
            name.text = it.name
            depart.text = it.department
            position.text = it.position
        }
        isShown = arguments.getBoolean(Extras.FLAG) // false 打分界面，true展示界面
        SoguApi.getService(baseActivity!!.application)
                .perAppraisal_TZ(person.user_id!!, person.type!!)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ payload ->
                    if (payload.isOk) {
                        payload.payload?.apply {
                            invest_adapter.dataList.addAll(this)
                            invest_adapter.notifyDataSetChanged()

                            this?.forEach {
                                maxItem += it.data!!.size
                            }
                            pinfen = this.get(0).pfbz!!
                        }
                        if (isShown) {
                            tv_socre.text = payload.total as String
                            btn_commit.visibility = View.GONE
                        }
                    } else
                        showCustomToast(R.drawable.icon_toast_fail, payload.message)
                }, { e ->
                    Trace.e(e)
                    ToastError(e)
                })
    }

    var maxItem = 0
    var pinfen = ArrayList<PFBZ>()

    inner class ProjectHolder(view: View)
        : RecyclerHolder<InvestManageItem>(view) {

        var head_title = convertView.findViewById(R.id.head_title) as TextView
        var list = convertView.findViewById(R.id.listview) as RecyclerView

        override fun setData(view: View, data: InvestManageItem, position: Int) {
            head_title.text = data.pName
            //invest_item
            var inner_adapter = RecyclerAdapter<InvestManageItem.InvestManageInnerItem>(context, { _adapter, parent, t ->
                ProjectHolderNoTitle(_adapter.getView(R.layout.fragment_item_invest, parent))
            })
            val layoutManager = LinearLayoutManager(context)
            layoutManager.orientation = LinearLayoutManager.VERTICAL
            list.layoutManager = layoutManager
            list.addItemDecoration(SpaceItemDecoration(Utils.dpToPx(context, 30)))
            list.adapter = inner_adapter

            data.data?.forEach {
                inner_adapter.dataList.add(it)
            }
            inner_adapter.notifyDataSetChanged()
        }
    }

    inner class ProjectHolderNoTitle(view: View)
        : RecyclerHolder<InvestManageItem.InvestManageInnerItem>(view) {

        var zhibiao = convertView.findViewById(R.id.title) as TextView
        var touzibiaozhun = convertView.findViewById(R.id.subtitle) as TextView
        var shijiqingkuang = convertView.findViewById(R.id.desc) as TextView
        var layout = convertView.findViewById(R.id.lll) as LinearLayout

        var progressBar = convertView.findViewById(R.id.progressBar) as ProgressBar
        var txt_btn = convertView.findViewById(R.id.text) as TextView

        override fun setData(view: View, data: InvestManageItem.InvestManageInnerItem, position: Int) {
            zhibiao.text = "指标${position + 1}：${data.target}"
            touzibiaozhun.text = "投资标准：${data.standard}"
            shijiqingkuang.text = "实际情况：${data.info}"

            if (data.standard.isNullOrEmpty()) {
                touzibiaozhun.visibility = View.GONE
            }
            if (data.info.isNullOrEmpty()) {
                shijiqingkuang.visibility = View.GONE
            }
            if (layout.childCount == 0) {
                layout.visibility = View.GONE
            }

            if (isShown) {
                var score = data.score?.toInt()!!
                progressBar.progress = score
                //1=>关键绩效指标评价 2=>岗位胜任力评价 3=>加分项 4=>减分项
                if (data.type == 4) {
                    progressBar.progressDrawable = context.resources.getDrawable(R.drawable.pb_min)
                } else if (data.type == 3) {
                    progressBar.progressDrawable = context.resources.getDrawable(R.drawable.pb_add)
                } else {
                    if (score >= 101 && score <= 120) {
                        progressBar.progressDrawable = context.resources.getDrawable(R.drawable.pb_a)
                    } else if (score >= 81 && score <= 100) {
                        progressBar.progressDrawable = context.resources.getDrawable(R.drawable.pb_b)
                    } else if (score >= 61 && score <= 80) {
                        progressBar.progressDrawable = context.resources.getDrawable(R.drawable.pb_c)
                    } else if (score >= 0 && score <= 60) {
                        progressBar.progressDrawable = context.resources.getDrawable(R.drawable.pb_d)
                    }
                }
                txt_btn.setText(data.score)
                txt_btn.setTextColor(Color.parseColor("#ffa0a4aa"))
                txt_btn.setTextSize(16f)
                txt_btn.setBackgroundDrawable(null)
            } else {
                //1=>关键绩效指标评价 2=>岗位胜任力评价 3=>加分项 4=>减分项
                if (data.type == 4) {
                    var obser = TextViewClickObservableAddOrMinus(context, txt_btn, progressBar, data.total_score!!, data.offset!!, R.drawable.pb_min)
                    observable_List.add(obser)
                } else if (data.type == 3) {
                    var obser = TextViewClickObservableAddOrMinus(context, txt_btn, progressBar, data.total_score!!, data.offset!!, R.drawable.pb_add)
                    observable_List.add(obser)
                } else {
                    var obser = TextViewClickObservable(context, txt_btn, progressBar, pinfen)
                    observable_List.add(obser)
                }

                if (data.type == 4) {//扣分项
                    weight_list.add(data.weight!! * -1)
                } else {
                    weight_list.add(data.weight!!)
                }

                var upload = TouZiUpload()
                upload.performance_id = data.id
                upload.type = data.type
                dataList.add(upload)

                if (observable_List.size == maxItem) {
                    Observable.combineLatest(observable_List, object : Function<Array<Any>, Double> {
                        override fun apply(str: Array<Any>): Double {
                            var result = 0.0
                            var date = ArrayList<Int>()//每项分数
                            for (ites in str) {
                                date.add(ites as Int)
                            }
                            for (i in weight_list.indices) {
                                dataList[i].score = date[i]
                                var single = date[i].toDouble() * weight_list[i] / 100
                                result += single
                            }
                            return result//isEmailValid(str[0].toString()) && isPasswordValid(str[1].toString())
                        }
                    }).subscribe(object : Consumer<Double> {
                        override fun accept(t: Double) {
                            tv_socre.text = "${String.format("%1$.2f", t)}"
                            btn_commit.setBackgroundColor(Color.parseColor("#FFE95C4A"))
                            btn_commit.setOnClickListener {
                                MaterialDialog.Builder(context)
                                        .theme(Theme.LIGHT)
                                        .title("提示")
                                        .content("确定要提交分数?")
                                        .onPositive { materialDialog, dialogAction ->
                                            upload(t)
                                        }
                                        .positiveText("确定")
                                        .negativeText("取消")
                                        .show()
                            }
                        }
                    })
                }
            }
        }
    }

    fun upload(result: Double) {

        var data = ArrayList<HashMap<String, Int>>()
        for (item in dataList) {
            val inner = HashMap<String, Int>()
            inner.put("performance_id", item.performance_id!!)
            inner.put("score", item.score!!)
            inner.put("type", item.type!!)
            data.add(inner)
        }

        var type = arguments.getInt(Extras.TYPE)

        val params = HashMap<String, Any>()
        params.put("data", data)
        params.put("user_id", person.user_id!!)
        params.put("type", 1)
        params.put("total", result)

        SoguApi.getService(baseActivity!!.application)
                .giveGrade(params)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ payload ->
                    if (payload.isOk) {
                        baseActivity?.finish()
                    } else
                        showCustomToast(R.drawable.icon_toast_fail, payload.message)
                }, { e ->
                    Trace.e(e)
                    ToastError(e)
                })
    }

    val weight_list = ArrayList<Int>()
    val observable_List = ArrayList<Observable<Int>>()
    var dataList = ArrayList<TouZiUpload>()
}
