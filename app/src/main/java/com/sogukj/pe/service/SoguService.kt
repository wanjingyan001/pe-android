package com.sogukj.service

import com.sogukj.pe.service.Payload
import io.reactivex.Observable
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

/**
 * Created by qinfei on 17/6/2.
 */

interface SoguService {

    //发送手机验证码
    @FormUrlEncoded
    @POST("/api/index/send_code")
    fun sendVerifyCode(@Field("phone") phone: String): Observable<Payload<Object>>

    //登录
    @FormUrlEncoded
    @POST("/api/index/verify_code")
    fun login(@Field("phone") phone: String,
              @Field("code") code: String): Observable<Payload<Object>>

}