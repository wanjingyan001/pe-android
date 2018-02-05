package com.sogukj.service

import com.sogukj.pe.bean.*
import com.sogukj.pe.service.Payload
import com.sogukj.pe.ui.IM.ChatFileBean
import com.sogukj.pe.ui.calendar.*
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
    @POST("/api/Userfont/changeMyInfo")
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
            , @Field("shortName") shortName: String
            , @Field("legalPersonName") legalPersonName: String? = null
            , @Field("regLocation") regLocation: String? = null
            , @Field("creditCode") creditCode: String? = null
            , @Field("info") info: String? = null
            , @Field("type") type: Int? = null
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

//    @FormUrlEncoded
//    @POST("/api/news/changeStatus")
//    fun editProject(
//            @Field("company_id") company_id: Int
//            , @Field(APPKEY_NAME) appkey: String = APPKEY_VALUE
//    ): Observable<Payload<Object>>

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
            , @Field("shortName") shortName: String
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
            , @Field("shortName") shortName: String
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


//    @FormUrlEncoded
//    @POST("/api/news/setUpProject")
//    fun setUpProject(@Field("company_id") company_id: Int): Observable<Payload<Object>>

    // 项目改投（修改项目状态）
    // 非空（1=>调研转储备，2=>储备转立项，3=>立项转已投，4=>已投转退出）
    @FormUrlEncoded
    @POST("/api/news/changeStatus")
    fun changeStatus(@Field("company_id") company_id: Int, @Field("status") status: Int): Observable<Payload<Object>>

    //非空（当type=1时1=>项目投资档案清单，2=>投资后项目跟踪管理清单，3=>项目推出档案清单    当type=2时,1=>储备期档案,2=>  存续期档案,3=> 退出期档案)
    @FormUrlEncoded
    @POST("/api/Listinformation/projectBook")
    fun projectBook(@Field("type") type: Int, @Field("company_id") company_id: Int): Observable<Payload<ProjectBookRSBean>>

    @POST("/api/Listinformation/projectFilter")
    fun projectFilter(): Observable<Payload<Map<Int, String>>>

    @POST("/api/Listinformation/uploadBook")
    fun uploadBook(@Body body: RequestBody): Observable<Payload<Object>>

    @POST("/api/Approve/uploadApprove")
    fun uploadApprove(@Body body: RequestBody): Observable<Payload<CustomSealBean.ValueBean>>

    //type	number	1	文件类型	非空（1=>项目文书，2=>基金类型）
    @FormUrlEncoded
    @POST("/api/Listinformation/projectSelect")
    fun projectBookSearch(
            @Field("company_id") company_id: Int
            , @Field("page") page: Int = 1
            , @Field("pageSize") pageSize: Int = 20
            , @Field("type") type: Int
            , @Field("fileClass") fileClass: String? = null
            , @Field("fuzzyQuery") fuzzyQuery: String? = null
            , @Field("status") status: Int? = null): Observable<Payload<List<ProjectBookBean>>>

    @FormUrlEncoded
    @POST("/api/Index/apply")
    fun mainApprove(@Field("pid") pid: Int = 3): Observable<Payload<List<SpGroupBean>>>

    @FormUrlEncoded
    @POST("/api/Approve/componentInfo")
    fun approveInfo(@Field("template_id") template_id: Int? = null
                    , @Field("sid") sid: Int? = null): Observable<Payload<List<CustomSealBean>>>

    @FormUrlEncoded
    @POST("/api/Approve/approveInfo")
    fun approver(@Field("template_id") template_id: Int? = null
                 , @Field("sid") sid: Int? = null
                 , @Field("type") type: Int? = null): Observable<Payload<List<ApproverBean>>>

    @FormUrlEncoded
    @POST("/api/Approve/getFundOrProject")
    fun listSelector(
            @Field("page") page: Int = 1
            , @Field("pageSize") pageSize: Int = 20
            , @Field("type") type: Int
            , @Field("fuzzyQuery") fuzzyQuery: String? = null): Observable<Payload<List<CustomSealBean.ValueBean>>>

    @POST("/api/Approve/submitApprove")
    fun submitApprove(@Body body: RequestBody): Observable<Payload<Object>>

    @POST("/api/Approve/updateApprove")
    fun updateApprove(@Body body: RequestBody): Observable<Payload<Int>>

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
    fun showApprove(@Field("approval_id") approval_id: Int, @Field("type") type: Int? = null, @Field("classify") classify: Int? = null
                    , @Field("is_mine") is_mine: Int)
            : Observable<Payload<ApproveViewBean>>

    @FormUrlEncoded
    @POST("/api/Approve/signShow")
    fun showApproveSign(@Field("approval_id") approval_id: Int, @Field("type") type: String? = null): Observable<Payload<ApproveViewBean>>


    @FormUrlEncoded
    @POST("/api/Approve/approveResult")
    fun examineApprove(@Field("approval_id") approval_id: Int
                       , @Field("type") type: Int? = null
                       , @Field("content") content: String): Observable<Payload<Object>>

    @FormUrlEncoded
    @POST("/api/Approve/applyUrgent")
    fun approveUrgent(@Field("approval_id") approval_id: Int): Observable<Payload<Object>>

    @FormUrlEncoded
    @POST("/api/Approve/finishApprove")
    fun finishApprove(@Field("approval_id") approval_id: Int): Observable<Payload<Object>>

    @POST("/api/Approve/signResult")
    fun approveSign(@Body body: RequestBody): Observable<Payload<Object>>

    @FormUrlEncoded
    @POST("/api/Approve/Comment")
    fun submitComment(@Field("hid") hid: Int
                      , @Field("comment_id") comment_id: Int = 0
                      , @Field("content") content: String): Observable<Payload<Object>>

    @FormUrlEncoded
    @POST("/api/Approve/updateApprove")
    fun resubApprove(@Field("approval_id") approval_id: Int): Observable<Payload<Object>>

    @FormUrlEncoded
    @POST("/api/Approve/derivePdf")
    fun exportPdf(@Field("approval_id") approval_id: Int): Observable<Payload<String>>

    @POST("/api/Message/getMessageIndex")
    fun msgIndex(): Observable<Payload<MessageIndexBean>>

    //可空（1=>待审批，2=>已审批）
    @FormUrlEncoded
    @POST("/api/Message/getMessageList")
    fun msgList(@Field("status") status: Int? = null): Observable<Payload<ArrayList<MessageBean>>>

    //    @FormUrlEncoded
//    @POST("/api/Approve/applyUrgent")
//    fun approveUrgent(@Field("approval_id")z approval_id: Int): Observable<Payload<String>>
    /**
     * 获取所有基金公司列表
     */
    @FormUrlEncoded
    @POST("/api/Foundation/fundList")
    fun getAllFunds(@Field("page") page: Int = 1,
                    @Field("pageSize") pageSize: Int = 20,
                    @Field("sort") sort: Int,
                    @Field("fuzzyQuery") fuzzyQuery: String = "",
                    @Field("type") type: Int): Observable<Payload<List<FundSmallBean>>>

    /**
     * 获取指定基金的详情
     */
    @FormUrlEncoded
    @POST("/api/Foundation/fundInfo")
    fun getFundDetail(@Field("fund_id") fund_id: Int): Observable<Payload<FundDetail>>

    /**
     * 获取指定基金的架构
     */
    @FormUrlEncoded
    @POST("/api/Foundation/fundStructure")
    fun getFundStructure(@Field("fund_id") fund_id: Int): Observable<Payload<FundStructure>>

    /**
     * 获取指定基金的台账
     */
    @FormUrlEncoded
    @POST("/api/Foundation/fundLedger")
    fun getFundAccount(@Field("fund_id") fund_id: Int): Observable<Payload<FundAccount>>

    /**
     * 征信开始界面接口
     */
    @FormUrlEncoded
    @POST("/api/Credit/showCreditInfo")
    fun showCreditInfo(@Field("company_id") company_id: Int): Observable<Payload<CreditInfo>>

    /**
     * 征信-敏感信息
     */
    @POST("/api/Credit/sensitiveData")
    fun sensitiveData(@Body map: HashMap<String, Any>): Observable<Payload<SensitiveInfo>>

    /**
     * 涉诉信息相关
     */
    @FormUrlEncoded
    @POST("/api/Credit/declarationList")
    fun declarationList(@Field("id") id: Int,
                        @Field("type") type: String): Observable<Payload<List<SecondaryBean>>>


    /**
     * 一键查询
     */
//    @FormUrlEncoded
    @POST("/api/Credit/queryCreditInfo")
    fun queryCreditInfo(@Body info: QueryReqBean): Observable<Payload<Any>>

    /**
     * 省市区选择
     */
    @POST("/api/Index/getCityArea")
    fun getCityArea(): Observable<Payload<List<CityArea>>>

    /**
     * 行业分类
     */
    @POST("/api/Index/industryCategory")
    fun industryCategory(): Observable<Payload<List<Industry>>>

    /**
     *获取个人简历所有信息
     */
    @FormUrlEncoded
    @POST("/api/Userfont/getPersonalResume")
    fun getPersonalResume(@Field("user_id") user_id: Int): Observable<Payload<Resume>>

    /**
     * 简历-添加教育经历
     */
    @POST("/api/Userfont/addExperience")
    fun addExperience(@Body reqBean: EducationReqBean): Observable<Payload<EducationBean>>

    /**
     * 简历-添加工作经历
     */
    @POST("/api/Userfont/addExperience")
    fun addWorkExperience(@Body reqBean: WorkReqBean): Observable<Payload<WorkEducationBean>>

    /**
     * 简历-修改个人简历基本信息
     */
    @POST("/api/Userfont/editResumeBaseInfo")
    fun editResumeBaseInfo(@Body ae: UserReq): Observable<Payload<Any>>


    /**
     * 简历-修改教育经历
     */
    @POST("/api/Userfont/editExperience")
    fun editExperience(@Body reqBean: EducationReqBean): Observable<Payload<EducationBean>>

    /**
     * 简历-修改工作经历
     */
    @POST("/api/Userfont/editExperience")
    fun editExperience(@Body reqBean: WorkReqBean): Observable<Payload<WorkEducationBean>>

    /**
     * 删除（教育|工作）经历
     */
    @FormUrlEncoded
    @POST("/api/UserFont/deleteExperience")
    fun deleteExperience(@Field("we_id") we_id: Int,//教育|工作ID（非空必传 )
                         @Field("type") type: Int //非空（1=>教育，2=>工作）
    ): Observable<Payload<Any>>

    /**
     * 获取日程/团队日程
     * stat:1=>日程，2=>团队日程,3=>项目事项
     * time: 2017-10-10形式
     * filter:stat=2时，多个uid以逗号隔开,例如’1，2‘
     */
    @FormUrlEncoded
    @POST("/api/Calendar/showSchedule")
    fun showSchedule(@Field("page") page: Int = 1,
                     @Field("pageSize") pageSize: Int = 20,
                     @Field("stat") stat: Int,
                     @Field("time") time: String,
                     @Field("filter") filter: String? = null): Observable<Payload<List<ScheduleBean>>>

    /**
     * 获取项目事项
     */
    @FormUrlEncoded
    @POST("/api/Calendar/showSchedule")
    fun ShowMatterSchedule(@Field("page") page: Int = 1,
                           @Field("pageSize") pageSize: Int = 20,
                           @Field("stat") stat: Int = 3,
                           @Field("time") time: String,
                           @Field("filter") filter: String? = null,
                           @Field("company_id") company_id: String? = null): Observable<Payload<List<ProjectMattersBean>>>

    /**
     * 获取任务列表
     */
    @FormUrlEncoded
    @POST("/api/Calendar/showTask")
    fun showTask(@Field("page") page: Int = 1,
                 @Field("pageSize") pageSize: Int = 20,
                 @Field("range") range: String,//时间区间 可空（’w'=>一周内，'m'=>一月内，’y‘=>一年内）
                 @Field("is_finish") is_finish: Int//是否完成  （1=>完成，0=>未完成）全部请传 2
    ): Observable<Payload<List<TaskItemBean>>>


    /**
     * 项目关键节点|项目代办|项目完成
     *
     */
    @FormUrlEncoded
    @POST("/api/Calendar/projectMatter")
    fun projectMatter(@Field("company_id") company_id: Int,
                      @Field("project_type") project_type: Int = 1//1=>项目关键节点,2=>项目完成，3=>项目代办
    ): Observable<Payload<List<KeyNode>>>


    /**
     * 项目代办|项目完成
     */
    @FormUrlEncoded
    @POST("/api/Calendar/projectMatter")
    fun projectMatter2(@Field("company_id") company_id: Int,
                       @Field("project_type") project_type: Int? = null//1=>项目关键节点,2=>项目完成，3=>项目代办
    ): Observable<Payload<List<MatterDetails>>>

    /**
     * 任务详情
     */
    @FormUrlEncoded
    @POST("/api/Calendar/showTaskInfo")
    fun showTaskDetail(@Field("data_id") data_id: Int): Observable<Payload<TaskDetailBean>>

    /**
     * 日程详情
     */
    @FormUrlEncoded
    @POST("/api/Calendar/showTaskInfo")
    fun showScheduleDetail(@Field("data_id") data_id: Int): Observable<Payload<ScheduleDetailsBean>>

    /**
     * 添加评论
     */
    @FormUrlEncoded
    @POST("/api/Calendar/addComment")
    fun addComment(@Field("data_id") data_id: Int,
                   @Field("content") content: String): Observable<Payload<TaskDetailBean.Record>>


    /**
     * 获取要修改的日程/任务数据
     */
    @FormUrlEncoded
    @POST("/api/Calendar/showEditTask")
    fun showEditTask(@Field("data_id") data_id: Int): Observable<Payload<ModifiedTaskBean>>

    /**
     * 提交修改
     */
    @POST("/api/Calendar/aeCalendarInfo")
    fun aeCalendarInfo(@Body reqBean: TaskModifyBean): Observable<Payload<Any>>

    @FormUrlEncoded
    @POST("/api/Calendar/deleteTask")
    fun deleteTask(@Field("data_id") data_id: Int): Observable<Payload<Any>>

    /**
     * 重大事件
     */
    @FormUrlEncoded
    @POST("api/Calendar/showGreatPoint")
    fun showGreatPoint(@Field("timer") timer: String): Observable<Payload<List<String>>>

    /**
     * 完成任务
     */
    @FormUrlEncoded
    @POST("/api/Calendar/finishTask")
    fun finishTask(@Field("rid") rid: Int): Observable<Payload<Int>>

    /**
     * 意见反馈
     */
    @FormUrlEncoded
    @POST("/api/Userfont/addFeedback")
    fun addFeedback(@Field("suggestion") suggestion: String,
                    @Field("contacter") contacter: String? = null,
                    @Field("contactWay") contactWay: String): Observable<Payload<Any>>

    /**
     * 获取个人项目归属信息
     */
    @FormUrlEncoded
    @POST("/api/UserFont/getBelongProject")
    fun getBelongProject(@Field("user_id") user_id: Int): Observable<Payload<BelongBean>>

    @FormUrlEncoded
    @POST("/api/News/singleCom")
    fun singleCompany(@Field("cId") cId: Int): Observable<Payload<ProjectBean>>

    companion object {
        const val APPKEY_NAME = "appkey"
        const val APPKEY_VALUE = "d5f17cafef0829b5"
    }

    // 投资项目管理-立项尽调数据
    @FormUrlEncoded
    @POST("/api/Listinformation/surveyData")
    fun surveyData(@Field("company_id") company_id: Int): Observable<Payload<SurveyDataBean>>

    // 添加或修改立项尽调数据
    @POST("/api/Listinformation/addEditSurveyData")
    fun addEditSurveyData(@Body map: HashMap<String, Any>): Observable<Payload<Object>>

    // 投资项目管理-投资决策数据
    @FormUrlEncoded
    @POST("/api/Listinformation/investSuggest")
    fun investSuggest(@Field("company_id") company_id: Int): Observable<Payload<InvestSuggestBean>>

    // 投资项目管理-添加或修改投资决策数据
    @POST("/api/Listinformation/addEditInvestSuggest")
    fun addEditInvestSuggest(@Body map: HashMap<String, Any>): Observable<Payload<Object>>

    // 投资项目管理-投后管理数据
    @FormUrlEncoded
    @POST("/api/Listinformation/manageData")
    fun manageData(@Field("company_id") company_id: Int): Observable<Payload<ManageDataBean>>

    // 投资项目管理-添加或修改投后管理数据
    @POST("/api/Listinformation/addEditManageData")
    fun addEditManageData(@Body map: HashMap<String, Any>): Observable<Payload<Object>>

    // 记录列表
    @FormUrlEncoded
    @POST("/api/Archives/recodeInfo")
    fun recodeInfo(@Field("company_id") company_id: Int): Observable<Payload<RecordInfoBean>>

    //跟踪记录-新增记录
    @POST("/api/Archives/addRecord")
    fun addRecord(@Body map: HashMap<String, Any>): Observable<Payload<Object>>

    //周报-本周周报
    @FormUrlEncoded
    @POST("/api/Weekly/index")
    fun getWeekly(@Field("user_id") user_id: Int? = null,
                  @Field("issue") issue: Int? = null,
                  @Field("start_time") start_time: String? = null,
                  @Field("end_time") end_time: String? = null,
                  @Field("week_id") week_id: Int? = null): Observable<Payload<WeeklyThisBean>>

    // 补充工作日程 新增和编辑都是这个接口
    @FormUrlEncoded
    @POST("/api/Weekly/addReport")
    fun addEditReport(@Field("start_time") start_time: String,
                      @Field("end_time") end_time: String,
                      @Field("content") content: String,
                      @Field("week_id") week_id: Int? = null): Observable<Payload<Object>>

    //周报-发送周报
    @FormUrlEncoded
    @POST("/api/Weekly/sendReport")
    fun sendReport(@Field("week_id") week_id: Int? = null,
                   @Field("accept_uid") accept_uid: String,
                   @Field("watch_uid") watch_uid: String? = null): Observable<Payload<Object>>

    //周报-我发出的
    @FormUrlEncoded
    @POST("/api/Weekly/send")
    fun send(@Field("page") page: Int = 1,
             @Field("pageSize") pageSize: Int = 5,
             @Field("start_time") start_time: String,
             @Field("end_time") end_time: String): Observable<Payload<ArrayList<WeeklySendBean>>>

    //待我查看的周报
    @FormUrlEncoded
    @POST("/api/Weekly/receive")
    fun receive(@Field("is_read") is_read: Int? = null,
                @Field("de_id") de_id: Int? = null,
                @Field("start_time") start_time: String? = null,
                @Field("end_time") end_time: String? = null,
                @Field("page") page: Int = 1,
                @Field("pageSize") pageSize: Int = 5): Observable<Payload<ArrayList<WeeklyWatchBean>>>


    @POST("/api/UserFont/getDepartment")
    fun getDepartment(): Observable<Payload<ArrayList<ReceiveSpinnerBean>>>


    //投资经理评价
    @FormUrlEncoded
    @POST("/api/Comment/assess")
    fun assess(@Field("company_id") company_id: Int,
               @Field("is_business") is_business: Int,
               @Field("is_ability") is_ability: Int): Observable<Payload<Object>>

    //进入考评系统判断角色
    @POST("/api/grade/getType")
    fun getType(): Observable<Payload<TypeBean>>

    //全员考评分数总览
    @POST("/api/grade/pointRank")
    fun pointRank(): Observable<Payload<ArrayList<ScoreBean>>>

    //(领导)年总考核中心   	1=>进入绩效考核列表页面，2=>进入岗位胜任力列表 3=>进入风控部填写页，4=>进入投资部填写页
    @FormUrlEncoded
    @POST("/api/grade/check")
    fun check(@Field("type") type: Int): Observable<Payload<GradeCheckBean>>

    //投资部填写页提交
    @POST("/api/grade/invest_add")
    fun invest_add(@Body map: HashMap<String, ArrayList<HashMap<String, String>>>): Observable<Payload<Object>>

    //关键绩效指标评价
    //user_id被打分人id
    @FormUrlEncoded
    @POST("/api/grade/perAppraisal")
    fun perAppraisal_TZ(@Field("user_id") user_id: Int, @Field("type") type: Int): Observable<Payload<ArrayList<InvestManageItem>>>

    //关键绩效指标评价----------风控部
    //user_id被打分人id
    @FormUrlEncoded
    @POST("/api/grade/perAppraisal")
    fun perAppraisal_FK(@Field("user_id") user_id: Int, @Field("type") type: Int): Observable<Payload<FKItem>>

    //关键绩效指标评价----------普通部门
    //user_id被打分人id
    @FormUrlEncoded
    @POST("/api/grade/perAppraisal")
    fun perAppraisal_NORMAL(@Field("user_id") user_id: Int, @Field("type") type: Int): Observable<Payload<NormalItemBean>>

    //提交关键绩效和岗位胜任力打分   type  1:提交关键绩效打分 2:提交岗位胜任力打分
    @POST("/api/grade/giveGrade")
    fun giveGrade(@Body map: HashMap<String, Any>): Observable<Payload<Any>>

    //风控部填写页提交
    @POST("/api/grade/risk_add")
    fun risk_add(@Body map: HashMap<String, ArrayList<HashMap<String, String>>>): Observable<Payload<Object>>

    //(领导)岗位胜任力互评详情页面|员工互评结果      可空 为空则是查看员工互评结果 传入user_id则是互评详情
    @FormUrlEncoded
    @POST("/api/grade/info")
    fun grade_info(@Field("user_id") user_id: Int? = null): Observable<Payload<ArrayList<EmployeeInteractBean>>>

    //(领导)关键绩效考核结果
    @POST("/api/grade/achievement")
    fun achievement(): Observable<Payload<ArrayList<EmployeeInteractBean.EmployeeItem>>>

    //岗位胜任力评价页面  普通员工
    @FormUrlEncoded
    @POST("/api/grade/each_comment")
    fun each_comment(@Field("user_id") user_id: Int): Observable<Payload<ArrayList<EmployeeInteractBean.EmployeeItem>>>

    //岗位胜任力评分|查看评分  可空（若传递则查看评分）
    @FormUrlEncoded
    @POST("/api/grade/showJobPage")
    fun showJobPage(@Field("user_id") user_id: Int? = null): Observable<Payload<JobPageBean>>

    //岗位胜任力评分|查看评分  可空（若传递则查看评分）  -1=>还未打完分 1=>尚未完成打分，2=>已完成
    @POST("/api/grade/showSumScore")
    fun showSumScore(): Observable<Payload<TotalScoreBean>>

    //填写页展示页
    @POST("/api/grade/showWrite")
    fun showWrite(): Observable<Payload<TemplateBean>>

    //填写页提交
    @POST("/api/grade/writeAdd")
    fun writeAdd(@Body map: HashMap<String, ArrayList<HashMap<String, String>>>): Observable<Payload<Object>>


    //关键绩效指标评价  ----非空（1=>绩效，3=>加减项）
    @FormUrlEncoded
    @POST("/api/grade/perAppraisal")
    fun perAppraisal(@Field("user_id") user_id: Int, @Field("type") type: Int): Observable<Payload<NormalItemBean>>

    //全员互评进度表
    @POST("/api/Grade/progress")
    fun GradeProgress(): Observable<Payload<ProgressBean>>

    // 项目退出输入
    @POST("/api/News/addQuit")
    fun addQuit(@Body map: HashMap<String, Any>): Observable<Payload<Any>>

    //历史退出记录
    @FormUrlEncoded
    @POST("/api/News/quitHistory")
    fun quitHistory(@Field("company_id") company_id: Int): Observable<Payload<ArrayList<QuitBean>>>

    //退出详情
    @FormUrlEncoded
    @POST("/api/News/quitInfo")
    fun quitInfo(@Field("id") id: Int): Observable<Payload<QuitInfoBean>>

    //加入项目群组,第一次调用是创建群组
    @FormUrlEncoded
    @POST("/api/News/createJoinGroup")
    fun createJoinGroup(@Field("accid")accid:String,@Field("company_id")company_id:String):Observable<Payload<Int>>


    fun chatFile(@Field("type")type:Int):Observable<Payload<List<ChatFileBean>>>

    //首页小红点
    @POST("/api/Index/getNumber")
    fun getNumber(): Observable<Payload<NumberBean>>
}