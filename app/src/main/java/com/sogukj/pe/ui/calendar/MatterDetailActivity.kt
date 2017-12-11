package com.sogukj.pe.ui.calendar

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import android.widget.TextView
import com.framework.base.ToolbarActivity
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.util.Utils
import com.sogukj.pe.view.RecyclerAdapter
import com.sogukj.pe.view.RecyclerHolder
import com.sogukj.service.SoguApi
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_matter_dateil.*
import org.jetbrains.anko.find
import java.text.SimpleDateFormat

class MatterDetailActivity : ToolbarActivity() {
    lateinit var adapter: RecyclerAdapter<KeyNode>

    companion object {
        fun start(ctx: Context?) {
            ctx?.startActivity(Intent(ctx, MatterDetailActivity::class.java))
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_matter_dateil)
        val company = intent.getSerializableExtra(Extras.DATA) as ProjectMatterCompany
        title = company.companyName
        setBack(true)
        adapter = RecyclerAdapter(this, { _adapter, parent, position ->
            val convertView = _adapter.getView(R.layout.item_matter_detail_list, parent)
            object : RecyclerHolder<KeyNode>(convertView) {
                val year = convertView.find<TextView>(R.id.year)
                val dayOfMonth = convertView.find<TextView>(R.id.dayOfMonth)
                val hour = convertView.find<TextView>(R.id.hour)
                val matter_content = convertView.find<TextView>(R.id.matter_content)
                override fun setData(view: View, data: KeyNode, position: Int) {
                    var date = SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(data.end_time)
                    year.text = Utils.getTime(date, "yyyy年")
                    dayOfMonth.text = Utils.getTime(date, "MM月dd日")
                    hour.text = Utils.getTime(date, "HH:mm")
                    matter_content.text = data.title
                }
            }
        })
        nodeList.layoutManager = LinearLayoutManager(this)
        nodeList.adapter = adapter

        val fragments = ArrayList<Fragment>()
        fragments.add(TodoFragment.newInstance("", ""))
        fragments.add(CompleteProjectFragment.newInstance("", ""))
        mattersList.adapter = MatterPagerAdapter(supportFragmentManager, fragments)
        matterTab.setupWithViewPager(mattersList)

        doRequest(company.companyId)
    }

    fun doRequest(id: String) {
        SoguApi.getService(application)
                .projectMatter(id.toInt(), 1)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ payload ->
                    if (payload.isOk) {
                        payload.payload?.let {
                            adapter.dataList.addAll(it)
                            adapter.notifyDataSetChanged()
                        }
                    } else {
                        showToast(payload.message)
                    }
                })
    }
}
