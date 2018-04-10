package com.sogukj.pe.ui.partyBuild

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.design.widget.BottomSheetDialog
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.Gravity
import android.view.View
import android.widget.PopupWindow
import android.widget.TextView
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
import com.sogukj.pe.view.RecyclerAdapter
import com.sogukj.pe.view.RecyclerHolder
import com.sogukj.service.SoguApi
import com.sogukj.util.XmlDb
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_party_upload.*
import kotlinx.android.synthetic.main.item_fund_account_list.view.*
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import org.jetbrains.anko.find
import org.jetbrains.anko.textColor
import java.io.File
import kotlin.properties.Delegates

class PartyUploadActivity : BaseActivity() {
    var selectFile: File? = null
    var columnId: Int by Delegates.notNull()

    companion object {
        val SELECTFILE = 0x1009
        fun start(context: Activity) {
            val intent = Intent(context, PartyUploadActivity::class.java)
            context.startActivityForResult(intent, Extras.REQUESTCODE)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_party_upload)
        Utils.setWindowStatusBarColor(this, R.color.party_toolbar_red)
        toolbar_title.text = "文件上传"
        addTv.text = "上传"
        val dialog = initBottomDialog()
        uploadLayout.setOnClickListener {
            FileMainActivity.start(this, 1, true, SELECTFILE)
        }
        fileNameEdtLayout.setOnClickListener {
            titleEdt.requestFocus()
        }
        titleEdt.setOnFocusChangeListener { v, hasFocus ->
            if (hasFocus) {
                titleEdt.setSelection(titleEdt.text.length)
            }
        }
        columnLayout.setOnClickListener {

            //                MaterialDialog.Builder(this)
//                        .theme(Theme.LIGHT)
//                        .items(map)
//                        .itemsCallbackSingleChoice(map.indexOf(columnTv.text), { dialog, itemView, which, text ->
//                            columnId = tabs[which].id
//                            columnTv.text = text
//                            true
//                        })
//                        .positiveText("确定")
//                        .negativeText("取消")
//                        .show()
            dialog.show()
        }
        addTv.setOnClickListener {
            uploadFile()
        }
        back.setOnClickListener {
            finish()
        }
    }

    private fun initBottomDialog() : BottomSheetDialog{
        val dialog = BottomSheetDialog(this@PartyUploadActivity)
        val inflate = layoutInflater.inflate(R.layout.layout_party_upload, null)
        val tabsList = inflate.find<RecyclerView>(R.id.tabList)
        val adapter = RecyclerAdapter<String>(context, { _adapter, parent, type ->
            val view = _adapter.getView(android.R.layout.simple_list_item_1, parent)
            object : RecyclerHolder<String>(view) {
                val tv = view.find<TextView>(android.R.id.text1)
                override fun setData(view: View, data: String, position: Int) {
                    tv.gravity = Gravity.CENTER
                    tv.textColor = R.color.text_1
                    tv.text = data
                }
            }
        })
        val tabsJson = XmlDb.open(this).get(PartyMainActivity.TABS, "")
        if (tabsJson.isNotEmpty() && tabsJson != "") {
            val tabs: List<PartyTabBean> = Gson().fromJson(tabsJson, object : TypeToken<List<PartyTabBean>>() {}.type)
            val map = tabs.map { it.classname }
            adapter.dataList.addAll(ArrayList<String>(map))
            adapter.onItemClick = { _, p ->
                columnId = tabs[p].id
                columnTv.text = adapter.dataList[p]
                dialog.dismiss()
            }
        }
        tabsList.layoutManager = LinearLayoutManager(this)
        tabsList.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))
        tabsList.adapter = adapter
        dialog.setContentView(inflate)
        return dialog
    }

    private fun uploadFile() {
        if (selectFile == null) {
            showCustomToast(R.drawable.icon_toast_common, "请选择文件")
            return
        }
        if (columnId <= 0) {
            showCustomToast(R.drawable.icon_toast_common, "请选择栏目")
            return
        }
        val name = if (titleEdt.text.toString().isNotEmpty() && titleEdt.hint != "不填则标题默认为文件名称") {
            titleEdt.text.toString()
        } else {
            selectFile!!.name
        }
        SoguApi.getService(application)
                .uploadPartyFile(MultipartBody.Builder().setType(MultipartBody.FORM)
                        .addFormDataPart("file", selectFile!!.name, RequestBody.create(MediaType.parse("*/*"), selectFile!!))
                        .addFormDataPart("file_name", name)
                        .addFormDataPart("id", "$columnId")
                        .build())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ payload ->
                    if (payload.isOk) {
                        showCustomToast(R.drawable.icon_toast_success, "上传成功")
                        setResult(Extras.RESULTCODE)
                        finish()
                    } else {
                        showCustomToast(R.drawable.icon_toast_fail, payload.message)
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
