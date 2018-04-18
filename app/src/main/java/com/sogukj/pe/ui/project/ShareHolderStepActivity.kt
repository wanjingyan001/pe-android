package com.sogukj.pe.ui.project

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import com.framework.base.ToolbarActivity
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import kotlinx.android.synthetic.main.activity_share_holder_step.*
import org.jetbrains.anko.backgroundResource

class ShareHolderStepActivity : ToolbarActivity() {

    companion object {

        fun start(ctx: Context?, step: Int) {
            val intent = Intent(ctx, ShareHolderStepActivity::class.java)
            intent.putExtra(Extras.DATA, step)
            ctx?.startActivity(intent)
        }
    }

    var step = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_share_holder_step)
        step = intent.getIntExtra(Extras.DATA, 0)
        setBack(true)
        if (step == 1) {
            title = "关联公司"
            step_icon.backgroundResource = R.drawable.step1
            step_title.text = "选择关联公司"
            step_subtitle.text = "可将征信记录自动归类至项目中"

            enter.text = "下一步"
            step_layout_1.visibility = View.VISIBLE
            step_layout_2.visibility = View.GONE
        } else if (step == 2) {
            title = "添加人员"
            step_icon.backgroundResource = R.drawable.step2
            step_title.text = "填写查询基本信息"
            step_subtitle.text = "选填信息可增加查询信息准确度"

            enter.text = "开始查询"
            step_layout_1.visibility = View.GONE
            step_layout_2.visibility = View.VISIBLE
        }
    }
}
