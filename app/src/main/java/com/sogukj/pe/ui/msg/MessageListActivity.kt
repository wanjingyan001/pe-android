package com.sogukj.pe.ui.msg

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import com.framework.base.ToolbarActivity
import com.lcodecore.tkrefreshlayout.RefreshListenerAdapter
import com.lcodecore.tkrefreshlayout.TwinklingRefreshLayout
import com.lcodecore.tkrefreshlayout.footer.BallPulseView
import com.lcodecore.tkrefreshlayout.header.progresslayout.ProgressLayout
import com.sogukj.pe.R
import com.sogukj.pe.bean.MessageBean
import com.sogukj.pe.ui.SupportEmptyView
import com.sogukj.pe.ui.approve.SealApproveActivity
import com.sogukj.pe.ui.approve.SignApproveActivity
import com.sogukj.pe.util.Trace
import com.sogukj.pe.view.RecyclerAdapter
import com.sogukj.pe.view.RecyclerHolder
import com.sogukj.service.SoguApi
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_list_common.*

class MessageListActivity : ToolbarActivity() {

    lateinit var adapter: RecyclerAdapter<MessageBean>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_list_common)
        title = "消息助手"
        setBack(true)
        val inflater = LayoutInflater.from(this)
        adapter = RecyclerAdapter<MessageBean>(this, { _adapter, parent, type ->
            val convertView = _adapter.getView(R.layout.item_msg_content, parent)
            object : RecyclerHolder<MessageBean>(convertView) {
                val tvTime = convertView.findViewById(R.id.tv_time) as TextView
                val tvTitle = convertView.findViewById(R.id.tv_title) as TextView
                val tvNum = convertView.findViewById(R.id.tv_num) as TextView
                val tvState = convertView.findViewById(R.id.tv_state) as TextView
                val tvFrom = convertView.findViewById(R.id.tv_from) as TextView
                val tvType = convertView.findViewById(R.id.tv_type) as TextView
                val tvMsg = convertView.findViewById(R.id.tv_msg) as TextView
                override fun setData(view: View, data: MessageBean, position: Int) {

                    val strType = when (data.type) {
                        1 -> "出勤休假"
                        2 -> "用印审批"
                        3 -> "签字审批"
                        else -> ""
                    }
                    tvTitle.text = "${strType} No.${data.approval_id}"
                    tvState.text = if (data.status == 1) "已审批" else "待审批"
                    tvTime.text = data.time
                    tvFrom.text = "发起人:" + data.username
                    tvType.text = "类型:" + data.type_name
                    tvMsg.text = data.title
                    val cnt = data.message_count
                    tvNum.text = "${cnt}"
                    if (cnt != null && cnt > 0)
                        tvNum.visibility = View.VISIBLE
                    else
                        tvNum.visibility = View.GONE

                }

            }
        })
        adapter.onItemClick = { v, p ->
            val data = adapter.dataList.get(p)
            if (data.type == 2)
                SealApproveActivity.start(this, data)
            else if (data.type == 3)
                SignApproveActivity.start(this, data)
        }
        val layoutManager = LinearLayoutManager(this)
        layoutManager.orientation = LinearLayoutManager.VERTICAL
//        recycler_view.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))
        recycler_view.layoutManager = layoutManager
        recycler_view.adapter = adapter

        val header = ProgressLayout(this)
        header.setColorSchemeColors(ContextCompat.getColor(this, R.color.color_main))
        refresh.setHeaderView(header)
        val footer = BallPulseView(this)
        footer.setAnimatingColor(ContextCompat.getColor(this, R.color.color_main))
        refresh.setBottomView(footer)
        refresh.setOverScrollRefreshShow(false)
        refresh.setEnableLoadmore(false)
//        refresh.setAutoLoadMore(false)
        refresh.setOnRefreshListener(object : RefreshListenerAdapter() {
            override fun onRefresh(refreshLayout: TwinklingRefreshLayout?) {
                page = 1
                doRequest()
            }

            override fun onLoadMore(refreshLayout: TwinklingRefreshLayout?) {
                refreshLayout?.finishLoadmore()
            }

        })
        handler.postDelayed({
            doRequest()
        }, 100)
    }

    var page = 1
    fun doRequest() {
        SoguApi.getService(application)
                .msgList()
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
    }

    companion object {
        fun start(ctx: Activity?) {
            val intent = Intent(ctx, MessageListActivity::class.java)
            ctx?.startActivity(intent)
        }
    }
}
