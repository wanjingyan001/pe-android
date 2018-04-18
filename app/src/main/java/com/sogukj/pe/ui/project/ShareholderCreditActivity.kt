package com.sogukj.pe.ui.project

import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.support.design.widget.AppBarLayout
import android.support.v4.content.ContextCompat
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.Toolbar
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.Theme
import com.framework.base.BaseActivity
import com.google.gson.Gson
import com.lcodecore.tkrefreshlayout.RefreshListenerAdapter
import com.lcodecore.tkrefreshlayout.TwinklingRefreshLayout
import com.lcodecore.tkrefreshlayout.footer.BallPulseView
import com.lcodecore.tkrefreshlayout.header.progresslayout.ProgressLayout
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.bean.CreditInfo
import com.sogukj.pe.bean.CreditReqBean
import com.sogukj.pe.bean.ProjectBean
import com.sogukj.pe.bean.QueryReqBean
import com.sogukj.pe.ui.SupportEmptyView
import com.sogukj.pe.util.Trace
import com.sogukj.pe.util.Utils
import com.sogukj.pe.view.RecyclerAdapter
import com.sogukj.pe.view.RecyclerHolder
import com.sogukj.pe.view.SpaceItemDecoration
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
    private lateinit var mAdapter: RecyclerAdapter<CreditInfo.Item>

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
        toolbar_title.text = "高管征信"
        initAdapter()
        back.setOnClickListener(this)
        inquireBtn.setOnClickListener(this)

        AppBarLayout.addOnOffsetChangedListener(object : AppBarLayout.OnOffsetChangedListener {
            override fun onOffsetChanged(appBarLayout: AppBarLayout?, verticalOffset: Int) {
                var alpha = Math.abs(verticalOffset) * 1.0 / Utils.dpToPx(context, 60)
                down.alpha = 1 - alpha.toFloat()
            }
        })
    }

    private fun initAdapter() {
        mAdapter = RecyclerAdapter(this, { _adapter, parent, _ ->
            val convertView = _adapter.getView(R.layout.item_shareholder_credit, parent)
            ShareHolder(convertView)
        })
        lister.layoutManager = LinearLayoutManager(this)
        lister.addItemDecoration(SpaceItemDecoration(Utils.dpToPx(context, 15)))
        lister.adapter = mAdapter

        val header = ProgressLayout(this)
        header.setColorSchemeColors(ContextCompat.getColor(this, R.color.color_main))
        refresh.setHeaderView(header)
        val footer = BallPulseView(this)
        footer.setAnimatingColor(ContextCompat.getColor(this, R.color.color_main))
        refresh.setBottomView(footer)
        refresh.setOverScrollRefreshShow(false)
        refresh.setEnableLoadmore(true)
        refresh.setOnRefreshListener(object : RefreshListenerAdapter() {
            override fun onRefresh(refreshLayout: TwinklingRefreshLayout?) {
                page = 1
                doRequest(bean.company_id)
            }

            override fun onLoadMore(refreshLayout: TwinklingRefreshLayout?) {
                ++page
                doRequest(bean.company_id)
            }

        })
        refresh.setAutoLoadMore(true)
    }

    override fun onResume() {
        super.onResume()
        doRequest(bean.company_id)
    }

    var page = 1

    fun doRequest(companyId: Int?) {
        if (companyId != null) {
            SoguApi.getService(application)
                    .showCreditList(company_id = companyId, page = page)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeOn(Schedulers.io())
                    .subscribe({ payload ->
                        Log.d(TAG, Gson().toJson(payload))
                        if (payload.isOk) {
                            if (page == 1)
                                mAdapter.dataList.clear()
                            payload.payload?.forEach {
                                mAdapter.dataList.add(it)
                            }
                        } else {
                            showCustomToast(R.drawable.icon_toast_fail, payload.message)
                        }
                    }, { e ->
                        Log.e(TAG, e.message)
                        Trace.e(e)
                        SupportEmptyView.checkEmpty(this, mAdapter)
                    }, {
                        SupportEmptyView.checkEmpty(this, mAdapter)
                        refresh?.setEnableLoadmore(mAdapter.dataList.size % 20 == 0)
                        mAdapter.notifyDataSetChanged()
                        if (page == 1)
                            refresh?.finishRefreshing()
                        else
                            refresh?.finishLoadmore()
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
                            showCustomToast(R.drawable.icon_toast_fail, payload.message)
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
        private val inquireStatus = convertView.find<ImageView>(R.id.inquireStatus)
        private val phoneNumberTv = convertView.find<TextView>(R.id.phoneNumberTv)
        private val IDCardTv = convertView.find<TextView>(R.id.IDCardTv)
        private val companyTv = convertView.find<TextView>(R.id.companyTv)
        private val edit = convertView.find<ImageView>(R.id.edit)

        override fun setData(view: View, data: CreditInfo.Item, position: Int) {
            directorName.text = data.name
            directorPosition.text = data.position
            phoneNumberTv.text = data.phone
            IDCardTv.text = data.idCard
            //公司名字(可能消失)
            if (data.company.isNullOrEmpty()) {
                companyTv.visibility = View.GONE
            } else {
                companyTv.visibility = View.VISIBLE
                companyTv.text = data.company
            }
            when (data.status) {
                2 -> {
//                    inquireStatus.text = "查询完成"
//                    inquireStatus.textColor = Color.parseColor("#50d59d")
                }
                3 -> {
//                    inquireStatus.text = "查询失败"
//                    inquireStatus.textColor = Color.parseColor("#f7b62b")
                    inquireStatus.setImageResource(R.drawable.zhengxin_chaxunshibai)
                }
            }
            edit.setOnClickListener {
                //AddCreditActivity
            }
        }

        /**
         * 修改数据后,保存
         */
        private fun saveReqBean(data: CreditInfo.Item, inquireStatus: TextView) {
//            phoneNumberEdt.addTextChangedListener(object : TextWatcher {
//                override fun afterTextChanged(s: Editable?) {
//                    data.phone = s.toString()
//                    s?.let {
//                        if (!it.equals(data.phone)) {
//                            inquireBtn.isEnabled = true
//                            inquireBtn.text = "一键查询"
//                            data.isChange = true
//                        }
//                        if (data.isChange) {
//                            inquireStatus.text = "待查询"
//                            inquireStatus.textColor = Color.parseColor("#ffa715")
//                        }
//                    }
//                }
//
//                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
//                }
//
//                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
//
//                }
//
//            })
//            phoneNumberEdt.setOnFocusChangeListener { v, hasFocus ->
//                val editText = v as EditText
//                if (!hasFocus && editText.text.isNotEmpty() && !Utils.isMobileExact(editText.text)) {
//                    editText.setText("")
//                    showCustomToast(R.drawable.icon_toast_common, "请输入正确的手机号")
//                }
//            }


//            IDCardEdt.addTextChangedListener(object : TextWatcher {
//                override fun afterTextChanged(s: Editable?) {
//                    data.idCard = s.toString()
//                    s?.let {
//                        if (it.isNotEmpty()) {
//                            inquireBtn.isEnabled = true
//
//                        }
//                    }
//                }
//
//                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
//                }
//
//                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
//
//                }
//
//            })
//            IDCardEdt.setOnFocusChangeListener { v, hasFocus ->
//                val editText = v as EditText
//                if (!hasFocus && editText.text.isNotEmpty() && !Utils.isIDCard18(editText.text)) {
//                    editText.setText("")
//                    showCustomToast(R.drawable.icon_toast_common, "请输入正确的身份证号")
//                }
//            }
        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.back -> finish()
        //R.id.addTv -> AddCreditActivity.startForResult(this, bean.company_id)
            R.id.inquireBtn -> {
//                queryDataList.clear()
//                doInquire(queryDataList)
                AddCreditActivity.start(context, 0)
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
