package com.sogukj.pe.ui.score


import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.framework.base.BaseFragment

import com.sogukj.pe.R


/**
 * A simple [Fragment] subclass.
 */
class InvestManageFragment : BaseFragment() {

    override val containerViewId: Int
        get() = R.layout.fragment_invest_manage

    companion object {
        fun newInstance(): InvestManageFragment {
            val fragment = InvestManageFragment()
            val intent = Bundle()
            //intent.putInt(Extras.TYPE, type)
            fragment.arguments = intent
            return fragment
        }
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

    }
}
