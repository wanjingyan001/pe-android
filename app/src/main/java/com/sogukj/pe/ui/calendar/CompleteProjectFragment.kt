package com.sogukj.pe.ui.calendar


import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.framework.base.BaseFragment

import com.sogukj.pe.R
import kotlinx.android.synthetic.main.fragment_complete_project.*


/**
 * A simple [Fragment] subclass.
 * Use the [CompleteProjectFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class CompleteProjectFragment : BaseFragment() {
    override val containerViewId: Int
        get() = R.layout.fragment_complete_project

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
        data.add(TodoYear("2017年度"))
        data.add(CompleteInfo("储备该项目", "2015年11月13日 14:02"))
        data.add(CompleteInfo("项目立项", "2015年11月6日 14:02"))
        data.add(CompleteInfo("对赌协议签订（这里是内容这里是内容这里是内容这里是内容） ", "2015年11月1日 14:02"))
        val adapter = CompleteAdapter(context, data)
        completeList.layoutManager = LinearLayoutManager(context)
        completeList.adapter = adapter
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
         * @return A new instance of fragment CompleteProjectFragment.
         */
        fun newInstance(param1: String, param2: String): CompleteProjectFragment {
            val fragment = CompleteProjectFragment()
            val args = Bundle()
            args.putString(ARG_PARAM1, param1)
            args.putString(ARG_PARAM2, param2)
            fragment.arguments = args
            return fragment
        }
    }

}