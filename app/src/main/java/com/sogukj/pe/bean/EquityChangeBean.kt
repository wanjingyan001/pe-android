package com.sogukj.pe.bean

import java.io.Serializable

/**
 * Created by qinfei on 17/8/9.
 */
class EquityChangeBean : Serializable {
    var changeDate: Long? = null//	mediumint	变动日期	微秒
    var afterAll: String? = null//	Date	变动后流通A股	(万股)
    var afterNoLimit: String? = null//	Varchar	变动后流通A股	(万股)
    var afterLimit: String? = null//	Varchar	变动后限售A股	(万股)
    var changeReason: String? = null//	Varchar	变动原因
}