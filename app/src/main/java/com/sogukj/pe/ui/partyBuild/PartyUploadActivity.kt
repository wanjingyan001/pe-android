package com.sogukj.pe.ui.partyBuild

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.Theme
import com.framework.base.BaseActivity
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.bean.PartyTabBean
import com.sogukj.pe.ui.fileSelector.FileMainActivity
import com.sogukj.pe.util.Utils
import com.sogukj.service.SoguApi
import com.sogukj.util.XmlDb
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_party_upload.*
import kotlinx.android.synthetic.main.layout_shareholder_toolbar.*
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.File
import kotlin.properties.Delegates

class PartyUploadActivity : BaseActivity() {
    var selectFile: File? = null
    var columnId: Int by Delegates.notNull()

    companion object {
        val SELECTFILE = 0x1009
        fun start(context: Activity) {
            val intent = Intent(context, PartyUploadActivity::class.java)
            context.startActivityForResult(intent,Extras.REQUESTCODE)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_party_upload)
        Utils.setWindowStatusBarColor(this, R.color.white)
        toolbar_title.text = "文件上传"
        addTv.text = "上传"

        uploadLayout.setOnClickListener {
            FileMainActivity.start(this, 1, true, SELECTFILE)
        }
        fileNameEdtLayout.setOnClickListener {
            titleEdt.requestFocus()
        }
        columnLayout.setOnClickListener {
            val tabsJson = XmlDb.open(this).get(PartyMainActivity.TABS, "")
            if (tabsJson.isNotEmpty() && tabsJson != "") {
                val tabs: List<PartyTabBean> = Gson().fromJson(tabsJson, object : TypeToken<List<PartyTabBean>>() {}.type)
                val map = tabs.map { it.classname }
                MaterialDialog.Builder(this)
                        .theme(Theme.LIGHT)
                        .items(map)
                        .itemsCallbackSingleChoice(map.indexOf(columnTv.text), { dialog, itemView, which, text ->
                            columnId = tabs[which].id
                            columnTv.text = text
                            true
                        })
                        .positiveText("确定")
                        .negativeText("取消")
                        .show()
            }
        }
        addTv.setOnClickListener {
            uploadFile()
        }
        back.setOnClickListener {
            finish()
        }
    }

    private fun uploadFile() {
        if (selectFile == null) {
            showToast("请选择文件")
            return
        }
        if (columnId <= 0) {
            showToast("请选择栏目")
            return
        }
        val name = if (titleEdt.text.toString().isNotEmpty() && titleEdt.hint != "不填则标题默认为文件名称") {
            titleEdt.text.toString()
        } else {
            selectFile!!.name
        }
        SoguApi.getService(application)
                .uploadPartyFile(MultipartBody.Builder().setType(MultipartBody.FORM)
                        .addFormDataPart("file",  selectFile!!.name, RequestBody.create(MediaType.parse("*/*"),  selectFile!!))
                        .addFormDataPart("file_name", name)
                        .addFormDataPart("id", "$columnId")
                        .build())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ payload ->
                    if (payload.isOk) {
                        showToast("上传成功")
                        setResult(Extras.RESULTCODE)
                        finish()
                    } else {
                        showToast(payload.message)
                    }
                }, {
                    hideProgress()
                }, {
                    hideProgress()
                }, {
                    showProgress("正在上传")
                })
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (data != null && requestCode == SELECTFILE && resultCode == Activity.RESULT_OK) {
            val file = data.getSerializableExtra(Extras.DATA) as File
            selectFile = file
            selectedFile.text = file.name
        }
    }
}
