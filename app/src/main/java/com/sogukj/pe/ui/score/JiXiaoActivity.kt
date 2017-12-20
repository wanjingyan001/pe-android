package com.sogukj.pe.ui.score

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.framework.base.ToolbarActivity
import com.sogukj.pe.R
import com.sogukj.pe.bean.JudgeBean
import com.sogukj.pe.view.RecyclerAdapter
import com.sogukj.pe.view.RecyclerHolder
import com.sogukj.pe.view.SpaceItemDecoration
import kotlinx.android.synthetic.main.activity_ji_xiao.*
import org.jetbrains.anko.textColor

class JiXiaoActivity : ToolbarActivity() {

    companion object {
        fun start(ctx: Context?) {
            val intent = Intent(ctx, JiXiaoActivity::class.java)
            ctx?.startActivity(intent)
        }
    }

    lateinit var adapter: RecyclerAdapter<JudgeBean>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ji_xiao)

        setBack(true)
        setTitle("关键绩效考核结果")
        toolbar?.setBackgroundColor(Color.WHITE)
        toolbar?.apply {
            val title = this.findViewById(R.id.toolbar_title) as TextView?
            title?.textColor = Color.parseColor("#282828")
            val back = this.findViewById(R.id.toolbar_back) as ImageView
            back?.visibility = View.VISIBLE
            back.setImageResource(R.drawable.grey_back)
        }


        adapter = RecyclerAdapter<JudgeBean>(context, { _adapter, parent, type0 ->
            val convertView = _adapter.getView(R.layout.item_judge, parent) as LinearLayout
            object : RecyclerHolder<JudgeBean>(convertView) {

                val tvSeq = convertView.findViewById(R.id.tag1) as TextView
                val tvDepart = convertView.findViewById(R.id.tag2) as TextView
                val tvName = convertView.findViewById(R.id.tag3) as TextView
                val tvScore = convertView.findViewById(R.id.tag4) as TextView

                override fun setData(view: View, data: JudgeBean, position: Int) {
                }
            }
        })
        val layoutManager = LinearLayoutManager(context)
        layoutManager.orientation = LinearLayoutManager.VERTICAL
        jixiao_list.layoutManager = layoutManager
        jixiao_list.addItemDecoration(SpaceItemDecoration(10))
        jixiao_list.adapter = adapter

        var bean = JudgeBean()
        bean.name = "1"
        bean.depart = "投资部"
        bean.progress = "张三"
        bean.time = "60.80"
        adapter.dataList.add(bean)
        adapter.dataList.add(bean)
        adapter.dataList.add(bean)
        adapter.notifyDataSetChanged()
    }
}
