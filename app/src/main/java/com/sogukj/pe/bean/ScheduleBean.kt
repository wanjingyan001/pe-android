package com.sogukj.pe.bean

import java.io.Serializable

/**
 * Created by admin on 2017/12/5.
 */
class ScheduleBean : Serializable {
    var id: Int? = null//日程ID
    var title: String? = null//标题
    var start_time: String? = null//开始时间
    var end_time: String? = null//截止时间
    var name: String? = null//姓名  当stat=2返回
    var type: Int? = null//类别
    var data_id: Int? = null//对应表ID
    var timing: String? = null//时间  当stat=1或2时返回时间段，当stat=3时返回时间点
    var is_finish: Int? = null//是否完成  （0=>未完成，1=>完成）仅当stat=3时返回
    var company_id: Int? = null//公司id  仅当stat=3时返回

}