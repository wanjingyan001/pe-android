package com.sogukj.pe.ui.score


import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import com.framework.base.BaseFragment
import com.sogukj.pe.Extras

import com.sogukj.pe.R
import com.sogukj.pe.bean.JudgeBean
import com.sogukj.pe.bean.WeeklySendBean
import com.sogukj.pe.view.MyGridView
import com.sogukj.pe.view.RecyclerAdapter
import com.sogukj.pe.view.RecyclerHolder
import com.sogukj.pe.view.SpaceItemDecoration
import kotlinx.android.synthetic.main.fragment_judge.*


/**
 * a simple [Fragment] subclass.
 */
class JudgeFragment : BaseFragment() {

    lateinit var adapter: RecyclerAdapter<JudgeBean>

    override val containerViewId: Int
        get() = R.layout.fragment_judge

    companion object {
        /**
         * type决定哪个界面，type1决定是员工还是领导
         */
        fun newInstance(type: Int, type1: Int): JudgeFragment {
            val fragment = JudgeFragment()
            val intent = Bundle()
            intent.putInt(Extras.TYPE, type)
            intent.putInt(Extras.TYPE1, type1)
            fragment.arguments = intent
            return fragment
        }
    }

    val TYPE_WAIT = 1
    val TYPE_END = 2
    val TYPE_EMPLOYEE = 3
    val TYPE_MANAGE = 4

    var type: Int? = null
    var type1: Int? = null

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        type = arguments.getInt(Extras.TYPE)
        type1 = arguments.getInt(Extras.TYPE1)

        if (type == TYPE_WAIT && type1 == TYPE_EMPLOYEE) {
            tag_progress.visibility = View.GONE
            tag_time.visibility = View.GONE
        } else if (type == TYPE_END && type1 == TYPE_EMPLOYEE) {
            tag_progress.visibility = View.GONE
        } else if (type == TYPE_WAIT && type1 == TYPE_MANAGE) {
            tag_time.visibility = View.GONE
        }

        adapter = RecyclerAdapter<JudgeBean>(context, { _adapter, parent, type0 ->
            val convertView = _adapter.getView(R.layout.item_judge, parent) as LinearLayout
            object : RecyclerHolder<JudgeBean>(convertView) {

                val tvName = convertView.findViewById(R.id.name) as TextView
                val tvDepart = convertView.findViewById(R.id.depart) as TextView
                val tvProgress = convertView.findViewById(R.id.progress) as TextView
                val tvTime = convertView.findViewById(R.id.time) as TextView

                override fun setData(view: View, data: JudgeBean, position: Int) {
                    if (type == TYPE_WAIT && type1 == TYPE_EMPLOYEE) {
                        tvProgress.visibility = View.GONE
                        tvTime.visibility = View.GONE
                    } else if (type == TYPE_END && type1 == TYPE_EMPLOYEE) {
                        tvProgress.visibility = View.GONE
                    } else if (type == TYPE_WAIT && type1 == TYPE_MANAGE) {
                        tvTime.visibility = View.GONE
                    }
                    tvName.text = data.name
                    tvDepart.text = data.depart
                    tvProgress.text = data.progress
                    tvTime.text = data.time
                }
            }
        })
        adapter.onItemClick = { v, p ->
            if (type1 == TYPE_EMPLOYEE) {
                if (p == 0) {
                    callback.judgeFinish()
                } else {
                    EmployeeInteractActivity.start(context)
                }
            } else if (type1 == TYPE_MANAGE) {
                if (p == 0) {
                    callback.judgeFinish()
                } else if (p == 1) {
                    RateActivity.start(context)
                } else if (p == 2) {
                    //JudgeActivity.start(context, TYPE_MANAGE)
                }
            }
        }
        val layoutManager = LinearLayoutManager(context)
        layoutManager.orientation = LinearLayoutManager.VERTICAL
        list.layoutManager = layoutManager
        list.addItemDecoration(SpaceItemDecoration(10))
        list.adapter = adapter

        var bean = JudgeBean()
        bean.name = "张三"
        bean.depart = "投资部"
        bean.progress = "50%"
        bean.time = "9月11日 14:00"
        adapter.dataList.add(bean)
        adapter.dataList.add(bean)
        adapter.dataList.add(bean)
        adapter.notifyDataSetChanged()
    }

    interface judgeInterface {
        fun judgeFinish()
    }

    lateinit var callback: judgeInterface

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        callback = context as judgeInterface
    }
}
