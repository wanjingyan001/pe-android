package com.sogukj.pe.ui

import android.view.View
import com.framework.base.BaseActivity
import com.framework.base.BaseFragment
import com.lcodecore.tkrefreshlayout.TwinklingRefreshLayout
import com.sogukj.pe.R
import com.sogukj.pe.adapter.RecyclerAdapter
import org.jetbrains.anko.find

/**
 * Created by qinfei on 17/8/31.
 */
interface SupportEmptyView {

    companion object {
        fun checkEmpty(fragment: BaseFragment, adapter: RecyclerAdapter<*>) {
            val view = fragment.view
            val refreshLayout: TwinklingRefreshLayout? = view?.find<TwinklingRefreshLayout>(R.id.refresh)
            val emptyView: View? = view?.find<View>(R.id.iv_empty)
            emptyView?.visibility = if (adapter.dataList.isEmpty()) View.VISIBLE else View.GONE
            emptyView?.setOnClickListener { v ->
                refreshLayout?.startRefresh()
            }
        }

        fun checkEmpty(activity: BaseActivity, adapter: RecyclerAdapter<*>) {
            val refreshLayout: TwinklingRefreshLayout? = activity?.find<TwinklingRefreshLayout>(R.id.refresh)
            val emptyView: View? = activity?.find<View>(R.id.iv_empty)
            emptyView?.visibility = if (adapter.dataList.isEmpty()) View.VISIBLE else View.GONE
            emptyView?.setOnClickListener { v ->
                refreshLayout?.startRefresh()
            }
        }
    }
}