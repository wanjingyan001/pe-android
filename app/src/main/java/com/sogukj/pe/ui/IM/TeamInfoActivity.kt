package com.sogukj.pe.ui.IM

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar
import android.view.View
import android.widget.RelativeLayout
import android.widget.TextView
import com.afollestad.materialdialogs.MaterialDialog
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.gson.Gson
import com.netease.nim.uikit.api.NimUIKit
import com.netease.nim.uikit.business.team.helper.TeamHelper
import com.netease.nimlib.sdk.NIMClient
import com.netease.nimlib.sdk.RequestCallback
import com.netease.nimlib.sdk.team.TeamService
import com.netease.nimlib.sdk.team.model.Team
import com.netease.nimlib.sdk.team.model.TeamMember
import com.netease.nimlib.sdk.uinfo.model.NimUserInfo
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.bean.UserBean
import com.sogukj.pe.util.Utils
import kotlinx.android.synthetic.main.activity_team_info.*
import org.jetbrains.anko.toast


class TeamInfoActivity : AppCompatActivity(), View.OnClickListener {
    lateinit var sessionId: String
    lateinit var toolbar: Toolbar
    lateinit var teamMember: RecyclerView
    lateinit var teamLayout: RelativeLayout
    var teamMembers = ArrayList<UserBean>()
    var adapter: MemberAdapter? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_team_info)
        Utils.setWindowStatusBarColor(this, R.color.color_blue_0888ff)
        sessionId = intent.getStringExtra("sessionId")
        toolbar = findViewById(R.id.team_toolbar) as Toolbar
        toolbar.setNavigationIcon(R.drawable.sogu_ic_back)
        toolbar.setNavigationOnClickListener { finish() }
        teamMember = findViewById(R.id.team_member) as RecyclerView
        teamLayout = findViewById(R.id.team_layout) as RelativeLayout
        val manager = LinearLayoutManager(this)
        manager.orientation = LinearLayoutManager.HORIZONTAL
        teamMember.layoutManager = manager
        adapter = MemberAdapter(this, teamMembers)
        teamMember.adapter = adapter

        val teamPic = findViewById(R.id.team_pic) as TextView
        val teamFile = findViewById(R.id.team_file) as TextView
        val teamLink = findViewById(R.id.team_link) as TextView
        val teamSearch = findViewById(R.id.team_search) as TextView

        teamPic.setOnClickListener(this)
        teamFile.setOnClickListener(this)
        teamLink.setOnClickListener(this)
        teamSearch.setOnClickListener(this)
        teamLayout.setOnClickListener(this)
        exit_team.setOnClickListener(this)
        doRequest()
    }


    fun doRequest() {
        NIMClient.getService(TeamService::class.java).queryTeam(sessionId).setCallback(object : RequestCallback<Team> {
            override fun onSuccess(p0: Team?) {
                p0?.let {
                    Glide.with(this@TeamInfoActivity)
                            .load(it.icon)
                            .apply(RequestOptions().error(R.drawable.invalid_name2))
                            .into(team_logo)
                    team_title.text = it.name
                    val bean = Gson().fromJson(it.extension, TeamBean::class.java)
                    if (bean != null) {
                        team_project.text = bean.project_name
                    }
                    team_number.text = "${it.memberCount}人"
                    team_name.text = it.name
                    NIMClient.getService(TeamService::class.java).queryMemberList(it.id).setCallback(object : RequestCallback<List<TeamMember>> {
                        override fun onSuccess(p0: List<TeamMember>?) {
                            val accounts = ArrayList<String>()
                            p0?.let {
                                it.forEach {
                                    accounts.add(it.account)
                                }
                            }
                            getUsersInfoAsync(accounts)
                        }

                        override fun onFailed(p0: Int) {

                        }

                        override fun onException(p0: Throwable?) {
                        }
                    })
                }
            }

            override fun onFailed(p0: Int) {
            }

            override fun onException(p0: Throwable?) {
            }
        })
    }

    /**
     * 获取用户资料
     */
    fun getUsersInfoAsync(accounts: List<String>) {
        NimUIKit.getUserInfoProvider().getUserInfoAsync(accounts) { success, result, code ->
            result.forEach {
                val info = it as NimUserInfo
                val uid = info.extensionMap["uid"] as Int
                val user = UserBean()
                user.uid = uid
                user.user_id = uid
                user.url = info.avatar
                user.accid = info.account
                teamMembers.add(user)
            }
            adapter?.notifyDataSetChanged()
        }
    }


    override fun onClick(v: View) {
        val i = v.id
        when (i) {
            R.id.team_pic -> startActivity(Intent(this, TeamPictureActivity::class.java))
            R.id.team_file -> startActivity(Intent(this, TeamHistoryFileActivity::class.java))
            R.id.team_link -> {

            }
            R.id.team_search -> {
                val intent = Intent(this, TeamSearchActivity::class.java)
                intent.putExtra("sessionId", getIntent().getStringExtra("sessionId"))
                startActivity(intent)
            }
            R.id.team_layout -> {
                TeamSelectActivity.startForResult(this, true, teamMembers, false)
            }
            R.id.exit_team -> {
                MaterialDialog.Builder(this)
                        .title("确定退出?")
                        .content("是否退出${team_title.text}")
                        .positiveText("确认")
                        .negativeText("取消")
                        .onPositive { dialog, which ->
                            NIMClient.getService(TeamService::class.java).quitTeam(sessionId).setCallback(object : RequestCallback<Void> {
                                override fun onFailed(p0: Int) {

                                }

                                override fun onException(p0: Throwable?) {
                                }

                                override fun onSuccess(p0: Void?) {
                                    toast("您已退出该群")
                                    finish()
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == Extras.REQUESTCODE && data != null) {
            when (resultCode) {
                Extras.RESULTCODE -> {
                    val newMembers = data.getSerializableExtra(Extras.DATA) as ArrayList<UserBean>
                    teamMembers.forEach { oldMember ->
                        val bean = newMembers.find { it.accid == oldMember.accid }
                        newMembers.remove(bean)
                    }
                    val accounts = ArrayList<String>()
                    newMembers.forEach {
                        it.accid?.let {
                            accounts.add(it)
                        }
                    }
                    NIMClient.getService(TeamService::class.java).addMembers(sessionId, accounts)
                            .setCallback(object : RequestCallback<List<String>> {
                                override fun onSuccess(p0: List<String>?) {
                                    if (p0 != null && !p0.isEmpty()) {
                                        TeamHelper.onMemberTeamNumOverrun(p0, this@TeamInfoActivity)
                                    } else {
                                        toast("邀请成功")
                                    }
                                    p0?.forEach { account ->
                                        newMembers.find { it.accid == account }?.let {
                                            teamMembers.add(it)
                                        }
                                    }
                                    team_number.text = "${teamMembers.size}人"
                                    adapter?.notifyDataSetChanged()
                                }

                                override fun onFailed(p0: Int) {
                                    if (p0 == 408) {
                                        toast("请检查网络")
                                    } else {
                                        toast("邀请失败")
                                    }
                                }

                                override fun onException(p0: Throwable?) {
                                }
                            })
                }
            }
        }
    }


    companion object {

        fun start(context: Context, sessionId: String) {
            val intent = Intent(context, TeamInfoActivity::class.java)
            intent.putExtra("sessionId", sessionId)
            context.startActivity(intent)
        }
    }

}