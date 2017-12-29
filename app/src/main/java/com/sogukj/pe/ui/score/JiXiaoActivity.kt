package com.sogukj.pe.ui.score

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.support.v7.widget.DialogTitle
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.framework.base.ToolbarActivity
import com.google.gson.JsonSyntaxException
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.bean.EmployeeInteractBean
import com.sogukj.pe.bean.EmployeeInteractBean.EmployeeItem
import com.sogukj.pe.bean.JudgeBean
import com.sogukj.pe.util.Trace
import com.sogukj.pe.view.RecyclerAdapter
import com.sogukj.pe.view.RecyclerHolder
import com.sogukj.pe.view.SpaceItemDecoration
import com.sogukj.service.SoguApi
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_ji_xiao.*
import kotlinx.android.synthetic.main.activity_user_edit.*
import kotlinx.android.synthetic.main.item_empty.*
import org.jetbrains.anko.textColor
import java.net.UnknownHostException

class JiXiaoActivity : ToolbarActivity() {

    companion object {
        // JIXIAO    RED_BLACK
        fun start(ctx: Context?, type: Int, data: EmployeeInteractBean? = null) {
            val intent = Intent(ctx, JiXiaoActivity::class.java)
            intent.putExtra(Extras.TYPE, type)
            intent.putExtra(Extras.DATA, data)
            ctx?.startActivity(intent)
        }
    }

    var type = 0
    lateinit var data: EmployeeInteractBean

    lateinit var adapter: RecyclerAdapter<EmployeeItem>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ji_xiao)

        type = intent.getIntExtra(Extras.TYPE, 0)
        var tmp = intent.getSerializableExtra(Extras.DATA)
        if (tmp != null) {
            data = tmp as EmployeeInteractBean
        }

        setBack(true)
        if (type == Extras.JIXIAO) {
            setTitle("关键绩效考核结果")
        } else if (type == Extras.RED_BLACK) {
            setTitle(data.title)
        }
        toolbar?.setBackgroundColor(Color.WHITE)
        toolbar?.apply {
            val title = this.findViewById(R.id.toolbar_title) as TextView?
            title?.textColor = Color.parseColor("#282828")
            val back = this.findViewById(R.id.toolbar_back) as ImageView
            back?.visibility = View.VISIBLE
            back.setImageResource(R.drawable.grey_back)
        }


        adapter = RecyclerAdapter<EmployeeItem>(context, { _adapter, parent, type0 ->
            val convertView = _adapter.getView(R.layout.item_child, parent) as LinearLayout
            object : RecyclerHolder<EmployeeItem>(convertView) {

                var seq = convertView.findViewById(R.id.seq) as TextView
                var depart = convertView.findViewById(R.id.depart) as TextView
                var name = convertView.findViewById(R.id.name) as TextView
                var score = convertView.findViewById(R.id.score) as TextView

                override fun setData(view: View, data: EmployeeItem, position: Int) {
                    seq.text = "${data.sort}"
                    depart.text = data.department
                    name.text = data.name
                    score.text = data.grade_case
                }
            }
        })
        val layoutManager = LinearLayoutManager(context)
        layoutManager.orientation = LinearLayoutManager.VERTICAL
        jixiao_list.layoutManager = layoutManager
        jixiao_list.addItemDecoration(SpaceItemDecoration(10))
        jixiao_list.adapter = adapter

        adapter.dataList.addAll(data.data!!)
        adapter.notifyDataSetChanged()
    }
}
