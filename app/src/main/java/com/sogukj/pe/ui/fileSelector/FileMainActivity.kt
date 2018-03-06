package com.sogukj.pe.ui.fileSelector

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.support.v4.app.FragmentStatePagerAdapter
import android.view.KeyEvent
import android.view.View
import android.widget.AdapterView
import android.widget.PopupWindow
import com.nbsp.materialfilepicker.ui.DirectoryFragment
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.util.FileUtil
import kotlinx.android.synthetic.main.activity_file_main.*
import kotlinx.android.synthetic.main.fragment_weekly_wait_to_watch.*
import java.io.File
import kotlin.properties.Delegates

class FileMainActivity : AppCompatActivity() {
    val selectedFile = ArrayList<File>()
    var maxSize: Int by Delegates.notNull()
    var isReplace: Boolean = false
    @SuppressLint("NewApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_file_main)
        maxSize = intent.getIntExtra(Extras.DATA, 9)
        isReplace = intent.getBooleanExtra(Extras.FLAG, false)
        back.setOnClickListener {
            finish()
        }
        val spinner = SpinnerWindow(this, AdapterView.OnItemClickListener { parent, view, position, id ->
            file_pager.currentItem = position
            directory_type.text = parent.adapter.getItem(position).toString()
        })
        directory_type.setOnClickListener {
            if (!spinner.isShowing) {
                spinner.showAsDropDown(directory_type)
            } else {
                spinner.dismiss()
            }
        }
//        directory_type.adapter = SpinnerAdapter(this, resources.getStringArray(R.array.spinner_select_file))
//        directory_type.dropDownVerticalOffset = 20
//        directory_type.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
//            override fun onNothingSelected(parent: AdapterView<*>?) {
//
//            }
//
//            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
//                file_pager.currentItem = position
//            }
//
//        }
        val fragments = listOf(CommonDocumentsFragment.newInstance(), AllFileFragment())
        file_pager.adapter = FilePageAdapter(supportFragmentManager, fragments)
        send_selected_files.setOnClickListener {
            val intent = Intent()
            val paths = ArrayList<String>()
            selectedFile.forEach {
                paths.add(it.path)
            }
            intent.putExtra(Extras.LIST, paths)
            setResult(Activity.RESULT_OK, intent)
            finish()
        }
    }

    fun showSelectedInfo() {
        var size: Long = 0
        selectedFile.forEach {
            size += it.length()
        }
        selected_files_size.text = "已选择 : ${FileUtil.formatFileSize(size, FileUtil.SizeUnit.Auto)}"
        send_selected_files.isEnabled = selectedFile.size > 0
        send_selected_files.text = "选择(${selectedFile.size}/$maxSize)"
    }

    fun sendChangeFile(file: File) {
        val intent = Intent()
        intent.putExtra(Extras.DATA, file.path)
        setResult(Activity.RESULT_OK, intent)
        finish()
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        supportFragmentManager.fragments.forEach {
            if (it is AllFileFragment) {
                return it.onKeyDown(keyCode, event)
            }
        }
        return super.onKeyDown(keyCode, event)
    }


    inner class FilePageAdapter(fm: FragmentManager, val fragments: List<Fragment>) : FragmentPagerAdapter(fm) {
        override fun getItem(position: Int): Fragment = fragments[position]

        override fun getCount(): Int = fragments.size

    }

    companion object {
        fun start(context: Activity, maxSize: Int? = 9, isReplace: Boolean? = false, requestCode: Int) {
            val intent = Intent(context, FileMainActivity::class.java)
            intent.putExtra(Extras.DATA, maxSize)
            intent.putExtra(Extras.FLAG, isReplace)
            context.startActivityForResult(intent, requestCode)
        }
    }
}
