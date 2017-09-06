package com.sogukj.pe.bean

import java.io.Serializable

/**
 * Created by qinfei on 17/7/21.
 */
class ProjectBean : Serializable {
    var name: String? = null//	Varchar	公司名称
    var shortName: String? = null;
    var state: String? = null//	Datetime	现状	A轮  B轮  类似这些（type=2时取此数据）
    var update_time: String? = null//	Varchar	最近更新时间	（type=2时取此数据）
    var add_time: String? = null//	Varchar	录入时间	（type=1时取此数据）
    var status: Int = 1//	Int	状态（默认1）	0禁用 1准备中  2已完成（type=1时取此数据）
    var company_id: Int? = null
    var is_focus: Int = 0//	Int	是否关注	is_focus=1表示关注is_focus=0表示未关注
    //
    var legalPersonName: String? = null//	varchar		法人	可空
    var regLocation: String? = null//	varchar		注册地址	可空
    var creditCode: String? = null//	varchar		统一社会信用代码	可空
    var info: String? = null//	text		其他信息	可空
    var is_volatility = 0
}