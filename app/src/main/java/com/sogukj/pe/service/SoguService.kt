package com.sogukj.service

import com.sogukj.pe.bean.*
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
    fun sendVerifyCode(@Field("phone") phone: String
                       , @Field(APPKEY_NAME) appkey: String = APPKEY_VALUE): Observable<Payload<Object>>

    @FormUrlEncoded
    @POST("/api/index/verify_code")
    fun login(@Field("phone") phone: String
              , @Field("code") code: String
              , @Field(APPKEY_NAME) appkey: String = APPKEY_VALUE): Observable<Payload<UserBean>>

    @FormUrlEncoded
    @POST("/api/UserFont/getDepartmentInfo")
    fun userDepart(@Field(APPKEY_NAME) appkey: String = APPKEY_VALUE): Observable<Payload<List<DepartmentBean>>>

    @FormUrlEncoded
    @POST("/api/userFont/getFrontUserInfo")
    fun userInfo(@Field("uid") uid: Int, @Field(APPKEY_NAME) appkey: String = APPKEY_VALUE): Observable<Payload<UserBean>>

    @FormUrlEncoded
    @POST("/api/news/newsLists")
    fun listNews(@Field("page") page: Int, @Field("pageSize") pageSize: Int = 20
                 , @Field("user_id") user_id: Int? = null
                 , @Field("type") type: Int? = null
                 , @Field("company_id") company_id: Int? = null
                 , @Field(APPKEY_NAME) appkey: String = APPKEY_VALUE)
            : Observable<Payload<List<NewsBean>>>

    @FormUrlEncoded
    @POST("/api/news/focusCompanyLists")
    fun listProject(@Field("page") page: Int
                    , @Field("pageSize") pageSize: Int = 20
                    , @Field("uid") user_id: Int? = null
                    , @Field("type") type: Int? = null
                    , @Field(APPKEY_NAME) appkey: String = APPKEY_VALUE): Observable<Payload<List<ProjectBean>>>

    @FormUrlEncoded
    @POST("/api/news/newsInfo")
    fun newsInfo(@Field("table_id") table_id: Int
                 , @Field("data_id") data_id: Int
                 , @Field(APPKEY_NAME) appkey: String = APPKEY_VALUE): Observable<Payload<Map<String, Object>>>

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
                 , @Field(APPKEY_NAME) appkey: String = APPKEY_VALUE
    ): Observable<Payload<Object>>

    @FormUrlEncoded
    @POST("/api/news/applyNewProject")
    fun addProject(
            @Field("name") name: String
            , @Field("legalPersonName") legalPersonName: String? = null
            , @Field("regLocation") regLocation: String? = null
            , @Field("creditCode") creditCode: String? = null
            , @Field("info") info: String? = null
            , @Field(APPKEY_NAME) appkey: String = APPKEY_VALUE
    ): Observable<Payload<Object>>

    @FormUrlEncoded
    @POST("/api/Listinformation/volatility")
    fun companyInfo(
            @Field("company_id") company_id: String
            , @Field(APPKEY_NAME) appkey: String = APPKEY_VALUE
    )

    @FormUrlEncoded
    @POST("/api/news/ncFocus")
    fun mark(@Field("uid") uid: Int
             , @Field("company_id") company_id: Int
             , @Field("type") type: Int = 1
             , @Field(APPKEY_NAME) appkey: String = APPKEY_VALUE
    ): Observable<Payload<Object>>

    @FormUrlEncoded
    @POST("/api/Listinformation/Volatility")
    fun stockInfo(
            @Field("company_id") company_id: Int
            , @Field(APPKEY_NAME) appkey: String = APPKEY_VALUE
    ): Observable<Payload<StockBean>>

    @FormUrlEncoded
    @POST("/api/Listinformation/Companyinfo")
    fun companyInfo(
            @Field("company_id") company_id: Int
            , @Field(APPKEY_NAME) appkey: String = APPKEY_VALUE
    ): Observable<Payload<CompanyBean>>

    @FormUrlEncoded
    @POST("/api/Listinformation/Seniorexecutive")
    fun listSeniorExecutive(
            @Field("company_id") company_id: Int
            , @Field(APPKEY_NAME) appkey: String = APPKEY_VALUE
    ): Observable<Payload<List<GaoGuanBean>>>

    @FormUrlEncoded
    @POST("/api/Listinformation/Holdingcompany")
    fun cangu(
            @Field("company_id") company_id: Int
            , @Field(APPKEY_NAME) appkey: String = APPKEY_VALUE
    ): Observable<Payload<List<CanGuBean>>>

    @FormUrlEncoded
    @POST("/api/Listinformation/Announcement")
    fun listAnnouncement(
            @Field("company_id") company_id: Int
            , @Field(APPKEY_NAME) appkey: String = APPKEY_VALUE
    ): Observable<Payload<List<AnnouncementBean>>>

    @FormUrlEncoded
    @POST("/api/Listinformation/Issuerelated")
    fun issueInfo(
            @Field("company_id") company_id: Int
            , @Field(APPKEY_NAME) appkey: String = APPKEY_VALUE
    ): Observable<Payload<IssueRelatedBean>>

    @FormUrlEncoded
    @POST("/api/Listinformation/Equitychange")
    fun listEquityChange(
            @Field("company_id") company_id: Int
            , @Field(APPKEY_NAME) appkey: String = APPKEY_VALUE
    ): Observable<Payload<List<EquityChangeBean>>>

    @FormUrlEncoded
    @POST("/api/Listinformation/Bonusinfo")
    fun listBonusInfo(
            @Field("company_id") company_id: Int
            , @Field("page") page: Int = 1
            , @Field("pageSize") pageSize: Int = 20
            , @Field(APPKEY_NAME) appkey: String = APPKEY_VALUE
    ): Observable<Payload<List<BonusBean>>>

    @FormUrlEncoded
    @POST("/api/Listinformation/Allotmen")
    fun listAllotment(
            @Field("company_id") company_id: Int
            , @Field("page") page: Int = 1
            , @Field("pageSize") pageSize: Int = 20
            , @Field(APPKEY_NAME) appkey: String = APPKEY_VALUE
    ): Observable<Payload<List<AllotmentBean>>>

    @FormUrlEncoded
    @POST("/api/Stockinfo/tenShareHolder")
    fun listTenShareHolders(
            @Field("company_id") company_id: Int
            , @Field("shareholder_type") shareholder_type: Int = 1
            , @Field(APPKEY_NAME) appkey: String = APPKEY_VALUE
    ): Observable<Payload<List<TimeGroupedShareHolderBean>>>

    @FormUrlEncoded
    @POST("/api/Stockinfo/tenShareHolder")
    fun gubenjiegou(
            @Field("company_id") company_id: Int
            , @Field("shareholder_type") shareholder_type: Int = 3
            , @Field(APPKEY_NAME) appkey: String = APPKEY_VALUE
    ): Observable<Payload<List<TimeGroupedCapitalStructureBean>>>

    @FormUrlEncoded
    @POST("/api/Listinformation/Sgcompanyinfo")
    fun bizinfo(
            @Field("company_id") company_id: Int
            , @Field(APPKEY_NAME) appkey: String = APPKEY_VALUE
    ): Observable<Payload<BizInfoBean>>

    @FormUrlEncoded
    @POST("/api/Listinformation/Holder")
    fun listShareholderInfo(
            @Field("company_id") company_id: Int
            , @Field("page") page: Int = 1
            , @Field("pageSize") pageSize: Int = 20
            , @Field(APPKEY_NAME) appkey: String = APPKEY_VALUE
    ): Observable<Payload<List<ShareHolderBean>>>

    @FormUrlEncoded
    @POST("/api/Listinformation/Annualreport")
    fun listAnnualReport(
            @Field("company_id") company_id: Int
            , @Field("page") page: Int = 1
            , @Field("pageSize") pageSize: Int = 50
            , @Field(APPKEY_NAME) appkey: String = APPKEY_VALUE
    ): Observable<Payload<List<AnnualReportBean>>>

    @FormUrlEncoded
    @POST("/api/Listinformation/Changeinfo")
    fun listChangeRecord(
            @Field("company_id") company_id: Int
            , @Field("page") page: Int = 1
            , @Field("pageSize") pageSize: Int = 20
            , @Field(APPKEY_NAME) appkey: String = APPKEY_VALUE
    ): Observable<Payload<List<ChangeRecordBean>>>

    @FormUrlEncoded
    @POST("/api/Listinformation/Invest")
    fun listInvestment(
            @Field("company_id") company_id: Int
            , @Field("page") page: Int = 1
            , @Field("pageSize") pageSize: Int = 20
            , @Field(APPKEY_NAME) appkey: String = APPKEY_VALUE
    ): Observable<Payload<List<InvestmentBean>>>

    @FormUrlEncoded
    @POST("/api/Listinformation/Staff")
    fun listKeyPersonal(
            @Field("company_id") company_id: Int
            , @Field("page") page: Int = 1
            , @Field("pageSize") pageSize: Int = 20
            , @Field(APPKEY_NAME) appkey: String = APPKEY_VALUE
    ): Observable<Payload<List<KeyPersonalBean>>>

    @FormUrlEncoded
    @POST("/api/Stockinfo/equityRatio")
    fun equityStructure(
            @Field("company_id") company_id: Int
            , @Field("page") page: Int = 1
            , @Field("pageSize") pageSize: Int = 1000
            , @Field(APPKEY_NAME) appkey: String = APPKEY_VALUE
    ): Observable<Payload<EquityStructureBean>>

    @FormUrlEncoded
    @POST("/api/Listinformation/Findhistoryrongzi")
    fun listFinanceHistory(
            @Field("company_id") company_id: Int
            , @Field("page") page: Int = 1
            , @Field("pageSize") pageSize: Int = 20
            , @Field(APPKEY_NAME) appkey: String = APPKEY_VALUE
    ): Observable<Payload<List<FinanceHistoryBean>>>

    @FormUrlEncoded
    @POST("/api/Listinformation/Findtzanli")
    fun listInvestEvent(
            @Field("company_id") company_id: Int
            , @Field("page") page: Int = 1
            , @Field("pageSize") pageSize: Int = 20
            , @Field(APPKEY_NAME) appkey: String = APPKEY_VALUE
    ): Observable<Payload<List<InvestEventBean>>>

    @FormUrlEncoded
    @POST("/api/Stockinfo/InvestDistribute")
    fun listInvestDistribute(
            @Field("company_id") company_id: Int
            , @Field(APPKEY_NAME) appkey: String = APPKEY_VALUE
    ): Observable<Payload<Map<String, Any>>>

    @FormUrlEncoded
    @POST("/api/Listinformation/Findteammember")
    fun listCoreTeam(
            @Field("company_id") company_id: Int
            , @Field("page") page: Int = 1
            , @Field("pageSize") pageSize: Int = 20
            , @Field(APPKEY_NAME) appkey: String = APPKEY_VALUE
    ): Observable<Payload<List<TeamMemberBean>>>

    @FormUrlEncoded
    @POST("/api/Listinformation/Getproductinfo")
    fun listBizInfo(
            @Field("company_id") company_id: Int
            , @Field("page") page: Int = 1
            , @Field("pageSize") pageSize: Int = 20
            , @Field(APPKEY_NAME) appkey: String = APPKEY_VALUE
    ): Observable<Payload<List<ProductBean>>>

    @FormUrlEncoded
    @POST("/api/Listinformation/Findjingpin")
    fun listProductInfo(
            @Field("company_id") company_id: Int
            , @Field("page") page: Int = 1
            , @Field("pageSize") pageSize: Int = 20
            , @Field(APPKEY_NAME) appkey: String = APPKEY_VALUE
    ): Observable<Payload<List<ProductBean>>>

    @FormUrlEncoded
    @POST("/api/Listinformation/Employments")
    fun listRecruit(
            @Field("company_id") company_id: Int
            , @Field("page") page: Int = 1
            , @Field("pageSize") pageSize: Int = 20
            , @Field(APPKEY_NAME) appkey: String = APPKEY_VALUE
    ): Observable<Payload<List<RecruitBean>>>

    @FormUrlEncoded
    @POST("/api/Listinformation/Bond")
    fun listBond(
            @Field("company_id") company_id: Int
            , @Field("page") page: Int = 1
            , @Field("pageSize") pageSize: Int = 20
            , @Field(APPKEY_NAME) appkey: String = APPKEY_VALUE
    ): Observable<Payload<List<BondBean>>>

    @FormUrlEncoded
    @POST("/api/Listinformation/Taxcredit")
    fun listTaxRate(
            @Field("company_id") company_id: Int
            , @Field("page") page: Int = 1
            , @Field("pageSize") pageSize: Int = 20
            , @Field(APPKEY_NAME) appkey: String = APPKEY_VALUE
    ): Observable<Payload<List<TaxRateBean>>>

    @FormUrlEncoded
    @POST("/api/Listinformation/Purchaseland")
    fun listLandPurchase(
            @Field("company_id") company_id: Int
            , @Field("page") page: Int = 1
            , @Field("pageSize") pageSize: Int = 20
            , @Field(APPKEY_NAME) appkey: String = APPKEY_VALUE
    ): Observable<Payload<List<LandPurchaseBean>>>

    companion object {
        const val APPKEY_NAME = "appkey"
        const val APPKEY_VALUE = "d5f17cafef0829b5"
    }
}