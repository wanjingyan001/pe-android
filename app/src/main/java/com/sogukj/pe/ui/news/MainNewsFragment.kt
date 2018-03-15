package com.sogukj.pe.ui.news

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Bundle
import android.support.design.widget.TabLayout
import android.support.v4.content.ContextCompat
import android.support.v4.graphics.drawable.RoundedBitmapDrawable
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory
import android.support.v4.view.ViewPager
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.text.Html
import android.text.InputType
import android.text.TextUtils
import android.view.View
import android.widget.ImageView
import android.widget.RadioGroup
import android.widget.TextView
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.Theme
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.transition.Transition
import com.framework.base.BaseFragment
import com.lcodecore.tkrefreshlayout.RefreshListenerAdapter
import com.lcodecore.tkrefreshlayout.TwinklingRefreshLayout
import com.lcodecore.tkrefreshlayout.footer.BallPulseView
import com.lcodecore.tkrefreshlayout.header.progresslayout.ProgressLayout
import com.sogukj.pe.R
import com.sogukj.pe.bean.NewsBean
import com.sogukj.pe.ui.main.MainActivity
import com.sogukj.pe.ui.project.ProjectAddActivity
import com.sogukj.pe.util.Trace
import com.sogukj.pe.util.Utils
import com.sogukj.pe.view.ArrayPagerAdapter
import com.sogukj.pe.view.FlowLayout
import com.sogukj.pe.view.RecyclerAdapter
import com.sogukj.pe.view.RecyclerHolder
import com.sogukj.service.SoguApi
import com.sogukj.util.Store
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_main_news.*
import kotlinx.android.synthetic.main.search_view.*
import org.jetbrains.anko.find
import org.jetbrains.anko.textColor
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

/**
 * Created by qinfei on 17/7/18.
 */
class MainNewsFragment : BaseFragment() {
    override val containerViewId: Int
        get() = R.layout.fragment_main_news //To change initializer of created properties use File | Settings | File Templates.

    val fragments = arrayOf(
            NewsListFragment.newInstance(0),
            NewsListFragment.newInstance(1)
            //NewsListFragment.newInstance(2)
    )
    lateinit var adapter: RecyclerAdapter<NewsBean>
    lateinit var hisAdapter: RecyclerAdapter<String>
    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //toolbar_back.visibility = View.GONE
        Utils.setUpIndicatorWidth(tabs, 8, 8, context)
        adapter = RecyclerAdapter<NewsBean>(baseActivity!!, { _adapter, parent, type ->
            val convertView = _adapter.getView(R.layout.item_main_news, parent) as View
            object : RecyclerHolder<NewsBean>(convertView) {
                val tv_summary = convertView.findViewById(R.id.tv_summary) as TextView
                val tv_time = convertView.findViewById(R.id.tv_time) as TextView
                val tv_from = convertView.findViewById(R.id.tv_from) as TextView
                val tags = convertView.findViewById(R.id.tags) as FlowLayout
                val tv_date = convertView.findViewById(R.id.tv_date) as TextView
                val iv_icon = convertView.findViewById(R.id.imageIcon) as ImageView

                override fun setData(view: View, data: NewsBean, position: Int) {
                    if (data.url.isNullOrEmpty()) {
                        var bitmap = BitmapFactory.decodeResource(resources, R.drawable.default_icon)
                        var draw = RoundedBitmapDrawableFactory.create(resources, bitmap) as RoundedBitmapDrawable
                        draw.setCornerRadius(Utils.dpToPx(context, 4).toFloat())
                        iv_icon.setBackgroundDrawable(draw)
                    } else {
                        Glide.with(context).asBitmap().load(data.url).into(object : SimpleTarget<Bitmap>() {
                            override fun onResourceReady(bitmap: Bitmap?, glideAnimation: Transition<in Bitmap>?) {
                                var draw = RoundedBitmapDrawableFactory.create(resources, bitmap) as RoundedBitmapDrawable
                                draw.setCornerRadius(Utils.dpToPx(context, 4).toFloat())
                                iv_icon.setBackgroundDrawable(draw)
                            }
                        })
                    }
                    var label = data.title
                    if (!TextUtils.isEmpty(label) && !TextUtils.isEmpty(key)) {
                        label = label!!.replaceFirst(key, "<font color='#ff3300'>${key}</font>")
                    }
                    tv_summary.text = Html.fromHtml(label)
                    val strTime = data.time
                    tv_time.visibility = View.GONE
                    if (!TextUtils.isEmpty(strTime)) {
                        val strs = strTime!!.trim().split(" ")
                        if (!TextUtils.isEmpty(strs.getOrNull(1))) {
                            tv_time.visibility = View.VISIBLE
                        }
                        tv_date.text = strs
                                .getOrNull(0)
                        tv_time.text = strs
                                .getOrNull(1)
                    }
                    tv_from.text = data.source

                    data.setTags(baseActivity!!, tags)

//                    var isRead = isRead(data)
//                    if (isRead) {
//                        tv_summary.textColor = resources.getColor(R.color.text_3)
//                        tv_time.textColor = resources.getColor(R.color.text_3)
//                        tv_from.textColor = resources.getColor(R.color.text_3)
//                    } else {
//                        tv_summary.textColor = resources.getColor(R.color.text_1)
//                        tv_time.textColor = resources.getColor(R.color.text_2)
//                        tv_from.textColor = resources.getColor(R.color.text_2)
//                    }
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
                        Store.store.newsSearchRemover(baseActivity!!, position)
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
                val news = adapter.getItem(p);
                NewsDetailActivity.start(baseActivity, news)
//                read(news)
            }
        }
        tv_result_title.text = Html.fromHtml(getString(R.string.tv_title_result_news, 0))

//        iv_user.setOnClickListener {
//            val activity = activity as MainActivity
//            activity.find<RadioGroup>(R.id.rg_tab_main).check(R.id.rb_my)
////            UserFragment.start(baseActivity);
//        }
//
//        Store.store.getUser(baseActivity!!)?.apply {
//            if (null != url)
//                Glide.with(baseActivity)
//                        .load(headImage())
//                        .error(R.drawable.img_logo_user)
//                        .into(iv_user)
//        }
//        iv_add.setOnClickListener {
//            ProjectAddActivity.startAdd(baseActivity)
//        }
        //不做实时查询,只有点击软键盘上的查询时才进行查询
//        search_view.onTextChange = { text ->
//            if (TextUtils.isEmpty(text)) {
//                ll_history.visibility = View.VISIBLE
//            } else {
//                page = 1
//                handler.removeCallbacks(searchTask)
//                handler.postDelayed(searchTask, 100)
//            }
//        }
        search_view.tv_cancel.visibility = View.VISIBLE
        search_view.tv_cancel.setOnClickListener {
            this.key = ""
            search_view.search = ""
            ll_search.visibility = View.GONE

            hisAdapter.dataList.clear()
            hisAdapter.dataList.addAll(Store.store.newsSearch(baseActivity!!))
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
                        Store.store.newsSearchClear(baseActivity!!)
                        hisAdapter.dataList.clear()
                        hisAdapter.dataList.addAll(Store.store.newsSearch(baseActivity!!))
                        hisAdapter.notifyDataSetChanged()
                        last_search_layout.visibility = View.GONE
                    }
                    .show()
        }
        search_view.onSearch = { text ->
            if (null != text && !TextUtils.isEmpty(text))
                doSearch(text!!)
        }
        iv_search.setOnClickListener {
            ll_search.visibility = View.VISIBLE
            et_search.postDelayed({
                et_search.inputType = InputType.TYPE_CLASS_TEXT
                et_search.isFocusable = true
                et_search.isFocusableInTouchMode = true
                et_search.requestFocus()
                Utils.toggleSoftInput(baseActivity, et_search)
            }, 100)
            if (Store.store.newsSearch(baseActivity!!).isEmpty()) {
                last_search_layout.visibility = View.GONE
            } else {
                last_search_layout.visibility = View.VISIBLE
            }
        }
        var adapter = ArrayPagerAdapter(childFragmentManager, fragments)
        view_pager.adapter = adapter
        tabs?.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabReselected(tab: TabLayout.Tab?) {

            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {

            }

            override fun onTabSelected(tab: TabLayout.Tab) {
                view_pager?.currentItem = tab.position
                resetGrid()
            }

        })
        view_pager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) {
            }

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
            }

            override fun onPageSelected(position: Int) {
                tabs?.getTabAt(position)?.select()
            }

        })
        val search = Store.store.newsSearch(baseActivity!!)
        hisAdapter.dataList.clear()
        hisAdapter.dataList.addAll(search)
        hisAdapter.notifyDataSetChanged()
        ll_history.visibility = View.VISIBLE


        val header = ProgressLayout(baseActivity)
        header.setColorSchemeColors(ContextCompat.getColor(baseActivity, R.color.color_main))
        refresh.setHeaderView(header)
        val footer = BallPulseView(baseActivity)
        footer.setAnimatingColor(ContextCompat.getColor(baseActivity, R.color.color_main))
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

        iv_filter.setOnClickListener {
            if (fl_filter.visibility == View.GONE) {
                view_pager.visibility = View.GONE
                fl_filter.visibility = View.VISIBLE
            } else {
                view_pager.visibility = View.VISIBLE
                fl_filter.visibility = View.GONE
            }
        }

        btn_reset.setOnClickListener {
            resetGrid()
        }

        btn_ok.setOnClickListener {
            fragments[view_pager.currentItem].setTagList(getTagList())
            view_pager.visibility = View.VISIBLE
            fl_filter.visibility = View.GONE
        }

        for (i in 0 until grid.childCount) {
            var child = grid.getChildAt(i) as TextView
            child.setOnClickListener {
                if (child.tag.equals("F")) {
                    child.textColor = Color.parseColor("#1787fb")
                    child.setBackgroundResource(R.drawable.tg_bg_t)
                    child.tag = "T"
                } else {
                    child.textColor = Color.parseColor("#808080")
                    child.setBackgroundResource(R.drawable.tag_bg_f)
                    child.tag = "F"
                }
            }
        }
    }

    fun resetGrid() {
        fragments[view_pager.currentItem].setTagList(ArrayList<String>())
        view_pager.visibility = View.VISIBLE
        fl_filter.visibility = View.GONE
        for (i in 0 until grid.childCount) {
            var child = grid.getChildAt(i) as TextView
            child.textColor = Color.parseColor("#808080")
            child.setBackgroundResource(R.drawable.tag_bg_f)
            child.tag = "F"
        }
    }

    fun getTagList(): ArrayList<String> {
        //获取gridlayout中的tag
        var tagList = ArrayList<String>()
        for (i in 0 until grid.childCount) {
            var child = grid.getChildAt(i) as TextView
            if (child.tag.equals("T")) {
                tagList.add(child.text.toString())
            }
        }
        return tagList
    }

    val searchTask = Runnable {
        doSearch(search_view.search)
    }
    var type = 1
    var key = ""
    var page = 1
    fun doSearch(text: String) {
        this.key = text
        if (TextUtils.isEmpty(key)) return
        val user = Store.store.getUser(baseActivity!!)
        val userId = if (tabs.selectedTabPosition == 0) null else user?.uid
        var type = when (tabs.selectedTabPosition) {
            0 -> null
            1 -> 3
            2 -> 1
            else -> null
        }
        val tmplist = LinkedList<String>()
        tmplist.add(text)
        Store.store.newsSearch(baseActivity!!, tmplist)
        SoguApi.getService(baseActivity!!.application)
                .listNews(page = page, pageSize = 20, type = type, uid = userId, fuzzyQuery = text)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ payload ->
                    if (payload.isOk) {
                        if (page == 1)
                            adapter.dataList.clear()
                        payload.payload?.apply {
                            adapter.dataList.addAll(this)
                        }
                        payload?.apply {
                            tv_result_title.text = Html.fromHtml(getString(R.string.tv_title_result_news, adapter.dataList.size))
                        }
                    } else
                        showToast(payload.message)
                }, { e ->
                    Trace.e(e)
                }, {
                    ll_history.visibility = View.GONE
                    adapter.notifyDataSetChanged()
                    if (page == 1)
                        refresh?.finishRefreshing()
                    else
                        refresh?.finishLoadmore()

                    hisAdapter.dataList.clear()
                    hisAdapter.dataList.addAll(Store.store.newsSearch(baseActivity!!))
                    hisAdapter.notifyDataSetChanged()
                })
    }

    val fmt = SimpleDateFormat("yyyy/MM/dd HH:mm")
    val KEY_NEWS = "news"

    companion object {
        val TAG = MainNewsFragment::class.java.simpleName

        fun newInstance(): MainNewsFragment {
            val fragment = MainNewsFragment()
            val intent = Bundle()
            fragment.arguments = intent
            return fragment
        }
    }


}