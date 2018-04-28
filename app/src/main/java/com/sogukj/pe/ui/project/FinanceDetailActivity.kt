package com.sogukj.pe.ui.project

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.text.TextUtils
import android.view.View
import android.widget.CheckBox
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import com.framework.base.ToolbarActivity
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.bean.FinanceDetailBean
import com.sogukj.pe.bean.FinanceListBean
import com.sogukj.pe.util.Trace
import com.sogukj.pe.util.Utils
import com.sogukj.pe.view.RecyclerAdapter
import com.sogukj.pe.view.RecyclerHolder
import com.sogukj.pe.view.SpaceItemDecoration
import com.sogukj.service.SoguApi
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_finance_detail.*

class FinanceDetailActivity : ToolbarActivity() {

    companion object {
        fun start(ctx: Activity?, bean: FinanceListBean) {
            val intent = Intent(ctx, FinanceDetailActivity::class.java)
            intent.putExtra(Extras.DATA, bean)
            ctx?.startActivity(intent)
        }
    }

    lateinit var adapter: RecyclerAdapter<FinanceDetailBean>
    lateinit var bean: FinanceListBean

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_finance_detail)

        bean = intent.getSerializableExtra(Extras.DATA) as FinanceListBean
        setBack(true)
        setTitle("财务报表")

        subtitle.text = bean.title
        time.text = bean.issueTime

        adapter = RecyclerAdapter<FinanceDetailBean>(context, { _adapter, parent, t ->
            Holder(_adapter.getView(R.layout.finance_item, parent))
        })
        val layoutManager = LinearLayoutManager(context)
        layoutManager.orientation = LinearLayoutManager.VERTICAL
        list.addItemDecoration(SpaceItemDecoration(Utils.dpToPx(context, 10)))
        list.layoutManager = layoutManager
        list.adapter = adapter

        adapter.onItemClick = { v, p ->
            val data = adapter.dataList[p]
            //download(data.url!!, data.doc_title!!)
            if (!TextUtils.isEmpty(data.path)) {
                val intent = Intent(Intent.ACTION_VIEW)
                intent.data = Uri.parse(data.path)
                startActivity(intent)
            }
        }

        SoguApi.getService(application)
                .financialInfo(bean.fin_id!!)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ payload ->
                    if (payload.isOk) {
                        payload.payload?.forEach {
                            adapter.dataList.add(it)
                        }
                        adapter.notifyDataSetChanged()
                        if (adapter.dataList.size == 0) {
                            list_layout.visibility = View.GONE
                            iv_empty.visibility = View.VISIBLE
                        }
                    } else {
                        if (adapter.dataList.size == 0) {
                            list_layout.visibility = View.GONE
                            iv_empty.visibility = View.VISIBLE
                        }
                        showCustomToast(R.drawable.icon_toast_fail, payload.message)
                    }
                }, { e ->
                    Trace.e(e)
                    if (adapter.dataList.size == 0) {
                        list_layout.visibility = View.GONE
                        iv_empty.visibility = View.VISIBLE
                    }
                    showCustomToast(R.drawable.icon_toast_fail, "查询失败")
                })
    }

    inner class Holder(convertView: View) : RecyclerHolder<FinanceDetailBean>(convertView) {

        val text: TextView

        init {
            text = convertView.findViewById(R.id.text) as TextView
        }

        override fun setData(view: View, data: FinanceDetailBean, position: Int) {
            text.text = data.file_name
        }
    }
}
