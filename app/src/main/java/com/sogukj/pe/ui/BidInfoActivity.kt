package com.sogukj.pe.ui

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import com.framework.base.ToolbarActivity
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.bean.BidBean
import com.sogukj.pe.bean.ProjectBean
import java.text.SimpleDateFormat

/**
 * Created by qinfei on 17/8/11.
 */
class BidInfoActivity : ToolbarActivity() {
    lateinit var project: ProjectBean
    lateinit var data: BidBean
    val fmt = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        project = intent.getSerializableExtra(Extras.DATA) as ProjectBean
        data = intent.getSerializableExtra(BidBean::class.java.simpleName) as BidBean
        setContentView(R.layout.activity_bid_info)
        setBack(true)
        setTitle("招投标")
        data.apply {

        }
    }

    companion object {
        fun start(ctx: Activity?, project: ProjectBean, data: BidBean) {
            val intent = Intent(ctx, BidInfoActivity::class.java)
            intent.putExtra(Extras.DATA, project)
            intent.putExtra(BidBean::class.java.simpleName, data)
            ctx?.startActivity(intent)
        }
    }
}