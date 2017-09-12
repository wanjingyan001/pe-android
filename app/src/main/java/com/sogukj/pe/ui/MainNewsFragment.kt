package com.sogukj.pe.ui

import android.os.Bundle
import android.support.design.widget.TabLayout
import android.support.v4.view.ViewPager
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.text.Html
import android.text.TextUtils
import android.view.View
import android.widget.TextView
import com.bumptech.glide.Glide
import com.framework.base.BaseFragment
import com.sogukj.pe.R
import com.sogukj.pe.bean.NewsBean
import com.sogukj.pe.util.Trace
import com.sogukj.pe.view.ArrayPagerAdapter
import com.sogukj.pe.view.FlowLayout
import com.sogukj.pe.view.RecyclerAdapter
import com.sogukj.pe.view.RecyclerHolder
import com.sogukj.service.SoguApi
import com.sogukj.util.Store
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_main_news.*
import kotlinx.android.synthetic.main.sogu_toolbar_main_news.*
import org.jetbrains.anko.find
import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by qinfei on 17/7/18.
 */
class MainNewsFragment : BaseFragment() {
    override val containerViewId: Int
        get() = R.layout.fragment_main_news //To change initializer of created properties use File | Settings | File Templates.

    val fragments = arrayOf(
            NewsListFragment.newInstance(0),
            NewsListFragment.newInstance(1),
            NewsListFragment.newInstance(2)
    )
    lateinit var adapter: RecyclerAdapter<NewsBean>
    lateinit var hisAdapter: RecyclerAdapter<String>
    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        adapter = RecyclerAdapter<NewsBean>(baseActivity!!, { _adapter, parent, type ->
            val convertView = _adapter.getView(R.layout.item_main_news, parent) as View
            object : RecyclerHolder<NewsBean>(convertView) {
                val tv_summary = convertView.findViewById(R.id.tv_summary) as TextView
                val tv_time = convertView.findViewById(R.id.tv_time) as TextView
                val tv_from = convertView.findViewById(R.id.tv_from) as TextView
                val tags = convertView.findViewById(R.id.tags) as FlowLayout


                override fun setData(view: View, data: NewsBean, position: Int) {
                    var label = data.title
                    if (!TextUtils.isEmpty(label) && !TextUtils.isEmpty(key)) {
                        label = label!!.replaceFirst(key, "<font color='#ff3300'>${key}</font>")
                    }
                    tv_summary.text = label
                    tv_time.text = data.time
                    tv_from.text = data.source
                    tags.removeAllViews()
                    data.tag?.split("#")
                            ?.forEach { str ->
                                if (!TextUtils.isEmpty(str)) {
                                    val itemRes = when (str!!) {
                                        "财务风险", "坏账增加", "经营风险",
                                        "法律风险", "财务造假", "诉讼判决",
                                        "违规违法"
                                        -> R.layout.item_tag_news_1
                                        "负面", "业绩不佳", "市场份额下降",
                                        "企业风险", "系统风险", "操作风险",
                                        "技术风险"
                                        -> R.layout.item_tag_news_2
                                        "股权转让", "人事变动", "内部重组"
                                            , "股权出售", "质押担保", "行业企业重大事件"
                                        -> R.layout.item_tag_news_3
                                        "重大" -> R.layout.item_tag_news_5
                                        else -> R.layout.item_tag_news_4
                                    }
                                    val itemTag = View.inflate(baseActivity, itemRes, null)
                                    val tvTag = itemTag.find<TextView>(R.id.tv_tag)
                                    tvTag.text = str
                                    tags.addView(itemTag)
                                }
                            }

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
                val news = adapter.getItem(p);
                NewsDetailActivity.start(baseActivity, news)
//                read(news)
            }
        }
        tv_result_title.text = Html.fromHtml(getString(R.string.tv_title_result_news, 0))

        iv_user.setOnClickListener {
            UserActivity.start(baseActivity);
        }

        Store.store.getUser(baseActivity!!)?.apply {
            if (null != url)
                Glide.with(baseActivity)
                        .load(headImage())
                        .error(R.drawable.img_user_default)
                        .into(iv_user)
        }
        iv_add.setOnClickListener {
            AddProjectActivity.start(baseActivity)
        }
        search_view.onTextChange = { text ->
            if (TextUtils.isEmpty(text)) {
                ll_history.visibility = View.VISIBLE
            } else {
                handler.removeCallbacks(searchTask)
                handler.postDelayed(searchTask, 100)
            }
        }
        search_view.setOnCancelListener {
            this.key = ""
            search_view.search = ""
            ll_search.visibility = View.GONE

            hisAdapter.dataList.clear()
            hisAdapter.dataList.addAll(Store.store.newsSearch(baseActivity!!))
            hisAdapter.notifyDataSetChanged()
            ll_history.visibility = View.VISIBLE
        }
        iv_clear.setOnClickListener {
            Store.store.newsSearchClear(baseActivity!!)
            hisAdapter.dataList.clear()
            hisAdapter.dataList.addAll(Store.store.newsSearch(baseActivity!!))
            hisAdapter.notifyDataSetChanged()
        }
        search_view.onSearch = { text ->
            if (null != text && !TextUtils.isEmpty(text))
                doSearch(text!!)
        }
        iv_search.setOnClickListener {
            ll_search.visibility = View.VISIBLE
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
        hisAdapter.dataList.clear()
        hisAdapter.dataList.addAll(Store.store.newsSearch(baseActivity!!))
        hisAdapter.notifyDataSetChanged()
        ll_history.visibility = View.VISIBLE
    }

    val searchTask = Runnable {
        doSearch(search_view.search)
    }
    var type = 1
    var key = ""
    var page = 1
    fun doSearch(text: String) {
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
                .listNews(page = 1, pageSize = 50, type = type, uid = userId, fuzzyQuery = text)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ payload ->
                    if (payload.isOk) {
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
                    tv_result_title.text = Html.fromHtml(getString(R.string.tv_title_result_news, adapter.dataList.size))

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