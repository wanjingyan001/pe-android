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
import com.sogukj.pe.util.Utils
import io.reactivex.Observable
import io.reactivex.functions.Consumer
import io.reactivex.functions.Function
import kotlin.collections.ArrayList


/**
 * a simple [Fragment] subclass.
 */
class RateFragment : BaseFragment() {

    lateinit var head_adapter: RecyclerAdapter<RateItem>
    lateinit var sub_adapter: RecyclerAdapter<RateItem.RateBean>

    companion object {
        const val TYPE_JOB = 1
        const val TYPE_RATE = 2

        fun newInstance(type: Int): RateFragment {
            val fragment = RateFragment()
            val intent = Bundle()
            intent.putInt(Extras.TYPE, type)
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
            sub_adapter = RecyclerAdapter<RateItem.RateBean>(context, { _adapter, parent, t ->
                ProjectHolderNoTitle(_adapter.getView(R.layout.item_rate, parent))
            })
            val layoutManager = LinearLayoutManager(context)
            layoutManager.orientation = LinearLayoutManager.VERTICAL
            rate_list.layoutManager = layoutManager
            rate_list.addItemDecoration(SpaceItemDecoration(30))
            rate_list.adapter = sub_adapter
        } else if (type == TYPE_RATE) {
            ItemType = 1
            head_adapter = RecyclerAdapter<RateItem>(context, { _adapter, parent, t ->
                ProjectHolderTitle(_adapter.getView(R.layout.item_rate_title, parent))
            })
            val layoutManager = LinearLayoutManager(context)
            layoutManager.orientation = LinearLayoutManager.VERTICAL
            rate_list.layoutManager = layoutManager
            rate_list.adapter = head_adapter
        }
//        adapter.onItemClick = { v, p ->
//        }


//        adapter.dataList.add(WeeklySendBean())
//        adapter.dataList.add(WeeklySendBean())
//        adapter.dataList.add(WeeklySendBean())
//        adapter.dataList.add(WeeklySendBean())
//        adapter.dataList.add(WeeklySendBean())
//        adapter.notifyDataSetChanged()

        if (ItemType == 0) {
            maxItem = 0
        } else if (ItemType == 1) {
            var rateItem = RateItem()
            rateItem.head_title = ""
            var rate1 = RateItem.RateBean()
            rate1.title = "基金募集"
            rate1.percentage = "20"
            rate1.subtitle = "描述:"
            rate1.desc = "完成基金募集，包括：潜在投资人挖掘、编写募集说明书、制作公司宣传材料、基础法律文件等。"
            rateItem.list.add(rate1)
            var rate2 = RateItem.RateBean()
            rate2.title = "渠道开发"
            rate2.percentage = "15"
            rate2.subtitle = "描述:"
            rate2.desc = "对外开发外部渠道，包括：拜访潜在上市公司、机构投资人及高净值客户。"
            rateItem.list.add(rate2)
            var rate3 = RateItem.RateBean()
            rate3.title = "基金业务"
            rate3.percentage = "15"
            rate3.subtitle = "描述:"
            rate3.desc = "完成基金募集说明书、合伙协议、路演等材料的制作；协助完成基金设立工作，包括政策信息搜集、各地政府机关、基金投资的沟通、工商注册等。"
            rateItem.list.add(rate3)
            var rate4 = RateItem.RateBean()
            rate4.title = "现有渠道维护"
            rate4.percentage = "10"
            rate4.subtitle = "描述:"
            rate4.desc = "维护现有投资人渠道"
            rateItem.list.add(rate4)
            var rate5 = RateItem.RateBean()
            rate5.title = "其他"
            rate5.percentage = "10"
            rate5.subtitle = "描述:"
            rate5.desc = "PPT材料制作、政策信息搜集、参加行业论坛及会议。"
            rateItem.list.add(rate5)

            var rateItem1 = RateItem()
            rateItem1.head_title = "加分项"
            var rateBean1 = RateItem.RateBean()
            rateBean1.title = "承担岗位外任务"
            rateBean1.percentage = "20"
            rateBean1.subtitle = ""
            rateBean1.desc = "每个岗位外任务 5分，最高10分"
            rateItem1.list.add(rateBean1)

            var rateItem2 = RateItem()
            rateItem2.head_title = "扣分项"
            var rateBean2 = RateItem.RateBean()
            rateBean2.title = "尽调不充分"
            rateBean2.percentage = "20"
            rateBean2.subtitle = ""
            rateBean2.desc = "每个扣20分，最高扣20分"
            rateItem2.list.add(rateBean2)


            var list = ArrayList<RateItem>()
            list.add(rateItem)
            list.add(rateItem1)
            list.add(rateItem2)
            head_adapter.dataList.addAll(list)
            head_adapter.notifyDataSetChanged()

            maxItem = 7
        }
    }

    val observable_List = ArrayList<Observable<Int>>()
    var num = 0

    inner class ProjectHolderNoTitle(view: View)
        : RecyclerHolder<RateItem.RateBean>(view) {

        var bar = convertView.findViewById(R.id.progressBar) as ProgressBar
        var judge = convertView.findViewById(R.id.text) as TextView
        var title = convertView.findViewById(R.id.title) as TextView
        var sub_title = convertView.findViewById(R.id.subtitle) as TextView
        var desc = convertView.findViewById(R.id.desc) as TextView
        var lll = convertView.findViewById(R.id.lll) as LinearLayout

        override fun setData(view: View, data: RateItem.RateBean, position: Int) {

            title.text = "${data.title}(${data.percentage}%)"

            if (type == TYPE_JOB) {
                lll.visibility = View.GONE
            } else if (type == TYPE_RATE) {
                if (data.subtitle == "") {
                    sub_title.visibility = View.GONE
                } else {
                    sub_title.text = data.subtitle
                }
                desc.text = data.desc
            }

            var obser = TextViewClickObservable(context, judge, bar)
            observable_List.add(obser)

            num++

            if (type == TYPE_JOB) {
            } else if (type == TYPE_RATE) {
                if (num == maxItem) {
                    Observable.combineLatest(observable_List, object : Function<Array<Any>, Double> {
                        override fun apply(str: Array<Any>): Double {
                            var result = 0.00
                            var date = ArrayList<Int>()
                            for (ites in str) {
                                date.add(ites as Int)
                            }
                            result = date[0] * 0.2 + date[1] * 0.15 + date[2] * 0.15 + date[3] * 0.1 + date[4] * 0.1 + date[5] * 0.2 - date[6] * 0.2
                            return result//isEmailValid(str[0].toString()) && isPasswordValid(str[1].toString())
                        }
                    }).subscribe(object : Consumer<Double> {
                        override fun accept(t: Double) {
                            tv_socre.text = "${String.format("%1$.2f", t)}"
                            btn_commit.setBackgroundColor(Color.parseColor("#FFE95C4A"))
                            btn_commit.setOnClickListener {

                            }
                        }
                    })
                }
            }
        }

    }

    inner class ProjectHolderTitle(view: View)
        : RecyclerHolder<RateItem>(view) {

        var head_ll = convertView.findViewById(R.id.ll_head) as LinearLayout
        var head_title = convertView.findViewById(R.id.head_title) as TextView
        var data_list = convertView.findViewById(R.id.listview) as RecyclerView

        override fun setData(view: View, data: RateItem, position: Int) {

            if (data.head_title == "") {
                head_ll.visibility = View.GONE
            } else {
                head_title.text = data.head_title
            }

            var inner_adapter = RecyclerAdapter<RateItem.RateBean>(context, { _adapter, parent, t ->
                ProjectHolderNoTitle(_adapter.getView(R.layout.item_rate, parent))
            })
            inner_adapter.onItemClick = { v, p ->
            }
            val layoutManager = LinearLayoutManager(context)
            layoutManager.orientation = LinearLayoutManager.VERTICAL
            data_list.layoutManager = layoutManager
            data_list.addItemDecoration(SpaceItemDecoration(Utils.dpToPx(context, 30)))
            data_list.adapter = inner_adapter

            inner_adapter.dataList.addAll(data.list)
            inner_adapter.notifyDataSetChanged()
        }
    }

    class RateItem : Any() {
        var head_title = ""
        var list = ArrayList<RateBean>()

        class RateBean : Any() {
            var title = ""
            var percentage = ""//百分比整数
            var subtitle = ""
            var desc = ""
        }
    }
}
