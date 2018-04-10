package com.sougukj

import android.view.View
import android.widget.EditText
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.sogukj.pe.util.Ex_T0_Unit
import com.sogukj.pe.util.OnClickFastListener
import com.sogukj.pe.util.SubscriberHelper
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

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

fun <T> List<T>?.isNullOrEmpty(): Boolean = this == null || this.isEmpty()
/**
 * 判断EditText的text是否不为空
 */
fun EditText.isNullOrEmpty(): Boolean =
        text?.trim().isNullOrEmpty()

/**
 * edittext扩展属性，获取其文本
 */
val EditText.textStr: String
    get() = text.trim().toString()

/**
 * 扩展View是否可见，VISIBLE 与 GONE。
 */
fun View.setVisible(visible: Boolean) {
    visibility = if (visible) View.VISIBLE else View.GONE
}


fun <T> Observable<T>.execute(init: Ex_T0_Unit<SubscriberHelper<T>>) {
    val subscriberHelper = SubscriberHelper<T>()
    init(subscriberHelper)
    subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(subscriberHelper)
}


fun CharSequence?.checkEmpty():CharSequence{
    return if (this == null || this.isEmpty() || this == "null")
        ""
    else
        this
}