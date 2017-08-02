package com.sogukj.pe.ui

import android.os.Bundle
import android.support.design.widget.TabLayout
import android.support.v4.view.ViewPager
import android.view.View
import com.bumptech.glide.Glide
import com.framework.base.BaseFragment
import com.sogukj.pe.R
import com.sogukj.pe.adapter.ArrayPagerAdapter
import com.sogukj.util.Store
import kotlinx.android.synthetic.main.fragment_main_project.*
import kotlinx.android.synthetic.main.sogu_toolbar_main_proj.*
import org.jetbrains.anko.sdk25.coroutines.onClick

/**
 * Created by qinfei on 17/7/18.
 */
class MainProjectFragment : BaseFragment() {
    override val containerViewId: Int
        get() = R.layout.fragment_main_project //To change initializer of created properties use File | Settings | File Templates.

    val fragments = arrayOf(
            ProjectListFragment.newInstance(0),
            ProjectListFragment.newInstance(1),
            ProjectListFragment.newInstance(2)
    )

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        iv_user.onClick {
            UserActivity.start(baseActivity);
        }

        Store.store.getUser(baseActivity!!)?.apply {
            if (null != url)
                Glide.with(baseActivity)
                        .load(headImage())
                        .placeholder(R.drawable.img_user_default)
                        .error(R.drawable.img_user_default)
                        .into(iv_user)
        }
        iv_add.onClick {
            AddProjectActivity.start(baseActivity)
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
                iv_add?.visibility = if (position == 2) View.VISIBLE else View.GONE
            }

        })
    }

    companion object {
        val TAG = MainProjectFragment::class.java.simpleName

        fun newInstance(): MainProjectFragment {
            val fragment = MainProjectFragment()
            val intent = Bundle()
            fragment.arguments = intent
            return fragment
        }
    }
}