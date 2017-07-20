package com.sogukj.pe.ui

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.Toolbar
import com.framework.base.ToolbarActivity
import com.sogukj.pe.R

class UserActivity : ToolbarActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user)
    }
}
