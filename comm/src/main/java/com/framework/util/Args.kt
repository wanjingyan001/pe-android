package com.framework.util

import android.text.TextUtils

/**
 * Created by qff on 2015/12/14.
 */
object Args {
    fun notNull(obj: Any?) {
        if (null == obj) throw NullPointerException()
    }

    fun notNull(obj: Any?, msg: String) {
        if (null == obj) throw NullPointerException(msg)
    }

    fun notEmpty(argument: String?,
                 name: String) {
        if (argument == null) {
            throw IllegalArgumentException(name + " may not be null")
        }
        if (TextUtils.isEmpty(argument)) {
            throw IllegalArgumentException(
                    name + " may not be empty")
        }
    }
}
