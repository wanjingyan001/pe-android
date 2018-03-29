package com.sogukj.pe.util

import android.view.View

/**
 * Created by admin on 2018/3/29.
 */
class ExtendUtils {
    fun View.setOnFastListener(listener: OnClickFastListener) {
        this.setOnClickListener(listener)
    }
}