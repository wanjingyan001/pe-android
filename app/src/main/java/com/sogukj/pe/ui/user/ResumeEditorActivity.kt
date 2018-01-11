package com.sogukj.pe.ui.user

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.framework.base.BaseActivity
import com.mcxtzhang.swipemenulib.SwipeMenuLayout
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.bean.EducationBean
import com.sogukj.pe.bean.Resume
import com.sogukj.pe.bean.WorkEducationBean
import com.sogukj.pe.util.Trace
import com.sogukj.pe.util.Utils
import com.sogukj.pe.view.RecyclerAdapter
import com.sogukj.pe.view.RecyclerHolder
import com.sogukj.service.SoguApi
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_resume_editor.*
import kotlinx.android.synthetic.main.layout_shareholder_toolbar.*
import org.jetbrains.anko.find
import kotlin.properties.Delegates

class ResumeEditorActivity : BaseActivity(), View.OnClickListener {


    lateinit var eduadapter: RecyclerAdapter<EducationBean>
    lateinit var workAdapter: RecyclerAdapter<WorkEducationBean>
    private var intExtra: Int by Delegates.notNull()

    companion object {
        val EDU = 1
        val WORK = 2
        fun start(ctx: Activity?, resume: Resume) {
            val intent = Intent(ctx, ResumeEditorActivity::class.java)
            intent.putExtra(Extras.TYPE, EDU)
            intent.putExtra(Extras.DATA, resume)
            ctx?.startActivity(intent)
        }

        fun start2(ctx: Activity?, resume: Resume) {
            val intent = Intent(ctx, ResumeEditorActivity::class.java)
            intent.putExtra(Extras.TYPE, WORK)
            intent.putExtra(Extras.DATA, resume)
            ctx?.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_resume_editor)
        Utils.setWindowStatusBarColor(this, R.color.white)
        toolbar_title.text = "个人简历"
        addTv.text = "编辑"
        back.setOnClickListener(this)
        addTv.setOnClickListener(this)
        intExtra = intent.getIntExtra(Extras.TYPE, -1)
        when (intExtra) {
            EDU -> {
                tv_add_work_expericence.visibility = View.GONE
                val resume = intent.getSerializableExtra(Extras.DATA) as Resume
                eduadapter = RecyclerAdapter(this, { _adapter, parent, position ->
                    val convertView = _adapter.getView(R.layout.item_resume_editlist, parent)
                    object : RecyclerHolder<EducationBean>(convertView) {
                        val name = convertView.find<TextView>(R.id.name)
                        val time = convertView.find<TextView>(R.id.time)
                        val delete = convertView.find<TextView>(R.id.delete)
                        val deleteImg = convertView.find<ImageView>(R.id.deleteImg)
                        val educationLayout = convertView.find<SwipeMenuLayout>(R.id.educationLayout)
                        override fun setData(view: View, data: EducationBean, position: Int) {
                            name.text = data.school
                            time.text = "${data.toSchoolDate}-${data.graduationDate}"
                            delete.setOnClickListener {
                                data.id?.let {
                                    deleteExperience(it, 1, position)
                                }
                            }
                            deleteImg.visibility = if (data.isShow) View.VISIBLE else View.GONE
                            deleteImg.setOnClickListener {
                                educationLayout.smoothExpand()
                            }
                        }

                    }
                })
                resume.eduction?.let {
                    it.forEachIndexed { index, eductionBean ->
                        val edu = EducationBean()
                        edu.id = eductionBean.id
                        edu.graduationDate = eductionBean.graduationDate.toString()
                        edu.toSchoolDate = eductionBean.toSchoolDate.toString()
                        edu.education = eductionBean.education.toString()
                        edu.major = eductionBean.major.toString()
                        edu.majorInfo = eductionBean.majorInfo
                        edu.school = eductionBean.school.toString()
                        eduadapter.dataList.add(edu)
                    }
                }
                resumeList.layoutManager = LinearLayoutManager(this)
                resumeList.adapter = eduadapter

                tv_add_education.setOnClickListener(this)
            }
            WORK -> {
                tv_add_education.visibility = View.GONE
                val resume = intent.getSerializableExtra(Extras.DATA) as Resume
                workAdapter = RecyclerAdapter(this, { _adapter, parent, position ->
                    val convertView = _adapter.getView(R.layout.item_resume_editlist, parent)
                    object : RecyclerHolder<WorkEducationBean>(convertView) {
                        val name = convertView.find<TextView>(R.id.name)
                        val time = convertView.find<TextView>(R.id.time)
                        val delete = convertView.find<TextView>(R.id.delete)
                        val deleteImg = convertView.find<ImageView>(R.id.deleteImg)
                        val educationLayout = convertView.find<SwipeMenuLayout>(R.id.educationLayout)
                        override fun setData(view: View, data: WorkEducationBean, position: Int) {
                            name.text = data.company
                            time.text = "${data.employDate}-${data.leaveDate}"
                            delete.setOnClickListener {
                                data.id.let {
                                    deleteExperience(it, 2, position)
                                }
                            }
                            deleteImg.visibility = if (data.isShow) View.VISIBLE else View.GONE
                            deleteImg.setOnClickListener {
                                educationLayout.smoothExpand()
                            }
                        }
                    }
                })
                resume.work?.let {
                    val works = ArrayList<WorkEducationBean>()
                    it.forEachIndexed { i, workBean ->
                        val work = WorkEducationBean()
                        work.id = workBean.id
                        work.company = workBean.company
                        work.companyProperty = workBean.companyProperty
                        work.companyScale = workBean.companyScale
                        work.department = workBean.department
                        work.employDate = workBean.employDate
                        work.jobInfo = workBean.jobInfo
                        work.leaveDate = workBean.leaveDate
                        work.pid = workBean.pid
                        work.responsibility = workBean.responsibility
                        work.trade = workBean.trade
                        work.trade_name = workBean.trade_name
                        works.add(work)
                    }
                    workAdapter.dataList.addAll(works)
                }
                resumeList.layoutManager = LinearLayoutManager(this)
                resumeList.adapter = workAdapter

                tv_add_work_expericence.setOnClickListener(this)
            }
        }
    }


    fun deleteExperience(id: Int, type: Int, position: Int) {
        SoguApi.getService(application)
                .deleteExperience(id, type)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ payload ->
                    if (payload.isOk) {
                        showToast("删除成功")
                    } else {
                        showToast(payload.message)
                    }
                }, { e ->
                    Trace.e(e)
                }, {
                    when (intExtra) {
                        EDU -> {
                            eduadapter.dataList.removeAt(position)
                            eduadapter.notifyItemRemoved(position)
                        }
                        WORK -> {
                            workAdapter.dataList.removeAt(position)
                            workAdapter.notifyItemRemoved(position)
                        }
                    }
                    hideProgress()
                }, {
                    showProgress("正在提交删除")
                })
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.tv_add_education -> {
                EducationActivity.start(this)
            }
            R.id.tv_add_work_expericence -> {
                WorkExpericenceAddActivity.start(this)
            }
            R.id.back -> {
                onBackPressed()
            }
            R.id.addTv -> {
                addTv.isSelected = !addTv.isSelected
                addTv.text = if (addTv.isSelected) "完成" else "编辑"
                when (intExtra) {
                    EDU -> {
                        eduadapter.dataList.forEach {
                            it.isShow = addTv.isSelected
                        }
                        eduadapter.notifyDataSetChanged()
                    }
                    WORK -> {
                        workAdapter.dataList.forEach {
                            it.isShow = addTv.isSelected
                        }
                        workAdapter.notifyDataSetChanged()
                    }
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == Extras.REQUESTCODE && data != null) {
            when (resultCode) {
                Extras.RESULTCODE -> {
                    val list = data.getParcelableExtra<WorkEducationBean>(Extras.LIST)
                    workAdapter.dataList.add(list)
                    workAdapter.notifyDataSetChanged()
                }
                Extras.RESULTCODE2 -> {
                    val list = data.getParcelableExtra<EducationBean>(Extras.LIST2)
                    eduadapter.dataList.add(list)
                    eduadapter.notifyDataSetChanged()
                }
            }
        }
    }
}
