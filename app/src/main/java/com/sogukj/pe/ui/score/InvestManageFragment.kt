package com.sogukj.pe.ui.score


import android.graphics.Color
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.framework.base.BaseFragment

import com.sogukj.pe.R
import com.sogukj.pe.util.Utils
import com.sogukj.pe.view.RecyclerAdapter
import com.sogukj.pe.view.RecyclerHolder
import com.sogukj.pe.view.SpaceItemDecoration
import kotlinx.android.synthetic.main.fragment_invest_manage.*
import org.jetbrains.anko.textColor


/**
 * A simple [Fragment] subclass.
 */
class InvestManageFragment : BaseFragment() {

    override val containerViewId: Int
        get() = R.layout.fragment_invest_manage

    companion object {
        fun newInstance(): InvestManageFragment {
            val fragment = InvestManageFragment()
            val intent = Bundle()
            //intent.putInt(Extras.TYPE, type)
            fragment.arguments = intent
            return fragment
        }
    }

    lateinit var invest_adapter: RecyclerAdapter<RateFragment.RateItem>

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        invest_adapter = RecyclerAdapter<RateFragment.RateItem>(context, { _adapter, parent, t ->
            ProjectHolder(_adapter.getView(R.layout.item_rate_title, parent))
        })
        val layoutManager = LinearLayoutManager(context)
        layoutManager.orientation = LinearLayoutManager.VERTICAL
        invest_list_fragment.layoutManager = layoutManager
        //invest_list.addItemDecoration(SpaceItemDecoration(Utils.dpToPx(context, 30)))
        invest_list_fragment.adapter = invest_adapter

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
                ProjectHolderNoTitle(_adapter.getView(R.layout.fragment_item_invest, parent))
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
