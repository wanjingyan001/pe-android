package com.sogukj.pe.ui.approve

import android.os.Bundle
import com.framework.base.ToolbarActivity
import com.sogukj.pe.R

class MyHolidayActivity : ToolbarActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_holiday)
        setBack(true)
        title = "我的假期"
    }
}
