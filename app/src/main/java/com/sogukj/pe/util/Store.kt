package com.sogukj.util

import android.content.Context
import android.text.TextUtils
import com.google.gson.Gson
import com.sogukj.pe.bean.UserBean
import java.util.*
import kotlin.collections.HashSet

/**
 * Created by qinff on 2016/5/20.
 * 小数据存取工具类
 */
class Store private constructor() {
    private var _user: UserBean? = null

    fun checkLogin(ctx: Context): Boolean {
        return null != getUser(ctx) && null != _user?.uid
    }

    var readList = HashSet<String>();
    fun getRead(ctx: Context): HashSet<String> {
        try {
            val strJson = XmlDb.open(ctx).get("isRead", "")
            if (!TextUtils.isEmpty(strJson)) {
                this.readList.clear()
                val tmp = GSON.fromJson<Array<String>>(strJson, Array<String>::class.java)
                this.readList.addAll(Arrays.asList<String>(*tmp))
            }
        } catch (e: Exception) {
        }
        return readList
    }

    fun setRead(ctx: Context, readList: HashSet<String>): HashSet<String> {
        this.readList.clear()
        this.readList.addAll(readList)
        XmlDb.open(ctx).set("isRead", GSON.toJson(this.readList.toArray()))
        return readList;
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

    fun clearUser(ctx: Context) {
        this._user = null
        XmlDb.open(ctx).set(UserBean::class.java.simpleName, "{}")
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
