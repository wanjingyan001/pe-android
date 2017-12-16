package com.sogukj.pe.ui.score

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import com.bigkoo.pickerview.OptionsPickerView
import com.framework.base.ToolbarActivity
import com.sogukj.pe.R
import com.sogukj.pe.bean.WeeklySendBean
import com.sogukj.pe.view.RecyclerAdapter
import com.sogukj.pe.view.RecyclerHolder
import com.sogukj.pe.view.SpaceItemDecoration
import io.reactivex.Observable
import io.reactivex.functions.Consumer
import io.reactivex.functions.Function
import kotlinx.android.synthetic.main.fragment_rate.*
import kotlinx.android.synthetic.main.item_child.*
import org.jetbrains.anko.textColor
import org.jetbrains.anko.backgroundDrawable


class EmployeeInteractActivity : ToolbarActivity() {

    companion object {
        fun start(ctx: Context?) {
            val intent = Intent(ctx, EmployeeInteractActivity::class.java)
            ctx?.startActivity(intent)
        }
    }

    lateinit var adapter: RecyclerAdapter<WeeklySendBean>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_rate)

        setBack(true)
        setTitle("员工互评页")
        toolbar?.setBackgroundColor(Color.WHITE)
        toolbar?.apply {
            val title = this.findViewById(R.id.toolbar_title) as TextView?
            title?.textColor = Color.parseColor("#282828")
            val back = this.findViewById(R.id.toolbar_back) as ImageView
            back?.visibility = View.VISIBLE
            back.setImageResource(R.drawable.grey_back)
        }

        adapter = RecyclerAdapter<WeeklySendBean>(context, { _adapter, parent, type ->
            val convertView = _adapter.getView(R.layout.item_rate, parent) as LinearLayout
            object : RecyclerHolder<WeeklySendBean>(convertView) {

                var bar = convertView.findViewById(R.id.progressBar) as ProgressBar
                var judge = convertView.findViewById(R.id.text) as TextView
                var title = convertView.findViewById(R.id.title) as TextView
                var desc = convertView.findViewById(R.id.desc) as TextView
                var lll = convertView.findViewById(R.id.lll) as LinearLayout

                override fun setData(view: View, data: WeeklySendBean, position: Int) {
                    lll.visibility = View.GONE

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
        adapter.dataList.add(WeeklySendBean())
        adapter.dataList.add(WeeklySendBean())
        adapter.dataList.add(WeeklySendBean())
        adapter.dataList.add(WeeklySendBean())
        adapter.dataList.add(WeeklySendBean())
        adapter.notifyDataSetChanged()
    }

    val observable_List = ArrayList<Observable<CharSequence>>()
}
