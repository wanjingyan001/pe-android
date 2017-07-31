package com.sogukj.service

import com.sogukj.pe.bean.DepartmentBean
import com.sogukj.pe.bean.NewsBean
import com.sogukj.pe.bean.UserBean
import com.sogukj.pe.service.Payload
import io.reactivex.Observable
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

/**
 * Created by qinfei on 17/6/2.
 */

interface SoguService {

    @FormUrlEncoded
    @POST("/api/index/send_code")
    fun sendVerifyCode(@Field("phone") phone: String): Observable<Payload<Object>>

    @FormUrlEncoded
    @POST("/api/index/verify_code")
    fun login(@Field("phone") phone: String, @Field("code") code: String): Observable<Payload<UserBean>>

    @FormUrlEncoded
    @POST("/api/UserFont/getDepartmentInfo")
    fun userInfo(@Field("appkey") appkey: String = "d5f17cafef0829b5"): Observable<Payload<List<DepartmentBean>>>

    @FormUrlEncoded
    @POST("/api/news/newsLists")
    fun newsList(@Field("page") page: Int, @Field("pageSize") pageSize: Int = 20, @Field("user_id") user_id: String? = null, @Field("type") type: Int? = null, @Field("company_id") company_id: Int? = null)
            : Observable<Payload<List<NewsBean>>>

    @FormUrlEncoded
    @POST("/api/news/newsInfo")
    fun newsInfo(): Observable<Payload<Map<String, Object>>>


}