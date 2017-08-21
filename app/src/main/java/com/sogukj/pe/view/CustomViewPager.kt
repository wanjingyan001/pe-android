package com.sogukj.pe.view

import android.content.Context
import android.support.v4.view.ViewPager
import android.util.AttributeSet
import android.view.MotionEvent

/**
 * Created by qinfei on 17/2/24.
 */

class CustomViewPager : ViewPager {

    internal var preX = -1
    internal var preY = -1

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {

    }

    constructor(context: Context) : super(context) {

    }

    override fun onInterceptTouchEvent(event: MotionEvent): Boolean {

        if (event.action == MotionEvent.ACTION_DOWN) {
            preX = event.x.toInt()
            preY = event.y.toInt()
        } else {
            val dif = (Math.abs(event.x.toInt() - preX) - Math.abs(event.y - preY)).toInt()
            if (dif > 5) {
                return true
            } else {
                preX = event.x.toInt()
                preY = event.y.toInt()
            }
        }
        return super.onInterceptTouchEvent(event)
    }
}