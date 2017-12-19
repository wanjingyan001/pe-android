package com.sogukj.pe.view

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.EditText
import android.widget.TextView
import com.sogukj.pe.R

/**
 * Created by sogubaby on 2017/12/19.
 */
class FengKongAdapter(val context: Context) : BaseAdapter() {

    val datalist = ArrayList<Bean>()

    fun addAll(list: ArrayList<Bean>) {
        datalist.addAll(list)
        notifyDataSetChanged()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        var view = convertView
        var holder: Holder? = null
        if (view == null) {
            holder = Holder()
            view = LayoutInflater.from(context).inflate(R.layout.item_fengkong, null)
            holder?.title = view.findViewById(R.id.item_title) as TextView
            holder?.content = view.findViewById(R.id.item_content) as EditText
            view.setTag(holder)
        } else {
            holder = view.tag as Holder
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
}

class Bean {

}

class Holder {
    var title: TextView? = null
    var content: EditText? = null
}