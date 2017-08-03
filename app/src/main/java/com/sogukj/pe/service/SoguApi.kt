package com.sogukj.service

import android.app.Application
import com.framework.util.Trace
import com.sogukj.pe.Consts
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
                    val response = chain.proceed(request)
                    Trace.i("http", "${request.url()} => ${response.code()}:${response.message()}")
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
        const val APPKEY_NAME = "appkey"
        const val APPKEY_VALUE = "d5f17cafef0829b5"
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
