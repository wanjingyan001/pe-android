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
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.framework.base.ToolbarActivity
import com.netease.nim.uikit.common.ui.imageview.CircleImageView
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.bean.CustomSealBean
import com.sogukj.pe.util.Trace
import com.sogukj.service.SoguApi
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_leave_business.*
import com.bigkoo.pickerview.OptionsPickerView
import com.bigkoo.pickerview.TimePickerView
import com.sogukj.pe.bean.CityArea
import com.sogukj.pe.bean.UserBean
import com.sogukj.pe.ui.IM.TeamSelectActivity
import com.sogukj.pe.ui.user.CityAreaActivity
import com.sogukj.pe.util.Utils
import java.util.*
import kotlin.collections.ArrayList

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
        checkList.clear()
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
                                    addSP(sp!!)
                                    cs!!.add(UserBean())
                                    addCS(cs!!)
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
        paramMap.put(bean.fields, "")//TODO
        if (map.isNotEmpty())
            etValue.setOnClickListener {
                var pvOptions = OptionsPickerView.Builder(this, OptionsPickerView.OnOptionsSelectListener { options1, option2, options3, v ->
                    val tx = items.get(options1)
                    etValue.text = tx

                    val valBean = map[tx]
                    paramMap.put(bean.fields, valBean?.id)
                }).build()
                pvOptions.setPicker(items, null, null)
                pvOptions.show()
            }
        checkList.add {
            val str = etValue.text?.toString()?.trim()
            if (bean.is_must == 1 && str.isNullOrEmpty()) {
                false
            } else {
                true
            }
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
        convertView.tag = "time"

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
        var list = bean.value_map?.value
        if (list == null || list.size == 0) {

        } else {
            initDstCity(list)
        }

        //end_city
        etValue.setOnClickListener {
            DstCityActivity.start(context, paramId!!, dstCity)
        }
    }

    var startDate: Date? = null
    var endDate: Date? = null

    private fun add11(bean: CustomSealBean) {
        //time_range
        var nameList = bean.name?.split("&") as ArrayList<String>
        for (index in nameList.indices) {
            var name = nameList[index]
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
                if (index == 1) {
                    if (startDate == null) {
                        return@setOnClickListener
                    }
                }
                val builder = TimePickerView.Builder(this, { date, view ->
                    if (index == 0) {
                        startDate = date
                    } else if (index == 1) {
                        endDate = date
                        if (startDate!!.time > endDate!!.time) {
                            return@Builder
                        }
                        paramMap.put(bean.fields, "${startDate!!.time}&${endDate!!.time}")

                        var total = ll_content.findViewWithTag("total_hours") as TextView
                        if (paramId == 10) {
                            var day = (endDate!!.getTime() - startDate!!.getTime()) / (24 * 60 * 60 * 1000) + 1
                            total.text = day.toString() + "天"
                            paramMap.put("total_hours", day)
                        } else if (paramId == 11) {
//                            var hour = (endDate!!.getTime() - startDate!!.getTime()) / (60 * 60 * 1000)
//                            total.text = hour.toString()
//                            paramMap.put("total_hours", hour)
                        }
                    }
                    if (paramId == 10) {
                        etValue.text = Utils.getTime(date, "yyyy-MM-dd")
                    } else if (paramId == 11) {
                        etValue.text = Utils.getTime(date, "yyyy-MM-dd HH:mm")
                    }
                })
                        //年月日时分秒 的显示与否，不设置则默认全部显示
                        .setDividerColor(Color.DKGRAY)
                        .setContentSize(18)
                        .setDate(Calendar.getInstance())
                        .setCancelColor(resources.getColor(R.color.shareholder_text_gray))
                if (paramId == 10) {
                    builder.setType(booleanArrayOf(true, true, true, false, false, false)).build().show()
                } else if (paramId == 11) {
                    builder.setType(booleanArrayOf(true, true, true, true, true, false)).build().show()
                }
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
        //etValue.text = bean.value_map?.value
        etValue.setOnClickListener {
            if (paramId == 11) {
                MyHolidayActivity.start(context)
            }
        }
    }

    fun add14(bean: CustomSealBean) {
        //fields为空
        add12(bean)
    }

    val paramMap = HashMap<String, Any?>()
    val checkList = ArrayList<() -> Boolean>()

    fun add13(bean: CustomSealBean) {
        //total_hours
        val convertView = inflater.inflate(R.layout.cs_row_approver, null) as LinearLayout
        ll_content.addView(convertView)
        val tvLabel = convertView.findViewById(R.id.tv_label) as TextView
        val etValue = convertView.findViewById(R.id.et_value) as TextView
        val icon = convertView.findViewById(R.id.starIcon) as ImageView
        tvLabel.text = bean.name
        etValue.hint = bean.value_map?.pla
        etValue.tag = bean.fields
        if (bean.is_must == 1) {
            icon.visibility = View.VISIBLE
        } else {
            icon.visibility = View.GONE
        }
    }

    // WeeklyThisFragment
    fun addSP(list: ArrayList<ArrayList<UserBean>>) {
        var datalist = ArrayList<UserBean>()
        for (inner in list) {
            datalist.add(UserBean())
            datalist.addAll(inner)
        }
        datalist.removeAt(0)

        val convertView = inflater.inflate(R.layout.cs_row_sendto, null) as LinearLayout
        ll_content.addView(convertView)
        var grid_to = convertView.findViewById(R.id.grid_chaosong_to) as GridView
        var adapter = MySPAdapter(context, datalist)
        //grid_to.adapter = adapter

        val tvLabel = convertView.findViewById(R.id.tv_label) as TextView
        val icon = convertView.findViewById(R.id.starIcon) as ImageView

        grid_to.tag = "SP"
        icon.visibility = View.VISIBLE
        tvLabel.text = "审批人"

        grid_to.visibility = View.GONE
        var content = convertView.findViewById(R.id.llll) as LinearLayout
        content.visibility = View.VISIBLE
        for (index in 0 until datalist.size) {
            var item = datalist[index]
            if (item.name.equals("")) {
                var view = inflater.inflate(R.layout.send_item11, null) as LinearLayout

                var params = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
                params.leftMargin = Utils.dpToPx(context, 7)
                params.rightMargin = Utils.dpToPx(context, 7)
                view.layoutParams = params

                content.addView(view)
            } else {
                var view = inflater.inflate(R.layout.send_item, null) as LinearLayout
                var icon = view.findViewById(R.id.icon) as CircleImageView
                var name = view.findViewById(R.id.name) as TextView
                Glide.with(context)
                        .load(item.url)
                        .apply(RequestOptions().error(R.drawable.nim_avatar_default).fallback(R.drawable.nim_avatar_default))
                        .into(icon)
                name.text = item.name

                var params = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
                if (index == 0) {
                    params.leftMargin = Utils.dpToPx(context, 15)
                } else {
                    if (datalist[index - 1].name.equals("")) {
                        params.leftMargin = Utils.dpToPx(context, 0)
                    } else {
                        params.leftMargin = Utils.dpToPx(context, 15)
                    }
                }
                view.layoutParams = params

                content.addView(view)
            }
        }
    }

    fun addCS(list: ArrayList<UserBean>) {
        val convertView = inflater.inflate(R.layout.cs_row_sendto, null) as LinearLayout
        ll_content.addView(convertView)
        var grid_to = convertView.findViewById(R.id.grid_chaosong_to) as GridView
        var adapter = MyCSAdapter(context, list)
        grid_to.adapter = adapter

        val tvLabel = convertView.findViewById(R.id.tv_label) as TextView
        val icon = convertView.findViewById(R.id.starIcon) as ImageView

        icon.visibility = View.INVISIBLE
        tvLabel.text = "抄送人"
        grid_to.tag = "CS"
        grid_to.setOnItemClickListener { parent, view, position, id ->
            if (position == adapter.list.size - 1) {
                var list = ArrayList<UserBean>()
                for (index in 0 until adapter.list.size - 1) {//不包含
                    list.add(adapter.list[index])
                }
                TeamSelectActivity.startForResult(this, true, list, false, false, true, SEND)
            } else {
                adapter.list.removeAt(position)
                adapter.notifyDataSetChanged()
            }
        }
    }

    var SEND = 0x007

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == SEND && resultCode == Extras.RESULTCODE) {
            var grid_to = ll_content.findViewWithTag("CS") as GridView
            if (grid_to != null) {
                var adapter = grid_to.adapter as MyCSAdapter
                adapter.list.clear()
                adapter.list.addAll(data!!.getSerializableExtra(Extras.DATA) as ArrayList<UserBean>)
                adapter.list.add(UserBean())
                adapter.notifyDataSetChanged()
            }
        } else if (requestCode == Extras.REQUESTCODE && resultCode == Extras.RESULTCODE) {
            var list = data!!.getSerializableExtra(Extras.DATA) as ArrayList<CityArea.City>
            initDstCity(list)
        }
    }

    private var dstCity = ArrayList<CityArea.City>()

    fun initDstCity(list: ArrayList<CityArea.City>) {
        dstCity.clear()
        dstCity.addAll(list)

        var view = ll_content.findViewWithTag("time") as LinearLayout
        var citys = view.findViewById(R.id.cities) as LinearLayout
        citys.visibility = View.VISIBLE

        var etValue = view.findViewById(R.id.et_value) as TextView
        etValue.text = "${list.size}个"
        etValue.requestLayout()

        var cityValue1 = view.findViewById(R.id.city1) as TextView
        var cityValue2 = view.findViewById(R.id.city2) as TextView
        var cityValue3 = view.findViewById(R.id.city3) as TextView
        cityValue1.visibility = View.VISIBLE
        cityValue2.visibility = View.VISIBLE
        cityValue3.visibility = View.VISIBLE
        if (list.size == 1) {
            cityValue1.visibility = View.GONE
            cityValue2.visibility = View.GONE
            cityValue3.text = list[0].name
        } else if (list.size == 2) {
            cityValue1.visibility = View.GONE
            cityValue2.text = list[0].name
            cityValue3.text = list[1].name
        } else if (list.size >= 3) {
            cityValue1.text = list[0].name
            cityValue2.text = list[1].name
            cityValue3.text = list[2].name
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

    class MySPAdapter(var context: Context, val list: ArrayList<UserBean>) : BaseAdapter() {

        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            var conView = convertView

            if (getItemViewType(position) == 0) {
                conView = LayoutInflater.from(context).inflate(R.layout.send_item11, null) as LinearLayout
            } else {
                conView = LayoutInflater.from(context).inflate(R.layout.send_item, null) as LinearLayout
                var icon = conView.findViewById(R.id.icon) as CircleImageView
                var name = conView.findViewById(R.id.name) as TextView
                Glide.with(context)
                        .load(list[position].url)
                        .apply(RequestOptions().error(R.drawable.nim_avatar_default).fallback(R.drawable.nim_avatar_default))
                        .into(icon)
                name.text = list[position].name
            }
            return conView
        }

        override fun getItem(position: Int): Any {
            return list.get(position)
        }

        override fun getItemId(position: Int): Long {
            return position.toLong()
        }

        override fun getItemViewType(position: Int): Int {
            if (list[position].name.equals("")) {
                return 0
            } else {
                return 1
            }
        }

        override fun getViewTypeCount(): Int {
            return 2
        }

        override fun getCount(): Int {
            return list.size
        }
    }

    class MyCSAdapter(var context: Context, val list: ArrayList<UserBean>) : BaseAdapter() {

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
            if (position == list.size - 1) {
                //load("android.resource://包名/drawable/"+R.drawable.news)
                //viewHolder.icon?.setImageResource(R.drawable.send_add)
                Glide.with(context)
                        .load("android.resource://" + context.packageName + "/drawable/" + R.drawable.send_add)
                        .apply(RequestOptions().error(R.drawable.nim_avatar_default).fallback(R.drawable.nim_avatar_default))
                        .into(viewHolder.icon)
                viewHolder.name?.text = "添加"
            } else {
                Glide.with(context)
                        .load(list[position].url)
                        .apply(RequestOptions().error(R.drawable.nim_avatar_default).fallback(R.drawable.nim_avatar_default))
                        .into(viewHolder.icon)
                viewHolder.name?.text = list[position].name
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
