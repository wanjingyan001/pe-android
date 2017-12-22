package com.sogukj.pe.ui.score

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.framework.base.BaseFragment
import com.google.gson.JsonSyntaxException
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.bean.*
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

    lateinit var sub_adapter: RecyclerAdapter<FKItem.THGL.ItemData>

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

        var adapter = InnerAdapter(context)
        list_.adapter = adapter


        sub_adapter = RecyclerAdapter<FKItem.THGL.ItemData>(context, { _adapter, parent, t ->
            ProjectHolderNoTitle(_adapter.getView(R.layout.item_rate, parent))
        })
        val layoutManager = LinearLayoutManager(context)
        layoutManager.orientation = LinearLayoutManager.VERTICAL
        rate_list_FK.layoutManager = layoutManager
        rate_list_FK.addItemDecoration(SpaceItemDecoration(Utils.dpToPx(context, 30)))
        rate_list_FK.adapter = sub_adapter

        person = arguments.getSerializable(Extras.DATA) as GradeCheckBean.ScoreItem
        SoguApi.getService(baseActivity!!.application)
                .perAppraisal_FK(person.user_id!!, person.type!!)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ payload ->
                    if (payload.isOk) {
                        payload.payload?.apply {
                            jdxm?.data?.forEach {
                                adapter.datalist.add(it)
                            }
                            adapter.notifyDataSetChanged()

                            fk_score.text = "${jdxm?.selfScore}"

                            thgl?.data?.forEach {
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

    val weight_list = ArrayList<Int>()
    val observable_List = ArrayList<Observable<Int>>()

    inner class ProjectHolderNoTitle(view: View)
        : RecyclerHolder<FKItem.THGL.ItemData>(view) {

        var bar = convertView.findViewById(R.id.progressBar) as ProgressBar
        var judge = convertView.findViewById(R.id.text) as TextView
        var title = convertView.findViewById(R.id.title) as TextView
        var sub_title = convertView.findViewById(R.id.subtitle) as TextView
        var desc = convertView.findViewById(R.id.desc) as TextView
        var lll = convertView.findViewById(R.id.lll) as LinearLayout

        override fun setData(view: View, data: FKItem.THGL.ItemData, position: Int) {

            title.text = data.target
            lll.visibility = View.GONE

            var obser = TextViewClickObservable(context, judge, bar)
            observable_List.add(obser)

            weight_list.add(data.weight?.toInt()!!)

            if (observable_List.size == sub_adapter.dataList.size) {
                Observable.combineLatest(observable_List, object : Function<Array<Any>, Double> {
                    override fun apply(str: Array<Any>): Double {
                        var result = 0.0
                        var date = ArrayList<Int>()//每项分数
                        for (ites in str) {
                            date.add(ites as Int)
                        }
                        for (i in weight_list.indices) {
                            //dataList[i].score = date[i]
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

                        }
                    }
                })
            }
        }

    }

    class InnerAdapter(val context: Context) : BaseAdapter() {

        val datalist = ArrayList<FKItem.JDXM.InnerData>()

        fun addAll(list: ArrayList<FKItem.JDXM.InnerData>) {
            datalist.addAll(list)
            notifyDataSetChanged()
        }

        fun add(item: FKItem.JDXM.InnerData) {
            datalist.add(item)
            notifyDataSetChanged()
        }

        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            var view = convertView
            var holder: Holder? = null
            if (view == null) {
                holder = Holder()
                view = LayoutInflater.from(context).inflate(R.layout.item_fengkong, null)
                holder?.title = view.findViewById(R.id.item_title) as TextView
                holder?.content = view.findViewById(R.id.item_content) as EditText
                view?.setTag(holder)
            } else {
                holder = view.tag as Holder
            }
            holder?.title?.text = datalist[position].target
            holder?.content?.setText(datalist[position].info)
//            var item = TouHouManageItem()
//            item.performance_id = datalist[position].performance_id
//            item.info = ""
//            touhou_tmp.add(item)
//            holder?.content?.addTextChangedListener(object : TextWatcher {
//                override fun afterTextChanged(s: Editable?) {
//                    touhou_tmp[position].info = s.toString()
//                }
//
//                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
//                }
//
//                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
//                }
//            })
            return view!!
        }

        override fun getItem(position: Int): Any {
            return datalist.get(position)
        }

        override fun getItemId(position: Int): Long {
            return position.toLong()
        }

        override fun getCount(): Int {
            return datalist.size
        }

        class Holder {
            var title: TextView? = null
            var content: EditText? = null

            var zhibiao: EditText? = null
            var condition: EditText? = null
        }
    }
}
