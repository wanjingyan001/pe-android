package com.sogukj.pe.ui.user

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import com.framework.base.ToolbarActivity
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import kotlinx.android.synthetic.main.activity_form.*

class FormActivity : ToolbarActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_form)
        setBack(true)

        title = intent.getStringExtra(Extras.TITLE)
        var content = intent.getStringExtra(Extras.DATA)
        if (content.isNullOrEmpty()) {
            fill.setText("")
            fill.setSelection(0)
        } else {
            fill.setText(content)
            fill.setSelection(content.length)
        }

        toolbar_menu.setOnClickListener {
            if (fill.text.toString().trim().equals("")) {
                showCustomToast(R.drawable.icon_toast_common, "数据不能为空")
                return@setOnClickListener
            }
            var intent = Intent()
            intent.putExtra(Extras.DATA, fill.text.toString().trim())
            setResult(Activity.RESULT_OK, intent)
            finish()
        }
    }

    companion object {
        fun start(ctx: Activity?, title: String, content: String, code: Int) {
            val intent = Intent(ctx, FormActivity::class.java)
            intent.putExtra(Extras.TITLE, title)
            intent.putExtra(Extras.DATA, content)
            ctx?.startActivityForResult(intent, code)
        }
    }
}
