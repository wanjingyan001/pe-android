package com.sogukj.pe.ui.approve

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.provider.MediaStore
import android.text.*
import android.text.style.ForegroundColorSpan
import android.util.Log
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
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_build_sign.*
import kotlinx.android.synthetic.main.toolbar.*
import okhttp3.FormBody
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import org.jetbrains.anko.find
import java.io.File
import java.util.HashMap
import kotlin.collections.ArrayList
import kotlin.collections.component1
import kotlin.collections.component2

@Deprecated("被ApproveFillActivity取代")
class BuildSignActivity : ToolbarActivity() {

    val gson = Gson()
    lateinit var inflater: LayoutInflater
    lateinit var paramTitle: String
    var paramId: Int? = null
    var paramType: Int? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        inflater = LayoutInflater.from(this)
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
            showCustomToast(R.drawable.icon_toast_fail,"参数错误")
            finish()
        }
        setContentView(R.layout.activity_build_sign)
        setBack(true)
        title = paramTitle
//        ll_seal.removeAllViews()
//        ll_approver.removeAllViews()
//        SoguApi.getService(application)
//                .approveInfo(template_id = paramId!!)
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
//                }, { e ->
//                    Trace.e(e)
//                    showToast("暂无可用数据")
//                })
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
                showCustomToast(R.drawable.icon_toast_common,"请填写完整后再提交")
                //showToast("请填写完整后再提交")
            }
        }
        toolbar_menu.setImageResource(R.drawable.copy)
        toolbar_menu.visibility = View.VISIBLE
        SoguApi.getService(application)
                .getLastApprove(paramId!!)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ payload ->
                    if (payload.isOk) {
                        if (payload.payload?.sid == null) {
                            toolbar_menu.visibility = View.INVISIBLE
                        }
                        //toolbar_menu 可能要隐藏，比如重新发起审批就不需要这个
                        toolbar_menu.setOnClickListener {

                            sid = payload.payload?.sid
                            var name = payload.payload?.name

                            var mDialog = MaterialDialog.Builder(this@BuildSignActivity)
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

//    override fun onPause() {
//        super.onPause()
//        onBackPressed()
//    }

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

        //project_id          项目名称                2
        //reasons               签字事由                4
        //sealFile              签字文件                9
        //info                  备注说明                4
        //sms              是否发短信提醒审批人      5
        if ((paramMap.get("reasons") == null || (paramMap.get("reasons") as String?).isNullOrEmpty()) &&
                (paramMap.get("sealFile") == null || (paramMap.get("sealFile") as ArrayList<CustomSealBean.ValueBean>).size == 0) &&
                (paramMap.get("info") == null || (paramMap.get("info") as String?).isNullOrEmpty()) &&
                (paramMap.get("sms") == null || (paramMap.get("sms") as Int) == 0) &&
                (paramMap.get("project_id") == null)) {//project_id 选填
            super.onBackPressed()
            return
        }
        var mDialog = MaterialDialog.Builder(this@BuildSignActivity)
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
            builder.put("template_id", paramId!!)
            builder.put("data", HashMap<String, Any?>())
            SoguApi.getService(application)
                    .saveDraft(builder)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeOn(Schedulers.io())
                    .subscribe({ payload ->
                        if (payload.isOk) {
                            super.onBackPressed()
                        } else {
                            showCustomToast(R.drawable.icon_toast_fail,payload.message)
                        }
                    }, { e ->
                        Trace.e(e)
                    })
        }
        yes.setOnClickListener {
            val builder = HashMap<String, Any>()
            builder.put("template_id", paramId!!)
            builder.put("data", paramMap)
            SoguApi.getService(application)
                    .saveDraft(builder)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeOn(Schedulers.io())
                    .subscribe({ payload ->
                        if (payload.isOk) {
                            //                            showToast("草稿保存成功")
                            showCustomToast(R.drawable.icon_toast_success,"草稿保存成功")
                            super.onBackPressed()
                        } else {
                            showCustomToast(R.drawable.icon_toast_fail,payload.message)
                        }
                    }, { e ->
                        Trace.e(e)
//                        showToast("草稿保存失败")
                        showCustomToast(R.drawable.icon_toast_fail,"草稿保存失败")
                    })
        }
    }

    private var sid:Int? = null

    private fun load() {
        checkList.clear()
        ll_seal.removeAllViews()
        ll_approver.removeAllViews()
        SoguApi.getService(application)
                .approveInfo(template_id = paramId!!, sid = sid)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ payload ->
                    if (!payload.isOk) {
                        showCustomToast(R.drawable.icon_toast_fail,payload.message)
                        return@subscribe
                    }
                    payload.payload?.forEach { bean ->
                        addRow(bean, inflater)
                    }
                }, { e ->
                    Trace.e(e)
                    showCustomToast(R.drawable.icon_toast_common, "暂无可用数据")
                })
        requestApprove()
    }

    private fun requestApprove(fund_id: Int? = null) {
        SoguApi.getService(application)
                .approver(template_id = paramId!!
                        , type = paramType, fund_id = fund_id)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ payload ->
                    if (!payload.isOk) {
                        showCustomToast(R.drawable.icon_toast_fail, payload.message)
                        return@subscribe
                    }
                    payload.payload?.forEach { bean ->
                        addApprover(bean)
                    }
                }, { e ->
                    Trace.e(e)
                    //showToast("暂无可用数据")
                    showCustomToast(R.drawable.icon_toast_common, "暂无可用数据")
                })
    }

    fun doConfirm() {
        val builder = FormBody.Builder()
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
//                        showToast("提交成功")
                        showCustomToast(R.drawable.icon_toast_success,"提交成功")
                        handler.postDelayed({
                            finish()
                        },2000)
                    } else {
                        showCustomToast(R.drawable.icon_toast_fail, payload.message)
                    }
                }, { e ->
                    Trace.e(e)
//                    showToast("提交失败")
                    showCustomToast(R.drawable.icon_toast_fail,"提交失败")
                })
    }

    private fun addApprover(bean: ApproverBean) {
        val convertView = inflater.inflate(R.layout.cs_row_approver, null) as LinearLayout
        ll_approver.addView(convertView)
        val tvLabel = convertView.findViewById(R.id.tv_label) as TextView
        val etValue = convertView.findViewById(R.id.et_value) as TextView
        tvLabel.text = bean.position
        etValue.text = bean.approver
    }

    private fun addRow(bean: CustomSealBean, inflater: LayoutInflater) {
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
        val convertView = inflater.inflate(R.layout.cs_row_pop_list, null);
        ll_seal.addView(convertView)

        val tvLabel = convertView.findViewById(R.id.tv_label) as TextView
        val etValue = convertView.findViewById(R.id.et_value) as TextView
        tvLabel.text = if (bean.is_must == 1) bean.name + "(必填)" else bean.name

        checkList.add {
            val str = etValue.text?.toString()
            if (bean.is_must == 1 && str.isNullOrEmpty()) {
                false
            } else {
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
                MaterialDialog.Builder(this@BuildSignActivity)
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
                                paramMap.put(bean.fields!!, valBean?.id)
                                return true
                            }

                        })
                        .show()
            }

    }


    fun refreshListSelector(bean: CustomSealBean, data: CustomSealBean.ValueBean) {
        val view = viewMap.get(bean.fields)
        if (null != view) {
            view?.text = data.name
            paramMap.put(bean?.fields!!, data.id)
        }
    }

    val viewMap = HashMap<String, TextView>()
    fun add2(bean: CustomSealBean, inflater: LayoutInflater) {
        val convertView = inflater.inflate(R.layout.cs_row_pop_list, null);
        ll_seal.addView(convertView)

        val tvLabel = convertView.findViewById(R.id.tv_label) as TextView
        val etValue = convertView.findViewById(R.id.et_value) as TextView
        tvLabel.text = if (bean.is_must == 1) bean.name + "(必填)" else bean.name
        etValue.text = bean.value_map?.name
        viewMap.put(bean.fields, etValue)
        paramMap.put(bean.fields, bean.value_map?.id)// TODO
        checkList.add {
            val str = etValue.text?.toString()
            if (bean.is_must == 1 && str.isNullOrEmpty()) {
                false
            } else {
                true
            }
        }
        etValue.setOnClickListener {
            ListSelectorActivity.start(this, bean)
        }
    }

    private fun add3(bean: CustomSealBean, inflater: LayoutInflater) {
        val convertView = inflater.inflate(R.layout.cs_row_edit_text, null);
        ll_seal.addView(convertView)

        val tvLabel = convertView.findViewById(R.id.tv_label) as TextView
        val etValue = convertView.findViewById(R.id.et_value) as EditText
        tvLabel.text = if (bean.is_must == 1) bean.name + "(必填)" else bean.name
        etValue.filters = Utils.getFilter(this)

        checkList.add {
            val str = etValue.text?.toString()
            paramMap.put(bean.fields, str)
            if (bean.is_must == 1 && str.isNullOrEmpty()) {
                false
            } else {
                true
            }
        }

    }

    private fun add4(bean: CustomSealBean, inflater: LayoutInflater) {
        val convertView = inflater.inflate(R.layout.cs_row_edit_box, null);
        ll_seal.addView(convertView)

        val tvLabel = convertView.findViewById(R.id.tv_label) as TextView
        val etValue = convertView.findViewById(R.id.et_value) as EditText
        tvLabel.text = if (bean.is_must == 1) bean.name + "(必填)" else bean.name
        etValue.setText(bean.value)
        etValue.filters = Utils.getFilter(this)

        paramMap.put(bean.fields, bean.value)// TODO
        checkList.add {
            val str = etValue.text?.toString()
            paramMap.put(bean.fields, str)
            if (bean.is_must == 1 && str.isNullOrEmpty()) {
                false
            } else {
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

    private fun add5(bean: CustomSealBean, inflater: LayoutInflater) {
        val convertView = inflater.inflate(R.layout.cs_row_radio, null);
        ll_seal.addView(convertView)
        val tvLabel = convertView.findViewById(R.id.tv_label) as TextView
        val rgCheck = convertView.findViewById(R.id.rg_check) as RadioGroup
        tvLabel.text = if (bean.is_must == 1) bean.name + "(必填)" else bean.name
        paramMap.put(bean.fields, 0)
        rgCheck.setOnCheckedChangeListener { group, checkedId ->
            if (checkedId == R.id.rb_yes) {
                paramMap.put(bean.fields, 1)
            } else {
                paramMap.put(bean.fields, 0)
            }
        }
    }

    private fun add6(bean: CustomSealBean, inflater: LayoutInflater) {

        val convertView = inflater.inflate(R.layout.cs_row_check_box, null);
        ll_seal.addView(convertView)

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
                false
            } else {
                true
            }
        }
    }

    fun add8(bean: CustomSealBean, inflater: LayoutInflater) {
        val convertView = inflater.inflate(R.layout.cs_row_images, null);
        ll_seal.addView(convertView)
        val tvLabel = convertView.findViewById(R.id.tv_label) as TextView
        tvLabel.text = if (bean.is_must == 1) bean.name + "(必填)" else bean.name

        val ll_images = convertView.findViewById(R.id.ll_images) as FlowLayout
        ll_images.removeAllViews()
        refreshImages(bean!!, ll_images!!)

    }

    fun uploadImage(filePath: String?, imagesBean: CustomSealBean, imagesView: FlowLayout) {
        if (null != imagesBean && null != imagesView && null != filePath) {
            val file = File(filePath!!)
            SoguApi.getService(application)
                    .uploadApprove(MultipartBody.Builder().setType(MultipartBody.FORM)
                            .addFormDataPart("file", file.getName(), RequestBody.create(MediaType.parse("*/*"), file))
                            .addFormDataPart("control", 8.toString())
                            .addFormDataPart("template_id", "${paramId}")
                            .build())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeOn(Schedulers.io())
                    .subscribe({ payload ->
                        if (payload.isOk && payload.payload != null) {
//                            showToast("上传成功")
                            showCustomToast(R.drawable.icon_toast_success,"上传成功")
                            imagesBean?.value_list?.add(payload.payload!!)
                            refreshImages(imagesBean!!, imagesView!!)
                        } else {
                            showCustomToast(R.drawable.icon_toast_fail,payload.message)
                        }
                    }, { e ->
                        Trace.e(e)
//                        showToast("上传失败")
                        showCustomToast(R.drawable.icon_toast_fail,"上传失败")
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
                RxGalleryFinal.with(this@BuildSignActivity)
                        .image()
                        .radio()
                        .imageLoader(ImageLoaderType.GLIDE)
                        .subscribe(object : RxBusResultDisposable<ImageRadioResultEvent>() {
                            override fun onEvent(event: ImageRadioResultEvent?) {
                                val path = event?.result?.originalPath
                                if (!TextUtils.isEmpty(path))
                                    uploadImage(path!!, bean!!, imagesView!!)
                            }
                        })
                        .openGallery()
//                var intent = Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
//                startActivityForResult(intent, 0x101)
            }
        }
    }

    var filesBean: CustomSealBean? = null
    var filesView: LinearLayout? = null
    private fun add9(bean: CustomSealBean, inflater: LayoutInflater) {
        val convertView = inflater.inflate(R.layout.cs_row_files, null);
        ll_seal.addView(convertView)
        val tvLabel = convertView.findViewById(R.id.tv_label) as TextView
        val tvFile = convertView.findViewById(R.id.tv_file) as TextView
        tvLabel.text = if (bean.is_must == 1) bean.name + "(必填)" else bean.name

        val ll_files = convertView.findViewById(R.id.ll_files) as LinearLayout
        ll_files.removeAllViews()
        filesBean = bean;
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
                            .addFormDataPart("file", file.getName(), RequestBody.create(MediaType.parse("*/*"), file))
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
                        } else {
                            showCustomToast(R.drawable.icon_toast_fail,payload.message)
                        }
                    }, { e ->
                        Trace.e(e)
//                        showToast("上传失败")
                        showCustomToast(R.drawable.icon_toast_fail,"上传失败")
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

    val REQ_SELECT_FILE = 0xf0;
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
            val data = data?.getSerializableExtra(Extras.DATA2) as CustomSealBean.ValueBean
            if (paramTitle == "基金用印") {
                ll_approver.removeAllViews()
                requestApprove(data.id)
            }
            refreshListSelector(bean, data)
        } else if (resultCode == RESULT_OK && requestCode == 0x101) {//带测试，用来选择图片的
            var uri = data?.getData()
            var cursor = getContentResolver().query(uri, null, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                var path = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA))
                Log.e("path", path)
                var imagesView = (ll_seal.parent as LinearLayout).findViewWithTag("UNIQUE") as FlowLayout
                //uploadImage(path!!, bean!!, imagesView!!)
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    companion object {
        fun start(ctx: Activity?, itemBean: SpGroupItemBean) {
            val intent = Intent(ctx, BuildSignActivity::class.java)
            intent.putExtra(Extras.DATA, itemBean)
            ctx?.startActivity(intent)
        }

        fun start(ctx: Activity?, bean: ApprovalBean) {
            val intent = Intent(ctx, BuildSignActivity::class.java)
            intent.putExtra(Extras.DATA, bean)
            ctx?.startActivity(intent)
        }

        fun start(ctx: Activity?, bean: MessageBean) {
            val intent = Intent(ctx, BuildSignActivity::class.java)
            intent.putExtra(Extras.DATA, bean)
            ctx?.startActivity(intent)
        }


        fun start(ctx: Activity?, id: Int, paramType: Int?, paramTitle: String) {
            val intent = Intent(ctx, BuildSignActivity::class.java)
            intent.putExtra(Extras.ID, id)
            intent.putExtra(Extras.TYPE, paramType)
            intent.putExtra(Extras.TITLE, paramTitle)
            ctx?.startActivity(intent)
        }
    }
}