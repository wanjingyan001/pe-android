package com.sogukj.pe.ui.score

import android.app.AlertDialog
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
import android.content.DialogInterface
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.util.Log
import com.afollestad.materialdialogs.GravityEnum
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.Theme


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

        val layoutManager = LinearLayoutManager(context)
        layoutManager.orientation = LinearLayoutManager.VERTICAL
        list.layoutManager = layoutManager
        list.addItemDecoration(SpaceItemDecoration(Utils.dpToPx(context, 10)))
        list.adapter = adapter

        toolbar_menu.setOnClickListener {
            var content1 = SpannableString("本页中的三个状态含义如下：\n" +
                    "工作结果：指员工自己是否填写了绩效考核中的工作结果。\n" +
                    "岗位互评：指员工自己是否为全公司人员评分完毕。\n" +
                    "直线上级评分：此处上级包含直线上级与班子两部分，只有两部分都完成打分才会显示完成打分。")
            content1.setSpan(ForegroundColorSpan(Color.parseColor("#ff282828")), 14, 19, Spanned.SPAN_INCLUSIVE_EXCLUSIVE)
            content1.setSpan(ForegroundColorSpan(Color.parseColor("#ff282828")), 41, 46, Spanned.SPAN_INCLUSIVE_EXCLUSIVE)
            content1.setSpan(ForegroundColorSpan(Color.parseColor("#ff282828")), 65, 72, Spanned.SPAN_INCLUSIVE_EXCLUSIVE)
            MaterialDialog.Builder(this)
                    .theme(Theme.LIGHT)
                    .title("提示")
                    .content(content1)
                    .contentColor(Color.parseColor("#ffa0a4aa"))
                    .neutralText("知道了")
                    .neutralColor(Color.parseColor("#ff282828"))
                    .buttonsGravity(GravityEnum.CENTER)
                    .onNeutral { dialog, which ->

                    }
                    .show()
        }
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
