package com.sogukj.pe.ui.approve

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import android.widget.TextView
import com.bumptech.glide.Glide
import com.framework.base.ToolbarActivity
import com.lcodecore.tkrefreshlayout.RefreshListenerAdapter
import com.lcodecore.tkrefreshlayout.TwinklingRefreshLayout
import com.lcodecore.tkrefreshlayout.footer.BallPulseView
import com.lcodecore.tkrefreshlayout.header.progresslayout.ProgressLayout
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.bean.LeaveRecordBean
import com.sogukj.pe.ui.SupportEmptyView
import com.sogukj.pe.util.Trace
import com.sogukj.pe.util.Utils
import com.sogukj.pe.view.CircleImageView
import com.sogukj.pe.view.RecyclerAdapter
import com.sogukj.pe.view.RecyclerHolder
import com.sogukj.pe.view.SpaceItemDecoration
import com.sogukj.service.SoguApi
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_vacation_record.*
import org.jetbrains.anko.backgroundResource

class VacationRecordActivity : ToolbarActivity() {

    lateinit var adapter: RecyclerAdapter<LeaveRecordBean>

    var typeCCQJ: Int = 0
    var id: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_vacation_record)
        title = "记录"
        setBack(true)

        typeCCQJ = intent.getIntExtra(Extras.TYPE, 0)
        id = intent.getIntExtra(Extras.ID, -1)
        id = if (id == -1) null else id

        adapter = RecyclerAdapter(context, { _adapter, parent, type ->
            val convertView = _adapter.getView(R.layout.qj_record_item, parent)
            object : RecyclerHolder<LeaveRecordBean>(convertView) {

                val icon = convertView.findViewById(R.id.icon) as CircleImageView
                val title = convertView.findViewById(R.id.title) as TextView
                val type1 = convertView.findViewById(R.id.type) as TextView
                val start_time = convertView.findViewById(R.id.start_time) as TextView
                val end_time = convertView.findViewById(R.id.end_time) as TextView
                val time = convertView.findViewById(R.id.time) as TextView
                val status = convertView.findViewById(R.id.status) as TextView

                override fun setData(view: View, data: LeaveRecordBean, position: Int) {
//                    if (data.url.isNullOrEmpty()) {
//                        icon.setImageResource(R.drawable.default_icon)
//                    } else {
//                        Glide.with(context).load(data.url).into(icon)
//                    }
                    val ch = data.title?.first()
                    icon.setChar(ch)
                    Glide.with(context).load(data.url).into(icon)
                    title.text = data.title
                    if (typeCCQJ == 1) {
                        type1.text = "请假类型：${data.name}"
                    } else {
                        type1.visibility = View.GONE
                    }
                    start_time.text = "开始时间：${data.start_time}"
                    end_time.text = "结束时间：${data.end_time}"
                    time.text = data.add_time
                    status.text = when (data.status) {//-1=>不通过，0=>待审批，1=>审批中，4=>审批通过
                        -1 -> "审批未通过"
                        0 -> "待审批"
                        1 -> "审批中"
                        4 -> "审批通过"
                        5 -> "已撤销"
                        else -> "未知状态"
                    }
                    status.backgroundResource = when (data.status) {//-1=>不通过，0=>待审批，1=>审批中，4=>审批通过
                        -1 -> R.drawable.qj_bg1
                        0 -> R.drawable.qj_bg2
                        1 -> R.drawable.qj_bg3
                        4 -> R.drawable.qj_bg
                        5 -> R.drawable.qj_bg4
                        else -> R.drawable.qj_bg
                    }
                }
            }
        })
        recycler_view.layoutManager = LinearLayoutManager(context)
        recycler_view.addItemDecoration(SpaceItemDecoration(Utils.dpToPx(context, 10)))
        recycler_view.adapter = adapter

        val header = ProgressLayout(context)
        header.setColorSchemeColors(ContextCompat.getColor(context, R.color.color_main))
        refresh.setHeaderView(header)
        val footer = BallPulseView(context)
        footer.setAnimatingColor(ContextCompat.getColor(context, R.color.color_main))
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

        doRequest()
    }

    var page = 1

    fun doRequest() {
//        var type = when(typeCCQJ){
//            1 -> 2
//            0 -> 1
//        }
        SoguApi.getService(application)
                .showLeaveTravel(user_id = id, type = typeCCQJ + 1, page = page)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ payload ->
                    if (payload.isOk) {
                        if (page == 1)
                            adapter.dataList.clear()
                        payload.payload?.apply {
                            adapter.dataList.addAll(this)
                        }
                    } else {
                        showCustomToast(R.drawable.icon_toast_fail, payload.message)
                    }
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
        //我的出差记录  user_id查看别人的	可空，空则是自己的
        //type 0--出差，1--请假
        fun start(ctx: Context, user_id: Int? = null, type: Int) {
            val intent = Intent(ctx, VacationRecordActivity::class.java)
            intent.putExtra(Extras.TYPE, type)
            intent.putExtra(Extras.ID, user_id)
            ctx.startActivity(intent)
        }
    }
}
