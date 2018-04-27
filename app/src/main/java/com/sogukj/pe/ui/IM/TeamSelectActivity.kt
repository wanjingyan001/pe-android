package com.sogukj.pe.ui.IM

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
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
import com.framework.base.BaseActivity
import com.google.gson.Gson
import com.netease.nim.uikit.business.team.activity.CustomExpandableListView
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.R.layout.header
import com.sogukj.pe.bean.DepartmentBean
import com.sogukj.pe.bean.UserBean
import com.sogukj.pe.util.Trace
import com.sogukj.pe.util.Utils
import com.sogukj.pe.view.CircleImageView
import com.sogukj.service.SoguApi
import com.sogukj.util.Store
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_team_select.*
import kotlinx.android.synthetic.main.item_fund_account_list.view.*
import org.jetbrains.anko.*

@Deprecated("被ContactsActivity取代")
class TeamSelectActivity : BaseActivity() {
    private val departList = ArrayList<DepartmentBean>() //组织架构
    private val contactList = ArrayList<UserBean>()//最近联系人
    private val resultData = ArrayList<UserBean>()//搜索结果
    lateinit var alreadyList: ArrayList<UserBean>//已选中的
    var tag: String? = null
    val mine = Store.store.getUser(this)//自己
    var isSelectUser: Boolean = false//是否是选择用户
    var isCreateTeam: Boolean = true
    var canRemove: Boolean = true
    var fromTeam: Boolean = true
    var flag: String? = ""
    lateinit var searchKey: String
    private lateinit var orgAdapter: OrganizationAdapter
    private lateinit var contactAdapter: ContactAdapter
    private lateinit var resultAdapter: ContactAdapter
    private lateinit var header: View
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
        getShareFile()
        initSearchView()
        initResultList()
        default = intent.getSerializableExtra(Extras.DEFAULT) as ArrayList<Int>?
        header = initHeader()
        val organizationList = initOrganizationList()
        val contactList = initContactList()
        val layout = LinearLayout(this)
        val params = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        params.bottomMargin = Utils.dpToPx(this, 10)
        layout.orientation = LinearLayout.VERTICAL

        var orgHeader = initOrgHead()
        var contactHeader = initContactHead()

        if (isSelectUser) {
            team_toolbar_title.text = "选择联系人"
            confirmSelectLayout.visibility = View.VISIBLE
            //layout.addView(header)
            layout.addView(contactHeader)
            layout.addView(contactList, params)
            layout.addView(orgHeader)
            layout.addView(organizationList, params)
        } else {
            team_toolbar_title.text = "通讯录"
            confirmSelectLayout.visibility = View.GONE
            layout.addView(header)
            layout.addView(orgHeader)
            layout.addView(organizationList, params)
            layout.addView(contactList, params)
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
                                if (id == it.uid.toString()) {
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
        canRemove = intent.getBooleanExtra(Extras.CAN_REMOVE_MEMBER, true)
        if (data != null) {
            alreadyList = data as ArrayList<UserBean>
            selectNumber.text = "已选择: ${alreadyList.size} 人"
        } else {
            val accounts = intent.getSerializableExtra(Extras.DATA2) as ArrayList<String>?
            if (accounts != null) {
                alreadyList = ArrayList()
                accounts.forEach {
                    val bean = UserBean()
                    bean.uid = it.toInt()
                    bean.user_id = it.toInt()
                    alreadyList.add(bean)
                }
            } else {
                alreadyList = ArrayList()
            }
        }
    }


    private fun initSearchView() {
        search_edt.filters = Utils.getFilter(this)
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

    private fun initHeader(): View {
        val inflate = layoutInflater.inflate(R.layout.layout_team_select_list_header, null)
        val icon = inflate.find<CircleImageView>(R.id.icon)
        val name = inflate.find<TextView>(R.id.companyName)
        when (Utils.getEnvironment()) {
            "civc" -> {
                icon.imageResource = R.mipmap.ic_launcher_zd
                name.text = "中缔资本"
            }
            "ht" -> {
                icon.imageResource = R.mipmap.ic_launcher_ht
                name.text = "海通创新"
            }
            "kk" -> {
                icon.imageResource = R.mipmap.ic_launcher_kk
                name.text = "夸克"
            }
            "yge" -> {
                icon.imageResource = R.mipmap.ic_launcher_yge
                name.text = "雅戈尔"
            }
            else -> {
                icon.imageResource = R.mipmap.ic_launcher_pe
                name.text = "海通创新"
            }
        }
        val params = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        params.bottomMargin = dip(10)
        inflate.layoutParams = params
        return inflate
    }

    private fun initOrgHead(): LinearLayout {
        var view = layoutInflater.inflate(R.layout.org_header, null) as LinearLayout
        return view
    }

    private fun initContactHead(): LinearLayout {
        var view = layoutInflater.inflate(R.layout.org_header, null) as LinearLayout
        var icon = view.findViewById(R.id.icon) as ImageView
        var title = view.findViewById(R.id.title) as TextView
        icon.setBackgroundResource(R.drawable.contact)
        title.text = "最近联系人"
        return view
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
//        val title = TextView(this)
//        title.setTextColor(Color.BLACK)
//        title.text = "最近联系人"
//        val layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
//        layoutParams.leftMargin = Utils.dpToPx(this, 10)
//        layoutParams.topMargin = Utils.dpToPx(this, 20)
//        layoutParams.bottomMargin = Utils.dpToPx(this, 20)
//        title.layoutParams = layoutParams
        val contact = RecyclerView(this)
        contact.layoutManager = LinearLayoutManager(this)
        contactAdapter = ContactAdapter(contactList)
        contact.adapter = contactAdapter
        //layout.addView(title)
        layout.addView(contact)
        layout.id = R.id.contactLayout

        SoguApi.getService(application)
                .recentContacts()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ payload ->
                    if (payload.isOk) {
                        contactList.clear()
                        contactList.addAll(payload.payload!!)
                        contactAdapter.notifyDataSetChanged()
                    } else
                        showCustomToast(R.drawable.icon_toast_fail, payload.message)
                }, { e ->
                    Trace.e(e)
                    showCustomToast(R.drawable.icon_toast_fail, "最近联系人数据获取失败")
                })

        return layout
    }

    private fun initResultList() {
        resultAdapter = ContactAdapter(resultData)
        resultList.layoutManager = LinearLayoutManager(this)
        resultList.adapter = resultAdapter
    }

    @SuppressLint("SetTextI18n")
    fun doRequest() {
        SoguApi.getService(application)
                .userDepart()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ payload ->
                    if (payload.isOk) {
                        var i = 0
                        departList.clear()
                        payload.payload?.forEach { depart ->
                            departList.add(depart)
                            depart.data?.forEach {
                                i += 1
                                it.depart_name = depart.de_name
                                it.uid?.let {
                                    if (it == mine?.uid && fromTeam) {
                                        selectMap.put(it.toString(), true)
                                    } else {
                                        selectMap.put(it.toString(), false)
                                    }
                                }
                            }
                            alreadyList.forEach {
                                it.uid?.let {
                                    selectMap.put(it.toString(), true)
                                }
                            }
                        }
                        val num = header.find<TextView>(R.id.num)
                        num.text = "共${i}人"
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

    private var pathByUri: String? = null
    private fun getShareFile() {
        if (intent.action == Intent.ACTION_SEND && intent.extras.containsKey(Intent.EXTRA_STREAM)) {
            val uri = intent.extras.getParcelable<Uri>(Intent.EXTRA_STREAM)
            pathByUri = Utils.getFileAbsolutePathByUri(this, uri)


            AnkoLogger("WJY").info {
                "分享的文件:${Gson().toJson(uri)}\n" +
                        "path:${uri.path}--${uri.encodedPath}\n" +
                        "pathByUri:$pathByUri"
            }
        }
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
                    if (isCreateTeam) {
                        team_toolbar_title.text = "创建群组"
                    } else {
                        team_toolbar_title.text = "选择联系人"
                    }
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
                Glide.with(this@TeamSelectActivity)
                        .load(userBean.headImage())
                        .apply(RequestOptions().error(R.drawable.nim_avatar_default).placeholder(R.drawable.nim_avatar_default))
                        .into(holder.userImg)
                holder.userName.text = userBean.name
                holder.userPosition.text = userBean.position

                if (isSelectUser) {
                    holder.selectIcon.visibility = View.VISIBLE
                    userBean.uid?.let {
                        //                        if (it != mine.uid) {
//                        }
                        holder.selectIcon.isSelected = selectMap[it.toString()]!!
                    }
                } else {
                    holder.selectIcon.visibility = View.GONE
                }

                holder.itemView.setOnClickListener {
                    search_edt.clearFocus()
                    if (isSelectUser) {
                        //选人
                        val find = alreadyList.find { it.uid == userBean.uid }
                        if (find != null && !canRemove) {
                            return@setOnClickListener
                        }

                        default?.let {
                            if (it.contains(userBean.uid!!)) {
                                Toast.makeText(this@TeamSelectActivity, "默认抄送人，无法取消", Toast.LENGTH_SHORT).show()
                                return@setOnClickListener
                            }
                        }

                        if (userBean.uid != mine.uid || !fromTeam) {
                            holder.selectIcon.isSelected = !holder.selectIcon.isSelected
                            userBean.uid?.let {
                                selectMap.put(it.toString(), holder.selectIcon.isSelected)
                                val map = selectMap.filterValues { it }
                                selectNumber.text = "已选择: ${map.size}人"
                            }
                        }
                    } else {
                        //查看详情
                        PersonalInfoActivity.start(this@TeamSelectActivity, userBean, pathByUri)
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
            Glide.with(this@TeamSelectActivity)
                    .load(userBean.headImage())
                    .apply(RequestOptions().error(R.drawable.nim_avatar_default).placeholder(R.drawable.nim_avatar_default))
                    .into(holder.userImg)
            var name = userBean.name
            if (searchKey.isNotEmpty()) {
                name = name.replaceFirst(searchKey, "<font color='#1787fb'>$searchKey</font>")
            }
            holder.userName.text = Html.fromHtml(name)
            holder.userPosition.text = userBean.position

            if (isSelectUser) {
                holder.selectIcon.visibility = View.VISIBLE
                userBean.uid?.let {
                    if (it != mine!!.uid) {
                        holder.selectIcon.isSelected = selectMap[it.toString()]!!
                    } else {
                        holder.selectIcon.imageResource = R.drawable.cannot_select
                    }

                    if (holder.selectIcon.isSelected && !canRemove) {
                        holder.selectIcon.isEnabled = false
                    }
                }
            } else {
                holder.selectIcon.visibility = View.GONE
            }

            holder.itemView.setOnClickListener {
                search_edt.clearFocus()
                if (isSelectUser) {
                    //选人
                    val find = alreadyList.find { it.uid == userBean.uid }
                    if (find != null && !canRemove) {
                        return@setOnClickListener
                    }
                    default?.let {
                        if (it.contains(userBean.uid!!)) {
                            Toast.makeText(this@TeamSelectActivity, "默认抄送人，无法取消", Toast.LENGTH_SHORT).show()
                            return@setOnClickListener
                        }
                    }

                    if (userBean.uid != mine!!.uid) {
                        holder.selectIcon.isSelected = !holder.selectIcon.isSelected
                        userBean.uid?.let {
                            selectMap.put(it.toString(), holder.selectIcon.isSelected)
                            val map = selectMap.filterValues { it }
                            selectNumber.text = "已选择: ${map.size}人"
                        }
                    }
                } else {
                    //查看详情
                    PersonalInfoActivity.start(this@TeamSelectActivity, userBean, pathByUri)
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

    var default: ArrayList<Int>? = null

    companion object {

        fun startForResult(context: Context, isSelectUser: Boolean? = null,
                           alreadySelect: ArrayList<UserBean>? = null,
                           isCreateTeam: Boolean? = null,
                           fromTeam: Boolean? = null,
                           canRemoveMember: Boolean? = null,
                           requestCode: Int? = null,
                           defalut: ArrayList<Int>? = null) {
            val intent = Intent(context, TeamSelectActivity::class.java)
            intent.putExtra(Extras.SELECT_USER, isSelectUser)
            intent.putExtra(Extras.DATA, alreadySelect)
            intent.putExtra(Extras.CREATE_TEAM, isCreateTeam)
            intent.putExtra(Extras.FLAG, fromTeam)
            intent.putExtra(Extras.DEFAULT, defalut)
            intent.putExtra(Extras.CAN_REMOVE_MEMBER, canRemoveMember)
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

        fun start(context: Context, isSelectUser: Boolean? = null,
                  alreadySelectAccount: ArrayList<String>? = null,
                  isCreateTeam: Boolean? = false,
                  fromTeam: Boolean? = false,
                  canRemoveMember: Boolean? = true,
                  requestCode: Int? = null) {
            val intent = Intent(context, TeamSelectActivity::class.java)
            intent.putExtra(Extras.SELECT_USER, isSelectUser)
            intent.putExtra(Extras.DATA2, alreadySelectAccount)
            intent.putExtra(Extras.CREATE_TEAM, isCreateTeam)
            intent.putExtra(Extras.FLAG, fromTeam)
            intent.putExtra(Extras.CAN_REMOVE_MEMBER, canRemoveMember)
            val code = requestCode ?: Extras.REQUESTCODE
            if (context is Fragment) {
                context.startActivityForResult(intent, code)
            } else if (context is Activity) {
                context.startActivityForResult(intent, code)
            }
        }
    }
}
