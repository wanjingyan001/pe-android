package com.sogukj.pe.ui.score

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.EditText
import android.widget.TextView
import com.sogukj.pe.R
import com.sogukj.pe.bean.GradeCheckBean
import com.sogukj.pe.bean.JinDiaoItem
import com.sogukj.pe.bean.TouHouManageItem

/**
 * Created by sogubaby on 2017/12/21.
 * 0--touhou   1   jindiao
 */
class FengKongAdapter(val context: Context, val type: Int, val jin_tmp: ArrayList<JinDiaoItem>, val touhou_tmp: ArrayList<TouHouManageItem>) : BaseAdapter() {

//    var jin_tmp = ArrayList<JinDiaoItem>()
//    var touhou_tmp = ArrayList<TouHouManageItem>()

    val datalist = ArrayList<GradeCheckBean.FengKongItem.FengKongInnerItem>()

    fun addAll(list: ArrayList<GradeCheckBean.FengKongItem.FengKongInnerItem>) {
        datalist.addAll(list)
        notifyDataSetChanged()
    }

    fun add(item: GradeCheckBean.FengKongItem.FengKongInnerItem) {
        datalist.add(item)
        notifyDataSetChanged()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        var view = convertView
        var holder: Holder? = null
        if (view == null) {
            holder = Holder()
            if (type == 0) {// tou  hou
                view = LayoutInflater.from(context).inflate(R.layout.item_fengkong, null)
                holder?.title = view.findViewById(R.id.item_title) as TextView
                holder?.content = view.findViewById(R.id.item_content) as EditText
            } else if (type == 1) {// jin diao
                view = LayoutInflater.from(context).inflate(R.layout.fengkong_item, null)
                holder?.zhibiao = view.findViewById(R.id.zhibiao) as EditText
                holder?.condition = view.findViewById(R.id.condi) as EditText
            }
            view?.setTag(holder)
        } else {
            holder = view.tag as Holder
        }
        if (type == 0) {
            holder?.title?.text = datalist[position].zhibiao
            var item = TouHouManageItem()
            item.performance_id = datalist[position].performance_id
            item.info = ""
            touhou_tmp.add(item)
            holder?.content?.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(s: Editable?) {
                    touhou_tmp[position].info = s.toString()
                }

                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                }
            })
        } else if (type == 1) {
            var item = JinDiaoItem()
            item.info = ""
            item.title = ""
            jin_tmp.add(item)
            holder?.zhibiao?.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(s: Editable?) {
                    jin_tmp[position].title = s.toString()
                }

                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                }
            })
            holder?.condition?.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(s: Editable?) {
                    jin_tmp[position].info = s.toString()
                }

                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                }
            })
        }
        return view!!
    }

    override fun getItem(position: Int): Any {
        return datalist.get(position)
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getCount(): Int {
        return datalist.size
    }

    class Holder {
        var title: TextView? = null
        var content: EditText? = null

        var zhibiao: EditText? = null
        var condition: EditText? = null
    }
}