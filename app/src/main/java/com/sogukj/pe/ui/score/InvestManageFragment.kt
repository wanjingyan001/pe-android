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
import com.framework.base.BaseFragment
import com.google.gson.JsonSyntaxException
import com.sogukj.pe.Extras

import com.sogukj.pe.R
import com.sogukj.pe.bean.GradeCheckBean
import com.sogukj.pe.bean.InvestManageItem
import com.sogukj.pe.bean.TouZiUpload
import com.sogukj.pe.ui.score.RateFragment.Companion.TYPE_JOB
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
import java.net.UnknownHostException


/**
 * A simple [Fragment] subclass.投资部关键绩效
 */
class InvestManageFragment : BaseFragment() {

    override val containerViewId: Int
        get() = R.layout.fragment_invest_manage

    companion object {
        //type 决定是岗位胜任力和关键绩效
        fun newInstance(check_person: GradeCheckBean.ScoreItem, type: Int): InvestManageFragment {
            val fragment = InvestManageFragment()
            val intent = Bundle()
            intent.putSerializable(Extras.DATA, check_person)
            intent.putInt(Extras.TYPE, type)
            fragment.arguments = intent
            return fragment
        }
    }

    lateinit var invest_adapter: RecyclerAdapter<InvestManageItem>
    lateinit var person: GradeCheckBean.ScoreItem

    var type = 0
    val TYPE_WAIT = 1
    val TYPE_END = 2

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
        SoguApi.getService(baseActivity!!.application)
                .perAppraisal(person.user_id!!, person.type!!)
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
                        }
                    } else
                        showToast(payload.message)
                }, { e ->
                    Trace.e(e)
                    when (e) {
                        is JsonSyntaxException -> showToast("后台数据出错")
                        is UnknownHostException -> showToast("网络出错")
                        else -> showToast("未知错误")
                    }
                })
    }

    var maxItem = 0

    inner class ProjectHolder(view: View)
        : RecyclerHolder<InvestManageItem>(view) {

        var head_title = convertView.findViewById(R.id.head_title) as TextView
        var list = convertView.findViewById(R.id.listview) as RecyclerView

        override fun setData(view: View, data: InvestManageItem, position: Int) {
            head_title.text = data.pName
            head_title.setOnClickListener {
                Log.e("最外层", "最外层")
            }
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
            touzibiaozhun.text = data.standard
            shijiqingkuang.text = data.info

            if (data.standard.isNullOrEmpty()) {
                touzibiaozhun.visibility = View.GONE
            }
            if (data.info.isNullOrEmpty()) {
                shijiqingkuang.visibility = View.GONE
            }
            if (layout.childCount == 0) {
                layout.visibility = View.GONE
            }

            //1=>关键绩效指标评价 2=>岗位胜任力评价 3=>加分项 4=>减分项
            if (data.type == 4) {
                var obser = TextViewClickObservableMinus(context, txt_btn, progressBar, data.total_score!!, data.offset!!)
                observable_List.add(obser)
            } else if (data.type == 3) {
                var obser = TextViewClickObservableAdd(context, txt_btn, progressBar, data.total_score!!, data.offset!!)
                observable_List.add(obser)
            } else {
                var obser = TextViewClickObservable(context, txt_btn, progressBar)
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
                            upload(t)
                        }
                    }
                })
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
        if (type == 18) {//TYPE_GANGWEI
            params.put("type", 2)
        } else if (type == 19) {//TYPE_JIXIAO
            params.put("type", 1)
        }
        params.put("total", result)

        SoguApi.getService(baseActivity!!.application)
                .giveGrade(params)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ payload ->
                    if (payload.isOk) {
                        baseActivity?.finish()
                    } else
                        showToast(payload.message)
                }, { e ->
                    Trace.e(e)
                    when (e) {
                        is JsonSyntaxException -> showToast("后台数据出错")
                        is UnknownHostException -> showToast("网络出错")
                        else -> showToast("未知错误")
                    }
                })
    }

    val weight_list = ArrayList<Int>()
    val observable_List = ArrayList<Observable<Int>>()
    var dataList = ArrayList<TouZiUpload>()
}
