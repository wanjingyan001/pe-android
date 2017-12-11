package com.sogukj.pe.ui.calendar


import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import com.framework.base.BaseFragment
import com.sogukj.pe.R
import kotlinx.android.synthetic.main.fragment_todo.*


/**
 * A simple [Fragment] subclass.
 * Use the [TodoFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class TodoFragment : BaseFragment() {
    override val containerViewId: Int
        get() = R.layout.fragment_todo

    private var mParam1: String? = null
    private var mParam2: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (arguments != null) {
            mParam1 = arguments.getString(ARG_PARAM1)
            mParam2 = arguments.getString(ARG_PARAM2)
        }
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val data = ArrayList<Any>()
        data.add(TodoYear("2017"))
        data.add(TodoDay("14"))
        data.add(TodoInfo("09:30", "完成对同花顺的立项尽调完成对同..."))
        data.add(TodoInfo("12:00", "参加股东会议"))
        val adapter = TodoAdapter(data, context)
        todoList.layoutManager = LinearLayoutManager(context)
        todoList.adapter = adapter

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
         * @return A new instance of fragment TodoFragment.
         */
        fun newInstance(param1: String, param2: String): TodoFragment {
            val fragment = TodoFragment()
            val args = Bundle()
            args.putString(ARG_PARAM1, param1)
            args.putString(ARG_PARAM2, param2)
            fragment.arguments = args
            return fragment
        }
    }
}
