package com.sogukj.pe.ui.score

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.bumptech.glide.Glide
import com.framework.base.ToolbarActivity
import com.google.gson.JsonSyntaxException
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.bean.ScoreBean
import com.sogukj.pe.util.MyGlideUrl
import com.sogukj.pe.util.Trace
import com.sogukj.pe.util.Utils
import com.sogukj.pe.view.CircleImageView
import com.sogukj.pe.view.RecyclerAdapter
import com.sogukj.pe.view.RecyclerHolder
import com.sogukj.pe.view.SpaceItemDecoration
import com.sogukj.service.SoguApi
import com.sogukj.util.XmlDb
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_score_list.*
import kotlinx.android.synthetic.main.toolbar.*
import org.jetbrains.anko.backgroundColor
import org.jetbrains.anko.textColor
import java.net.UnknownHostException

class ScoreListActivity : ToolbarActivity() {

    companion object {
        fun start(ctx: Activity?, data: ArrayList<ScoreBean>) {
            val intent = Intent(ctx, ScoreListActivity::class.java)
            intent.putExtra(Extras.DATA, data)
            ctx?.startActivity(intent)
        }
    }

    lateinit var adapter: RecyclerAdapter<ScoreBean>

    var quanxian = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_score_list)

        var qx = XmlDb.open(context).get(Extras.QUANXIAN, "")
        quanxian = qx.toInt()

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
                var dengji = convertView.findViewById(R.id.dengji) as TextView

                override fun setData(view: View, data: ScoreBean, position: Int) {
                    tvSeq.text = "${position + 4}"
                    if(data.url.isNullOrEmpty()){
                        Head.setChar(data.name?.first())
                    } else {
                        Glide.with(context).load(MyGlideUrl(data.url)).into(Head)
                    }
                    tvName.text = data.name
                    final_score.text = "最终得分：${data.total_grade}"
                    finishing_task.text = "岗位胜任力评价：${data.resumption}"
                    kpi.text = "关键绩效指标评价：${data.achieve_check}"
                    dengji.text = data.level
                }
            }
        })
        try {
            adapter.onItemClick = { v, p ->
                if (quanxian == 1) {//0=>不能查看，1=>可以
                    ScoreDetailActivity.start(context, Extras.TYPE_LISTITEM, adapter.dataList.get(p))
                }
            }
            head_1.setOnClickListener {
                if (quanxian == 1) {
                    ScoreDetailActivity.start(context, Extras.TYPE_LISTITEM, adapter.dataList.get(0))
                }
            }
            head_2.setOnClickListener {
                if (quanxian == 1) {
                    ScoreDetailActivity.start(context, Extras.TYPE_LISTITEM, adapter.dataList.get(1))
                }
            }
            head_3.setOnClickListener {
                if (quanxian == 1) {
                    ScoreDetailActivity.start(context, Extras.TYPE_LISTITEM, adapter.dataList.get(2))
                }
            }
        } catch (e: Exception) {
            //可能数据个数小于3个
            Log.e("Exception", e.localizedMessage)
        }
        val layoutManager = LinearLayoutManager(context)
        layoutManager.orientation = LinearLayoutManager.VERTICAL
        score_list.layoutManager = layoutManager
        score_list.addItemDecoration(SpaceItemDecoration(Utils.dpToPx(context, 30)))
        score_list.adapter = adapter

        var data = intent.getSerializableExtra(Extras.DATA) as ArrayList<ScoreBean>
        if (data == null || data.size == 0) {
            hide(0)
            hide(1)
            hide(2)
            frame.backgroundColor = Color.WHITE
            toolbar_title.textColor = Color.parseColor("#ff000000")
        } else {
            //  前三名单独设置---不足三人
            var disIndex = 0
            try {
                set(data.get(0), 0)
                disIndex++
                set(data.get(1), 1)
                disIndex++
                set(data.get(2), 2)

                adapter.dataList.addAll(data.subList(3, data.size))
                adapter.notifyDataSetChanged()
            } catch (e: Exception) {
                for (i in disIndex until 3) {
                    hide(disIndex)
                }
            }
        }
    }

    fun hide(index: Int) {
        var id_name = resources.getIdentifier("name_" + (index + 1), "id", context.packageName)
        var name = findViewById(id_name) as TextView
        name.visibility = View.GONE

        var id_dengji = resources.getIdentifier("dengji_" + (index + 1), "id", context.packageName)
        var dengji = findViewById(id_dengji) as TextView
        dengji.visibility = View.GONE

        var id_final = resources.getIdentifier("final_" + (index + 1), "id", context.packageName)
        var final = findViewById(id_final) as TextView
        final.visibility = View.GONE

        var finishing_task_id = resources.getIdentifier("finishing_task_" + (index + 1), "id", context.packageName)
        var finishing_task_ = findViewById(finishing_task_id) as TextView
        finishing_task_.visibility = View.GONE

        var kpi_id = resources.getIdentifier("kpi_" + (index + 1), "id", context.packageName)
        var kpi = findViewById(kpi_id) as TextView
        kpi.visibility = View.GONE
    }

    /**
     * index对应ScoreBean下标
     */
    fun set(bean: ScoreBean, index: Int) {
        var id = resources.getIdentifier("head_" + (index + 1), "id", context.packageName)
        var headIcon = findViewById(id) as CircleImageView
        if(bean.url.isNullOrEmpty()){
            headIcon.setChar(bean.name?.first())
        } else {
            Glide.with(context).load(MyGlideUrl(bean.url)).into(headIcon)
        }

        var id_dengji = resources.getIdentifier("dengji_" + (index + 1), "id", context.packageName)
        var dengji = findViewById(id_dengji) as TextView
        dengji.text = bean.level
        dengji.visibility = View.VISIBLE

        var id_name = resources.getIdentifier("name_" + (index + 1), "id", context.packageName)
        var name = findViewById(id_name) as TextView
        name.text = bean.name

        var id_final = resources.getIdentifier("final_" + (index + 1), "id", context.packageName)
        var final = findViewById(id_final) as TextView
        final.text = "最终得分：${bean.total_grade}"

        var finishing_task_id = resources.getIdentifier("finishing_task_" + (index + 1), "id", context.packageName)
        var finishing_task_ = findViewById(finishing_task_id) as TextView
        finishing_task_.text = "岗位胜任力评价：${bean.resumption}"

        var kpi_id = resources.getIdentifier("kpi_" + (index + 1), "id", context.packageName)
        var kpi = findViewById(kpi_id) as TextView
        kpi.text = "关键绩效指标评价：${bean.achieve_check}"
    }
}
