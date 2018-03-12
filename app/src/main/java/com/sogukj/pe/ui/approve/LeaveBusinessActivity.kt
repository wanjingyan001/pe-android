package com.sogukj.pe.ui.approve

import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.Theme
import com.framework.base.ToolbarActivity
import com.sogukj.pe.R
import com.sogukj.pe.bean.ApproverBean
import com.sogukj.pe.bean.CustomSealBean

class LeaveBusinessActivity : ToolbarActivity() {

    lateinit var inflater: LayoutInflater

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_leave_business)
        setBack(true)

        inflater = LayoutInflater.from(context)
    }

    private fun add1(bean: CustomSealBean) {
        val convertView = inflater.inflate(R.layout.cs_row_pop_list, null)
        //ll_seal.addView(convertView)
        //fieldMap.put(bean.fields, convertView)

        val tvLabel = convertView.findViewById(R.id.tv_label) as TextView
        val etValue = convertView.findViewById(R.id.et_value) as TextView

        val iv_alert = convertView.findViewById(R.id.iv_alert)
        iv_alert.visibility = View.GONE

        val iv_star = convertView.findViewById(R.id.star)
        iv_star.visibility = View.VISIBLE

//        checkList.add {
//            val str = etValue.text?.toString()
//            if (bean.is_must == 1 && str.isNullOrEmpty()) {
//                iv_alert.visibility = View.VISIBLE
//                false
//            } else {
//                iv_alert.visibility = View.GONE
//                true
//            }
//        }

        val items = ArrayList<String?>()
        val map = HashMap<String, CustomSealBean.ValueBean>()
        bean.value_list?.forEach { v ->
            if (v.name != null && v.name!!.isNotEmpty()) {
                items.add(v.name)
                map.put(v.name!!, v)
            }
        }
        if (map.isNotEmpty())
            etValue.setOnClickListener {
                MaterialDialog.Builder(this@LeaveBusinessActivity)
                        .theme(Theme.LIGHT)
                        .items(items)
                        .canceledOnTouchOutside(true)
                        .itemsCallbackSingleChoice(-1, object : MaterialDialog.ListCallbackSingleChoice {
                            override fun onSelection(dialog: MaterialDialog?, v: View?, p: Int, s: CharSequence?): Boolean {
                                if (p == -1) return false
                                val name = items[p]
                                val valBean = map[name]
                                etValue.text = name
                                etValue.tag = "${valBean?.id}"
                                dialog?.dismiss()
                                //paramMap.put(bean.fields, valBean?.id)
                                return true
                            }

                        })
                        .show()
            }
    }

    fun addApprover(bean: ApproverBean) {
        val convertView = inflater.inflate(R.layout.cs_row_approver, null) as LinearLayout
        //ll_approver.addView(convertView)
        val tvLabel = convertView.findViewById(R.id.tv_label) as TextView
        val etValue = convertView.findViewById(R.id.et_value) as TextView
        tvLabel.text = bean.position
        etValue.text = bean.approver
    }

    fun addReason(bean: ApproverBean) {
        val convertView = inflater.inflate(R.layout.cs_row_reason, null) as LinearLayout
        //ll_approver.addView(convertView)
    }

    // WeeklyThisFragment
    fun addPerson() {
        val convertView = inflater.inflate(R.layout.cs_row_sendto, null) as LinearLayout
    }

    //请假-记录   qj_record_item
    //我的假期  MyHolidayActivity
    //出差-出差明细  BusinessTripDetailActivity
    //出差-出差明细--item

    companion object {
        fun start(ctx: Activity) {

        }
    }
}
