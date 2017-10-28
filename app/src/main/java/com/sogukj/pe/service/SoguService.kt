package com.sogukj.service

import com.sogukj.pe.bean.*
import com.sogukj.pe.service.Payload
import io.reactivex.Observable
import okhttp3.Call
import okhttp3.RequestBody
import okhttp3.Response
import okhttp3.ResponseBody
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
                 , @Field("uid") uid: Int? = null
                 , @Field("type") type: Int? = null
                 , @Field("company_id") company_id: Int? = null
                 , @Field("fuzzyQuery") fuzzyQuery: String? = null
                 , @Field(APPKEY_NAME) appkey: String = APPKEY_VALUE)
            : Observable<Payload<List<NewsBean>>>

    @FormUrlEncoded
    @POST("/api/news/focusCompanyLists")
    fun listProject(@Field("offset") offset: Int
                    , @Field("pageSize") pageSize: Int = 20
                    , @Field("uid") uid: Int? = null
                    , @Field("type") type: Int? = null
                    , @Field("sort") sort: Int? = null
                    , @Field("fuzzyQuery") fuzzyQuery: String? = null
                    , @Field(APPKEY_NAME) appkey: String = APPKEY_VALUE): Observable<Payload<List<ProjectBean>>>

    @FormUrlEncoded
    @POST("/api/news/newsInfo")
    fun newsInfo(@Field("table_id") table_id: Int
                 , @Field("data_id") data_id: Int
                 , @Field(APPKEY_NAME) appkey: String = APPKEY_VALUE): Observable<Payload<Map<String, Object?>>>

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
                 , @Field("advice_token") advice_token: String? = null
                 , @Field("phone_type") phone_type: Int = 1
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
            , @Field("page") page: Int = 1
            , @Field("pageSize") pageSize: Int = 20
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

    @FormUrlEncoded
    @POST("/api/Listinformation/Bids")
    fun listBids(
            @Field("company_id") company_id: Int
            , @Field("page") page: Int = 1
            , @Field("pageSize") pageSize: Int = 20
            , @Field(APPKEY_NAME) appkey: String = APPKEY_VALUE
    ): Observable<Payload<List<BidBean>>>

    @FormUrlEncoded
    @POST("/api/Listinformation/Qualification")
    fun listQualification(
            @Field("company_id") company_id: Int
            , @Field("page") page: Int = 1
            , @Field("pageSize") pageSize: Int = 20
            , @Field(APPKEY_NAME) appkey: String = APPKEY_VALUE
    ): Observable<Payload<List<QualificationBean>>>

    @FormUrlEncoded
    @POST("/api/Listinformation/Checkinfo")
    fun listCheck(
            @Field("company_id") company_id: Int
            , @Field("page") page: Int = 1
            , @Field("pageSize") pageSize: Int = 20
            , @Field(APPKEY_NAME) appkey: String = APPKEY_VALUE
    ): Observable<Payload<List<CheckBean>>>

    @FormUrlEncoded
    @POST("/api/Listinformation/Appbkinfo")
    fun listApp(
            @Field("company_id") company_id: Int
            , @Field("page") page: Int = 1
            , @Field("pageSize") pageSize: Int = 20
            , @Field(APPKEY_NAME) appkey: String = APPKEY_VALUE
    ): Observable<Payload<List<AppBean>>>

    @FormUrlEncoded
    @POST("/api/Listinformation/Tm")
    fun listBrand(
            @Field("company_id") company_id: Int
            , @Field("page") page: Int = 1
            , @Field("pageSize") pageSize: Int = 20
            , @Field(APPKEY_NAME) appkey: String = APPKEY_VALUE
    ): Observable<Payload<List<BrandBean>>>

    @FormUrlEncoded
    @POST("/api/Listinformation/Patents")
    fun listPatent(
            @Field("company_id") company_id: Int
            , @Field("page") page: Int = 1
            , @Field("pageSize") pageSize: Int = 20
            , @Field(APPKEY_NAME) appkey: String = APPKEY_VALUE
    ): Observable<Payload<List<PatentBean>>>

    @FormUrlEncoded
    @POST("/api/Stockinfo/Copyreg")
    fun listCopyright(
            @Field("company_id") company_id: Int
            , @Field("type") type: Int = 1
            , @Field("page") page: Int = 1
            , @Field("pageSize") pageSize: Int = 20
            , @Field(APPKEY_NAME) appkey: String = APPKEY_VALUE
    ): Observable<Payload<List<CopyRightBean>>>

    @FormUrlEncoded
    @POST("/api/Listinformation/Icp")
    fun listICP(
            @Field("company_id") company_id: Int
            , @Field("page") page: Int = 1
            , @Field("pageSize") pageSize: Int = 20
            , @Field(APPKEY_NAME) appkey: String = APPKEY_VALUE
    ): Observable<Payload<List<IcpBean>>>

    @FormUrlEncoded
    @POST("/api/news/companyInfo")
    fun projectPage(
            @Field("company_id") company_id: Int
            , @Field("page") page: Int = 1
            , @Field("pageSize") pageSize: Int = 3
            , @Field("uid") uid: Int?
            , @Field(APPKEY_NAME) appkey: String = APPKEY_VALUE
    ): Observable<Payload<ProjectDetailBean>>

    @FormUrlEncoded
    @POST("/api/news/changeStatus")
    fun editProject(
            @Field("company_id") company_id: Int
            , @Field(APPKEY_NAME) appkey: String = APPKEY_VALUE
    ): Observable<Payload<Object>>

    @FormUrlEncoded
    @POST("/api/news/deleteProject")
    fun delProject(
            @Field("company_id") company_id: Int
            , @Field(APPKEY_NAME) appkey: String = APPKEY_VALUE
    ): Observable<Payload<Object>>

    @FormUrlEncoded
    @POST("/api/Listinformation/Branch")
    fun listBranch(
            @Field("company_id") company_id: Int
            , @Field("page") page: Int = 1
            , @Field("pageSize") pageSize: Int = 20
            , @Field(APPKEY_NAME) appkey: String = APPKEY_VALUE
    ): Observable<Payload<List<BranchBean>>>

    @FormUrlEncoded
    @POST("/api/Stockinfo/Companyintro")
    fun companyInfo2(
            @Field("company_id") company_id: Int
            , @Field("page") page: Int = 1
            , @Field("pageSize") pageSize: Int = 20
            , @Field(APPKEY_NAME) appkey: String = APPKEY_VALUE
    ): Observable<Payload<String>>

    /*
    name	varchar		公司名称	必传参数（非空）
    uid	int		用户id	必传参数（非空）
    info	text		相关概念	可空
    estiblishTime	date		成立时间	可空(日期形式例如2017-01-01)
    enterpriseType	varchar		企业性质	可空
    regCapital	varchar		注册资金	可空
    mainBusiness	varchar		主营业务	可空
    ownershipRatio	varchar		股权比例	可空
    lastYearIncome	varchar		去年营收	可空
    lastYearProfit	varchar		去年利润	可空
    ThisYearIncome	varchar		今年营收	可空
    ThisYearProfit	varchar		今年利润	可空
    lunci	varchar		融资轮次	可空
    appraisement	varchar		投后估值	可空
    financeUse	varchar		融资用途	可空
    capitalPlan	varchar		资本规划	可空
     */
    @FormUrlEncoded
    @POST("/api/news/addStoreProject")
    fun addStoreProject(
            @Field("name") name: String
            , @Field("info") info: String? = null
            , @Field("estiblishTime") estiblishTime: String? = null//yyyy-MM-dd
            , @Field("enterpriseType") enterpriseType: String? = null
            , @Field("regCapital") regCapital: String? = null
            , @Field("mainBusiness") mainBusiness: String? = null
            , @Field("ownershipRatio") ownershipRatio: String? = null
            , @Field("lastYearIncome") lastYearIncome: String? = null
            , @Field("lastYearProfit") lastYearProfit: String? = null
            , @Field("thisYearIncome") thisYearIncome: String? = null
            , @Field("thisYearProfit") thisYearProfit: String? = null
            , @Field("lunci") lunci: String? = null
            , @Field("appraisement") appraisement: String? = null
            , @Field("financeUse") financeUse: String? = null
            , @Field("capitalPlan") capitalPlan: String? = null
            , @Field(APPKEY_NAME) appkey: String = APPKEY_VALUE
    ): Observable<Payload<Object>>

    @FormUrlEncoded
    @POST("/api/news/editStoreProject")
    fun editStoreProject(
            @Field("company_id") company_id: Int
            , @Field("name") name: String
            , @Field("info") info: String? = null
            , @Field("estiblishTime") estiblishTime: String? = null//yyyy-MM-dd
            , @Field("enterpriseType") enterpriseType: String? = null
            , @Field("regCapital") regCapital: String? = null
            , @Field("mainBusiness") mainBusiness: String? = null
            , @Field("ownershipRatio") ownershipRatio: String? = null
            , @Field("lastYearIncome") lastYearIncome: String? = null
            , @Field("lastYearProfit") lastYearProfit: String? = null
            , @Field("thisYearIncome") thisYearIncome: String? = null
            , @Field("thisYearProfit") thisYearProfit: String? = null
            , @Field("lunci") lunci: String? = null
            , @Field("appraisement") appraisement: String? = null
            , @Field("financeUse") financeUse: String? = null
            , @Field("capitalPlan") capitalPlan: String? = null
            , @Field(APPKEY_NAME) appkey: String = APPKEY_VALUE
    ): Observable<Payload<Object>>

    @FormUrlEncoded
    @POST("/api/news/getStoreProject")
    fun getStoreProject(@Field("company_id") company_id: Int): Observable<Payload<StoreProjectBean>>


    @FormUrlEncoded
    @POST("/api/news/setUpProject")
    fun setUpProject(@Field("company_id") company_id: Int): Observable<Payload<Object>>

    @FormUrlEncoded
    @POST("/api/Listinformation/projectBook")
    fun projectBook(@Field("company_id") company_id: Int
                    , @Field("page") page: Int = 1
                    , @Field("pageSize") pageSize: Int = 20
                    , @Field("fileClass") fileClass: Int? = null
                    , @Field("fuzzQuery") fuzzQuery: String? = null
    ): Observable<Payload<ProjectBookRSBean>>

    @POST("/api/Listinformation/projectFilter")
    fun projectFilter(): Observable<Payload<Map<Int, String>>>

    @POST("/api/Listinformation/uploadBook")
    fun uploadBook(@Body body: RequestBody): Observable<Payload<Object>>

    @POST("/api/Approve/uploadApprove")
    fun uploadApprove(@Body body: RequestBody): Observable<Payload<CustomSealBean.ValueBean>>

    @FormUrlEncoded
    @POST("/api/Listinformation/projectSelect")
    fun projectBookSearch(
            @Field("company_id") company_id: Int
            , @Field("page") page: Int = 1
            , @Field("pageSize") pageSize: Int = 20
            , @Field("fileClass") fileClass: String? = null
            , @Field("fuzzyQuery") fuzzyQuery: String? = null
            , @Field("status") status: Int? = null): Observable<Payload<List<ProjectBookBean>>>

    @FormUrlEncoded
    @POST("/api/Index/apply")
    fun mainApprove(@Field("pid") pid: Int = 3): Observable<Payload<List<SpGroupBean>>>

    @FormUrlEncoded
    @POST("/api/Approve/componentInfo")
    fun approveInfo(@Field("template_id") template_id: Int = 1
                    , @Field("sid") sid: Int? = null): Observable<Payload<List<CustomSealBean>>>

    @FormUrlEncoded
    @POST("/api/Approve/approveInfo")
    fun approver(@Field("template_id") template_id: Int = 1
                 , @Field("type") type: Int? = null): Observable<Payload<List<ApproverBean>>>

    @FormUrlEncoded
    @POST("/api/Approve/getFundOrProject")
    fun listSelector(
            @Field("page") page: Int = 1
            , @Field("pageSize") pageSize: Int = 20
            , @Field("type") type: Int): Observable<Payload<List<CustomSealBean.ValueBean>>>

    @POST("/api/Approve/submitApprove")
    fun submitApprove(@Body body: RequestBody): Observable<Payload<Object>>

    @FormUrlEncoded
    @POST("/api/Approve/waitingMeApproval")
    fun listApproval(
            @Field("page") page: Int = 1
            , @Field("pageSize") pageSize: Int = 20
            , @Field("status") status: Int
            , @Field("fuzzyQuery") fuzzyQuery: String? = null
            , @Field("type") type: Int? = null
            , @Field("template_id") template_id: String? = null
            , @Field("filter") filter: String? = null
            , @Field("sort") sort: Int? = null): Observable<Payload<List<ApprovalBean>>>


    @POST("/api/Approve/approveFilter")
    fun approveFilter(): Observable<Payload<ApproveFilterBean>>

    @FormUrlEncoded
    @POST("/api/Approve/approveShow")
    fun showApprove(@Field("approval_id") approval_id: Int, @Field("type") type: String? = null): Observable<Payload<ApproveViewBean>>

    @FormUrlEncoded
    @POST("/api/Approve/approveResult")
    fun examineApprove(@Field("approval_id") approval_id: Int
                       , @Field("type") type: Int? = null
                       , @Field("content") content: String): Observable<Payload<Object>>

    @FormUrlEncoded
    @POST("/api/Approve/applyUrgent")
    fun approveUrgent(@Field("approval_id") approval_id: Int): Observable<Payload<Object>>

    @FormUrlEncoded
    @POST("/api/Approve/approveResult")
    fun submitComment(@Field("hid") hid: Int
                      , @Field("comment_id") comment_id: Int = 0
                      , @Field("content") content: String): Observable<Payload<Object>>

//    @FormUrlEncoded
//    @POST("/api/Approve/applyUrgent")
//    fun approveUrgent(@Field("approval_id") approval_id: Int): Observable<Payload<String>>

    companion object {
        const val APPKEY_NAME = "appkey"
        const val APPKEY_VALUE = "d5f17cafef0829b5"
    }
}