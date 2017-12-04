package com.sogukj.pe.ui.project

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import com.framework.base.ToolbarActivity
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.bean.ProjectBean
import com.sogukj.pe.util.Trace
import com.sogukj.service.SoguApi
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_manage_data.*
import java.net.UnknownHostException

class ManageDataActivity : ToolbarActivity() {

    lateinit var project: ProjectBean
    val gson = Gson()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manage_data)

        project = intent.getSerializableExtra(Extras.DATA) as ProjectBean
        title = project.name
        setBack(true)

        //
        project.company_id = 2

        project.company_id?.let {
            load(it)
        }

        btn_commit.setOnClickListener {
            upload()
        }
    }

    fun load(it: Int) {
        SoguApi.getService(application)
                .manageData(it)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ payload ->
                    if (payload.isOk) {
                        var data = payload.payload
                        data?.apply {
                            et_majorEvents.setText(majorEvents)
                            et_recentFinance.setText(recentFinance)
                            et_tendency.setText(tendency)
                            et_policyIssues.setText(policyIssues)
                            et_clause.setText(clause)
                            et_opinion.setText(opinion)
                            et_bonus.setText(bonus)
                        }
                    } else
                        showToast(payload.message)
                }, { e ->
                    Trace.e(e)
                    when (e) {
                        is JsonSyntaxException -> showToast("后台数据出错")
                        is UnknownHostException -> showToast("网络出错")
                        else -> showToast("未知错误")
                    }
                })
    }

    val paramMap = HashMap<String, Any>()

    fun prepareParams(it: Int) {
        paramMap.put("company_id", "$it")

        var manageDataBean = HashMap<String, String>()
        manageDataBean.put("majorEvents", et_majorEvents.text.toString())
        manageDataBean.put("recentFinance", et_recentFinance.text.toString())
        manageDataBean.put("tendency", et_tendency.text.toString())
        manageDataBean.put("policyIssues", et_policyIssues.text.toString())
        manageDataBean.put("clause", et_clause.text.toString())
        manageDataBean.put("opinion", et_opinion.text.toString())
        manageDataBean.put("bonus", et_bonus.text.toString())
        paramMap.put("ae", manageDataBean)
    }


    fun upload() {
        project.company_id?.let {
            prepareParams(it)
            SoguApi.getService(application)
                    .addEditManageData(paramMap)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeOn(Schedulers.io())
                    .subscribe({ payload ->
                        if (payload.isOk) {
                            finish()
                        } else
                            showToast(payload.message)
                    }, { e ->
                        Trace.e(e)
                        showToast("保存失败")
                    })
        }
    }

    companion object {
        fun start(ctx: Activity?, project: ProjectBean) {
            val intent = Intent(ctx, ManageDataActivity::class.java)
            intent.putExtra(Extras.DATA, project)
            ctx?.startActivity(intent)
        }
    }
}
