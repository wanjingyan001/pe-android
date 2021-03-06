package com.sogukj.pe.ui.main

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.support.design.widget.AppBarLayout
import android.support.v4.content.ContextCompat
import android.support.v7.widget.LinearLayoutManager
import android.text.Editable
import android.text.Html
import android.text.TextUtils
import android.text.TextWatcher
import android.util.Log
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import android.widget.Toolbar
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.Theme
import com.amap.api.mapcore.util.it
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import com.framework.base.BaseFragment
import com.framework.base.ToolbarFragment
import com.google.gson.Gson
import com.lcodecore.tkrefreshlayout.RefreshListenerAdapter
import com.lcodecore.tkrefreshlayout.TwinklingRefreshLayout
import com.lcodecore.tkrefreshlayout.footer.BallPulseView
import com.lcodecore.tkrefreshlayout.header.progresslayout.ProgressLayout
import com.netease.nim.uikit.api.NimUIKit
import com.netease.nim.uikit.business.recent.TeamMemberAitHelper
import com.netease.nim.uikit.business.uinfo.UserInfoHelper
import com.netease.nimlib.sdk.NIMClient
import com.netease.nimlib.sdk.Observer
import com.netease.nimlib.sdk.RequestCallback
import com.netease.nimlib.sdk.lucene.LuceneService
import com.netease.nimlib.sdk.msg.MsgService
import com.netease.nimlib.sdk.msg.MsgServiceObserve
import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum
import com.netease.nimlib.sdk.msg.model.IMMessage
import com.netease.nimlib.sdk.msg.model.RecentContact
import com.netease.nimlib.sdk.search.model.MsgIndexRecord
import com.sogukj.pe.Manifest
import com.sogukj.pe.R
import com.sogukj.pe.bean.MessageIndexBean
import com.sogukj.pe.bean.UserBean
import com.sogukj.pe.ui.IM.DisplayMessageActivity
import com.sogukj.pe.ui.IM.TeamSearchActivity
import com.sogukj.pe.ui.IM.TeamSelectActivity
import com.sogukj.pe.ui.ScanResultActivity
import com.sogukj.pe.ui.SupportEmptyView
import com.sogukj.pe.ui.msg.MessageListActivity
import com.sogukj.pe.ui.user.UserActivity
import com.sogukj.pe.util.MyGlideUrl
import com.sogukj.pe.util.PermissionUtils
import com.sogukj.pe.util.Trace
import com.sogukj.pe.util.Utils
import com.sogukj.pe.view.CircleImageView
import com.sogukj.pe.view.RecyclerAdapter
import com.sogukj.pe.view.RecyclerHolder
import com.sogukj.service.SoguApi
import com.sogukj.util.Store
import com.sougukj.setVisible
import com.sougukj.textStr
import com.xuexuan.zxing.android.activity.CaptureActivity
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_msg_center.*
import kotlinx.android.synthetic.main.item_shareholder_credit.*
import okhttp3.FormBody
import org.jetbrains.anko.*
import org.jetbrains.anko.support.v4.ctx
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

/**
 * Created by qinfei on 17/10/11.
 */
class MainMsgFragment : BaseFragment() {
    var recentList = ArrayList<RecentContact>()
    override val containerViewId: Int
        get() = R.layout.fragment_msg_center

    lateinit var adapter: RecyclerAdapter<Any>
    lateinit var searchResult: RecyclerAdapter<MsgIndexRecord>
    val extMap = HashMap<String, Any>()

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (hidden) {   // 不在最前端显示 相当于调用了onPause();

        } else {  // 在最前端显示 相当于调用了onResume();
            loadHead()
            add_layout.visibility = View.GONE
        }
    }

    fun loadHead() {
        var header = toolbar_back.getChildAt(0) as CircleImageView
        val user = Store.store.getUser(baseActivity!!)
        if (user?.url.isNullOrEmpty()) {
            val ch = user?.name?.first()
            header.setChar(ch)
        } else {
            Glide.with(context)
                    .load(MyGlideUrl(user?.url))
                    .listener(object : RequestListener<Drawable> {
                        override fun onResourceReady(resource: Drawable?, model: Any?, target: Target<Drawable>?, dataSource: DataSource?, isFirstResource: Boolean): Boolean {
                            header.setImageDrawable(resource)
                            return true
                        }

                        override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<Drawable>?, isFirstResource: Boolean): Boolean {
                            val ch = user?.name?.first()
                            header.setChar(ch)
                            return true
                        }
                    })
                    .into(header)
        }
    }

    private fun initSearchView() {
        search_edt.filters = Utils.getFilter(context)
        search_edt.setOnFocusChangeListener { v, hasFocus ->
            if (hasFocus || search_edt.textStr.isNotEmpty()) {
                search_hint.visibility = View.GONE
                search_icon.visibility = View.VISIBLE
                delete1.visibility = View.VISIBLE
            } else {
                search_hint.visibility = View.VISIBLE
                search_icon.visibility = View.GONE
                delete1.visibility = View.GONE
                search_edt.clearFocus()
            }
        }
        search_edt.setOnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                if (search_edt.textStr.isNotEmpty()){
                    searchKey = search_edt.text.toString()
                    searchWithName()
                }
                true
            } else {
                false
            }
        }
        delete1.setOnClickListener {
            Utils.toggleSoftInput(context, search_edt)
            search_edt.setText("")
            search_edt.clearFocus()
            recycler_view.adapter = adapter
            doRequest()
        }
        search_edt.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {

            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (search_edt.text.toString().isEmpty()) {
                    searchKey = ""
                    adapter.dataList.clear()
                    adapter.dataList.add(zhushou)
                    adapter.dataList.addAll(recentList)
                    recycler_view.adapter = adapter
                    adapter.notifyDataSetChanged()
                    if (adapter.dataList.size == 0) {
                        recycler_view.visibility = View.GONE
                        iv_empty.visibility = View.VISIBLE
                    } else {
                        recycler_view.visibility = View.VISIBLE
                        iv_empty.visibility = View.GONE
                    }
                }
                if (!search_edt.text.isNullOrEmpty()) {
                    delete1.visibility = View.VISIBLE
                } else {
                    delete1.visibility = View.GONE
                }
            }
        })
    }

    private fun searchWithName() {
//        val result = ArrayList<RecentContact>()
//        recentList.forEach {
//            if (it.fromNick.contains(searchKey)) {
//                result.add(it)
//            }
//        }
//        adapter.dataList.clear()
//        adapter.dataList.addAll(result)
//        adapter.dataList.distinct()
//        adapter.notifyDataSetChanged()
//        if (adapter.dataList.size == 0) {
//            recycler_view.visibility = View.GONE
//            iv_empty.visibility = View.VISIBLE
//        } else {
//            recycler_view.visibility = View.VISIBLE
//            iv_empty.visibility = View.GONE
//        }
//        TODO("全文消息检索")
        NIMClient.getService(LuceneService::class.java).searchAllSession(searchKey, 20)
                .setCallback(object : RequestCallback<List<MsgIndexRecord>> {
                    override fun onSuccess(param: List<MsgIndexRecord>?) {
                        searchResult.dataList.clear()
                        param?.let {
                            searchResult.dataList.addAll(it)
                            recycler_view.adapter = searchResult
                        }
                        if (searchResult.dataList.isEmpty()) {
                            recycler_view.visibility = View.GONE
                            iv_empty.visibility = View.VISIBLE
                        } else {
                            recycler_view.visibility = View.VISIBLE
                            iv_empty.visibility = View.GONE
                        }
                    }

                    override fun onException(exception: Throwable?) {
                        recycler_view.visibility = View.VISIBLE
                        iv_empty.visibility = View.GONE
                    }

                    override fun onFailed(code: Int) {
                        recycler_view.visibility = View.VISIBLE
                        iv_empty.visibility = View.GONE
                    }
                })
    }

    lateinit var searchKey: String

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (grantResults.size > 0) {
            for (result in grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED)
                    return
            }
            if (requestCode == 200) {
                val openCameraIntent = Intent(context, CaptureActivity::class.java)
                startActivityForResult(openCameraIntent, 0)
            }
        } else {
            showCustomToast(R.drawable.icon_toast_common, "该功能需要相机权限")
        }
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        toolbar_title.text = "消息"

        loadHead()
        initSearchView()
        toolbar_back.setOnClickListener {
            if (add_layout.visibility == View.VISIBLE) {
                add_layout.visibility = View.GONE
            }
            val intent = Intent(context, UserActivity::class.java)
            startActivityForResult(intent, 0x789)
        }

        toolbar_menu.setOnClickListener {
            if (add_layout.visibility == View.VISIBLE) {
                add_layout.visibility = View.GONE
            } else {
                add_layout.visibility = View.VISIBLE
                add_layout.setOnClickListener {
                    add_layout.visibility = View.GONE
                }
            }
        }
        toolbar.setOnTouchListener(object : View.OnTouchListener {
            override fun onTouch(v: View?, event: MotionEvent?): Boolean {
                if (add_layout.visibility == View.VISIBLE) {
                    add_layout.visibility = View.GONE
                }
                return true
            }
        })
        start_chat.setOnClickListener {
            add_layout.visibility = View.GONE
            var alreadySelect = ArrayList<UserBean>()
            alreadySelect.add(Store.store.getUser(context)!!)
//            TeamSelectActivity.startForResult(context, isSelectUser = true, alreadySelect = alreadySelect, isCreateTeam = true)
            ContactsActivity.start(ctx, alreadySelect, true, true)
        }
        scan.setOnClickListener {
            add_layout.visibility = View.GONE
            var per = "android.permission.CAMERA"
            if (ContextCompat.checkSelfPermission(context, per) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(per), 200)
            } else {
                val openCameraIntent = Intent(context, CaptureActivity::class.java)
                startActivityForResult(openCameraIntent, 0)
            }
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
                                        .listener(object : RequestListener<Drawable> {
                                            override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<Drawable>?, isFirstResource: Boolean): Boolean {
                                                val ch = userInfo.name.first()
                                                msgIcon.setChar(ch)
                                                return false
                                            }

                                            override fun onResourceReady(resource: Drawable?, model: Any?, target: Target<Drawable>?, dataSource: DataSource?, isFirstResource: Boolean): Boolean {
                                                return false
                                            }

                                        })
                                        .into(msgIcon)
                            }
                        } else if (data.sessionType == SessionTypeEnum.Team) {
                            val value = data.msgStatus.value
                            val fromNick = if (data.fromNick.isNullOrEmpty()) "" else "${data.fromNick}:"
                            when (value) {
                                3 -> tvTitleMsg.text = Html.fromHtml("<font color='#a0a4aa'>[已读]</font>$fromNick${data.content}")
                                4 -> tvTitleMsg.text = Html.fromHtml("<font color='#1787fb'>[未读]</font>$fromNick${data.content}")
                                else -> tvTitleMsg.text = "$fromNick${data.content}"
                            }
                            val team = NimUIKit.getTeamProvider().getTeamById(data.contactId)
                            team?.let {
                                Glide.with(this@MainMsgFragment)
                                        .load(it.icon)
                                        .apply(RequestOptions().error(R.drawable.im_team_default))
                                        .into(msgIcon)
                            }
                        }
                        try {
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
            if (search_edt.textStr.isEmpty()) {
                search_edt.clearFocus()
            }
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
        recycler_view.layoutManager = layoutManager
        recycler_view.adapter = adapter

//        val header = ProgressLayout(baseActivity)
//        header.setColorSchemeColors(ContextCompat.getColor(baseActivity, R.color.color_main))
//        refresh.setHeaderView(header)
//        val footer = BallPulseView(baseActivity)
//        footer.setAnimatingColor(ContextCompat.getColor(baseActivity, R.color.color_main))
//        refresh.setBottomView(footer)
//        refresh.setOverScrollRefreshShow(false)
//        refresh.setOnRefreshListener(object : RefreshListenerAdapter() {
//            override fun onRefresh(refreshLayout: TwinklingRefreshLayout?) {
//                doRequest()
//                refresh.finishRefreshing()
//            }
//
//            override fun onLoadMore(refreshLayout: TwinklingRefreshLayout?) {
//                doRequest()
//                refresh.finishLoadmore()
//            }
//
//        })
//        refresh.setAutoLoadMore(true)
        searchResult = RecyclerAdapter(ctx) { _adapter, parent, _ ->
            SearchResultHolder(_adapter.getView(R.layout.item_msg_index, parent))
        }
        searchResult.onItemClick = {v, position ->
            search_edt.setText("")
            search_edt.clearFocus()
            val record = searchResult.dataList[position]
            when(record.sessionType){
                SessionTypeEnum.P2P->{
                    NimUIKit.startP2PSession(ctx,record.sessionId,record.message)
                }
                SessionTypeEnum.Team->{
                    NimUIKit.startTeamSession(ctx,record.sessionId,record.message)
                }
            }
//            DisplayMessageActivity.start(ctx, record.message)
        }

        refresh.setOnRefreshListener {
            doRequest()
            refresh.finishRefresh(1000)
        }
        refresh.setOnLoadMoreListener {
            doRequest()
            refresh.finishLoadMore(1000)
        }

        registerObservers(true)

        mAppBarLayout.addOnOffsetChangedListener(object : AppBarLayout.OnOffsetChangedListener {
            override fun onOffsetChanged(appBarLayout: AppBarLayout?, verticalOffset: Int) {
                if (mAppBarLayout.height > 0) {
                    if (Math.abs(verticalOffset) > mAppBarLayout.height - 10) {
                        Utils.closeInput(context, search_edt)
                    }
                }
            }
        })

        Glide.with(baseActivity)
                .load(Uri.parse("file:///android_asset/img_loading.gif"))
                .into(iv_loading)
        iv_loading?.visibility = View.VISIBLE
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            val bundle = data!!.extras
            val scanResult = bundle!!.getString("result")//       /api/qrlogin/notify
            if (scanResult.contains("/api/qrlogin/notify")) {

                //   http://pre.pe.stockalert.cn/api/qrlogin/notify?uuid=0D381CE1-70AE-C02E-98AB-84DC7B55EC0D&fd=1
                var index = scanResult.indexOf("/api/")

                SoguApi.getService(baseActivity!!.application)
                        .qrNotify(scanResult.substring(index), 1)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeOn(Schedulers.io())
                        .subscribe({ payload ->
                            if (payload.isOk) {
                                ScanResultActivity.start(baseActivity, scanResult)
                                baseActivity?.overridePendingTransition(R.anim.activity_in, 0)
                                add_layout.visibility = View.GONE
                            } else {
                                showCustomToast(R.drawable.icon_toast_fail, payload.message)
                            }
                        }, { e ->
                            //showCustomToast(R.drawable.icon_toast_fail, "二维码错误")
                        })
            }
        } else if (requestCode == 0x789) {
            loadHead()
        }
    }

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
                        adapter.dataList.clear()
                        payload.payload?.apply {
                            adapter.dataList.add(this)
                            zhushou = this
                        }
                        getIMRecentContact()
                    } else {
                        showCustomToast(R.drawable.icon_toast_fail, payload.message)
                        getIMRecentContact()
                    }
                }, { e ->
                    Trace.e(e)
                    getIMRecentContact()
                    //showCustomToast(R.drawable.icon_toast_common, "暂无可用数据")
                })
    }

    var zhushou = MessageIndexBean()

    private fun getIMRecentContact() {
        //recentList = ArrayList()
        recentList.clear()
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
                adapter.dataList.distinct()
                adapter.notifyDataSetChanged()
                iv_loading.visibility = View.GONE
                //iv_empty?.visibility = if (adapter.dataList.isEmpty()) View.VISIBLE else View.GONE
                //refresh?.setEnableLoadmore(adapter.dataList.size % 20 == 0)
                if (adapter.dataList.size == 0) {
                    recycler_view.visibility = View.GONE
                    iv_empty.visibility = View.VISIBLE
                } else {
                    recycler_view.visibility = View.VISIBLE
                    iv_empty.visibility = View.GONE
                }
            }

            override fun onException(p0: Throwable?) {
                iv_loading.visibility = View.GONE
            }

            override fun onFailed(p0: Int) {
                iv_loading.visibility = View.GONE
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
        adapter.dataList.distinct()
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

    inner class SearchResultHolder(itemView: View) : RecyclerHolder<MsgIndexRecord>(itemView) {
        val msgIcon = convertView.findViewById(R.id.msg_icon) as CircleImageView
        val tvTitle = convertView.findViewById(R.id.tv_title) as TextView
        val tvDate = convertView.findViewById(R.id.tv_date) as TextView
        val tvTitleMsg = convertView.findViewById(R.id.tv_title_msg) as TextView
        val tvNum = convertView.findViewById(R.id.tv_num) as TextView
        @SuppressLint("SetTextI18n")
        override fun setData(view: View, data: MsgIndexRecord, position: Int) {
            tvNum.setVisible(false)
            val titleName = UserInfoHelper.getUserTitleName(data.sessionId, data.sessionType)
            tvTitle.text = titleName
            val content = data.message.content
            if (data.sessionType == SessionTypeEnum.P2P) {
                tvTitleMsg.text = Html.fromHtml(content.replace(searchKey, "<font color='#1787fb'>$searchKey</font>"))
                val userInfo = NimUIKit.getUserInfoProvider().getUserInfo(data.sessionId)
                userInfo?.let {
                    Glide.with(this@MainMsgFragment)
                            .load(it.avatar)
                            .listener(object : RequestListener<Drawable> {
                                override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<Drawable>?, isFirstResource: Boolean): Boolean {
                                    val ch = userInfo.name.first()
                                    msgIcon.setChar(ch)
                                    return false
                                }

                                override fun onResourceReady(resource: Drawable?, model: Any?, target: Target<Drawable>?, dataSource: DataSource?, isFirstResource: Boolean): Boolean {
                                    return false
                                }

                            })
                            .into(msgIcon)
                }
            } else if (data.sessionType == SessionTypeEnum.Team) {
                val fromNick = if (data.message.fromNick.isNullOrEmpty()) "" else "${data.message.fromNick}:"
                tvTitleMsg.text = Html.fromHtml("$fromNick${content.replace(searchKey, "<font color='#1787fb'>$searchKey</font>")}")
                val team = NimUIKit.getTeamProvider().getTeamById(data.sessionId)
                team?.let {
                    Glide.with(this@MainMsgFragment)
                            .load(it.icon)
                            .apply(RequestOptions().error(R.drawable.im_team_default))
                            .into(msgIcon)
                }
            }
            try {
                val time = Utils.getTime(data.time, "yyyy-MM-dd HH:mm:ss")
                tvDate.text = Utils.formatDate(time)
            } catch (e: Exception) {
            }
        }
    }
}