package com.sogukj.pe.ui

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import com.framework.base.ToolbarActivity
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.bean.ProjectBean

class NegativeNewsActivity : ToolbarActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_negative_news)
        setTitle("负面信息")
        setBack(true)
        val project = intent.getSerializableExtra(Extras.DATA) as ProjectBean
        supportFragmentManager
                .beginTransaction()
                .replace(R.id.container, ProjectNewsFragment.newInstance(project))
                .commit()
    }

    companion object {
        fun start(ctx: Activity?) {
            ctx?.startActivity(Intent(ctx, NegativeNewsActivity::class.java))
        }
    }
}
