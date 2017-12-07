package com.sogukj.pe.ui.weekly

import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.framework.base.ToolbarActivity
import com.sogukj.pe.R

class PersonalWeeklyActivity : ToolbarActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_personal_weekly)
    }

    companion object {
        fun start(ctx: Context?) {
            val intent = Intent(ctx, PersonalWeeklyActivity::class.java)
            ctx?.startActivity(intent)
        }
    }
}
