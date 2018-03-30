package com.sogukj.pe.ui.project

import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.Toolbar
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.TextView
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.Theme
import com.framework.base.BaseActivity
import com.google.gson.Gson
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.bean.CreditInfo
import com.sogukj.pe.bean.CreditReqBean
import com.sogukj.pe.bean.ProjectBean
import com.sogukj.pe.bean.QueryReqBean
import com.sogukj.pe.util.Trace
import com.sogukj.pe.util.Utils
import com.sogukj.pe.view.RecyclerAdapter
import com.sogukj.pe.view.RecyclerHolder
import com.sogukj.service.SoguApi
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_shareholder_credit.*
import org.jetbrains.anko.backgroundColor
import org.jetbrains.anko.find
import org.jetbrains.anko.info
import org.jetbrains.anko.textColor

class ShareholderCreditActivity : BaseActivity(), View.OnClickListener {


    var toolbar: Toolbar? = null
    private lateinit var bean: ProjectBean
    private lateinit var directorsAdapter: RecyclerAdapter<CreditInfo.Item>
    private lateinit var shareholderAdapter: RecyclerAdapter<CreditInfo.Item>

    companion object {
        val TAG = ShareholderCreditActivity::class.java.simpleName
        fun start(ctx: Context?, project: ProjectBean) {
            val intent = Intent(ctx, ShareholderCreditActivity::class.java)
            intent.putExtra(Extras.DATA, project)
            ctx?.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_shareholder_credit)
        bean = intent.getSerializableExtra(Extras.DATA) as ProjectBean
        cardCompanyName.text = bean.name
        toolbar_title.text = "征信"
        initAdapter()
        back.setOnClickListener(this)
        addTv.setOnClickListener(this)
        inquireBtn.setOnClickListener(this)

    }

    private fun initAdapter() {
        directorsAdapter = RecyclerAdapter(this, { _adapter, parent, _ ->
            val convertView = _adapter.getView(R.layout.item_shareholder_credit, parent)
            ShareHolder(convertView)
        })
        directorsList.layoutManager = LinearLayoutManager(this)
        directorsList.adapter = directorsAdapter

        shareholderAdapter = RecyclerAdapter(this, { _adapter, parent, _ ->
            val convertView = _adapter.getView(R.layout.item_shareholder_credit, parent)
            ShareHolder(convertView)
        })
        shareholderList.layoutManager = LinearLayoutManager(this)
        shareholderList.adapter = shareholderAdapter

    }

    override fun onResume() {
        super.onResume()
        doRequest(bean.company_id)
    }


    fun doRequest(companyId: Int?) {
        if (companyId != null) {
            SoguApi.getService(application)
                    .showCreditInfo(companyId)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeOn(Schedulers.io())
                    .subscribe({ payload ->
                        Log.d(TAG, Gson().toJson(payload))
                        if (payload.isOk) {
                            payload.payload?.apply {
                                directorsAdapter.dataList.clear()
                                shareholderAdapter.dataList.clear()
                                this.item.forEach {
                                    when (it.type) {
                                        1 -> directorsAdapter.dataList.add(it)
                                        2 -> shareholderAdapter.dataList.add(it)
                                    }
                                }
                                when (this.click) {
                                    1 -> {
                                        inquireBtn.text = "一键查询"
                                        inquireBtn.isEnabled = true
                                    }
                                    2 -> {
                                        inquireBtn.text = "查询中，请稍后再看"
                                        inquireBtn.isEnabled = false
                                    }
                                    3 -> {
                                        inquireBtn.text = "提交查询"
                                        inquireBtn.isEnabled = false
                                    }
                                }
                            }

                        } else {
                            showToast(payload.message)
                        }
                    }, { e ->
                        Log.e(TAG, e.message)
                        Trace.e(e)
                    }, {
                        if (directorsAdapter.dataList.isEmpty()) {
                            empty1.visibility = View.VISIBLE
                        } else {
                            empty1.visibility = View.GONE
                            directorsAdapter.notifyDataSetChanged()
                        }
                        if (shareholderAdapter.dataList.isEmpty()) {
                            empty2.visibility = View.VISIBLE
                        } else {
                            empty2.visibility = View.GONE
                            shareholderAdapter.notifyDataSetChanged()
                        }
                        inquireBtn.isEnabled = !(directorsAdapter.dataList.isEmpty()
                                && shareholderAdapter.dataList.isEmpty())
                    })
        }
    }

    private fun doInquire(list: List<CreditReqBean>) {
        if (list.isNotEmpty()) {
            val info = QueryReqBean()
            info.info = list as ArrayList<CreditReqBean>
            info { Gson().toJson(info) }
            SoguApi.getService(application)
                    .queryCreditInfo(info)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeOn(Schedulers.io())
                    .subscribe({ payload ->
                        if (payload.isOk) {
                            inquireBtn.text = "查询中，请稍后再看"
                            inquireBtn.isEnabled = false
                        } else {
                            showToast(payload.message)
                        }
                    }, { e ->
                        Trace.e(e)
                    })

        }
    }

    private val queryDataList = ArrayList<CreditReqBean>()

    inner class ShareHolder(convertView: View) : RecyclerHolder<CreditInfo.Item>(convertView) {
        private val directorName = convertView.find<TextView>(R.id.directorName)
        private val directorPosition = convertView.find<TextView>(R.id.directorPosition)
        private val inquireStatus = convertView.find<TextView>(R.id.inquireStatus)
        private val phoneNumberEdt = convertView.find<EditText>(R.id.phoneNumberEdt)
        private val phoneNumberTv = convertView.find<TextView>(R.id.phoneNumberTv)
        private val IDCardEdt = convertView.find<EditText>(R.id.IDCardEdt)
        private val IDCardTv = convertView.find<TextView>(R.id.IDCardTv)
        private val sensitiveNews = convertView.find<TextView>(R.id.sensitiveNews)
        @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
        @SuppressLint("SetTextI18n", "ResourceType")
        override fun setData(view: View, data: CreditInfo.Item, position: Int) {
            phoneNumberEdt.visibility = View.GONE
            IDCardEdt.visibility = View.GONE
            phoneNumberEdt.filters = Utils.getFilter(context)
            IDCardEdt.filters = Utils.getFilter(context)
            directorName.text = data.name
            if (data.position.isEmpty()) {
                directorPosition.text = "股东"
            } else {
                directorPosition.text = data.position
            }
            if (data.idCard != null) {
                data.idCard?.let {
                    if (it.isEmpty()) {
                        IDCardTv.hint = "点击填写"
                    } else {
                        IDCardTv.text = it
                    }
                }
            }
            when (data.status) {
                0 -> {
                    inquireStatus.text = "信息待填写"
                    inquireStatus.textColor = Color.parseColor("#ffa715")
                }
                1 -> {
                    inquireStatus.text = "查询中"
                    inquireStatus.textColor = Color.parseColor("#608cf8")

                }
                2 -> {
                    inquireStatus.text = "查询完成"
                    inquireStatus.textColor = Color.parseColor("#50d59d")
                }
                3 -> {
                    inquireStatus.text = "查询失败"
                    inquireStatus.textColor = Color.parseColor("#f7b62b")
                }
            }

            if (data.phone != null) {
                data.phone?.let {
                    if (it.isEmpty()) {
                        phoneNumberTv.hint = "点击填写"
                    } else {
                        phoneNumberTv.text = it
                    }
                }
            }
            if (data.error_info != null && !data.error_info?.isEmpty()!!) {
                data.error_info?.let {
                    if (!it.isEmpty()) {
                        sensitiveNews.visibility = View.VISIBLE
                        sensitiveNews.text = data.error_info
                        sensitiveNews.textColor = Color.parseColor("#f7b62b")
                        sensitiveNews.background = resources.getDrawable(R.drawable.bg_shareholder_credit_item1)
                    }
                }
            } else {
                if (data.sum != null) {
                    data.sum.let {
                        if (it != 0) {
                            sensitiveNews.visibility = View.VISIBLE
                            sensitiveNews.text = "${it}条敏感信息>"
                            sensitiveNews.textColor = Color.parseColor("#ff3300")
                            sensitiveNews.backgroundColor = resources.getColor(R.color.shareholder_sensitive_bg)
                            sensitiveNews.setOnClickListener { SensitiveInfoActivity.start(this@ShareholderCreditActivity, data) }
                        } else {
                            sensitiveNews.visibility = View.VISIBLE
                            sensitiveNews.text = "无敏感信息"
                            sensitiveNews.textColor = Color.parseColor("#50d59d")
                            sensitiveNews.background = resources.getDrawable(R.drawable.bg_shareholder_credit_item2)
                            sensitiveNews.setOnClickListener(null)
                        }
                    }
                } else {
                    sensitiveNews.visibility = View.GONE
                }
            }
            phoneNumberTv.setOnClickListener {
                val phoneEdt = EditText(this@ShareholderCreditActivity)
                phoneEdt.background = null
                phoneEdt.setText(phoneNumberTv.text)
                val i = Utils.dpToPx(this@ShareholderCreditActivity, 24)
                phoneEdt.setPadding(i, 0, 0, i)
                phoneEdt.textColor = Color.parseColor("#282828")
                phoneEdt.setSelection(phoneNumberTv.text.length)
                phoneEdt.filters = Utils.getFilter(context)
                MaterialDialog.Builder(this@ShareholderCreditActivity)
                        .title("修改手机号")
                        .theme(Theme.LIGHT)
                        .customView(phoneEdt, false)
                        .onPositive { dialog, which ->
                            val phone = phoneEdt.text.toString()
                            if (Utils.isMobileExact(phone)) {
                                if (phone != phoneNumberTv.text) {
                                    inquireBtn.isEnabled = true
                                    inquireBtn.text = "一键查询"
                                    data.isChange = true
                                }
                                if (data.isChange) {
                                    inquireStatus.text = "待查询"
                                    inquireStatus.textColor = Color.parseColor("#ffa715")
                                    data.phone = phone
                                }
                                phoneNumberTv.text = phone
                                Utils.closeInput(this@ShareholderCreditActivity, phoneEdt)
                                dialog.dismiss()
                            } else {
                                phoneEdt.setText("")
                                showToast("请输入正确的手机号")
                            }
                        }
                        .positiveText("确定")
                        .negativeText("取消")
                        .show()
            }
            IDCardTv.setOnClickListener {
                val idEdt = EditText(this@ShareholderCreditActivity)
                idEdt.background = null
                idEdt.setText(IDCardTv.text)
                val i = Utils.dpToPx(this@ShareholderCreditActivity, 24)
                idEdt.setPadding(i, 0, 0, i)
                idEdt.textColor = Color.parseColor("#282828")
                idEdt.setSelection(IDCardTv.text.length)
                idEdt.filters = Utils.getFilter(context)
                MaterialDialog.Builder(this@ShareholderCreditActivity)
                        .title("修改身份证号")
                        .theme(Theme.LIGHT)
                        .customView(idEdt, false)
                        .onPositive { dialog, which ->
                            val idCard = idEdt.text.toString()
                            if (Utils.isIDCard18(idCard)) {
                                if (IDCardTv.text != idCard) {
                                    inquireBtn.isEnabled = true
                                    inquireBtn.text = "一键查询"
                                    data.isChange = true
                                }
                                if (data.isChange) {
                                    inquireStatus.text = "待查询"
                                    inquireStatus.textColor = Color.parseColor("#ffa715")
                                    data.idCard = idCard
                                }
                                Utils.closeInput(this@ShareholderCreditActivity, idEdt)
                                IDCardTv.text = idCard
                            } else {
                                idEdt.setText("")
                                showToast("请输入正确的身份证号")
                            }
                        }
                        .positiveText("确定")
                        .negativeText("取消")
                        .show()
            }
        }

        /**
         * 修改数据后,保存
         */
        private fun saveReqBean(data: CreditInfo.Item, inquireStatus: TextView) {
            phoneNumberEdt.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(s: Editable?) {
                    data.phone = s.toString()
                    s?.let {
                        if (!it.equals(data.phone)) {
                            inquireBtn.isEnabled = true
                            inquireBtn.text = "一键查询"
                            data.isChange = true
                        }
                        if (data.isChange) {
                            inquireStatus.text = "待查询"
                            inquireStatus.textColor = Color.parseColor("#ffa715")
                        }
                    }
                }

                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

                }

            })
            phoneNumberEdt.setOnFocusChangeListener { v, hasFocus ->
                val editText = v as EditText
                if (!hasFocus && editText.text.isNotEmpty() && !Utils.isMobileExact(editText.text)) {
                    editText.setText("")
                    showToast("请输入正确的手机号")
                }
            }
            IDCardEdt.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(s: Editable?) {
                    data.idCard = s.toString()
                    s?.let {
                        if (it.isNotEmpty()) {
                            inquireBtn.isEnabled = true

                        }
                        data.isChange = it.isNotEmpty()
                        if (data.isChange) {
                            inquireStatus.text = "待查询"
                            inquireStatus.textColor = Color.parseColor("#ffa715")
                        }
                    }
                }

                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

                }

            })
            IDCardEdt.setOnFocusChangeListener { v, hasFocus ->
                val editText = v as EditText
                if (!hasFocus && editText.text.isNotEmpty() && !Utils.isIDCard18(editText.text)) {
                    editText.setText("")
                    showToast("请输入正确的身份证号")
                }
            }
//            if (reqBean.phone != null && reqBean.idCard != null) {
//                if (reqBean.phone!!.isNotEmpty() || reqBean.idCard!!.isNotEmpty()) {
//                    if (data.isChange) {
//                        queryDataList.add(reqBean)
//                    }
//                }
//            }
        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.back -> finish()
            R.id.addTv -> AddCreditActivity.startForResult(this, bean.company_id)
            R.id.inquireBtn -> {
                queryDataList.clear()
                directorsAdapter.dataList.forEach {
                    if (it.isChange) {
                        val reqBean = CreditReqBean()
                        reqBean.id = it.id
                        reqBean.company_id = it.company_id
                        reqBean.name = it.name
                        reqBean.position = it.position
                        reqBean.type = it.type
                        reqBean.phone = it.phone
                        reqBean.idCard = it.idCard
                        queryDataList.add(reqBean)
                    }
                }
                shareholderAdapter.dataList.forEach {
                    if (it.isChange) {
                        val reqBean = CreditReqBean()
                        reqBean.id = it.id
                        reqBean.company_id = it.company_id
                        reqBean.name = it.name
                        reqBean.position = "股东"
                        reqBean.type = it.type
                        reqBean.phone = it.phone
                        reqBean.idCard = it.idCard
                        queryDataList.add(reqBean)
                    }
                }
                doInquire(queryDataList)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == Extras.REQUESTCODE && resultCode == Extras.RESULTCODE && data != null) {
            val reqBean = data.getSerializableExtra(Extras.DATA) as QueryReqBean
//            if (reqBean.info.isNotEmpty()) {
//                doInquire(reqBean.info)
//            }
        }
    }
}
