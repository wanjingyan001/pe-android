package com.sogukj.pe.ui.approve

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.Theme
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.framework.base.ToolbarActivity
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.bean.CustomSealBean
import com.sogukj.pe.util.Trace
import com.sogukj.service.SoguApi
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_leave_business.*
import com.bigkoo.pickerview.OptionsPickerView
import com.google.gson.Gson
import com.sogukj.pe.bean.CityArea
import com.sogukj.pe.bean.UserBean
import com.sogukj.pe.ui.IM.TeamSelectActivity
import com.sogukj.pe.util.Utils
import okhttp3.FormBody
import java.util.*
import kotlin.collections.ArrayList
import com.google.gson.internal.LinkedTreeMap
import com.sogukj.pe.view.CalendarDingDing
import com.sogukj.pe.view.CircleImageView
import com.sogukj.util.XmlDb
import org.jetbrains.anko.find
import java.text.SimpleDateFormat
import kotlinx.android.synthetic.main.toolbar.*

@Deprecated("被ApproveFillActivity取代")
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
        if (flagEdit == true) {
            var paramsTitle = intent.getStringExtra(Extras.TITLE)
            if (paramsTitle.equals("出差")) {
                title = "出差"
            } else if (paramsTitle.equals("请假")) {
                title = "请假"
                divider.visibility = View.GONE
            }
        }

        load()

        btn_commit.setOnClickListener {
            var flag = true
            for (chk in checkList) {
                flag = flag.and(chk())
                if (!flag) break
            }
            if (flag) {
                doConfirm()
            } else {
                showCustomToast(R.drawable.icon_toast_common, "请填写完整后再提交")
            }
        }
        toolbar_menu.setImageResource(R.drawable.copy)
        toolbar_menu.visibility = View.VISIBLE
        XmlDb.open(context).set(Extras.ID, "${paramId}")
        var tmpId = XmlDb.open(context).get(Extras.ID, "")
        SoguApi.getService(application)
                .getLastApprove(tmpId.toInt())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ payload ->
                    if (payload.isOk) {
                        if (payload.payload?.sid == null) {
                            toolbar_menu.visibility = View.INVISIBLE
                        }
                        //toolbar_menu 可能要隐藏，比如重新发起审批就不需要这个
                        toolbar_menu.setOnClickListener {

                            paramId = payload.payload?.sid
                            flagEdit = true
                            isOneKey = true
                            var name = payload.payload?.name

                            var mDialog = MaterialDialog.Builder(this@LeaveBusinessActivity)
                                    .theme(Theme.LIGHT)
                                    .canceledOnTouchOutside(true)
                                    .customView(R.layout.dialog_yongyin, false).build()
                            mDialog.show()
                            val content = mDialog.find<TextView>(R.id.content)
                            val cancel = mDialog.find<Button>(R.id.cancel)
                            val yes = mDialog.find<Button>(R.id.yes)

                            name = "“${name}”"
                            val spannable1 = SpannableString("是否将上次${name}填写内容一键复制填写")
                            spannable1.setSpan(ForegroundColorSpan(Color.parseColor("#808080")), 5, 5 + name.length, Spanned.SPAN_INCLUSIVE_EXCLUSIVE)
                            content.text = spannable1

                            cancel.setOnClickListener {
                                mDialog.dismiss()
                            }
                            yes.setOnClickListener {
                                load()
                                mDialog.dismiss()
                            }
                        }
                    }
                }, { e ->
                    Trace.e(e)
                    showCustomToast(R.drawable.icon_toast_common, "暂无可用数据")
                })
    }

    private var isOneKey = false

    override fun onBackPressed() {
        if (flagEdit && isOneKey == false) {
            super.onBackPressed()
            return
        }
        for (chk in checkList) {
            chk()
        }
        var tmpMap = HashMap<String, Any?>()
        for ((k, v) in paramMap) {
            if (v == null) {

            } else {
                tmpMap.put(k, v)
            }
        }
        paramMap.clear()
        paramMap.putAll(tmpMap)

        if ((paramMap.get("end_city") == null || (paramMap.get("end_city") as String?).isNullOrEmpty()) &&
                (paramMap.get("reasons") == null || (paramMap.get("reasons") as String?).isNullOrEmpty()) &&
                (paramMap.get("time_range") == null || (paramMap.get("time_range") as String?).isNullOrEmpty()) &&
                (paramMap.get("total_hours") == null || (paramMap.get("total_hours") as String?).isNullOrEmpty()) &&
                //(paramMap.get("copier") == null || (paramMap.get("copier") as String?).isNullOrEmpty()) &&
                (paramMap.get("leave_type") == null)) {
            super.onBackPressed()
            return
        }
        var mDialog = MaterialDialog.Builder(this@LeaveBusinessActivity)
                .theme(Theme.LIGHT)
                .canceledOnTouchOutside(true)
                .customView(R.layout.dialog_yongyin, false).build()
        mDialog.show()
        val content = mDialog.find<TextView>(R.id.content)
        val cancel = mDialog.find<Button>(R.id.cancel)
        val yes = mDialog.find<Button>(R.id.yes)
        content.text = "是否需要保存草稿"
        cancel.text = "否"
        yes.text = "是"
        cancel.setOnClickListener {
            val builder = HashMap<String, Any>()
            paramId = XmlDb.open(context).get(Extras.ID, "").toInt()
            builder.put("template_id", paramId!!)
            builder.put("data", HashMap<String, Any?>())
            SoguApi.getService(application)
                    .saveDraft(builder)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeOn(Schedulers.io())
                    .subscribe({ payload ->
                        if (payload.isOk) {
                            mDialog.dismiss()
                            super.onBackPressed()
                        } else {
                            showCustomToast(R.drawable.icon_toast_fail, payload.message)
                        }
                    }, { e ->
                        Trace.e(e)
                    })
        }
        yes.setOnClickListener {
            val builder = HashMap<String, Any>()
            paramId = XmlDb.open(context).get(Extras.ID, "").toInt()
            builder.put("template_id", paramId!!)
            builder.put("data", paramMap)
            SoguApi.getService(application)
                    .saveDraft(builder)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeOn(Schedulers.io())
                    .subscribe({ payload ->
                        if (payload.isOk) {
                            showCustomToast(R.drawable.icon_toast_success, "草稿保存成功")
                            mDialog.dismiss()
                            handler.postDelayed({
                                super.onBackPressed()
                            }, 2000)
                        } else {
                            showCustomToast(R.drawable.icon_toast_fail, payload.message)
                        }
                    }, { e ->
                        Trace.e(e)
                        showCustomToast(R.drawable.icon_toast_fail, "草稿保存失败")
                    })
        }
    }

    //出差  end_city  time_range（,）  total_hours  reasons   copy
    fun doConfirm() {
        if (isOneKey) {
            flagEdit = false
            paramId = XmlDb.open(context).get(Extras.ID, "").toInt()
        }
        if (flagEdit) {
            val builder = FormBody.Builder()
            builder.add("approval_id", "${paramId}")
            for ((k, v) in paramMap) {
                if (v == null) {

                } else if (v is String) {
                    builder.add(k, v)
                } else
                    builder.add(k, Gson().toJson(v))
            }
            SoguApi.getService(application)
                    .editLeave(builder.build())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeOn(Schedulers.io())
                    .subscribe({ payload ->
                        if (payload.isOk) {
                            showCustomToast(R.drawable.icon_toast_success, "提交成功")
                            finish()
                        } else {
                            showCustomToast(R.drawable.icon_toast_fail, payload.message)
                        }
                    }, { e ->
                        Trace.e(e)
                        showCustomToast(R.drawable.icon_toast_fail, "提交失败")
                    })
        } else {
            val builder = FormBody.Builder()
            builder.add("template_id", "${paramId}")
            for ((k, v) in paramMap) {
                if (v == null) {

                } else if (v is String) {
                    builder.add(k, v)
                } else
                    builder.add(k, Gson().toJson(v))
            }
            SoguApi.getService(application)
                    .submitApprove(builder.build())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeOn(Schedulers.io())
                    .subscribe({ payload ->
                        if (payload.isOk) {
                            showCustomToast(R.drawable.icon_toast_success, "提交成功")
                            finish()
                        } else {
                            showCustomToast(R.drawable.icon_toast_fail, payload.message)
                        }
                    }, { e ->
                        Trace.e(e)
                        showCustomToast(R.drawable.icon_toast_fail, "提交失败")
                    })
        }
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
                        showCustomToast(R.drawable.icon_toast_fail, payload.message)
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
                                    showCustomToast(R.drawable.icon_toast_fail, payload.message)
                                    return@subscribe
                                }
                                payload.payload?.apply {
                                    addSP(sp!!)

                                    default.clear()
                                    for (user in cs!!) {
                                        default.add(user.uid!!)
                                    }

                                    cs!!.add(UserBean())
                                    addCS(cs!!)
                                }
                            }, { e ->
                                Trace.e(e)
                                showCustomToast(R.drawable.icon_toast_common, "暂无可用数据")
                            })
                }, { e ->
                    Trace.e(e)
                    showCustomToast(R.drawable.icon_toast_common, "暂无可用数据")
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
        val convertView = inflater.inflate(R.layout.cs_row_10, null) as LinearLayout
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
        if (!bean.value_map?.pla.isNullOrEmpty()) {
            etValue.hint = bean.value_map?.pla
        }
        if (!bean.value_map?.name.isNullOrEmpty()) {
            etValue.text = bean.value_map?.name
        }

        val items = ArrayList<String?>()
        val map = HashMap<String, CustomSealBean.ValueBean>()
        bean.value_list?.forEach { v ->
            if (v.name != null && v.name!!.isNotEmpty()) {
                items.add(v.name)
                map.put(v.name!!, v)
                if (v.is_select == 1) {
                    etValue.text = v.name
                    paramMap.put(bean.fields, v.id)
                }
            }
        }
        //paramMap.put(bean.fields, "")//TODO
        if (map.isNotEmpty())
            convertView.setOnClickListener {
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
        val et_reason = convertView.findViewById(R.id.et_value) as EditText

        if (bean.is_must == 1) {
            icon.visibility = View.VISIBLE
        } else {
            icon.visibility = View.GONE
        }
        tv_title.text = bean.name
        et_reason.setText(bean.value)
        paramMap.put(bean.fields, bean.value)
        //reasons
        checkList.add {
            val str = et_reason.text?.toString()?.trim()
            paramMap.put(bean.fields, str)
            if (bean.is_must == 1 && str.isNullOrEmpty()) {
                false
            } else {
                true
            }
        }
    }

    private fun add10(bean: CustomSealBean) {
        val convertView = inflater.inflate(R.layout.cs_row_10, null) as LinearLayout
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
        //etValue.hint = bean.value_map?.pla
        if (!bean.value_map?.name.isNullOrEmpty()) {
            etValue.text = bean.value_map?.name
        }
        var list = bean.value_map?.value as ArrayList<LinkedTreeMap<String, Any>>
        if (list == null || list.size == 0) {

        } else {
            var changeList = ArrayList<CityArea.City>()
            for (city in list) {
                var item = CityArea.City()

                item.id = city.get("id").toString().toDouble().toInt()
                item.pid = city.get("pid").toString().toDouble().toInt()
                item.name = city.get("name").toString()

                changeList.add(item)
            }
            initDstCity(changeList)
        }

        //end_city
        convertView.setOnClickListener {
            var input_id = XmlDb.open(context).get(Extras.ID, "").toInt()
            if (flagEdit) {
                var paramsTitle = intent.getStringExtra(Extras.TITLE)
                if (paramsTitle.equals("出差")) {
                    input_id = 10
                } else if (paramsTitle.equals("请假")) {
                    input_id = 11
                }
                if (isOneKey) {
                    input_id = XmlDb.open(context).get(Extras.ID, "").toInt()
                }
            } else {
                input_id = paramId!!
            }
            DstCityActivity.start(context, input_id!!, dstCity)
        }

        checkList.add {
            //end_city
            var cities = ""
            for (city in dstCity) {
                cities = "${cities}${city.id},"
            }
            cities = cities.removeSuffix(",")
            paramMap.put(bean.fields, cities)
            if (bean.is_must == 1) {
                if (dstCity.size == 0) {
                    false
                } else {
                    true
                }
            } else {
                true
            }
        }
    }

    var startDate: Date? = null
    var endDate: Date? = null
    var date_type: Int? = null
    lateinit var ding_start: CalendarDingDing
    lateinit var ding_end: CalendarDingDing

    private fun add11(bean: CustomSealBean) {
        //time_range
        var nameList = bean.name?.split(",") as ArrayList<String>
        for (index in nameList.indices) {
            var name = nameList[index]
            val convertView = inflater.inflate(R.layout.cs_row_11, null) as LinearLayout
            ll_content.addView(convertView)

            val tvLabel = convertView.findViewById(R.id.tv_label) as TextView
            val etValue = convertView.findViewById(R.id.et_value) as TextView
            if (index == 0) {
                etValue.tag = "index0"
            } else {
                etValue.tag = "index1"
            }

            val iv_star = convertView.findViewById(R.id.star)
            if (bean.is_must == 1) {
                iv_star.visibility = View.VISIBLE
            } else {
                iv_star.visibility = View.GONE
            }

            tvLabel.text = name
            etValue.hint = bean.value_map?.pla
            var str = bean.value_map?.value as String?
            paramMap.put(bean.fields, str)

            date_type = bean.value_map?.type// 1天数   2时分

            if (!str.isNullOrEmpty()) {
                var dates = str!!.split(",")
                if (!dates[0].isNullOrEmpty()) {
                    if (date_type == 1) {
                        val format = SimpleDateFormat("yyyy-MM-dd")
                        etValue.text = format.format(dates[index].toLong() * 1000)
                        startDate = format.parse(format.format(dates[0].toLong() * 1000))
                        endDate = format.parse(format.format(dates[1].toLong() * 1000))
                    } else if (date_type == 2) {
                        val format = SimpleDateFormat("yyyy-MM-dd HH:mm")
                        etValue.text = format.format(dates[index].toLong() * 1000)
                        startDate = format.parse(format.format(dates[0].toLong() * 1000))
                        endDate = format.parse(format.format(dates[1].toLong() * 1000))
                    }
//                    if (flagEdit == true) {
//                        var paramsTitle = intent.getStringExtra(Extras.TITLE)
//                        if (paramsTitle.equals("出差")) {
//                            val format = SimpleDateFormat("yyyy-MM-dd")
//                            etValue.text = format.format(dates[index].toLong() * 1000)
//                            startDate = format.parse(format.format(dates[0].toLong() * 1000))
//                            endDate = format.parse(format.format(dates[1].toLong() * 1000))
//                        } else if (paramsTitle.equals("请假")) {
//                            val format = SimpleDateFormat("yyyy-MM-dd HH:mm")
//                            etValue.text = format.format(dates[index].toLong() * 1000)
//                            startDate = format.parse(format.format(dates[0].toLong() * 1000))
//                            endDate = format.parse(format.format(dates[1].toLong() * 1000))
//                        }
//                        if (isOneKey) {
//                            var tmpId = XmlDb.open(context).get(Extras.ID, "")
//                            if (tmpId.equals("10")) {
//                                val format = SimpleDateFormat("yyyy-MM-dd")
//                                etValue.text = format.format(dates[index].toLong() * 1000)
//                                startDate = format.parse(format.format(dates[0].toLong() * 1000))
//                                endDate = format.parse(format.format(dates[1].toLong() * 1000))
//                            } else if (tmpId.equals("11")) {
//                                val format = SimpleDateFormat("yyyy-MM-dd HH:mm")
//                                etValue.text = format.format(dates[index].toLong() * 1000)
//                                startDate = format.parse(format.format(dates[0].toLong() * 1000))
//                                endDate = format.parse(format.format(dates[1].toLong() * 1000))
//                            }
//                        }
//                    } else {
//                        if (paramId == 10) {
//                            val format = SimpleDateFormat("yyyy-MM-dd")
//                            etValue.text = format.format(dates[index].toLong() * 1000)
//                            startDate = format.parse(format.format(dates[0].toLong() * 1000))
//                            endDate = format.parse(format.format(dates[1].toLong() * 1000))
//                        } else if (paramId == 11) {
//                            val format = SimpleDateFormat("yyyy-MM-dd HH:mm")
//                            etValue.text = format.format(dates[index].toLong() * 1000)
//                            startDate = format.parse(format.format(dates[0].toLong() * 1000))
//                            endDate = format.parse(format.format(dates[1].toLong() * 1000))
//                        }
//                    }
                }
            }

            if (index == 0) {
                ding_start = CalendarDingDing(context)
            } else if (index == 1) {
                ding_end = CalendarDingDing(context)
            }
            convertView.setOnClickListener {
                if (index == 0) {
                    if (startDate == null) {
                        startDate = Date()
                    }
                    ding_start.show(date_type!!, startDate, object : CalendarDingDing.onTimeClick {
                        override fun onClick(date: Date?) {
                            if (date == null) {
                                var etValue = ll_content.findViewWithTag("index0") as TextView
                                if (etValue.text.trim().equals("")) {
                                    startDate = null
                                }
                                return
                            } else {
                                startDate = date!!
                                setTime(etValue, date!!, date_type!!)
                                if (endDate == null) {
                                    return
                                }
                            }

                            if (date_type == 1) {
                                if (startDate!!.time / 1000 > endDate!!.time / 1000) {
                                    showCustomToast(R.drawable.icon_toast_common, "开始时间不能大于结束时间")
                                    return
                                }
                            } else if (date_type == 2) {
                                if (startDate!!.time / 1000 >= endDate!!.time / 1000) {
                                    showCustomToast(R.drawable.icon_toast_common, "开始时间不能大于等于结束时间")
                                    return
                                }
                            }
                            setTime(etValue, date!!, date_type!!)
                            calculateTime(date_type!!)
                        }
                    })
                } else if (index == 1) {
                    if (startDate == null) {
                        showCustomToast(R.drawable.icon_toast_common, "请先选择开始时间")
                        return@setOnClickListener
                    }
                    if (endDate == null) {
                        endDate = Date()
                    }
                    ding_end.show(date_type!!, endDate, object : CalendarDingDing.onTimeClick {
                        override fun onClick(date: Date?) {
                            if (date == null) {
                                var etValue = ll_content.findViewWithTag("index1") as TextView
                                if (etValue.text.trim().equals("")) {
                                    endDate = null
                                }
                                return
                            } else {
                                endDate = date!!
                            }

                            if (date_type == 1) {
                                if (startDate!!.time / 1000 > endDate!!.time / 1000) {
                                    showCustomToast(R.drawable.icon_toast_common, "开始时间不能大于结束时间")
                                    return
                                }
                            } else if (date_type == 2) {
                                if (startDate!!.time / 1000 >= endDate!!.time / 1000) {
                                    showCustomToast(R.drawable.icon_toast_common, "开始时间不能大于等于结束时间")
                                    return
                                }
                            }
                            setTime(etValue, date!!, date_type!!)
                            calculateTime(date_type!!)
                            if (!isTimeAdd) {
                                checkList.add {
                                    if (bean.is_must == 1) {
                                        if (startDate != null && endDate != null) {
                                            paramMap.put(bean.fields, "${(startDate!!.time) / 1000},${(endDate!!.time) / 1000}")
                                            true
                                        } else {
                                            false
                                        }
                                    } else {
                                        true
                                    }
                                }
                                isTimeAdd = true
                            }
                        }
                    })
                }
//                if (index == 1) {
//                    if (startDate == null) {
//                        return@setOnClickListener
//                    }
//                }
//                val builder = TimePickerView.Builder(this, { date, view ->
//                    if (index == 0) {
//                        startDate = date
//                        if (endDate == null) {
//                            setTime(etValue, date)
//                            return@Builder
//                        }
//                        if (startDate!!.time > endDate!!.time) {
//                            return@Builder
//                        }
//                        setTime(etValue, date)
//                        calculateTime()
//                    } else if (index == 1) {
//                        endDate = date
//                        if (startDate!!.time > endDate!!.time) {
//                            return@Builder
//                        }
//                        setTime(etValue, date)
//                        calculateTime()
//                    }
//                    checkList.add {
//                        if (bean.is_must == 1) {
//                            if (startDate != null && endDate != null) {
//                                paramMap.put(bean.fields, "${(startDate!!.time) / 1000},${(endDate!!.time) / 1000}")
//                                true
//                            } else {
//                                false
//                            }
//                        } else {
//                            true
//                        }
//                    }
//                })
//                        //年月日时分秒 的显示与否，不设置则默认全部显示
//                        .setDividerColor(Color.DKGRAY)
//                        .setContentSize(18)
//                        .isCyclic(true)
//                        .setDate(Calendar.getInstance())
//                        .setCancelColor(resources.getColor(R.color.shareholder_text_gray))
//                if (flagEdit) {
//                    var paramsTitle = intent.getStringExtra(Extras.TITLE)
//                    if (paramsTitle.equals("出差")) {
//                        builder.setType(booleanArrayOf(true, true, true, false, false, false)).build().show()
//                    } else if (paramsTitle.equals("请假")) {
//                        builder.setType(booleanArrayOf(true, true, true, true, true, false)).build().show()
//                    }
//                    if (isOneKey) {
//                        var tmpId = XmlDb.open(context).get(Extras.ID, "").toInt()
//                        if (tmpId == 10) {
//                            builder.setType(booleanArrayOf(true, true, true, false, false, false)).build().show()
//                        } else if (tmpId == 11) {
//                            builder.setType(booleanArrayOf(true, true, true, true, true, false)).build().show()
//                        }
//                    }
//                } else {
//                    if (paramId == 10) {
//                        builder.setType(booleanArrayOf(true, true, true, false, false, false)).build().show()
//                    } else if (paramId == 11) {
//                        builder.setType(booleanArrayOf(true, true, true, true, true, false)).build().show()
//                    }
//                }
            }
        }
    }

    private var isTimeAdd = false

    fun setTime(etValue: TextView, date: Date, type: Int) {
        if (type == 1) {
            etValue.text = Utils.getTime(date, "yyyy-MM-dd")
        } else if (type == 2) {
            etValue.text = Utils.getTime(date, "yyyy-MM-dd HH:mm")
        }
//        if (flagEdit) {
//            var paramsTitle = intent.getStringExtra(Extras.TITLE)
//            if (paramsTitle.equals("出差")) {
//                etValue.text = Utils.getTime(date, "yyyy-MM-dd")
//            } else if (paramsTitle.equals("请假")) {
//                etValue.text = Utils.getTime(date, "yyyy-MM-dd HH:mm")
//            }
//            if (isOneKey) {
//                var tmpId = XmlDb.open(context).get(Extras.ID, "").toInt()
//                if (tmpId == 10) {
//                    etValue.text = Utils.getTime(date, "yyyy-MM-dd")
//                } else if (tmpId == 11) {
//                    etValue.text = Utils.getTime(date, "yyyy-MM-dd HH:mm")
//                }
//            }
//        } else {
//            if (paramId == 10) {
//                etValue.text = Utils.getTime(date, "yyyy-MM-dd")
//            } else if (paramId == 11) {
//                etValue.text = Utils.getTime(date, "yyyy-MM-dd HH:mm")
//            }
//        }
    }

    fun calculateTime(type: Int) {
        var pattern = ""
//        if (flagEdit) {
//            var paramsTitle = intent.getStringExtra(Extras.TITLE)
//            if (paramsTitle.equals("出差")) {
//                pattern = "yyyy-MM-dd"
//            } else if (paramsTitle.equals("请假")) {
//                pattern = "yyyy-MM-dd HH:mm"
//            }
//            if (isOneKey) {
//                var tmpId = XmlDb.open(context).get(Extras.ID, "").toInt()
//                if (tmpId == 10) {
//                    pattern = "yyyy-MM-dd"
//                } else if (tmpId == 11) {
//                    pattern = "yyyy-MM-dd HH:mm"
//                }
//            }
//        } else {
//            if (paramId == 10) {
//                pattern = "yyyy-MM-dd"
//            } else if (paramId == 11) {
//                pattern = "yyyy-MM-dd HH:mm"
//            }
//        }
//        if (type == 1) {
//            pattern = "yyyy-MM-dd"
//        } else if (type == 2) {
//            pattern = "yyyy-MM-dd HH:mm"
//        }
//        if (Utils.getTime(startDate, pattern).equals(Utils.getTime(endDate, pattern))) {
//            showToast("选择的时长不能为0")
//            return
//        }

        var total = ll_content.findViewWithTag("total_hours") as TextView
        SoguApi.getService(application)
                .calcTotalTime(start_time = (startDate!!.time / 1000).toString(), end_time = (endDate!!.time / 1000).toString(), type = type)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ payload ->
                    if (payload.isOk) {
                        if (type == 1) {
                            total.text = payload.payload + "天"
                        } else if (type == 2) {
                            total.text = payload.payload + "小时"
                        }
                        paramMap.put("total_hours", payload.payload)//total_hours
                    } else {
                        showCustomToast(R.drawable.icon_toast_fail, payload.message)
                    }
                }, { e ->
                    Trace.e(e)
                })

//        if (flagEdit) {
//            var paramsTitle = intent.getStringExtra(Extras.TITLE)
//            if (paramsTitle.equals("出差")) {
//                load10()
//            } else if (paramsTitle.equals("请假")) {
//                load11()
//            }
//            if (isOneKey) {
//                var tmpId = XmlDb.open(context).get(Extras.ID, "").toInt()
//                if (tmpId == 10) {
//                    load10()
//                } else if (tmpId == 11) {
//                    load11()
//                }
//            }
//        } else {
//            if (paramId == 10) {
//                load10()
//            } else if (paramId == 11) {
//                load11()
//            }
//        }
    }

    fun load10() {
        var total = ll_content.findViewWithTag("total_hours") as TextView
        SoguApi.getService(application)
                .calcTotalTime(start_time = (startDate!!.time / 1000).toString(), end_time = (endDate!!.time / 1000).toString(), type = 1)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ payload ->
                    if (payload.isOk) {
                        total.text = payload.payload + "天"
                        paramMap.put("total_hours", payload.payload)//total_hours
                    } else {
                        showCustomToast(R.drawable.icon_toast_fail, payload.message)
                    }
                }, { e ->
                    Trace.e(e)
                    showCustomToast(R.drawable.icon_toast_fail, "时间计算出错")
                })
    }

    fun load11() {
        var total = ll_content.findViewWithTag("total_hours") as TextView
        SoguApi.getService(application)
                .calcTotalTime(start_time = (startDate!!.time / 1000).toString(), end_time = (endDate!!.time / 1000).toString(), type = 2)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ payload ->
                    if (payload.isOk) {
                        total.text = payload.payload + "小时"
                        paramMap.put("total_hours", payload.payload)//total_hours
                    } else {
                        showCustomToast(R.drawable.icon_toast_fail, payload.message)
                    }
                }, { e ->
                    Trace.e(e)
                    showCustomToast(R.drawable.icon_toast_fail, "时间计算出错")
                })
    }

    fun add12(bean: CustomSealBean) {
        //fields为空
        val convertView = inflater.inflate(R.layout.cs_row_13, null) as LinearLayout
        ll_content.addView(convertView)
        val tvLabel = convertView.findViewById(R.id.tv_label) as TextView
        val etValue = convertView.findViewById(R.id.et_value) as TextView
        tvLabel.text = bean.name
        etValue.text = bean.value_map?.value as String?
        convertView.setOnClickListener {
            if (paramId == 11 || (flagEdit && intent.getStringExtra(Extras.TITLE).equals("请假")) || isOneKey) {
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
        //etValue.text = bean.value_map?.value as String?
        if (startDate != null && endDate != null) {
            calculateTime(date_type!!)
        }
        if (flagEdit == true) {
            var paramsTitle = intent.getStringExtra(Extras.TITLE)
            var str = bean.value_map?.value as String?
            if (paramsTitle.equals("出差")) {
                etValue.text = "${str}天"
            } else if (paramsTitle.equals("请假")) {
                etValue.text = "${str}小时"
            }
            if (isOneKey) {
                var tmpId = XmlDb.open(context).get(Extras.ID, "").toInt()
                if (tmpId == 10) {
                    etValue.text = "${str}天"
                } else if (tmpId == 11) {
                    etValue.text = "${str}小时"
                }
            }
        }
        if (bean.is_must == 1) {
            icon.visibility = View.VISIBLE
        } else {
            icon.visibility = View.GONE
        }
        checkList.add {
            var txt = etValue.text.toString()
            if (bean.is_must == 1) {
                if (txt.isNullOrEmpty()) {
                    false
                } else {
                    true
                }
            } else {
                true
            }
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
//                if (item.url.isNullOrEmpty()) {
//                    icon.setChar(item.name.first())
//                } else {
//                    Glide.with(context)
//                            .load(item.url)
//                            .apply(RequestOptions().error(R.drawable.nim_avatar_default).fallback(R.drawable.nim_avatar_default))
//                            .into(icon)
//                }
                if (item.url.isNullOrEmpty()) {
                    val ch = item.name?.first()
                    icon.setChar(ch)
                } else {
                    Glide.with(context)
                            .load(item.url)
                            .apply(RequestOptions().error(R.drawable.nim_avatar_default).fallback(R.drawable.nim_avatar_default))
                            .into(icon)
                }
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

    var default = ArrayList<Int>()

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
                TeamSelectActivity.startForResult(this, true, list, false, false, true, SEND, default)
            } else {
                adapter.list.removeAt(position)
                adapter.notifyDataSetChanged()
            }
        }
        checkList.add {
            var copyid = ""
            var list = adapter.list
            for (index in 0 until (list.size - 1)) {
                copyid = "${copyid}${list[index].uid},"
            }
            copyid = copyid.removeSuffix(",")
            paramMap.put("copier", copyid)
            true
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

        var cities = ""
        for (city in dstCity) {
            cities = "${cities}${city.id},"
        }
        cities = cities.removeSuffix(",")
        paramMap.put("end_city", cities)

        var view = ll_content.findViewWithTag("time") as LinearLayout
        var citys = view.findViewById(R.id.cities) as LinearLayout
        citys.visibility = View.VISIBLE

        var etValue = view.findViewById(R.id.et_value) as TextView
        etValue.text = "${list.size}个"

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
        fun start(ctx: Activity, edit: Boolean, id: Int, title: String) {
            var intent = Intent(ctx, LeaveBusinessActivity::class.java)
            intent.putExtra(Extras.FLAG, edit)
            intent.putExtra(Extras.ID, id)
            intent.putExtra(Extras.TITLE, title)
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
//                Glide.with(context)
//                        .load(list[position].url)
//                        .apply(RequestOptions().error(R.drawable.nim_avatar_default).fallback(R.drawable.nim_avatar_default))
//                        .into(icon)
                if (list[position].url.isNullOrEmpty()) {
                    val ch = list[position].name?.first()
                    icon.setChar(ch)
                } else {
                    Glide.with(context)
                            .load(list[position].url)
                            .apply(RequestOptions().error(R.drawable.nim_avatar_default).fallback(R.drawable.nim_avatar_default))
                            .into(icon)
                }
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
                if (list[position].url.isNullOrEmpty()) {
                    viewHolder.icon?.setChar(list[position].name.first())
                } else {
                    Glide.with(context)
                            .load(list[position].url)
                            .apply(RequestOptions().error(R.drawable.nim_avatar_default).fallback(R.drawable.nim_avatar_default))
                            .into(viewHolder.icon)
                }
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
