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
import com.sogukj.pe.bean.JobPageBean
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

    //lateinit var head_adapter: RecyclerAdapter<RateItem>
    lateinit var sub_adapter: RecyclerAdapter<JobPageBean.PageItem>

    companion object {
        const val TYPE_JOB = 1
        const val TYPE_RATE = 2

        fun newInstance(type: Int, id: Int): RateFragment {
            val fragment = RateFragment()
            val intent = Bundle()
            intent.putInt(Extras.TYPE, type)
            intent.putInt(Extras.DATA, id)
            fragment.arguments = intent
            return fragment
        }
    }

    override val containerViewId: Int
        get() = R.layout.fragment_rate

    var type = 0
    var ItemType = 0//0--无title，1-有title
    var maxItem = 0

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val layoutManager = LinearLayoutManager(context)
        layoutManager.orientation = LinearLayoutManager.VERTICAL
        rate_list.layoutManager = layoutManager
        rate_list.addItemDecoration(SpaceItemDecoration(25))

        type = arguments.getInt(Extras.TYPE)
        if (type == TYPE_JOB) {
            ItemType = 0
            sub_adapter = RecyclerAdapter<JobPageBean.PageItem>(context, { _adapter, parent, t ->
                ProjectHolderNoTitle(_adapter.getView(R.layout.item_rate, parent))
            })
            val layoutManager = LinearLayoutManager(context)
            layoutManager.orientation = LinearLayoutManager.VERTICAL
            rate_list.layoutManager = layoutManager
            rate_list.addItemDecoration(SpaceItemDecoration(30))
            rate_list.adapter = sub_adapter
            doRequest()
        } else if (type == TYPE_RATE) {
//            ItemType = 1
//            head_adapter = RecyclerAdapter<RateItem>(context, { _adapter, parent, t ->
//                ProjectHolderTitle(_adapter.getView(R.layout.item_rate_title, parent))
//            })
//            val layoutManager = LinearLayoutManager(context)
//            layoutManager.orientation = LinearLayoutManager.VERTICAL
//            rate_list.layoutManager = layoutManager
//            rate_list.adapter = head_adapter
        }
//        adapter.onItemClick = { v, p ->
//        }


//        if (ItemType == 0) {
//            maxItem = 0
//        } else if (ItemType == 1) {
//            var rateItem = RateItem()
//            rateItem.head_title = ""
//            var rate1 = RateItem.RateBean()
//            rate1.title = "基金募集"
//            rate1.percentage = "20"
//            rate1.subtitle = "描述:"
//            rate1.desc = "完成基金募集，包括：潜在投资人挖掘、编写募集说明书、制作公司宣传材料、基础法律文件等。"
//            rateItem.list.add(rate1)
//            var rate2 = RateItem.RateBean()
//            rate2.title = "渠道开发"
//            rate2.percentage = "15"
//            rate2.subtitle = "描述:"
//            rate2.desc = "对外开发外部渠道，包括：拜访潜在上市公司、机构投资人及高净值客户。"
//            rateItem.list.add(rate2)
//            var rate3 = RateItem.RateBean()
//            rate3.title = "基金业务"
//            rate3.percentage = "15"
//            rate3.subtitle = "描述:"
//            rate3.desc = "完成基金募集说明书、合伙协议、路演等材料的制作；协助完成基金设立工作，包括政策信息搜集、各地政府机关、基金投资的沟通、工商注册等。"
//            rateItem.list.add(rate3)
//            var rate4 = RateItem.RateBean()
//            rate4.title = "现有渠道维护"
//            rate4.percentage = "10"
//            rate4.subtitle = "描述:"
//            rate4.desc = "维护现有投资人渠道"
//            rateItem.list.add(rate4)
//            var rate5 = RateItem.RateBean()
//            rate5.title = "其他"
//            rate5.percentage = "10"
//            rate5.subtitle = "描述:"
//            rate5.desc = "PPT材料制作、政策信息搜集、参加行业论坛及会议。"
//            rateItem.list.add(rate5)
//
//            var rateItem1 = RateItem()
//            rateItem1.head_title = "加分项"
//            var rateBean1 = RateItem.RateBean()
//            rateBean1.title = "承担岗位外任务"
//            rateBean1.percentage = "20"
//            rateBean1.subtitle = ""
//            rateBean1.desc = "每个岗位外任务 5分，最高10分"
//            rateItem1.list.add(rateBean1)
//
//            var rateItem2 = RateItem()
//            rateItem2.head_title = "扣分项"
//            var rateBean2 = RateItem.RateBean()
//            rateBean2.title = "尽调不充分"
//            rateBean2.percentage = "20"
//            rateBean2.subtitle = ""
//            rateBean2.desc = "每个扣20分，最高扣20分"
//            rateItem2.list.add(rateBean2)
//
//
//            var list = ArrayList<RateItem>()
//            list.add(rateItem)
//            list.add(rateItem1)
//            list.add(rateItem2)
//            head_adapter.dataList.addAll(list)
//            head_adapter.notifyDataSetChanged()
//
//            maxItem = 7
//        }
    }

    fun doRequest() {
        //ItemType = 0  岗位胜任力
        // 1
        var id = arguments.getInt(Extras.DATA, 0)
        SoguApi.getService(baseActivity!!.application)
                .showJobPage(id)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ payload ->
                    if (payload.isOk) {
                        payload.payload?.apply {
                            data?.forEach {
                                sub_adapter.dataList.add(it)
                            }
                            sub_adapter.notifyDataSetChanged()
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

        var type = arguments.getInt(Extras.TYPE)

        val params = HashMap<String, Any>()
        params.put("data", data)
        params.put("user_id", arguments.getInt(Extras.DATA, 0))
        if (type == 1) {//TYPE_JOB
            params.put("type", 2)
        } else if (type == 2) {//TYPE_RATE
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

    inner class ProjectHolderNoTitle(view: View)
        : RecyclerHolder<JobPageBean.PageItem>(view) {

        var bar = convertView.findViewById(R.id.progressBar) as ProgressBar
        var judge = convertView.findViewById(R.id.text) as TextView
        var title = convertView.findViewById(R.id.title) as TextView
        var sub_title = convertView.findViewById(R.id.subtitle) as TextView
        var desc = convertView.findViewById(R.id.desc) as TextView
        var lll = convertView.findViewById(R.id.lll) as LinearLayout

        override fun setData(view: View, data: JobPageBean.PageItem, position: Int) {

            title.text = data.name

            if (type == TYPE_JOB) {
                lll.visibility = View.GONE
            } else if (type == TYPE_RATE) {
//                if (data.subtitle == "") {
//                    sub_title.visibility = View.GONE
//                } else {
//                    sub_title.text = data.subtitle
//                }
//                desc.text = data.desc
            }

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

            num++

            if (type == TYPE_JOB) {
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
            } else if (type == TYPE_RATE) {
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
            }
        }

    }

//    inner class ProjectHolderTitle(view: View)
//        : RecyclerHolder<RateItem>(view) {
//
//        var head_ll = convertView.findViewById(R.id.ll_head) as LinearLayout
//        var head_title = convertView.findViewById(R.id.head_title) as TextView
//        var data_list = convertView.findViewById(R.id.listview) as RecyclerView
//
//        override fun setData(view: View, data: RateItem, position: Int) {
//
//            if (data.head_title == "") {
//                head_ll.visibility = View.GONE
//            } else {
//                head_title.text = data.head_title
//            }
//
//            var inner_adapter = RecyclerAdapter<RateItem.RateBean>(context, { _adapter, parent, t ->
//                ProjectHolderNoTitle(_adapter.getView(R.layout.item_rate, parent))
//            })
//            inner_adapter.onItemClick = { v, p ->
//            }
//            val layoutManager = LinearLayoutManager(context)
//            layoutManager.orientation = LinearLayoutManager.VERTICAL
//            data_list.layoutManager = layoutManager
//            data_list.addItemDecoration(SpaceItemDecoration(Utils.dpToPx(context, 30)))
//            data_list.adapter = inner_adapter
//
//            inner_adapter.dataList.addAll(data.list)
//            inner_adapter.notifyDataSetChanged()
//        }
//    }
}
