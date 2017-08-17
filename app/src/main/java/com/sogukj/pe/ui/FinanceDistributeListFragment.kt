package com.sogukj.pe.ui

import android.os.Bundle
import android.view.View
import android.widget.TableLayout
import android.widget.TextView
import com.framework.base.BaseFragment
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.adapter.RecyclerHolder
import com.sogukj.pe.bean.NewsBean
import com.sogukj.pe.bean.ProjectBean
import com.sogukj.pe.util.Trace
import com.sogukj.pe.view.FlowLayout
import com.sogukj.service.SoguApi
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_proj_finance_distribute.*
import org.jetbrains.anko.find


/**
 * Created by qinfei on 17/7/18.
 */
class FinanceDistributeListFragment : BaseFragment() {
    override val containerViewId: Int
        get() = R.layout.fragment_proj_finance_distribute //To change initializer of created properties use File | Settings | File Templates.

    lateinit var project: ProjectBean
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        project = arguments.getSerializable(Extras.DATA) as ProjectBean
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        handler.postDelayed({
            doRequest()
        }, 100)
    }


    var page = 1
    fun doRequest() {
        SoguApi.getService(baseActivity!!.application)
                .listInvestDistribute(project.company_id!!)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ payload ->
                    if (payload.isOk) {
                        payload.payload?.apply {
                            val lunci = this["lunci"] as Map<String, Int>
                            lunci?.apply { setData(tab1, this) }
                            val hangye = this["hangye"] as Map<String, Int>
                            hangye?.apply { setData(tab2, this) }
                        }
                    } else
                        showToast(payload.message)
                }, { e ->
                    Trace.e(e)
                    showToast("暂无可用数据")
                })
    }

    fun setData(tab1: TableLayout, map: Map<String, Int>) {
        tab1.removeAllViews()
        for ((k, v) in map) {
            val convertView = View.inflate(baseActivity, R.layout.item_proj_finance_distribute, null)
            tab1.addView(convertView)

            val tag = convertView.findViewById(R.id.tag) as TextView
            val bar = convertView.findViewById(R.id.bar)
            tag.text = k
            val lp = bar.layoutParams
            lp.width = v
            bar.layoutParams = lp

        }
    }


    inner class NewsHolder(view: View)
        : RecyclerHolder<NewsBean>(view) {
        val tv_summary: TextView
        val tv_time: TextView
        val tv_from: TextView
        val tags: FlowLayout

        init {
            tv_summary = view.find(R.id.tv_summary)
            tv_time = view.find(R.id.tv_time)
            tv_from = view.find(R.id.tv_from)
            tags = view.find(R.id.tags)
        }

        override fun setData(view: View, data: NewsBean, position: Int) {
            tv_summary.text = data.title
            tv_time.text = data.time
            tv_from.text = data.source

        }

    }

    companion object {
        val TAG = NewsListFragment::class.java.simpleName

        fun newInstance(project: ProjectBean): FinanceDistributeListFragment {
            val fragment = FinanceDistributeListFragment()
            val intent = Bundle()
            intent.putSerializable(Extras.DATA, project)
            fragment.arguments = intent
            return fragment
        }
    }
}