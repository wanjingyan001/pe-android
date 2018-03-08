package com.sogukj.pe.ui.approve

import android.app.Activity
import android.os.Bundle
import com.framework.base.ToolbarActivity
import com.sogukj.pe.R

class LeaveBusinessActivity : ToolbarActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_leave_business)
        setBack(true)
    }

    companion object {
        fun start(ctx: Activity) {

        }
    }
}
