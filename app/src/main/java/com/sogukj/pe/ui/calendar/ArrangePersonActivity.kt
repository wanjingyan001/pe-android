package com.sogukj.pe.ui.calendar

import android.app.Activity
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.framework.base.ToolbarActivity
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.bean.UserBean
import kotlinx.android.synthetic.main.activity_arrange_person.*

class ArrangePersonActivity : ToolbarActivity() {
    override val menuId: Int
        get() = R.menu.menu_confirm

    companion object {
        fun start(context: Activity, alreadyList: ArrayList<UserBean>? = null, requestCode: Int? = null) {
            val intent = Intent(context,ArrangePersonActivity::class.java)
            intent.putExtra(Extras.DATA, alreadyList)
            val code = requestCode ?: Extras.REQUESTCODE
            context.startActivityForResult(intent, code)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_arrange_person)
        title = "选择参加人"
        setBack(true)
        toContacts.setOnClickListener {

        }
    }
}
