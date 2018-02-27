package com.sogukj.pe.ui.approve

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.text.Html
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.Theme
import com.bumptech.glide.Glide
import com.framework.base.ToolbarActivity
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.bean.*
import com.sogukj.pe.util.Trace
import com.sogukj.pe.util.Utils
import com.sogukj.pe.view.CircleImageView
import com.sogukj.service.SoguApi
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_seal_approve.*
import kotlinx.android.synthetic.main.seal_approve_part1.*
import kotlinx.android.synthetic.main.seal_approve_part2.*
import kotlinx.android.synthetic.main.seal_approve_part3.*
import org.jetbrains.anko.collections.forEachWithIndex
import org.jetbrains.anko.find
import org.jetbrains.anko.sdk25.coroutines.onClick
import java.text.SimpleDateFormat

/**
 * Created by qinfei on 17/10/18.
 */
class SealApproveActivity : ToolbarActivity() {

    lateinit var inflater: LayoutInflater
    lateinit var paramTitle: String
    var paramId: Int? = null
    var paramType: Int? = null
    var is_mine = 2
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        inflater = LayoutInflater.from(this)
        val paramObj = intent.getSerializableExtra(Extras.DATA)
        is_mine = intent.getIntExtra("is_mine", 2)
        if (paramObj == null) {
            paramId = intent.getIntExtra(Extras.ID, -1)
            paramTitle = intent.getStringExtra(Extras.TITLE)
            paramType = intent.getIntExtra(Extras.TYPE, 2)
        } else {
            if (paramObj is ApprovalBean) {
                paramTitle = paramObj.kind!!
                paramId = paramObj.approval_id!!
                paramType = paramObj.type
            } else if (paramObj is MessageBean) {
                paramTitle = paramObj.type_name!!
                paramId = paramObj.approval_id!!
                paramType = paramObj.type
            } else if (paramObj is SpGroupItemBean) {
                paramTitle = paramObj.name!!
                paramId = paramObj.id!!
                paramType = paramObj.type
            } else {
                finish()
            }
        }
        setContentView(R.layout.activity_seal_approve)
        setBack(true)
        title = paramTitle

        refresh()
    }

    fun refresh() {
        SoguApi.getService(application)
                .showApprove(approval_id = paramId!!, classify = paramType, is_mine = is_mine)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ payload ->
                    if (payload.isOk) {
                        initUser(payload.payload?.fixation)
                        initInfo(payload.payload?.fixation, payload.payload?.relax)
                        initFiles(payload.payload?.file_list)
                        initApprovers(payload.payload?.approve)
                        initSegments(payload.payload?.segment)
                        initButtons(payload.payload?.click)

                        iv_state_agreed.visibility = View.GONE
                        iv_state_signed.visibility = View.GONE

                        val status = payload?.payload?.mainStatus
                        if (status != null && status > 1) {
                            iv_state_agreed.visibility = View.VISIBLE
                        }

                    } else
                        showToast(payload.message)
                }, { e ->
                    Trace.e(e)
                    showToast("暂无可用数据")
                })
    }

    fun initButtons(click: Int?) {
        btn_single.visibility = View.GONE
        ll_twins.visibility = View.GONE
        when (click) {
            0 -> {
                btn_single.visibility = View.GONE
            }
            1 -> {
                btn_single.visibility = View.VISIBLE
                btn_single.text = "申请加急"
                btn_single.setOnClickListener {
                    SoguApi.getService(application)
                            .approveUrgent(paramId!!)
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
            3 -> {
                btn_single.visibility = View.VISIBLE
                btn_single.text = "重新发起审批"
                btn_single.setOnClickListener {
                    BuildSealActivity.start(this, paramId!!, paramType, paramTitle, true)
                    finish()
                }
            }
            4 -> {
                ll_twins.visibility = View.VISIBLE
                btn_left.setOnClickListener {
                    SoguApi.getService(application)
                            .exportPdf(paramId!!)
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribeOn(Schedulers.io())
                            .subscribe({ payload ->
                                if (payload.isOk) {
                                    val url = payload.payload
                                    if (!TextUtils.isEmpty(url)) {
                                        val intent = Intent(Intent.ACTION_VIEW)
                                        intent.data = Uri.parse(url)
                                        startActivity(intent)
                                    }
                                } else
                                    showToast(payload.message)
                            }, { e ->
                                Trace.e(e)
                                showToast("请求失败")
                            })
                }
                btn_right.setOnClickListener {
                    SoguApi.getService(application)
                            .finishApprove(paramId!!)
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribeOn(Schedulers.io())
                            .subscribe({ payload ->
                                if (payload.isOk) {
                                    refresh()
                                } else
                                    showToast(payload.message)
                            }, { e ->
                                Trace.e(e)
                                showToast("请求失败")
                            })
                }
            }
            5 -> {
                btn_single.visibility = View.VISIBLE
                btn_single.text = "审批"
                btn_single.setOnClickListener {
                    val inflate = LayoutInflater.from(this).inflate(R.layout.layout_input_dialog, null)
                    val dialog = MaterialDialog.Builder(this)
                            .customView(inflate, false)
                            .cancelable(true)
                            .build()
                    dialog.window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                    val commentInput = inflate.find<EditText>(R.id.approval_comments_input)
                    val veto = inflate.find<TextView>(R.id.veto_comment)
                    val confirm = inflate.find<TextView>(R.id.confirm_comment)
                    commentInput.filters = Utils.getFilter(this)
                    veto.setOnClickListener {
                        if (dialog.isShowing) {
                            dialog.dismiss()
                        }
                        showConfirmDialog(-1, commentInput.text.toString())
                    }
                    confirm.setOnClickListener {
                        if (dialog.isShowing) {
                            dialog.dismiss()
                        }
                        showConfirmDialog(1, commentInput.text.toString())
                    }
                    dialog.show()
                }
            }
            6 -> {
                btn_single.visibility = View.VISIBLE
                btn_single.text = "导出审批单"
                btn_single.setOnClickListener {
                    SoguApi.getService(application)
                            .exportPdf(paramId!!)
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribeOn(Schedulers.io())
                            .subscribe({ payload ->
                                if (payload.isOk) {
                                    val url = payload.payload
                                    if (!TextUtils.isEmpty(url)) {
                                        val intent = Intent(Intent.ACTION_VIEW)
                                        intent.data = Uri.parse(url)
                                        startActivity(intent)
                                    }
//                                        PdfUtil.loadPdf(this, url!!)
                                } else
                                    showToast(payload.message)
                            }, { e ->
                                Trace.e(e)
                                showToast("请求失败")
                            })
                }


            }
        }
    }

    private fun showConfirmDialog(type: Int, text: String = "") {
        val title = if (type == 1) "是否确认通过审批" else "是否确认否决审批"
        val build = MaterialDialog.Builder(this)
                .theme(Theme.LIGHT)
                .customView(R.layout.layout_confirm_approve, false)
                .canceledOnTouchOutside(false)
                .build()
        build.window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        val titleTv = build.find<TextView>(R.id.confirm_title)
        val cancel = build.find<TextView>(R.id.cancel_comment)
        val confirm = build.find<TextView>(R.id.confirm_comment)
        titleTv.text = title
        cancel.setOnClickListener {
            if (build.isShowing) {
                build.dismiss()
            }
        }
        confirm.setOnClickListener {
            if (build.isShowing) {
                build.dismiss()
            }
            examineApprove(type, text)
        }
        build.show()
    }

    fun examineApprove(type: Int, text: String = "") {
        SoguApi.getService(application)
                .examineApprove(paramId!!, type, text)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ payload ->
                    if (payload.isOk) {
                        showToast("提交成功")
                        refresh()
                    } else
                        showToast(payload.message)
                }, { e ->
                    Trace.e(e)
                    showToast("提交失败")
                })
    }


    fun initSegments(segments: List<ApproverBean>?) {
        ll_segments.removeAllViews()
        if (null == segments || segments.isEmpty()) {
            part3.visibility = View.GONE
            return
        }
        part3.visibility = View.VISIBLE
        val inflater = LayoutInflater.from(this)
        segments?.forEach { v ->
            val convertView = inflater.inflate(R.layout.item_approve_seal_segment, null)
            ll_segments.addView(convertView)

            val tvPosition = convertView.findViewById(R.id.tv_position) as TextView
            val ivUser = convertView.findViewById(R.id.iv_user) as com.sogukj.pe.view.CircleImageView
            val tvName = convertView.findViewById(R.id.tv_name) as TextView
            val tvStatus = convertView.findViewById(R.id.tv_status) as TextView
            val tvTime = convertView.findViewById(R.id.tv_time) as TextView
            tvPosition.text = v.position
            tvName.text = v.name
            tvStatus.text = v.status_str
            tvTime.text = v.approval_time
            if (null != v.approval_time || !TextUtils.isEmpty(v.approval_time)) {
                tvTime.visibility = View.VISIBLE
            } else {
                tvTime.visibility = View.GONE
            }
            val ch = v.name?.first()
            ivUser.setChar(ch)
            Glide.with(this)
                    .load(v.url)
                    .into(ivUser)

        }

    }

    private fun initApprovers(approveList: List<ApproverBean>?) {
        ll_approvers.removeAllViews()
        if (null == approveList || approveList.isEmpty()) {
            part2.visibility = View.GONE
            return
        }
        part2.visibility = View.VISIBLE
        val inflater = LayoutInflater.from(this)
        approveList?.forEach { v ->
            val convertView = inflater.inflate(R.layout.item_approve_seal_approver, null)
            ll_approvers.addView(convertView)

            val tvPosition = convertView.findViewById(R.id.tv_position) as TextView
            val ivUser = convertView.findViewById(R.id.iv_user) as CircleImageView
            val tvName = convertView.findViewById(R.id.tv_name) as TextView
            val tvStatus = convertView.findViewById(R.id.tv_status) as TextView
            val tvTime = convertView.findViewById(R.id.tv_time) as TextView
            val tvEdit = convertView.findViewById(R.id.tv_edit) as TextView
            val tvContent = convertView.findViewById(R.id.tv_content) as TextView
            val llComments = convertView.findViewById(R.id.ll_comments) as LinearLayout

            if (v?.status == 3) {
                tvEdit.visibility = View.VISIBLE
                if (v?.is_edit_file == 1) {
                    tvEdit.text = "文件已修改"
                    tvEdit.setBackgroundResource(R.drawable.bg_tag_edit_file_1)
                } else {
                    tvEdit.text = "文件未修改"
                    tvEdit.setBackgroundResource(R.drawable.bg_tag_edit_file_0)
                }
            } else
                tvEdit.visibility = View.GONE
            tvPosition.text = v.position
            tvName.text = v.name
            tvTime.text = v.approval_time
            if (null != v.approval_time || !TextUtils.isEmpty(v.approval_time)) {
                tvTime.visibility = View.VISIBLE
            } else {
                tvTime.visibility = View.GONE
            }
            val ch = v.name?.first()
            ivUser.setChar(ch)
            Glide.with(this)
                    .load(v.url)
                    .into(ivUser)
            tvStatus.text = v.status_str
            val buff = StringBuffer()
            if (null != v.content) {
                appendLine(buff, "意见", v.content)
            }
            tvContent.text = Html.fromHtml(buff.toString())
            tvContent.visibility = View.GONE
            llComments.visibility = View.GONE
            if (null != v.content && !TextUtils.isEmpty(v.content)) {
                tvContent.visibility = View.VISIBLE
                tvContent.setOnClickListener {
                    doComment(llComments, v.hid!!)
                }

                if (null != v.comment && v.comment!!.isNotEmpty()) {
                    llComments.visibility = View.VISIBLE
                    llComments.removeAllViews()
                    v.comment?.forEach { data ->
                        addComment(llComments, v.hid!!, data)
                    }
                }
            }
        }
    }

    fun doComment(llComments: LinearLayout, hid: Int, commentId: Int = 0) {
        val inflate = LayoutInflater.from(this).inflate(R.layout.layout_input_dialog, null)
        val dialog = MaterialDialog.Builder(this)
                .theme(Theme.LIGHT)
                .customView(inflate, false)
                .cancelable(true)
                .build()
        dialog.window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        val title = inflate.find<TextView>(R.id.approval_comments_title)
        val commentInput = inflate.find<EditText>(R.id.approval_comments_input)
        val veto = inflate.find<TextView>(R.id.veto_comment)
        val confirm = inflate.find<TextView>(R.id.confirm_comment)
        title.text = "评论"
        commentInput.filters = Utils.getFilter(this)
        commentInput.hint = "请输入评论文字..."
        veto.text = resources.getString(R.string.cancel)
        confirm.text = resources.getString(R.string.confirm)
        veto.setOnClickListener {
            Utils.closeInput(this,commentInput)
            if (dialog.isShowing) {
                dialog.dismiss()
            }
        }
        confirm.setOnClickListener {
            Utils.closeInput(this,commentInput)
            if (dialog.isShowing) {
                dialog.dismiss()
            }
            val text = commentInput.text.toString()
            if (!TextUtils.isEmpty(text))
                SoguApi.getService(application)
                        .submitComment(hid, comment_id = commentId, content = text)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeOn(Schedulers.io())
                        .subscribe({ payload ->
                            if (payload.isOk) {
                                showToast("提交成功")
                                refresh()
                            } else
                                showToast(payload.message)
                        }, { e ->
                            Trace.e(e)
                            showToast("提交失败")
                        })
        }
        dialog.show()
    }

    val fmt_time = SimpleDateFormat("")
    fun addComment(llComments: LinearLayout, hid: Int, data: ApproveViewBean.CommentBean) {
        val convertView = inflater.inflate(R.layout.item_approve_comment, null)
        llComments.addView(convertView)
        val ivUser = convertView.findViewById(R.id.iv_user) as CircleImageView
        val tvName = convertView.findViewById(R.id.tv_name) as TextView
        val tvTime = convertView.findViewById(R.id.tv_time) as TextView
        val tvComment = convertView.findViewById(R.id.tv_comment) as TextView

        tvName.text = data.name
        tvTime.text = data.add_time
        val buff = StringBuffer()
        if (!TextUtils.isEmpty(data.reply))
            buff.append("回复<font color='#608cf8'>${data.reply}</font>")
        buff.append(data.content)
        tvComment.text = Html.fromHtml(buff.toString())
        val ch = data.name?.first()
        ivUser.setChar(ch)
        Glide.with(this)
                .load(data.url)
                .into(ivUser)

        convertView.setOnClickListener {
            doComment(llComments, hid, data.comment_id!!)
        }
    }

    private fun initFiles(file_list: List<ApproveViewBean.FileBean>?) {
        if (file_list == null || file_list.isEmpty()) {
            part1.visibility = View.GONE
            return
        }
        part1.visibility = View.VISIBLE
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

    private fun initInfo(from: ApproveViewBean.FromBean?, relax: List<ApproveViewBean.ValueBean>?) {
        val buff = StringBuffer()
        if (null != from) {
            appendLine(buff, "用印类别", from.sp_type)
            appendLine(buff, "提交时间", from.add_time)
        }
        relax?.forEach { v ->
            appendLine(buff, v.name, v.value)
        }
        tv_info.text = Html.fromHtml(buff.toString())
    }

    private fun initUser(fixation: ApproveViewBean.FromBean?) {
        if (null == fixation) return
        val ch = fixation.name?.first()
        iv_user.setChar(ch)
        Glide.with(this)
                .load(fixation.url)
                .into(iv_user)
        tv_name.text = fixation.name
        tv_num.text = "审批编号:${fixation.number}"
    }

    fun appendLine(buff: StringBuffer, k: String?, v: String?) {
        if (null == k) return
        val sval = if (TextUtils.isEmpty(v)) "" else v
        buff.append("$k: <font color='#666666'>$sval</font><br/>")
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == BuildSealActivity.REQ_EDIT && resultCode === Activity.RESULT_OK) {
            val id = data?.getIntExtra(Extras.ID, paramId!!)
            if (null != id) {
                paramId = id
                refresh()
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    companion object {
        fun start(ctx: Activity?, bean: ApprovalBean, is_mine: Int) {
            val intent = Intent(ctx, SealApproveActivity::class.java)
            intent.putExtra(Extras.DATA, bean)
            intent.putExtra("is_mine", is_mine)
            ctx?.startActivity(intent)
        }

        fun start(ctx: Activity?, bean: MessageBean, is_mine: Int) {
            val intent = Intent(ctx, SealApproveActivity::class.java)
            intent.putExtra("is_mine", is_mine)
            intent.putExtra(Extras.DATA, bean)
            ctx?.startActivity(intent)
        }

        fun start(ctx: Activity?, data_id: Int, title: String) {
            val intent = Intent(ctx, SealApproveActivity::class.java)
            intent.putExtra("is_mine", 2)
            intent.putExtra(Extras.ID, data_id)
            intent.putExtra(Extras.TITLE, title)
            ctx?.startActivity(intent)
        }

        fun start(ctx: Context, approval_id: Int, is_mine: Int, title: String, type: Int? = 2) {
            val intent = Intent(ctx, SealApproveActivity::class.java)
            intent.putExtra(Extras.ID, approval_id)
            intent.putExtra("is_mine", is_mine)
            intent.putExtra(Extras.TITLE, title)
            intent.putExtra(Extras.TYPE, type)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            ctx.startActivity(intent)
        }
    }
}
