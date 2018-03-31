package com.sougukj.service

import android.content.Context
import com.sogukj.pe.util.Utils
import com.sogukj.util.Store
import okhttp3.FormBody
import okhttp3.Interceptor
import okhttp3.Response
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.info

/**
 * 请求Log拦截器
* Created by admin on 2018/3/28.
*/
class LoggingInterceptor(private val context: Context) : Interceptor, AnkoLogger {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val builder = request.newBuilder()
        val user = Store.store.getUser(context)
        if (user?.uid != null) {
            builder.addHeader("uid", user.uid.toString())
        }
        builder.addHeader("appkey", "d5f17cafef0829b5")
        builder.addHeader("version", Utils.getVersionName(context))
        builder.addHeader("client", "android")
        val l1 = System.nanoTime()//请求发起的时间
        when (request.method()) {
            "POST" -> {
                val build = StringBuilder()
                val body = request.body()
                if (body is FormBody) {
                    for (i in 0 until body.size()) {
                        build.append(body.encodedName(i) + "=" + body.encodedValue(i) + ",")
                    }
                    build.delete(build.length - 1, build.length)
                    info {
                        "发送${request.method()}请求${request.url()}\n on ${chain.connection()}\n" +
                                "${request.headers()}\n RequestParams:{$build}"
                    }
                }
            }
            else -> {
                info {
                    "发送${request.method()}请求${request.url()}\n on ${chain.connection()}\n" +
                            "${request.headers()}"
                }
            }
        }
        val response = chain.proceed(request)
        val l2 = System.nanoTime()
        response.body()?.let {
            if (it.contentLength()>0){
                val responseBody = response.peekBody(it.contentLength())
                info {
                    "接收响应:[${response.request().url()}],\n" +
                            "返回JSON:${responseBody.string()},\n" +
                            "耗时:${(l2 - l1) / 1e6}ms,\n" +
                            "${response.headers()}"
                }
            }
        }
        return response
    }
}