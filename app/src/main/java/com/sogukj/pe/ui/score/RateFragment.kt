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
import com.sogukj.pe.bean.WeeklySendBean
import com.sogukj.pe.view.RecyclerAdapter
import com.sogukj.pe.view.RecyclerHolder
import com.sogukj.pe.view.SpaceItemDecoration
import kotlinx.android.synthetic.main.fragment_rate.*
import android.graphics.drawable.LayerDrawable
import android.graphics.drawable.Drawable
import android.databinding.adapters.ViewBindingAdapter.setPadding
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.RoundRectShape
import android.widget.TextView
import com.sogukj.pe.util.Utils
import io.reactivex.Observable
import io.reactivex.functions.Consumer
import io.reactivex.functions.Function


/**
 * a simple [Fragment] subclass.
 */
class RateFragment : BaseFragment() {

    lateinit var adapter: RecyclerAdapter<WeeklySendBean>

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

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        type = arguments.getInt(Extras.TYPE)

        adapter = RecyclerAdapter<WeeklySendBean>(context, { _adapter, parent, type0 ->
            val convertView = _adapter.getView(R.layout.item_rate, parent) as LinearLayout
            object : RecyclerHolder<WeeklySendBean>(convertView) {

                var bar = convertView.findViewById(R.id.progressBar) as ProgressBar
                var judge = convertView.findViewById(R.id.text) as TextView
                var title = convertView.findViewById(R.id.title) as TextView
                var desc = convertView.findViewById(R.id.desc) as TextView
                var lll = convertView.findViewById(R.id.lll) as LinearLayout

                override fun setData(view: View, data: WeeklySendBean, position: Int) {
                    if (type == TYPE_JOB) {
                        lll.visibility = View.GONE
                    } else if (type == TYPE_RATE) {

                    }

                    var obser = TextViewClickObservable(context, judge, bar)
                    observable_List.add(obser)

                    if (position == adapter.dataList.size - 1) {
                        Observable.combineLatest(observable_List, object : Function<Array<Any>, Boolean> {
                            override fun apply(str: Array<Any>): Boolean {
                                return true//isEmailValid(str[0].toString()) && isPasswordValid(str[1].toString())
                            }
                        }).subscribe(object : Consumer<Boolean> {
                            override fun accept(t: Boolean) {
                                if (t == true) {
                                    tv_socre.text = "98.00"
                                    btn_commit.setBackgroundColor(Color.parseColor("#FFE95C4A"))
                                    btn_commit.setOnClickListener {

                                    }
                                }
                            }
                        })
                    }
                }
            }
        })
        adapter.onItemClick = { v, p ->
        }
        val layoutManager = LinearLayoutManager(context)
        layoutManager.orientation = LinearLayoutManager.VERTICAL
        rate_list.layoutManager = layoutManager
        rate_list.addItemDecoration(SpaceItemDecoration(25))
        rate_list.adapter = adapter


        adapter.dataList.add(WeeklySendBean())
        adapter.dataList.add(WeeklySendBean())
        adapter.dataList.add(WeeklySendBean())
        adapter.notifyDataSetChanged()
    }

    val observable_List = ArrayList<Observable<CharSequence>>()
}
