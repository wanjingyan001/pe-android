package com.sogukj.pe.bean

import java.io.Serializable

/**
 * Created by qinfei on 17/8/16.
 */

class EquityBean : Serializable {
    var controllerName: String? = null//	Varchar	疑似控制人或公司名
    var percent: String? = null//	占股比例
    var companyName: String? = null//	公司名
}

class StructureBean : Serializable {
    var name: String? = null// 人或公司名
    var amount: String? = null// 认缴金额
    var percent: String? = null// 占股比例
    var sh_type: String? = null// 股东类型 工商股东／十大股东
    var children: List<StructureBean>? = null// 子节点
//    6	id；type； regCapital； parentName； actualHolding	Varchar	多余字段，无需考虑	这些字段是没有用的

}

class EquityStructureBean : Serializable {
    var info: EquityBean? = null
    var structure: List<StructureBean>? = null
}