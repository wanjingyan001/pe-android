package com.sogukj.pe.ui.main

import android.graphics.Color
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.text.Html
import android.text.TextUtils
import android.view.View
import android.widget.TextView
import com.framework.base.ToolbarFragment
import com.lcodecore.tkrefreshlayout.RefreshListenerAdapter
import com.lcodecore.tkrefreshlayout.TwinklingRefreshLayout
import com.lcodecore.tkrefreshlayout.footer.BallPulseView
import com.lcodecore.tkrefreshlayout.header.progresslayout.ProgressLayout
import com.sogukj.pe.R
import com.sogukj.pe.bean.MessageIndexBean
import com.sogukj.pe.bean.ProjectBean
import com.sogukj.pe.ui.SupportEmptyView
import com.sogukj.pe.ui.msg.MessageListActivity
import com.sogukj.pe.util.Trace
import com.sogukj.pe.view.RecyclerAdapter
import com.sogukj.pe.view.RecyclerHolder
import com.sogukj.service.SoguApi
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_msg_center.*

/**
 * Created by qinfei on 17/10/11.
 */
class MainMsgFragment : ToolbarFragment() {
    override val containerViewId: Int
        get() = R.layout.fragment_msg_center

    lateinit var adapter: RecyclerAdapter<MessageIndexBean>
    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setTitle("消息中心")
        adapter = RecyclerAdapter<MessageIndexBean>(baseActivity!!, { _adapter, parent, type ->
            val convertView = _adapter.getView(R.layout.item_msg_index, parent) as View
            object : RecyclerHolder<MessageIndexBean>(convertView) {

                val tvTitle = convertView.findViewById(R.id.tv_title) as TextView
                val tvDate = convertView.findViewById(R.id.tv_date) as TextView
                val tvTitleMsg = convertView.findViewById(R.id.tv_title_msg) as TextView
                val tvNum = convertView.findViewById(R.id.tv_num) as TextView

                override fun setData(view: View, data: MessageIndexBean, position: Int) {
                    tvTitleMsg.text = data.title
                    tvDate.text = data.time
                    tvNum.text = "${data.count}"
                }

            }
        })
        adapter.onItemClick = { v, p ->
            MessageListActivity.start(baseActivity)
        }
        val layoutManager = LinearLayoutManager(baseActivity)
        layoutManager.orientation = LinearLayoutManager.VERTICAL
        recycler_view.addItemDecoration(DividerItemDecoration(baseActivity, DividerItemDecoration.VERTICAL))
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
                page++
                doRequest()
            }

        })
        refresh.setAutoLoadMore(true)
        handler.postDelayed({
            doRequest()
        }, 100)
    }

    var page = 1
    fun doRequest() {

        SoguApi.getService(baseActivity!!.application)
                .msgIndex()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ payload ->
                    if (payload.isOk) {
                        if (page == 1)
                            adapter.dataList.clear()
                        payload.payload?.apply {
                            adapter.dataList.add(this)
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
    }

    companion object {
        val TAG = MainMsgFragment::class.java.simpleName

        fun newInstance(): MainMsgFragment {
            val fragment = MainMsgFragment()
            val intent = Bundle()
            fragment.arguments = intent
            return fragment
        }
    }
}