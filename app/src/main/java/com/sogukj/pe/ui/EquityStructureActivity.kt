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
import com.sogukj.pe.bean.ProjectBean
import com.sogukj.pe.bean.StructureBean
import com.sogukj.pe.util.Trace
import com.sogukj.service.SoguApi
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_equity_structure.*
import kotlinx.android.synthetic.main.item_equity_structure.*
import java.text.SimpleDateFormat

/**
 * Created by qinfei on 17/8/11.
 */
class EquityStructureActivity : ToolbarActivity() {
    lateinit var project: ProjectBean
    val fmt = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        project = intent.getSerializableExtra(Extras.DATA) as ProjectBean
        setContentView(R.layout.activity_equity_structure)
        setBack(true)
        setTitle("股权结构")
        SoguApi.getService(application)
                .equityStructure(project.company_id!!)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ payload ->
                    if (payload.isOk) {
                        val data = payload.payload
                        data?.apply {
                            tv_companyName.text = info?.companyName
                            tv_controllerName.text = info?.controllerName
                            tv_mainPercent.text = info?.percent

                            val llHeader = ll_node.findViewById(R.id.fl_header) as FrameLayout
                            val llChildren = ll_node.findViewById(R.id.ll_children) as LinearLayout
                            var tvName = ll_node.findViewById(R.id.tv_name) as TextView
                            tvName.text = info?.companyName
                            val cbxHeader = ll_node.findViewById(R.id.cbx_header) as CheckBox
                            cbxHeader.setOnCheckedChangeListener { buttonView, isChecked ->
                                llChildren.visibility = if (isChecked) View.VISIBLE else View.GONE
                            }
                            structure?.apply {
                                setChildren(llChildren, this, 0)
                            }
                        }
                    } else
                        showCustomToast(R.drawable.icon_toast_fail, payload.message)
                }, { e ->
                    Trace.e(e)
                })
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
                    val cbxHeader = node.findViewById(R.id.cbx_header) as CheckBox
                    cbxHeader.setOnCheckedChangeListener { buttonView, isChecked ->
                        llChildren.visibility = if (isChecked) View.VISIBLE else View.GONE
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
        fun start(ctx: Activity?, project: ProjectBean) {
            val intent = Intent(ctx, EquityStructureActivity::class.java)
            intent.putExtra(Extras.DATA, project)
            ctx?.startActivity(intent)
        }
    }
}
