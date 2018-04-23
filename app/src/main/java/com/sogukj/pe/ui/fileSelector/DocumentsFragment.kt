package com.sogukj.pe.ui.fileSelector


import android.content.Context
import android.os.Bundle
import android.os.Environment
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.sogukj.pe.Extras

import com.sogukj.pe.R
import com.sogukj.pe.util.FileTypeUtils
import com.sogukj.pe.util.FileUtil
import com.sogukj.pe.util.RxBus
import com.sogukj.pe.util.Utils
import com.sogukj.pe.view.RecyclerAdapter
import com.sogukj.pe.view.RecyclerHolder
import com.sougukj.setOnClickFastListener
import com.sougukj.setVisible
import kotlinx.android.synthetic.main.calendar_dingding.*
import kotlinx.android.synthetic.main.fragment_documents.*
import kotlinx.android.synthetic.main.layout_empty.*
import org.jetbrains.anko.find
import org.jetbrains.anko.imageResource
import org.jetbrains.anko.support.v4.ctx
import org.jetbrains.anko.toast
import java.io.File
import java.util.*


/**
 * A simple [Fragment] subclass.
 * Use the [DocumentsFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class DocumentsFragment : Fragment(), View.OnClickListener {


    private var type: Int? = null
    private var mParam2: String? = null

    lateinit var adapter: RecyclerAdapter<File>
    lateinit var files: List<File>
    lateinit var fileActivity: FileMainActivity
    lateinit var header: LinearLayout

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        fileActivity = activity as FileMainActivity
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (arguments != null) {
            type = arguments.getInt(TYPE, 0)
            mParam2 = arguments.getString(ARG_PARAM2)
        }
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_documents, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        adapter = RecyclerAdapter(context, { _adpater, parent, type ->
            DocumentHolder(_adpater.getView(R.layout.item_document_list, parent))
        })
        documentList.layoutManager = LinearLayoutManager(context)
        documentList.adapter = adapter
        header = layoutInflater.inflate(R.layout.layout_documents_header, documentList, false) as LinearLayout
        header.find<LinearLayout>(R.id.mPicManage).setOnClickListener(this)
        header.find<LinearLayout>(R.id.mVideoManage).setOnClickListener(this)
        header.find<LinearLayout>(R.id.mDocManage).setOnClickListener(this)
        header.find<LinearLayout>(R.id.mZipManage).setOnClickListener(this)
        documentList.addHeaderView(header)
    }


    override fun onStart() {
        super.onStart()
        val subscribe = RxBus.getIntanceBus().doSubscribe(String::class.java, { t ->
            if (t == Extras.REFRESH) {
                refreshList()
            }
        }, {
        })
        RxBus.getIntanceBus().addSubscription(Extras.REFRESH, subscribe)
    }

    override fun onResume() {
        super.onResume()
        refreshList()
    }


    private fun refreshList() {
        adapter.dataList.clear()
        when (type) {
            PE_LOCAL -> {
                files = FileUtil.getFiles(FileUtil.getExternalFilesDir(fileActivity.applicationContext))
            }
            ALL_DOC -> {
                val list = FileUtil.getFiles(WX_DOC_PATH1)
                val list1 = FileUtil.getFiles(WX_DOC_PATH2)
                val list2 = FileUtil.getFiles(QQ_DOC_PATH)
                val list4 = FileUtil.getFiles(QQ_DOC_PATH1)
                val list5 = FileUtil.getFiles(DING_TALK_PATH)
                val list3 = FileUtil.getFiles(FileUtil.getExternalFilesDir(fileActivity.applicationContext))
                files = list.plus(list1).plus(list2).plus(list3).plus(list4).plus(list5)
            }
            WX_DOC -> {
                val list = FileUtil.getFiles(WX_DOC_PATH1)
                val list1 = FileUtil.getFiles(WX_DOC_PATH2)
                files = list.plus(list1)
            }
            QQ_DOC -> {
                val list = FileUtil.getFiles(QQ_DOC_PATH)
                val list1 = FileUtil.getFiles(QQ_DOC_PATH1)
                files = list.plus(list1)
            }
            DING_TALK -> {
                val list = FileUtil.getFiles(DING_TALK_PATH)
                files = list
            }
        }
        Collections.sort(files) { o1, o2 ->
            o2.lastModified().compareTo(o1.lastModified())
        }
        if (files.isEmpty()) {
            documentList.setVisible(false)
            iv_empty.setVisible(true)
        } else {
            documentList.setVisible(true)
            iv_empty.setVisible(false)
            adapter.dataList.addAll(files)
            adapter.notifyDataSetChanged()
        }
        val map = FileUtil.getFilesByType(files)
        header.find<TextView>(R.id.mPicNum).text = "(${map[FileUtil.FileType.IMAGE]?.size})"
        header.find<TextView>(R.id.mVideoNum).text = "(${map[FileUtil.FileType.VIDEO]?.size})"
        header.find<TextView>(R.id.mDocNum).text = "(${map[FileUtil.FileType.DOC]?.size})"
        header.find<TextView>(R.id.mZipNum).text = "(${map[FileUtil.FileType.ZIP]?.size})"
    }


    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.mPicManage -> {
                FilterFileActivity.start(fileActivity, fileActivity.maxSize,
                        fileActivity.isReplace, fileActivity.isForResult, type!!, FileUtil.FileType.IMAGE)
            }
            R.id.mVideoManage -> {
                FilterFileActivity.start(fileActivity, fileActivity.maxSize,
                        fileActivity.isReplace, fileActivity.isForResult, type!!, FileUtil.FileType.VIDEO)
            }
            R.id.mDocManage -> {
                FilterFileActivity.start(fileActivity, fileActivity.maxSize,
                        fileActivity.isReplace, fileActivity.isForResult, type!!, FileUtil.FileType.DOC)
            }
            R.id.mZipManage -> {
                FilterFileActivity.start(fileActivity, fileActivity.maxSize,
                        fileActivity.isReplace, fileActivity.isForResult, type!!, FileUtil.FileType.ZIP)
            }
            else -> {

            }
        }
    }

    companion object {
        private val TYPE = "type"
        private val ARG_PARAM2 = "param2"
        val QQ_DOC_PATH = Environment.getExternalStorageDirectory().path + "/tencent/QQfile_recv/"
        val QQ_DOC_PATH1 = Environment.getExternalStorageDirectory().path + "/tencent/QQ_Images/"
        val WX_DOC_PATH1 = Environment.getExternalStorageDirectory().path + "/tencent/MicroMsg/WeiXin/"
        val WX_DOC_PATH2 = Environment.getExternalStorageDirectory().path + "/tencent/MicroMsg/Download/"
        val DING_TALK_PATH = Environment.getExternalStorageDirectory().path + "/DingTalk/"
        val PE_LOCAL = 0
        val ALL_DOC = 1
        val WX_DOC = 2
        val QQ_DOC = 3
        val DING_TALK = 4


        fun newInstance(type: Int, param2: String? = null): DocumentsFragment {
            val fragment = DocumentsFragment()
            val args = Bundle()
            args.putInt(TYPE, type)
            args.putString(ARG_PARAM2, param2)
            fragment.arguments = args
            return fragment
        }
    }


    inner class DocumentHolder(view: View) : RecyclerHolder<File>(view) {
        val slector = view.find<ImageView>(R.id.selectIcon)
        val icon = view.find<ImageView>(R.id.file_icon)
        val name = view.find<TextView>(R.id.file_name)
        val info = view.find<TextView>(R.id.file_info)
        val point = view.find<ImageView>(R.id.red_point)
        override fun setData(view: View, data: File, position: Int) {
            if (position == 0) {
                point.visibility = View.VISIBLE
            } else {
                point.visibility = View.GONE
            }
            slector.isSelected = fileActivity.selectedFile.contains(data)
            if (FileUtil.getFileType(data.absolutePath) != null) {
                Glide.with(context)
                        .load(data.absoluteFile)
                        .apply(RequestOptions().error(R.drawable.icon_pic))
                        .into(icon)
            } else {
                icon.imageResource = FileTypeUtils.getFileType(data).icon
            }
            name.text = data.name
            val builder = StringBuilder()
            when {
                data.absolutePath.contains("QQ") -> builder.append("QQ  ")
                data.absolutePath.contains(context.packageName) -> builder.append("本应用  ")
                data.absolutePath.contains("DingTalk") -> builder.append("钉钉  ")
                else -> builder.append("微信  ")
            }
            val time = Utils.getTime(data.lastModified(), "yyyy/MM/dd HH:mm")
            builder.append(time.substring(2, time.length) + "  ")
            builder.append(FileUtil.formatFileSize(data.length(), FileUtil.SizeUnit.Auto))
            info.text = builder.toString()

            view.setOnClickListener {
                if (!fileActivity.isReplace) {
                    if (fileActivity.selectedFile.contains(data)) {
                        fileActivity.selectedFile.remove(data)
                        slector.isSelected = false
                    } else {
                        if (fileActivity.selectedFile.size < fileActivity.maxSize) {
                            slector.isSelected = true
                            fileActivity.selectedFile.add(data)
                        } else {
                            context.toast("最多只能选择${fileActivity.maxSize}个")
                        }
                    }
                    fileActivity.showSelectedInfo()
                } else {
                    fileActivity.sendChangeFile(data)
                }
            }
        }
    }
}
