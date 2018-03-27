package com.sogukj.pe.ui.approve

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.*
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import cn.finalteam.rxgalleryfinal.RxGalleryFinal
import cn.finalteam.rxgalleryfinal.imageloader.ImageLoaderType
import cn.finalteam.rxgalleryfinal.rxbus.RxBusResultDisposable
import cn.finalteam.rxgalleryfinal.rxbus.event.ImageRadioResultEvent
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.Theme
import com.bumptech.glide.Glide
import com.framework.base.ToolbarActivity
import com.google.gson.Gson
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.bean.*
import com.sogukj.pe.ui.fileSelector.FileMainActivity
import com.sogukj.pe.util.Trace
import com.sogukj.pe.util.Utils
import com.sogukj.pe.view.FlowLayout
import com.sogukj.service.SoguApi
import com.sogukj.util.XmlDb
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_build_seal.*
import kotlinx.android.synthetic.main.toolbar.*
import okhttp3.FormBody
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import org.jetbrains.anko.find
import java.io.File
import java.io.Serializable
import java.util.concurrent.ConcurrentHashMap

/**
 * Created by qinfei on 17/10/18.
 */
class BuildSealActivity : ToolbarActivity() {

    val gson = Gson()
    lateinit var inflater: LayoutInflater
    var paramTitle: String? = null
    var paramId: Int? = null
    var paramType: Int? = null
    var flagEdit = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        inflater = LayoutInflater.from(this)
        flagEdit = intent.getBooleanExtra(Extras.FLAG, false)
        val paramObj = intent.getSerializableExtra(Extras.DATA)
        if (paramObj is ApprovalBean) {
            paramTitle = paramObj.kind!!
            paramId = paramObj.approval_id!!
            paramType = paramObj.type
        } else if (paramObj is MessageBean) {
            paramTitle = paramObj.type_name!!
            paramId = paramObj.approval_id!!
            paramType = paramObj.type
        } else if (paramObj is SpGroupItemBean) {
            paramTitle = paramObj.name!!
            paramId = paramObj.id!!
            paramType = paramObj.type
        } else {
            paramId = intent.getIntExtra(Extras.ID, -1)
            paramType = intent.getIntExtra(Extras.TYPE, -1)
            paramTitle = intent.getStringExtra(Extras.TITLE)
        }
        if (paramId == -1 || paramType == -1) {
//            showToast("参数错误")
            showCustomToast(R.drawable.icon_toast_error,"参数错误")
            finish()
        }
        setContentView(R.layout.activity_build_seal)
        setBack(true)
        title = paramTitle
//        ll_seal.removeAllViews()
//        ll_approver.removeAllViews()
//        SoguApi.getService(application)
//                .approveInfo(template_id = if (flagEdit) null else paramId!!,
//                        sid = if (!flagEdit) null else paramId!!)
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribeOn(Schedulers.io())
//                .subscribe({ payload ->
//                    if (!payload.isOk) {
//                        showToast(payload.message)
//                        return@subscribe
//                    }
//                    payload.payload?.forEach { bean ->
//                        addRow(bean, inflater)
//                    }
//                    hideFields.forEach { field ->
//                        val view = fieldMap.get(field)
//                        view?.visibility = View.GONE
//                    }
//                    //律师意见默认为否，渲染的时候view未生成，所以生成不了。等生成的时候并没有隐藏
//                    payload.payload?.forEach { bean ->
//                        if (bean.control == 5) {
//                            if (bean.value_map?.is_select == 1) {
//                                bean.value_map?.hide?.split(",")?.forEach { field ->
//                                    if (!TextUtils.isEmpty(field)) {
//                                        val view = fieldMap[field]
//                                        view?.visibility = View.VISIBLE
//                                    }
//                                }
//                            } else {
//                                bean.value_map?.hide?.split(",")?.forEach { field ->
//                                    if (!TextUtils.isEmpty(field)) {
//                                        val view = fieldMap[field]
//                                        view?.visibility = View.GONE
//                                    }
//                                }
//                            }
//                        }
//                    }
//                }, { e ->
//                    Trace.e(e)
//                    showToast("暂无可用数据")
//                })
//
//        requestApprove()
        load()
        btn_confirm.setOnClickListener {
            var flag = true
            for (chk in checkList) {
                flag = flag.and(chk())
                if (!flag) break
            }
            if (flag) {
                doConfirm()
            } else {
//                showToast("请填写完整后再提交")
                showCustomToast(R.drawable.icon_toast_error,"请填写完整后再提交")
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

                            var mDialog = MaterialDialog.Builder(this@BuildSealActivity)
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
                    //showToast("暂无可用数据")
                })
    }

    private var isOneKey = false

    private fun judgeSealEmpty(list: ArrayList<CustomSealBean.ValueBean>): Boolean {
        if (list.size == 0) {
            return true
        } else {
            var flag = true
            for (item in list) {
                if (item.count > 0) {
                    flag = false
                    break
                }
            }
            return flag
        }
    }

    override fun onPause() {
        super.onPause()
        onBackPressed()
    }

    override fun onBackPressed() {
        var tmpMap = HashMap<String, Any?>()
        for ((k, v) in paramMap) {
            if (v == null) {

            } else {
                tmpMap.put(k, v)
            }
        }
        paramMap.clear()
        paramMap.putAll(tmpMap)
        //project_name          项目名称                2
        //seal                  用印选择                6
        //reasons               用印事由                4
        //sealFile              用印文件                9
        //info                  备注说明                4
        //sms              是否发短信提醒审批人      5
        //manager_opinion       投资经理意见              4
        //is_lawyer             是否需要律师意见        5
        //lawyer_opinion        律师意见                4
        //lawyerFile            律师意见文件          8

        //foreign_id            外资名称                2

        //fund_id               基金名称                2
        if ((paramMap.get("seal") == null || judgeSealEmpty(paramMap.get("seal") as ArrayList<CustomSealBean.ValueBean>)) &&
                (paramMap.get("fund_id") == null) &&
                (paramMap.get("reasons") == null || (paramMap.get("reasons") as String?).isNullOrEmpty()) &&
                (paramMap.get("info") == null || (paramMap.get("info") as String?).isNullOrEmpty()) &&
                (paramMap.get("manager_opinion") == null || (paramMap.get("manager_opinion") as String?).isNullOrEmpty()) &&
                (paramMap.get("lawyerFile") == null || (paramMap.get("lawyerFile") as ArrayList<CustomSealBean.ValueBean>).size == 0) &&
                (paramMap.get("is_lawyer") == null || (paramMap.get("is_lawyer") as Int) == 0) &&
                (paramMap.get("sms") == null || (paramMap.get("sms") as Int) == 0) &&
                (paramMap.get("sealFile") == null || (paramMap.get("sealFile") as ArrayList<CustomSealBean.ValueBean>).size == 0) &&
                (paramMap.get("project_name") == null) &&
                (paramMap.get("foreign_id") == null)) {
            super.onBackPressed()
            return
        }
        var mDialog = MaterialDialog.Builder(this@BuildSealActivity)
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
                            super.onBackPressed()
                        } else
                            showToast(payload.message)
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
//                            showToast("草稿提交成功")
                            showCustomToast(R.drawable.icon_toast_success,"草稿保存成功")
                            handler.postDelayed({
                                super.onBackPressed()
                            },2000)
                        } else
                            showToast(payload.message)
                    }, { e ->
                        Trace.e(e)
//                        showToast("草稿提交失败")
                        showCustomToast(R.drawable.icon_toast_error,"草稿保存失败")
                    })
        }
    }

    private fun load() {
        ll_seal.removeAllViews()
        ll_approver.removeAllViews()
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
                        addRow(bean, inflater)
                    }
                    hideFields.forEach { field ->
                        val view = fieldMap.get(field)
                        view?.visibility = View.GONE
                    }
                    //律师意见默认为否，渲染的时候view未生成，所以生成不了。等生成的时候并没有隐藏
                    payload.payload?.forEach { bean ->
                        if (bean.control == 5) {
                            if (bean.value_map?.is_select == 1) {
                                bean.value_map?.hide?.split(",")?.forEach { field ->
                                    if (!TextUtils.isEmpty(field)) {
                                        val view = fieldMap[field]
                                        view?.visibility = View.VISIBLE
                                    }
                                }
                            } else {
                                bean.value_map?.hide?.split(",")?.forEach { field ->
                                    if (!TextUtils.isEmpty(field)) {
                                        val view = fieldMap[field]
                                        view?.visibility = View.GONE
                                    }
                                }
                            }
                        }
                    }
                }, { e ->
                    Trace.e(e)
                    showToast("暂无可用数据")
                })

        requestApprove()
    }

    private fun requestApprove(fund_id: Int? = null) {
        SoguApi.getService(application)
                .approver(template_id = if (flagEdit) null else paramId!!,
                        sid = if (!flagEdit) null else paramId!!, fund_id = fund_id)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ payload ->
                    if (!payload.isOk) {
                        showToast(payload.message)
                        return@subscribe
                    }
                    payload.payload?.forEach { bean ->
                        addApprover(bean)
                    }
                }, { e ->
                    Trace.e(e)
                    showToast("暂无可用数据")
                })
    }

    fun doConfirm() {
        if(isOneKey){
            flagEdit = false
            paramId = XmlDb.open(context).get(Extras.ID, "").toInt()
        }
        val builder = FormBody.Builder()
        if (flagEdit) {

            builder.add("approval_id", "${paramId}")
            for ((k, v) in paramMap) {
                if (v == null) {

                } else if (v is String) {
                    builder.add(k, v)
                } else
                    builder.add(k, gson.toJson(v))
            }
            SoguApi.getService(application)
                    .updateApprove(builder.build())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeOn(Schedulers.io())
                    .subscribe({ payload ->
                        if (payload.isOk) {
//                            showToast("提交成功")
                            showCustomToast(R.drawable.icon_toast_success,"提交成功")
                            handler.postDelayed({
                                val intent = Intent()
                                intent.putExtra(Extras.ID, payload.payload!!)
                                setResult(Activity.RESULT_OK, intent)
                                finish()
                            },2000)
                        } else
                            showToast(payload.message)
                    }, { e ->
                        Trace.e(e)
//                        showToast("提交失败")
                        showCustomToast(R.drawable.icon_toast_error,"提交失败")
                    })
        } else {
            builder.add("template_id", "${paramId}")
            for ((k, v) in paramMap) {
                if (v == null) {

                } else if (v is String) {
                    builder.add(k, v)
                } else
                    builder.add(k, gson.toJson(v))
            }
            SoguApi.getService(application)
                    .submitApprove(builder.build())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeOn(Schedulers.io())
                    .subscribe({ payload ->
                        if (payload.isOk) {
//                            showToast("提交成功")
                            showCustomToast(R.drawable.icon_toast_success,"提交成功")
                            handler.postDelayed({
                                finish()
                            },2000)
                        } else
                            showToast(payload.message)
                    }, { e ->
                        Trace.e(e)
//                        showToast("提交失败")
                        showCustomToast(R.drawable.icon_toast_error,"提交失败")
                    })
        }
    }

    fun addApprover(bean: ApproverBean) {
        val convertView = inflater.inflate(R.layout.cs_row_approver, null) as LinearLayout
        ll_approver.addView(convertView)
        val tvLabel = convertView.findViewById(R.id.tv_label) as TextView
        val etValue = convertView.findViewById(R.id.et_value) as TextView
        tvLabel.text = bean.position
        etValue.text = bean.approver
    }

    fun addRow(bean: CustomSealBean, inflater: LayoutInflater) {
        when (bean.control) {
            1 -> add1(bean, inflater)
            2 -> add2(bean, inflater)
            3 -> add3(bean, inflater)
            4 -> add4(bean, inflater)
            5 -> add5(bean, inflater)
            6 -> add6(bean, inflater)
            8 -> add8(bean, inflater)
            9 -> add9(bean, inflater)
        }
    }

    val paramMap = HashMap<String, Any?>()
    val checkList = ArrayList<() -> Boolean>()

    private fun add1(bean: CustomSealBean, inflater: LayoutInflater) {
        val convertView = inflater.inflate(R.layout.cs_row_pop_list, null)
        ll_seal.addView(convertView)
        fieldMap.put(bean.fields, convertView)

        val tvLabel = convertView.findViewById(R.id.tv_label) as TextView
        val etValue = convertView.findViewById(R.id.et_value) as TextView
        tvLabel.text = if (bean.is_must == 1) bean.name + "(必填)" else bean.name

        val iv_alert = convertView.findViewById(R.id.iv_alert)
        iv_alert.visibility = View.GONE
        checkList.add {
            val str = etValue.text?.toString()
            if (bean.is_must == 1 && str.isNullOrEmpty()) {
                iv_alert.visibility = View.VISIBLE
                false
            } else {
                iv_alert.visibility = View.GONE
                true
            }
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
                MaterialDialog.Builder(this@BuildSealActivity)
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
                                paramMap.put(bean.fields, valBean?.id)
                                return true
                            }

                        })
                        .show()
            }

    }


    fun refreshListSelector(bean: CustomSealBean, data: CustomSealBean.ValueBean) {
        val view = viewMap[bean.fields]
        if (null != view) {
            view.text = data.name
        }
    }

    val viewMap = HashMap<String, TextView>()
    val fieldMap = HashMap<String, View?>()
    fun add2(bean: CustomSealBean, inflater: LayoutInflater) {
        val convertView = inflater.inflate(R.layout.cs_row_pop_list, null)
        ll_seal.addView(convertView)
        fieldMap.put(bean.fields, convertView)
        val tvLabel = convertView.findViewById(R.id.tv_label) as TextView
        val etValue = convertView.findViewById(R.id.et_value) as TextView
        tvLabel.text = if (bean.is_must == 1) bean.name + "(必填)" else bean.name
        etValue.text = bean.value_map?.name
        viewMap.put(bean.fields, etValue)
        val iv_alert = convertView.findViewById(R.id.iv_alert)
        iv_alert.visibility = View.GONE
        paramMap.put(bean.fields, bean.value_map?.id)// TODO
        checkList.add {
            val str = etValue.text?.toString()
            if (flagEdit)
                paramMap.put(bean.fields, bean.value_map?.id)
            if (bean.is_must == 1 && str.isNullOrEmpty()) {
                iv_alert.visibility = View.VISIBLE
                false
            } else {
                iv_alert.visibility = View.GONE
                true
            }
        }
        etValue.setOnClickListener {
            ListSelectorActivity.start(this, bean)
        }
    }

    private fun add3(bean: CustomSealBean, inflater: LayoutInflater) {
        val convertView = inflater.inflate(R.layout.cs_row_edit_text, null)
        ll_seal.addView(convertView)
        fieldMap.put(bean.fields, convertView)

        val tvLabel = convertView.findViewById(R.id.tv_label) as TextView
        val etValue = convertView.findViewById(R.id.et_value) as EditText
        tvLabel.text = if (bean.is_must == 1) bean.name + "(必填)" else bean.name
        etValue.setText(bean.value)
        etValue.filters = Utils.getFilter(this)

        val iv_alert = convertView.findViewById(R.id.iv_alert)
        iv_alert.visibility = View.GONE
        checkList.add {
            val str = etValue.text?.toString()
            paramMap.put(bean.fields, str)
            if (bean.is_must == 1 && str.isNullOrEmpty()) {
                iv_alert.visibility = View.VISIBLE
                false
            } else {
                iv_alert.visibility = View.GONE
                true
            }
        }

    }

    private fun add4(bean: CustomSealBean, inflater: LayoutInflater) {
        val convertView = inflater.inflate(R.layout.cs_row_edit_box, null)
        ll_seal.addView(convertView)
        fieldMap.put(bean.fields, convertView)

        val tvLabel = convertView.findViewById(R.id.tv_label) as TextView
        val etValue = convertView.findViewById(R.id.et_value) as EditText
        tvLabel.text = if (bean.is_must == 1) bean.name + "(必填)" else bean.name
        etValue.setText(bean.value)
        etValue.filters = Utils.getFilter(this)
        val iv_alert = convertView.findViewById(R.id.iv_alert)
        iv_alert.visibility = View.GONE
        paramMap.put(bean.fields, bean.value)// TODO
        checkList.add {
            val str = etValue.text?.toString()
            paramMap.put(bean.fields, str)
            if (bean.is_must == 1 && str.isNullOrEmpty()) {
                iv_alert.visibility = View.VISIBLE
                false
            } else {
                iv_alert.visibility = View.GONE
                true
            }
        }
        etValue.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                paramMap.put(bean.fields, s.toString())
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }
        })
    }

    val hideFields = ArrayList<String>()
    fun add5(bean: CustomSealBean, inflater: LayoutInflater) {
        val convertView = inflater.inflate(R.layout.cs_row_radio, null)
        ll_seal.addView(convertView)
        fieldMap.put(bean.fields, convertView)

        val tvLabel = convertView.findViewById(R.id.tv_label) as TextView
        val ivAlert = convertView.findViewById(R.id.iv_alert) as ImageView
        val rgCheck = convertView.findViewById(R.id.rg_check) as RadioGroup
        val rbYes = convertView.findViewById(R.id.rb_yes) as RadioButton
        val rbNo = convertView.findViewById(R.id.rb_no) as RadioButton

        tvLabel.text = if (bean.is_must == 1) bean.name + "(必填)" else bean.name
        paramMap.put(bean.fields, bean.value_map?.is_select)
        if (bean.value_map?.is_select == 1) {
            rbNo.isChecked = false
            rbYes.isChecked = true
        } else {
            rbNo.isChecked = true
            rbYes.isChecked = false
        }
        bean.value_map?.hide?.split(",")?.forEach { field ->
            if (bean.value_map?.is_select == 1)
                hideFields.add(field)
        }
        rbNo.setOnClickListener {
            paramMap.put(bean.fields, 0)
            if (bean.fields.equals("sms")) {
                return@setOnClickListener
            }
            bean.value_map?.hide?.split(",")?.forEach { field ->
                if (!TextUtils.isEmpty(field)) {
                    val view = fieldMap[field]
                    view?.visibility = View.GONE
                }
            }
        }

        rbYes.setOnClickListener {
            paramMap.put(bean.fields, 1)
            if (bean.fields.equals("sms")) {
                return@setOnClickListener
            }
            bean.value_map?.hide?.split(",")?.forEach { field ->
                if (!TextUtils.isEmpty(field)) {
                    val view = fieldMap[field]
                    view?.visibility = View.VISIBLE
                }
            }
        }
    }

    private fun add6(bean: CustomSealBean, inflater: LayoutInflater) {

        val convertView = inflater.inflate(R.layout.cs_row_check_box, null)
        ll_seal.addView(convertView)
        fieldMap.put(bean.fields, convertView)

        val tvLabel = convertView.findViewById(R.id.tv_label) as TextView
        tvLabel.text = if (bean.is_must == 1) bean.name + "(必填)" else bean.name

        val ll_check = convertView.findViewById(R.id.ll_check) as LinearLayout
        ll_check.removeAllViews()
        bean.value_list?.forEach { v ->
            if (v.name != null && v.name!!.isNotEmpty()) {
                val convertView = inflater.inflate(R.layout.cs_item_check, null)
                ll_check.addView(convertView)
                val cbCheck = convertView.findViewById(R.id.cb_check) as CheckBox
                val tvMinus = convertView.findViewById(R.id.tv_minus) as TextView
                val etNum = convertView.findViewById(R.id.et_num) as TextView
                val tvPlus = convertView.findViewById(R.id.tv_plus) as TextView
                cbCheck.text = v.name
                cbCheck.isChecked = v.is_select == 1 || v.count > 0
                etNum.text = "${v.count}"
                if (cbCheck.isChecked) {
                    v.is_select = 1
                    paramMap.put(bean.fields, bean.value_list)
                }
                cbCheck.setOnCheckedChangeListener { buttonView, isChecked ->
                    if (isChecked) {
                        v.is_select = 1
                        v.count = 1
                    } else {
                        v.is_select = 0
                        v.count = 0
                    }
                    etNum.text = v.count.toString()
                    paramMap.put(bean.fields, bean.value_list)
                }
                etNum.setOnFocusChangeListener { view, hasFocus ->
                    val editable = etNum.text.toString()
                    try {
                        var num = editable.toIntOrNull()
                        if (num == null) {
                            num = 0
                        }
                        etNum.text = "${num}"
                        v.count = num
                        paramMap.put(bean.fields, bean.value_list)
                    } catch (e: Exception) {

                    }
                }

                tvMinus.setOnClickListener {
                    var num = 0
                    etNum.text.toString().toIntOrNull()?.apply {
                        num = this
                    }
                    --num
                    if (num <= 0) {
                        num = 0
                        cbCheck.isChecked = false
                    }
                    etNum.text = "${num}"
                    v.count = num
                    paramMap.put(bean.fields, bean.value_list)
                }
                tvPlus.setOnClickListener {
                    var num = 0
                    etNum.text.toString().toIntOrNull()?.apply {
                        num = this
                    }
                    ++num
                    cbCheck.isChecked = true
                    etNum.text = "${num}"
                    v.count = num
                    paramMap.put(bean.fields, bean.value_list)
                }

            }
        }

        val iv_alert = convertView.findViewById(R.id.iv_alert)
        iv_alert.visibility = View.GONE
        checkList.add {
            paramMap.put(bean.fields, bean.value_list)
            var flag = false
            if (bean.is_must == 1) {
                bean.value_list?.forEach { v ->
                    flag = flag.or(v.is_select == 1 && v.count > 0)
                }
            } else {
                flag = true
            }
            if (!flag) {
                iv_alert.visibility = View.VISIBLE
                false
            } else {
                iv_alert.visibility = View.GONE
                true
            }
        }
    }

    fun add8(bean: CustomSealBean, inflater: LayoutInflater) {
        val convertView = inflater.inflate(R.layout.cs_row_images, null)
        ll_seal.addView(convertView)
        val tvLabel = convertView.findViewById(R.id.tv_label) as TextView
        tvLabel.text = if (bean.is_must == 1) bean.name + "(必填)" else bean.name
        fieldMap.put(bean.fields, convertView)

        val ll_images = convertView.findViewById(R.id.ll_images) as FlowLayout
        ll_images.removeAllViews()
        refreshImages(bean, ll_images)

    }

    fun uploadImage(filePath: String?, imagesBean: CustomSealBean, imagesView: FlowLayout) {
        if (null != imagesBean && null != imagesView && null != filePath) {
            val file = File(filePath)
            SoguApi.getService(application)
                    .uploadApprove(MultipartBody.Builder().setType(MultipartBody.FORM)
                            .addFormDataPart("file", file.name, RequestBody.create(MediaType.parse("*/*"), file))
                            .addFormDataPart("control", 8.toString())
                            .addFormDataPart("template_id", "${paramId}")
                            .build())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeOn(Schedulers.io())
                    .subscribe({ payload ->
                        if (payload.isOk && payload.payload != null) {
//                            showToast("上传成功")
                            showCustomToast(R.drawable.icon_toast_success,"上传成功")
                            imagesBean.value_list?.add(payload.payload!!)
                            refreshImages(imagesBean, imagesView)
                        } else
                            showToast(payload.message)
                    }, { e ->
                        Trace.e(e)
//                        showToast("上传失败")
                        showCustomToast(R.drawable.icon_toast_error,"上传失败")
                    })
        }
    }

    fun refreshImages(bean: CustomSealBean, imagesView: FlowLayout) {
        paramMap.put(bean.fields, bean.value_list)
        imagesView.removeAllViews()
        val inflater = LayoutInflater.from(this)

        bean.value_list?.forEach { v ->
            if (v.url != null && v.url!!.isNotEmpty()) {
                val convertView = inflater.inflate(R.layout.cs_item_img, null)
                imagesView.addView(convertView)

                val img = convertView.findViewById(R.id.img) as ImageView
                val ivDel = convertView.findViewById(R.id.iv_del) as ImageView
                val ivAdd = convertView.findViewById(R.id.iv_add) as ImageView
                ivAdd.visibility = View.GONE
                Glide.with(this)
                        .load(v.url)
                        .into(img)
                img.setOnClickListener {
                    bean.value_list?.remove(v)
                    refreshImages(bean, imagesView)
                }
            }
        }

        run {
            val convertView = inflater.inflate(R.layout.cs_item_img, null)
            imagesView.addView(convertView)
            val img = convertView.findViewById(R.id.img) as ImageView
            val ivDel = convertView.findViewById(R.id.iv_del) as ImageView
            val ivAdd = convertView.findViewById(R.id.iv_add) as ImageView
            ivAdd.setOnClickListener {
                RxGalleryFinal
                        .with(this@BuildSealActivity)
                        .image()
                        .radio()
                        .imageLoader(ImageLoaderType.GLIDE)
                        .subscribe(object : RxBusResultDisposable<ImageRadioResultEvent>() {
                            override fun onEvent(event: ImageRadioResultEvent?) {
                                val path = event?.result?.originalPath
                                if (!TextUtils.isEmpty(path))
                                    uploadImage(path!!, bean, imagesView)
                            }
                        })
                        .openGallery()
            }
        }
    }

    var filesBean: CustomSealBean? = null
    var filesView: LinearLayout? = null
    private fun add9(bean: CustomSealBean, inflater: LayoutInflater) {
        val convertView = inflater.inflate(R.layout.cs_row_files, null)
        ll_seal.addView(convertView)
        fieldMap.put(bean.fields, convertView)
        val tvLabel = convertView.findViewById(R.id.tv_label) as TextView
        val tvFile = convertView.findViewById(R.id.tv_file) as TextView
        tvLabel.text = if (bean.is_must == 1) bean.name + "(必填)" else bean.name

        val ll_files = convertView.findViewById(R.id.ll_files) as LinearLayout
        ll_files.removeAllViews()
        filesBean = bean
        filesView = ll_files
        refreshFiles(filesBean!!, filesView!!)

        tvFile.setOnClickListener {
            FileMainActivity.start(this, requestCode = REQ_SELECT_FILE)
//            MaterialFilePicker()
//                    .withActivity(this)
//                    .withRequestCode(REQ_SELECT_FILE)
//                    .withTitle("内部存储")
//                    .withFilterDirectories(true)
//                    .withHiddenFiles(true)
//                    .withCloseMenu(false)
////                    .withFilter(Pattern.compile(".*\\.txt$")) // Filtering files and directories by file name using regexp
//                    .start()
        }
    }

    fun uploadFile(filePath: String?) {
        if (null != filesBean && null != filesView && null != filePath) {
            val file = File(filePath)
            SoguApi.getService(application)
                    .uploadApprove(MultipartBody.Builder().setType(MultipartBody.FORM)
                            .addFormDataPart("file", file.name, RequestBody.create(MediaType.parse("*/*"), file))
                            .addFormDataPart("control", 9.toString())
                            .addFormDataPart("template_id", "${paramId}")
                            .build())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeOn(Schedulers.io())
                    .subscribe({ payload ->
                        if (payload.isOk && payload.payload != null) {
//                            showToast("上传成功")
                            showCustomToast(R.drawable.icon_toast_success,"上传成功")
                            filesBean?.value_list?.add(payload.payload!!)
                            refreshFiles(filesBean!!, filesView!!)
                        } else
                            showToast(payload.message)
                    }, { e ->
                        Trace.e(e)
//                        showToast("上传失败")
                        showCustomToast(R.drawable.icon_toast_error,"上传失败")
                        hideProgress()
                    }, {
                        hideProgress()
                    }, {
                        showProgress("正在上传")
                    })
        }
    }

    fun refreshFiles(bean: CustomSealBean, filesView: LinearLayout) {
        paramMap.put(bean.fields, bean.value_list)
        filesView.removeAllViews()
        val inflater = LayoutInflater.from(this)
        bean.value_list?.forEach { v ->
            if (v.url != null && v.url!!.isNotEmpty()) {
                val convertView = inflater.inflate(R.layout.cs_item_file, null)
                filesView.addView(convertView)

                val ivFile = convertView.findViewById(R.id.iv_file) as ImageView
                val tvName = convertView.findViewById(R.id.tv_name) as TextView
                val tvSize = convertView.findViewById(R.id.tv_size) as TextView
                val ivDel = convertView.findViewById(R.id.iv_del) as ImageView

                tvName.text = v.file_name
                tvSize.text = v.size
                ivDel.setOnClickListener {
                    bean.value_list?.remove(v)
                    refreshFiles(bean, filesView)
                }

            }
        }
    }

    val REQ_SELECT_FILE = 0xf0
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQ_SELECT_FILE && resultCode === Activity.RESULT_OK) {
//            val filePath = data?.getStringExtra(FilePickerActivity.RESULT_FILE_PATH)
//            uploadFile(filePath)
            val paths = data?.getStringArrayListExtra(Extras.LIST)
            paths?.forEach {
                uploadFile(it)
            }
        } else if (requestCode == ListSelectorActivity.REQ_LIST_SELECTOR && resultCode === Activity.RESULT_OK) {
            val bean = data?.getSerializableExtra(Extras.DATA) as CustomSealBean
            val valueBean = data.getSerializableExtra(Extras.DATA2) as CustomSealBean.ValueBean
            paramMap.put(bean.fields, valueBean.id)
            if (paramTitle == "基金用印") {
                ll_approver.removeAllViews()
                requestApprove(valueBean.id)
            }
            refreshListSelector(bean, valueBean)
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    companion object {
        fun start(ctx: Activity?, itemBean: SpGroupItemBean) {
            val intent = Intent(ctx, BuildSealActivity::class.java)
            intent.putExtra(Extras.DATA, itemBean)
            ctx?.startActivity(intent)
        }

        fun start(ctx: Activity?, bean: ApprovalBean) {
            val intent = Intent(ctx, BuildSealActivity::class.java)
            intent.putExtra(Extras.DATA, bean)
            ctx?.startActivity(intent)
        }

        fun start(ctx: Activity?, bean: MessageBean) {
            val intent = Intent(ctx, BuildSealActivity::class.java)
            intent.putExtra(Extras.DATA, bean)
            ctx?.startActivity(intent)
        }

        val REQ_EDIT = 0xe0

        fun start(ctx: Activity?, id: Int, paramType: Int?, paramTitle: String, edit: Boolean = false) {
            val intent = Intent(ctx, BuildSealActivity::class.java)
            intent.putExtra(Extras.ID, id)
            intent.putExtra(Extras.TYPE, paramType)
            intent.putExtra(Extras.TITLE, paramTitle)
            intent.putExtra(Extras.FLAG, edit)
            ctx?.startActivityForResult(intent, REQ_EDIT)
        }
    }
}
