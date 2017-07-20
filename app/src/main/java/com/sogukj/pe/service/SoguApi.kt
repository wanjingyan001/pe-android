package com.sogukj.service

import android.app.Application
import com.sogukj.pe.Consts
import com.sogukj.util.Store
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
                    val userInfo = Store.store.getUserInfo(context)
                    val token = if (null != userInfo) userInfo.token else ""
                    val newRequest = chain.request().newBuilder()
                            .addHeader("Authorization", "Bearer " + token)
                            .build()
                    val response = chain.proceed(newRequest)
                    if (response.code() == 401) {
                        userInfo!!.token = ""
                        Store.store.setUserInfo(context, userInfo)
                    }
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
