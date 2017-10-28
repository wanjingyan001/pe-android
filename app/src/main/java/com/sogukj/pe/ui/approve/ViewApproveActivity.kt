package com.sogukj.pe.ui.approve

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.text.Html
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.Theme
import com.bumptech.glide.Glide
import com.framework.base.ToolbarActivity
import com.lcodecore.tkrefreshlayout.header.progresslayout.CircleImageView
import com.sogukj.pe.App
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.bean.ApprovalBean
import com.sogukj.pe.bean.ApproveViewBean
import com.sogukj.pe.bean.ApproverBean
import com.sogukj.pe.ui.LoginActivity
import com.sogukj.pe.util.Trace
import com.sogukj.service.SoguApi
import com.sogukj.util.Store
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_view_approve.*
import org.jetbrains.anko.Android
import org.jetbrains.anko.collections.forEachByIndex
import org.jetbrains.anko.collections.forEachWithIndex

class ViewApproveActivity : ToolbarActivity() {

    lateinit var paramApprove: ApprovalBean
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        paramApprove = intent.getSerializableExtra(Extras.DATA) as ApprovalBean
        setContentView(R.layout.activity_view_approve)
        setBack(true)
        title = "用印审批"

        SoguApi.getService(application)
                .showApprove(paramApprove.approval_id!!, paramApprove.kind)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ payload ->
                    if (payload.isOk) {
                        initUser(payload.payload?.fixation)
                        initInfo(payload.payload?.relax)
                        initFiles(payload.payload?.file_list)
                        initApprovers(payload.payload?.approve)
                        initButtons(payload.payload?.click)
                    } else
                        showToast(payload.message)
                }, { e ->
                    Trace.e(e)
                    showToast("暂无可用数据")
                })

    }

    private fun initButtons(click: Int?) {
        ll_single.visibility = View.VISIBLE
        ll_twins.visibility = View.GONE
        when (click) {
            1 -> {
                btn_ok.text = "申请加急"
                btn_ok.setOnClickListener {
                    SoguApi.getService(application)
                            .approveUrgent(paramApprove.approval_id!!)
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribeOn(Schedulers.io())
                            .subscribe({ payload ->
                                if (payload.isOk) {
                                    showToast("发送成功")
                                } else
                                    showToast(payload.message)
                            }, { e ->
                                Trace.e(e)
                                showToast("请求失败")
                            })
                }
            }
            2 -> {
                btn_ok.text = "审批完成"
                iv_state_agreed.visibility = View.VISIBLE
                btn_ok.setOnClickListener {
                    finish()
                }
            }
            3 -> {
                btn_ok.text = "重新发起审批"
                btn_ok.setOnClickListener {

                }
            }
            4 -> {
                iv_state_signed.visibility = View.VISIBLE
                ll_single.visibility = View.GONE
                ll_twins.visibility = View.VISIBLE
            }
            5 -> {
                btn_ok.text = "审批"
                btn_ok.setOnClickListener {
                    MaterialDialog.Builder(this)
                            .theme(Theme.LIGHT)
                            .title("审批意见")
                            .input("输入意见...", "", true) { dialog, text ->

                            }
                            .cancelable(true)
                            .onPositive { dialog, dialogAction ->
                                val text = dialog.inputEditText?.text.toString()
                                examineApprove(1, text)
                            }
                            .onNegative { dialog, which ->
                                val text = dialog.inputEditText?.text.toString()
                                examineApprove(2, text)
                            }
                            .positiveText("确认")
                            .negativeText("否决")
                            .show()
                }
            }
        }
    }

    fun examineApprove(type: Int, text: String = "") {
        SoguApi.getService(application)
                .examineApprove(paramApprove.approval_id!!, type, text)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ payload ->
                    if (payload.isOk) {
                        showToast("提交成功")
                    } else
                        showToast(payload.message)
                }, { e ->
                    Trace.e(e)
                    showToast("提交失败")
                })
    }

    private fun initApprovers(approveList: List<ApproverBean>?) {
        ll_approvers.removeAllViews()
        val inflater = LayoutInflater.from(this)
        approveList?.forEach { v ->
            val convertView = inflater.inflate(R.layout.item_approve_view_approver, null)
            ll_approvers.addView(convertView)

            val tvPosition = convertView.findViewById(R.id.tv_position) as TextView
            val ivUser = convertView.findViewById(R.id.iv_user) as com.sogukj.pe.view.CircleImageView
            val tvName = convertView.findViewById(R.id.tv_name) as TextView
            val tvResult = convertView.findViewById(R.id.tv_result) as TextView
            val tvTime = convertView.findViewById(R.id.tv_time) as TextView
            val tvComment = convertView.findViewById(R.id.tv_comment) as TextView
            tvPosition.text = v.position
            tvName.text = v.name
            val ch = v.name?.first()
            iv_user.setChar(ch)
            Glide.with(this)
                    .load(v.url)
                    .into(ivUser)
            tvResult.text = v.status
            tvComment.text = v.content
            tvTime.text = v.approval_time
            if (null != v.content || !TextUtils.isEmpty(v.content)) {
                tvComment.visibility = View.VISIBLE
            }

            if (null != v.approval_time || !TextUtils.isEmpty(v.approval_time)) {
                tvTime.visibility = View.VISIBLE
            }


        }
    }

    private fun initFiles(file_list: List<ApproveViewBean.FileBean>?) {
        ll_files.removeAllViews()
        val inflater = LayoutInflater.from(this)
        file_list?.forEachWithIndex { i, v ->
            val view = inflater.inflate(R.layout.item_file_single, null) as TextView
            view.text = "${i + 1}、${v.file_name}"
            ll_files.addView(view)
            if (!TextUtils.isEmpty(v.url))
                view.setOnClickListener {
                    val intent = Intent(Intent.ACTION_VIEW)
                    intent.data = Uri.parse(v.url)
                    startActivity(intent)
                }
        }
    }

    private fun initInfo(relax: List<ApproveViewBean.ValueBean>?) {
        val buff = StringBuffer()
        relax?.forEach { v ->
            appendLine(buff, v.name, v.value)
        }
        tv_info.text = Html.fromHtml(buff.toString())
    }

    private fun initUser(fixation: ApproveViewBean.FromBean?) {
        if (null == fixation) return
        Glide.with(this)
                .load(fixation.url)
                .into(iv_user)
        tv_name.text = fixation.name
        tv_num.text = "审批编号:${fixation.number}"
    }

    fun appendLine(buff: StringBuffer, k: String?, v: String?) {
        if (null == k) return
        buff.append("$k: <font color='#666666'>$v</font><br/>")
    }

    companion object {
        fun start(ctx: Activity?, bean: ApprovalBean) {
            val intent = Intent(ctx, ViewApproveActivity::class.java)
            intent.putExtra(Extras.DATA, bean)
            ctx?.startActivity(intent)
        }
    }
}
