package com.framework.adapter


import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.framework.util.Trace
import java.util.*

/**
 * Created by qinfei on 16/10/19.
 */
class RecyclerAdapter<T>(val context: Context, val creator: Creator<T>)
    : RecyclerView.Adapter<RecyclerAdapter.SimpleViewHolder<T>>() {

    var dataList = mutableListOf<T>()
    val inflater: LayoutInflater
    var comparator: Comparator<T>? = null
    var onItemClickListener: OnItemClickListener? = null
    var selectedItems = ArrayList<Int>()
        private set
    var mode: Int = 0
    var selectedPosition = -1

    init {
        this.inflater = LayoutInflater.from(context)
        this.selectedItems = ArrayList<Int>()
        this.mode = MODE_SINGLE
    }


    fun getView(layout: Int, parent: ViewGroup): View {
        return inflater.inflate(layout, parent, false)
    }

    override fun onCreateViewHolder(parent: ViewGroup, type: Int): SimpleViewHolder<T> {
        return creator.create(this, parent, type)
    }


    override fun onBindViewHolder(holder: SimpleViewHolder<T>, position: Int) {
        val data = dataList[position]
        holder.setData(holder.convertView, data, position)
    }

    override fun getItemCount(): Int {
        return dataList.size
    }

    override fun getItemViewType(position: Int): Int {
        return super.getItemViewType(position)
    }

    override fun getItemId(position: Int): Long {
        return super.getItemId(position)
    }


    override fun onBindViewHolder(holder: SimpleViewHolder<T>, position: Int, payloads: List<Any>?) {
        holder.itemView.setOnClickListener { v ->
            selectedPosition = position
            if (null != onItemClickListener)
                onItemClickListener!!.onItemClick(v, position)
        }
        super.onBindViewHolder(holder, position, payloads)
    }

    override fun setHasStableIds(hasStableIds: Boolean) {
        super.setHasStableIds(hasStableIds)
    }

    override fun onViewRecycled(holder: SimpleViewHolder<T>?) {
        super.onViewRecycled(holder)
    }

    override fun onFailedToRecycleView(holder: SimpleViewHolder<T>?): Boolean {
        return super.onFailedToRecycleView(holder)
    }


    override fun onViewAttachedToWindow(holder: SimpleViewHolder<T>?) {
        super.onViewAttachedToWindow(holder)
    }

    override fun onViewDetachedFromWindow(holder: SimpleViewHolder<T>?) {
        super.onViewDetachedFromWindow(holder)
    }

    override fun onAttachedToRecyclerView(view: RecyclerView?) {
        super.onAttachedToRecyclerView(view)
        view!!.isNestedScrollingEnabled = false
    }

    override fun onDetachedFromRecyclerView(view: RecyclerView?) {
        super.onDetachedFromRecyclerView(view)
    }

    override fun registerAdapterDataObserver(observer: RecyclerView.AdapterDataObserver) {
        super.registerAdapterDataObserver(observer)
    }

    override fun unregisterAdapterDataObserver(observer: RecyclerView.AdapterDataObserver) {
        super.unregisterAdapterDataObserver(observer)
    }


    fun getItem(position: Int): T {
        return dataList[position]
    }


    fun isSelected(position: Int): Boolean {
        return selectedItems!!.contains(Integer.valueOf(position))
    }


    @JvmOverloads fun toggleSelection(position: Int, invalidate: Boolean = false) {
        if (position < 0) return
        if (mode == MODE_SINGLE) clearSelection()

        val index = selectedItems!!.indexOf(position)
        if (index != -1) {
            Trace.d(TAG, "toggleSelection removing selection on position " + position)
            selectedItems!!.removeAt(index)
        } else {
            Trace.d(TAG, "toggleSelection adding selection on position " + position)
            selectedItems!!.add(position)
        }
        if (invalidate) {
            Trace.d(TAG, "toggleSelection notifyItemChanged on position " + position)
            notifyItemChanged(position)
        }
        Trace.d(TAG, "toggleSelection current selection " + selectedItems!!)
    }


    @JvmOverloads fun selectAll(skipViewType: Int = -1) {
        Trace.d(TAG, "selectAll")
        selectedItems = ArrayList<Int>(itemCount)
        for (i in 0..itemCount - 1) {
            if (getItemViewType(i) == skipViewType) continue
            selectedItems!!.add(i)
            Trace.d(TAG, "selectAll notifyItemChanged on position " + i)
            notifyItemChanged(i)
        }
    }


    fun clearSelection() {
        val iterator = selectedItems!!.iterator()
        while (iterator.hasNext()) {
            val i = iterator.next()
            iterator.remove()
            Trace.d(TAG, "clearSelection notifyItemChanged on position " + i)
            notifyItemChanged(i)
        }
    }


    val selectedItemCount: Int
        get() = selectedItems!!.size

    abstract class SimpleViewHolder<T>(view: View) : RecyclerView.ViewHolder(view) {

        var convertView: View
            internal set

        init {
            this.convertView = view
        }

        abstract fun setData(view: View, data: T, position: Int)
    }

    /**
     * Created by Fei on 2016/11/06.
     */
    interface OnItemClickListener {
        fun onItemClick(v: View, position: Int)
    }

    interface Creator<T> {
        fun create(adapter: RecyclerAdapter<T>, parent: ViewGroup, type: Int): SimpleViewHolder<T>
    }
    companion object {
        val TAG = RecyclerAdapter::class.java.simpleName


        val MODE_SINGLE = 1
        val MODE_MULTI = 2
    }
}
