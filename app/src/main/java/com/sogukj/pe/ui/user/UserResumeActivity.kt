package com.sogukj.pe.ui.user

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.Theme
import com.framework.base.BaseActivity
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.bean.CityArea
import com.sogukj.pe.bean.Resume
import com.sogukj.pe.bean.UserBean
import com.sogukj.pe.util.Utils
import com.sogukj.service.SoguApi
import com.sogukj.util.Store
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_user_resume.*
import kotlinx.android.synthetic.main.layout_shareholder_toolbar.*

class UserResumeActivity : BaseActivity(), View.OnClickListener {
    var user: UserBean? = null
    var city: CityArea.City? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_resume)
        Utils.setWindowStatusBarColor(this, R.color.white)
        toolbar_title.text = "个人简历"
        addTv.text = "保存"

        back.setOnClickListener { finish() }
        addTv.setOnClickListener { }
        tv_add_work_expericence.setOnClickListener {
            WorkExpericenceAddActivity.start(this)
        }
        tv_add_education.setOnClickListener {
            EducationActivity.start(this)
        }
        tv_job.setOnClickListener(this)
        tv_sex.setOnClickListener(this)
        tv_language.setOnClickListener(this)
        tv_woek_experience.setOnClickListener(this)
        tv_city.setOnClickListener(this)
        tv_education.setOnClickListener(this)
        doRequest(Store.store.getUser(this)?.uid!!)
    }

    fun setData(baseInfo: Resume.BaseInfoBean) {
        baseInfo.let {
            tv_name.setText(it.name)
            tv_name.isEnabled = false
            tv_job.setText(it.position)
            when (it.sex) {
                1 -> tv_sex.text = "男"
                2 -> tv_sex.text = "女"
                else -> tv_sex.text = "请选择"
            }
            tv_language.text = it.language
            tv_woek_experience.text = it.workYear
            tv_city.text = it.cityName
            tv_education.text = it.educationLevel
            tv_mail.setText(it.email)
            tv_phone.setText(it.phone)
            tv_phone.isEnabled = false
        }
    }


    fun doRequest(uId: Int) {
        SoguApi.getService(application)
                .getPersonalResume(uid = uId)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ payload ->
                    if (payload.isOk) {
                        payload.payload?.apply {
                            this.baseInfo?.apply {
                                setData(this)
                            }
                        }
                    } else {
                        showToast(payload.message)
                    }
                })
    }


    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.tv_sex -> {
                MaterialDialog.Builder(this)
                        .title("选择性别")
                        .theme(Theme.LIGHT)
                        .items(arrayListOf("男", "女"))
                        .itemsCallbackSingleChoice(0) { dialog, itemView, which, text ->
                            tv_sex.text = text
                            true
                        }
                        .positiveText("确定")
                        .negativeText("取消")
                        .show()

            }
            R.id.tv_language -> {
                MaterialDialog.Builder(this)
                        .title("选择语言")
                        .theme(Theme.LIGHT)
                        .items(resources.getStringArray(R.array.Language).toList())
                        .itemsCallbackSingleChoice(0) { dialog, itemView, which, text ->
                            tv_language.text = text
                            true
                        }
                        .positiveText("确定")
                        .negativeText("取消")
                        .show()
            }
            R.id.tv_woek_experience -> {
                val experience = ArrayList<String>()
                (1..30).mapTo(experience) { "${it}年" }
                MaterialDialog.Builder(this)
                        .title("选择工作年限")
                        .theme(Theme.LIGHT)
                        .items(experience)
                        .itemsCallbackSingleChoice(0) { dialog, itemView, which, text ->
                            tv_woek_experience.text = text
                            true
                        }
                        .positiveText("确定")
                        .negativeText("取消")
                        .show()
            }
            R.id.tv_city -> {
                CityAreaActivity.start(this)
            }
            R.id.tv_education -> {
                MaterialDialog.Builder(this)
                        .title("选择最高学历")
                        .theme(Theme.LIGHT)
                        .items(resources.getStringArray(R.array.Education).toList())
                        .itemsCallbackSingleChoice(0) { dialog, itemView, which, text ->
                            tv_education.text = text
                            true
                        }
                        .positiveText("确定")
                        .negativeText("取消")
                        .show()
            }
        }
    }

    override fun startActivityForResult(intent: Intent?, requestCode: Int) {
        super.startActivityForResult(intent, requestCode)
        if (requestCode == Extras.REQUESTCODE && intent != null) {
            city = intent.getSerializableExtra(Extras.DATA) as CityArea.City?
            city?.let { tv_city.text = it.name }

        }
    }

    companion object {
        fun start(ctx: Activity?) {
            val intent = Intent(ctx, UserResumeActivity::class.java)
            ctx?.startActivity(intent)
        }
    }
}
