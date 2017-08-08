package com.sogukj.pe.bean

import java.io.Serializable

/**
 * Created by qinfei on 17/8/8.
 */
class CanGuBean:Serializable {
   var name:String?=null//	Varchar	公司名
   var relationship:String?=null//	Varchar	参股关系
   var participationRatio:Float?=null//	Float	参股比例（%）
   var investmentAmount:String?=null//	Mediumint	投资金额（万元）
   var profit:String?=null//	Varchar	被参股公司 净利润(元)
   var reportMerge:String?=null//	Enum	是否报表合并
   var mainBusiness:String?=null//	Text	被参股公司主营业务
}