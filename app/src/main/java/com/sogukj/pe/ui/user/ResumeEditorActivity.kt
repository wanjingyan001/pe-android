package com.sogukj.pe.ui.user

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import cn.finalteam.rxgalleryfinal.rxbus.RxBus
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

        fun start2(ctx: Activity?, dataList: ArrayList<WorkEducationBean>) {
            val intent = Intent(ctx, ResumeEditorActivity::class.java)
            intent.putExtra(Extras.TYPE, WORK)
            intent.putParcelableArrayListExtra(Extras.LIST, dataList)
            ctx?.startActivity(intent)
        }

        fun start(ctx: Activity?, dataList: ArrayList<EducationBean>) {
            val intent = Intent(ctx, ResumeEditorActivity::class.java)
            intent.putExtra(Extras.TYPE, EDU)
            intent.putParcelableArrayListExtra(Extras.LIST, dataList)
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
                val list = intent.getParcelableArrayListExtra<EducationBean>(Extras.LIST)
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
                eduadapter.dataList.addAll(list)
                resumeList.layoutManager = LinearLayoutManager(this)
                resumeList.adapter = eduadapter

                tv_add_education.setOnClickListener(this)
            }
            WORK -> {
                tv_add_education.visibility = View.GONE
                val list = intent.getParcelableArrayListExtra<WorkEducationBean>(Extras.LIST)
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
                workAdapter.dataList.addAll(list)
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
                            RxBus.getDefault().post(eduadapter.dataList[position])
                            eduadapter.dataList.removeAt(position)
                            eduadapter.notifyItemRemoved(position)
                            eduadapter.notifyDataSetChanged()
                        }
                        WORK -> {
                            RxBus.getDefault().post(workAdapter.dataList[position])
                            workAdapter.dataList.removeAt(position)
                            workAdapter.notifyItemRemoved(position)
                            workAdapter.notifyDataSetChanged()
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
