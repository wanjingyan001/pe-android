package com.sougukj

import android.view.View
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.sogukj.pe.util.OnClickFastListener

/**
 * kotlin扩展方法
 * Created by admin on 2018/3/30.
 */
inline fun <reified T> Gson.fromJson(json: String): T {
    return fromJson(json, T::class.java)
}

inline fun <reified T> Gson.arrayFromJson(json: String): Collection<T> {
    return Gson().fromJson(json, object : TypeToken<Collection<T>>() {}.type)
}

fun View.setOnClickFastListener(listener: OnClickFastListener.(v: View) -> Unit) {
    this.setOnClickListener {
        listener.invoke(object : OnClickFastListener() {
            override fun onFastClick(v: View) {

            }
        }, it)
    }
}