package com.sogukj.pe.ui

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import com.framework.base.ToolbarActivity
import com.sogukj.pe.R
import kotlinx.android.synthetic.main.activity_project.*

/**
 * Created by qinfei on 17/7/18.
 */
class ProjectActivity : ToolbarActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_project)
        setBack(true)
        tv_more.setOnClickListener {
            NegativeNewsActivity.start(this)
        }
    }

    companion object {
        fun start(ctx: Activity?) {
            ctx?.startActivity(Intent(ctx, ProjectActivity::class.java))
        }
    }
}
