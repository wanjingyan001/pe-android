package com.sogukj.pe.ui.score

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.bumptech.glide.Glide
import com.framework.base.ToolbarActivity
import com.google.gson.JsonSyntaxException
import com.sogukj.pe.R
import com.sogukj.pe.bean.ScoreBean
import com.sogukj.pe.util.Trace
import com.sogukj.pe.util.Utils
import com.sogukj.pe.view.CircleImageView
import com.sogukj.pe.view.RecyclerAdapter
import com.sogukj.pe.view.RecyclerHolder
import com.sogukj.pe.view.SpaceItemDecoration
import com.sogukj.service.SoguApi
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_score_list.*
import org.jetbrains.anko.textColor
import java.net.UnknownHostException

class ScoreListActivity : ToolbarActivity() {

    companion object {
        fun start(ctx: Activity?) {
            val intent = Intent(ctx, ScoreListActivity::class.java)
            ctx?.startActivity(intent)
        }
    }

    lateinit var adapter: RecyclerAdapter<ScoreBean>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_score_list)

        setBack(true)
        setTitle("全员考评分数总览")
        toolbar?.setBackgroundColor(Color.TRANSPARENT)
        toolbar?.apply {
            val title = this.findViewById(R.id.toolbar_title) as TextView?
            title?.textColor = Color.parseColor("#ffffff")
            val back = this.findViewById(R.id.toolbar_back) as ImageView
            back?.visibility = View.VISIBLE
            back.setImageResource(R.drawable.grey_back)
        }

        adapter = RecyclerAdapter<ScoreBean>(context, { _adapter, parent, type ->

            val convertView = _adapter.getView(R.layout.item_score, parent) as LinearLayout

            object : RecyclerHolder<ScoreBean>(convertView) {

                var tvSeq = convertView.findViewById(R.id.tv_seq) as TextView
                var Head = convertView.findViewById(R.id.head) as CircleImageView
                var tvName = convertView.findViewById(R.id.tv_name) as TextView
                var final_score = convertView.findViewById(R.id.final_score) as TextView
                var finishing_task = convertView.findViewById(R.id.finishing_task) as TextView
                var kpi = convertView.findViewById(R.id.kpi) as TextView

                override fun setData(view: View, data: ScoreBean, position: Int) {
                    tvSeq.text = "${position + 3}"
                    Glide.with(context).load(data.img_url).into(Head)
                    tvName.text = data.name
                    final_score.text = "最终得分：${data.last_grade}"
                    finishing_task.text = "岗位胜任力评价：${data.finishing_task}"
                    kpi.text = "关键绩效指标评价：${data.kpi}"
                }
            }
        })
        adapter.onItemClick = { v, p ->
            ScoreDetailActivity.start(context)
        }
        val layoutManager = LinearLayoutManager(context)
        layoutManager.orientation = LinearLayoutManager.VERTICAL
        score_list.layoutManager = layoutManager
        score_list.addItemDecoration(SpaceItemDecoration(Utils.dpToPx(context, 30)))
        score_list.adapter = adapter

        SoguApi.getService(application)
                .grade_pandect()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ payload ->
                    if (payload.isOk) {
                        payload.payload?.apply {
                            //  前三名单独设置---不足三人
                            try {
                                set(this.removeAt(0), 0)
                                set(this.removeAt(1), 1)
                                set(this.removeAt(2), 2)

                                adapter.dataList.addAll(this)
                                adapter.notifyDataSetChanged()
                            } catch (e: Exception) {
                            }
                        }
                    } else {
                        showToast(payload.message)
                    }
                }, { e ->
                    Trace.e(e)
                    when (e) {
                        is JsonSyntaxException -> showToast("后台数据出错")
                        is UnknownHostException -> showToast("网络出错")
                        else -> showToast("未知错误")
                    }
                })
    }

    /**
     * index对应ScoreBean下标
     */
    fun set(bean: ScoreBean, index: Int) {
        var id = resources.getIdentifier("head_" + (index + 1), "id", context.packageName)
        var headIcon = findViewById(id) as CircleImageView
        Glide.with(context).load(bean.img_url).into(headIcon)

        var id_name = resources.getIdentifier("name_" + (index + 1), "id", context.packageName)
        var name = findViewById(id_name) as TextView
        name.text = bean.name

        var id_final = resources.getIdentifier("final_" + (index + 1), "id", context.packageName)
        var final = findViewById(id_final) as TextView
        final.text = "最终得分：${bean.last_grade}"

        var finishing_task_id = resources.getIdentifier("finishing_task_" + (index + 1), "id", context.packageName)
        var finishing_task_ = findViewById(finishing_task_id) as TextView
        finishing_task_.text = "岗位胜任力评价：${bean.finishing_task}"

        var kpi_id = resources.getIdentifier("kpi_" + (index + 1), "id", context.packageName)
        var kpi = findViewById(kpi_id) as TextView
        kpi.text = "关键绩效指标评价：${bean.kpi}"
    }
}
