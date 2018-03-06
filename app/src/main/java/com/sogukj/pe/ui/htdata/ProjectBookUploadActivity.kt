package com.sogukj.pe.ui.htdata

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
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.Theme
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.framework.base.ToolbarActivity
import com.nbsp.materialfilepicker.MaterialFilePicker
import com.nbsp.materialfilepicker.ui.FilePickerActivity
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.bean.ProjectBean
import com.sogukj.pe.service.Payload
import com.sogukj.pe.ui.fileSelector.FileMainActivity
import com.sogukj.pe.ui.fund.FundListFragment
import com.sogukj.pe.ui.fund.FundUploadActivity
import com.sogukj.pe.ui.fund.FundUploadActivity.Companion.REQ_CHANGE_FILE
import com.sogukj.pe.ui.fund.FundUploadActivity.Companion.REQ_SELECT_FILE
import com.sogukj.pe.ui.project.ProjectListFragment
import com.sogukj.pe.util.Trace
import com.sogukj.pe.util.Utils
import com.sogukj.service.SoguApi
import com.sogukj.util.Store
import com.sogukj.util.XmlDb
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_project_book_upload.*
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
import kotlin.collections.HashMap


class ProjectBookUploadActivity : ToolbarActivity() {
    val filterList = TreeMap<Int, String>()
    lateinit var project: ProjectBean
    lateinit var map: HashMap<Int, String>
    lateinit var uploadAdapter: UploadAdapter
    val uploadList = ArrayList<UploadBean>()
    val upBean = UploadBean()
    var currentPosition = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        project = intent.getSerializableExtra(Extras.DATA) as ProjectBean
        map = intent.getSerializableExtra(Extras.MAP) as HashMap<Int, String>
        filterList.putAll(map)
        setContentView(R.layout.activity_project_book_upload)
        setBack(true)
        title = "项目文书上传"
        val user = Store.store.getUser(this)
        tv_user.text = user?.name

        var tmpInt = XmlDb.open(context).get(Extras.TYPE, "").toInt()
        var tmpStr = ""
        when (tmpInt) {
            ProjectListFragment.TYPE_CB -> tmpStr = "储备"
            ProjectListFragment.TYPE_LX -> tmpStr = "立项"
            ProjectListFragment.TYPE_YT -> tmpStr = "已投"
            ProjectListFragment.TYPE_DY -> tmpStr = "调研"
            ProjectListFragment.TYPE_TC -> tmpStr = "退出"
        }
        tv_step.text = tmpStr

        Glide.with(this)
                .load(user?.headImage())
                .apply(RequestOptions().error(R.drawable.img_logo_user))
                .into(iv_user)
        tv_title.text = project.name
        uploadAdapter = UploadAdapter(this, uploadList)
        project_upload_list.layoutManager = LinearLayoutManager(this)
        project_upload_list.adapter = uploadAdapter
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
        if (null == bean.type) {
            showToast("请选择标签")
            return false
        }
        return true
    }

    var index = 0
    private fun doSave(uploadBean: UploadBean, btn_upload: Button) {
        if (!doCheck(uploadBean)) {
            return
        }
        if (uploadBean.isSuccess) {
            return
        }
        btn_upload.isEnabled = false
        btn_upload.backgroundColor = Color.GRAY
        val file = File(uploadBean.file)
        //type	number	1	档案类型	非空（1=>项目文书，2=>基金档案）
        SoguApi.getService(application)
                .uploadBook(MultipartBody.Builder().setType(MultipartBody.FORM)
                        .addFormDataPart("type", "1")
                        .addFormDataPart("file", file.name, RequestBody.create(MediaType.parse("*/*"), file))
                        .addFormDataPart("company_id", project.company_id?.toString())
                        .addFormDataPart("status", uploadBean.group)
                        .addFormDataPart("fileClass", uploadBean.type)
                        .build())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ payload ->
                    if (payload.isOk) {
                        index++
                        uploadBean.isSuccess = true
                    } else {
                        showToast(payload.message)
                    }
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
            if (requestCode == FundUploadActivity.REQ_SELECT_FILE) {
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
            } else if (requestCode == FundUploadActivity.REQ_CHANGE_FILE) {
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
        fun start(ctx: Activity?, project: ProjectBean, filterList: HashMap<Int, String>) {
            val intent = Intent(ctx, ProjectBookUploadActivity::class.java)
            intent.putExtra(Extras.DATA, project)
            intent.putExtra(Extras.MAP, filterList)
            ctx?.startActivity(intent)
        }
    }

    inner class UploadAdapter(val context: Context, val uploadList: List<UploadBean>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
        val items = ArrayList<String?>()

        init {
            items.add("项目投资档案")
            items.add("投资后项目跟踪管理档案")
            items.add("项目退出档案")
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
            val tag_layout = itemView.find<LinearLayout>(R.id.tag_layout)
            fun setData(data: UploadBean, position: Int) {
                tag_layout.visibility = View.VISIBLE
                val file = File(data.file)
                tv_file.text = file.name
                tv_time.text = Utils.getTime(data.date!!, "yyyy-MM-dd HH:mm")
                if (upBean.group != null) {
                    tv_group.tag = upBean.group
                    tv_group.text = items[upBean.group?.toInt()!! - 1]
                }
                if (upBean.type != null) {
                    tv_class.text = map[upBean.type?.toInt()]
                }
                //分类点击
                llgroup.setOnClickListener {
                    MaterialDialog.Builder(this@ProjectBookUploadActivity)
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
                    MaterialDialog.Builder(this@ProjectBookUploadActivity)
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
                    FileMainActivity.start(this@ProjectBookUploadActivity, 1, true, REQ_CHANGE_FILE)
                }

            }
        }

        inner class EmptyHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val ll_upload_empty = itemView.find<LinearLayout>(R.id.ll_upload_empty)
            val llgroup = itemView.find<LinearLayout>(R.id.llgroup)
            val tv_group = itemView.find<TextView>(R.id.tv_group)
            val tv_class = itemView.find<TextView>(R.id.tv_class)
            val btn_upload = itemView.find<Button>(R.id.btn_upload)
            val tag_layout = itemView.find<LinearLayout>(R.id.tag_layout)
            fun setData() {
                tag_layout.visibility = View.VISIBLE
                //分类点击
                llgroup.setOnClickListener {
                    MaterialDialog.Builder(this@ProjectBookUploadActivity)
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
                    MaterialDialog.Builder(this@ProjectBookUploadActivity)
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
                    FileMainActivity.start(this@ProjectBookUploadActivity, requestCode = REQ_SELECT_FILE)
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
