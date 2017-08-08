package com.sogukj.pe.bean

import java.io.Serializable

/**
 * Created by qinfei on 17/8/8.
 */
class AnnouncementBean : Serializable {
    var time: String? = null//	Date	发布日期
    var companyName: String? = null//		Varchar	公司
    var name: String? = null//		Varchar	股票名
}