package com.sogukj.pe.ui.project

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Bundle
import android.support.v4.graphics.drawable.RoundedBitmapDrawable
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory
import android.support.v7.widget.LinearLayoutManager
import android.text.TextUtils
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.transition.Transition
import com.framework.base.ToolbarActivity
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.bean.EquityListBean
import com.sogukj.pe.bean.FinanceListBean
import com.sogukj.pe.bean.ProjectBean
import com.sogukj.pe.util.Trace
import com.sogukj.pe.util.Utils
import com.sogukj.pe.view.*
import com.sogukj.service.SoguApi
import com.sougukj.setOnClickFastListener
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_equity_list.*

class FinanceListActivity : ToolbarActivity() {

    companion object {
        fun start(ctx: Activity?, project: ProjectBean) {
            val intent = Intent(ctx, FinanceListActivity::class.java)
            intent.putExtra(Extras.DATA, project)
            ctx?.startActivity(intent)
        }
    }

    lateinit var adapter: RecyclerAdapter<FinanceListBean>
    lateinit var project: ProjectBean

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_equity_list)
        setBack(true)
        title = "财务报表"

        project = intent.getSerializableExtra(Extras.DATA) as ProjectBean
        adapter = RecyclerAdapter<FinanceListBean>(context, { _adapter, parent, t ->
            Holder(_adapter.getView(R.layout.equity_item, parent))
        })
        val layoutManager = LinearLayoutManager(context)
        layoutManager.orientation = LinearLayoutManager.VERTICAL
        list.addItemDecoration(SpaceItemDecoration(Utils.dpToPx(context, 10)))
        list.layoutManager = layoutManager
        list.adapter = adapter

        SoguApi.getService(application)
                .financialList(project.company_id!!)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ payload ->
                    if (payload.isOk) {
                        payload.payload?.forEach {
                            adapter.dataList.add(it)
                        }
                        adapter.notifyDataSetChanged()
                        if (adapter.dataList.size == 0) {
                            list.visibility = View.GONE
                            iv_empty.visibility = View.VISIBLE
                        }
                    } else {
                        if (adapter.dataList.size == 0) {
                            list.visibility = View.GONE
                            iv_empty.visibility = View.VISIBLE
                        }
                        showCustomToast(R.drawable.icon_toast_fail, payload.message)
                    }
                }, { e ->
                    showCustomToast(R.drawable.icon_toast_fail, "查询失败")
                    if (adapter.dataList.size == 0) {
                        list.visibility = View.GONE
                        iv_empty.visibility = View.VISIBLE
                    }
                    Trace.e(e)
                })
    }

    inner class Holder(convertView: View) : RecyclerHolder<FinanceListBean>(convertView) {

        val root: LinearLayout
        val tuxiang: CircleImageView
        val record_title: TextView
        val state_modify: TextView
        val state_create: TextView
        val record_time: TextView

        init {
            root = convertView.findViewById(R.id.root) as LinearLayout
            tuxiang = convertView.findViewById(R.id.tuxiang) as CircleImageView
            record_title = convertView.findViewById(R.id.record_title) as TextView
            state_modify = convertView.findViewById(R.id.state_modify) as TextView
            state_create = convertView.findViewById(R.id.state_create) as TextView
            record_time = convertView.findViewById(R.id.record_time) as TextView
        }

        override fun setData(view: View, data: FinanceListBean, position: Int) {
            tuxiang.setImageResource(R.drawable.cw_icon)
            record_title.text = data.title

            state_modify.visibility = View.GONE
            state_create.visibility = View.GONE

            record_time.text = data.issueTime
            root.setOnClickListener {
                FinanceDetailActivity.start(context, data)
            }
        }
    }
}
