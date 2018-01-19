package com.sogukj.pe.ui.fund

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.widget.LinearLayoutManager
import android.text.Html
import android.text.InputType
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
import com.sogukj.pe.bean.FundSmallBean
import com.sogukj.pe.bean.ProjectBean
import com.sogukj.pe.bean.ProjectBookBean
import com.sogukj.pe.ui.SupportEmptyView
import com.sogukj.pe.ui.htdata.ProjectBookMoreActivity
import com.sogukj.pe.ui.htdata.ProjectBookUploadActivity
import com.sogukj.pe.util.Trace
import com.sogukj.pe.view.ListAdapter
import com.sogukj.pe.view.ListHolder
import com.sogukj.pe.view.RecyclerAdapter
import com.sogukj.pe.view.RecyclerHolder
import com.sogukj.service.SoguApi
import com.sogukj.util.Store
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_fund_book.*
import kotlinx.android.synthetic.main.search_view.*
import java.text.SimpleDateFormat

class FundBookActivity : ToolbarActivity(), SupportEmptyView {

    lateinit var adapter1: ListAdapter<ProjectBookBean>
    lateinit var adapter2: ListAdapter<ProjectBookBean>
    lateinit var adapter3: ListAdapter<ProjectBookBean>
    lateinit var adapter: RecyclerAdapter<ProjectBookBean>

    lateinit var bean: FundSmallBean
    val df = SimpleDateFormat("yyyy-MM-dd")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fund_book)

        bean = intent.getSerializableExtra(Extras.DATA) as FundSmallBean
        setBack(true)
        title = "项目文书"
        btn_upload.setOnClickListener {
            if (!filterList.isEmpty()) {
                FundUploadActivity.start(this, bean, filterList)
            }
        }
        adapter1 = ListAdapter<ProjectBookBean> { ProjectBookHolder() }
        adapter2 = ListAdapter<ProjectBookBean> { ProjectBookHolder() }
        adapter3 = ListAdapter<ProjectBookBean> { ProjectBookHolder() }
        adapter = RecyclerAdapter<ProjectBookBean>(this, { _adapter, parent, type ->
            val convertView = _adapter.getView(R.layout.item_project_book, parent) as View
            object : RecyclerHolder<ProjectBookBean>(convertView) {
                val tvSummary = convertView.findViewById(R.id.tv_summary) as TextView
                val tvDate = convertView.findViewById(R.id.tv_date) as TextView
                val tvTime = convertView.findViewById(R.id.tv_time) as TextView
                val tvType = convertView.findViewById(R.id.tv_type) as TextView
                override fun setData(view: View, data: ProjectBookBean, position: Int) {
                    tvSummary.text = data?.doc_title
                    val strTime = data?.add_time
                    tvTime.visibility = View.GONE
                    if (!TextUtils.isEmpty(strTime)) {
                        val strs = strTime!!.trim().split(" ")
                        if (!TextUtils.isEmpty(strs.getOrNull(1))) {
                            tvTime.visibility = View.VISIBLE
                        }
                        tvDate.text = strs.getOrNull(0)
                        tvTime.text = strs.getOrNull(1)
                    }
                    tvType.text = data?.name
                }

            }
        })

        run {
            stateDefault()
            val layoutManager = LinearLayoutManager(this)
            layoutManager.orientation = LinearLayoutManager.VERTICAL
//            recycler_result.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))
            recycler_result.layoutManager = layoutManager
            recycler_result.adapter = adapter

            adapter.onItemClick = { v, p ->
                val data = adapter.getItem(p);
                if (!TextUtils.isEmpty(data.url)) {
                    val intent = Intent(Intent.ACTION_VIEW)
                    intent.data = Uri.parse(data.url)
                    startActivity(intent)
                }
            }


            tv_result_title.text = Html.fromHtml(getString(R.string.tv_title_result_search, 0))
            search_view.iv_back.visibility = View.VISIBLE
            search_view.iv_back.setOnClickListener {
                onBackPressed()
            }
            search_view.tv_cancel.visibility = View.GONE
            search_view.tv_cancel.setOnClickListener {
                stateDefault()
                this.key = ""
                search_view.search = ""
            }
            search_view.onTextChange = { text ->
                handler.removeCallbacks(searchTask)
                handler.postDelayed(searchTask, 100)
            }
            et_search.setOnClickListener {
                stateSearch()
            }
            search_view.onSearch = { text ->
                if (null != text && !TextUtils.isEmpty(text))
                    doSearch(text!!)
            }
        }
        list1.adapter = adapter1
        list2.adapter = adapter2
        list3.adapter = adapter3
        list1.setOnItemClickListener { parent, view, position, id ->
            val data = adapter1.getItem(position);
            if (!TextUtils.isEmpty(data?.url)) {
                val intent = Intent(Intent.ACTION_VIEW)
                intent.data = Uri.parse(data!!.url)
                startActivity(intent)
            }
        }
        list2.setOnItemClickListener { parent, view, position, id ->
            val data = adapter2.getItem(position);
            if (!TextUtils.isEmpty(data?.url)) {
                val intent = Intent(Intent.ACTION_VIEW)
                intent.data = Uri.parse(data!!.url)
                startActivity(intent)
            }
        }
        list3.setOnItemClickListener { parent, view, position, id ->
            val data = adapter3.getItem(position);
            if (!TextUtils.isEmpty(data?.url)) {
                val intent = Intent(Intent.ACTION_VIEW)
                intent.data = Uri.parse(data!!.url)
                startActivity(intent)
            }
        }
        tv_more1.setOnClickListener {
            //ProjectBookMoreActivity.start(this, bean, 1)
        }
        tv_more2.setOnClickListener {
            //ProjectBookMoreActivity.start(this, bean, 2)
        }
        tv_more3.setOnClickListener {
            //ProjectBookMoreActivity.start(this, bean, 2)
        }
        list1.setOnItemClickListener { parent, view, position, id ->
            val data = adapter1.dataList[position]
//            NewsDetailActivity.start(this, data)
        }
        list2.setOnItemClickListener { parent, view, position, id ->
            val data = adapter2.dataList[position]
//            NewsDetailActivity.start(this, data)
        }
        list3.setOnItemClickListener { parent, view, position, id ->
            val data = adapter3.dataList[position]
//            NewsDetailActivity.start(this, data)
        }

        val header = ProgressLayout(this)
        header.setColorSchemeColors(ContextCompat.getColor(this, R.color.color_main))
        refresh.setHeaderView(header)
        val footer = BallPulseView(this)
        footer.setAnimatingColor(ContextCompat.getColor(this, R.color.color_main))
        refresh.setBottomView(footer)
        refresh.setOverScrollRefreshShow(false)
        refresh.setOnRefreshListener(object : RefreshListenerAdapter() {
            override fun onRefresh(refreshLayout: TwinklingRefreshLayout?) {
                page = 1
                handler.post(searchTask)
            }

            override fun onLoadMore(refreshLayout: TwinklingRefreshLayout?) {
                ++page
                handler.post(searchTask)
            }

        })
        refresh.setAutoLoadMore(true)
        handler.postDelayed({
            doRequest()
        }, 100)
    }

    fun stateDefault() {
        page = 1
        fl_filter.visibility = View.GONE
        search_view.tv_cancel.visibility = View.GONE
        iv_filter.visibility = View.VISIBLE
        ll_result.visibility = View.GONE
        et_search.isFocusable = false
        et_search.clearFocus()
    }

    fun stateSearch() {
        page = 1
        checkedFilter.clear()
        fl_filter.visibility = View.GONE
        search_view.tv_cancel.visibility = View.VISIBLE
        iv_filter.visibility = View.GONE
        ll_result.visibility = View.VISIBLE

        et_search.inputType = InputType.TYPE_CLASS_TEXT
        et_search.isFocusable = true
        et_search.isFocusableInTouchMode = true
        et_search.requestFocus()
//        Utils.showInput(this, et_search)

    }

    fun stateFilter() {
        page = 1
        checkedFilter.clear()
        setTags(filterList)
        search_view.search = ""
        fl_filter.visibility = View.VISIBLE
        search_view.tv_cancel.visibility = View.GONE
        iv_filter.visibility = View.VISIBLE
        ll_result.visibility = View.GONE
        et_search.isFocusable = false
        et_search.clearFocus()

    }


    val searchTask = Runnable {
        doSearch(search_view.search)
    }
    var key = ""
    val filterList = HashMap<Int, String>()
    var page = 1

    fun doSearch(text: String = "") {
        this.key = text
        val user = Store.store.getUser(this)
        val filter = if (checkedFilter.isEmpty()) null else checkedFilter.joinToString(",")
//        project.company_id = 325
        //TODO
//        SoguApi.getService(application)
//                .projectBookSearch(company_id = bean.company_id!!,
//                        fuzzyQuery = text
//                        , fileClass = filter, page = page)
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribeOn(Schedulers.io())
//                .subscribe({ payload ->
//                    if (payload.isOk) {
//                        if (page == 1)
//                            adapter.dataList.clear()
//                        payload.payload?.apply {
//                            adapter.dataList.addAll(this)
//                        }
//                    } else
//                        showToast(payload.message)
//                }, { e ->
//                    Trace.e(e)
//                }, {
//                    tv_result_title.text = Html.fromHtml(getString(R.string.tv_title_result_search, adapter.dataList.size))
//                    adapter.notifyDataSetChanged()
//                    if (page == 1)
//                        refresh?.finishRefreshing()
//                    else
//                        refresh?.finishLoadmore()
//                })

    }

    fun doRequest() {
//        project.company_id = 325
//        SoguApi.getService(application)
//                .projectBook(company_id = bean.company_id!!)
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribeOn(Schedulers.io())
//                .subscribe({ payload ->
//                    if (payload.isOk) {
//                        adapter1.dataList.clear()
//                        adapter2.dataList.clear()
//                        adapter3.dataList.clear()
//                        payload.payload?.apply {
//                            if (null != list1)
//                                for (i in 0..2) {
//                                    if (list1!!.size > i) {
//                                        adapter1.dataList.add(list1!!.get(i))
//                                    }
//                                }
//
//                            if (null != list2)
//                                for (i in 0..2) {
//                                    if (list2!!.size > i) {
//                                        adapter2.dataList.add(list2!!.get(i))
//                                    }
//                                }
//                            if (null != list3)
//                                for (i in 0..2) {
//                                    if (list3!!.size > i) {
//                                        adapter3.dataList.add(list3!!.get(i))
//                                    }
//                                }
//                        }
//                        adapter1.notifyDataSetChanged()
//                        adapter2.notifyDataSetChanged()
//                        adapter3.notifyDataSetChanged()
//                    } else
//                        showToast(payload.message)
//                }, { e ->
//                    Trace.e(e)
//                }, {
//                    if (adapter1.dataList.size < 3)
//                        tv_more1.visibility = View.GONE
//                    else
//                        tv_more1.visibility = View.VISIBLE
//
//                    if (adapter2.dataList.size < 3)
//                        tv_more2.visibility = View.GONE
//                    else
//                        tv_more2.visibility = View.VISIBLE
//
//                    if (adapter3.dataList.size < 3)
//                        tv_more3.visibility = View.GONE
//                    else
//                        tv_more3.visibility = View.VISIBLE
//                })
//        SoguApi.getService(application)
//                .projectFilter()
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribeOn(Schedulers.io())
//                .subscribe({ payload ->
//                    if (payload.isOk && payload.payload != null) {
//                        filterList.clear()
//                        filterList.putAll(payload.payload!!)
//                        setTags(filterList)
//
//                    } else
//                        showToast(payload.message)
//                }, { e ->
//                    Trace.e(e)
//                })
    }

    val checkedFilter = ArrayList<String>()
    fun setTags(filterList: HashMap<Int, String>) {
        checkedFilter.clear()
        tags.removeAllViews()
        val textColor1 = resources.getColor(R.color.white)
        val textColor0 = resources.getColor(R.color.text_1)
        val bgColor0 = R.drawable.bg_tag_filter_0
        val bgColor1 = R.drawable.bg_tag_filter_1
        val onClick: (View) -> Unit = { v ->
            val tvTag = v as TextView
            val ftag = tvTag.tag as String
            if (checkedFilter.contains(ftag)) {
                checkedFilter.remove(ftag)
                tvTag.setTextColor(textColor0)
                tvTag.setBackgroundResource(bgColor0)
            } else {
                checkedFilter.add(ftag)
                tvTag.setTextColor(textColor1)
                tvTag.setBackgroundResource(bgColor1)
            }
        }
        kotlin.run {
            val itemTag = View.inflate(this, R.layout.item_tag_filter, null)
            val tvTag = itemTag.findViewById(R.id.tv_tag) as TextView
            tvTag.text = "全部"
            tags.addView(itemTag)
            tvTag.setOnClickListener {
                fl_filter.visibility = View.GONE
                ll_result.visibility = View.VISIBLE
                checkedFilter.clear()
                doSearch()
            }
        }
        filterList.entries.forEach { e ->
            val itemTag = View.inflate(this, R.layout.item_tag_filter, null)
            val tvTag = itemTag.findViewById(R.id.tv_tag) as TextView
            tvTag.text = e.value
            tvTag.tag = "${e.key}"
            tvTag.setOnClickListener(onClick)
            tags.addView(itemTag)
        }


        fl_filter.setOnClickListener {
            stateDefault()
        }
        iv_filter.setOnClickListener {
            if (fl_filter.visibility == View.VISIBLE) {
                stateDefault()
            } else {
                stateFilter()
            }
        }
        ll_filter.setOnClickListener { }
        btn_reset.setOnClickListener {
            checkedFilter.clear()
            for (i in 0..tags.childCount - 1) {
                val itemTag = tags.getChildAt(i)
                val tvTag = itemTag.findViewById(R.id.tv_tag) as TextView
                tvTag.setTextColor(textColor0)
                tvTag.setBackgroundResource(bgColor0)
            }
        }
        btn_ok.setOnClickListener {
            fl_filter.visibility = View.GONE
            ll_result.visibility = View.VISIBLE
            if (!checkedFilter.isEmpty())
                doSearch()
            else
                doSearch()
        }
    }

    class ProjectBookHolder
        : ListHolder<ProjectBookBean> {
        lateinit var tvSummary: TextView
        lateinit var tvDate: TextView
        lateinit var tvTime: TextView
        lateinit var tvType: TextView
        override fun createView(inflater: LayoutInflater): View {
            val convertView = inflater.inflate(R.layout.item_project_book, null)
            tvSummary = convertView.findViewById(R.id.tv_summary) as TextView
            tvDate = convertView.findViewById(R.id.tv_date) as TextView
            tvTime = convertView.findViewById(R.id.tv_time) as TextView
            tvType = convertView.findViewById(R.id.tv_type) as TextView
            return convertView
        }

        override fun showData(convertView: View, position: Int, data: ProjectBookBean?) {
            tvSummary.text = data?.doc_title
            val strTime = data?.add_time
            tvTime.visibility = View.GONE
            if (!TextUtils.isEmpty(strTime)) {
                val strs = strTime!!.trim().split(" ")
                if (!TextUtils.isEmpty(strs.getOrNull(1))) {
                    tvTime.visibility = View.VISIBLE
                }
                tvDate.text = strs.getOrNull(0)
                tvTime.text = strs.getOrNull(1)
            }
            tvType.text = data?.name
        }

    }

    override fun onResume() {
        super.onResume()
        doRequest()
    }

    companion object {
        fun start(ctx: Activity?, bean: FundSmallBean) {
            val intent = Intent(ctx, FundBookActivity::class.java)
            intent.putExtra(Extras.DATA, bean)
            ctx?.startActivity(intent)
        }
    }
}
