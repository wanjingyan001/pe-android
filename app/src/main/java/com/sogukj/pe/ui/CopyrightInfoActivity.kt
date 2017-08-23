package com.sogukj.pe.ui

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.Html
import com.framework.base.ToolbarActivity
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.bean.CopyRightBean
import com.sogukj.pe.bean.ProjectBean
import kotlinx.android.synthetic.main.activity_patent_info.*
import java.text.SimpleDateFormat

/**
 * Created by qinfei on 17/8/11.
 */
class CopyrightInfoActivity : ToolbarActivity() {
    lateinit var project: ProjectBean
    lateinit var data: CopyRightBean
    val fmt = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        project = intent.getSerializableExtra(Extras.DATA) as ProjectBean
        data = intent.getSerializableExtra(CopyRightBean::class.java.simpleName) as CopyRightBean
        val type = intent.getIntExtra(Extras.TYPE, 1)
        setContentView(R.layout.activity_copyright_1)
        setBack(true)
        if (type == 1) {
            setTitle("软著权详情")
            data.apply {
                tv_title.text = fullname
                val buff = StringBuffer()
                appendLine(buff, "简称", simplename)
                appendLine(buff, "登记号", regnum)
                appendLine(buff, "分类号", catnum)
                buff.append("\n")
                appendLine(buff, "版本号", version)
                buff.append("\n")
                appendLine(buff, "著作权人", authorNationality)
                buff.append("\n")
                appendLine(buff, "首次发表日期", publishtime)
                appendLine(buff, "登记日期", regtime)
                tv_content.text = buff.toString()
            }
        } else {
            setTitle("著作权详情")
            data.apply {
                tv_title.text = simplename
                val buff = StringBuffer()
                appendLine(buff, "类别", category)
                appendLine(buff, "著作权人", authorNationality)
                buff.append("\n")
                appendLine(buff, "登记号", regnum)
                appendLine(buff, "完成日期", finishTime)
                appendLine(buff, "首次发表日期", publishtime)
                appendLine(buff, "登记日期", regtime)
                buff.append("\n")
                tv_content.text = buff.toString()
            }
        }
    }

    fun appendLine(buff: StringBuffer, key: String, value: String? = "") {
        val hval = Html.fromHtml("<span style='color:#666;'>${value}</span>")
        val hkey = Html.fromHtml("<span style='color:#000;'>${key}</span>")
        buff.append("$hkey:  $hval\n")
    }

    companion object {
        fun start(ctx: Activity?, project: ProjectBean, data: CopyRightBean, type: Int = 1) {
            val intent = Intent(ctx, CopyrightInfoActivity::class.java)
            intent.putExtra(Extras.TYPE, type)
            intent.putExtra(Extras.DATA, project)
            intent.putExtra(CopyRightBean::class.java.simpleName, data)
            ctx?.startActivity(intent)
        }
    }
}