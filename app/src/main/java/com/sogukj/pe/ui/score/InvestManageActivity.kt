package com.sogukj.pe.ui.score

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.framework.base.ToolbarActivity
import com.sogukj.pe.R
import com.sogukj.pe.util.Utils
import com.sogukj.pe.view.RecyclerAdapter
import com.sogukj.pe.view.RecyclerHolder
import com.sogukj.pe.view.SpaceItemDecoration
import kotlinx.android.synthetic.main.activity_invest_manage.*
import org.jetbrains.anko.textColor

class InvestManageActivity : ToolbarActivity() {

    companion object {
        fun start(ctx: Context?) {
            val intent = Intent(ctx, InvestManageActivity::class.java)
            ctx?.startActivity(intent)
        }
    }

    lateinit var invest_adapter: RecyclerAdapter<RateFragment.RateItem>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_invest_manage)

        setBack(true)
        setTitle("投资经理")
        toolbar?.setBackgroundColor(Color.WHITE)
        toolbar?.apply {
            val title = this.findViewById(R.id.toolbar_title) as TextView?
            title?.textColor = Color.parseColor("#282828")
            val back = this.findViewById(R.id.toolbar_back) as ImageView
            back?.visibility = View.VISIBLE
            back.setImageResource(R.drawable.grey_back)
        }

        invest_adapter = RecyclerAdapter<RateFragment.RateItem>(context, { _adapter, parent, t ->
            ProjectHolder(_adapter.getView(R.layout.item_rate_title, parent))
        })
        val layoutManager = LinearLayoutManager(context)
        layoutManager.orientation = LinearLayoutManager.VERTICAL
        invest_list.layoutManager = layoutManager
        //invest_list.addItemDecoration(SpaceItemDecoration(Utils.dpToPx(context, 30)))
        invest_list.adapter = invest_adapter

        invest_adapter.dataList.addAll(arrayListOf(RateFragment.RateItem(), RateFragment.RateItem()))
        invest_adapter.notifyDataSetChanged()
    }

    inner class ProjectHolder(view: View)
        : RecyclerHolder<RateFragment.RateItem>(view) {

        var head_title = convertView.findViewById(R.id.head_title) as TextView
        var list = convertView.findViewById(R.id.listview) as RecyclerView

        override fun setData(view: View, data: RateFragment.RateItem, position: Int) {
            //invest_item
            var inner_adapter = RecyclerAdapter<RateFragment.RateItem.RateBean>(context, { _adapter, parent, t ->
                ProjectHolderNoTitle(_adapter.getView(R.layout.invest_item, parent))
            })
            val layoutManager = LinearLayoutManager(context)
            layoutManager.orientation = LinearLayoutManager.VERTICAL
            list.layoutManager = layoutManager
            list.addItemDecoration(SpaceItemDecoration(Utils.dpToPx(context, 30)))
            list.adapter = inner_adapter

            inner_adapter.dataList.addAll(arrayListOf(RateFragment.RateItem.RateBean(), RateFragment.RateItem.RateBean()))
            inner_adapter.notifyDataSetChanged()
        }
    }

    inner class ProjectHolderNoTitle(view: View)
        : RecyclerHolder<RateFragment.RateItem.RateBean>(view) {

        override fun setData(view: View, data: RateFragment.RateItem.RateBean, position: Int) {

        }
    }
}
