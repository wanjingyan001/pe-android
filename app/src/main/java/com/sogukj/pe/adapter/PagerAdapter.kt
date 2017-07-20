package com.sogukj.pe.adapter

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter

/**
 * Created by qinfei on 17/3/6.
 */

class PagerAdapter(val fm: FragmentManager, val fragments: Array<Fragment>)
    : FragmentPagerAdapter(fm) {

    override fun getItem(position: Int): Fragment {
        return fragments!![position]
    }

    override fun getCount(): Int {
        return fragments!!.size
    }
}
