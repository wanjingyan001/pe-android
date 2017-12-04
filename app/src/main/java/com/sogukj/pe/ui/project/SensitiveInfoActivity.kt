package com.sogukj.pe.ui.project

import android.content.Context
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import com.framework.base.BaseActivity
import com.google.gson.Gson
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.bean.CreditInfo
import com.sogukj.pe.bean.SensitiveInfo
import com.sogukj.pe.bean.SensitiveReqBean
import com.sogukj.pe.util.Trace
import com.sogukj.pe.util.Utils
import com.sogukj.service.SoguApi
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_sensitive_info.*
import kotlinx.android.synthetic.main.layout_shareholder_toolbar.*
import org.jetbrains.anko.imageResource
import org.jetbrains.anko.textColor

class SensitiveInfoActivity : BaseActivity(), View.OnClickListener {
    lateinit var info: SensitiveInfo
    lateinit var data: CreditInfo.Item

    companion object {
        val TAG = SensitiveInfoActivity::class.java.simpleName
        val COURTNOTICE = 1//法庭公告
        val COURTROOMT = 2//开庭公告
        val REFEREEDOCUMENTS = 3//裁判文书
        val EXECUTIVEBULLETIN = 4//执行公告
        val LOSSCREDIT = 5//法院失信名单
        val CASEDETEIL = 6//案件详情
        fun start(ctx: Context?, data: CreditInfo.Item) {
            val intent = Intent(ctx, SensitiveInfoActivity::class.java)
            intent.putExtra(Extras.DATA, data)
            ctx?.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sensitive_info)
        Utils.setWindowStatusBarColor(this, R.color.white)
        data = intent.getSerializableExtra(Extras.DATA) as CreditInfo.Item
        addTv.visibility = View.GONE
        toolbar_title.text = "征信"
        name.text = data.name
        post.text = data.position
        data.piece?.let {
            doRequest(data.id, Gson().toJson(it))
        }
        back.setOnClickListener(this)
    }


    fun doRequest(id: Int, piece: String) {
        val reqBean = SensitiveReqBean(id, piece)
        SoguApi.getService(application)
                .sensitiveData(reqBean)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ payload ->
                    if (payload.isOk) {
                        payload.payload?.apply {
                            info = this
                            this.crime?.let {
                                setDangerousStatus(it)
                                if (it.num == 0) {
                                    status.text = "正常"
                                    dangerImage.imageResource = R.drawable.ic_sensitive_green
                                    toDetail.setOnClickListener(null)
                                } else {
                                    status.text = "风险人物"
                                    dangerImage.imageResource = R.drawable.ic_sensitive_red
                                    infoNumber.text = "${it.num}条"
                                    toDetail.setOnClickListener(this@SensitiveInfoActivity)
                                }
                            }
                            this.court?.let {
                                setInfo(courtNoticeCount, it.item.fygg != 0, "${it.item.fygg}条记录")
                                setInfo(courtroomtNoticeCount, it.item.ktgg != 0, "${it.item.ktgg}条记录")
                                setInfo(refereeDocumentsCount, it.item.cpws != 0, "${it.item.cpws}条记录")
                                setInfo(executiveBulletinCount, it.item.zxgg != 0, "${it.item.zxgg}条记录")
                                recordNumber2.setOnClickListener(this@SensitiveInfoActivity)
                            }
                            this.dishonest?.let { setLostList(it) }
                            this.bankRisk?.let { setBankRisk(it) }
                        }
                    } else {
                        showToast(payload.message)
                    }
                }, { e ->
                    Trace.e(e)
                })

    }

    /**
     * 涉诉记录
     */
    private fun setInfo(tv: TextView, flag: Boolean, info: String = "无") {
        if (flag) {
            tv.text = info
            tv.textColor = resources.getColor(R.color.fund_deep_red)
            val drawable = resources.getDrawable(R.drawable.ic_right)
            drawable.setBounds(0, 0, drawable.minimumWidth, drawable.minimumHeight)
            tv.setCompoundDrawables(null, null, drawable, null)
            tv.compoundDrawablePadding = Utils.dpToPx(this, 10)
            tv.setOnClickListener(this)
        } else {
            tv.text = "无"
            tv.textColor = resources.getColor(R.color.text_1)
            tv.setCompoundDrawables(null, null, null, null)
            tv.setOnClickListener(null)
        }
    }

    /**
     * 银行风险用户
     */
    private fun setBankRisk(bankRisk: SensitiveInfo.BankRisk) {
        lowRisk.isEnabled = bankRisk.bank_bad == 1
        mediumRisk.isEnabled = bankRisk.bank_overdue == 1
        highRisk.isEnabled = bankRisk.bank_lost == 1
        DangerousCredit.isEnabled = bankRisk.bank_fraud == 1
        refuse.isEnabled = bankRisk.bank_refuse == 1
    }


    /**
     * 法院失信名单
     */
    private fun setLostList(dishonest: SensitiveInfo.Dishonest) {
        recordNumber2.text = "${dishonest.num}条记录"
        if (dishonest.item.size != 0) {
            filingDate.text = dishonest.item[0].time
            caseNumberTv.text = dishonest.item[0].casenum
            executionCourtTv.text = dishonest.item[0].court
            fulfillStateTv.text = dishonest.item[0].performance
            executionBasisTv.text = dishonest.item[0].base
        }
    }

    /**
     * 危险身份
     */
    private fun setDangerousStatus(crime: SensitiveInfo.Crime) {
        crime.checkCode.forEach {
            when (it) {
                "1" -> {
                    isSuspects.text = "是"
                    isSuspects.textColor = resources.getColor(R.color.fund_deep_red)
                }
                "2" -> {
                    isFormerStaff.text = "是"
                    isFormerStaff.textColor = resources.getColor(R.color.fund_deep_red)
                }
                "3" -> {
                    isDrugRelated.text = "是"
                    isDrugRelated.textColor = resources.getColor(R.color.fund_deep_red)
                }
                "4" -> {
                    isDrugAddicts.text = "是"
                    isDrugAddicts.textColor = resources.getColor(R.color.fund_deep_red)
                }
            }
        }
        caseNumber2.text = "${crime.num}件"
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.back -> finish()
            R.id.courtNoticeCount -> SecondaryActivity.start(this, COURTNOTICE, info, data.id)
            R.id.courtroomtNoticeCount -> SecondaryActivity.start(this, COURTROOMT, info, data.id)
            R.id.refereeDocumentsCount -> SecondaryActivity.start(this, REFEREEDOCUMENTS, info, data.id)
            R.id.executiveBulletinCount -> SecondaryActivity.start(this, EXECUTIVEBULLETIN, info, data.id)
            R.id.recordNumber2 -> SecondaryActivity.start(this, LOSSCREDIT, info, data.id)
            R.id.toDetail -> SecondaryActivity.start(this, CASEDETEIL, info, data.id)
        }
    }

}