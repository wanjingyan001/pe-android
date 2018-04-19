package com.sogukj.pe.ui.project

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.AppBarLayout
import com.framework.base.BaseActivity
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.bean.ProjectBean
import com.sogukj.pe.util.Utils
import kotlinx.android.synthetic.main.activity_share_holder_desc.*

//股东征信介绍
class ShareHolderDescActivity : BaseActivity() {

    companion object {

        fun start(ctx: Context?, project: ProjectBean, tag: String) {//INNER   OUTER
            val intent = Intent(ctx, ShareHolderDescActivity::class.java)
            intent.putExtra(Extras.DATA, project)
            intent.putExtra(Extras.TYPE, tag)
            ctx?.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_share_holder_desc)

        back.setOnClickListener {
            finish()
        }

        start.setOnClickListener {
            var bean = intent.getSerializableExtra(Extras.DATA) as ProjectBean
            var type = intent.getStringExtra(Extras.TYPE)
            if (type.equals("INNER")) {
                ShareholderCreditActivity.start(this@ShareHolderDescActivity, bean)//高管征信（股东征信）
            } else if (type.equals("OUTER")) {//此时bean是空的，不是null
                ShareHolderStepActivity.start(context, 1)
            }
            finish()
        }

        AppBarLayout.addOnOffsetChangedListener(object : AppBarLayout.OnOffsetChangedListener {
            override fun onOffsetChanged(appBarLayout: AppBarLayout?, verticalOffset: Int) {
                var alpha = Math.abs(verticalOffset) * 1.0 / Utils.dpToPx(context, 60)
                down.alpha = 1 - alpha.toFloat()
            }
        })
    }
}
