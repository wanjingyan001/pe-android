package com.sogukj.pe.ui.score


import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import android.widget.LinearLayout
import android.widget.ProgressBar
import com.framework.base.BaseFragment
import com.sogukj.pe.Extras

import com.sogukj.pe.R
import com.sogukj.pe.view.RecyclerAdapter
import com.sogukj.pe.view.RecyclerHolder
import com.sogukj.pe.view.SpaceItemDecoration
import kotlinx.android.synthetic.main.fragment_rate.*
import android.graphics.Color
import android.support.v7.widget.RecyclerView
import android.widget.TextView
import com.google.gson.JsonSyntaxException
import com.sogukj.pe.bean.GradeCheckBean
import com.sogukj.pe.bean.JobPageBean
import com.sogukj.pe.bean.NormalItemBean
import com.sogukj.pe.bean.TouZiUpload
import com.sogukj.pe.util.Trace
import com.sogukj.pe.util.Utils
import com.sogukj.service.SoguApi
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.functions.Consumer
import io.reactivex.functions.Function
import io.reactivex.schedulers.Schedulers
import java.net.UnknownHostException
import kotlin.collections.ArrayList


/**
 * a simple [Fragment] subclass.
 */
class RateFragment : BaseFragment() {

    lateinit var head_adapter: RecyclerAdapter<NormalItemBean.NormalItem>
    lateinit var sub_adapter: RecyclerAdapter<NormalItemBean.NormalItem.BeanItem>

    companion object {

        //isShow  = false 打分界面，true展示界面
        fun newInstance(check_person: GradeCheckBean.ScoreItem, isShow: Boolean): RateFragment {
            val fragment = RateFragment()
            val intent = Bundle()
            intent.putBoolean(Extras.FLAG, isShow)
            intent.putSerializable(Extras.DATA, check_person)
            fragment.arguments = intent
            return fragment
        }
    }

    override val containerViewId: Int
        get() = R.layout.fragment_rate

    lateinit var person: GradeCheckBean.ScoreItem
    var isShown = false
    var hasTitle = false

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val layoutManager = LinearLayoutManager(context)
        layoutManager.orientation = LinearLayoutManager.VERTICAL
        rate_list.layoutManager = layoutManager
        rate_list.addItemDecoration(SpaceItemDecoration(25))

        person = arguments.getSerializable(Extras.DATA) as GradeCheckBean.ScoreItem
        isShown = arguments.getBoolean(Extras.FLAG) // false 打分界面，true展示界面

        SoguApi.getService(baseActivity!!.application)
                .perAppraisal_NORMAL(person.user_id!!, person.type!!)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ payload ->
                    if (payload.isOk) {
                        payload.payload?.apply {
                            if (item!![0].pName.isNullOrEmpty()) {
                                //没有title
                                sub_adapter = RecyclerAdapter<NormalItemBean.NormalItem.BeanItem>(context, { _adapter, parent, t ->
                                    ProjectHolderNoTitle(_adapter.getView(R.layout.item_rate, parent))
                                })
                                val layoutManager = LinearLayoutManager(context)
                                layoutManager.orientation = LinearLayoutManager.VERTICAL
                                rate_list.layoutManager = layoutManager
                                rate_list.addItemDecoration(SpaceItemDecoration(30))
                                rate_list.adapter = sub_adapter

                                item!![0].data?.forEach {
                                    sub_adapter.dataList.add(it)
                                }
                                sub_adapter.notifyDataSetChanged()

                                num = sub_adapter.dataList.size

                                hasTitle = false
                            } else {
                                //有title
                                head_adapter = RecyclerAdapter<NormalItemBean.NormalItem>(context, { _adapter, parent, t ->
                                    ProjectHolderTitle(_adapter.getView(R.layout.item_rate_title, parent))
                                })
                                val layoutManager = LinearLayoutManager(context)
                                layoutManager.orientation = LinearLayoutManager.VERTICAL
                                rate_list.layoutManager = layoutManager
                                rate_list.adapter = head_adapter

                                for (list in head_adapter.dataList) {
                                    num += list.data!!.size
                                }

                                hasTitle = true
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

    var num = 0

    val observable_List = ArrayList<Observable<Int>>()
    val weight_list = ArrayList<Int>()
    var dataList = ArrayList<TouZiUpload>()

    fun upload(result: Double) {

        var data = ArrayList<HashMap<String, Int>>()
        for (item in dataList) {
            val inner = HashMap<String, Int>()
            inner.put("performance_id", item.performance_id!!)
            inner.put("score", item.score!!)
            inner.put("type", item.type!!)
            data.add(inner)
        }

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

    inner class ProjectHolderNoTitle(view: View)
        : RecyclerHolder<NormalItemBean.NormalItem.BeanItem>(view) {

        var bar = convertView.findViewById(R.id.progressBar) as ProgressBar
        var judge = convertView.findViewById(R.id.text) as TextView
        var title = convertView.findViewById(R.id.title) as TextView
        var sub_title = convertView.findViewById(R.id.subtitle) as TextView
        var desc = convertView.findViewById(R.id.desc) as TextView
        var lll = convertView.findViewById(R.id.lll) as LinearLayout

        override fun setData(view: View, data: NormalItemBean.NormalItem.BeanItem, position: Int) {

            title.text = data.target

            if (hasTitle == false) {
                lll.visibility = View.GONE
            } else if (hasTitle == true) {
                if (data.info.isNullOrEmpty()) {
                    lll.visibility = View.GONE
                } else {
                    sub_title.text = data.info
                }
                //desc.text = data.desc
            }

            if (isShown) {
                bar.progress = data.score?.toInt()!!
                judge.setText(data.score)
                judge.setTextColor(Color.parseColor("#ffa0a4aa"))
                judge.setTextSize(16f)
                judge.setBackgroundDrawable(null)
            } else {
                var obser = TextViewClickObservable(context, judge, bar)
                observable_List.add(obser)

                if (data.type == 4) {//扣分项
                    weight_list.add(data.weight!!.toInt() * -1)
                } else {
                    weight_list.add(data.weight!!.toInt())
                }

                var upload = TouZiUpload()
                upload.performance_id = data.id!!.toInt()
                upload.type = data.type
                dataList.add(upload)

                if (observable_List.size == num) {
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
    }

    inner class ProjectHolderTitle(view: View)
        : RecyclerHolder<NormalItemBean.NormalItem>(view) {

        var head_ll = convertView.findViewById(R.id.ll_head) as LinearLayout
        var head_title = convertView.findViewById(R.id.head_title) as TextView
        var data_list = convertView.findViewById(R.id.listview) as RecyclerView

        override fun setData(view: View, data: NormalItemBean.NormalItem, position: Int) {

            if (data.pName == "") {
                head_ll.visibility = View.GONE
            } else {
                head_title.text = data.pName
            }

            var inner_adapter = RecyclerAdapter<NormalItemBean.NormalItem.BeanItem>(context, { _adapter, parent, t ->
                ProjectHolderNoTitle(_adapter.getView(R.layout.item_rate, parent))
            })
            inner_adapter.onItemClick = { v, p ->
            }
            val layoutManager = LinearLayoutManager(context)
            layoutManager.orientation = LinearLayoutManager.VERTICAL
            data_list.layoutManager = layoutManager
            data_list.addItemDecoration(SpaceItemDecoration(Utils.dpToPx(context, 30)))
            data_list.adapter = inner_adapter

            data.data?.forEach {
                inner_adapter.dataList.add(it)
            }
            inner_adapter.notifyDataSetChanged()
        }
    }
}
