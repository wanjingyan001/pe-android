package com.sogukj.util

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager

/**
 * @author qinfeifei XML数据存储工具类
 */
class XmlDb private constructor(ctx: Context) {
    private val pref: SharedPreferences

    init {
        pref = PreferenceManager.getDefaultSharedPreferences(ctx)
    }

    fun set(key: String, `val`: String): Boolean {
        return pref.edit().putString(key, `val`).commit()
    }

    fun set(key: String, `val`: Boolean): Boolean {
        return pref.edit().putBoolean(key, `val`).commit()
    }

    fun get(key: String): Boolean {
        return pref.getBoolean(key, false)
    }

    fun get(key: String, defValue: String): String {
        return pref.getString(key, defValue)
    }

    companion object {
        private var sPrefs: XmlDb? = null

        fun open(ctx: Context): XmlDb {
            if (null == sPrefs) {
                sPrefs = XmlDb(ctx)
            }
            return sPrefs!!
        }
    }

}