package com.sogukj.pe.ui.project

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import com.framework.base.ToolbarActivity
import com.sogukj.pe.R
import com.sogukj.pe.bean.ProjectBean
import com.sogukj.pe.util.Trace
import com.sogukj.service.SoguApi
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_add_project.*
/**
 * Created by qinfei on 17/7/18.
 */
class ProjectAddActivity : ToolbarActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_project)
        setTitle("申请新增项目数据")
        setBack(true)
    }

    override val menuId: Int
        get() = R.menu.user_edit

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.action_save -> {
                doSave();return true;
            }
        }
        return false
    }

    private fun doSave() {
        val project = ProjectBean()
        project.name = et_name?.text?.trim()?.toString()
        if (project.name == null) return
        project.legalPersonName = et_faren?.text?.trim()?.toString()
        project.regLocation = et_reg_address?.text?.trim()?.toString()
        project.creditCode = et_credit_code?.text?.trim()?.toString()
        project.info = et_other?.text?.trim()?.toString()
        SoguApi.getService(application)
                .addProject(name = project.name!!
                        , creditCode = project.creditCode
                        , legalPersonName = project.legalPersonName
                        , regLocation = project.regLocation
                        , info = project.info)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ payload ->
                    if (payload.isOk) {
                        showToast("保存成功")
                        finish()
                    } else {
                        showToast(payload.message)
                    }

                }, { e ->
                    Trace.e(e)
                    showToast("保存失败")
                })
    }

    companion object {
        fun start(ctx: Activity?) {
            ctx?.startActivity(Intent(ctx, ProjectAddActivity::class.java))
        }
    }
}
