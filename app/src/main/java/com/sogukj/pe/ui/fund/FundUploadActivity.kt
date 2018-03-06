package com.sogukj.pe.ui.fund

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.Theme
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.framework.base.ToolbarActivity
import com.nbsp.materialfilepicker.MaterialFilePicker
import com.nbsp.materialfilepicker.ui.FilePickerActivity
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.bean.FundSmallBean
import com.sogukj.pe.bean.ProjectBean
import com.sogukj.pe.service.Payload
import com.sogukj.pe.ui.fileSelector.FileMainActivity
import com.sogukj.pe.ui.fund.FundListFragment.Companion.TYPE_CB
import com.sogukj.pe.ui.fund.FundListFragment.Companion.TYPE_CX
import com.sogukj.pe.ui.fund.FundListFragment.Companion.TYPE_TC
import com.sogukj.pe.util.Trace
import com.sogukj.pe.util.Utils
import com.sogukj.pe.view.RecyclerAdapter
import com.sogukj.pe.view.RecyclerHolder
import com.sogukj.service.SoguApi
import com.sogukj.util.Store
import com.sogukj.util.XmlDb
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_fund_upload.*
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import org.jetbrains.anko.backgroundColor
import org.jetbrains.anko.backgroundResource
import org.jetbrains.anko.find
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class FundUploadActivity : ToolbarActivity() {
    val filterList = TreeMap<Int, String>()
    lateinit var project: FundSmallBean
    lateinit var map: HashMap<Int, String>
    lateinit var uploadAdapter: UploadAdapter
    val uploadList = ArrayList<UploadBean>()
    val upBean = UploadBean()
    var currentPosition = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        project = intent.getSerializableExtra(Extras.DATA) as FundSmallBean
        map = intent.getSerializableExtra(Extras.MAP) as HashMap<Int, String>
        filterList.putAll(map)
        setContentView(R.layout.activity_fund_upload)
        setBack(true)
        title = project.fundName

        val tmpInt = XmlDb.open(context).get(Extras.TYPE, "").toInt()
        var tmpStr = ""
        when (tmpInt) {
            TYPE_CB -> tmpStr = "储备"
            TYPE_CX -> tmpStr = "存续"
            TYPE_TC -> tmpStr = "退出"
        }
        tv_step.text = tmpStr

        val user = Store.store.getUser(this)
        tv_user.text = user?.name
        Glide.with(this)
                .load(user?.headImage())
                .apply(RequestOptions().error(R.drawable.img_logo_user))
                .into(iv_user)
        uploadAdapter = UploadAdapter(this, uploadList)
        upload_file_list.layoutManager = LinearLayoutManager(this)
        upload_file_list.adapter = uploadAdapter

    }

    class UploadBean {
        var file: String? = null
        var group: String? = null
        var type: String? = null
        var date: Long? = null
        var isSuccess: Boolean = false
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

    var index = 0
    private fun doSave(bean: UploadBean, btn_upload: Button) {
        if (!doCheck(bean)) {
            return
        }
        if (bean.isSuccess) {
            return
        }
        btn_upload.isEnabled = false
        btn_upload.backgroundColor = Color.GRAY
        val file = File(bean.file)
        //type	number	1	档案类型	非空（1=>项目文书，2=>基金档案）
        SoguApi.getService(application)
                .uploadBook(MultipartBody.Builder().setType(MultipartBody.FORM)
                        .addFormDataPart("type", "2")
                        .addFormDataPart("file", file.name, RequestBody.create(MediaType.parse("*/*"), file))
                        .addFormDataPart("company_id", project.id.toString())
                        .addFormDataPart("status", bean.group)
                        //.addFormDataPart("fileClass", uploadBean.type)//非空(当type=1时值为首次进入此页面返回的filter=>id,当type=2时无需传此字段)
                        .build())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(
                        { payload ->
                            if (payload.isOk) {
                                index++
                                bean.isSuccess = true
                            } else
                                showToast(payload.message)
                        }, { e ->
                    Trace.e(e)
                    hideProgress()
                    showToast("上传失败")
                    btn_upload.isEnabled = true
                    btn_upload.backgroundResource = R.drawable.bg_btn_blue
                }, {
                    if (index == uploadList.size) {
                        showToast("上传成功")
                        hideProgress()
                        finish()
                    }
                }, {
                    showProgress("正在上传")
                })
    }




    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (data != null && resultCode === Activity.RESULT_OK) {
            if (requestCode == REQ_SELECT_FILE) {
                data.let {
                    val paths = it.getStringArrayListExtra(Extras.LIST)
                    paths.forEach {
                        val bean = UploadBean()
                        bean.file = it
                        bean.date = System.currentTimeMillis()
                        uploadList.add(bean)
                    }
                    uploadAdapter.notifyDataSetChanged()
                }
            } else if (requestCode == REQ_CHANGE_FILE) {
                data.let {
                    val path = it.getStringExtra(Extras.DATA)
                    uploadList[currentPosition].file = path
                    uploadList[currentPosition].date = System.currentTimeMillis()
                    uploadAdapter.notifyDataSetChanged()
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    companion object {
        val REQ_SELECT_FILE = 0xf0
        val REQ_CHANGE_FILE = 0xf1
        fun start(ctx: Activity?, bean: FundSmallBean, filterList: HashMap<Int, String>) {
            val intent = Intent(ctx, FundUploadActivity::class.java)
            intent.putExtra(Extras.DATA, bean)
            intent.putExtra(Extras.MAP, filterList)
            ctx?.startActivity(intent)
        }
    }

    inner class UploadAdapter(val context: Context, val uploadList: List<UploadBean>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
        val items = ArrayList<String?>()

        init {
            items.add("储备期档案")
            items.add("存续期档案")
            items.add("退出期档案")
        }

        override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): RecyclerView.ViewHolder {
            return when (viewType) {
                0 -> {
                    FileHolder(LayoutInflater.from(context).inflate(R.layout.item_fund_upload, parent, false))
                }
                else -> {
                    EmptyHolder(LayoutInflater.from(context).inflate(R.layout.item_empty_upload, parent, false))
                }
            }
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder?, position: Int) {
            if (holder is FileHolder) {
                holder.setData(uploadList[position], position)
            } else if (holder is EmptyHolder) {
                holder.setData()
            }
        }

        override fun getItemCount(): Int = uploadList.size + 1

        override fun getItemViewType(position: Int): Int {
            return if (position == uploadList.size) 1 else 0
        }

        inner class FileHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val ll_upload_full = itemView.find<LinearLayout>(R.id.ll_upload_full)
            val tv_file = itemView.find<TextView>(R.id.tv_file)
            val tv_time = itemView.find<TextView>(R.id.tv_time)
            val llgroup = itemView.find<LinearLayout>(R.id.llgroup)
            val tv_group = itemView.find<TextView>(R.id.tv_group)
            val tv_class = itemView.find<TextView>(R.id.tv_class)
            fun setData(data: UploadBean, position: Int) {
                val file = File(data.file)
                tv_file.text = file.name
                tv_time.text = Utils.getTime(data.date!!, "yyyy-MM-dd HH:mm")
                if (upBean.group != null) {
                    tv_group.tag = upBean.group
                    tv_group.text = items[upBean.group?.toInt()!! - 1]
                }
                //分类点击
                llgroup.setOnClickListener {
                    //1=>储备期档案,2=>  存续期档案,3=> 退出期档案
                    MaterialDialog.Builder(this@FundUploadActivity)
                            .theme(Theme.LIGHT)
                            .items(items)
                            .canceledOnTouchOutside(true)
                            .itemsCallbackSingleChoice(-1, MaterialDialog.ListCallbackSingleChoice { dialog, v, p, s ->
                                if (p == -1) return@ListCallbackSingleChoice false
                                tv_group.text = items[p]
                                tv_group.tag = "${p + 1}"
                                data.group = "${p + 1}"
                                dialog?.dismiss()
                                true
                            })
                            .show()
                }
                //标签点击
                val rMap = HashMap<String, String>()
                map.entries.forEach { e -> rMap.put(e.value, "${e.key}") }
                tv_class.setOnClickListener {
                    val items = ArrayList<String?>()
                    items.addAll(map.values)
                    MaterialDialog.Builder(this@FundUploadActivity)
                            .theme(Theme.LIGHT)
                            .items(items)
                            .canceledOnTouchOutside(true)
                            .itemsCallbackSingleChoice(-1, MaterialDialog.ListCallbackSingleChoice { dialog, v, p, s ->
                                if (p == -1) return@ListCallbackSingleChoice false
                                tv_class.text = items[p]
                                tv_class.tag = rMap[items[p]]
                                data.type = "${rMap[items[p]]}"
                                dialog?.dismiss()
                                true
                            })
                            .show()
                }
                //替换文件
                ll_upload_full.setOnClickListener {
                    currentPosition = position
                    FileMainActivity.start(this@FundUploadActivity, 1, true, REQ_CHANGE_FILE)
                }

            }
        }

        inner class EmptyHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val ll_upload_empty = itemView.find<LinearLayout>(R.id.ll_upload_empty)
            val llgroup = itemView.find<LinearLayout>(R.id.llgroup)
            val tv_group = itemView.find<TextView>(R.id.tv_group)
            val tv_class = itemView.find<TextView>(R.id.tv_class)
            val btn_upload = itemView.find<Button>(R.id.btn_upload)
            fun setData() {
                //分类点击
                llgroup.setOnClickListener {
                    //1=>储备期档案,2=>  存续期档案,3=> 退出期档案
                    MaterialDialog.Builder(this@FundUploadActivity)
                            .theme(Theme.LIGHT)
                            .items(items)
                            .canceledOnTouchOutside(true)
                            .itemsCallbackSingleChoice(-1, MaterialDialog.ListCallbackSingleChoice { dialog, v, p, s ->
                                if (p == -1) return@ListCallbackSingleChoice false
                                tv_group.text = items[p]
                                tv_group.tag = "${p + 1}"
                                upBean.group = "${p + 1}"
                                dialog?.dismiss()
                                true
                            })
                            .show()
                }
                //标签点击
                val rMap = HashMap<String, String>()
                map.entries.forEach { e -> rMap.put(e.value, "${e.key}") }
                tv_class.setOnClickListener {
                    val items = ArrayList<String?>()
                    items.addAll(map.values)
                    MaterialDialog.Builder(this@FundUploadActivity)
                            .theme(Theme.LIGHT)
                            .items(items)
                            .canceledOnTouchOutside(true)
                            .itemsCallbackSingleChoice(-1, MaterialDialog.ListCallbackSingleChoice { dialog, v, p, s ->
                                if (p == -1) return@ListCallbackSingleChoice false
                                tv_class.text = items[p]
                                tv_class.tag = rMap[items[p]]
                                upBean.type = "${rMap[items[p]]}"
                                dialog?.dismiss()
                                true
                            })
                            .show()
                }
                //选择文件
                ll_upload_empty.setOnClickListener {
                    FileMainActivity.start(this@FundUploadActivity, requestCode = REQ_SELECT_FILE)
                }

                //上传
                btn_upload.setOnClickListener {
                    handler.postDelayed({
                        uploadList.forEach {
                            doSave(it, btn_upload)
                        }
                    }, 10)
                }
            }
        }
    }

}
