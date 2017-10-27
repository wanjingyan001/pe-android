package com.sogukj.pe.ui.approve

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import com.framework.base.ToolbarActivity
import com.lcodecore.tkrefreshlayout.RefreshListenerAdapter
import com.lcodecore.tkrefreshlayout.TwinklingRefreshLayout
import com.lcodecore.tkrefreshlayout.footer.BallPulseView
import com.lcodecore.tkrefreshlayout.header.progresslayout.ProgressLayout
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.bean.ApprovalBean
import com.sogukj.pe.bean.ApproveFilterBean
import com.sogukj.pe.bean.CustomSealBean
import com.sogukj.pe.ui.SupportEmptyView
import com.sogukj.pe.ui.htdata.ProjectBookActivity
import com.sogukj.pe.util.Trace
import com.sogukj.pe.view.RecyclerAdapter
import com.sogukj.pe.view.RecyclerHolder
import com.sogukj.service.SoguApi
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_approve_list.*

class ApproveListActivity : ToolbarActivity() {

    lateinit var adapter: RecyclerAdapter<ApprovalBean>
    lateinit var inflater: LayoutInflater
    var mType: Int = 1
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_approve_list)
        inflater = LayoutInflater.from(this)
        mType = intent.getIntExtra(Extras.TYPE, 1)
        title = intent.getStringExtra(Extras.TITLE)
        setBack(true)
        adapter = RecyclerAdapter<ApprovalBean>(this, { _adapter, parent, type ->
            val convertView = _adapter.getView(R.layout.item_approval, parent) as View

            object : RecyclerHolder<ApprovalBean>(convertView) {
                val tvTitle = convertView.findViewById(R.id.tv_title) as TextView
                val tvType = convertView.findViewById(R.id.tv_type) as TextView
                val tvApplicant = convertView.findViewById(R.id.tv_applicant) as TextView
                val tvDate = convertView.findViewById(R.id.tv_date) as TextView
                val tvTime = convertView.findViewById(R.id.tv_time) as TextView
                val tvState = convertView.findViewById(R.id.tv_state) as TextView

                override fun setData(view: View, data: ApprovalBean, position: Int) {
                    tvTitle.text = data.title
                    tvType.text = data.kind
                    tvApplicant.text = data.name
                    val strTime = data.add_time
                    tvTime.visibility = View.GONE
                    if (!TextUtils.isEmpty(strTime)) {
                        val strs = strTime!!.trim().split(" ")
                        if (!TextUtils.isEmpty(strs.getOrNull(1))) {
                            tvTime.visibility = View.VISIBLE
                        }
                        tvDate.text = strs
                                .getOrNull(0)
                        tvTime.text = strs
                                .getOrNull(1)
                    }
                    tvState.text = data.status
                }
            }
        })
        adapter.onItemClick = { v, p ->
            val data = adapter.dataList.get(p)
        }
        val layoutManager = LinearLayoutManager(this)
        layoutManager.orientation = LinearLayoutManager.VERTICAL
        recycler_view.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))
        recycler_view.layoutManager = layoutManager
        recycler_view.adapter = adapter

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
                doRequest()
            }

            override fun onLoadMore(refreshLayout: TwinklingRefreshLayout?) {
                ++page
                doRequest()
            }

        })
        refresh.setAutoLoadMore(true)

        run {
            fl_filter.setOnClickListener {
                stateDefault()
            }
            ll_filter.setOnClickListener { }
            iv_filter.setOnClickListener {
                if (fl_filter.visibility == View.VISIBLE) {
                    stateDefault()
                } else {
                    stateFilter()
                }
            }
        }
        handler.postDelayed({
            doRequest()
        }, 100)
    }

    var filterBean: ApproveFilterBean? = null
    var filterType: Int? = null
    var key = ""
    fun stateDefault() {
        key = ""
        page = 1
        fl_filter.visibility = View.GONE
        paramStates.clear()
        paramTemplates.clear()
        doRequest()
    }

    fun stateSearch() {
        page = 1
        fl_filter.visibility = View.GONE
        paramStates.clear()
        paramTemplates.clear()
    }

    fun stateFilter() {
        key = ""
        page = 1
        if (filterBean == null) return
        fl_filter.visibility = View.VISIBLE
        tag_all.setOnClickListener {
            stateDefault()
        }
        rg_category.setOnCheckedChangeListener { group, checkedId ->
            if (checkedId == R.id.rb_all) {
                filterType = null
                ll_filter_other.visibility = View.GONE
            } else if (checkedId == R.id.rb_seal) {
                filterType = 2
                ll_filter_other.visibility = View.VISIBLE
                setFilterTags(filterBean!!.approve)
            } else if (checkedId == R.id.rb_sign) {
                filterType = 3
                setFilterTags(filterBean!!.sign)
                ll_filter_other.visibility = View.VISIBLE
            }
        }

        btn_reset.setOnClickListener {
            paramTemplates.clear()
            paramStates.clear()
            when (filterType) {
                3 -> setFilterTags(filterBean!!.sign)
                2 -> setFilterTags(filterBean!!.approve)
                else -> {
                }
            }

        }
        btn_ok.setOnClickListener {
            fl_filter.visibility = View.GONE
            doRequest()
        }
    }

    var paramTemplates = ArrayList<String>()
    var paramStates = ArrayList<String>()
    fun setFilterTags(itemBean: ApproveFilterBean.ItemBean?) {
        if (itemBean == null) return
        tags_type.removeAllViews()
        tags_state.removeAllViews()
        val textColor1 = resources.getColor(R.color.white)
        val textColor0 = resources.getColor(R.color.text_1)
        val bgColor0 = R.drawable.bg_tag_filter_0
        val bgColor1 = R.drawable.bg_tag_filter_1
        val onClickTemplate: (View) -> Unit = { v ->
            val tvTag = v as TextView
            val ftag = tvTag.tag as String
            if (paramTemplates.contains(ftag)) {
                paramTemplates.remove(ftag)
                tvTag.setTextColor(textColor0)
                tvTag.setBackgroundResource(bgColor0)
            } else {
                paramTemplates.add(ftag)
                tvTag.setTextColor(textColor1)
                tvTag.setBackgroundResource(bgColor1)
            }
        }

        val onClickStatus: (View) -> Unit = { v ->
            val tvTag = v as TextView
            val ftag = tvTag.tag as String
            if (paramStates.contains(ftag)) {
                paramStates.remove(ftag)
                tvTag.setTextColor(textColor0)
                tvTag.setBackgroundResource(bgColor0)
            } else {
                paramStates.add(ftag)
                tvTag.setTextColor(textColor1)
                tvTag.setBackgroundResource(bgColor1)
            }
        }
        itemBean.kind?.entries?.forEach { e ->
            val itemTag = inflater.inflate(R.layout.item_tag_filter2, null)
            val tvTag = itemTag.findViewById(R.id.tv_tag) as TextView
            tvTag.text = e.value
            tvTag.setOnClickListener(onClickTemplate)
            tags_type.addView(itemTag)
        }
        itemBean.status?.entries?.forEach { e ->
            val itemTag = inflater.inflate(R.layout.item_tag_filter2, null)
            val tvTag = itemTag.findViewById(R.id.tv_tag) as TextView
            tvTag.text = e.value
            tvTag.setOnClickListener(onClickStatus)
            tags_state.addView(itemTag)
        }
    }

    var page = 1
    fun doRequest() {
        val templates = paramTemplates.joinToString(",")
        val status = paramStates.joinToString(",")
        SoguApi.getService(application)
                .listApproval(status = mType, page = page,
                        fuzzyQuery = key,
                        type = filterType, template_id = templates, filter = status)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ payload ->
                    if (payload.isOk) {
                        if (page == 1)
                            adapter.dataList.clear()
                        payload.payload?.apply {
                            adapter.dataList.addAll(this)
                        }
                    } else
                        showToast(payload.message)
                }, { e ->
                    Trace.e(e)
                    showToast("暂无可用数据")
                }, {
                    SupportEmptyView.checkEmpty(this, adapter)
                    refresh?.setEnableLoadmore(adapter.dataList.size % 20 == 0)
                    adapter.notifyDataSetChanged()
                    if (page == 1)
                        refresh?.finishRefreshing()
                    else
                        refresh?.finishLoadmore()
                })


        SoguApi.getService(application)
                .approveFilter()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ payload ->
                    if (payload.isOk && payload.payload != null) {
                        filterBean = payload.payload!!
                    } else
                        showToast(payload.message)
                }, { e ->
                    Trace.e(e)
                })
    }


    companion object {
        fun start(ctx: Activity?, type: Int) {
            val intent = Intent(ctx, ApproveListActivity::class.java)
            val title = when (type) {
                1, 2 -> "待我审批"
                3 -> "我发起的"
                else -> ""
            }
            intent.putExtra(Extras.TYPE, type)
            intent.putExtra(Extras.TITLE, title)
            ctx?.startActivity(intent)
        }
    }
}