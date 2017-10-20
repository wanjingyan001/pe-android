package com.sogukj.pe.ui.approve

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import com.framework.base.ToolbarActivity
import com.sogukj.pe.R
import com.sogukj.pe.ui.htdata.ProjectBookActivity

class ApproveListActivity : ToolbarActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_approve_list)
    }

    companion object {
        fun start(ctx: Activity?) {
            val intent = Intent(ctx, ProjectBookActivity::class.java)
            ctx?.startActivity(intent)
        }
    }
}
