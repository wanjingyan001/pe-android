package com.sogukj.service

import com.sogukj.pe.bean.UserInfo
import com.sogukj.pe.util.Result
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST
import rx.Observable

/**
 * Created by qinfei on 17/6/2.
 */

interface SoguService {

    //发送手机验证码
    @FormUrlEncoded
    @POST("sendVerifyCode/v3")
    fun sendVerifyCode(@Field("mobile") mobile: String): Observable<Result<Object>>

    //登录
    @FormUrlEncoded
    @POST("auth/smsLogin/v2")
    fun login(
            @Field("mobile") mobile: String,
            @Field("verifyCode") verifyCode: String): Observable<Result<UserInfo>>

}