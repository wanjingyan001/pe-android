package com.sogukj.pe.ui

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.Editable
import android.text.Html
import android.text.TextWatcher
import android.util.Log
import android.view.*
import android.view.inputmethod.EditorInfo
import android.widget.*
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.framework.base.BaseFragment
import com.google.gson.Gson
import com.netease.nim.uikit.api.NimUIKit
import com.netease.nim.uikit.business.team.activity.CustomExpandableListView
import com.netease.nim.uikit.common.ui.imageview.CircleImageView
import com.netease.nimlib.sdk.NIMClient
import com.netease.nimlib.sdk.RequestCallback
import com.netease.nimlib.sdk.team.TeamService
import com.netease.nimlib.sdk.team.model.Team
import com.sogukj.pe.R
import com.sogukj.pe.bean.DepartmentBean
import com.sogukj.pe.bean.UserBean
import com.sogukj.pe.ui.IM.PersonalInfoActivity
import com.sogukj.pe.ui.IM.TeamBean
import com.sogukj.pe.ui.IM.TeamInfoActivity
import com.sogukj.pe.ui.user.UserActivity
import com.sogukj.pe.util.Trace
import com.sogukj.pe.util.Utils
import com.sogukj.service.SoguApi
import com.sogukj.util.Store
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_team_select.*
import org.jetbrains.anko.*
import org.jetbrains.anko.support.v4.ctx
import java.util.*
import kotlin.collections.ArrayList

class TeamSelectFragment : BaseFragment() {
    private val departList = ArrayList<DepartmentBean>() //组织架构
    private val contactList = ArrayList<UserBean>()//最近联系人
    private val resultData = ArrayList<UserBean>()//搜索结果
    var mine: UserBean? = null//自己
    lateinit var searchKey: String
    private lateinit var orgAdapter: OrganizationAdapter
    private lateinit var contactAdapter: ContactAdapter
    private lateinit var resultAdapter: ContactAdapter
    private val selectMap = HashMap<String, Boolean>()

    override val containerViewId: Int
        get() = R.layout.fragment_team_select

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Utils.setWindowStatusBarColor(baseActivity, R.color.color_blue_0888ff)
        mine = Store.store.getUser(context)
        initSearchView()
        initResultList()
        initHeader()
        initGroupDiscuss()
        initOrganizationList()
//        val contactList = initContactList()
        doRequest()

        val user = Store.store.getUser(baseActivity!!)
        if (user?.url.isNullOrEmpty()) {
            toolbar_back.setChar(user?.name?.first())
        } else {
            Glide.with(context).load(user?.url).into(toolbar_back)
        }
        toolbar_back.setOnClickListener {
            UserActivity.start(context)
        }
    }

    private fun initSearchView() {
        search_edt.filters = Utils.getFilter(context)
        search_edt.setOnFocusChangeListener { v, hasFocus ->
            if (hasFocus) {
                search_hint.visibility = View.GONE
                search_icon.visibility = View.VISIBLE
            } else {
                search_hint.visibility = View.VISIBLE
                search_icon.visibility = View.GONE
                search_edt.clearFocus()
            }
        }
        search_edt.setOnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                searchKey = search_edt.text.toString()
                searchWithName()
                true
            } else {
                false
            }
        }
        search_edt.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {

            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (search_edt.text.toString().isEmpty()) {
                    searchKey = ""
                    listContent.visibility = View.VISIBLE
                    resultList.visibility = View.GONE
                }
            }
        })
    }

    private fun initHeader() {
        when (Utils.getEnvironment()) {
            "civc" -> {
                company_icon.imageResource = R.mipmap.ic_launcher_zd
                companyName.text = "中缔资本"
            }
            "ht" -> {
                company_icon.imageResource = R.mipmap.ic_launcher_ht
                companyName.text = "海通创新"
            }
            else -> {
                company_icon.imageResource = R.mipmap.ic_launcher
                companyName.text = "海通创新"
            }
        }
    }

    private fun initGroupDiscuss() {
        //groupDiscuss
        NIMClient.getService(TeamService::class.java).queryTeamList().setCallback(object : RequestCallback<List<Team>> {
            override fun onSuccess(param: List<Team>?) {
                var parents = ArrayList<String>(Arrays.asList("群聊", "讨论组"))
                var ql = ArrayList<Team>()
                var tlz = ArrayList<Team>()
                param?.let {
                    it.forEach { team ->
                        if (team.isMyTeam) {
                            val bean = Gson().fromJson(team.extension, TeamBean::class.java)
                            if (bean != null) {
                                Log.e("zzz", "讨论组" + team.name)
                                tlz.add(team)
                            } else {
                                Log.e("zzz", "群聊" + team.name)
                                ql.add(team)
                            }
                        }
                    }
                }
                var chids = ArrayList<ArrayList<Team>>()
                chids.add(ql)
                chids.add(tlz)
                var mDisAdapter = DiscussAdapter(parents, chids)
                groupDiscuss.setAdapter(mDisAdapter)
            }

            override fun onFailed(code: Int) {
            }

            override fun onException(exception: Throwable?) {
            }
        })
    }

    private fun initOrganizationList() {
        orgAdapter = OrganizationAdapter(departList)
        organizationList.setAdapter(orgAdapter)
    }

    private fun initContactList(): LinearLayout {
        val layout = LinearLayout(context)
        layout.orientation = LinearLayout.VERTICAL
        layout.backgroundColor = Color.WHITE
        val title = TextView(context)
        title.setTextColor(Color.BLACK)
        title.text = "最近联系人"
        val layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        layoutParams.leftMargin = Utils.dpToPx(context, 10)
        layoutParams.topMargin = Utils.dpToPx(context, 20)
        layoutParams.bottomMargin = Utils.dpToPx(context, 20)
        title.layoutParams = layoutParams
        val contact = RecyclerView(context)
        contact.layoutManager = LinearLayoutManager(context)
        contactAdapter = ContactAdapter(contactList)
        contact.adapter = contactAdapter
        layout.addView(title)
        layout.addView(contact)
        layout.id = R.id.contactLayout
        return layout
    }

    private fun initResultList() {
        resultAdapter = ContactAdapter(resultData)
        resultList.layoutManager = LinearLayoutManager(context)
        resultList.adapter = resultAdapter
    }

    @SuppressLint("SetTextI18n")
    fun doRequest() {
        SoguApi.getService(baseActivity!!.application)
                .userDepart()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ payload ->
                    if (payload.isOk) {
                        var i = 0
                        departList.clear()
                        payload.payload?.forEach { depart ->
                            departList.add(depart)
                            i += depart.data!!.size
                        }
                        num.text = "共${i}人"
                        orgAdapter.notifyDataSetChanged()
                    } else
                        showCustomToast(R.drawable.icon_toast_fail, payload.message)
                }, { e ->
                    Trace.e(e)
                    showCustomToast(R.drawable.icon_toast_fail, "数据获取失败")
                })
    }

    private fun searchWithName() {
        val result = ArrayList<UserBean>()
        departList.forEach {
            it.data?.let {
                it.forEach {
                    if (it.name.contains(searchKey) && it.user_id != mine?.uid) {
                        result.add(it)
                    }
                }
            }
        }
        listContent.visibility = View.GONE
        resultList.visibility = View.VISIBLE
        resultData.clear()
        resultData.addAll(result)
        resultAdapter.notifyDataSetChanged()
    }

    internal inner class DiscussAdapter(val parents: ArrayList<String>, val childs: ArrayList<ArrayList<Team>>) : BaseExpandableListAdapter() {

        override fun getGroupCount(): Int = parents.size

        override fun getChildrenCount(groupPosition: Int): Int {
            return childs[groupPosition].size
        }

        override fun getGroup(groupPosition: Int): Any = parents[groupPosition]

        override fun getChild(groupPosition: Int, childPosition: Int): Any? {
            return childs[groupPosition][childPosition]
        }

        override fun getGroupId(groupPosition: Int): Long = groupPosition.toLong()

        override fun getChildId(groupPosition: Int, childPosition: Int): Long =
                childPosition.toLong()

        override fun hasStableIds(): Boolean = true

        override fun getGroupView(groupPosition: Int, isExpanded: Boolean, convertView: View?, parent: ViewGroup): View {
            var convertView = convertView
            val holder: ParentHolder
            if (convertView == null) {
                convertView = LayoutInflater.from(context).inflate(R.layout.item_team_organization_parent, null)
                holder = ParentHolder(convertView)
                convertView!!.tag = holder
            } else {
                holder = convertView.tag as ParentHolder
            }
            val title = parents[groupPosition]
            holder.departmentName.text = "${title} (${childs[groupPosition].size})"
            holder.indicator.isSelected = isExpanded
            return convertView
        }

        override fun getChildView(groupPosition: Int, childPosition: Int, isLastChild: Boolean, convertView: View?, parent: ViewGroup): View {
            var convertView = convertView
            val holder: ChildHolder
            convertView = LayoutInflater.from(context).inflate(R.layout.item_team_organization_chlid_2, null)
            holder = ChildHolder(convertView)
            convertView!!.tag = holder
            childs[groupPosition].let {
                val team = it[childPosition]
                holder.userName.text = team.name
                holder.selectIcon.visibility = View.GONE
                holder.itemView.setOnClickListener {
                    search_edt.clearFocus()
                    NimUIKit.startTeamSession(context, team.id)
                }
            }
            return convertView
        }

        override fun isChildSelectable(groupPosition: Int, childPosition: Int): Boolean = true

        internal inner class ParentHolder(view: View) {
            val indicator: ImageView
            val departmentName: TextView

            init {
                departmentName = view.findViewById(R.id.departmentName) as TextView
                indicator = view.findViewById(R.id.indicator) as ImageView
            }
        }

        internal inner class ChildHolder(view: View) {
            val selectIcon: ImageView
            val userImg: CircleImageView
            val userName: TextView
            val userPosition: TextView
            val itemView: View

            init {
                selectIcon = view.findViewById(R.id.selectIcon) as ImageView
                userImg = view.findViewById(R.id.userHeadImg) as CircleImageView
                userName = view.findViewById(R.id.userName) as TextView
                userPosition = view.findViewById(R.id.userPosition) as TextView
                itemView = view
            }
        }
    }

    internal inner class OrganizationAdapter(private val parents: List<DepartmentBean>) : BaseExpandableListAdapter() {

        override fun getGroupCount(): Int = parents.size

        override fun getChildrenCount(groupPosition: Int): Int {
            return if (parents[groupPosition].data != null) {
                parents[groupPosition].data!!.size
            } else {
                0
            }
        }

        override fun getGroup(groupPosition: Int): Any = parents[groupPosition]

        override fun getChild(groupPosition: Int, childPosition: Int): Any? {
            return if (parents[groupPosition].data != null) {
                parents[groupPosition].data!![childPosition]
            } else {
                null
            }
        }

        override fun getGroupId(groupPosition: Int): Long = groupPosition.toLong()

        override fun getChildId(groupPosition: Int, childPosition: Int): Long =
                childPosition.toLong()

        override fun hasStableIds(): Boolean = true

        override fun getGroupView(groupPosition: Int, isExpanded: Boolean, convertView: View?, parent: ViewGroup): View {
            var convertView = convertView
            val holder: ParentHolder
            if (convertView == null) {
                convertView = LayoutInflater.from(context).inflate(R.layout.item_team_organization_parent, null)
                holder = ParentHolder(convertView)
                convertView!!.tag = holder
            } else {
                holder = convertView.tag as ParentHolder
            }
            val departmentBean = parents[groupPosition]
            holder.departmentName.text = "${departmentBean.de_name} (${parents[groupPosition].data?.size})"
            holder.indicator.isSelected = isExpanded
            return convertView
        }

        override fun getChildView(groupPosition: Int, childPosition: Int, isLastChild: Boolean, convertView: View?, parent: ViewGroup): View {
            var convertView = convertView
            val holder: ChildHolder
            convertView = LayoutInflater.from(context).inflate(R.layout.item_team_organization_chlid, null)
            holder = ChildHolder(convertView)
            convertView!!.tag = holder
            parents[groupPosition].data?.let {
                val userBean = it[childPosition]
                if (userBean.user_id == mine!!.uid) {
                    holder.selectIcon.imageResource = R.drawable.cannot_select
                }
                Glide.with(context)
                        .load(userBean.headImage())
                        .apply(RequestOptions().error(R.drawable.nim_avatar_default).placeholder(R.drawable.nim_avatar_default))
                        .into(holder.userImg)
                holder.userName.text = userBean.name
                holder.userPosition.text = userBean.position
                holder.selectIcon.visibility = View.GONE
                holder.itemView.setOnClickListener {
                    search_edt.clearFocus()
                    PersonalInfoActivity.start(context, userBean, null)
                }
            }
            return convertView
        }

        override fun isChildSelectable(groupPosition: Int, childPosition: Int): Boolean = true

        internal inner class ParentHolder(view: View) {
            val indicator: ImageView
            val departmentName: TextView

            init {
                departmentName = view.findViewById(R.id.departmentName) as TextView
                indicator = view.findViewById(R.id.indicator) as ImageView
            }
        }

        internal inner class ChildHolder(view: View) {
            val selectIcon: ImageView
            val userImg: CircleImageView
            val userName: TextView
            val userPosition: TextView
            val itemView: View

            init {
                selectIcon = view.findViewById(R.id.selectIcon) as ImageView
                userImg = view.findViewById(R.id.userHeadImg) as CircleImageView
                userName = view.findViewById(R.id.userName) as TextView
                userPosition = view.findViewById(R.id.userPosition) as TextView
                itemView = view
            }
        }
    }

    internal inner class ContactAdapter(private val datas: List<UserBean>) : RecyclerView.Adapter<ContactAdapter.ContactHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactHolder =
                ContactHolder(LayoutInflater.from(context).inflate(R.layout.item_team_organization_chlid, parent, false))

        override fun onBindViewHolder(holder: ContactHolder, position: Int) {
            val userBean = datas[position]
            Glide.with(context)
                    .load(userBean.headImage())
                    .apply(RequestOptions().error(R.drawable.nim_avatar_default).placeholder(R.drawable.nim_avatar_default))
                    .into(holder.userImg)
            var name = userBean.name
            if (searchKey.isNotEmpty()) {
                name = name.replaceFirst(searchKey, "<font color='#1787fb'>$searchKey</font>")
            }
            holder.userName.text = Html.fromHtml(name)
            holder.userPosition.text = userBean.position
            holder.selectIcon.visibility = View.GONE
            holder.itemView.setOnClickListener {
                search_edt.clearFocus()
                //查看详情
                PersonalInfoActivity.start(context, userBean, null)
            }

        }

        override fun getItemCount(): Int = datas.size

        internal inner class ContactHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val selectIcon: ImageView
            val userImg: CircleImageView
            val userName: TextView
            val userPosition: TextView

            init {
                selectIcon = itemView.findViewById(R.id.selectIcon) as ImageView
                userImg = itemView.findViewById(R.id.userHeadImg) as CircleImageView
                userName = itemView.findViewById(R.id.userName) as TextView
                userPosition = itemView.findViewById(R.id.userPosition) as TextView
            }
        }
    }

    companion object {
        fun newInstance(): TeamSelectFragment {
            val fragment = TeamSelectFragment()
            return fragment
        }
    }
}