package com.sogukj.pe.ui.score

import android.graphics.Color
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
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
import com.sogukj.pe.bean.JinDiaoItem
import com.sogukj.pe.bean.TouHouManageItem
import com.sogukj.pe.util.Trace
import com.sogukj.pe.util.Utils
import com.sogukj.pe.view.*
import com.sogukj.service.SoguApi
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.functions.Consumer
import io.reactivex.functions.Function
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_feng_kong.*
import java.net.UnknownHostException

/**
 * A simple [Fragment] subclass.
 */
class FengKongFragment : BaseFragment() {
    override val containerViewId: Int
        get() = R.layout.fragment_feng_kong

    lateinit var sub_adapter: RecyclerAdapter<InvestManageItem>

    companion object {

        fun newInstance(check_person: GradeCheckBean.ScoreItem): FengKongFragment {
            val fragment = FengKongFragment()
            val intent = Bundle()
            intent.putSerializable(Extras.DATA, check_person)
            fragment.arguments = intent
            return fragment
        }
    }

    lateinit var person: GradeCheckBean.ScoreItem

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        var adapter = FengKongAdapter(context, 0, ArrayList<JinDiaoItem>(), ArrayList<TouHouManageItem>())
        list_.adapter = adapter

//        adapter.addAll(arrayListOf(Bean(), Bean(), Bean()))
//        adapter.notifyDataSetChanged()

        sub_adapter = RecyclerAdapter<InvestManageItem>(context, { _adapter, parent, t ->
            ProjectHolderNoTitle(_adapter.getView(R.layout.item_rate, parent))
        })
        val layoutManager = LinearLayoutManager(context)
        layoutManager.orientation = LinearLayoutManager.VERTICAL
        rate_list_FK.layoutManager = layoutManager
        rate_list_FK.addItemDecoration(SpaceItemDecoration(Utils.dpToPx(context, 30)))
        rate_list_FK.adapter = sub_adapter

        person = arguments.getSerializable(Extras.DATA) as GradeCheckBean.ScoreItem
        SoguApi.getService(baseActivity!!.application)
                .perAppraisal(person.user_id!!, person.type!!)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ payload ->
                    if (payload.isOk) {
                        payload.payload?.apply {
                            sub_adapter.dataList.addAll(this)
                            sub_adapter.notifyDataSetChanged()

                            this?.forEach {
                                //maxItem += it.data!!.size
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

    val observable_List = ArrayList<Observable<Int>>()
    var num = 0

    inner class ProjectHolderNoTitle(view: View)
        : RecyclerHolder<InvestManageItem>(view) {

        var bar = convertView.findViewById(R.id.progressBar) as ProgressBar
        var judge = convertView.findViewById(R.id.text) as TextView
        var title = convertView.findViewById(R.id.title) as TextView
        var sub_title = convertView.findViewById(R.id.subtitle) as TextView
        var desc = convertView.findViewById(R.id.desc) as TextView
        var lll = convertView.findViewById(R.id.lll) as LinearLayout

        override fun setData(view: View, data: InvestManageItem, position: Int) {

//            title.text = "${data.title}(${data.percentage}%)"
//
//            if (type == RateFragment.TYPE_JOB) {
//                lll.visibility = View.GONE
//            } else if (type == RateFragment.TYPE_RATE) {
//                if (data.subtitle == "") {
//                    sub_title.visibility = View.GONE
//                } else {
//                    sub_title.text = data.subtitle
//                }
//                desc.text = data.desc
//            }
//
//            var obser = TextViewClickObservable(context, judge, bar)
//            observable_List.add(obser)
//
//            num++
//
//            if (type == RateFragment.TYPE_JOB) {
//            } else if (type == RateFragment.TYPE_RATE) {
//                if (num == maxItem) {
//                    Observable.combineLatest(observable_List, object : Function<Array<Any>, Double> {
//                        override fun apply(str: Array<Any>): Double {
//                            var result = 0.00
//                            var date = ArrayList<Int>()
//                            for (ites in str) {
//                                date.add(ites as Int)
//                            }
//                            result = date[0] * 0.2 + date[1] * 0.15 + date[2] * 0.15 + date[3] * 0.1 + date[4] * 0.1 + date[5] * 0.2 - date[6] * 0.2
//                            return result//isEmailValid(str[0].toString()) && isPasswordValid(str[1].toString())
//                        }
//                    }).subscribe(object : Consumer<Double> {
//                        override fun accept(t: Double) {
//                            tv_socre.text = "${String.format("%1$.2f", t)}"
//                            btn_commit.setBackgroundColor(Color.parseColor("#FFE95C4A"))
//                            btn_commit.setOnClickListener {
//
//                            }
//                        }
//                    })
//                }
//            }
        }

    }
}
