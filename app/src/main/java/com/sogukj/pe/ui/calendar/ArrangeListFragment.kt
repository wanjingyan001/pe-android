package com.sogukj.pe.ui.calendar


import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.sogukj.pe.R
import com.sogukj.pe.view.RecyclerAdapter
import com.sogukj.pe.view.RecyclerHolder
import kotlinx.android.synthetic.main.fragment_arrange_list.*


/**
 * A simple [Fragment] subclass.
 * Use the [ArrangeListFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ArrangeListFragment : Fragment() {
    private var mParam1: String? = null
    private var mParam2: String? = null
    private lateinit var arrangeAdapter: RecyclerAdapter<Any>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (arguments != null) {
            mParam1 = arguments.getString(ARG_PARAM1)
            mParam2 = arguments.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_arrange_list, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        arrangeAdapter = RecyclerAdapter(context, { adapter, parent, type ->
            val convertView = adapter.getView(R.layout.item_arrange_weekly, parent)
            object : RecyclerHolder<Any>(convertView) {
                override fun setData(view: View, data: Any, position: Int) {

                }
            }
        })
        recycler_view.layoutManager = LinearLayoutManager(context)
        recycler_view.adapter = arrangeAdapter
        val inflate = layoutInflater.inflate(R.layout.layout_arrange_weekly_header, recycler_view, false)
        recycler_view.addHeaderView(inflate)
        for (i in 0..7){
            arrangeAdapter.dataList.add("")
        }
    }

    companion object {
        private val ARG_PARAM1 = "param1"
        private val ARG_PARAM2 = "param2"

        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment ArrangeListFragment.
         */
        fun newInstance(param1: String, param2: String): ArrangeListFragment {
            val fragment = ArrangeListFragment()
            val args = Bundle()
            args.putString(ARG_PARAM1, param1)
            args.putString(ARG_PARAM2, param2)
            fragment.arguments = args
            return fragment
        }
    }
}
