package com.sogukj.pe.ui.htdata

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import com.framework.base.ToolbarActivity
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.bean.ProjectBean
import com.sogukj.pe.ui.project.StoreProjectMoreFragment

/**
 * Created by qinfei on 17/8/11.
 */
class ProjectBookMoreActivity : ToolbarActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_negative_news)
        val project = intent.getSerializableExtra(Extras.DATA) as ProjectBean
        val type = intent.getIntExtra(Extras.TYPE, 1)
        if (type == 1) setTitle("负面信息")
        else setTitle("企业舆情")
        setBack(true)
        supportFragmentManager
                .beginTransaction()
                .replace(R.id.container, StoreProjectMoreFragment.newInstance(project, type))
                .commit()
    }

    companion object {
        fun start(ctx: Activity?, project: ProjectBean, type: Int = 1) {
            val intent = Intent(ctx, ProjectBookMoreActivity::class.java)
            intent.putExtra(Extras.DATA, project)
            intent.putExtra(Extras.TYPE, type)
            ctx?.startActivity(intent)
        }
    }
}