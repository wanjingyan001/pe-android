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
import com.framework.base.ToolbarActivity
import com.sogukj.pe.R
import com.sogukj.pe.bean.WeeklySendBean
import com.sogukj.pe.util.Utils
import com.sogukj.pe.view.RecyclerAdapter
import com.sogukj.pe.view.RecyclerHolder
import com.sogukj.pe.view.SpaceItemDecoration
import kotlinx.android.synthetic.main.activity_score_list.*
import org.jetbrains.anko.textColor

class ScoreListActivity : ToolbarActivity() {

    companion object {
        fun start(ctx: Activity?) {
            val intent = Intent(ctx, ScoreListActivity::class.java)
            ctx?.startActivity(intent)
        }
    }

    lateinit var adapter: RecyclerAdapter<WeeklySendBean>

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

        adapter = RecyclerAdapter<WeeklySendBean>(context, { _adapter, parent, type ->
            val convertView = _adapter.getView(R.layout.item_score, parent) as LinearLayout
            object : RecyclerHolder<WeeklySendBean>(convertView) {
                override fun setData(view: View, data: WeeklySendBean, position: Int) {
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

        adapter.dataList.add(WeeklySendBean())
        adapter.dataList.add(WeeklySendBean())
        adapter.dataList.add(WeeklySendBean())
        adapter.dataList.add(WeeklySendBean())
        adapter.dataList.add(WeeklySendBean())
        adapter.dataList.add(WeeklySendBean())
        adapter.notifyDataSetChanged()
    }
}
