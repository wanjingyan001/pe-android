package com.sogukj.pe.ui.news

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v4.graphics.drawable.RoundedBitmapDrawable
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.text.Html
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.transition.Transition
import com.framework.base.BaseFragment
import com.lcodecore.tkrefreshlayout.RefreshListenerAdapter
import com.lcodecore.tkrefreshlayout.TwinklingRefreshLayout
import com.lcodecore.tkrefreshlayout.footer.BallPulseView
import com.lcodecore.tkrefreshlayout.header.progresslayout.ProgressLayout
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.bean.NewsBean
import com.sogukj.pe.ui.SupportEmptyView
import com.sogukj.pe.util.Trace
import com.sogukj.pe.util.Utils
import com.sogukj.pe.view.FlowLayout
import com.sogukj.pe.view.RecyclerAdapter
import com.sogukj.pe.view.RecyclerHolder
import com.sogukj.pe.view.SpaceItemDecoration
import com.sogukj.service.SoguApi
import com.sogukj.util.Store
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_list_news.*
import kotlinx.android.synthetic.main.layout_empty.*
import kotlinx.android.synthetic.main.layout_loading.*
import org.jetbrains.anko.textColor
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

/**
 * Created by qinfei on 17/7/18.
 */
class NewsListFragment : BaseFragment(), SupportEmptyView {
    override val containerViewId: Int
        get() = R.layout.fragment_list_news //To change initializer of created properties use File | Settings | File Templates.

    lateinit var adapter: RecyclerAdapter<NewsBean>
    var index: Int = 0
    var type: Int? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        index = arguments.getInt(Extras.INDEX)
        type = when (index) {
            0 -> null
            1 -> 3
            2 -> 1
            else -> null
        }
        Store.store.getRead(baseActivity!!)
    }

    fun isRead(data: NewsBean)
            = (Store.store.readList.contains("${KEY_NEWS}${data.data_id}"))

    fun read(data: NewsBean) {
        var readList = HashSet<String>();
        readList.addAll(Store.store.readList)
        readList.add("${KEY_NEWS}${data.data_id}")
        Store.store.setRead(baseActivity!!, readList)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

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
                filter()
            }
        }

        adapter = RecyclerAdapter<NewsBean>(baseActivity!!, { _adapter, parent, type ->
            NewsHolder(_adapter.getView(R.layout.item_main_news, parent))
        })
        adapter.onItemClick = { v, p ->
            val news = adapter.getItem(p);
            NewsDetailActivity.start(baseActivity, news)
            read(news)
        }
        val layoutManager = LinearLayoutManager(baseActivity)
        layoutManager.orientation = LinearLayoutManager.VERTICAL
        //recycler_view.addItemDecoration(DividerItemDecoration(baseActivity, DividerItemDecoration.VERTICAL))
        recycler_view.addItemDecoration(SpaceItemDecoration(Utils.dpToPx(context, 10)))
        recycler_view.layoutManager = layoutManager
        recycler_view.adapter = adapter

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
                doRequest()
            }

            override fun onLoadMore(refreshLayout: TwinklingRefreshLayout?) {
                ++page
                doRequest()
            }

        })
        refresh.setAutoLoadMore(true)
        Glide.with(baseActivity)
                .load(Uri.parse("file:///android_asset/img_loading.gif"))
                .into(iv_loading)
        iv_loading?.visibility = View.VISIBLE
        handler.postDelayed({
            doRequest()
        }, 100)
    }

//    fun onItemClick(news: NewsBean) {
//        when (news.table_id) {
//            else -> NewsDetailActivity.start(baseActivity)
//        }
//    }

    override fun onStart() {
        super.onStart()
        doRequest()
    }

    var initialData = ArrayList<NewsBean>()

    var page = 1
    fun doRequest() {
        val user = Store.store.getUser(baseActivity!!)
        val userId = if (index == 0) null else user?.uid
        SoguApi.getService(baseActivity!!.application)
                .listNews(page = page, type = type, uid = userId)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ payload ->
                    if (payload.isOk) {
                        if (page == 1) {
                            initialData.clear()
                            adapter.dataList.clear()
                        }
                        payload.payload?.apply {
                            initialData.addAll(this)
                            filter()
                            //adapter.dataList.addAll(this)
                        }
                    } else
                        showToast(payload.message)
                    iv_loading?.visibility = View.GONE
                }, { e ->
                    Trace.e(e)
//                    showToast("暂无可用数据")
                    iv_loading?.visibility = View.GONE
                    SupportEmptyView.checkEmpty(this, adapter)
                }, {
                    SupportEmptyView.checkEmpty(this, adapter)
                    refresh?.setEnableLoadmore(adapter.dataList.size % 20 == 0)
                    adapter.notifyDataSetChanged()
                    if (page == 1)
                        refresh?.finishRefreshing()
                    else
                        refresh?.finishLoadmore()
                })
    }

    private fun filter() {
        //获取gridlayout中的tag
        var tagList = ArrayList<String>()
        for (i in 0 until grid.childCount) {
            var child = grid.getChildAt(i) as TextView
            if (child.tag.equals("T")) {
                tagList.add(child.text.toString())
            }
        }

        //过滤tag
        var filterData = ArrayList<NewsBean>()
        if (tagList.size == 0) {
            adapter.dataList.clear()
            adapter.dataList.addAll(initialData)
            adapter.notifyDataSetChanged()
            iv_empty.visibility = View.GONE
        } else {
            for (bean in initialData) {
                var tags = bean.tag?.split("#")
                if (tags!!.containsAll(tagList)) {
                    filterData.add(bean)
                }
            }
            adapter.dataList.clear()
            adapter.dataList.addAll(filterData)
            adapter.notifyDataSetChanged()
            if (filterData.size == 0) {
                iv_empty.visibility = View.VISIBLE
            } else {
                iv_empty.visibility = View.GONE
            }
        }
    }

    val fmt = SimpleDateFormat("yyyy/MM/dd HH:mm")
    val KEY_NEWS = "news"

    inner class NewsHolder(view: View)
        : RecyclerHolder<NewsBean>(view) {
        val tv_summary = convertView.findViewById(R.id.tv_summary) as TextView
        val tv_time = convertView.findViewById(R.id.tv_time) as TextView
        val tv_from = convertView.findViewById(R.id.tv_from) as TextView
        val tags = convertView.findViewById(R.id.tags) as FlowLayout
        val tv_date = convertView.findViewById(R.id.tv_date) as TextView
        val iv_icon = convertView.findViewById(R.id.imageIcon) as ImageView


        override fun setData(view: View, data: NewsBean, position: Int) {
            var label = data.title
//            if (!TextUtils.isEmpty(label) && !TextUtils.isEmpty(key)) {
//                label = label!!.replaceFirst(key, "<font color='#ff3300'>${key}</font>")
//            }
            tv_summary.text = Html.fromHtml(label)
            val strTime = data.time
            tv_time.visibility = View.GONE
            if (!TextUtils.isEmpty(strTime)) {
                val strs = strTime!!.trim().split(" ")
//                if (!TextUtils.isEmpty(strs.getOrNull(1))) {
//                    tv_time.visibility = View.VISIBLE
//                }
                tv_date.text = strs
                        .getOrNull(0)
                tv_time.text = strs
                        .getOrNull(1)
            }
            tv_from.text = data.source
            data.setTags(baseActivity!!, tags)
            var isRead = isRead(data)
            if (isRead) {
                tv_summary.textColor = resources.getColor(R.color.text_2)
            } else {
                tv_summary.textColor = resources.getColor(R.color.text_1)
            }

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

        }

    }

    companion object {
        val TAG = NewsListFragment::class.java.simpleName

        fun newInstance(idx: Int): NewsListFragment {
            val fragment = NewsListFragment()
            val intent = Bundle()
            intent.putInt(Extras.INDEX, idx)
            fragment.arguments = intent
            return fragment
        }
    }
}