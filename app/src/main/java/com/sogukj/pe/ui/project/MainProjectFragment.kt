package com.sogukj.pe.ui.project

import android.annotation.SuppressLint
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
import android.util.Log
import android.view.View
import android.view.ViewTreeObserver
import android.widget.ImageView
import android.widget.TextView
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.Theme
import com.framework.base.BaseFragment
import com.google.gson.Gson
import com.lcodecore.tkrefreshlayout.RefreshListenerAdapter
import com.lcodecore.tkrefreshlayout.TwinklingRefreshLayout
import com.lcodecore.tkrefreshlayout.footer.BallPulseView
import com.lcodecore.tkrefreshlayout.header.progresslayout.ProgressLayout
import com.sogukj.pe.R
import com.sogukj.pe.bean.ProjectBean
import com.sogukj.pe.util.Trace
import com.sogukj.pe.util.Utils
import com.sogukj.pe.view.*
import com.sogukj.service.SoguApi
import com.sogukj.util.Store
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_main_project.*
import kotlinx.android.synthetic.main.search_view.*
import org.jetbrains.anko.textColor
import java.util.*

/**
 * Created by qinfei on 17/7/18.
 */
class MainProjectFragment : BaseFragment() {
    override val containerViewId: Int
        get() = R.layout.fragment_main_project //To change initializer of created properties use File | Settings | File Templates.

    val fragments = arrayOf(
            ProjectListFragment.newInstance(ProjectListFragment.TYPE_DY),
            ProjectListFragment.newInstance(ProjectListFragment.TYPE_CB),
            ProjectListFragment.newInstance(ProjectListFragment.TYPE_LX),
            ProjectListFragment.newInstance(ProjectListFragment.TYPE_YT),
            ProjectListFragment.newInstance(ProjectListFragment.TYPE_TC)
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
                    data.shortName?.apply {
                        if (this != "") {
                            label = this
                        }
                    }
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
            val convertView = _adapter.getView(R.layout.item_project_search_item, parent) as View
            object : RecyclerHolder<String>(convertView) {
                val tv1 = convertView.findViewById(R.id.tv1) as TextView
                val delete = convertView.findViewById(R.id.delete) as ImageView

                override fun setData(view: View, data: String, position: Int) {
                    tv1.text = data
                    delete.setOnClickListener {
                        hisAdapter.dataList.removeAt(position)
                        hisAdapter.notifyDataSetChanged()
                        Store.store.projectSearchRemover(baseActivity!!, position)
                    }
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

//        iv_user.setOnClickListener {
//            val activity = activity as MainActivity
//            activity.find<RadioGroup>(R.id.rg_tab_main).check(R.id.rb_my)
//        }
//        Store.store.getUser(baseActivity!!)?.apply {
//            if (null != url) {
//                val ch = name?.first()
//                iv_user.setChar(ch)
//                Glide.with(baseActivity)
//                        .load(headImage())
//                        .into(iv_user)
//            }
//
//        }

//        iv_add.setOnClickListener {
//            ProjectAddActivity.start(baseActivity)
//        }
//        iv_focus.setOnClickListener {
//            ProjectFocusActivity.start(baseActivity)
//        }
        //不做实时查询,只有点击软键盘上的查询时才进行查询
//        search_view.onTextChange = { text ->
//            if (TextUtils.isEmpty(text)) {
//                ll_history.visibility = View.VISIBLE
//            } else {
//                offset = 0
//                handler.removeCallbacks(searchTask)
//                handler.postDelayed(searchTask, 100)
//            }
//        }


        search_view.tv_cancel.visibility = View.VISIBLE
        val search = Store.store.projectSearch(baseActivity!!)


        search_view.tv_cancel.setOnClickListener {
            this.key = ""
            search_view.search = ""
            ll_search.visibility = View.GONE
            toolbar.visibility = View.VISIBLE

            hisAdapter.dataList.clear()
            hisAdapter.dataList.addAll(search)
            hisAdapter.notifyDataSetChanged()
            ll_history.visibility = View.VISIBLE
        }
        iv_clear.setOnClickListener {
            MaterialDialog.Builder(activity)
                    .theme(Theme.LIGHT)
                    .title("提示")
                    .content("确认全部删除?")
                    .positiveText("确认")
                    .negativeText("取消")
                    .onPositive { dialog, which ->
                        Store.store.projectSearchClear(baseActivity!!)
                        hisAdapter.dataList.clear()
                        hisAdapter.notifyDataSetChanged()
                        last_search_layout.visibility = View.GONE
                    }
                    .show()
        }
        search_view.onSearch = { text ->
            if (null != text && !TextUtils.isEmpty(text))
                doSearch(text!!)
        }

        fb_add.setOnClickListener {
            if (view_pager.currentItem == 0) {
                ProjectAddActivity.startAdd(baseActivity)
            } else if (view_pager.currentItem == 1) {
                StoreProjectAddActivity.startAdd(baseActivity)
            }
        }
        fb_search.setOnClickListener {
            toolbar.visibility = View.GONE
            ll_search.visibility = View.VISIBLE
            et_search.postDelayed({
                et_search.inputType = InputType.TYPE_CLASS_TEXT
                et_search.isFocusable = true
                et_search.isFocusableInTouchMode = true
                et_search.requestFocus()
                Utils.toggleSoftInput(baseActivity, et_search)
            }, 100)
            if (Store.store.projectSearch(baseActivity!!).isEmpty()) {
                last_search_layout.visibility = View.GONE
            } else {
                last_search_layout.visibility = View.VISIBLE
            }
        }
        var adapter = ArrayPagerAdapter(childFragmentManager, fragments)
        view_pager.adapter = adapter
        view_pager.offscreenPageLimit = fragments.size
        var transFormer = ProjectPageTransformer()
        view_pager.setPageTransformer(true, transFormer)

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
                //Log.e("PageScrollState", "${state}")
                if (state == ViewPager.SCROLL_STATE_DRAGGING) {
                    judgeOncce = 0
                }
            }

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
                //position永远都是两个的左边一个，viewpager往右滑，positionOffset从0到1（到达1以后position++）
                //Log.e("onPageScrolled", "${position} +++ ${positionOffset} +++ ${positionOffsetPixels}")
                if (judgeOncce == 0) {
                    if (positionOffset > 0.5f) {
                        transFormer.setDirection(true)
                    } else if (positionOffset < 0.5f) {
                        transFormer.setDirection(false)
                    }
                    judgeOncce = 1
                }
            }

            //滑到新页面才算，没滑到又返回不算
            override fun onPageSelected(position: Int) {
                //Log.e("onPageSelected", "onPageSelected")
                tabs?.getTabAt(position)?.select()
                changeView()
                fb_add.visibility = if (position == 0 || position == 1) View.VISIBLE else View.GONE
//                iv_search?.visibility = if (position == 2) View.VISIBLE else View.GONE
//                iv_add?.visibility = if (position == 1 && user?.is_admin == 1) View.VISIBLE else View.GONE
                setContent()
            }

        })
        hisAdapter.dataList.clear()
        hisAdapter.dataList.addAll(search)
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

    private fun changeView() {
        if (MyNestedScrollParent2.scrollY < toolbarLayout.height) {
            tabs.setBackgroundResource(R.drawable.tab_bg_1)
            tabs.setTabTextColors(Color.parseColor("#a0a4aa"), Color.parseColor("#282828"))
            for (i in 0 until tabs.getTabCount()) {
                if (i == tabs.getSelectedTabPosition()) {
                    setDrawable(i, "1", true)
                } else {
                    setDrawable(i, "1", false)
                }
            }
        } else if (MyNestedScrollParent2.scrollY >= toolbarLayout.height) {
            tabs.setBackgroundResource(R.drawable.tab_bg_2)
            tabs.setTabTextColors(Color.parseColor("#ff7bb4fc"), Color.parseColor("#ffffff"))
            for (i in 0 until tabs.getTabCount()) {
                if (i == tabs.getSelectedTabPosition()) {
                    setDrawable(i, "2", true)
                } else {
                    setDrawable(i, "2", false)
                }
            }
        }
    }

    /**
     * @param index--------(tabs对应的index，分别对应dy,cb等)
     * @param state---------（1，2）
     * @param isSelect--------是否选中
     */
    private fun setDrawable(index: Int, state: String, isSelect: Boolean) {
        var name = ""
        when (index) {
            0 -> name += "dy_"
            1 -> name += "cb_"
            2 -> name += "lx_"
            3 -> name += "yt_"
            4 -> name += "tc_"
        }
        name += state
        if (isSelect) {
            name += "_select"
        } else {
            name += "_unselect"
        }
        val id = resources.getIdentifier(name, "drawable", context.packageName)
        tabs.getTabAt(index)!!.setIcon(id)
    }

    /**
     * 不知道为啥能成功
     */
    private fun setContent() {
        var view = fragments.get(view_pager.currentItem).getRecycleView()
        //RecycleView可能还没渲染，这样第一个setCurrentContentView失效
        view.post(object : Runnable {
            override fun run() {
                if (view.height > 0) {
                    MyNestedScrollParent2.setCurrentContentView(view)
                }
            }
        })
    }

    override fun onResume() {
        super.onResume()
        setContent()
    }

    var judgeOncce = 0

    val searchTask = Runnable {
        doSearch(search_view.search)
    }
    var key = ""
    var offset = 0
    @SuppressLint("StringFormatMatches")
    fun doSearch(text: String) {
        this.key = text
        if (TextUtils.isEmpty(key)) return
        val user = Store.store.getUser(baseActivity!!)
        val pos = tabs.selectedTabPosition
        var type = when (pos) {
            0 -> 6
            1 -> 4
            2 -> 1
            3 -> 2
            4 -> 5
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
                        Log.d("WJY", Gson().toJson(payload.payload))
                        payload?.apply {
                            tv_result_title.text = Html.fromHtml(getString(R.string.tv_title_result_project, (total as Double).toInt()))
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
                    if (Store.store.projectSearch(baseActivity!!).isEmpty()) {
                        last_search_layout.visibility = View.GONE
                    } else {
                        last_search_layout.visibility = View.VISIBLE
                    }

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