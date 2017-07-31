package com.sogukj.service

import android.app.Application
import com.framework.util.Encoder
import com.sogukj.pe.Consts
import okhttp3.FormBody
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit


class SoguApi {

    private val apiService: SoguService
    private val context: Application

    private constructor(context: Application) {
        this.context = context
        val client = OkHttpClient.Builder()
                .addInterceptor { chain ->
                    val request = chain.request()
                    val requestBuilder = request.newBuilder()
                    if (request.body() is FormBody) {
                        val newBody = FormBody.Builder()
                        val body = request.body() as FormBody
                        val buff = StringBuffer()
                        for (i in 0..body.size() - 1) {
                            newBody.addEncoded(body.encodedName(i), body.encodedValue(i))
                            buff.append(body.name(i)).append("=").append(body.value(i)).append("&")
                        }
                        newBody.addEncoded("appkey", "d5f17cafef0829b5")
                        buff.append("appkey=d5f17cafef0829b5")
                        buff.append("pe2017Signkey")
                        val sign = Encoder.md5(buff.toString())
                        newBody.add("sign", sign)
                        requestBuilder.method(request.method(), newBody.build())
                    }
                    val response = chain.proceed(requestBuilder.build())
                    response
                }
                .readTimeout(10, TimeUnit.SECONDS)
                .connectTimeout(10, TimeUnit.SECONDS)
                .build()

        val retrofit = Retrofit.Builder()
                .baseUrl(Consts.HTTP_HOST)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .client(client)
                .build()

        apiService = retrofit.create(SoguService::class.java)
    }

//    fun checkToken(appid: String, secret: String): Observable<Result<Map<String, String>>> {
//        val sign = Encoder.sha1(appid + "&" + secret + "sogu_")
//        return apiService.login(appid, sign)
//                .doOnNext { payload ->
//                    if (payload.isOk) {
//                        val tmp = payload.payload["token"]
//                        var userInfo: UserInfo? = Store.store.getUserInfo(context)
//                        if (null == userInfo) userInfo = UserInfo()
//                        userInfo.token = tmp
//                        Store.store.setUserInfo(context, userInfo)
//                        Trace.d(TAG, userInfo.token)
//                    }
//                }
//
//    }

    companion object {
        var TAG = SoguApi::class.java.simpleName

        private var sApi: SoguApi? = null

        @Synchronized fun getApi(ctx: Application): SoguApi {
            if (null == sApi) sApi = SoguApi(ctx)
            return sApi!!
        }

        fun getService(ctx: Application): SoguService {
            return getApi(ctx).apiService
        }
    }
}
