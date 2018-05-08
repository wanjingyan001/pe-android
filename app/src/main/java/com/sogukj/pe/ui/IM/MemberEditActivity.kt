package com.sogukj.pe.ui.IM

import android.content.Context
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.util.DiffUtil
import android.support.v7.widget.LinearLayoutManager
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.Theme
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.framework.base.BaseActivity
import com.framework.base.ToolbarActivity
import com.netease.nimlib.sdk.NIMClient
import com.netease.nimlib.sdk.RequestCallback
import com.netease.nimlib.sdk.team.TeamService
import com.netease.nimlib.sdk.team.model.Team
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.bean.UserBean
import com.sogukj.pe.view.CircleImageView
import com.sogukj.pe.view.RecyclerAdapter
import com.sogukj.pe.view.RecyclerHolder
import com.sogukj.util.Store
import com.sougukj.clickWithTrigger
import com.sougukj.setVisible
import kotlinx.android.synthetic.main.activity_member_edit.*
import kotlinx.android.synthetic.main.toolbar.*
import org.jetbrains.anko.find

class MemberEditActivity : ToolbarActivity() {
    private lateinit var memberAdapter: RecyclerAdapter<UserBean>
    private lateinit var teamMembers: ArrayList<UserBean>
    private lateinit var team: Team
    private var isEdit = false
    private val mine by lazy { Store.store.getUser(this) }
    override val menuId: Int
        get() = R.menu.menu_edit

    companion object {
        fun start(context: Context, teamMembers: ArrayList<UserBean>, team: Team) {
            val intent = Intent(context, MemberEditActivity::class.java)
            intent.putExtra(Extras.LIST, teamMembers)
            intent.putExtra(Extras.DATA, team)
            context.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_member_edit)
        teamMembers = intent.getSerializableExtra(Extras.LIST) as ArrayList<UserBean>
        team = intent.getSerializableExtra(Extras.DATA) as Team
        setBack(true)
        title = "群成员"
        supportInvalidateOptionsMenu()
        memberAdapter = RecyclerAdapter(this) { _adapter, parent, type ->
            val itemView = _adapter.getView(R.layout.item_member_list, parent)
            object : RecyclerHolder<UserBean>(itemView) {
                val deleteIcon = itemView.find<ImageView>(R.id.deleteIcon)
                val userHeadImg = itemView.find<CircleImageView>(R.id.userHeadImg)
                val userName = itemView.find<TextView>(R.id.userName)
                val creator = itemView.find<TextView>(R.id.creator)
                val userPosition = itemView.find<TextView>(R.id.userPosition)
                override fun setData(view: View, data: UserBean, position: Int) {
                    if (isEdit && data.accid != team.creator) {
                        deleteIcon.setVisible(true)
                    } else {
                        deleteIcon.setVisible(false)
                    }
                    Glide.with(this@MemberEditActivity)
                            .load(data.headImage())
                            .apply(RequestOptions().error(R.drawable.nim_avatar_default).placeholder(R.drawable.nim_avatar_default))
                            .into(userHeadImg)
                    userName.text = data.name
                    userPosition.text = data.position
                    creator.setVisible(data.accid == team.creator)
                    deleteIcon.clickWithTrigger {
                        MaterialDialog.Builder(this@MemberEditActivity)
                                .theme(Theme.LIGHT)
                                .title("警告")
                                .content("是否移除该群员")
                                .positiveText("确定")
                                .negativeText("取消")
                                .onPositive { dialog, which ->
                                    dialog.dismiss()
                                    NIMClient.getService(TeamService::class.java)
                                            .removeMember(team.id, data.accid)
                                            .setCallback(object : RequestCallback<Void> {
                                                override fun onSuccess(param: Void?) {
                                                    showCustomToast(R.drawable.icon_toast_success, "移除成功")
                                                    teamMembers.remove(data)
                                                    val result = DiffUtil.calculateDiff(DiffCallBack(memberAdapter.dataList, teamMembers))
                                                    result.dispatchUpdatesTo(memberAdapter)
                                                    memberAdapter.dataList.clear()
                                                    memberAdapter.dataList.addAll(teamMembers)
                                                }

                                                override fun onFailed(code: Int) {
                                                    showCustomToast(R.drawable.icon_toast_fail, "移除失败")
                                                }

                                                override fun onException(exception: Throwable?) {
                                                }

                                            })

                                }
                                .onNegative { dialog, which ->
                                    dialog.dismiss()
                                }
                                .show()
                    }
                }
            }
        }
        memberAdapter.onItemClick = { v, position ->
            if (!isEdit) {
                PersonalInfoActivity.start(this, memberAdapter.dataList[position].uid!!)
            }
        }
        memberAdapter.dataList.addAll(teamMembers)
        teamMemberList.apply {
            layoutManager = LinearLayoutManager(this@MemberEditActivity)
            adapter = memberAdapter
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        if (mine?.accid == team.creator) {
            menuInflater.inflate(menuId, menu)
        }
        return super.onCreateOptionsMenu(menu)
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        menu.clear()
        if (isEdit) {
            menuInflater.inflate(R.menu.menu_confirm, menu)
        } else {
            menuInflater.inflate(R.menu.menu_edit, menu)
        }
        if (mine?.accid != team.creator) {
//            menu.findItem(R.id.action_confirm).isVisible = false
            menu.findItem(R.id.edit).isVisible = false
        }
        return super.onPrepareOptionsMenu(menu)
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.edit, R.id.action_confirm -> {
                if (mine?.accid == team.creator) {
                    isEdit = !isEdit
                    supportInvalidateOptionsMenu()
                    memberAdapter.notifyDataSetChanged()
                }
            }
        }
        return super.onOptionsItemSelected(item)
    }
}
