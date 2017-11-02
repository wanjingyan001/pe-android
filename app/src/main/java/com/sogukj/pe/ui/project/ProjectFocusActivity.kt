package com.sogukj.pe.ui.project

import android.app.Activity
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.framework.base.ToolbarActivity
import com.sogukj.pe.R

class ProjectFocusActivity : ToolbarActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_project_focus)
        setBack(true)
        title = "关注"
        supportFragmentManager
                .beginTransaction()
                .replace(R.id.fl_container, ProjectListFragment.newInstance(ProjectListFragment.TYPE_GZ))
                .commit()
    }

    companion object {
        fun start(ctx: Activity?) {
            ctx?.startActivity(Intent(ctx, ProjectFocusActivity::class.java))
        }
    }
}
