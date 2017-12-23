package com.sogukj.pe.bean

/**
 * Created by sogubaby on 2017/12/21.
 */
class EmployeeInteractBean {
    var title: String? = null
    var per_grade: String? = null
    var data: ArrayList<EmployeeItem>? = null

    class EmployeeItem {
        var user_id: Int? = null
        var name: String? = null
        var grade_case: String? = null
        var sort: Int? = null
        var url: String? = null
        var department: String? = null
    }
}