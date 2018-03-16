package com.sogukj.pe.ui.approve

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import com.framework.base.ToolbarActivity
import com.sogukj.pe.Extras
import com.sogukj.pe.R

class MultiCityActivity : ToolbarActivity() {

    companion object {
        fun start(ctx: Activity, edit: Boolean, id: Int) {
            var intent = Intent(ctx, MultiCityActivity::class.java)
            intent.putExtra(Extras.FLAG, edit)
            intent.putExtra(Extras.ID, id)
            ctx.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_multi_city)
        setBack(true)
        title = "目的城市"
    }
}
