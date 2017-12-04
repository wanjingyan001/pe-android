package com.sogukj.pe.ui.project

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import com.framework.base.ToolbarActivity
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.bean.ProjectBean
import com.sogukj.pe.bean.RecordInfoBean
import com.sogukj.pe.util.DateUtils
import com.sogukj.pe.util.Trace
import com.sogukj.pe.view.DotView
import com.sogukj.pe.view.RecyclerAdapter
import com.sogukj.pe.view.RecyclerHolder
import com.sogukj.service.SoguApi
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_record_trace.*
import org.jetbrains.anko.textColor
import java.net.UnknownHostException

class RecordTraceActivity : ToolbarActivity() {

    lateinit var project: ProjectBean
    val gson = Gson()
    lateinit var adapter: RecyclerAdapter<RecordInfoBean.ListBean>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_record_trace)

        project = intent.getSerializableExtra(Extras.DATA) as ProjectBean
        setBack(true)
        setTitle("跟踪记录")
        company_name.text = project.name

        //
        project.company_id = 1

        adapter = RecyclerAdapter<RecordInfoBean.ListBean>(this, { _adapter, parent, type ->
            val convertView = _adapter.getView(R.layout.item_recordtrace, parent) as LinearLayout
            object : RecyclerHolder<RecordInfoBean.ListBean>(convertView) {
                val dot = convertView.findViewById(R.id.dot) as DotView
                val tvImportant = convertView.findViewById(R.id.important) as TextView
                val tvEvent = convertView.findViewById(R.id.event) as TextView
                val tvTime = convertView.findViewById(R.id.time) as TextView
                override fun setData(view: View, data: RecordInfoBean.ListBean, position: Int) {
                    if (position == 0) {
                        dot.setUp(false)
                        dot.setLow(true)
                    } else if (position == adapter.dataList.size - 1) {
                        dot.setUp(true)
                        dot.setLow(false)
                    } else {
                        dot.setUp(true)
                        dot.setLow(true)
                    }
                    dot.setImportant(if (data.important == 0) false else true)

                    if (data.important == 0) {
                        tvImportant.visibility = View.GONE
                    } else {
                        tvImportant.visibility = View.VISIBLE
                    }
                    tvEvent.text = data.des
                    tvTime.text = DateUtils.timet("${data.start_time}")

                    if (data.id in isViewed) {
                        convertView.setBackgroundColor(Color.parseColor("#f9f9f9"))
                        tvEvent.textColor = Color.parseColor("#656565")
                    } else {
                        convertView.setBackgroundColor(Color.parseColor("#ffffff"))
                        tvEvent.textColor = Color.parseColor("#282828")
                    }
                }
            }
        })
        adapter.onItemClick = { v, p ->
            var item = adapter.dataList.get(p)
            isViewed.add(item.id ?: -1)
            RecordDetailActivity.startView(this@RecordTraceActivity, project, item)
        }
        val layoutManager = LinearLayoutManager(this)
        layoutManager.orientation = LinearLayoutManager.VERTICAL
        //list.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))
        list.layoutManager = layoutManager
        list.adapter = adapter

        project.company_id?.let {
            load(it)
        }
    }

    fun load(it: Int) {
        SoguApi.getService(application)
                .recodeInfo(it)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ payload ->
                    if (payload.isOk) {
                        var data = payload.payload
                        data?.apply {
                            tv_investCost.text = info?.investCost
                            tv_investDate.text = info?.investDate
                            tv_equityRatio.text = info?.equityRatio
                            tv_riskControls.text = info?.riskControls
                            tv_invests.text = info?.invests

                            if (list != null && list?.size != 0) {
                                adapter.dataList.clear()
                                adapter.dataList.addAll(list)
                                adapter.notifyDataSetChanged()
                            } else {
                                showToast("无信息列表")
                            }
                        }
                    } else
                        showToast(payload.message)
                }, { e ->
                    Trace.e(e)
                    when (e) {
                        is JsonSyntaxException -> showToast("后台数据出错")
                        is UnknownHostException -> showToast("网络出错")
                        else -> showToast("未知错误")
                    }
                })
    }

    override val menuId: Int
        get() = R.menu.menu_mark

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val flag = super.onCreateOptionsMenu(menu)
        val menuMark = menu.findItem(R.id.action_mark) as MenuItem
        menuMark?.title = "添加"
        return flag
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.action_mark -> {
                RecordDetailActivity.startAdd(this@RecordTraceActivity, project)
            }
        }
        return false
    }

    companion object {
        fun start(ctx: Activity?, project: ProjectBean) {
            val intent = Intent(ctx, RecordTraceActivity::class.java)
            intent.putExtra(Extras.DATA, project)
            ctx?.startActivity(intent)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 0x001 && resultCode == Activity.RESULT_OK) {
            project.company_id?.let {
                load(it)
            }
        } else if (requestCode == 0x002 && resultCode == Activity.RESULT_CANCELED) {
            adapter.notifyDataSetChanged()
        }
    }

    var isViewed = ArrayList<Long>()
}
