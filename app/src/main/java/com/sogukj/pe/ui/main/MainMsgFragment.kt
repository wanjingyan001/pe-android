package com.sogukj.pe.ui.main

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.widget.LinearLayoutManager
import android.text.Html
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.TextView
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.Theme
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.framework.base.BaseFragment
import com.framework.base.ToolbarFragment
import com.lcodecore.tkrefreshlayout.RefreshListenerAdapter
import com.lcodecore.tkrefreshlayout.TwinklingRefreshLayout
import com.lcodecore.tkrefreshlayout.footer.BallPulseView
import com.lcodecore.tkrefreshlayout.header.progresslayout.ProgressLayout
import com.netease.nim.uikit.api.NimUIKit
import com.netease.nim.uikit.business.recent.TeamMemberAitHelper
import com.netease.nim.uikit.business.uinfo.UserInfoHelper
import com.netease.nim.uikit.common.ui.imageview.CircleImageView
import com.netease.nimlib.sdk.NIMClient
import com.netease.nimlib.sdk.Observer
import com.netease.nimlib.sdk.RequestCallback
import com.netease.nimlib.sdk.msg.MsgService
import com.netease.nimlib.sdk.msg.MsgServiceObserve
import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum
import com.netease.nimlib.sdk.msg.model.IMMessage
import com.netease.nimlib.sdk.msg.model.RecentContact
import com.sogukj.pe.R
import com.sogukj.pe.bean.MessageIndexBean
import com.sogukj.pe.ui.IM.TeamSearchActivity
import com.sogukj.pe.ui.IM.TeamSelectActivity
import com.sogukj.pe.ui.SupportEmptyView
import com.sogukj.pe.ui.msg.MessageListActivity
import com.sogukj.pe.ui.user.UserActivity
import com.sogukj.pe.util.Trace
import com.sogukj.pe.util.Utils
import com.sogukj.pe.view.RecyclerAdapter
import com.sogukj.pe.view.RecyclerHolder
import com.sogukj.service.SoguApi
import com.sogukj.util.Store
import com.xuexuan.zxing.android.activity.CaptureActivity
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_msg_center.*
import org.jetbrains.anko.backgroundResource
import org.jetbrains.anko.imageResource
import org.jetbrains.anko.support.v4.ctx
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

/**
 * Created by qinfei on 17/10/11.
 */
class MainMsgFragment : BaseFragment() {
    lateinit var recentList: ArrayList<RecentContact>
    override val containerViewId: Int
        get() = R.layout.fragment_msg_center

    lateinit var adapter: RecyclerAdapter<Any>
    val extMap = HashMap<String, Any>()

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        toolbar_title.text = "消息首页"

        val user = Store.store.getUser(baseActivity!!)
        if (user?.url.isNullOrEmpty()) {
            toolbar_back.setChar(user?.name?.first())
        } else {
            Glide.with(context).load(user?.url).into(toolbar_back)
        }
        toolbar_back.setOnClickListener {
            UserActivity.start(context)
        }

        toolbar_menu.setOnClickListener {
            if (add_layout.visibility == View.VISIBLE) {
                add_layout.visibility = View.GONE
                refresh.visibility = View.VISIBLE
            } else {
                add_layout.visibility = View.VISIBLE
                refresh.visibility = View.GONE
            }
        }
        start_chat.setOnClickListener {
            TeamSelectActivity.start(context, isSelectUser = true, isCreateTeam = true)
        }
        scan.setOnClickListener {
            val openCameraIntent = Intent(context, CaptureActivity::class.java)
            startActivityForResult(openCameraIntent, 0)
        }

        adapter = RecyclerAdapter(baseActivity!!, { _adapter, parent, type ->
            val convertView = _adapter.getView(R.layout.item_msg_index, parent)
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
                        else
                            tvTitleMsg.text = "暂无数据"
                        tvDate.text = data.time
                        tvNum.text = "${data.count}"
                        tvTitle.text = "系统消息助手"
                        if (data.count > 0) {
                            tvNum.visibility = View.VISIBLE
                        } else {
                            tvNum.visibility = View.INVISIBLE
                        }
                        msgIcon.imageResource = R.drawable.ic_msg_alert
                    } else if (data is RecentContact) {
                        val titleName = UserInfoHelper.getUserTitleName(data.contactId, data.sessionType)
                        tvTitle.text = titleName
                        if (data.sessionType == SessionTypeEnum.P2P) {
                            val value = data.msgStatus.value
                            when (value) {
                                3 -> tvTitleMsg.text = Html.fromHtml("<font color='#a0a4aa'>[已读]</font>${data.content}")
                                4 -> tvTitleMsg.text = Html.fromHtml("<font color='#1787fb'>[未读]</font>${data.content}")
                                else -> tvTitleMsg.text = data.content
                            }
                            val userInfo = NimUIKit.getUserInfoProvider().getUserInfo(data.contactId)
                            userInfo?.let {
                                Glide.with(this@MainMsgFragment)
                                        .load(it.avatar)
                                        .apply(RequestOptions().error(R.drawable.ewm))
                                        .into(msgIcon)
                            }
                        } else if (data.sessionType == SessionTypeEnum.Team) {
                            val value = data.msgStatus.value
                            val fromNick = if (data.fromNick.isEmpty()) "" else "${data.fromNick}:"
                            when (value) {
                                3 -> tvTitleMsg.text = Html.fromHtml("<font color='#a0a4aa'>[已读]</font>$fromNick${data.content}")
                                4 -> tvTitleMsg.text = Html.fromHtml("<font color='#1787fb'>[未读]</font>$fromNick${data.content}")
                                else -> tvTitleMsg.text = "$fromNick${data.content}"
                            }
                            msgIcon.imageResource = R.drawable.im_team_default
                        }
                        try {
//                            tvDate.text = Utils.getTimeDate(data.time)
                            val time = Utils.getTime(data.time, "yyyy-MM-dd HH:mm:ss")
                            tvDate.text = Utils.formatDate(time)
                        } catch (e: Exception) {
                        }
                        val mutableMap = data.extension
                        if (mutableMap != null && mutableMap.isNotEmpty() && mutableMap[data.contactId] == "Mute") {
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
        adapter.onItemLongClick = { v, positon ->
            val data = adapter.dataList[positon]
            if (data is RecentContact) {
                val top = if (isTagSet(data, RECENT_TAG_STICKY)) "取消置顶" else "置顶该聊天"
                MaterialDialog.Builder(ctx)
                        .theme(Theme.LIGHT)
                        .items(mutableListOf(top, "删除"))
                        .itemsCallback { dialog, itemView, position, text ->
                            when (position) {
                                0 -> {
                                    if (isTagSet(data, RECENT_TAG_STICKY)) {
                                        removeTag(data, RECENT_TAG_STICKY)
                                    } else {
                                        addTag(data, RECENT_TAG_STICKY)
                                    }
                                    NIMClient.getService(MsgService::class.java).updateRecent(data)
                                    doRequest()
                                }
                                1 -> {
                                    deleteRecentContact(data.contactId, data.sessionType)
                                }
                            }
                        }
                        .show()
            }
            true
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

        registerObservers(true)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            val bundle = data!!.extras
            val scanResult = bundle!!.getString("result")
            Log.e("11111111111", scanResult)
        }
    }

    var page = 1
    override fun onResume() {
        super.onResume()
        doRequest()
    }

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
                        showCustomToast(R.drawable.icon_toast_fail, payload.message)
                }, { e ->
                    Trace.e(e)
                    showCustomToast(R.drawable.icon_toast_common, "暂无可用数据")
                }, {
                    getIMRecentContact()
                })
    }

    private fun getIMRecentContact() {
        recentList = ArrayList()
        NIMClient.getService(MsgService::class.java).queryRecentContacts().setCallback(object : RequestCallback<MutableList<RecentContact>> {
            override fun onSuccess(p0: MutableList<RecentContact>?) {
                p0?.forEach { recentContact ->
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
                    // 先比较置顶tag
                    val sticky = (o1.tag and RECENT_TAG_STICKY) - (o2.tag and RECENT_TAG_STICKY)
                    if (sticky != 0L) {
                        return@sort if (sticky > 0) -1 else 1
                    } else {
                        val time = o1.time - o2.time
                        return@sort if (time == 0L) 0 else if (time > 0) -1 else 1
                    }
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

    private fun deleteRecentContact(account: String, sessionType: SessionTypeEnum) {
        NIMClient.getService(MsgService::class.java).deleteRecentContact2(account, sessionType)
    }

    private fun registerObservers(register: Boolean) {
        val service = NIMClient.getService(MsgServiceObserve::class.java)
        service.observeRecentContact(messageObserver, register)
        service.observeRecentContactDeleted(deleteObserver, register)
    }


    private var messageObserver: Observer<List<RecentContact>> = Observer { recentContacts ->
        onRecentContactChanged(recentContacts)
    }

    private var deleteObserver: Observer<RecentContact> = Observer { recentContact ->
        doRequest()
    }

    // 暂存消息，当RecentContact 监听回来时使用，结束后清掉
    private val cacheMessages = HashMap<String, Set<IMMessage>>()

    private fun onRecentContactChanged(recentContacts: List<RecentContact>) {
        var index: Int
        for (r in recentContacts) {
            index = -1
            for (i in recentList.indices) {
                if (r.contactId == recentList[i].contactId && r.sessionType == recentList[i].sessionType) {
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

    private fun addTag(recent: RecentContact, tag: Long) {
        recent.tag = recent.tag or tag
    }

    private fun removeTag(recent: RecentContact, tag: Long) {
        recent.tag = recent.tag and tag.inv()
    }

    private fun isTagSet(recent: RecentContact, tag: Long) = recent.tag and tag == tag


    companion object {
        val TAG = MainMsgFragment::class.java.simpleName
        val RECENT_TAG_STICKY = 1L

        fun newInstance(): MainMsgFragment {
            val fragment = MainMsgFragment()
            val intent = Bundle()
            fragment.arguments = intent
            return fragment
        }
    }
}