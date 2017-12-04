package com.sogukj.pe.ui.main

import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Bundle
import android.view.View
import android.widget.TextView
import com.framework.base.BaseFragment
import com.sogukj.pe.R
import com.sogukj.pe.ui.approve.EntryApproveActivity
import com.sogukj.pe.ui.fund.FundMainActivity
import com.sogukj.pe.ui.news.NewsListActivity
import com.sogukj.pe.ui.user.UserActivity
import com.sogukj.pe.ui.weekly.WeeklyActivity
import kotlinx.android.synthetic.main.fragment_home.*
import org.jetbrains.anko.sdk25.coroutines.onClick

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
        tv_jj.onClick { FundMainActivity.start(baseActivity) }
        tv_weekly.onClick { WeeklyActivity.start(baseActivity) }
//        disable(tv_jj)
        disable(tv_rl)
        disable(tv_lxr)
    }

    val colorGray = Color.parseColor("#D9D9D9")
    fun disable(view: TextView) {
        view.compoundDrawables[1]?.setColorFilter(colorGray, PorterDuff.Mode.SRC_ATOP)
        view.setOnClickListener(null)
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