package com.sogukj.pe.ui.approve

import android.os.Bundle
import com.framework.base.ToolbarActivity
import com.sogukj.pe.R

class BusinessTripDetailActivity : ToolbarActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_business_trip_detail)
        setBack(true)
        title = "出差明细"
    }
}
