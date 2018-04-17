package com.sogukj.pe.ui.project

import android.annotation.TargetApi
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.EditText
import com.framework.base.BaseActivity
import com.google.gson.Gson
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.bean.CreditReqBean
import com.sogukj.pe.bean.QueryReqBean
import com.sogukj.pe.util.Trace
import com.sogukj.pe.util.Utils
import com.sogukj.pe.view.IOSPopwindow
import com.sogukj.service.SoguApi
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_add_credit.*
import kotlinx.android.synthetic.main.toolbar.*
import org.jetbrains.anko.find
import org.jetbrains.anko.info
import kotlin.properties.Delegates


class AddCreditActivity : BaseActivity(), View.OnClickListener {
    private lateinit var popwin: IOSPopwindow
    private var selectType = 1
    var id: Int by Delegates.notNull()


    companion object {
        fun start(ctx: Context?, id: Int?) {
            val intent = Intent(ctx, AddCreditActivity::class.java)
            intent.putExtra(Extras.ID, id)
            ctx?.startActivity(intent)
        }

        fun startForResult(ctx: Activity?, id: Int?) {
            val intent = Intent(ctx, AddCreditActivity::class.java)
            intent.putExtra(Extras.ID, id)
            ctx?.startActivityForResult(intent, Extras.REQUESTCODE)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_credit)
        Utils.setWindowStatusBarColor(this, R.color.white)
        id = intent.getIntExtra(Extras.ID, -1)
        toolbar_title.text = "添加人员"
        popwin = IOSPopwindow(this)
        toolbar_back.setOnClickListener(this)
        typeSelect.setOnClickListener(this)
        save.setOnClickListener(this)
        phoneEdt.setOnFocusChangeListener { v, hasFocus ->
            val editText = v as EditText
            if (!hasFocus && editText.text.isNotEmpty() && !Utils.isMobileExact(editText.text)) {
                editText.setText("")
                //showToast("请输入正确的手机号")
                showCustomToast(R.drawable.icon_toast_common, "请输入正确的手机号")
            }
        }
        IDCardEdt.setOnFocusChangeListener { v, hasFocus ->
            val editText = v as EditText
            if (!hasFocus && editText.text.isNotEmpty() && !Utils.isIDCard18(editText.text)) {
                editText.setText("")
                //showToast("请输入正确的身份证号")
                showCustomToast(R.drawable.icon_toast_common, "请输入正确的身份证号")
            }
        }
        popwin.setOnItemClickListener { v, select ->
            if (select == 1) {
                typeSelectTv.text = "董监高"
                postLayout.visibility = View.VISIBLE
            } else {
                typeSelectTv.text = "股东"
                postLayout.visibility = View.GONE
            }
            selectType = select
        }
    }

    private fun saveReqBean(): CreditReqBean? {
        if (nameEdt.text.isEmpty() || "点击填写" == nameEdt.text.toString()) {
            //showToast("请填写名字")
            showCustomToast(R.drawable.icon_toast_common, "请填写名字")
            return null
        }
        if (selectType == 1 && postEdt.text.toString().isEmpty()) {
            //showToast("请填写职位")
            showCustomToast(R.drawable.icon_toast_common, "请填写职位")
            return null
        }

        val creditReq = CreditReqBean()
        creditReq.company_id = id
        creditReq.name = nameEdt.text.toString()
        creditReq.phone = phoneEdt.text.toString()
        creditReq.idCard = IDCardEdt.text.toString()
        creditReq.type = selectType
        if (selectType == 1) {
            creditReq.position = postEdt.text.toString()
        }
        return creditReq
    }

    private fun doInquire(list: List<CreditReqBean>) {
        if (list.isNotEmpty()) {
            val info = QueryReqBean()
            info.info = list as ArrayList<CreditReqBean>
            info { "RequestBean==>${Gson().toJson(info)}" }
            SoguApi.getService(application)
                    .queryCreditInfo(info)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeOn(Schedulers.io())
                    .subscribe({ payload ->
                        if (payload.isOk) {
                            val query = QueryReqBean()
                            //query.info = adapter.dataList as ArrayList<CreditReqBean>
                            val intent = Intent()
                            intent.putExtra(Extras.DATA, query)
                            setResult(Extras.RESULTCODE, intent)
                            finish()
                        } else {
                            //showToast(payload.message)
                            showCustomToast(R.drawable.icon_toast_fail, payload.message)
                        }
                    }, { e ->
                        Trace.e(e)
                        hideProgress()
                    },{
                        hideProgress()
                    },{
                        showProgress("正在提交")
                    })

        }
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.toolbar_back -> finish()
//            R.id.addTv -> {
//                if (adapter.dataList.isNotEmpty()) {
//                    doInquire(adapter.dataList)
//                } else {
//                    finish()
//                }
//            }
            R.id.typeSelect -> {
                Utils.closeInput(this, IDCardEdt)
                popwin.showAtLocation(find(R.id.add_layout),  Gravity.BOTTOM, 0,0)
            }
        }
    }

}
