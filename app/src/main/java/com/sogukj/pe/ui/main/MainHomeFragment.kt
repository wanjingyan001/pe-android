package com.sogukj.pe.ui.main

import android.os.Bundle
import android.view.View
import com.framework.base.BaseFragment
import com.sogukj.pe.R
import com.sogukj.pe.ui.approve.EntryApproveActivity
import com.sogukj.pe.ui.news.NewsListActivity
import com.sogukj.pe.ui.user.UserActivity
import kotlinx.android.synthetic.main.fragment_home.*

/**
 * Created by qinfei on 17/10/11.
 */
class MainHomeFragment : BaseFragment() {
    override val containerViewId: Int
        get() = R.layout.fragment_home

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        tv_zx.setOnClickListener {
            NewsListActivity.start(baseActivity)
        }
        tv_me.setOnClickListener {
            UserActivity.start(baseActivity)
        }
        tv_sp.setOnClickListener {
            EntryApproveActivity.start(baseActivity)
        }
    }

    companion object {
        val TAG = MainHomeFragment::class.java.simpleName

        fun newInstance(): MainHomeFragment {
            val fragment = MainHomeFragment()
            val intent = Bundle()
            fragment.arguments = intent
            return fragment
        }
    }
}