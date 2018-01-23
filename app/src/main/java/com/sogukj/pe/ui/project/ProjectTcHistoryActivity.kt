package com.sogukj.pe.ui.project

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import com.framework.base.ToolbarActivity
import com.google.gson.JsonSyntaxException
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.bean.ProjectBean
import com.sogukj.pe.bean.QuitBean
import com.sogukj.pe.util.Trace
import com.sogukj.pe.util.Utils
import com.sogukj.pe.view.DotView
import com.sogukj.pe.view.RecyclerAdapter
import com.sogukj.pe.view.RecyclerHolder
import com.sogukj.pe.view.SpaceItemDecoration
import com.sogukj.service.SoguApi
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_project_tc_history.*
import java.net.UnknownHostException

class ProjectTcHistoryActivity : ToolbarActivity() {

    companion object {
        fun start(ctx: Activity?, id: Int) {
            val intent = Intent(ctx, ProjectTcHistoryActivity::class.java)
            intent.putExtra(Extras.ID, id)
            ctx?.startActivity(intent)
        }
    }

    var id = 0
    lateinit var adapter: RecyclerAdapter<QuitBean>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_project_tc_history)
        setBack(true)
        title = "历史退出记录"

        id = intent.getIntExtra(Extras.ID, 0)

        adapter = RecyclerAdapter<QuitBean>(this, { _adapter, parent, type ->
            val convertView = _adapter.getView(R.layout.item_quit, parent) as LinearLayout
            object : RecyclerHolder<QuitBean>(convertView) {
                val title = convertView.findViewById(R.id.title) as TextView
                val name = convertView.findViewById(R.id.name) as TextView
                val outTime = convertView.findViewById(R.id.outTime) as TextView
                override fun setData(view: View, data: QuitBean, position: Int) {
                    title.text = data.title
                    name.text = data.name
                    outTime.text = data.outTime
                }
            }
        })
        adapter.onItemClick = { v, p ->
            var item = adapter.dataList.get(p)
            var bean = ProjectBean()
            bean.company_id = item.id
            ProjectTCActivity.start(context, true, bean)
        }
        val layoutManager = LinearLayoutManager(this)
        layoutManager.orientation = LinearLayoutManager.VERTICAL
        list.addItemDecoration(SpaceItemDecoration(Utils.dpToPx(context, 10)))
        list.layoutManager = layoutManager
        list.adapter = adapter

        load()
    }

    fun load() {
        SoguApi.getService(application)
                .quitHistory(id)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ payload ->
                    if (payload.isOk) {
                        var data = payload.payload
                        data?.apply {
                            adapter.dataList.clear()
                            adapter.dataList.addAll(this)
                            adapter.notifyDataSetChanged()
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
}
