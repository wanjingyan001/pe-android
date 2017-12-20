package com.sogukj.pe.ui.score

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.framework.base.ToolbarActivity
import com.sogukj.pe.R
import kotlinx.android.synthetic.main.activity_leader.*
import org.jetbrains.anko.textColor

class LeaderActivity : ToolbarActivity() {

    companion object {
        fun start(ctx: Context?) {
            val intent = Intent(ctx, LeaderActivity::class.java)
            ctx?.startActivity(intent)
        }
    }

    val TYPE_EMPLOYEE = 3
    val TYPE_MANAGE = 4

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_leader)

        setBack(true)
        setTitle("评分中心")
        toolbar?.setBackgroundColor(Color.WHITE)
        toolbar?.apply {
            val title = this.findViewById(R.id.toolbar_title) as TextView?
            title?.textColor = Color.parseColor("#282828")
            val back = this.findViewById(R.id.toolbar_back) as ImageView
            back?.visibility = View.VISIBLE
            back.setImageResource(R.drawable.grey_back)
        }

        gangwei_pingfenxiaozu.setOnClickListener {
            JudgeActivity.start(context, TYPE_MANAGE)
        }

        gangwei_huping.setOnClickListener {
            ScoreDetailActivity.start(context)
        }

        jixiao_pingfen.setOnClickListener {
            JudgeActivity.start(context, TYPE_MANAGE)
        }

        jixiao_kaohejieguo.setOnClickListener {
            JiXiaoActivity.start(context)
        }

        zonglan.setOnClickListener {
            ScoreListActivity.start(context)
        }
    }
}
