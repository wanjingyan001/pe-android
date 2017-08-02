package com.sogukj.service

import com.sogukj.pe.bean.DepartmentBean
import com.sogukj.pe.bean.NewsBean
import com.sogukj.pe.bean.ProjectBean
import com.sogukj.pe.bean.UserBean
import com.sogukj.pe.service.Payload
import io.reactivex.Observable
import okhttp3.RequestBody
import retrofit2.http.Body
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
    fun userDepart(@Field(SoguApi.APPKEY_NAME) appkey: String = SoguApi.APPKEY_VALUE): Observable<Payload<List<DepartmentBean>>>

    @FormUrlEncoded
    @POST("/api/userFont/getFrontUserInfo")
    fun userInfo(@Field("uid") uid: Int): Observable<Payload<UserBean>>

    @FormUrlEncoded
    @POST("/api/news/newsLists")
    fun newsList(@Field("page") page: Int, @Field("pageSize") pageSize: Int = 20, @Field("user_id") user_id: Int? = null, @Field("type") type: Int? = null, @Field("company_id") company_id: Int? = null)
            : Observable<Payload<List<NewsBean>>>

    @FormUrlEncoded
    @POST("/api/news/focusCompanyLists")
    fun projectList(@Field("page") page: Int, @Field("pageSize") pageSize: Int = 20, @Field("user_id") user_id: Int? = null, @Field("type") type: Int? = null): Observable<Payload<List<ProjectBean>>>

    @FormUrlEncoded
    @POST("/api/news/newsInfo")
    fun newsInfo(@Field("table_id") table_id: Int, @Field("data_id") data_id: Int): Observable<Payload<Map<String, Object>>>

    @POST("/api/Userfont/uploadImage")
    fun uploadImg(@Body body: RequestBody): Observable<Payload<Object>>

    @FormUrlEncoded
    @POST("/api/userFont/changeMyInfo")
    fun saveUser(@Field("uid") uid: Int
                 , @Field("name") name: String? = null
                 , @Field("depart_id") depart_id: Int? = null
                 , @Field("position") position: String? = null
                 , @Field("phone") phone: String? = null
                 , @Field("project") project: String? = null
                 , @Field("memo") memo: String? = null
                 , @Field("email") email: String? = null
                 , @Field(SoguApi.APPKEY_NAME) appkey: String = SoguApi.APPKEY_VALUE
    ): Observable<Payload<Object>>
}