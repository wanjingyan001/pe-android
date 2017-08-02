package com.sogukj.pe.bean

import java.io.Serializable

/**
 * Created by qinfei on 17/7/21.
 */
class ProjectBean : Serializable {
    var name: String? = null//	Varchar	公司名称
    var state: String? = null//	Datetime	现状	A轮  B轮  类似这些（type=2时取此数据）
    var update_time: String? = null//	Varchar	最近更新时间	（type=2时取此数据）
    var add_time: String? = null//	Varchar	录入时间	（type=1时取此数据）
    var status: String? = null//	Int	状态（默认1）	0禁用 1准备中  2已完成（type=1时取此数据）

}