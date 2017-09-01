package com.sogukj.pe.bean

import java.io.Serializable

/**
 * Created by qinfei on 17/8/25.
 */
class ProjectDetailBean : Serializable {
    var yuQing: List<NewsBean>? = null
    var fuMian: List<NewsBean>? = null
    var counts: Map<String, Int>? = null
}

//class ProjectCountBean : Serializable {
//    var lawSuits: Int = 0//	法律诉讼
//    var courtAnnouncement: Int = 0//    法院公告
//    var dishonest: Int = 0//   失信人
//    var zhixingInfo: Int = 0//  被执行人
//    var punishmentInfo: Int = 0//   行政处罚
//    var illegalInfo: Int = 0//   严重违法
//    var equityInfo: Int = 0//   股权出质
//    var mortgageInfo: Int = 0//  动产抵押
//    var ownTax: Int = 0//  欠税公告
//    var abnormal: Int = 0//   经营异常
//    var qcCourtAnnouncement: Int = 0//   开庭公告
//    var judicialAuction: Int = 0//    司法拍卖
//    var news: Int = 0//   新闻舆情
//    var volatility: Int = 0// 股票行情
//    var companyInfo: Int = 0//   企业简介
//    var seniorExecutive: Int = 0//   高管信息
//    var holdingCompany: Int = 0//  参股控股
//    var announcement: Int = 0// 上市公告
//    var shareholder: Int = 0// 十大股东
//    var shareholder2: Int = 0//  十大流通股东
//    var issueRelated: Int = 0//  发行相关
//    var shareStructure: Int = 0//   股本结构
//    var equityChange: Int = 0//  股本变动
//    var bonusInfo: Int = 0//  分红情况
//    var allotmen: Int = 0// 配股情况
//    var sgcompanyinfo: Int = 0//  工商信息
//    var holder: Int = 0//  股东信息
//    var holder2: Int = 0//   股权结构
//    var staff: Int = 0// 主要人员
//    var inverst: Int = 0// 对外投资
//    var changeinfo: Int = 0//  变更记录
//    var annualreport: Int = 0//  企业年报
//    var branch: Int = 0//  分支机构
//    var sgcompanyinfo2: Int = 0//   公司简介
//    var findHistoryRongzi: Int = 0//  融资历史
//    var findTzanli: Int = 0//    投资事件
//    var findTeamMember: Int = 0//   核心团队
//    var getProductInfo: Int = 0//   企业业务
//    var findJingpin: Int = 0//  竞品信息
//    var employments: Int = 0//  招聘信息
//    var bond: Int = 0//  债券信息
//    var taxCredit: Int = 0//  税务评级
//    var purchaseLand: Int = 0//  购地信息
//    var bids: Int = 0// 招投标
//    var qualification: Int = 0//  资质证书
//    var checkInfo: Int = 0//  抽查检查
//    var appbkInfo: Int = 0//   产品信息
//    var tm: Int = 0//  商标信息
//    var patents: Int = 0//    专利信息
//    var copyReg: Int = 0//  软著权
//    var icp: Int = 0//  网站备案
//    var copyReg2: Int = 0//   著作权
//}