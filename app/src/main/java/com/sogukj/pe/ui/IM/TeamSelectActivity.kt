package com.sogukj.pe.ui.IM

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.Editable
import android.text.Html
import android.text.TextWatcher
import android.view.*
import android.view.inputmethod.EditorInfo
import android.widget.BaseExpandableListAdapter
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import anet.channel.util.Utils.context
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.netease.nim.uikit.business.team.activity.CustomExpandableListView
import com.netease.nim.uikit.common.ui.imageview.CircleImageView
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.bean.DepartmentBean
import com.sogukj.pe.bean.UserBean
import com.sogukj.pe.util.Trace
import com.sogukj.pe.util.Utils
import com.sogukj.service.SoguApi
import com.sogukj.util.Store
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_team_select.*
import org.jetbrains.anko.backgroundColor
import org.jetbrains.anko.imageResource
import org.jetbrains.anko.toast

class TeamSelectActivity : AppCompatActivity() {
    private val departList = ArrayList<DepartmentBean>() //组织架构
    private val contactList = ArrayList<UserBean>()//最近联系人
    private val resultData = ArrayList<UserBean>()//搜索结果
    lateinit var alreadyList: ArrayList<UserBean>//已选中的
    var tag: String? = null
    val mine = Store.store.getUser(this)//自己
    var isSelectUser: Boolean = false//是否是选择用户
    var isCreateTeam: Boolean = true
    var fromTeam: Boolean = true
    var flag: String? = ""
    lateinit var searchKey: String
    private lateinit var orgAdapter: OrganizationAdapter
    private lateinit var contactAdapter: ContactAdapter
    private lateinit var resultAdapter: ContactAdapter
    private val selectMap = HashMap<String, Boolean>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_team_select)
        Utils.setWindowStatusBarColor(this, R.color.color_blue_0888ff)
        team_toolbar.title = ""
        setSupportActionBar(team_toolbar)
        team_toolbar.setNavigationIcon(R.drawable.sogu_ic_back)
        team_toolbar.setNavigationOnClickListener { finish() }
        getDataFromIntent()
        initSearchView()
        initResultList()
        val organizationList = initOrganizationList()
//        val contactList = initContactList()
        val layout = LinearLayout(this)
        val params = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        params.bottomMargin = Utils.dpToPx(this, 10)
        layout.orientation = LinearLayout.VERTICAL
        if (isSelectUser) {
            team_toolbar_title.text = "选择联系人"
            confirmSelectLayout.visibility = View.VISIBLE
//            layout.addView(contactList, params)
            layout.addView(organizationList, params)
        } else {
            team_toolbar_title.text = "通讯录"
            confirmSelectLayout.visibility = View.GONE
            layout.addView(organizationList, params)
//            layout.addView(contactList, params)
        }
        listContent.addView(layout)
        doRequest()
        confirmTv.setOnClickListener {
            if (isSelectUser) {
                alreadyList.clear()
                val map = selectMap.filterValues { it }
                if (map.isNotEmpty()) {
                    map.keys.forEach {
                        val id = it
                        departList.forEach {
                            it.data?.forEach {
                                if (id == it.accid) {
                                    alreadyList.add(it)
                                }
                            }
                        }
                    }
                }
                val intent = Intent()
                if (!isCreateTeam) {
                    intent.putExtra(Extras.DATA, alreadyList)
                    setResult(Extras.RESULTCODE, intent)
                    finish()
                } else {
                    TeamCreateActivity.start(this, alreadyList)
                }
            }
        }
    }


    private fun getDataFromIntent() {
        isSelectUser = intent.getBooleanExtra(Extras.SELECT_USER, false)
        val data = intent.getSerializableExtra(Extras.DATA)
        isCreateTeam = intent.getBooleanExtra(Extras.CREATE_TEAM, true)
        fromTeam = intent.getBooleanExtra(Extras.FLAG, true)
        if (data != null) {
            alreadyList = data as ArrayList<UserBean>
            selectNumber.text = "已选择: ${alreadyList.size} 人"
//            val find = alreadyList.find { it.accid == mine?.accid }
//            if (find!=null){
//                val i = if (alreadyList.size - 1 < 0) 0 else alreadyList.size - 1
//                selectNumber.text = "已选择: $i 人"
//            }else{
//
//            }
        } else {
            alreadyList = ArrayList()
        }
    }


    private fun initSearchView() {
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

    private fun initOrganizationList(): CustomExpandableListView {
        val organization = CustomExpandableListView(this)
        organization.setGroupIndicator(null)
        organization.divider = null
        orgAdapter = OrganizationAdapter(departList)
        organization.setAdapter(orgAdapter)
        organization.backgroundColor = Color.WHITE
        organization.id = R.id.organizationList
        return organization
    }

    private fun initContactList(): LinearLayout {
        val layout = LinearLayout(this)
        layout.orientation = LinearLayout.VERTICAL
        layout.backgroundColor = Color.WHITE
        val title = TextView(this)
        title.setTextColor(Color.BLACK)
        title.text = "最近联系人"
        val layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        layoutParams.leftMargin = Utils.dpToPx(this, 10)
        layoutParams.topMargin = Utils.dpToPx(this, 20)
        layoutParams.bottomMargin = Utils.dpToPx(this, 20)
        title.layoutParams = layoutParams
        val contact = RecyclerView(this)
        contact.layoutManager = LinearLayoutManager(this)
        contactAdapter = ContactAdapter(contactList)
        contact.adapter = contactAdapter
        layout.addView(title)
        layout.addView(contact)
        layout.id = R.id.contactLayout
        return layout
    }

    private fun initResultList() {
        resultAdapter = ContactAdapter(resultData)
        resultList.layoutManager = LinearLayoutManager(this)
        resultList.adapter = resultAdapter
    }

    fun doRequest() {
        SoguApi.getService(application)
                .userDepart()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ payload ->
                    if (payload.isOk) {
                        departList.clear()
                        payload.payload?.forEach { depart ->
                            departList.add(depart)
                            depart.data?.forEach {
                                it.depart_name = depart.de_name
                                it.accid?.let {
                                    if (it == mine?.accid && fromTeam) {
                                        selectMap.put(it, true)
                                    } else {
                                        selectMap.put(it, false)
                                    }
                                }
                            }
                            alreadyList.forEach {
                                it.accid?.let {
                                    selectMap.put(it, true)
                                }
                            }
                        }
                        orgAdapter.notifyDataSetChanged()
                    } else
                        toast(payload.message!!)
                }, { e ->
                    Trace.e(e)
                    toast("数据获取失败")
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


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        if (fromTeam) {
            menuInflater.inflate(R.menu.menu_create_imteam, menu)
        }
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        item?.let {
            if (it.itemId == R.id.create_imteam) {
                search_edt.clearFocus()
                isSelectUser = !isSelectUser
                orgAdapter.notifyDataSetChanged()
                val orgList = initOrganizationList()
//                val conLayout = initContactList()
                listContent.removeAllViews()
                val layout = LinearLayout(this)
                val params = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
                params.bottomMargin = Utils.dpToPx(this, 10)
                layout.orientation = LinearLayout.VERTICAL
                layout.removeAllViews()
                if (isSelectUser) {
                    team_toolbar_title.text = "选择联系人"
                    confirmSelectLayout.visibility = View.VISIBLE
//                    layout.addView(conLayout, params)
                    layout.addView(orgList, params)
                } else {
                    team_toolbar_title.text = "通讯录"
                    confirmSelectLayout.visibility = View.GONE
                    layout.addView(orgList, params)
//                    layout.addView(conLayout, params)
                }
                listContent.addView(layout)
            }
        }
        return super.onOptionsItemSelected(item)
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
                convertView = LayoutInflater.from(this@TeamSelectActivity).inflate(R.layout.item_team_organization_parent, null)
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
            convertView = LayoutInflater.from(this@TeamSelectActivity).inflate(R.layout.item_team_organization_chlid, null)
            holder = ChildHolder(convertView)
            convertView!!.tag = holder
            parents[groupPosition].data?.let {
                val userBean = it[childPosition]
                if (userBean.user_id == mine!!.uid && fromTeam) {
                    holder.selectIcon.imageResource = R.drawable.cannot_select
                }
                val options = RequestOptions()
                options.error(R.drawable.nim_avatar_default)
                options.fallback(R.drawable.nim_avatar_default)
                Glide.with(this@TeamSelectActivity)
                        .load(userBean.headImage())
                        .apply(options)
                        .into(holder.userImg)
                holder.userName.text = userBean.name
                holder.userPosition.text = userBean.position

                if (isSelectUser) {
                    holder.selectIcon.visibility = View.VISIBLE
                    userBean.accid?.let {
                        if (it != mine.accid) {
                            holder.selectIcon.isSelected = selectMap[it]!!
                        }
                    }
                } else {
                    holder.selectIcon.visibility = View.GONE
                }

                holder.itemView.setOnClickListener {
                    search_edt.clearFocus()
                    if (isSelectUser) {
                        //选人
                        if (userBean.accid != mine.accid || !fromTeam) {
                            holder.selectIcon.isSelected = !holder.selectIcon.isSelected
                            userBean.accid?.let {
                                selectMap.put(it, holder.selectIcon.isSelected)
                                val map = selectMap.filterValues { it }
                                selectNumber.text = "已选择: ${map.size}人"
                            }
                        }
                    } else {
                        //查看详情
                        PersonalInfoActivity.start(this@TeamSelectActivity, userBean)
                    }
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
                ContactHolder(LayoutInflater.from(this@TeamSelectActivity).inflate(R.layout.item_team_organization_chlid, parent, false))

        override fun onBindViewHolder(holder: ContactHolder, position: Int) {
            val userBean = datas[position]
            val options = RequestOptions()
            options.error(R.drawable.nim_avatar_default)
            options.fallback(R.drawable.nim_avatar_default)
            Glide.with(this@TeamSelectActivity)
                    .load(userBean.headImage())
                    .apply(options)
                    .into(holder.userImg)
            var name = userBean.name
            if (searchKey.isNotEmpty()) {
                name = name.replaceFirst(searchKey, "<font color='#1787fb'>$searchKey</font>")
            }
            holder.userName.text = Html.fromHtml(name)
            holder.userPosition.text = userBean.position

            if (isSelectUser) {
                holder.selectIcon.visibility = View.VISIBLE
                userBean.accid?.let {
                    if (it != mine!!.accid) {
                        holder.selectIcon.isSelected = selectMap[it]!!
                    } else {
                        holder.selectIcon.imageResource = R.drawable.cannot_select
                    }
                }
            } else {
                holder.selectIcon.visibility = View.GONE
            }

            holder.itemView.setOnClickListener {
                search_edt.clearFocus()
                if (isSelectUser) {
                    //选人
                    if (userBean.accid != mine!!.accid) {
                        holder.selectIcon.isSelected = !holder.selectIcon.isSelected
                        userBean.accid?.let {
                            selectMap.put(it, holder.selectIcon.isSelected)
                            val map = selectMap.filterValues { it }
                            selectNumber.text = "已选择: ${map.size}人"
                        }
                    }
                } else {
                    //查看详情
                    PersonalInfoActivity.start(this@TeamSelectActivity, userBean)
                }
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

        fun startForResult(context: Context, isSelectUser: Boolean? = null,
                           alreadySelect: ArrayList<UserBean>? = null,
                           isCreateTeam: Boolean? = null,
                           fromTeam: Boolean? = null,
                           requestCode: Int? = null) {
            val intent = Intent(context, TeamSelectActivity::class.java)
            intent.putExtra(Extras.SELECT_USER, isSelectUser)
            intent.putExtra(Extras.DATA, alreadySelect)
            intent.putExtra(Extras.CREATE_TEAM, isCreateTeam)
            intent.putExtra(Extras.FLAG, fromTeam)
            val code = requestCode ?: Extras.REQUESTCODE
            if (context is Fragment) {
                context.startActivityForResult(intent, code)
            } else if (context is Activity) {
                context.startActivityForResult(intent, code)
            }
        }

        fun startForResult(fragment: Fragment, isSelectUser: Boolean? = null,
                           alreadySelect: ArrayList<UserBean>? = null,
                           isCreateTeam: Boolean? = null,
                           fromTeam: Boolean? = null,
                           requestCode: Int? = null) {
            val intent = Intent(fragment.context, TeamSelectActivity::class.java)
            intent.putExtra(Extras.SELECT_USER, isSelectUser)
            intent.putExtra(Extras.DATA, alreadySelect)
            intent.putExtra(Extras.CREATE_TEAM, isCreateTeam)
            intent.putExtra(Extras.FLAG, fromTeam)
            val code = requestCode ?: Extras.REQUESTCODE
            fragment.startActivityForResult(intent, code)
        }
    }
}
