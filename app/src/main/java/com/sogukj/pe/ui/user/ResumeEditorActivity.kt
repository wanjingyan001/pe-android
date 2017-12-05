package com.sogukj.pe.ui.user

import android.app.Activity
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.framework.base.BaseActivity
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.bean.EducationBean
import com.sogukj.pe.bean.WorkEducationBean
import com.sogukj.pe.util.Utils
import com.sogukj.pe.view.RecyclerAdapter
import kotlinx.android.synthetic.main.layout_shareholder_toolbar.*
import okhttp3.internal.Internal
import kotlin.properties.Delegates

class ResumeEditorActivity : BaseActivity() {
    lateinit var eduadapter: RecyclerAdapter<EducationBean>
    lateinit var workAdapter: RecyclerAdapter<WorkEducationBean>
    private var intExtra: Int by Delegates.notNull()

    companion object {
        val EDU = 1
        val WORK = 2
        fun start(ctx: Activity?, type: Int, data: ArrayList<EducationBean>) {
            val intent = Intent(ctx, ResumeEditorActivity::class.java)
            intent.putExtra(Extras.TYPE, type)
            intent.putExtra(Extras.LIST, data)
            ctx?.startActivity(intent)
        }

        fun start2(ctx: Activity?, type: Int, data: ArrayList<WorkEducationBean>) {
            val intent = Intent(ctx, ResumeEditorActivity::class.java)
            intent.putExtra(Extras.TYPE, type)
            intent.putExtra(Extras.LIST, data)
            ctx?.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_resume_editor)
        Utils.setWindowStatusBarColor(this, R.color.white)
        toolbar_title.text = "个人简历"
        addTv.text = "编辑"
        intExtra = intent.getIntExtra(Extras.TYPE, -1)
        when (intExtra) {
            EDU -> {
                var list = intent.getParcelableArrayExtra(Extras.LIST) as ArrayList<EducationBean>


            }
            WORK -> {
                var list = intent.getParcelableArrayExtra(Extras.LIST) as ArrayList<WorkEducationBean>
            }
        }


    }


}
