package com.sogukj.pe.view

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.EditText
import android.widget.TextView
import com.sogukj.pe.R
import com.sogukj.pe.bean.GradeCheckBean.FengKongItem.FengKongInnerItem

/**
 * Created by sogubaby on 2017/12/19.
 */
class FengKongAdapter(val context: Context, val type: Int) : BaseAdapter() {

    val datalist = ArrayList<FengKongInnerItem>()

    fun addAll(list: ArrayList<FengKongInnerItem>) {
        datalist.addAll(list)
        notifyDataSetChanged()
    }

    fun add(item: FengKongInnerItem) {
        datalist.add(item)
        notifyDataSetChanged()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        var view = convertView
        var holder: Holder? = null
        if (view == null) {
            holder = Holder()
            if (type == 0) {
                view = LayoutInflater.from(context).inflate(R.layout.item_fengkong, null)
                holder?.title = view.findViewById(R.id.item_title) as TextView
                holder?.content = view.findViewById(R.id.item_content) as EditText
            } else if (type == 1) {
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
            holder?.content?.setText(datalist[position].info)
        } else if (type == 1) {
            holder?.zhibiao?.setText(datalist[position].zhibiao)
            holder?.condition?.setText(datalist[position].info)
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
