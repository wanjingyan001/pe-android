package com.sogukj.service

import android.app.Application
import android.widget.Toast
import com.google.gson.Gson
import com.sogukj.pe.Consts
import com.sogukj.pe.util.Trace
import com.sogukj.pe.util.Utils
import com.sogukj.util.Store
import com.sougukj.service.LoggingInterceptor
import okhttp3.CacheControl
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Created by qinfei on 17/7/18.
 */
class SoguApi {

    private val apiService: SoguService
    private val context: Application

    private constructor(context: Application) {
        this.context = context
        val client = OkHttpClient.Builder()
                .addInterceptor { chain ->
                    val builder = chain.request().newBuilder()
                    val user = Store.store.getUser(context);
                    if (null != user && null != user.uid) {
                        builder.addHeader("uid", user.uid.toString())
                    }
                    builder.addHeader("appkey", "d5f17cafef0829b5")
                    builder.addHeader("version", Utils.getVersionName(context))
                    builder.addHeader("client", "android")
                    val request = builder.build()
                    val response = chain.proceed(request)
                    Trace.i("http", "RequestBody:${Gson().toJson(response.request().body())}")
                    Trace.i("http", "${request.url()} => ${response.code()}:${response.message()}")
                    response
                }
                .retryOnConnectionFailure(false)
                .readTimeout(15, TimeUnit.SECONDS)
                .connectTimeout(15, TimeUnit.SECONDS)
                .build()

        val retrofit = Retrofit.Builder()
                .baseUrl(Consts.HTTP_HOST)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .client(client)
                .build()

        apiService = retrofit.create(SoguService::class.java)
    }

    companion object {
        var TAG = SoguApi::class.java.simpleName

        private var sApi: SoguApi? = null

        @Synchronized
        fun getApi(ctx: Application): SoguApi {
            if (null == sApi) sApi = SoguApi(ctx)
            return sApi!!
        }

        fun getService(ctx: Application): SoguService {
            return getApi(ctx).apiService
        }
    }
}
