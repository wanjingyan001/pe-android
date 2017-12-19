package com.sogukj.pe.ui.score

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
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

    lateinit var touzi_adapter: RecyclerAdapter<RateFragment.RateItem.RateBean>
    lateinit var manage_adapter: RecyclerAdapter<RateFragment.RateItem.RateBean>
    lateinit var rongzi_adapter: RecyclerAdapter<RateFragment.RateItem.RateBean>

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

        touzi_adapter = RecyclerAdapter<RateFragment.RateItem.RateBean>(context, { _adapter, parent, t ->
            ProjectHolder(_adapter.getView(R.layout.invest_item, parent))
        })
        val layoutManager = LinearLayoutManager(context)
        layoutManager.orientation = LinearLayoutManager.VERTICAL
        touzi_list.layoutManager = layoutManager
        touzi_list.addItemDecoration(SpaceItemDecoration(Utils.dpToPx(context, 30)))
        touzi_list.adapter = touzi_adapter

        touzi_adapter.dataList.addAll(arrayListOf(RateFragment.RateItem.RateBean(), RateFragment.RateItem.RateBean(), RateFragment.RateItem.RateBean()))
        touzi_adapter.notifyDataSetChanged()




        manage_adapter = RecyclerAdapter<RateFragment.RateItem.RateBean>(context, { _adapter, parent, t ->
            ProjectHolder(_adapter.getView(R.layout.invest_item, parent))
        })
        val layoutManager1 = LinearLayoutManager(context)
        layoutManager1.orientation = LinearLayoutManager.VERTICAL
        manage_list.layoutManager = layoutManager1
        manage_list.addItemDecoration(SpaceItemDecoration(Utils.dpToPx(context, 30)))
        manage_list.adapter = manage_adapter

        manage_adapter.dataList.addAll(arrayListOf(RateFragment.RateItem.RateBean(), RateFragment.RateItem.RateBean(), RateFragment.RateItem.RateBean()))
        manage_adapter.notifyDataSetChanged()






        rongzi_adapter = RecyclerAdapter<RateFragment.RateItem.RateBean>(context, { _adapter, parent, t ->
            ProjectHolder(_adapter.getView(R.layout.invest_item, parent))
        })
        val layoutManager2 = LinearLayoutManager(context)
        layoutManager2.orientation = LinearLayoutManager.VERTICAL
        rongzi_list.layoutManager = layoutManager2
        rongzi_list.addItemDecoration(SpaceItemDecoration(Utils.dpToPx(context, 30)))
        rongzi_list.adapter = rongzi_adapter

        rongzi_adapter.dataList.addAll(arrayListOf(RateFragment.RateItem.RateBean(), RateFragment.RateItem.RateBean(), RateFragment.RateItem.RateBean()))
        rongzi_adapter.notifyDataSetChanged()
    }

    inner class ProjectHolder(view: View)
        : RecyclerHolder<RateFragment.RateItem.RateBean>(view) {

        //var bar = convertView.findViewById(R.id.progressBar) as ProgressBar

        override fun setData(view: View, data: RateFragment.RateItem.RateBean, position: Int) {

        }
    }
}
