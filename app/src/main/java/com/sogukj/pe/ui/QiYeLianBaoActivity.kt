package com.sogukj.pe.ui

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import com.framework.adapter.ListAdapter
import com.framework.adapter.ListHolder
import com.framework.base.ToolbarActivity
import com.framework.util.Trace
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.bean.AnnualReportBean
import com.sogukj.pe.bean.ProjectBean
import com.sogukj.service.SoguApi
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_project_annual_report.*

open class QiYeLianBaoActivity : ToolbarActivity() {

    val adapterSelector = ListAdapter {
        object : ListHolder<AnnualReportBean> {
            lateinit var text: TextView
            override fun createView(inflater: LayoutInflater): View {
                text = inflater.inflate(R.layout.item_textview_selector, null) as TextView
                return text
            }

            override fun showData(convertView: View, position: Int, data: AnnualReportBean?) {
                text.text = data?.reportYear
                if (position != selectedIndex)
                    text.setTextColor(R.color.colorBlue)
                else
                    text.setTextColor(R.color.text_2)
            }

        }
    }

    lateinit var project: ProjectBean
    var selectedIndex = -1;
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_project_annual_report)

        project = intent.getSerializableExtra(Extras.DATA) as ProjectBean
        setBack(true)
        setTitle("企业年报")

        lv_dropdown.adapter = adapterSelector

        lv_dropdown.setOnItemClickListener { parent, view, position, id ->
            val group = adapterSelector.dataList[position]
            group?.apply { setGroup(position) }
            tv_select.isChecked = false
        }
        tv_select.setOnCheckedChangeListener { buttonView, isChecked ->
            lv_dropdown.visibility = when (isChecked) {
                true -> View.VISIBLE
                else -> View.GONE
            }
        }
        tv_previous.setOnClickListener {
            setGroup(selectedIndex - 1)
        }
        tv_next.setOnClickListener {
            setGroup(selectedIndex + 1)
        }

        handler.postDelayed({
            doRequest()
        }, 100)
    }

    fun setGroup(index: Int = 0) {
        tv_previous.isEnabled = false
        tv_next.isEnabled = false
        val size = adapterSelector.dataList.size
        if (size <= 0) return
        if (index < 0) return
        val group = adapterSelector.dataList[index]
        selectedIndex = index
        if (size > 0 && selectedIndex < size - 1)
            tv_next.isEnabled = true
        if (selectedIndex > 0)
            tv_previous.isEnabled = true
        adapterSelector.notifyDataSetChanged()
        group.apply {
            tv_select.text = reportYear
            tv_regNumber.text = regNumber
            tv_manageState.text = manageState
            tv_employeeNum.text=employeeNum
            tv_phoneNumber.text=phoneNumber
            tv_email.text=email
            tv_postcode.text=postcode
            tv_postalAddress.text=postalAddress
            tv_totalAssets.text=totalAssets
            tv_totalEquity.text=totalEquity
            tv_totalSales.text=totalSales
            tv_totalProfit.text=totalProfit
            tv_primeBusProfit.text=primeBusProfit
            tv_totalTax.text=totalTax
            tv_totalLiability.text=totalLiability
            tv_have_onlineStore.text=have_onlineStore
            tv_have_boundInvest.text=have_boundInvest

        }
    }

    fun doRequest() {
        SoguApi.getService(application)
                .listAnnualReport(project.company_id!!, page = 1)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ payload ->
                    adapterSelector.dataList.clear()
                    if (payload.isOk) {
                        payload.payload?.apply {
                            adapterSelector.dataList.clear()
                            adapterSelector.dataList.addAll(this)
                        }
                    } else
                        showToast(payload.message)
                    setGroup(0)
                    adapterSelector.notifyDataSetChanged()
                }, { e ->
                    Trace.e(e)
                })
    }

    companion object {
        fun start(ctx: Activity?, project: ProjectBean) {
            val intent = Intent(ctx, QiYeLianBaoActivity::class.java)
            intent.putExtra(Extras.DATA, project)
            ctx?.startActivity(intent)
        }
    }
}
