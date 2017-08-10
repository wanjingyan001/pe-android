package com.sogukj.pe.bean

import java.io.Serializable

/**
 * Created by qinfei on 17/8/10.
 */
class ShareHolderBean : Serializable {
    var name: String? = null//	Date	公司名
    var holdingNum: String? = null//	Varchar	持股数量
    var proportion: String? = null//	Varchar	占股本比例（%）
    var shareType: String? = null//	Varchar	股票类型
    var tenTotal: String? = null//	Varchar	前十大股东累计持有
    var tenPercent: String? = null//	Varchar	累计占总股本比
    var holdingChange: String? = null//	Varchar	较上期变化
    var time: String? = null//	date	时间点
}