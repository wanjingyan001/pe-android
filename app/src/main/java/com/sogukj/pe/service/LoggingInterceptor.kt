package com.sougukj.service

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import com.sogukj.pe.service.Payload
import com.sogukj.pe.util.Utils
import com.sogukj.util.Store
import com.sougukj.fromJson
import okhttp3.FormBody
import okhttp3.Interceptor
import okhttp3.Response
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.error
import org.jetbrains.anko.info

/**
 * 请求Log拦截器
 * Created by admin on 2018/3/28.
 */
class LoggingInterceptor(private val context: Context) : Interceptor, AnkoLogger {
    override val loggerTag: String
        get() = "HttpRequest"

    override fun intercept(chain: Interceptor.Chain): Response {
        val builder = chain.request().newBuilder()
        val user = Store.store.getUser(context)
        if (user?.uid != null) {
            builder.addHeader("uid", user.uid.toString())
        }
        builder.addHeader("appkey", "d5f17cafef0829b5")
        builder.addHeader("version", Utils.getVersionName(context))
        builder.addHeader("client", "android")
        val l1 = System.nanoTime()//请求发起的时间
        val request = builder.build()
        when (request.method()) {
            "POST" -> {
                val build = StringBuilder()
                val body = request.body()
                if (body is FormBody) {
                    for (i in 0 until body.size()) {
                        build.append(body.encodedName(i) + "=" + body.encodedValue(i) + ",")
                    }
                    if (build.isNotEmpty()) {
                        build.delete(build.length - 1, build.length)
                    }
                    info {
                        "发送${request.method()}请求:${request.url()}\n" +
                                " RequestParams:{$build}\n ${request.headers()}\n" +
                                "on ${chain.connection()}"
                    }
                }
            }
            else -> {
                info {
                    "发送${request.method()}请求:${request.url()}\n ${request.headers()}\n" +
                            "on ${chain.connection()}"
                }
            }
        }
        val response = chain.proceed(request)
        val l2 = System.nanoTime()
        try {
            val body = response.peekBody(1024 * 1024)
            info {
                "接收响应:[${request.url()}],code:${response.code()}\n" +
                        "返回JSON:${body.string()},\n" +
                        "耗时:${(l2 - l1) / 1e6}ms"
            }
        } catch (e: Exception) {
            error { e.message }
        }
        return response
    }
}