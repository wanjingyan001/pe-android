package com.sogukj.pe.ui.htdata

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.Theme
import com.bumptech.glide.Glide
import com.framework.base.ToolbarActivity
import com.nbsp.materialfilepicker.MaterialFilePicker
import com.nbsp.materialfilepicker.ui.FilePickerActivity
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.bean.ProjectBean
import com.sogukj.pe.util.Trace
import com.sogukj.service.SoguApi
import com.sogukj.util.Store
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_project_book_upload.*
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap


class ProjectBookUploadActivity : ToolbarActivity() {
    val filterList = TreeMap<Int, String>()
    lateinit var project: ProjectBean
    val fmt = SimpleDateFormat("yyyy-MM-dd HH:mm")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        project = intent.getSerializableExtra(Extras.DATA) as ProjectBean
        val map = intent.getSerializableExtra(Extras.MAP) as HashMap<Int, String>
        filterList.putAll(map)
        setContentView(R.layout.activity_project_book_upload)
        setBack(true)
        title = "项目文书上传"
        val user = Store.store.getUser(this)
        tv_user.text = user?.name
        Glide.with(this)
                .load(user?.headImage())
                .error(R.drawable.img_logo_user)
                .into(iv_user)
        tv_title.text = project.name
        tv_group.setOnClickListener {
            val items = ArrayList<String?>()
            items.add("项目投资档案清单")
            items.add("投资后项目跟踪管理清单")
            items.add("项目推出档案清单")
            MaterialDialog.Builder(this@ProjectBookUploadActivity)
                    .theme(Theme.LIGHT)
                    .items(items)
                    .canceledOnTouchOutside(true)
                    .itemsCallbackSingleChoice(-1, object : MaterialDialog.ListCallbackSingleChoice {
                        override fun onSelection(dialog: MaterialDialog?, v: View?, p: Int, s: CharSequence?): Boolean {
                            if (p == -1) return false
                            tv_group.text = items[p]
                            tv_group.tag = "${p + 1}"
                            uploadBean.group = "${p + 1}"
                            dialog?.dismiss()
                            return true
                        }

                    })
                    .show()
        }
        val rMap = HashMap<String, String>()
        map.entries.forEach { e -> rMap.put(e.value, "${e.key}") }
        tv_class.setOnClickListener {
            val items = ArrayList<String?>()
            items.addAll(map.values)
            MaterialDialog.Builder(this@ProjectBookUploadActivity)
                    .theme(Theme.LIGHT)
                    .items(items)
                    .canceledOnTouchOutside(true)
                    .itemsCallbackSingleChoice(-1, object : MaterialDialog.ListCallbackSingleChoice {
                        override fun onSelection(dialog: MaterialDialog?, v: View?, p: Int, s: CharSequence?): Boolean {
                            if (p == -1) return false
                            tv_class.text = items[p]
                            tv_class.tag = rMap.get(items[p])
                            uploadBean.type = "${rMap.get(items[p])}"
                            dialog?.dismiss()
                            return true
                        }

                    })
                    .show()
        }
        ll_upload_empty.setOnClickListener {
            val list = ArrayList<String>()
            MaterialFilePicker()
                    .withActivity(this)
                    .withRequestCode(REQ_SELECT_FILE)
                    .withTitle("内部存储")
                    .withFilterDirectories(true)
                    .withHiddenFiles(true)
                    .withCloseMenu(false)
                    .start()
        }
        ll_upload_full.setOnClickListener {
            val list = ArrayList<String>()
            MaterialFilePicker()
                    .withActivity(this)
                    .withRequestCode(REQ_SELECT_FILE)
                    .withTitle("内部存储")
                    .withFilterDirectories(true)
                    .withHiddenFiles(true)
                    .withCloseMenu(false)
                    .start()
        }

        btn_upload.setOnClickListener {
            handler.postDelayed({ doSave() }, 10)
        }
    }

    class UploadBean {
        var file: String? = null
        var group: String? = null
        var type: String? = null
    }

    fun doCheck(bean: UploadBean): Boolean {
        if (bean.file.isNullOrEmpty()) {
            showToast("请选择文件")
            return false
        }
        if (null == bean.group) {
            showToast("请选择分类")
            return false
        }
        return true
    }

    var uploadBean = UploadBean();
    private fun doSave() {
        if (!doCheck(uploadBean)) {
            return
        }
        val file = File(uploadBean.file)
        //type	number	1	档案类型	非空（1=>项目文书，2=>基金档案）
        SoguApi.getService(application)
                .uploadBook(MultipartBody.Builder().setType(MultipartBody.FORM)
                        .addFormDataPart("type", "1")
                        .addFormDataPart("file", file.getName(), RequestBody.create(MediaType.parse("*/*"), file))
                        .addFormDataPart("company_id", project.company_id?.toString())
                        .addFormDataPart("status", uploadBean.group)
                        .addFormDataPart("fileClass", uploadBean.type)
                        .build())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ payload ->
                    if (payload.isOk) {
                        showToast("上传成功")
                        finish()
                    } else
                        showToast(payload.message)
                }, { e ->
                    Trace.e(e)
                    showToast("上传失败")
                })
    }

    val REQ_SELECT_FILE = 0xf0;
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQ_SELECT_FILE && resultCode === Activity.RESULT_OK) {
            val filePath = data?.getStringExtra(FilePickerActivity.RESULT_FILE_PATH)
            btn_upload.tag = filePath
            uploadBean.file = filePath
            val file = File(filePath)
            tv_file.text = file.name
            tv_time.text = fmt.format(Date())
            ll_upload_empty.visibility = View.GONE
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    companion object {
        fun start(ctx: Activity?, project: ProjectBean, filterList: HashMap<Int, String>) {
            val intent = Intent(ctx, ProjectBookUploadActivity::class.java)
            intent.putExtra(Extras.DATA, project)
            intent.putExtra(Extras.MAP, filterList)
            ctx?.startActivity(intent)
        }
    }
}
