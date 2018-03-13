package com.sogukj.pe.ui.approve

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.Theme
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.framework.base.ToolbarActivity
import com.netease.nim.uikit.common.ui.imageview.CircleImageView
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.bean.ApproverBean
import com.sogukj.pe.bean.CustomSealBean
import com.sogukj.pe.util.Trace
import com.sogukj.service.SoguApi
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_leave_business.*

class LeaveBusinessActivity : ToolbarActivity() {

    lateinit var inflater: LayoutInflater
    lateinit var ll_content: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_leave_business)
        setBack(true)

        inflater = LayoutInflater.from(context)
        ll_content = findViewById(R.id.ll_content) as LinearLayout

        flagEdit = intent.getBooleanExtra(Extras.FLAG, false)
        paramId = intent.getIntExtra(Extras.ID, 0)
        if (paramId == 10) {
            title = "出差"
        } else if (paramId == 11) {
            title = "请假"
            divider.visibility = View.GONE
        }

        load()
    }

    private var flagEdit = false
    private var paramId: Int? = null

    fun load() {
        ll_content.removeAllViews()
        SoguApi.getService(application)
                .approveInfo(template_id = if (flagEdit) null else paramId!!,
                        sid = if (!flagEdit) null else paramId!!)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ payload ->
                    if (!payload.isOk) {
                        showToast(payload.message)
                        return@subscribe
                    }
                    payload.payload?.forEach { bean ->
                        addRow(bean)
                    }

                    SoguApi.getService(application)
                            .leaveInfo(template_id = if (flagEdit) null else paramId!!,
                                    sid = if (!flagEdit) null else paramId!!)
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribeOn(Schedulers.io())
                            .subscribe({ payload ->
                                if (!payload.isOk) {
                                    showToast(payload.message)
                                    return@subscribe
                                }
                                payload.payload?.apply {
                                    addGrid(sp!!)
                                    cs!!.add(ApproverBean())
                                    addGrid(cs!!)
                                }
                            }, { e ->
                                Trace.e(e)
                                showToast("暂无可用数据")
                            })
                }, { e ->
                    Trace.e(e)
                    showToast("暂无可用数据")
                })
    }

    private fun addRow(bean: CustomSealBean) {
        when (bean.control) {
            1 -> add1(bean)
            4 -> add4(bean)//出差事由
            10 -> add10(bean)//出发城市，目的城市
            11 -> add11(bean)//时间
            12 -> add12(bean)//根据排版自动计算时长"
            13 -> add13(bean)//计算时长---自动
            14 -> add14(bean)
        //出差 4，10，11，12，13
        //请假 14,1
        }
    }

    private fun add1(bean: CustomSealBean) {
        val convertView = inflater.inflate(R.layout.cs_row_10, null)
        ll_content.addView(convertView)
        //fieldMap.put(bean.fields, convertView)

        val tvLabel = convertView.findViewById(R.id.tv_label) as TextView
        val etValue = convertView.findViewById(R.id.et_value) as TextView

        val iv_star = convertView.findViewById(R.id.star)
        if (bean.is_must == 1) {
            iv_star.visibility = View.VISIBLE
        } else {
            iv_star.visibility = View.GONE
        }

        tvLabel.text = bean.name
        etValue.hint = bean.value_map?.pla
        if (!bean.value_map?.name.isNullOrEmpty()) {
            etValue.text = bean.value_map?.name
        }

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

    fun add4(bean: CustomSealBean) {
        val convertView = inflater.inflate(R.layout.cs_row_reason, null) as LinearLayout
        ll_content.addView(convertView)
        val icon = convertView.findViewById(R.id.starIcon) as ImageView
        val tv_title = convertView.findViewById(R.id.tv_label) as TextView
        val et_reason = convertView.findViewById(R.id.reason) as EditText

        if (bean.is_must == 1) {
            icon.visibility = View.VISIBLE
        } else {
            icon.visibility = View.GONE
        }
        tv_title.text = bean.name
        et_reason.setText(bean.value)
        //reasons
    }

    private fun add10(bean: CustomSealBean) {
        val convertView = inflater.inflate(R.layout.cs_row_10, null)
        ll_content.addView(convertView)
        //fieldMap.put(bean.fields, convertView)

        val tvLabel = convertView.findViewById(R.id.tv_label) as TextView
        val etValue = convertView.findViewById(R.id.et_value) as TextView

        val iv_star = convertView.findViewById(R.id.star)
        if (bean.is_must == 1) {
            iv_star.visibility = View.VISIBLE
        } else {
            iv_star.visibility = View.GONE
        }

        tvLabel.text = bean.name
        etValue.hint = bean.value_map?.pla
        if (!bean.value_map?.name.isNullOrEmpty()) {
            etValue.text = bean.value_map?.name
        }
        //start_city    end_city
        etValue.setOnClickListener {

        }
    }

    private fun add11(bean: CustomSealBean) {
        //time_range
        var nameList = bean.name?.split("&") as ArrayList<String>
        for (name in nameList) {
            val convertView = inflater.inflate(R.layout.cs_row_10, null)
            ll_content.addView(convertView)
            //fieldMap.put(bean.fields, convertView)

            val tvLabel = convertView.findViewById(R.id.tv_label) as TextView
            val etValue = convertView.findViewById(R.id.et_value) as TextView

            val iv_star = convertView.findViewById(R.id.star)
            if (bean.is_must == 1) {
                iv_star.visibility = View.VISIBLE
            } else {
                iv_star.visibility = View.GONE
            }

            tvLabel.text = name
            etValue.hint = bean.value_map?.pla
            if (!bean.value_map?.name.isNullOrEmpty()) {
                etValue.text = bean.value_map?.name
            }
            etValue.setOnClickListener {

            }
        }
    }

    fun add12(bean: CustomSealBean) {
        //fields为空
        val convertView = inflater.inflate(R.layout.cs_row_13, null) as LinearLayout
        ll_content.addView(convertView)
        val tvLabel = convertView.findViewById(R.id.tv_label) as TextView
        val etValue = convertView.findViewById(R.id.et_value) as TextView
        tvLabel.text = bean.name
        etValue.text = bean.value_map?.value
    }

    fun add14(bean: CustomSealBean) {
        //fields为空
        add12(bean)
    }

    fun add13(bean: CustomSealBean) {
        //total_hours
        val convertView = inflater.inflate(R.layout.cs_row_approver, null) as LinearLayout
        ll_content.addView(convertView)
        val tvLabel = convertView.findViewById(R.id.tv_label) as TextView
        val etValue = convertView.findViewById(R.id.et_value) as TextView
        val icon = convertView.findViewById(R.id.starIcon) as ImageView
        tvLabel.text = bean.name
        etValue.hint = bean.value_map?.pla
        if (bean.is_must == 1) {
            icon.visibility = View.VISIBLE
        } else {
            icon.visibility = View.GONE
        }
    }

    // WeeklyThisFragment
    fun addGrid(list: ArrayList<ApproverBean>) {
        val convertView = inflater.inflate(R.layout.cs_row_sendto, null) as LinearLayout
        ll_content.addView(convertView)
        var grid_to = convertView.findViewById(R.id.grid_chaosong_to) as GridView
        var adapter = MyAdapter(context, list)
        grid_to.adapter = adapter

        val tvLabel = convertView.findViewById(R.id.tv_label) as TextView
        val icon = convertView.findViewById(R.id.starIcon) as ImageView
        if (list[list.size - 1].approver.isNullOrEmpty()) {
            icon.visibility = View.INVISIBLE
            tvLabel.text = "抄送人"
        } else {
            icon.visibility = View.VISIBLE
            tvLabel.text = "审批人"
        }
    }

    //请假-记录   qj_record_item
    //我的假期  MyHolidayActivity
    //出差-出差明细  BusinessTripDetailActivity
    //出差-出差明细--item

    companion object {
        fun start(ctx: Activity, edit: Boolean, id: Int) {
            var intent = Intent(ctx, LeaveBusinessActivity::class.java)
            intent.putExtra(Extras.FLAG, edit)
            intent.putExtra(Extras.ID, id)
            ctx.startActivity(intent)
        }
    }

    class MyAdapter(var context: Context, val list: ArrayList<ApproverBean>) : BaseAdapter() {

        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            var viewHolder: ViewHolder
            var conView = convertView
            if (conView == null) {
                viewHolder = ViewHolder()
                conView = LayoutInflater.from(context).inflate(R.layout.send_item, null) as LinearLayout
                viewHolder.icon = conView.findViewById(R.id.icon) as CircleImageView
                viewHolder.name = conView.findViewById(R.id.name) as TextView
                conView.setTag(viewHolder)
            } else {
                viewHolder = conView.tag as ViewHolder
            }
            if (list[position].approver.isNullOrEmpty()) {
                viewHolder.icon?.setImageResource(R.drawable.send_add)
                viewHolder.name?.text = "添加"
            } else {
                Glide.with(context)
                        .load(list[position].url)
                        .apply(RequestOptions().error(R.drawable.nim_avatar_default).fallback(R.drawable.nim_avatar_default))
                        .into(viewHolder.icon)
                viewHolder.name?.text = list[position].approver
            }
            return conView
        }

        override fun getItem(position: Int): Any {
            return list.get(position)
        }

        override fun getItemId(position: Int): Long {
            return position.toLong()
        }

        override fun getCount(): Int {
            return list.size
        }

        class ViewHolder {
            var icon: CircleImageView? = null
            var name: TextView? = null
        }
    }
}
