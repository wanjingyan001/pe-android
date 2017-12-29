package com.sogukj.pe.ui.approve

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.bumptech.glide.Glide
import com.framework.base.ToolbarActivity
import com.google.gson.Gson
import com.sogukj.pe.R
import com.sogukj.pe.bean.SpGroupBean
import com.sogukj.pe.bean.SpGroupItemBean
import com.sogukj.pe.util.Trace
import com.sogukj.service.SoguApi
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_approve.*
import java.text.SimpleDateFormat
/**
 * Created by qinfei on 17/10/18.
 */
class EntryApproveActivity : ToolbarActivity() {

    val fmt = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
    val gson = Gson()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_approve)
        setBack(true)
        title = "审批"
        ll_custom.removeAllViews()
        SoguApi.getService(application)
                .mainApprove(3)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ payload ->
                    if (payload.isOk) {
                        initView(payload.payload)
                    } else
                        showToast(payload.message)
                }, { e ->
                    Trace.e(e)
                    showToast("暂无可用数据")
                })

        item_dwsp.setOnClickListener {
            ApproveListActivity.start(this, 1)
        }
        item_wfqd.setOnClickListener {
            ApproveListActivity.start(this, 3)
        }
    }

    fun initView(payload: List<SpGroupBean>?) {
        if (payload == null) return
        val inflater = LayoutInflater.from(this)
        payload.forEach { spGroupBean ->
            val groupView = inflater.inflate(R.layout.cs_group, null, false) as LinearLayout
            ll_custom.addView(groupView)
            groupView.removeAllViews()
//            val groupDivider = inflater.inflate(R.layout.cs_group_divider, null, false) as View
            val groupHeader = inflater.inflate(R.layout.cs_group_header, null, false) as LinearLayout
//            groupView.addView(groupDivider)
            groupView.addView(groupHeader)
            val tv_title = groupHeader.findViewById(R.id.tv_title) as TextView
            tv_title.text = spGroupBean.title

            val items = spGroupBean.item
            if (null != items && items.isNotEmpty()) {
                val gridRow = inflater.inflate(R.layout.cs_grid_row3, null, false) as LinearLayout
                groupView.addView(gridRow)

                setGridItem(items!!.getOrNull(2), gridRow.getChildAt(2)!!, spGroupBean)
                setGridItem(items!!.getOrNull(1), gridRow.getChildAt(1)!!, spGroupBean)
                setGridItem(items!!.getOrNull(0), gridRow.getChildAt(0)!!, spGroupBean)

            }
            if (null != items && items.size > 3) {
                val gridRow = inflater.inflate(R.layout.cs_grid_row3, null, false) as LinearLayout
                groupView.addView(gridRow)

                setGridItem(items!!.getOrNull(5), gridRow.getChildAt(2)!!, spGroupBean)
                setGridItem(items!!.getOrNull(4), gridRow.getChildAt(1)!!, spGroupBean)
                setGridItem(items!!.getOrNull(3), gridRow.getChildAt(0)!!, spGroupBean)
            }
        }
    }

    fun setGridItem(itemBean: SpGroupItemBean?, itemView: View, spGroupBean: SpGroupBean) {
        if (itemBean == null) return
        itemBean.type = spGroupBean.type
        itemView?.visibility = View.VISIBLE
        val iv_icon = itemView.findViewById(R.id.iv_icon) as ImageView
        val tv_label = itemView.findViewById(R.id.tv_label) as TextView
        Glide.with(this)
                .load(itemBean?.icon)
                .into(iv_icon)
        tv_label.text = itemBean?.name
        itemView.tag = "${itemBean.id}"
        itemView.setOnClickListener {
            if (itemBean.id == null) return@setOnClickListener
            when (itemBean.type) {
                2 -> BuildSealActivity.start(this, itemBean!!)
                3 -> BuildSignActivity.start(this, itemBean!!)
                else -> {
                }
            }
        }

    }

    companion object {
        fun start(ctx: Activity?) {
            val intent = Intent(ctx, EntryApproveActivity::class.java)
            ctx?.startActivity(intent)
        }
    }
}
