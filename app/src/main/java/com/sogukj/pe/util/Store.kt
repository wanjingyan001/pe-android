package com.sogukj.util

import android.content.Context
import android.text.TextUtils
import com.google.gson.Gson
import com.sogukj.pe.bean.UserInfo
import java.util.*

/**
 * Created by qinff on 2016/5/20.
 * 小数据存取工具类
 */
class Store private constructor() {
    private var user: UserInfo? = null

    fun checkLogin(ctx: Context): Boolean {
        return null != getUserInfo(ctx) && !TextUtils.isEmpty(user?.uid)
    }

    fun getUserInfo(ctx: Context): UserInfo? {
        try {
            if (null == user) {
                val jsonUser = XmlDb.open(ctx).get(UserInfo::class.java.simpleName,"")
                if (!TextUtils.isEmpty(jsonUser)) {
                    this.user = GSON.fromJson(jsonUser, UserInfo::class.java)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return user
    }

    fun setUserInfo(ctx: Context, user: UserInfo) {
        this.user = user
        XmlDb.open(ctx).set(UserInfo::class.java.simpleName, GSON.toJson(user))
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
