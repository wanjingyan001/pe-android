package com.sogukj.pe.ui.main

import android.annotation.SuppressLint
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.widget.LinearLayoutManager
import android.text.Html
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.TextView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.framework.base.ToolbarFragment
import com.google.gson.Gson
import com.lcodecore.tkrefreshlayout.RefreshListenerAdapter
import com.lcodecore.tkrefreshlayout.TwinklingRefreshLayout
import com.lcodecore.tkrefreshlayout.footer.BallPulseView
import com.lcodecore.tkrefreshlayout.header.progresslayout.ProgressLayout
import com.netease.nim.uikit.api.NimUIKit
import com.netease.nim.uikit.business.recent.TeamMemberAitHelper
import com.netease.nim.uikit.business.uinfo.UserInfoHelper
import com.netease.nim.uikit.common.ui.drop.DropManager
import com.netease.nim.uikit.common.ui.imageview.CircleImageView
import com.netease.nimlib.sdk.NIMClient
import com.netease.nimlib.sdk.Observer
import com.netease.nimlib.sdk.RequestCallback
import com.netease.nimlib.sdk.msg.MsgService
import com.netease.nimlib.sdk.msg.MsgServiceObserve
import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum
import com.netease.nimlib.sdk.msg.model.IMMessage
import com.netease.nimlib.sdk.msg.model.RecentContact
import com.netease.nimlib.sdk.team.constant.TeamMessageNotifyTypeEnum
import com.sogukj.pe.R
import com.sogukj.pe.bean.MessageIndexBean
import com.sogukj.pe.ui.SupportEmptyView
import com.sogukj.pe.ui.msg.MessageListActivity
import com.sogukj.pe.util.Trace
import com.sogukj.pe.util.Utils
import com.sogukj.pe.view.RecyclerAdapter
import com.sogukj.pe.view.RecyclerHolder
import com.sogukj.service.SoguApi
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_msg_center.*
import kotlinx.android.synthetic.main.toolbar.*
import org.jetbrains.anko.backgroundResource
import org.jetbrains.anko.imageResource
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

/**
 * Created by qinfei on 17/10/11.
 */
class MainMsgFragment : ToolbarFragment() {
    lateinit var recentList: ArrayList<RecentContact>
    override val containerViewId: Int
        get() = R.layout.fragment_msg_center

    lateinit var adapter: RecyclerAdapter<Any>
    val extMap = HashMap<String, Any>()


    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setTitle("消息首页")
        toolbar_back.visibility = View.VISIBLE
        adapter = RecyclerAdapter(baseActivity!!, { _adapter, parent, type ->
            val convertView = _adapter.getView(R.layout.item_msg_index, parent) as View
            object : RecyclerHolder<Any>(convertView) {
                val msgIcon = convertView.findViewById(R.id.msg_icon) as CircleImageView
                val tvTitle = convertView.findViewById(R.id.tv_title) as TextView
                val tvDate = convertView.findViewById(R.id.tv_date) as TextView
                val tvTitleMsg = convertView.findViewById(R.id.tv_title_msg) as TextView
                val tvNum = convertView.findViewById(R.id.tv_num) as TextView

                @SuppressLint("SetTextI18n")
                override fun setData(view: View, data: Any, position: Int) {
                    if (data is MessageIndexBean) {
                        if (!TextUtils.isEmpty(data.title))
                            tvTitleMsg.text = data.title
                        tvDate.text = data.time
                        tvNum.text = "${data.count}"
                        if (data.count > 0) {
                            tvNum.visibility = View.VISIBLE
                        } else {
                            tvNum.visibility = View.INVISIBLE
                        }
                        msgIcon.imageResource = R.drawable.ic_msg_alert
                    } else if (data is RecentContact) {
                        val titleName = UserInfoHelper.getUserTitleName(data.contactId, data.sessionType)
                        tvTitle.text = titleName
                        Log.d("WJY","data:${Gson().toJson(data)}")
                        if (data.sessionType == SessionTypeEnum.P2P) {
                            val value = data.msgStatus.value
                            when (value) {
                                3 -> tvTitleMsg.text = Html.fromHtml("<font color='#a0a4aa'>[已读]</font>${data.content}")
                                4 -> tvTitleMsg.text = Html.fromHtml("<font color='#1787fb'>[未读]</font>${data.content}")
                                else -> tvTitleMsg.text = data.content
                            }
                            val userInfo = NimUIKit.getUserInfoProvider().getUserInfo(data.fromAccount)
                            userInfo?.let {
                                Glide.with(this@MainMsgFragment)
                                        .load(it.avatar)
                                        .apply(RequestOptions().error(R.drawable.im_team_default).diskCacheStrategy(DiskCacheStrategy.NONE))
                                        .into(msgIcon)
                            }
                        } else if (data.sessionType == SessionTypeEnum.Team) {
                            val value = data.msgStatus.value
                            when (value) {
                                3 -> tvTitleMsg.text = Html.fromHtml("<font color='#a0a4aa'>[已读]</font>${data.fromNick}:${data.content}")
                                4 -> tvTitleMsg.text = Html.fromHtml("<font color='#1787fb'>[未读]</font>${data.fromNick}:${data.content}")
                                else -> tvTitleMsg.text = "${data.fromNick}:${data.content}"
                            }
                            msgIcon.imageResource = R.drawable.im_team_default
                        }
                        tvDate.text = Utils.getTime(data.time, "MM月dd日")
                        val mutableMap = data.extension
                        if (mutableMap != null && mutableMap.isNotEmpty() &&  mutableMap[data.contactId] == "Mute") {
                            tvNum.visibility = View.VISIBLE
                            tvNum.text = ""
                            tvNum.backgroundResource = R.drawable.im_team_shield
                        } else {
                            tvNum.backgroundResource = R.drawable.bg_tag_num
                            if (data.unreadCount > 0) {
                                tvNum.visibility = View.VISIBLE
                                tvNum.text = data.unreadCount.toString()
                            } else {
                                tvNum.visibility = View.INVISIBLE
                            }
                        }
                    }
                }

            }
        })
        adapter.onItemClick = { v, p ->
            val data = adapter.dataList[p]
            if (data is MessageIndexBean) {
                MessageListActivity.start(baseActivity)
            } else if (data is RecentContact) {
                if (NimUIKit.getAccount().isNotEmpty()) {
                    if (data.sessionType == SessionTypeEnum.P2P) {
                        NimUIKit.startP2PSession(activity, data.contactId)
                    } else if (data.sessionType == SessionTypeEnum.Team) {
                        NimUIKit.startTeamSession(activity, data.contactId)
                    }
                }
            }
        }
        val layoutManager = LinearLayoutManager(baseActivity)
        layoutManager.orientation = LinearLayoutManager.VERTICAL
//        recycler_view.addItemDecoration(DividerItemDecoration(baseActivity, DividerItemDecoration.VERTICAL))
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
        doRequest()
        toolbar_back.setOnClickListener { activity.onBackPressed() }
        registerObservers(true)
    }

    var page = 1
    fun doRequest() {
        SoguApi.getService(baseActivity!!.application)
                .msgIndex()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ payload ->
                    if (payload.isOk) {
//                        if (page == 1)
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
                    getIMRecentContact()
                })
    }

    private fun getIMRecentContact() {
        recentList = ArrayList()
        NIMClient.getService(MsgService::class.java).queryRecentContacts().setCallback(object : RequestCallback<MutableList<RecentContact>> {
            override fun onSuccess(p0: MutableList<RecentContact>?) {
                p0?.forEach { recentContact ->
                    Log.d("WJY","recentContact:${Gson().toJson(recentContact)}")
                    if (recentContact.sessionType == SessionTypeEnum.Team) {
                        extMap.clear()
                        val titleName = UserInfoHelper.getUserTitleName(recentContact.contactId, recentContact.sessionType)
                        if (titleName.isNotEmpty()) {
                            val team = NimUIKit.getTeamProvider().getTeamById(recentContact.contactId)
                            if (team.isMyTeam) {
                                extMap.put(recentContact.contactId, team.messageNotifyType)
                                recentContact.extension = extMap
                                recentList.add(recentContact)
                            }
                        }
                    } else if (recentContact.sessionType == SessionTypeEnum.P2P) {
                        recentList.add(recentContact)
                    }
                }
                Collections.sort(recentList) { o1, o2 ->
                    val time = o1.time - o2.time
                    return@sort if (time == 0L) 0 else if (time > 0) -1 else 1
                }
                adapter.dataList.addAll(recentList)
                adapter.notifyDataSetChanged()
                SupportEmptyView.checkEmpty(this@MainMsgFragment, adapter)
                refresh?.setEnableLoadmore(adapter.dataList.size % 20 == 0)
                if (page == 1)
                    refresh?.finishRefreshing()
                else
                    refresh?.finishLoadmore()
            }

            override fun onException(p0: Throwable?) {
            }

            override fun onFailed(p0: Int) {
            }

        })
    }

    private fun registerObservers(register: Boolean) {
        val service = NIMClient.getService(MsgServiceObserve::class.java)
        service.observeRecentContact(messageObserver, register)
    }


    var messageObserver: Observer<List<RecentContact>> = Observer { recentContacts ->
        onRecentContactChanged(recentContacts)
    }
    // 暂存消息，当RecentContact 监听回来时使用，结束后清掉
    private val cacheMessages = HashMap<String, Set<IMMessage>>()

    private fun onRecentContactChanged(recentContacts: List<RecentContact>) {
        var index: Int
        for (r in recentContacts) {
            index = -1
            for (i in recentList.indices) {
                if (r.contactId == recentList.get(i).getContactId() && r.sessionType == recentList.get(i).getSessionType()) {
                    index = i
                    break
                }
            }
            if (index >= 0) {
                recentList.removeAt(index)
            }
            recentList.add(r)
            if (r.sessionType == SessionTypeEnum.Team && cacheMessages[r.contactId] != null) {
                TeamMemberAitHelper.setRecentContactAited(r, cacheMessages[r.contactId])
            }
        }
        val iterator = adapter.dataList.iterator()
        while (iterator.hasNext()) {
            val next = iterator.next()
            if (next is RecentContact) {
                iterator.remove()
            }
        }
        Collections.sort(recentList) { o1, o2 ->
            val time = o1.time - o2.time
            return@sort if (time == 0L) 0 else if (time > 0) -1 else 1
        }
        adapter.dataList.addAll(recentList)
        adapter.notifyDataSetChanged()

    }

    override fun onDestroyView() {
        super.onDestroyView()
        registerObservers(false)
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