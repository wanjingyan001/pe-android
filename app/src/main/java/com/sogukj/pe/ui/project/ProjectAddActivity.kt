package com.sogukj.pe.ui.project

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import com.framework.base.ToolbarActivity
import com.sogukj.pe.Extras
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
        setBack(true)

        var type = intent.getStringExtra(Extras.TYPE)
        if (type == "ADD") {
            setTitle("申请新增项目数据")
        } else if (type == "EDIT") {
            setTitle("编辑调研项目")
            var data = intent.getSerializableExtra(Extras.DATA) as ProjectBean
            et_name.setText(data.name)
            et_short_name.setText(data.shortName)
            et_faren.setText(data.legalPersonName)
            et_reg_address.setText(data.regLocation)
            et_credit_code.setText(data.creditCode)
            et_other.setText(data.info)
        }
        btn_commit.setOnClickListener {
            if (type == "ADD") {
                doSave()
            } else if (type == "EDIT") {
                editSave()
            }
        }
    }

//    override val menuId: Int
//        get() = R.menu.user_edit
//
//    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
//        when (item?.itemId) {
//            R.id.action_save -> {
//                doSave();return true;
//            }
//        }
//        return false
//    }

    private fun doSave() {
        val project = ProjectBean()
        project.name = et_name?.text?.trim()?.toString()
        if (project.name == null) return
        project.shortName = et_short_name?.text?.trim()?.toString()
        project.legalPersonName = et_faren?.text?.trim()?.toString()
        project.regLocation = et_reg_address?.text?.trim()?.toString()
        project.creditCode = et_credit_code?.text?.trim()?.toString()
        project.info = et_other?.text?.trim()?.toString()
        SoguApi.getService(application)
                .addProject(name = project.name!!
                        , shortName = project.shortName!!
                        , creditCode = project.creditCode
                        , legalPersonName = project.legalPersonName
                        , regLocation = project.regLocation
                        , info = project.info
                        , type = 6)
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
                    hideProgress()
                    showToast("保存失败")
                }, {
                    hideProgress()
                }, {
                    showProgress("正在提交")
                })
    }

    private fun editSave() {
//        var data = intent.getSerializableExtra(Extras.DATA) as ProjectBean
//        if (et_name?.text?.trim()?.toString().isNullOrEmpty()) return
//        data.name = et_name?.text?.trim()?.toString()
//        data.shortName = et_short_name?.text?.trim()?.toString()
//        data.legalPersonName = et_faren?.text?.trim()?.toString()
//        data.regLocation = et_reg_address?.text?.trim()?.toString()
//        data.creditCode = et_credit_code?.text?.trim()?.toString()
//        data.info = et_other?.text?.trim()?.toString()
//
//        SoguApi.getService(application)
//                .addProject(name = project.name!!
//                        , shortName = project.shortName!!
//                        , creditCode = project.creditCode
//                        , legalPersonName = project.legalPersonName
//                        , regLocation = project.regLocation
//                        , info = project.info
//                        , type = 6)
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribeOn(Schedulers.io())
//                .subscribe({ payload ->
//                    if (payload.isOk) {
//                        showToast("保存成功")
//                        finish()
//                    } else {
//                        showToast(payload.message)
//                    }
//
//                }, { e ->
//                    Trace.e(e)
//                    hideProgress()
//                    showToast("保存失败")
//                }, {
//                    hideProgress()
//                }, {
//                    showProgress("正在提交")
//                })
    }

    companion object {
        fun startAdd(ctx: Activity?) {
            var intent = Intent(ctx, ProjectAddActivity::class.java)
            intent.putExtra(Extras.TYPE, "ADD")
            ctx?.startActivity(intent)
        }

        fun startEdit(ctx: Activity?, data: ProjectBean) {
            var intent = Intent(ctx, ProjectAddActivity::class.java)
            intent.putExtra(Extras.TYPE, "EDIT")
            intent.putExtra(Extras.DATA, data)
            ctx?.startActivity(intent)
        }
    }
}
