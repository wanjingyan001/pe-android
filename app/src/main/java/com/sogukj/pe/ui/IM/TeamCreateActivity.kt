package com.sogukj.pe.ui.IM

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import com.framework.base.ActivityHelper
import com.netease.nim.uikit.api.NimUIKit
import com.netease.nim.uikit.business.team.helper.TeamHelper
import com.netease.nim.uikit.common.ui.dialog.DialogMaker
import com.netease.nimlib.sdk.NIMClient
import com.netease.nimlib.sdk.RequestCallback
import com.netease.nimlib.sdk.team.TeamService
import com.netease.nimlib.sdk.team.constant.TeamFieldEnum
import com.netease.nimlib.sdk.team.constant.TeamTypeEnum
import com.netease.nimlib.sdk.team.model.CreateTeamResult
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.bean.CustomSealBean
import com.sogukj.pe.bean.UserBean
import com.sogukj.pe.ui.calendar.CompanySelectActivity
import com.sogukj.pe.ui.main.ContactsActivity
import com.sogukj.pe.util.Utils
import kotlinx.android.synthetic.main.activity_team_create.*
import org.jetbrains.anko.toast
import java.io.Serializable

class TeamCreateActivity : AppCompatActivity() {
    lateinit var teamMember: ArrayList<UserBean>
    lateinit var adapter: MemberAdapter
    var bean: CustomSealBean.ValueBean? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_team_create)
        Utils.setWindowStatusBarColor(this, R.color.color_blue_0888ff)
        team_toolbar.setNavigationIcon(R.drawable.sogu_ic_back)
        team_toolbar.setNavigationOnClickListener { finish() }
        val data = intent.getSerializableExtra(Extras.DATA)
        teamMember = if (data != null) {
            data as ArrayList<UserBean>
        } else {
            ArrayList()
        }
        adapter = MemberAdapter(this, teamMember)
        adapter.onItemClick = { v, position ->
            ContactsActivity.start(this, teamMember, true, true, Extras.REQUESTCODE)
            finish()
        }
        val layoutManager = LinearLayoutManager(this)
        layoutManager.orientation = LinearLayoutManager.HORIZONTAL
        team_member.layoutManager = layoutManager
        team_member.adapter = adapter

        team_number.text = "${teamMember.size}人"
        team_name.filters = Utils.getFilter(this)
        related_items_layout.setOnClickListener {
            CompanySelectActivity.start(this)
        }
        exit_team.setOnClickListener {
            createTeam()
        }

        //自动生成群名字
        var nameList = ArrayList<UserBean>(teamMember)
        if (teamMember.size > 4) {
            nameList.clear()
            for (index in 0 until 4) {
                nameList.add(teamMember.get(index))
            }
        }
        var nameStr = ""
        for (item in nameList) {
            nameStr = "${nameStr}、${item.name}"
        }
        if (nameList.size > 0) {
            nameStr = nameStr.removePrefix("、")
        }
        team_name.setText(nameStr)
    }

    /**
     * 创建群组
     */
    private fun createTeam() {
        val teamName = team_name.text.toString().trim()
        if (teamName.isEmpty()) {
            toast("请输入群名称")
            return
        }
        val memberAccounts = ArrayList<String>()
        if (teamMember.size == 1) {
            toast("请选择群成员")
            return
        }
        DialogMaker.showProgressDialog(this, "", true)
        teamMember.forEach {
            it.accid?.let {
                memberAccounts.add(it)
            }
        }
        val map = HashMap<TeamFieldEnum, Serializable>()
        map.put(TeamFieldEnum.Name, teamName)
        bean?.let {
            val teamBean = TeamBean()
            teamBean.project_id = it.id.toString()
            teamBean.project_name = it.name.toString()
            map.put(TeamFieldEnum.Extension, teamBean.toString())
        }
        NIMClient.getService(TeamService::class.java).createTeam(map, TeamTypeEnum.Advanced, "", memberAccounts)
                .setCallback(object : RequestCallback<CreateTeamResult> {
                    override fun onFailed(p0: Int) {
                        DialogMaker.dismissProgressDialog()
                        if (p0 == 802) {
                            toast("邀请失败，成员人数上限为200人")
                        } else {
                            toast("创建失败")
                        }
                    }

                    override fun onSuccess(p0: CreateTeamResult?) {
                        p0?.let {
                            DialogMaker.dismissProgressDialog()
                            val failedAccounts = it.failedInviteAccounts
                            if (failedAccounts != null && !failedAccounts.isEmpty()) {
                                TeamHelper.onMemberTeamNumOverrun(failedAccounts, this@TeamCreateActivity)
                            } else {
                                toast("创建成功")
                            }
                            NimUIKit.startTeamSession(this@TeamCreateActivity, it.team.id)
                            finish()
                            //去掉中间的activity(TeamSelectActivity)
                            ActivityHelper.removeTeamSelectActivity()
                        }

                    }

                    override fun onException(p0: Throwable?) {
                        DialogMaker.dismissProgressDialog()
                    }

                })

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == Extras.REQUESTCODE && data != null) {
            when (resultCode) {
                Activity.RESULT_OK -> {
                    bean = data.getSerializableExtra(Extras.DATA) as CustomSealBean.ValueBean?
                    bean?.let {
                        related_items.text = it.name
                    }
                }
                Extras.RESULTCODE -> {
                    val list = data.getSerializableExtra(Extras.DATA) as ArrayList<UserBean>?
                    list?.let {
                        teamMember.clear()
                        teamMember.addAll(it)
                        adapter.notifyDataSetChanged()
                        team_number.text = "${teamMember.size}人"
                    }
                }
            }
        }
    }

    companion object {
        fun start(ctx: Context, data: ArrayList<UserBean>?) {
            val intent = Intent(ctx, TeamCreateActivity::class.java)
            intent.putExtra(Extras.DATA, data)
            ctx.startActivity(intent)
        }
    }
}
