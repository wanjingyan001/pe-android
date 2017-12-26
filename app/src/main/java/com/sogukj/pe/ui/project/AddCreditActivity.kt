package com.sogukj.pe.ui.project

import android.annotation.TargetApi
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.Gravity
import android.view.View
import android.widget.EditText
import android.widget.TextView
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.Theme
import com.framework.base.BaseActivity
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.bean.CreditReqBean
import com.sogukj.pe.bean.QueryReqBean
import com.sogukj.pe.util.Utils
import com.sogukj.pe.view.IOSPopwindow
import com.sogukj.pe.view.RecyclerAdapter
import com.sogukj.pe.view.RecyclerHolder
import kotlinx.android.synthetic.main.activity_add_credit.*
import kotlinx.android.synthetic.main.layout_shareholder_toolbar.*
import org.jetbrains.anko.find
import kotlin.properties.Delegates


class AddCreditActivity : BaseActivity(), View.OnClickListener {
    private lateinit var popwin: IOSPopwindow
    lateinit var adapter: RecyclerAdapter<CreditReqBean>
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
        addTv.text = "完成"
        popwin = IOSPopwindow(this)
        initList()
        back.setOnClickListener(this)
        addTv.setOnClickListener(this)
        typeSelect.setOnClickListener(this)
        save.setOnClickListener(this)
        phoneEdt.setOnFocusChangeListener { v, hasFocus ->
            val editText = v as EditText
            if (!hasFocus && editText.text.isNotEmpty() && !Utils.isMobileExact(editText.text)) {
                editText.setText("")
                showToast("请输入正确的手机号")
            }
        }
        IDCardEdt.setOnFocusChangeListener { v, hasFocus ->
            val editText = v as EditText
            if (!hasFocus && editText.text.isNotEmpty() && !Utils.isIDCard18(editText.text)) {
                editText.setText("")
                showToast("请输入正确的身份证号")
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

    private fun initList() {
        adapter = RecyclerAdapter(this, { _adapter, parent, type ->
            val convertView = _adapter.getView(R.layout.item_shareholder_credit, parent)
            object : RecyclerHolder<CreditReqBean>(convertView) {
                private val directorName = convertView.find<TextView>(R.id.directorName)
                private val directorPosition = convertView.find<TextView>(R.id.directorPosition)
                private val phoneNumberEdt = convertView.find<EditText>(R.id.phoneNumberEdt)
                private val IDCardEdt = convertView.find<EditText>(R.id.IDCardEdt)
                override fun setData(view: View, data: CreditReqBean, position: Int) {
                    convertView.find<TextView>(R.id.inquireStatus).visibility = View.GONE
                    convertView.find<TextView>(R.id.sensitiveNews).visibility = View.GONE
                    directorName.text = data.name
                    if (data.type == 1) {
                        directorPosition.text = data.position
                    } else {
                        directorPosition.text = "股东"
                    }
                    phoneNumberEdt.setText(data.phone)
                    phoneNumberEdt.isEnabled = false
                    IDCardEdt.setText(data.idCard)
                    IDCardEdt.isEnabled = false
                }
            }
        })
        adapter.onItemLongClick = { v, position ->
            MaterialDialog.Builder(this@AddCreditActivity)
                    .theme(Theme.LIGHT)
                    .title("提示")
                    .content("确定要删除这条数据?")
                    .onPositive { materialDialog, dialogAction ->
                        adapter.dataList.removeAt(position)
                        adapter.notifyItemChanged(position)
                    }
                    .positiveText("确定")
                    .negativeText("取消")
                    .show()
            true
        }
        saveMsgList.layoutManager = LinearLayoutManager(this)
        saveMsgList.adapter = adapter
    }

    private fun saveReqBean(): CreditReqBean? {
        if (nameEdt.text.isEmpty() || "点击填写" == nameEdt.text.toString()) {
            showToast("请填写名字")
            return null
        }
        if (selectType == 1 && postEdt.text.toString().isEmpty()) {
            showToast("请填写职位")
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

    @TargetApi(Build.VERSION_CODES.KITKAT)
    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.back -> finish()
            R.id.addTv -> {
                if (adapter.dataList.isNotEmpty()) {
                    val query = QueryReqBean()
                    query.info = adapter.dataList as ArrayList<CreditReqBean>
                    val intent = Intent()
                    intent.putExtra(Extras.DATA, query)
                    setResult(Extras.RESULTCODE, intent)
                    finish()
                } else {
                    finish()
                }
            }
            R.id.typeSelect -> {
                Utils.closeInput(this, IDCardEdt)
                popwin.showAtLocation(find(R.id.add_layout),  Gravity.BOTTOM, 0,0)
            }
            R.id.save -> {
                val bean = saveReqBean() ?: return
                bean.let {
                    adapter.dataList.add(it)
                    adapter.notifyDataSetChanged()
                }
                nameEdt.setText("")
                phoneEdt.setText("")
                IDCardEdt.setText("")
                postEdt.setText("")
            }
        }
    }

}
