package com.sogukj.pe.ui.score

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.support.design.widget.TabLayout
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.framework.base.ToolbarActivity
import com.google.gson.JsonSyntaxException
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.bean.GradeCheckBean
import com.sogukj.pe.util.Trace
import com.sogukj.pe.util.Utils
import com.sogukj.pe.view.RecyclerAdapter
import com.sogukj.pe.view.RecyclerHolder
import com.sogukj.pe.view.SpaceItemDecoration
import com.sogukj.service.SoguApi
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_point_progress.*
import org.jetbrains.anko.textColor
import java.net.UnknownHostException

class PointProgressActivity : ToolbarActivity() {

    companion object {
        fun start(ctx: Context?) {
            val intent = Intent(ctx, PointProgressActivity::class.java)
            ctx?.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_point_progress)

        setBack(true)
        setTitle("全员打分进度")
        toolbar?.setBackgroundColor(Color.WHITE)
        toolbar?.apply {
            val title = this.findViewById(R.id.toolbar_title) as TextView?
            title?.textColor = Color.parseColor("#282828")
            val back = this.findViewById(R.id.toolbar_back) as ImageView
            back?.visibility = View.VISIBLE
            back.setImageResource(R.drawable.grey_back)
        }

        list_tabs?.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabReselected(tab: TabLayout.Tab?) {

            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {

            }

            override fun onTabSelected(tab: TabLayout.Tab) {
                if (tab.position == 0) {
                    currentIndex = 0
                    loadData()
                } else if (tab.position == 1) {
                    currentIndex = 1
                    loadData()
                }
            }
        })

        adapter = RecyclerAdapter<GradeCheckBean.ScoreItem>(context, { _adapter, parent, type0 ->
            val convertView = _adapter.getView(R.layout.item_judge, parent) as LinearLayout
            object : RecyclerHolder<GradeCheckBean.ScoreItem>(convertView) {

                val tvTag1 = convertView.findViewById(R.id.tag1) as TextView
                val tvTag2 = convertView.findViewById(R.id.tag2) as TextView
                val tvTag3 = convertView.findViewById(R.id.tag3) as TextView
                val tvTag4 = convertView.findViewById(R.id.tag4) as TextView

                override fun setData(view: View, data: GradeCheckBean.ScoreItem, position: Int) {
                    tvTag1.visibility = View.VISIBLE
                    tvTag2.visibility = View.VISIBLE
                    tvTag3.visibility = View.VISIBLE
                    tvTag4.visibility = View.VISIBLE

                    tvTag1.text = data.name
                    tvTag2.text = data.department
                    tvTag3.text = data.position
                    tvTag4.text = data.grade_date
                }
            }
        })
        adapter.onItemClick = { v, p ->
            //            //领导员工进同一个界面
//            if (currentIndex == 0) {
//                GangWeiShengRenLiActivity.start(context, adapter.dataList.get(p), false)
//            } else if (currentIndex == 1) {
//                GangWeiShengRenLiActivity.start(context, adapter.dataList.get(p), true)
//            }
        }

        val layoutManager = LinearLayoutManager(context)
        layoutManager.orientation = LinearLayoutManager.VERTICAL
        list.layoutManager = layoutManager
        list.addItemDecoration(SpaceItemDecoration(Utils.dpToPx(context, 10)))
        list.adapter = adapter
    }

    lateinit var adapter: RecyclerAdapter<GradeCheckBean.ScoreItem>
    var currentIndex = 0
    var unfinish = ArrayList<GradeCheckBean.ScoreItem>()
    var finish = ArrayList<GradeCheckBean.ScoreItem>()

    fun loadData() {
        if (currentIndex == 0) {
            adapter.dataList.clear()
            adapter.dataList.addAll(unfinish)
            adapter.notifyDataSetChanged()
        } else if (currentIndex == 1) {
            adapter.dataList.clear()
            adapter.dataList.addAll(finish)
            adapter.notifyDataSetChanged()
        }
    }

    override fun onResume() {
        super.onResume()
        SoguApi.getService(application)
                .check(2)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ payload ->
                    if (payload.isOk) {
                        payload.payload?.apply {
                            unfinish = ready_grade!!
                            finish = finish_grade!!
                            loadData()
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
