package com.sogukj.pe.ui

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import com.framework.base.ToolbarActivity
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.bean.ProjectBean
import com.sogukj.pe.bean.ProjectBookBean
import com.sogukj.pe.util.Trace
import com.sogukj.pe.view.ListAdapter
import com.sogukj.pe.view.ListHolder
import com.sogukj.service.SoguApi
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_list_project_book.*
import java.text.SimpleDateFormat


/**
 * Created by qinfei on 17/8/11.
 */
class ProjectBookActivity : ToolbarActivity(), SupportEmptyView {

    lateinit var adapter1: ListAdapter<ProjectBookBean>
    lateinit var adapter2: ListAdapter<ProjectBookBean>
    lateinit var adapter3: ListAdapter<ProjectBookBean>
    lateinit var project: ProjectBean
    val df = SimpleDateFormat("yyyy-MM-dd")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_list_project_book)

        project = intent.getSerializableExtra(Extras.DATA) as ProjectBean
        setBack(true)
        setTitle("项目文书")
        btn_upload.setOnClickListener {
            ProjectBookUploadActivity.start(this, project)
        }
        adapter1 = ListAdapter<ProjectBookBean> { ProjectBookHolder() }
        adapter2 = ListAdapter<ProjectBookBean> { ProjectBookHolder() }
        adapter3 = ListAdapter<ProjectBookBean> { ProjectBookHolder() }

        list1.adapter = adapter1
        list2.adapter = adapter2
        list3.adapter = adapter3
        tv_more1.setOnClickListener {
            NegativeNewsActivity.start(this, project, 1)
        }
        tv_more2.setOnClickListener {
            NegativeNewsActivity.start(this, project, 2)
        }
        tv_more3.setOnClickListener {
            NegativeNewsActivity.start(this, project, 2)
        }
        list1.setOnItemClickListener { parent, view, position, id ->
            val data = adapter1.dataList[position]
//            NewsDetailActivity.start(this, data)
        }
        list2.setOnItemClickListener { parent, view, position, id ->
            val data = adapter2.dataList[position]
//            NewsDetailActivity.start(this, data)
        }
        list3.setOnItemClickListener { parent, view, position, id ->
            val data = adapter3.dataList[position]
//            NewsDetailActivity.start(this, data)
        }
        handler.postDelayed({
            doRequest()
        }, 100)
    }

    var page = 1
    fun doRequest() {
        project.company_id = 325
        SoguApi.getService(application)
                .projectBook(company_id = project.company_id!!)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ payload ->
                    if (payload.isOk) {
                        adapter1.dataList.clear()
                        adapter2.dataList.clear()
                        adapter3.dataList.clear()
                        payload.payload?.apply {
                            if (null != list1)
                                adapter1.dataList.addAll(list1!!.asIterable())
                            if (null != list2)
                                adapter2.dataList.addAll(list2!!.asIterable())
                            if (null != list3)
                                adapter3.dataList.addAll(list3!!.asIterable())
                        }
                        adapter1.notifyDataSetChanged()
                        adapter2.notifyDataSetChanged()
                        adapter3.notifyDataSetChanged()
                    } else
                        showToast(payload.message)
                }, { e ->
                    Trace.e(e)
                }, {
                    if (adapter1.dataList.size < 3)
                        tv_more1.visibility = View.GONE
                    else
                        tv_more1.visibility = View.VISIBLE

                    if (adapter2.dataList.size < 3)
                        tv_more2.visibility = View.GONE
                    else
                        tv_more2.visibility = View.VISIBLE

                    if (adapter3.dataList.size < 3)
                        tv_more3.visibility = View.GONE
                    else
                        tv_more3.visibility = View.VISIBLE
                })
    }

    class ProjectBookHolder
        : ListHolder<ProjectBookBean> {
        lateinit var tvSummary: TextView
        lateinit var tvDate: TextView
        lateinit var tvTime: TextView
        lateinit var tvType: TextView
        override fun createView(inflater: LayoutInflater): View {
            val convertView = inflater.inflate(R.layout.item_project_book, null)
            tvSummary = convertView.findViewById(R.id.tv_summary) as TextView
            tvDate = convertView.findViewById(R.id.tv_date) as TextView
            tvTime = convertView.findViewById(R.id.tv_time) as TextView
            tvType = convertView.findViewById(R.id.tv_type) as TextView
            return convertView
        }

        override fun showData(convertView: View, position: Int, data: ProjectBookBean?) {
            tvSummary.text = data?.doc_title
            val strTime = data?.add_time
            tvTime.visibility = View.GONE
            if (!TextUtils.isEmpty(strTime)) {
                val strs = strTime!!.trim().split(" ")
                if (!TextUtils.isEmpty(strs.getOrNull(1))) {
                    tvTime.visibility = View.VISIBLE
                }
                tvDate.text = strs.getOrNull(0)
                tvTime.text = strs.getOrNull(1)
            }
            tvType.text = data?.name
        }

    }

    companion object {
        fun start(ctx: Activity?, project: ProjectBean) {
            val intent = Intent(ctx, ProjectBookActivity::class.java)
            intent.putExtra(Extras.DATA, project)
            ctx?.startActivity(intent)
        }
    }
}