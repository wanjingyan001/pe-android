package com.sogukj.pe.ui.approve

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import com.framework.base.ToolbarActivity
import com.google.gson.Gson
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.util.Trace
import com.sogukj.service.SoguApi
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_build_seal.*
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.Theme
import com.sogukj.pe.view.FlowLayout
import android.widget.TextView
import cn.finalteam.rxgalleryfinal.RxGalleryFinal
import cn.finalteam.rxgalleryfinal.imageloader.ImageLoaderType
import cn.finalteam.rxgalleryfinal.rxbus.RxBusResultDisposable
import cn.finalteam.rxgalleryfinal.rxbus.event.ImageRadioResultEvent
import com.bumptech.glide.Glide
import com.nbsp.materialfilepicker.MaterialFilePicker
import com.nbsp.materialfilepicker.ui.FilePickerActivity
import com.sogukj.pe.bean.*
import okhttp3.FormBody
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.File
import java.io.Serializable
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

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
        val paramObj = intent.getSerializableExtra(Extras.DATA) as Serializable?
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
            showToast("参数错误")
            finish()
        }
        setContentView(R.layout.activity_build_seal)
        setBack(true)
        title = paramTitle
        ll_seal.removeAllViews()
        ll_approver.removeAllViews()
        val inflater = LayoutInflater.from(this)

        SoguApi.getService(application)
                .approveInfo(template_id = if (flagEdit) null else paramId!!, sid = if (!flagEdit) null else paramId!!)
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
                }, { e ->
                    Trace.e(e)
                    showToast("暂无可用数据")
                })

        SoguApi.getService(application)
                .approver(template_id = paramId!!
                        , type = paramType)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ payload ->
                    if (!payload.isOk) {
                        showToast(payload.message)
                        return@subscribe
                    }
                    payload.payload?.forEach { bean ->
                        addApprover(bean, inflater)
                    }
                }, { e ->
                    Trace.e(e)
                    showToast("暂无可用数据")
                })
        btn_confirm.setOnClickListener {
            var flag = true
            for (chk in checkList) {
                flag = flag.and(chk())
                if (!flag) break
            }
            if (flag) {
                doConfirm()
            } else {
                showToast("请填写完整后再提交")
            }
        }
    }

    fun doConfirm() {
        val builder = FormBody.Builder()
        if (flagEdit) {

            builder.add("approval_id", "${paramId}")
            for ((k, v) in paramMap) {
                if (v is String) {
                    builder.add(k, v)
                } else
                    builder.add(k, gson.toJson(v))
            }
            SoguApi.getService(application)
                    .updateApprove(builder.build())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeOn(Schedulers.io())
                    .subscribe({ payload ->
                        if (payload.isOk && payload.payload != null) {
                            showToast("保存成功")
                            finish()
                        } else
                            showToast(payload.message)
                    }, { e ->
                        Trace.e(e)
                        showToast("保存失败")
                    })
        } else {
            builder.add("template_id", "${paramId}")
            for ((k, v) in paramMap) {
                if (v is String) {
                    builder.add(k, v)
                } else
                    builder.add(k, gson.toJson(v))
            }
            SoguApi.getService(application)
                    .submitApprove(builder.build())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeOn(Schedulers.io())
                    .subscribe({ payload ->
                        if (payload.isOk && payload.payload != null) {
                            showToast("保存成功")
                            finish()
                        } else
                            showToast(payload.message)
                    }, { e ->
                        Trace.e(e)
                        showToast("保存失败")
                    })
        }
    }

    fun addApprover(bean: ApproverBean, inflater: LayoutInflater) {
        val convertView = inflater.inflate(R.layout.cs_row_approver, null);
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
        val convertView = inflater.inflate(R.layout.cs_row_pop_list, null);
        ll_seal.addView(convertView)

        val tvLabel = convertView.findViewById(R.id.tv_label) as TextView
        val etValue = convertView.findViewById(R.id.et_value) as TextView
        tvLabel.text = bean.name

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
        }
    }

    val viewMap = HashMap<String, TextView>()
    fun add2(bean: CustomSealBean, inflater: LayoutInflater) {
        val convertView = inflater.inflate(R.layout.cs_row_pop_list, null);
        ll_seal.addView(convertView)

        val tvLabel = convertView.findViewById(R.id.tv_label) as TextView
        val etValue = convertView.findViewById(R.id.et_value) as TextView
        tvLabel.text = bean.name
        etValue.text = bean.value_map?.name
        viewMap.put(bean.fields, etValue)
        val iv_alert = convertView.findViewById(R.id.iv_alert)
        iv_alert.visibility = View.GONE
        checkList.add {
            val str = etValue.text?.toString()
            if (flagEdit)
                paramMap.put(bean.fields!!, bean.value_map?.id)
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
        val convertView = inflater.inflate(R.layout.cs_row_edit_text, null);
        ll_seal.addView(convertView)

        val tvLabel = convertView.findViewById(R.id.tv_label) as TextView
        val etValue = convertView.findViewById(R.id.et_value) as TextView
        tvLabel.text = bean.name

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
        val convertView = inflater.inflate(R.layout.cs_row_edit_box, null);
        ll_seal.addView(convertView)

        val tvLabel = convertView.findViewById(R.id.tv_label) as TextView
        val etValue = convertView.findViewById(R.id.et_value) as TextView
        tvLabel.text = bean.name

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

    private fun add5(bean: CustomSealBean, inflater: LayoutInflater) {
        val convertView = inflater.inflate(R.layout.cs_row_radio, null);
        ll_seal.addView(convertView)
        val tvLabel = convertView.findViewById(R.id.tv_label) as TextView
        val rgCheck = convertView.findViewById(R.id.rg_check) as RadioGroup
        tvLabel.text = bean.name
        paramMap.put(bean.fields, 0)
        rgCheck.setOnCheckedChangeListener { group, checkedId ->
            if (checkedId == R.id.rb_yes) {
                paramMap.put(bean.fields, 1)
            } else {
                paramMap.put(bean.fields, 0)
            }
        }

        val iv_alert = convertView.findViewById(R.id.iv_alert)
        iv_alert.visibility = View.GONE
    }

    private fun add6(bean: CustomSealBean, inflater: LayoutInflater) {

        val convertView = inflater.inflate(R.layout.cs_row_check_box, null);
        ll_seal.addView(convertView)

        val tvLabel = convertView.findViewById(R.id.tv_label) as TextView
        tvLabel.text = bean.name

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
                cbCheck.isChecked = v.is_select == 1
                etNum.text = "${v.count}"
                cbCheck.setOnCheckedChangeListener { buttonView, isChecked ->
                    if (isChecked) {
                        v.is_select = 1
                    } else {
                        v.is_select = 0
                    }
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
        val convertView = inflater.inflate(R.layout.cs_row_images, null);
        ll_seal.addView(convertView)
        val tvLabel = convertView.findViewById(R.id.tv_label) as TextView
        tvLabel.text = bean.name

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
                            showToast("上传成功")
                            imagesBean?.value_list?.add(payload.payload!!)
                            refreshImages(imagesBean!!, imagesView!!)
                        } else
                            showToast(payload.message)
                    }, { e ->
                        Trace.e(e)
                        showToast("上传失败")
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
                                    uploadImage(path!!, bean!!, imagesView!!)
                            }
                        })
                        .openGallery()
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
        tvLabel.text = bean.name

        val ll_files = convertView.findViewById(R.id.ll_files) as LinearLayout
        ll_files.removeAllViews()
        filesBean = bean;
        filesView = ll_files
        refreshFiles(filesBean!!, filesView!!)

        tvFile.setOnClickListener {
            MaterialFilePicker()
                    .withActivity(this)
                    .withRequestCode(REQ_SELECT_FILE)
//                    .withFilter(Pattern.compile(".*\\.txt$")) // Filtering files and directories by file name using regexp
                    .start()
        }
    }

    fun uploadFile(filePath: String?) {
        if (null != filesBean && null != filesView && null != filePath) {
            val file = File(filePath!!)
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
                            showToast("上传成功")
                            filesBean?.value_list?.add(payload.payload!!)
                            refreshFiles(filesBean!!, filesView!!)
                        } else
                            showToast(payload.message)
                    }, { e ->
                        Trace.e(e)
                        showToast("上传失败")
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
            val filePath = data?.getStringExtra(FilePickerActivity.RESULT_FILE_PATH)
            uploadFile(filePath)
        } else if (requestCode == ListSelectorActivity.REQ_LIST_SELECTOR && resultCode === Activity.RESULT_OK) {
            val bean = data?.getSerializableExtra(Extras.DATA) as CustomSealBean
            val data = data?.getSerializableExtra(Extras.DATA2) as CustomSealBean.ValueBean
            paramMap.put(bean?.fields!!, data.id)
            refreshListSelector(bean, data)
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

        fun start(ctx: Activity?, id: Int, paramType: Int?, paramTitle: String, edit: Boolean = false) {
            val intent = Intent(ctx, BuildSealActivity::class.java)
            intent.putExtra(Extras.ID, id)
            intent.putExtra(Extras.TYPE, paramType)
            intent.putExtra(Extras.TITLE, paramTitle)
            intent.putExtra(Extras.FLAG, edit)
            ctx?.startActivity(intent)
        }
    }
}
