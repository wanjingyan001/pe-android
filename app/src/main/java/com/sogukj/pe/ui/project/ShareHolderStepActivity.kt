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
import com.framework.base.ToolbarActivity
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.bean.CustomSealBean
import com.sogukj.pe.ui.approve.ListSelectorActivity
import com.sogukj.pe.util.Utils
import com.sogukj.pe.view.IOSPopwindow
import kotlinx.android.synthetic.main.activity_share_holder_step.*
import org.jetbrains.anko.backgroundResource
import org.jetbrains.anko.find

class ShareHolderStepActivity : ToolbarActivity(), View.OnClickListener {

    companion object {

        fun start(ctx: Context?, step: Int) {
            val intent = Intent(ctx, ShareHolderStepActivity::class.java)
            intent.putExtra(Extras.DATA, step)
            ctx?.startActivity(intent)
        }
    }

    private var selectType = 0
    private lateinit var popwin: IOSPopwindow
    var step = 0
    var selectId = 0

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

        popwin = IOSPopwindow(this)
        typeSelect.setOnClickListener(this)
        companySelect.setOnClickListener(this)
        enter.setOnClickListener(this)
        phoneEdt.setOnFocusChangeListener { v, hasFocus ->
            val editText = v as EditText
            if (!hasFocus && editText.text.isNotEmpty() && !Utils.isMobileExact(editText.text)) {
                editText.setText("")
                showCustomToast(R.drawable.icon_toast_common, "请输入正确的手机号")
            }
        }
        IDCardEdt.setOnFocusChangeListener { v, hasFocus ->
            val editText = v as EditText
            if (!hasFocus && editText.text.isNotEmpty() && !Utils.isIDCard18(editText.text)) {
                editText.setText("")
                showCustomToast(R.drawable.icon_toast_common, "请输入正确的身份证号")
            }
        }
        popwin.setOnItemClickListener { v, select ->
            if (select == 1) {
                typeSelectTv.text = "董监高"
            } else {
                typeSelectTv.text = "股东"
            }
            selectType = select
        }
    }

    private fun prepare(): Boolean {
        if (nameEdt.text.isEmpty()) {
            showCustomToast(R.drawable.icon_toast_common, "请填写名字")
            return false
        }
        if (IDCardEdt.text.toString().isEmpty()) {
            showCustomToast(R.drawable.icon_toast_common, "请填写身份证")
            return false
        }
        return true
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.enter -> {

            }
            R.id.companySelect -> {
                var map = CustomSealBean.ValueBean()
                map.type = 2
                var bean = CustomSealBean()
                bean.name = "公司名称"
                bean.value_map = map
                ListSelectorActivity.start(this, bean)
            }
            R.id.typeSelect -> {
                Utils.closeInput(this, IDCardEdt)
                popwin.showAtLocation(find(R.id.enter), Gravity.BOTTOM, 0, 0)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == ListSelectorActivity.REQ_LIST_SELECTOR && resultCode === Activity.RESULT_OK) {
            val valueBean = data!!.getSerializableExtra(Extras.DATA2) as CustomSealBean.ValueBean
            companyName.text = valueBean.name
            selectId = valueBean.id!!
        }
    }
}
