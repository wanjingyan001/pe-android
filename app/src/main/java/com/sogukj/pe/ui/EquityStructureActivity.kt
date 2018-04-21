package com.sogukj.pe.ui

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.CheckBox
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import com.framework.base.ToolbarActivity
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.bean.EquityListBean
import com.sogukj.pe.bean.ProjectBean
import com.sogukj.pe.bean.StructureBean
import com.sogukj.pe.util.Trace
import com.sogukj.service.SoguApi
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_equity_structure.*
import kotlinx.android.synthetic.main.item_equity_structure.*
import kotlinx.android.synthetic.main.toolbar.*
import java.text.SimpleDateFormat

/**
 * Created by qinfei on 17/8/11.
 */
class EquityStructureActivity : ToolbarActivity() {
    lateinit var bean: EquityListBean
    val fmt = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bean = intent.getSerializableExtra(Extras.DATA) as EquityListBean
        setContentView(R.layout.activity_equity_structure)
        setBack(true)
        setTitle(bean.title)
        SoguApi.getService(application)
                .equityInfo(bean.hid!!)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ payload ->
                    if (payload.isOk) {
                        val data = payload.payload
                        data?.apply {
                            val llHeader = ll_node.findViewById(R.id.fl_header) as FrameLayout
                            val llChildren = ll_node.findViewById(R.id.ll_children) as LinearLayout
                            var tvName = ll_node.findViewById(R.id.tv_name) as TextView
                            var tvPercent = ll_node.findViewById(R.id.tv_percent) as TextView
                            var tvAmount = ll_node.findViewById(R.id.tv_amount) as TextView
                            tvName.text = data.name
                            tvPercent.text = data.percent
                            tvAmount.text = data.amount
//                            val cbxHeader = ll_node.findViewById(R.id.cbx_header) as CheckBox
//                            cbxHeader.setOnCheckedChangeListener { buttonView, isChecked ->
//                                if (isChecked) {
//                                    if (children == null || children?.size == 0) {
//                                        llChildren.visibility = View.GONE
//                                    } else {
//                                        llChildren.visibility = View.VISIBLE
//                                    }
//                                } else {
//                                    llChildren.visibility = View.GONE
//                                }
//                            }
                            if (children == null || children?.size == 0) {
                                llChildren.visibility = View.GONE
                            } else {
                                llChildren.visibility = View.VISIBLE
                            }
                            children?.apply {
                                setChildren(llChildren, this, 0)
                            }
                        }
                    } else
                        showCustomToast(R.drawable.icon_toast_fail, payload.message)
                }, { e ->
                    Trace.e(e)
                    showCustomToast(R.drawable.icon_toast_fail, "查询失败")
                })

        toolbar_menu.visibility = View.VISIBLE
    }

    fun setChildren(llChildren: LinearLayout, dataList: List<StructureBean>, index: Int) {
        val index = index + 1
        dataList.forEach {
            val node = View.inflate(this@EquityStructureActivity, R.layout.item_equity_structure, null)
            llChildren.addView(node)
            val llHeader = node.findViewById(R.id.fl_header) as FrameLayout
            val llChildren = node.findViewById(R.id.ll_children) as LinearLayout

            setHeader(llHeader, it, index)
            if (index <= 3)
                it.children?.apply {
                    //                    val cbxHeader = node.findViewById(R.id.cbx_header) as CheckBox
//                    cbxHeader.setOnCheckedChangeListener { buttonView, isChecked ->
//                        if (isChecked) {
//                            if (this == null || this?.size == 0) {
//                                llChildren.visibility = View.GONE
//                            } else {
//                                llChildren.visibility = View.VISIBLE
//                            }
//                        } else {
//                            llChildren.visibility = View.GONE
//                        }
//                    }
                    if (this == null || this?.size == 0) {
                        llChildren.visibility = View.GONE
                    } else {
                        llChildren.visibility = View.VISIBLE
                    }
                    setChildren(llChildren, this, index)
                }
        }
    }

    val headers = arrayOf(R.drawable.bg_header_0, R.drawable.bg_header_1, R.drawable.bg_header_2, R.drawable.bg_header_3)

    fun setHeader(llHeader: View, data: StructureBean, index: Int) {
        llHeader.setBackgroundResource(headers[index])
        var llContent = llHeader.findViewById(R.id.ll_content)
        var tvName = llHeader.findViewById(R.id.tv_name) as TextView
        var tvPercent = llHeader.findViewById(R.id.tv_percent) as TextView
        var tvAmount = llHeader.findViewById(R.id.tv_amount) as TextView

        llContent.visibility = View.VISIBLE
        tvName.text = data.name
        tvPercent.text = data.percent
        tvAmount.text = data.amount
    }

    companion object {
        fun start(ctx: Activity?, bean: EquityListBean) {
            val intent = Intent(ctx, EquityStructureActivity::class.java)
            intent.putExtra(Extras.DATA, bean)
            ctx?.startActivity(intent)
        }
    }
}
