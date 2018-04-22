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
import android.support.v4.view.ViewPager
import android.view.KeyEvent
import android.view.View
import android.widget.AdapterView
import android.widget.PopupWindow
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.Theme
import com.nbsp.materialfilepicker.ui.DirectoryFragment
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.ui.partyBuild.PartyUploadActivity
import com.sogukj.pe.util.FileUtil
import com.sogukj.pe.util.Utils
import kotlinx.android.synthetic.main.activity_file_main.*
import kotlinx.android.synthetic.main.fragment_weekly_wait_to_watch.*
import org.jetbrains.anko.toast
import java.io.File
import kotlin.properties.Delegates

class FileMainActivity : AppCompatActivity(), ViewPager.OnPageChangeListener {
    val selectedFile = ArrayList<File>()
    var maxSize: Int by Delegates.notNull()
    var isReplace: Boolean = false
    private val comDocFragment by lazy { CommonDocumentsFragment.newInstance() }
    private val allFileFragment by lazy { AllFileFragment() }
    @SuppressLint("NewApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_file_main)
        Utils.setWindowStatusBarColor(this, R.color.white)
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
        val fragments = listOf(comDocFragment, allFileFragment)
        file_pager.adapter = FilePageAdapter(supportFragmentManager, fragments)
        file_pager.addOnPageChangeListener(this)
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
        help.setOnClickListener {
            MaterialDialog.Builder(this)
                    .theme(Theme.LIGHT)
                    .content(R.string.file_help)
                    .positiveText("确定")
                    .onPositive { dialog, which ->
                        dialog.dismiss()
                    }
                    .show()
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
        val requestCode = intent.getIntExtra(Extras.ID, -1)
        val intent = Intent()
        if (requestCode == PartyUploadActivity.SELECTFILE) {
            intent.putExtra(Extras.DATA, file)
        } else {
            intent.putExtra(Extras.DATA, file.path)
        }
        setResult(Activity.RESULT_OK, intent)
        finish()
    }

    @SuppressLint("RestrictedApi")
    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        supportFragmentManager.fragments.forEach {
            if (it is AllFileFragment) {
                return it.onKeyDown(keyCode, event)
            }
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun onPageScrollStateChanged(state: Int) {
    }

    override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
    }

    override fun onPageSelected(position: Int) {
        when (position) {
            0 -> directory_type.text = "常用应用"
            1 -> directory_type.text = "内部存储"
        }
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
            intent.putExtra(Extras.ID, requestCode)
            context.startActivityForResult(intent, requestCode)
        }
    }
}
