package com.sogukj.pe.view

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentPagerAdapter
import com.framework.base.BaseFragment

/**
 * Created by qinfei on 17/7/19.
 */
class ArrayPagerAdapter<T>(val fm: android.support.v4.app.FragmentManager, val fragments: Array<T>)
    : FragmentPagerAdapter(fm) where T: BaseFragment {

    override fun getItem(position: Int): Fragment {
        return fragments!![position]
    }

    override fun getCount(): Int {
        return fragments!!.size
    }
}