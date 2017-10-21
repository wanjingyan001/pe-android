package com.sogukj.pe.ui.approve

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import com.framework.base.ToolbarActivity
import com.google.gson.Gson
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.bean.CustomSealBean
import com.sogukj.pe.bean.SpGroupItemBean
import com.sogukj.pe.service.Payload
import com.sogukj.pe.util.Trace
import com.sogukj.service.SoguApi
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import org.json.JSONObject

class SealActivity : ToolbarActivity() {

    val gson = Gson()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_seal)
        val spGroudItemBean = intent.getSerializableExtra(Extras.DATA) as SpGroupItemBean
        setBack(true)
        setTitle(spGroudItemBean.name)
        SoguApi.getService(application)
                .approveInfo(template_id = spGroudItemBean.id!!)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ payload ->
                    if (!payload.isOk) {
                        showToast(payload.message)
                        return@subscribe
                    }
                    payload.payload?.forEach { bean ->
                        addRow(bean)
                    }

//                    val strJson = body?.string()
//                    val payload=gson.fromJson<Payload<Object>>(strJson, Payload::class.java)
//                    if (!payload.isOk){
//                        showToast(payload.message)
//                        return@subscribe
//                    }
//                    val jobj=JSONObject(strJson)
                }, { e ->
                    Trace.e(e)
                    showToast("暂无可用数据")
                })
    }

    private fun addRow(bean: CustomSealBean) {
        when (bean.control) {
            3 -> add3(bean)
            6-> add6(bean)
        }
    }

    private fun add6(bean: CustomSealBean) {

    }

    private fun add3(bean: CustomSealBean) {

    }

    companion object {
        fun start(ctx: Activity?, itemBean: SpGroupItemBean) {
            val intent = Intent(ctx, SealActivity::class.java)
            intent.putExtra(Extras.DATA, itemBean)
            ctx?.startActivity(intent)
        }
    }
}
