package com.sogukj.pe.ui.score


import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import android.widget.LinearLayout
import android.widget.ProgressBar
import com.framework.base.BaseFragment
import com.sogukj.pe.Extras

import com.sogukj.pe.R
import com.sogukj.pe.bean.WeeklySendBean
import com.sogukj.pe.view.RecyclerAdapter
import com.sogukj.pe.view.RecyclerHolder
import com.sogukj.pe.view.SpaceItemDecoration
import kotlinx.android.synthetic.main.fragment_rate.*
import android.graphics.drawable.LayerDrawable
import android.graphics.drawable.Drawable
import android.databinding.adapters.ViewBindingAdapter.setPadding
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.RoundRectShape
import com.sogukj.pe.util.Utils


/**
 * a simple [Fragment] subclass.
 */
class RateFragment : BaseFragment() {

    lateinit var adapter: RecyclerAdapter<WeeklySendBean>

    companion object {
        const val TYPE_JOB = 1
        const val TYPE_RATE = 2

        fun newInstance(type: Int): RateFragment {
            val fragment = RateFragment()
            val intent = Bundle()
            intent.putInt(Extras.TYPE, type)
            fragment.arguments = intent
            return fragment
        }
    }

    override val containerViewId: Int
        get() = R.layout.fragment_rate

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = RecyclerAdapter<WeeklySendBean>(context, { _adapter, parent, type ->
            val convertView = _adapter.getView(R.layout.item_rate, parent) as LinearLayout
            object : RecyclerHolder<WeeklySendBean>(convertView) {
                override fun setData(view: View, data: WeeklySendBean, position: Int) {
                    var bar = view.findViewById(R.id.progress) as ProgressBar
                    //pb_a,b,c,d
                }
            }
        })
        adapter.onItemClick = { v, p ->
        }
        val layoutManager = LinearLayoutManager(context)
        layoutManager.orientation = LinearLayoutManager.VERTICAL
        rate_list.layoutManager = layoutManager
        rate_list.addItemDecoration(SpaceItemDecoration(25))
        rate_list.adapter = adapter


        adapter.dataList.add(WeeklySendBean())
        adapter.dataList.add(WeeklySendBean())
        adapter.dataList.add(WeeklySendBean())
        adapter.notifyDataSetChanged()
    }
}
