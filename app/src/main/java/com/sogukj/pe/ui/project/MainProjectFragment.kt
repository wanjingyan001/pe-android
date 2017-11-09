package com.sogukj.pe.ui.project

import android.graphics.Color
import android.os.Bundle
import android.support.design.widget.TabLayout
import android.support.v4.content.ContextCompat
import android.support.v4.view.ViewPager
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.text.Html
import android.text.InputType
import android.text.TextUtils
import android.view.View
import android.widget.TextView
import com.bumptech.glide.Glide
import com.framework.base.BaseFragment
import com.lcodecore.tkrefreshlayout.RefreshListenerAdapter
import com.lcodecore.tkrefreshlayout.TwinklingRefreshLayout
import com.lcodecore.tkrefreshlayout.footer.BallPulseView
import com.lcodecore.tkrefreshlayout.header.progresslayout.ProgressLayout
import com.sogukj.pe.R
import com.sogukj.pe.bean.ProjectBean
import com.sogukj.pe.ui.user.UserActivity
import com.sogukj.pe.util.Trace
import com.sogukj.pe.util.Utils
import com.sogukj.pe.view.ArrayPagerAdapter
import com.sogukj.pe.view.RecyclerAdapter
import com.sogukj.pe.view.RecyclerHolder
import com.sogukj.service.SoguApi
import com.sogukj.util.Store
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_main_project.*
import kotlinx.android.synthetic.main.search_view.*
import kotlinx.android.synthetic.main.sogu_toolbar_main_proj.*
import org.jetbrains.anko.textColor
import java.util.*

/**
 * Created by qinfei on 17/7/18.
 */
class MainProjectFragment : BaseFragment() {
    override val containerViewId: Int
        get() = R.layout.fragment_main_project //To change initializer of created properties use File | Settings | File Templates.

    val fragments = arrayOf(
            ProjectListFragment.newInstance(ProjectListFragment.TYPE_CB),
            ProjectListFragment.newInstance(ProjectListFragment.TYPE_LX),
            ProjectListFragment.newInstance(ProjectListFragment.TYPE_YT)
    )
    lateinit var adapter: RecyclerAdapter<ProjectBean>

    lateinit var hisAdapter: RecyclerAdapter<String>
    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        adapter = RecyclerAdapter<ProjectBean>(baseActivity!!, { _adapter, parent, type ->
            val convertView = _adapter.getView(R.layout.item_main_project_search, parent) as View
            object : RecyclerHolder<ProjectBean>(convertView) {
                val tv1 = convertView.findViewById(R.id.tv1) as TextView
                val tv2 = convertView.findViewById(R.id.tv2) as TextView
                val tv3 = convertView.findViewById(R.id.tv3) as TextView

                override fun setData(view: View, data: ProjectBean, position: Int) {
                    var label = data.name
                    data.shortName?.apply { label = this }
                    if (!TextUtils.isEmpty(label) && !TextUtils.isEmpty(key)) {
                        label = label!!.replaceFirst(key, "<font color='#ff3300'>${key}</font>")
                    }
                    tv1.text = Html.fromHtml(label)
                    if (type == 1) {
                        tv2.text = when (data.status) {
                            2 -> "已完成"
                            else -> "准备中"
                        }
                    } else {
                        tv2.text = data.state
                        tv2.textColor = when (data.state) {
                            "初创" -> Color.parseColor("#9e9e9e")
                            "天使轮" -> Color.parseColor("#e64a19")
                            "A轮" -> Color.parseColor("#f57c00")
                            "B轮" -> Color.parseColor("#ffa000")
                            "C轮" -> Color.parseColor("#fbc02d")
                            "D轮" -> Color.parseColor("#afb42b")
                            "E轮" -> Color.parseColor("#689f38")
                            "PIPE轮" -> Color.parseColor("#388e3c")
                            "新三板" -> Color.parseColor("#00796b")
                            "IPO" -> Color.parseColor("#0097a7")
                            else -> Color.parseColor("#9e9e9e")
                        }
                        val bg = when (data.state) {
                            "初创" -> R.drawable.bg_border_proj1
                            "天使轮" -> R.drawable.bg_border_proj2
                            "A轮" -> R.drawable.bg_border_proj3
                            "B轮" -> R.drawable.bg_border_proj4
                            "C轮" -> R.drawable.bg_border_proj5
                            "D轮" -> R.drawable.bg_border_proj6
                            "E轮" -> R.drawable.bg_border_proj7
                            "PIPE轮" -> R.drawable.bg_border_proj8
                            "新三板" -> R.drawable.bg_border_proj9
                            "IPO" -> R.drawable.bg_border_proj10
                            else -> R.drawable.bg_border_proj1
                        }
                        tv2.setBackgroundResource(bg)

                    }
                    tv3.text = when (type) {
                        1 -> data.add_time
                        else -> data.update_time
                    }

                    if (tv2.text.isNullOrEmpty()) tv2.text = "--"
                    if (tv3.text.isNullOrEmpty()) tv3.text = "--"
                }

            }
        })
        hisAdapter = RecyclerAdapter<String>(baseActivity!!, { _adapter, parent, type ->
            val convertView = _adapter.getView(R.layout.item_main_project_search, parent) as View
            object : RecyclerHolder<String>(convertView) {
                val tv1 = convertView.findViewById(R.id.tv1) as TextView
                val tv2 = convertView.findViewById(R.id.tv2) as TextView
                val tv3 = convertView.findViewById(R.id.tv3) as TextView

                override fun setData(view: View, data: String, position: Int) {
                    tv1.text = data
                }

            }
        })
        run {
            val layoutManager = LinearLayoutManager(baseActivity)
            recycler_his.addItemDecoration(DividerItemDecoration(baseActivity, DividerItemDecoration.VERTICAL))
            recycler_his.layoutManager = layoutManager
            recycler_his.adapter = hisAdapter
            hisAdapter.onItemClick = { v, p ->
                val data = hisAdapter.dataList.get(p);
                search_view.search = (data)
                doSearch(data)

            }
        }
        run {
            val layoutManager = LinearLayoutManager(baseActivity)
            layoutManager.orientation = LinearLayoutManager.VERTICAL
            recycler_result.addItemDecoration(DividerItemDecoration(baseActivity, DividerItemDecoration.VERTICAL))
            recycler_result.layoutManager = layoutManager
            recycler_result.adapter = adapter

            adapter.onItemClick = { v, p ->
                val project = adapter.getItem(p);
                ProjectActivity.start(baseActivity, project)
            }
        }
        tv_result_title.text = Html.fromHtml(getString(R.string.tv_title_result_project, 0))

        iv_user.setOnClickListener {
            UserActivity.start(baseActivity);
        }

        Store.store.getUser(baseActivity!!)?.apply {
            if (null != url) {
                val ch = name?.first()
                iv_user.setChar(ch)
                Glide.with(baseActivity)
                        .load(headImage())
                        .into(iv_user)
            }

        }

//        iv_add.setOnClickListener {
//            ProjectAddActivity.start(baseActivity)
//        }
        iv_focus.setOnClickListener {
            ProjectFocusActivity.start(baseActivity)
        }
        search_view.onTextChange = { text ->
            if (TextUtils.isEmpty(text)) {
                ll_history.visibility = View.VISIBLE
            } else {
                offset = 0
                handler.removeCallbacks(searchTask)
                handler.postDelayed(searchTask, 100)
            }
        }
        search_view.tv_cancel.visibility = View.VISIBLE
        search_view.tv_cancel.setOnClickListener {
            this.key = ""
            search_view.search = ""
            ll_search.visibility = View.GONE

            hisAdapter.dataList.clear()
            hisAdapter.dataList.addAll(Store.store.projectSearch(baseActivity!!))
            hisAdapter.notifyDataSetChanged()
            ll_history.visibility = View.VISIBLE
        }
        iv_clear.setOnClickListener {
            Store.store.projectSearchClear(baseActivity!!)
            hisAdapter.dataList.clear()
            hisAdapter.dataList.addAll(Store.store.projectSearch(baseActivity!!))
            hisAdapter.notifyDataSetChanged()
        }
        search_view.onSearch = { text ->
            if (null != text && !TextUtils.isEmpty(text))
                doSearch(text!!)
        }


        fb_add.setOnClickListener {
            StoreProjectAddActivity.startAdd(baseActivity)
        }
        fb_search.setOnClickListener {
            ll_search.visibility = View.VISIBLE
            et_search.postDelayed({
                et_search.inputType = InputType.TYPE_CLASS_TEXT
                et_search.isFocusable = true
                et_search.isFocusableInTouchMode = true
                et_search.requestFocus()
                Utils.toggleSoftInput(baseActivity, et_search)
            }, 100)
        }
        var adapter = ArrayPagerAdapter(childFragmentManager, fragments)
        view_pager.adapter = adapter

        val user = Store.store.getUser(baseActivity!!)
        tabs?.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabReselected(tab: TabLayout.Tab?) {

            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {

            }

            override fun onTabSelected(tab: TabLayout.Tab) {
                view_pager?.currentItem = tab.position
            }

        })
        view_pager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) {
            }

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
            }

            override fun onPageSelected(position: Int) {
                tabs?.getTabAt(position)?.select()
                fb_add.visibility = if (position == 0) View.VISIBLE else View.GONE
//                iv_search?.visibility = if (position == 2) View.VISIBLE else View.GONE
//                iv_add?.visibility = if (position == 1 && user?.is_admin == 1) View.VISIBLE else View.GONE
            }

        })
        hisAdapter.dataList.clear()
        hisAdapter.dataList.addAll(Store.store.projectSearch(baseActivity!!))
        hisAdapter.notifyDataSetChanged()


        val header = ProgressLayout(baseActivity)
        header.setColorSchemeColors(ContextCompat.getColor(baseActivity, R.color.color_main))
        refresh.setHeaderView(header)
        val footer = BallPulseView(baseActivity)
        footer.setAnimatingColor(ContextCompat.getColor(baseActivity, R.color.color_main))
        refresh.setBottomView(footer)
        refresh.setOverScrollRefreshShow(false)
        refresh.setOnRefreshListener(object : RefreshListenerAdapter() {
            override fun onRefresh(refreshLayout: TwinklingRefreshLayout?) {
                offset = 0
                handler.post(searchTask)
            }

            override fun onLoadMore(refreshLayout: TwinklingRefreshLayout?) {
                offset = this@MainProjectFragment.adapter.dataList.size
                handler.post(searchTask)
            }

        })
        refresh.setAutoLoadMore(true)

    }

    val searchTask = Runnable {
        doSearch(search_view.search)
    }
    var key = ""
    var offset = 0
    fun doSearch(text: String) {
        this.key = text
        if (TextUtils.isEmpty(key)) return
        val user = Store.store.getUser(baseActivity!!)
        val pos = tabs.selectedTabPosition
        var type = when (pos) {
            0 -> 4;
            else -> pos
        }
        val tmplist = LinkedList<String>()
        tmplist.add(text)
        Store.store.projectSearch(baseActivity!!, tmplist)
        SoguApi.getService(baseActivity!!.application)
                .listProject(offset = offset, pageSize = 20, uid = user?.uid, type = type, fuzzyQuery = text)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ payload ->
                    if (payload.isOk) {
                        payload?.apply {
                            tv_result_title.text = Html.fromHtml(getString(R.string.tv_title_result_project, total))
                        }
                        if (offset == 0)
                            adapter.dataList.clear()
                        payload.payload?.apply {
                            adapter.dataList.addAll(this)
                        }
                    } else
                        showToast(payload.message)
                }, { e ->
                    Trace.e(e)
                }, {
                    ll_history.visibility = View.GONE
                    adapter.notifyDataSetChanged()
                    if (offset == 0)
                        refresh?.finishRefreshing()
                    else
                        refresh?.finishLoadmore()

                    hisAdapter.dataList.clear()
                    hisAdapter.dataList.addAll(Store.store.projectSearch(baseActivity!!))
                    hisAdapter.notifyDataSetChanged()


                })
    }

    companion object {
        val TAG = MainProjectFragment::class.java.simpleName

        fun newInstance(): MainProjectFragment {
            val fragment = MainProjectFragment()
            val intent = Bundle()
            fragment.arguments = intent
            return fragment
        }
    }
}