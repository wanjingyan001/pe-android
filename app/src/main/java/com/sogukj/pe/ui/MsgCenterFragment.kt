package com.sogukj.pe.ui

import android.os.Bundle
import android.view.View
import com.framework.base.BaseFragment
import com.sogukj.pe.R

/**
 * Created by qinfei on 17/10/11.
 */
class MsgCenterFragment : BaseFragment() {
    override val containerViewId: Int
        get() = R.layout.fragment_home

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

    }
    companion object {
        val TAG = MsgCenterFragment::class.java.simpleName

        fun newInstance(): MsgCenterFragment {
            val fragment = MsgCenterFragment()
            val intent = Bundle()
            fragment.arguments = intent
            return fragment
        }
    }
}