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

    fun save(key: String, `val`: String): Boolean {
        return pref.edit().putString(key, `val`).commit()
    }

    fun save(key: String, `val`: Boolean): Boolean {
        return pref.edit().putBoolean(key, `val`).commit()
    }

    fun getBoolean(key: String): Boolean {
        return pref.getBoolean(key, false)
    }

    operator fun get(key: String): String {
        return pref.getString(key, "")
    }

    operator fun get(key: String, defValue: String): String {
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