package com.sogukj.util

import android.content.Context
import android.text.TextUtils
import com.google.gson.Gson
import com.sogukj.pe.bean.UserBean
import java.util.*

/**
 * Created by qinff on 2016/5/20.
 * 小数据存取工具类
 */
class Store private constructor() {
    private var _user: UserBean? = null

    fun checkLogin(ctx: Context): Boolean {
        return null != getUser(ctx) && null != _user?.uid
    }

    fun getUser(ctx: Context): UserBean? {
        try {
            if (null == _user) {
                val jsonUser = XmlDb.open(ctx).get(UserBean::class.java.simpleName, "")
                if (!TextUtils.isEmpty(jsonUser)) {
                    this._user = GSON.fromJson(jsonUser, UserBean::class.java)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return _user
    }

    fun setUser(ctx: Context, user: UserBean) {
        this._user = user
        XmlDb.open(ctx).set(UserBean::class.java.simpleName, GSON.toJson(user))
    }


    class SizeList<E> : LinkedList<E>() {

        fun distinct() {

        }
    }

    companion object {
        val store = Store()
        internal val GSON = Gson()
    }
}
