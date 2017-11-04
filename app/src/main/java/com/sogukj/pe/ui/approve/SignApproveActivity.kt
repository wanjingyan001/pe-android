package com.sogukj.pe.ui.approve

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.text.Html
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RadioGroup
import android.widget.TextView
import cn.finalteam.rxgalleryfinal.utils.BitmapUtils
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.Theme
import com.bumptech.glide.Glide
import com.framework.base.ToolbarActivity
import com.github.gcacace.signaturepad.views.SignaturePad
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.bean.*
import com.sogukj.pe.util.FileUtil
import com.sogukj.pe.util.PdfUtil
import com.sogukj.pe.util.Trace
import com.sogukj.pe.view.CircleImageView
import com.sogukj.service.SoguApi
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_sign_approve.*
import kotlinx.android.synthetic.main.state_sign_confim.*
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import org.jetbrains.anko.collections.forEachWithIndex
import java.io.File
import java.io.FileOutputStream
/**
 * Created by qinfei on 17/10/18.
 */
class SignApproveActivity : ToolbarActivity() {

    lateinit var inflater: LayoutInflater
    lateinit var paramTitle: String
    var paramId: Int? = null
    var paramType: Int? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        inflater = LayoutInflater.from(this)
        val paramObj = intent.getSerializableExtra(Extras.DATA)
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
        setContentView(R.layout.activity_sign_approve)
        setBack(true)
        title = paramTitle

        SoguApi.getService(application)
                .showApprove(approval_id = paramId!!, classify = paramType)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ payload ->
                    if (payload.isOk) {
                        initUser(payload.payload?.fixation)
                        initInfo(payload.payload?.fixation, payload.payload?.relax)
                        initFiles(payload.payload?.file_list)
                        initApprovers(payload.payload?.approve)
                        initButtons(payload.payload?.click)
                    } else
                        showToast(payload.message)
                }, { e ->
                    Trace.e(e)
                    showToast("请求失败")
                })

    }

    fun showSignDialog(type: Int = 1) {
        val dialog = MaterialDialog.Builder(this)
                .theme(Theme.LIGHT)
                .customView(R.layout.dialog_approve_sgin, false)
                .cancelable(true)
                .canceledOnTouchOutside(true)
                .build()
//                .show()
        val pad = dialog.findViewById(R.id.signature_pad) as SignaturePad
        val btnLeft = dialog.findViewById(R.id.btn_left)
        val btnRight = dialog.findViewById(R.id.btn_right)
        btnLeft.setOnClickListener {
            pad.clear()
        }
        btnRight.setOnClickListener {
            dialog.dismiss()
            try {
                val bitmap = pad.signatureBitmap
                val file = File(cacheDir, "${System.currentTimeMillis()}.jpg")
                FileUtil.createNewFile(file)
                val fos = FileOutputStream(file)
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos)
                fos.flush()
                fos.close()
                SoguApi.getService(application)
                        .approveSign(MultipartBody.Builder().setType(MultipartBody.FORM)
                                .addFormDataPart("file", file.getName(), RequestBody.create(MediaType.parse("*/*"), file))
                                .addFormDataPart("approval_id", paramId.toString())
                                .addFormDataPart("type", "${type}")
                                .build())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeOn(Schedulers.io())
                        .subscribe({ payload ->
                            if (payload.isOk) {
                                showToast("保存成功")
                                finish()
                            } else
                                showToast(payload.message)
                        }, { e ->
                            Trace.e(e)
                            showToast("请求失败")
                        })
            } catch (e: Exception) {
                showToast("保存失败")
            }
        }
        dialog.show()
    }

    private fun initButtons(click: Int?) {
        btn_single.visibility = View.GONE
        ll_twins.visibility = View.GONE
        when (click) {
            0 -> {
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
            2 -> {
                iv_state_signed.visibility = View.VISIBLE
                btn_single.visibility = View.VISIBLE
                btn_single.text = "已签字"
                iv_state_signed.visibility = View.VISIBLE
                btn_single.setOnClickListener {
                    finish()
                }
            }
            4 -> {
                btn_single.text = "文件签发"
                iv_state_agreed.visibility = View.VISIBLE
                btn_single.setOnClickListener {
                    showSignDialog()
                }
            }
            5 -> {
                btn_single.text = "确认意见并签字"
                state_sign_confirm.visibility = View.VISIBLE
                btn_single.visibility = View.VISIBLE
                ll_twins.visibility = View.GONE
                btn_single.setOnClickListener {
                    val type = when (rg_sign.checkedRadioButtonId) {
                        R.id.rb_item1 -> 1
                        R.id.rb_item2 -> 2
                        R.id.rb_item3 -> 3
                        else -> 1
                    }
                    showSignDialog(type)
                }
            }
            6 -> {
                iv_state_signed.visibility = View.VISIBLE
                btn_single.visibility = View.GONE
                ll_twins.visibility = View.VISIBLE
                btn_left.setOnClickListener {
                    SoguApi.getService(application)
                            .exportPdf(paramId!!)
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribeOn(Schedulers.io())
                            .subscribe({ payload ->
                                if (payload.isOk) {
                                    val url = payload.payload
                                    if (!TextUtils.isEmpty(url))
                                        PdfUtil.loadPdf(this, url!!)
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

    fun examineApprove(type: Int, text: String = "") {
        SoguApi.getService(application)
                .examineApprove(paramId!!, type, text)
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

    fun initApprovers(approveList: List<ApproverBean>?) {
        ll_approvers.removeAllViews()
        val inflater = LayoutInflater.from(this)
        approveList?.forEach { v ->
            val convertView = inflater.inflate(R.layout.item_approve_sign_approver, null)
            ll_approvers.addView(convertView)

            val ivUser = convertView.findViewById(R.id.iv_user) as CircleImageView
            val tvName = convertView.findViewById(R.id.tv_name) as TextView
            val tvTime = convertView.findViewById(R.id.tv_time) as TextView
            val tvStatus = convertView.findViewById(R.id.tv_status) as TextView
            val ivSign = convertView.findViewById(R.id.iv_sign) as ImageView
            tvName.text = v.name
            tvTime.text = v.approval_time
            val ch = v.name?.first()
            ivUser.setChar(ch)
            Glide.with(this)
                    .load(v.url)
                    .into(ivUser)

            Glide.with(this)
                    .load(v.sign_img)
                    .into(ivSign)
            tvStatus.text = v.status_str

            if (null != v.approval_time || !TextUtils.isEmpty(v.approval_time)) {
                tvTime.visibility = View.VISIBLE
            }
        }
    }

    private fun submitComment(hid: Int, text: String) {
        SoguApi.getService(application)
                .submitComment(hid, 0, text)
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

    private fun setComment(llComments: LinearLayout, comments: List<ApproveViewBean.CommentBean>?) {
        llComments.removeAllViews()
        if (null == comments || comments.isEmpty()) {
            llComments.visibility = View.GONE
            return
        }
        comments?.forEach { data ->
            val convertView = inflater.inflate(R.layout.item_approve_comment, null)

            val ivUser = convertView.findViewById(R.id.iv_user) as CircleImageView
            val tvName = convertView.findViewById(R.id.tv_name) as TextView
            val tvTime = convertView.findViewById(R.id.tv_time) as TextView
            val tvComment = convertView.findViewById(R.id.tv_comment) as TextView

            tvName.text = data.name
            tvComment.text = data.content
            tvTime.text = data.add_time

            val ch = data.name?.first()
            ivUser.setChar(ch)
            Glide.with(this)
                    .load(data.url)
                    .into(ivUser)

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

    private fun initInfo(from: ApproveViewBean.FromBean?, relax: List<ApproveViewBean.ValueBean>?) {
        val buff = StringBuffer()
        if (null != from) {
            appendLine(buff, "签字类别", from.sp_type)
            appendLine(buff, "提交时间", from.add_time)
        }
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
            val intent = Intent(ctx, SignApproveActivity::class.java)
            intent.putExtra(Extras.DATA, bean)
            ctx?.startActivity(intent)
        }

        fun start(ctx: Activity?, bean: MessageBean) {
            val intent = Intent(ctx, SignApproveActivity::class.java)
            intent.putExtra(Extras.DATA, bean)
            ctx?.startActivity(intent)
        }
    }
}