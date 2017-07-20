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
    private var userInfo: UserInfo? = null

    fun checkLogin(ctx: Context): Boolean {
        return null != getUserInfo(ctx) && !TextUtils.isEmpty(userInfo!!.token)
    }

    fun getUserInfo(ctx: Context): UserInfo {
        try {
            if (null == userInfo) {
                val jsonUser = XmlDb.open(ctx)[UserInfo::class.java.simpleName]
                if (!TextUtils.isEmpty(jsonUser)) {
                    this.userInfo = GSON.fromJson(jsonUser, UserInfo::class.java)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        if (null == userInfo) userInfo = UserInfo()
        return userInfo!!
    }

    fun setUserInfo(ctx: Context, user: UserInfo) {
        this.userInfo = user
        XmlDb.open(ctx).save(UserInfo::class.java.simpleName, GSON.toJson(user))
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
