package com.sogukj.pe.ui.weekly


import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.framework.base.BaseFragment

import com.sogukj.pe.R
import kotlinx.android.synthetic.main.buchong_full.*


/**
 * A simple [Fragment] subclass.
 */
class RecordBuChongFragment : BaseFragment() {

    override val containerViewId: Int
        get() = R.layout.fragment_record_bu_chong

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        buchong_edit.visibility = View.GONE

    }
}
