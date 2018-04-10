package com.sogukj.pe.ui.approve

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.text.Html
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.TextView
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
import kotlinx.android.synthetic.main.sign_approve_part1.*
import kotlinx.android.synthetic.main.sign_approve_part2.*
import kotlinx.android.synthetic.main.sign_approve_part3.*
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
    var is_mine = 2
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        inflater = LayoutInflater.from(this)
        val paramObj = intent.getSerializableExtra(Extras.DATA)
        is_mine = intent.getIntExtra("is_mine", 2)
        if (paramObj == null){
            paramId = intent.getIntExtra(Extras.ID, -1)
            paramTitle = intent.getStringExtra(Extras.TITLE)
        }else{
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
        setContentView(R.layout.activity_sign_approve)
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

                        if (status != null && status >= 4) {
                            iv_state_signed.visibility = View.VISIBLE
                        } else if (status != null && status >= 3) {
                            iv_state_agreed.visibility = View.VISIBLE
                        }
                    } else {
                        showCustomToast(R.drawable.icon_toast_fail,payload.message)
                    }
                }, { e ->
                    Trace.e(e)
//                    showToast("请求失败")
                    showCustomToast(R.drawable.icon_toast_fail,"请求失败")
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
//                                showToast("保存成功")
                                showCustomToast(R.drawable.icon_toast_success,"保存成功")
                                refresh()
                            } else {
                                showCustomToast(R.drawable.icon_toast_fail,payload.message)
                            }
                        }, { e ->
                            Trace.e(e)
//                            showToast("请求失败")
                            showCustomToast(R.drawable.icon_toast_fail,"保存失败")
                        })
            } catch (e: Exception) {
//                showToast("保存失败")
                showCustomToast(R.drawable.icon_toast_fail,"保存失败")
            }
        }
        dialog.show()
    }

    private fun initButtons(click: Int?) {
        btn_single.visibility = View.GONE
        ll_twins.visibility = View.GONE
        state_sign_confirm.visibility = View.GONE
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
//                                    showToast("提交成功")
                                    showCustomToast(R.drawable.icon_toast_success,"提交成功")
                                } else {
                                    showCustomToast(R.drawable.icon_toast_fail,payload.message)
                                }
                            }, { e ->
                                Trace.e(e)
//                                showToast("请求失败")
                                showCustomToast(R.drawable.icon_toast_fail,"请求失败")
                            })
                }
            }
            4 -> {
                btn_single.visibility = View.VISIBLE
                btn_single.text = "文件签发"
                btn_single.setOnClickListener {
                    showSignDialog()
                }
            }
            5 -> {
                state_sign_confirm.visibility = View.VISIBLE
                btn_single.visibility = View.VISIBLE
                btn_single.text = "确认意见并签字"
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
                btn_single.visibility = View.VISIBLE
                btn_single.text = "导出审批单"
                btn_single.setOnClickListener {
                    SoguApi.getService(application)
                            .exportPdf(paramId!!)
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribeOn(Schedulers.io())
                            .subscribe({ payload ->
                                if (payload.isOk) {
                                    val bean = payload.payload
//                                    if (!TextUtils.isEmpty(url)) {
//                                        val intent = Intent(Intent.ACTION_VIEW)
//                                        intent.data = Uri.parse(url)
//                                        startActivity(intent)
//                                    }
                                    bean?.let {
                                        PdfUtil.loadPdf(this, it.url,it.name)
                                    }
                                } else {
                                    showCustomToast(R.drawable.icon_toast_fail,payload.message)
                                }
                            }, { e ->
                                Trace.e(e)
//                                showToast("请求失败")
                                showCustomToast(R.drawable.icon_toast_fail,"请求失败")
                            })
                }
            }
        }
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
            val convertView = inflater.inflate(R.layout.item_approve_sign_segment, null)
            ll_segments.addView(convertView)

            val ivUser = convertView.findViewById(R.id.iv_user) as CircleImageView
            val tvName = convertView.findViewById(R.id.tv_name) as TextView
            val tvTime = convertView.findViewById(R.id.tv_time) as TextView
            val ivSign = convertView.findViewById(R.id.iv_sign) as ImageView


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

            Glide.with(this)
                    .load(v.sign_img)
                    .into(ivSign)
        }

    }

    fun initApprovers(approveList: List<ApproverBean>?) {
        ll_approvers.removeAllViews()
        if (null == approveList || approveList.isEmpty()) {
            part2.visibility = View.GONE
            return
        }
        part2.visibility = View.VISIBLE
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

            Glide.with(this)
                    .load(v.sign_img)
                    .into(ivSign)
        }
    }

    private fun initFiles(file_list: List<ApproveViewBean.FileBean>?) {
        if (file_list == null || file_list.isEmpty()) {
            part1.visibility = View.GONE
            return
        }
        part1.visibility = View.VISIBLE
        ll_files.removeAllViews()
        file_list?.forEachWithIndex { i, v ->
            val view = inflater.inflate(R.layout.item_file_single, null) as TextView
            view.text = "${i + 1}、${v.file_name}"
            ll_files.addView(view)
            if (!TextUtils.isEmpty(v.url))
                view.setOnClickListener {
//                    val intent = Intent(Intent.ACTION_VIEW)
//                    intent.data = Uri.parse(v.url)
//                    startActivity(intent)
                    PdfUtil.loadPdf(this,v.url,v.file_name)
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
        buff.append("$k: <font color='#666666'>$v</font><br/>")
    }

    companion object {
        fun start(ctx: Activity?, bean: ApprovalBean, is_mine: Int) {
            val intent = Intent(ctx, SignApproveActivity::class.java)
            intent.putExtra(Extras.DATA, bean)
            intent.putExtra("is_mine", is_mine)
            ctx?.startActivity(intent)
        }

        fun start(ctx: Activity?, bean: MessageBean, is_mine: Int) {
            val intent = Intent(ctx, SignApproveActivity::class.java)
            intent.putExtra(Extras.DATA, bean)
            intent.putExtra("is_mine", is_mine)
            ctx?.startActivity(intent)
        }

        fun start(ctx: Activity?, data_id: Int, title: String) {
            val intent = Intent(ctx, SignApproveActivity::class.java)
            intent.putExtra("is_mine", 2)
            intent.putExtra(Extras.ID, data_id)
            intent.putExtra(Extras.TITLE, title)
            ctx?.startActivity(intent)
        }
        fun start(ctx: Context, approval_id:Int, is_mine: Int, title: String){
            val intent = Intent(ctx, SignApproveActivity::class.java)
            intent.putExtra(Extras.ID, approval_id)
            intent.putExtra("is_mine", is_mine)
            intent.putExtra(Extras.TITLE, title)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            ctx.startActivity(intent)
        }
    }
}