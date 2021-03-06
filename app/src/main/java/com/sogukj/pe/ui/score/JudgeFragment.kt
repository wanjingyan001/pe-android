package com.sogukj.pe.ui.score


import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import com.framework.base.BaseFragment
import com.sogukj.pe.Extras

import com.sogukj.pe.R
import com.sogukj.pe.bean.GradeCheckBean
import com.sogukj.pe.view.RecyclerAdapter
import com.sogukj.pe.view.RecyclerHolder
import com.sogukj.pe.view.SpaceItemDecoration
import kotlinx.android.synthetic.main.fragment_judge.*
import org.jetbrains.anko.textColor


/**
 * a simple [Fragment] subclass.
 */
class JudgeFragment : BaseFragment() {

    lateinit var adapter: RecyclerAdapter<GradeCheckBean.ScoreItem>

    override val containerViewId: Int
        get() = R.layout.fragment_judge

    companion object {
        /**
         * type决定哪个界面，type1决定是员工还是领导，type2  岗位胜任力和关键绩效
         */
        fun newInstance(type: Int, type1: Int, type2: Int, data: ArrayList<GradeCheckBean.ScoreItem>): JudgeFragment {
            val fragment = JudgeFragment()
            val intent = Bundle()
            intent.putInt(Extras.TYPE, type)
            intent.putInt(Extras.TYPE1, type1)
            intent.putInt(Extras.TYPE2, type2)
            intent.putSerializable(Extras.DATA, data)
            fragment.arguments = intent
            return fragment
        }
    }

    val TYPE_WAIT = 1
    val TYPE_END = 2
    val TYPE_EMPLOYEE = 3
    val TYPE_MANAGE = 4

    val TYPE_JOB = 1
    val TYPE_RATE = 2

    var type: Int? = null
    var type1: Int? = null
    var type2 = 0

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        type = arguments.getInt(Extras.TYPE)
        type1 = arguments.getInt(Extras.TYPE1)
        type2 = arguments.getInt(Extras.TYPE2)

        if (type == TYPE_WAIT && type1 == TYPE_EMPLOYEE) {
            tag_progress.visibility = View.GONE
            tag_time.visibility = View.GONE
        } else if (type == TYPE_END && type1 == TYPE_EMPLOYEE) {
            tag_progress.visibility = View.GONE
        } else if (type == TYPE_WAIT && type1 == TYPE_MANAGE) {
            tag_time.visibility = View.GONE
        }

        adapter = RecyclerAdapter<GradeCheckBean.ScoreItem>(context, { _adapter, parent, type0 ->
            val convertView = _adapter.getView(R.layout.item_judge, parent) as LinearLayout
            object : RecyclerHolder<GradeCheckBean.ScoreItem>(convertView) {

                val tvName = convertView.findViewById(R.id.tag1) as TextView
                val tvDepart = convertView.findViewById(R.id.tag2) as TextView
                val tvProgress = convertView.findViewById(R.id.tag3) as TextView
                val tvTime = convertView.findViewById(R.id.tag4) as TextView

                override fun setData(view: View, data: GradeCheckBean.ScoreItem, position: Int) {
                    if (type1 == TYPE_MANAGE && type == TYPE_END) {
                        tvProgress.textColor = Color.parseColor("#FFA1CEA9")
                    } else if (type1 == TYPE_EMPLOYEE && type == TYPE_WAIT) {
                        tvProgress.textColor = Color.parseColor("#FFCEA1A1")
                    }
                    if (type == TYPE_WAIT && type1 == TYPE_EMPLOYEE) {
                        tvProgress.visibility = View.GONE
                        tvTime.visibility = View.GONE
                    } else if (type == TYPE_END && type1 == TYPE_EMPLOYEE) {
                        tvProgress.visibility = View.GONE
                    } else if (type == TYPE_WAIT && type1 == TYPE_MANAGE) {
                        tvTime.visibility = View.GONE
                    }
                    tvProgress.visibility = View.GONE
                    tvName.text = data.name
                    tvDepart.text = data.department
                    //tvProgress.text = data.plan
                    tvTime.text = data.grade_date
                }
            }
        })
        adapter.onItemClick = { v, p ->
            //type2  岗位胜任力和关键绩效
            if (type2 == 18) {

            }
            if (type1 == TYPE_EMPLOYEE) {
                if (type == TYPE_WAIT) {
                    GangWeiShengRenLiActivity.start(context, adapter.dataList.get(p), false)
                    //RateActivity.start(context, adapter.dataList.get(p), type2, TYPE_EMPLOYEE, false)
                } else if (type == TYPE_END) {
                    GangWeiShengRenLiActivity.start(context, adapter.dataList.get(p), true)
                }
            } else if (type1 == TYPE_MANAGE) {
                if (type == TYPE_WAIT) {
                    //RateActivity.start(context, adapter.dataList.get(p), type2, TYPE_MANAGE, false)
                } else if (type == TYPE_END) {
                    //RateActivity.start(context, adapter.dataList.get(p), type2, TYPE_MANAGE, true)
                }
            }
        }
        val layoutManager = LinearLayoutManager(context)
        layoutManager.orientation = LinearLayoutManager.VERTICAL
        list.layoutManager = layoutManager
        list.addItemDecoration(SpaceItemDecoration(10))
        list.adapter = adapter

        var data = arguments.getSerializable(Extras.DATA) as ArrayList<GradeCheckBean.ScoreItem>
        adapter.dataList.clear()
        data.forEach {
            adapter.dataList.add(it)
        }
        adapter.notifyDataSetChanged()

        if (adapter.dataList.size == 0) {
            callback.judgeFinish()
        }
    }

    fun load() {

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
