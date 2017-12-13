package com.sogukj.pe.ui.user

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import android.widget.TextView
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.Theme
import com.framework.base.BaseActivity
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.bean.*
import com.sogukj.pe.util.Trace
import com.sogukj.pe.util.Utils
import com.sogukj.pe.view.RecyclerAdapter
import com.sogukj.pe.view.RecyclerHolder
import com.sogukj.service.SoguApi
import com.sogukj.util.Store
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_user_resume.*
import kotlinx.android.synthetic.main.layout_shareholder_toolbar.*
import org.jetbrains.anko.find

class UserResumeActivity : BaseActivity(), View.OnClickListener {
    lateinit var user: UserBean
    var city: CityArea.City? = null
    lateinit var educationAdapter: RecyclerAdapter<EducationBean>
    lateinit var workAdapter: RecyclerAdapter<WorkEducationBean>
    var isSelf: Boolean = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_resume)
        Utils.setWindowStatusBarColor(this, R.color.white)
        user = intent.getSerializableExtra(Extras.DATA) as UserBean
        if (user.uid == Store.store.getUser(this)?.uid) {
            isSelf = true
            toolbar_title.text = "个人简历"
            addTv.text = "保存"
        } else {
            isSelf = false
            toolbar_title.text = "${user.name}简历"
            addTv.visibility = View.GONE
        }

        initAdapter()

        back.setOnClickListener { finish() }
        tv_add_work_expericence.setOnClickListener {
            WorkExpericenceAddActivity.start(this)
        }
        tv_add_education.setOnClickListener {
            EducationActivity.start(this)
        }
        addTv.setOnClickListener(this)
        tv_job.setOnClickListener(this)
        tv_sex.setOnClickListener(this)
        tv_language.setOnClickListener(this)
        tv_woek_experience.setOnClickListener(this)
        tv_city.setOnClickListener(this)
        tv_education.setOnClickListener(this)
        educationLayout.setOnClickListener(this)
        workLayout.setOnClickListener(this)
        if (isSelf) {
            user.uid?.let { doRequest(it) }
        } else {
            user.user_id?.let { doRequest(it) }
        }
    }

    fun setData(baseInfo: Resume.BaseInfoBean) {
        baseInfo.let {
            tv_name.setText(it.name)
            tv_name.isEnabled = false
            tv_job.setText(it.position)
            when (it.sex) {
                1 -> tv_sex.text = "男"
                2 -> tv_sex.text = "女"
            }
            tv_language.text = it.language
            tv_woek_experience.text = it.workYear
            tv_city.text = it.cityName
            tv_education.text = it.educationLevel
            tv_mail.setText(it.email)
            tv_phone.setText(it.phone)
            tv_phone.isEnabled = false
            if (!isSelf) {
                tv_job.isEnabled = false
                tv_sex.isEnabled = false
                tv_language.isEnabled = false
                tv_woek_experience.isEnabled = false
                tv_city.isEnabled = false
                tv_education.isEnabled = false
                tv_mail.isEnabled = false
            }
        }
    }

    private fun setEducationListData(listData: List<Resume.EductionBean>) {
        listData.forEach {
            val education = EducationBean()
            education.toSchoolDate = it.toSchoolDate!!
            education.graduationDate = it.graduationDate!!
            education.school = it.school!!
            education.education = it.education!!
            education.major = it.major!!
            education.majorInfo = it.majorInfo
            educationAdapter.dataList.add(education)
        }
        educationAdapter.notifyDataSetChanged()
        if (!isSelf) {
            educationList.isEnabled = false
            tv_add_education.isEnabled = false
        }
    }

    fun setWorkListData(listData: List<Resume.WorkBean>) {
        listData.forEach {
            val workeducation = WorkEducationBean()
            workeducation.employDate = it.employDate
            workeducation.leaveDate = it.leaveDate
            workeducation.company = it.company
            workeducation.responsibility = it.responsibility
            workeducation.jobInfo = it.jobInfo
            workeducation.trade = it.trade
            workeducation.department = it.department
            workeducation.companyScale = it.companyScale
            workeducation.companyProperty = it.companyProperty
            workAdapter.dataList.add(workeducation)
        }
        workAdapter.notifyDataSetChanged()
        if (!isSelf) {
            workList.isEnabled = false
            tv_add_work_expericence.isEnabled = false
        }
    }

    lateinit var resume: Resume
    fun doRequest(uId: Int) {
        SoguApi.getService(application)
                .getPersonalResume(user_id = uId)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ payload ->
                    if (payload.isOk) {
                        payload.payload?.apply {
                            resume = this
                            this.baseInfo?.apply {
                                setData(this)
                            }
                            this.eduction?.apply {
                                setEducationListData(this)
                            }
                            this.work?.apply {
                                setWorkListData(this)
                            }
                        }
                    } else {
                        showToast(payload.message)
                    }
                })
    }

    private fun editResumeBaseInfo(reqBean: UserResumeReqBean) {
        SoguApi.getService(application)
                .editResumeBaseInfo(reqBean)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ payload ->
                    if (payload.isOk) {
                        showToast("修改成功")
                    } else {
                        showToast(payload.message)
                    }
                }, { e ->
                    Trace.e(e)
                }, {}, {
                    showProgress("正在提交,请稍等")
                })
    }


    private fun initAdapter() {
        educationAdapter = RecyclerAdapter(this, { _adapter, parent, type ->
            val convertView = _adapter.getView(R.layout.item_education_list, parent)
            object : RecyclerHolder<EducationBean>(convertView) {
                val SETime = convertView.find<TextView>(R.id.start_end_time)
                val schoolName = convertView.find<TextView>(R.id.schoolName)
                val education = convertView.find<TextView>(R.id.education)
                override fun setData(view: View, data: EducationBean, position: Int) {
                    SETime.text = "${data.toSchoolDate}-${data.graduationDate}"
                    schoolName.text = data.school
                    education.text = "${data.education} | ${data.major}"
                }
            }
        })
        educationAdapter.onItemClick = { v, position ->
            EducationActivity.start(this, educationAdapter.dataList[position])
        }
        educationList.layoutManager = LinearLayoutManager(this)
        educationList.adapter = educationAdapter

        workAdapter = RecyclerAdapter(this, { _adapter, parent, type ->
            val convertView = _adapter.getView(R.layout.item_education_list, parent)
            object : RecyclerHolder<WorkEducationBean>(convertView) {
                val SETime = convertView.find<TextView>(R.id.start_end_time)
                val companyName = convertView.find<TextView>(R.id.schoolName)
                val info = convertView.find<TextView>(R.id.education)
                override fun setData(view: View, data: WorkEducationBean, position: Int) {
                    SETime.text = "${data.employDate}-${data.leaveDate}"
                    companyName.text = data.company
                    info.text = "${data.responsibility}/${data.department}"
                }
            }
        })
        workAdapter.onItemClick = { v, position ->
        }
        workList.layoutManager = LinearLayoutManager(this)
        workList.adapter = workAdapter

    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.addTv -> {
                val reqBean = UserResumeReqBean()
                reqBean.position = tv_job.text.toString()
                if (tv_sex.text.toString() == "男") {
                    reqBean.sex = 1
                } else {
                    reqBean.sex = 2
                }
                reqBean.language = tv_language.text.toString()
                reqBean.workYear = tv_woek_experience.text.toString().replace("年", "").toInt()
                reqBean.city = city?.id
                reqBean.educationLevel = tv_education.text.toString()
                reqBean.email = tv_mail.text.toString()
                editResumeBaseInfo(reqBean)
            }
            R.id.tv_sex -> {
                val position: Int
                when {
                    tv_sex.text == "男" -> position = 0
                    tv_sex.text == "女" -> position = 1
                    else -> position = -1
                }
                MaterialDialog.Builder(this)
                        .title("选择性别")
                        .theme(Theme.LIGHT)
                        .items(arrayListOf("男", "女"))
                        .itemsCallbackSingleChoice(position) { dialog, itemView, which, text ->
                            tv_sex.text = text
                            true
                        }
                        .positiveText("确定")
                        .negativeText("取消")
                        .show()

            }
            R.id.tv_language -> {
                var position = -1
                val languageList = resources.getStringArray(R.array.Language).toList()
                languageList.forEachIndexed { index, s ->
                    if (tv_language.text == s) {
                        position = index
                    }
                }
                MaterialDialog.Builder(this)
                        .title("选择语言")
                        .theme(Theme.LIGHT)
                        .items(languageList)
                        .itemsCallbackSingleChoice(position) { dialog, itemView, which, text ->
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
            R.id.educationLayout -> {
                ResumeEditorActivity.start(this, resume)
            }
            R.id.workLayout -> {
                ResumeEditorActivity.start2(this, resume)
            }
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == Extras.REQUESTCODE && data != null) {
            when (resultCode) {
                Extras.RESULTCODE3 -> {
                    city = data.getSerializableExtra(Extras.DATA) as CityArea.City?
                    city?.let { tv_city.text = it.name }
                }
                Extras.RESULTCODE -> {
                    val list = data.getParcelableExtra<WorkEducationBean>(Extras.LIST)
                    workAdapter.dataList.add(list)
                    workAdapter.notifyDataSetChanged()
                }
                Extras.RESULTCODE2 -> {
                    val list = data.getParcelableExtra<EducationBean>(Extras.LIST2)
                    educationAdapter.dataList.add(list)
                    educationAdapter.notifyDataSetChanged()
                }
            }
        }
    }


    companion object {
        fun start(ctx: Activity?, userBean: UserBean) {
            val intent = Intent(ctx, UserResumeActivity::class.java)
            intent.putExtra(Extras.DATA, userBean)
            ctx?.startActivity(intent)
        }
    }
}
