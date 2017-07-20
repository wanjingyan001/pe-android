package com.framework.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ListView
import java.util.*

class ListAdapter<T>(val creator: ViewHolderCreator<T>) : BaseAdapter() {
    var dataList: List<T> = ArrayList()

    abstract class ViewHolderBase<T> {
        abstract fun createView(inflater: LayoutInflater): View

        abstract fun showData(convertView: View, position: Int, itemData: T?)
    }

    interface ViewHolderCreator<T> {
        fun createViewHolder(): ViewHolderBase<T>
    }

    private fun createViewHolder(): ViewHolderBase<T> {
        return creator.createViewHolder()
    }

    override fun getCount(): Int {
        return if (dataList == null) 0 else dataList.size
    }

    override fun getItem(position: Int): T? {
        return if (dataList == null || dataList.size <= position) null else dataList[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var convertView = convertView
        var holder: ViewHolderBase<T>? = null
        val itemData = getItem(position) as T?

        if (convertView == null) {
            val inflater = LayoutInflater.from(parent.context)

            holder = createViewHolder()
            convertView = holder.createView(inflater)
            convertView.tag = holder
        } else {
            holder = convertView.tag as ViewHolderBase<T>
        }

        if (holder != null) {
            holder.showData(convertView, position, itemData)
        }

        return convertView
    }

    //局部刷新
    fun updateSingleRow(listView: ListView?, id: Long) {
        if (listView != null) {
            val start = listView.firstVisiblePosition
            var i = start
            val j = listView.lastVisiblePosition
            while (i <= j) {
                if (id == listView.getItemIdAtPosition(i)) {
                    val view = listView.getChildAt(i - start)
                    getView(i, view, listView)
                    break
                }
                i++
            }
        }
    }
}
