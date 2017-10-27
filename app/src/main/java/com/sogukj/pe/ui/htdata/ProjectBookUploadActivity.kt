package com.sogukj.pe.ui.htdata

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.Theme
import com.framework.base.ToolbarActivity
import com.nbsp.materialfilepicker.MaterialFilePicker
import com.nbsp.materialfilepicker.ui.FilePickerActivity
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.bean.ProjectBean
import com.sogukj.pe.util.Trace
import com.sogukj.service.SoguApi
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
    val filterList = TreeMap<String, String>()
    lateinit var project: ProjectBean
    val fmt = SimpleDateFormat("yyyy-MM-dd HH:mm")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        project = intent.getSerializableExtra(Extras.DATA) as ProjectBean
        val map = intent.getSerializableExtra(Extras.MAP) as HashMap<String, String>
        filterList.putAll(map)
        setContentView(R.layout.activity_project_book_upload)
        setBack(true)
        title = "项目文书上传"
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
                            uploadBean.group = "${p + 1}".toString()
                            dialog?.dismiss()
                            return true
                        }

                    })
                    .show()
        }
        val rMap = HashMap<String, String>()
        map.entries.forEach { e -> rMap.put(e.value, e.key) }
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
        btn_upload.setOnClickListener {
            val list = ArrayList<String>()
            MaterialFilePicker()
                    .withActivity(this)
                    .withRequestCode(REQ_SELECT_FILE)
//                    .withFilter(Pattern.compile(".*\\.txt$")) // Filtering files and directories by file name using regexp
//                    .withFilterDirectories(true) // Set directories filterable (false by default)
//                    .withHiddenFiles(true) // Show hidden files and folders
                    .start()

//            val intent = Intent(Intent.ACTION_GET_CONTENT)
//            intent.type = "*/*"
//            intent.addCategory(Intent.CATEGORY_OPENABLE);
//            startActivityForResult(Intent.createChooser(intent, "选择文件"), REQ_SELECT_FILE)
        }

        btn_commit.setOnClickListener {
            handler.postDelayed({ doSave() }, 10)
        }
    }

    class UploadBean {
        var file: String? = null
        var group: String? = null
        var type: String? = null
        fun doCheck(): Boolean {
            if (file.isNullOrEmpty()) return false
            if (null == group) return false
            if (type == null) return false
            return true
        }
    }

    var uploadBean = UploadBean();
    private fun doSave() {
        if (!uploadBean.doCheck()) {
            return
        }
        val file = File(uploadBean.file)
        SoguApi.getService(application)
                .uploadBook(MultipartBody.Builder().setType(MultipartBody.FORM)
                        .addFormDataPart("fileBook", file.getName(), RequestBody.create(MediaType.parse("*/*"), file))
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