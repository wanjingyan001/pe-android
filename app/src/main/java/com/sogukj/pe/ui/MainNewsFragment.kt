package com.sogukj.pe.ui

import android.os.Bundle
import android.support.design.widget.TabLayout
import android.support.v4.view.ViewPager
import android.view.View
import com.framework.base.BaseFragment
import com.sogukj.pe.R
import com.sogukj.pe.adapter.ArrayPagerAdapter
import kotlinx.android.synthetic.main.fragment_main_news.*
import kotlinx.android.synthetic.main.sogu_toolbar_main_news.*

/**
 * Created by qinfei on 17/7/18.
 */
class MainNewsFragment : BaseFragment() {
    override val containerViewId: Int
        get() = R.layout.fragment_main_news //To change initializer of created properties use File | Settings | File Templates.

    val fragments = arrayOf(
            NewsListFragment.newInstance(0),
            NewsListFragment.newInstance(1),
            NewsListFragment.newInstance(2)
    )

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        iv_user.setOnClickListener {
            LoginActivity.start(baseActivity);
        }
        var adapter = ArrayPagerAdapter(childFragmentManager, fragments)
        view_pager.adapter = adapter
        tabs?.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabReselected(tab: TabLayout.Tab?) {

            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {

            }

            override fun onTabSelected(tab: TabLayout.Tab) {
                view_pager?.currentItem = tab.position
            }

        })
        view_pager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) {
            }

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
            }

            override fun onPageSelected(position: Int) {
                tabs?.getTabAt(position)?.select()
            }

        })
    }

    companion object {
        val TAG = MainNewsFragment::class.java.simpleName

        fun newInstance(): MainNewsFragment {
            val fragment = MainNewsFragment()
            val intent = Bundle()
            fragment.arguments = intent
            return fragment
        }
    }


}