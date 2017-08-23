package com.sogukj.pe.ui

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import com.framework.base.ToolbarActivity
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.bean.PatentBean
import com.sogukj.pe.bean.ProjectBean
import java.text.SimpleDateFormat

/**
 * Created by qinfei on 17/8/11.
 */
class PatentInfoActivity : ToolbarActivity() {
    lateinit var project: ProjectBean
    lateinit var data: PatentBean
    val fmt = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        project = intent.getSerializableExtra(Extras.DATA) as ProjectBean
        data = intent.getSerializableExtra(PatentBean::class.java.simpleName) as PatentBean
        setContentView(R.layout.activity_bond_info)
        setBack(true)
        setTitle("专利信息详情")
        data.apply {
        }
    }

    companion object {
        fun start(ctx: Activity?, project: ProjectBean, data: PatentBean) {
            val intent = Intent(ctx, PatentInfoActivity::class.java)
            intent.putExtra(Extras.DATA, project)
            intent.putExtra(PatentBean::class.java.simpleName, data)
            ctx?.startActivity(intent)
        }
    }
}