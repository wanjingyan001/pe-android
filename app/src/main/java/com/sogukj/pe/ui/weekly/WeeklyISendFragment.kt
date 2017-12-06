package com.sogukj.pe.ui.weekly


import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.framework.base.BaseFragment
import com.sogukj.pe.R
import com.sogukj.pe.bean.WeeklySendBean
import com.sogukj.pe.view.RecyclerAdapter
import com.sogukj.pe.view.RecyclerHolder
import kotlinx.android.synthetic.main.fragment_weekly_isend.*
import android.widget.TextView
import com.sogukj.pe.view.ListAdapter
import com.sogukj.pe.view.MyGridView
import kotlinx.android.synthetic.main.item_week_send.*


/**
 * A simple [Fragment] subclass.
 */
class WeeklyISendFragment : BaseFragment() {

    override val containerViewId: Int
        get() = R.layout.fragment_weekly_isend

    lateinit var adapter: RecyclerAdapter<WeeklySendBean>

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = RecyclerAdapter<WeeklySendBean>(context, { _adapter, parent, type ->
            val convertView = _adapter.getView(R.layout.item_week_send, parent) as LinearLayout
            object : RecyclerHolder<WeeklySendBean>(convertView) {
                val tv_title = convertView.findViewById(R.id.title_date) as TextView
                val grid = convertView.findViewById(R.id.grid_list) as MyGridView
                override fun setData(view: View, data: WeeklySendBean, position: Int) {
                    tv_title.text = data.date
                    data.list?.let {
                        var adapter = MyAdapter(context, it)
                        grid.adapter = adapter
                        adapter.notifyDataSetChanged()
                    }
                }
            }
        })
        adapter.onItemClick = { v, p ->
        }
        val layoutManager = LinearLayoutManager(context)
        layoutManager.orientation = LinearLayoutManager.VERTICAL
        list.layoutManager = layoutManager
        list.adapter = adapter

        var bean = WeeklySendBean()
        bean.date = "2017年10月"
        bean.list.add("10.06-10.12")
        bean.list.add("10.13-10.15")
        bean.list.add("10.16-10.18")
        bean.list.add("10.19-10.21")
        bean.list.add("10.22-10.23")
        bean.list.add("10.34-10.35")
        adapter.dataList.add(bean)

        var bean1 = WeeklySendBean()
        bean1.date = "2017年11月"
        bean1.list.add("11.06-11.12")
        bean1.list.add("11.13-11.20")
        adapter.dataList.add(bean1)

        var bean2 = WeeklySendBean()
        bean2.date = "2017年10月"
        bean2.list.add("10.06-10.12")
        bean2.list.add("10.13-10.15")
        bean2.list.add("10.16-10.18")
        bean2.list.add("10.19-10.21")
        bean2.list.add("10.22-10.23")
        bean2.list.add("10.34-10.35")
        adapter.dataList.add(bean2)

        adapter.notifyDataSetChanged()
    }

    class MyAdapter(var context: Context, val list: ArrayList<String>) : BaseAdapter() {


        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            var conView = convertView
            if (conView == null) {
                conView = LayoutInflater.from(context).inflate(R.layout.senditem, null)
            }
            (conView as TextView).text = list.get(position)
            return conView
        }

        override fun getItem(position: Int): Any {
            return list.get(position)
        }

        override fun getItemId(position: Int): Long {
            return position.toLong()
        }

        override fun getCount(): Int {
            return list.size
        }
    }
}
