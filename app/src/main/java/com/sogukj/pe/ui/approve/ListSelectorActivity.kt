package com.sogukj.pe.ui.approve

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import com.framework.base.ToolbarActivity
import com.lcodecore.tkrefreshlayout.RefreshListenerAdapter
import com.lcodecore.tkrefreshlayout.TwinklingRefreshLayout
import com.lcodecore.tkrefreshlayout.footer.BallPulseView
import com.lcodecore.tkrefreshlayout.header.progresslayout.ProgressLayout
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.bean.CustomSealBean
import com.sogukj.pe.ui.SupportEmptyView
import com.sogukj.pe.ui.htdata.ProjectBookActivity
import com.sogukj.pe.util.Trace
import com.sogukj.pe.util.Utils
import com.sogukj.pe.view.RecyclerAdapter
import com.sogukj.pe.view.RecyclerHolder
import com.sogukj.service.SoguApi
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_list_search.*
import kotlinx.android.synthetic.main.toolbar.*
import org.jetbrains.anko.backgroundColor

/**
 * Created by qinfei on 17/10/18.
 */
class ListSelectorActivity : ToolbarActivity() {
    lateinit var adapter: RecyclerAdapter<CustomSealBean.ValueBean>
    lateinit var bean: CustomSealBean
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_list_search)
        bean = intent.getSerializableExtra(Extras.DATA) as CustomSealBean
        title = bean.name
        setBack(true)
        toolbar_menu.visibility = View.INVISIBLE
        search_bar.setCancel(false, {})
        (search_bar.tv_cancel.parent as LinearLayout).backgroundColor = Color.parseColor("#ffffff")
        adapter = RecyclerAdapter<CustomSealBean.ValueBean>(this, { _adapter, parent, type ->
            val convertView = _adapter.getView(R.layout.item_main_project_search, parent) as View
            object : RecyclerHolder<CustomSealBean.ValueBean>(convertView) {
                val tv1 = convertView.findViewById(R.id.tv1) as TextView
                val tv2 = convertView.findViewById(R.id.tv2) as TextView
                val tv3 = convertView.findViewById(R.id.tv3) as TextView

                override fun setData(view: View, data: CustomSealBean.ValueBean, position: Int) {
                    tv1.text = "${position + 1}." + data.name
                }
            }
        })
        adapter.onItemClick = { v, p ->
            val data = adapter.dataList.get(p)
            intent.putExtra(Extras.DATA, bean)
            intent.putExtra(Extras.DATA2, data)
            setResult(Activity.RESULT_OK, intent)
            finish()
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

        toolbar_menu.setOnClickListener {
            search_bar.visibility = View.VISIBLE
        }

//        search_bar.setCancel(true, {
//            page = 1
//            search_bar.visibility = View.GONE
//            search_bar.search = ""
//            doRequest()
//        })
        search_bar.onTextChange = { text ->
            page = 1
            handler.removeCallbacks(searchTask)
            handler.postDelayed(searchTask, 100)
        }
        search_bar.onSearch = { text ->
            page = 1
            doRequest()
        }
        handler.postDelayed({
            doRequest()
        }, 100)
    }

    val searchTask = Runnable {
        page = 1
        doRequest()
    }
    var page = 1
    fun doRequest() {
        val text = search_bar.search
        val map = bean.value_map
        if (null != map)
            SoguApi.getService(application)
                    .listSelector(type = map.type!!, page = page, fuzzyQuery = text)
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
                            showCustomToast(R.drawable.icon_toast_fail, payload.message)
                    }, { e ->
                        Trace.e(e)
                        showCustomToast(R.drawable.icon_toast_common, "暂无可用数据")
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

    companion object {
        val REQ_LIST_SELECTOR = 0xff0
        fun start(ctx: Activity?, bean: CustomSealBean, code: Int = REQ_LIST_SELECTOR) {
            val intent = Intent(ctx, ListSelectorActivity::class.java)
            intent.putExtra(Extras.DATA, bean)
            ctx?.startActivityForResult(intent, code)
        }
    }
}
