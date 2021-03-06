package com.sogukj.pe.ui.fileSelector


import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.util.DiffUtil
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.sogukj.pe.Extras

import com.sogukj.pe.R
import com.sogukj.pe.util.FileUtil
import com.sogukj.pe.util.RxBus
import kotlinx.android.synthetic.main.calendar_dingding.*
import kotlinx.android.synthetic.main.fragment_storage_file.*
import java.io.File


class StorageFileFragment : Fragment() {
    interface FileClickListener {
        fun onFileClicked(clickedFile: File)
    }

    private var mPath: String? = null
    lateinit var mDirectoryAdapter: DirectoryAdapter
    private var mFileClickListener: FileClickListener? = null
    lateinit var fileActivity: FileMainActivity
    private lateinit var files: List<File>

    fun setListener(listener: FileClickListener) {
        mFileClickListener = listener
    }

    override fun onDestroy() {
        super.onDestroy()
        mFileClickListener = null
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        fileActivity = activity as FileMainActivity
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (arguments != null) {
            mPath = arguments.getString(ARG_FILE_PATH)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_storage_file, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        files = FileUtil.getFileListByDirPath(mPath, null).toMutableList()
        mDirectoryAdapter = DirectoryAdapter(context, files as MutableList<File>, fileActivity)
        mDirectoryAdapter.setOnItemClickListener(object : DirectoryAdapter.OnItemClickListener {
            override fun onItemClick(view: View, position: Int) {
                if (mFileClickListener != null) {
                    mFileClickListener?.onFileClicked(mDirectoryAdapter.getModel(position))
                }
            }
        })
        directory_recycler_view.layoutManager = LinearLayoutManager(context)
        directory_recycler_view.adapter = mDirectoryAdapter
        directory_recycler_view.setEmptyView(directory_empty_view)
    }


    fun changeData() {
        files = FileUtil.getFileListByDirPath(mPath, null).toMutableList()
        val result = DiffUtil.calculateDiff(DiffCallBack(mDirectoryAdapter.files, files))
        result.dispatchUpdatesTo(mDirectoryAdapter)
        mDirectoryAdapter.changeData(files.toMutableList())
//        mDirectoryAdapter.changeData(files as MutableList<File>)
    }

    companion object {
        private val ARG_FILE_PATH = "arg_file_path"

        fun newInstance(param1: String): StorageFileFragment {
            val fragment = StorageFileFragment()
            val args = Bundle()
            args.putString(ARG_FILE_PATH, param1)
            fragment.arguments = args
            return fragment
        }
    }
}
