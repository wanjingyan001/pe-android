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
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.ImageButton
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
        initSearchView()
        back.setOnClickListener(this)
        inquireBtn.setOnClickListener(this)

        AppBarLayout.addOnOffsetChangedListener(object : AppBarLayout.OnOffsetChangedListener {
            override fun onOffsetChanged(appBarLayout: AppBarLayout?, verticalOffset: Int) {
                var alpha = Math.abs(verticalOffset) * 1.0 / Utils.dpToPx(context, 60)
                down.alpha = 1 - alpha.toFloat()
            }
        })
    }

    var searchKey: String? = null

    private fun initSearchView() {
        search_edt.filters = Utils.getFilter(context)
        search_edt.setOnFocusChangeListener { v, hasFocus ->
            if (hasFocus) {
                search_hint.visibility = View.GONE
                search_icon.visibility = View.VISIBLE
            } else {
                search_hint.visibility = View.VISIBLE
                search_icon.visibility = View.GONE
                search_edt.clearFocus()
            }
        }
        search_edt.setOnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                searchKey = search_edt.text.toString()
                page = 1
                doRequest(bean.company_id)
                true
            } else {
                false
            }
        }
        search_edt.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {

            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (search_edt.text.toString().isEmpty()) {
                    searchKey = ""
                    page = 1
                    doRequest(bean.company_id)
                }
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
                    .showCreditList(company_id = companyId, page = page, fuzzyQuery = searchKey)
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

    inner class ShareHolder(convertView: View) : RecyclerHolder<CreditInfo.Item>(convertView) {

        private val directorName = convertView.find<TextView>(R.id.directorName)
        private val directorPosition = convertView.find<TextView>(R.id.directorPosition)
        private val inquireStatus = convertView.find<ImageView>(R.id.inquireStatus)
        private val phoneNumberTv = convertView.find<TextView>(R.id.phoneNumberTv)
        private val IDCardTv = convertView.find<TextView>(R.id.IDCardTv)
        private val companyTv = convertView.find<TextView>(R.id.companyTv)
        private val edit = convertView.find<ImageView>(R.id.edit)
        private val number = convertView.find<TextView>(R.id.number)

        override fun setData(view: View, data: CreditInfo.Item, position: Int) {
            directorName.text = data.name
            directorPosition.text = when (data.type) {
                1 -> "董监高"
                2 -> "股东"
                else -> ""
            }
            phoneNumberTv.text = data.phone
            IDCardTv.text = data.idCard
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
                    if (data.sum == null || data.sum == 0) {
                        number.visibility = View.GONE
                        inquireStatus.setImageResource(R.drawable.zhengxin_zhengchang)
                    } else {
                        number.visibility = View.VISIBLE
                        number.text = "${data.sum}"
                        inquireStatus.setImageResource(R.drawable.zhengxin_fail)
                    }
                }
                3 -> {
//                    inquireStatus.text = "查询失败"
//                    inquireStatus.textColor = Color.parseColor("#f7b62b")
                    number.visibility = View.GONE
                    inquireStatus.setImageResource(R.drawable.zhengxin_chaxunshibai)
                }
            }
            edit.setOnClickListener {
                AddCreditActivity.start(context, "EDIT", data, 0x002)
            }
        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.back -> finish()
            R.id.inquireBtn -> {
                AddCreditActivity.start(context, "ADD", null, 0x001)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 0x001) {
            data?.apply {
                var bean = this.getSerializableExtra(Extras.DATA) as CreditInfo.Item
            }
        } else if (requestCode == 0x002) {

        }
    }
}
