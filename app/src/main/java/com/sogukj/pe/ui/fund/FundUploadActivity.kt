package com.sogukj.pe.ui.fund

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.Theme
import com.bumptech.glide.Glide
import com.framework.base.ToolbarActivity
import com.nbsp.materialfilepicker.MaterialFilePicker
import com.nbsp.materialfilepicker.ui.FilePickerActivity
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.bean.FundSmallBean
import com.sogukj.pe.bean.ProjectBean
import com.sogukj.pe.ui.fund.FundListFragment.Companion.TYPE_CB
import com.sogukj.pe.ui.fund.FundListFragment.Companion.TYPE_CX
import com.sogukj.pe.ui.fund.FundListFragment.Companion.TYPE_TC
import com.sogukj.pe.util.Trace
import com.sogukj.service.SoguApi
import com.sogukj.util.Store
import com.sogukj.util.XmlDb
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_fund_upload.*
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import org.jetbrains.anko.backgroundColor
import org.jetbrains.anko.backgroundResource
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class FundUploadActivity : ToolbarActivity() {
    val filterList = TreeMap<Int, String>()
    lateinit var project: FundSmallBean
    val fmt = SimpleDateFormat("yyyy-MM-dd HH:mm")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        project = intent.getSerializableExtra(Extras.DATA) as FundSmallBean
        val map = intent.getSerializableExtra(Extras.MAP) as HashMap<Int, String>
        filterList.putAll(map)
        setContentView(R.layout.activity_fund_upload)
        setBack(true)
        title = project.fundName

        var tmpInt = XmlDb.open(context).get(Extras.TYPE, "").toInt()
        var tmpStr = ""
        if (tmpInt == TYPE_CB) {
            tmpStr = "储备"
        } else if (tmpInt == TYPE_CX) {
            tmpStr = "存续"
        } else if (tmpInt == TYPE_TC) {
            tmpStr = "退出"
        }
        tv_step.text = tmpStr

        val user = Store.store.getUser(this)
        tv_user.text = user?.name
        Glide.with(this)
                .load(user?.headImage())
                .error(R.drawable.img_logo_user)
                .into(iv_user)
        //tv_title.text = project.name
        llgroup.setOnClickListener {
            val items = ArrayList<String?>()
            items.add("储备期档案")
            items.add("存续期档案")
            items.add("退出期档案")
            //1=>储备期档案,2=>  存续期档案,3=> 退出期档案
            MaterialDialog.Builder(this@FundUploadActivity)
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
            MaterialDialog.Builder(this@FundUploadActivity)
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
        btn_upload.isEnabled = false
        btn_upload.backgroundColor = Color.GRAY
        showToast("正在上传")
        val file = File(uploadBean.file)
        //type	number	1	档案类型	非空（1=>项目文书，2=>基金档案）
        SoguApi.getService(application)
                .uploadBook(MultipartBody.Builder().setType(MultipartBody.FORM)
                        .addFormDataPart("type", "2")
                        .addFormDataPart("file", file.getName(), RequestBody.create(MediaType.parse("*/*"), file))
                        .addFormDataPart("company_id", project.id?.toString())
                        .addFormDataPart("status", uploadBean.group)
                        //.addFormDataPart("fileClass", uploadBean.type)//非空(当type=1时值为首次进入此页面返回的filter=>id,当type=2时无需传此字段)
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
                    btn_upload.isEnabled = true
                    btn_upload.backgroundResource = R.drawable.bg_btn_blue
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
        fun start(ctx: Activity?, bean: FundSmallBean, filterList: HashMap<Int, String>) {
            val intent = Intent(ctx, FundUploadActivity::class.java)
            intent.putExtra(Extras.DATA, bean)
            intent.putExtra(Extras.MAP, filterList)
            ctx?.startActivity(intent)
        }
    }
}
