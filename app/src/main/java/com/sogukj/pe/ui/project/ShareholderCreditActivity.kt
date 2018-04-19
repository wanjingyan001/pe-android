package com.sogukj.pe.ui.project

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.AppBarLayout
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.Toolbar
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.framework.base.BaseActivity
import com.google.gson.Gson
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.bean.CreditInfo
import com.sogukj.pe.bean.ProjectBean
import com.sogukj.pe.util.Trace
import com.sogukj.pe.util.Utils
import com.sogukj.pe.view.RecyclerAdapter
import com.sogukj.pe.view.RecyclerHolder
import com.sogukj.service.SoguApi
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_shareholder_credit.*
import org.jetbrains.anko.find

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

        Glide.with(context).asGif().load(R.drawable.dynamic).into(gif)

        AppBarLayout.addOnOffsetChangedListener(object : AppBarLayout.OnOffsetChangedListener {
            override fun onOffsetChanged(appBarLayout: AppBarLayout?, verticalOffset: Int) {
                var alpha = Math.abs(verticalOffset) * 1.0 / Utils.dpToPx(context, 60)
                down.alpha = 1 - alpha.toFloat()
            }
        })

        page = 1
        doRequest(bean.company_id)

        mAdapter.onItemClick = { v, p ->
            SensitiveInfoActivity.start(context, mAdapter.dataList.get(p))
        }
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
        lister.adapter = mAdapter

        refresh.setOnRefreshListener {
            page = 1
            doRequest(bean.company_id)
            refresh.finishRefresh(1000)
        }
        refresh.setOnLoadMoreListener {
            ++page
            doRequest(bean.company_id)
            refresh.finishLoadMore(1000)
        }
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
                        Trace.e(e)
                        iv_empty.visibility = if (mAdapter.dataList.isEmpty()) View.VISIBLE else View.GONE
                    }, {
                        mAdapter.notifyDataSetChanged()
                        iv_empty.visibility = if (mAdapter.dataList.isEmpty()) View.VISIBLE else View.GONE
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
            if (data.type == 0) {
                directorPosition.visibility = View.GONE
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
                var item = CreditInfo.Item()
                item.company = bean.name
                item.company_id = bean.company_id!!
                AddCreditActivity.start(context, "ADD", item, 0x001)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 0x001) {
            data?.apply {
                var bean = this.getSerializableExtra(Extras.DATA) as CreditInfo.Item
                mAdapter.dataList.add(bean)
                mAdapter.notifyDataSetChanged()
            }
        } else if (requestCode == 0x002) {
            data?.apply {
                var bean = this.getSerializableExtra(Extras.DATA) as CreditInfo.Item
                var type = ""
                try {
                    type = this.getSerializableExtra(Extras.TYPE) as String
                } catch (e: Exception) {
                }
                var list = ArrayList<CreditInfo.Item>(mAdapter.dataList)
                for (index in list.indices) {
                    if (list[index].id == bean.id) {
                        if (type.equals("DELETE")) {
                            list.removeAt(index)
                        } else {
                            list[index] = bean
                        }
                        break
                    }
                }
                mAdapter.dataList.clear()
                mAdapter.dataList.addAll(list)
                mAdapter.notifyDataSetChanged()
            }
        }
    }
}
