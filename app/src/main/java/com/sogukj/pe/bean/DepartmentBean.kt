package com.sogukj.pe.bean

import java.io.Serializable

/**
 * Created by qinfei on 17/7/31.
 */
class DepartmentBean : Serializable {
    var de_name: String = ""
    var depart_id: Int? = null
    var data: List<UserBean>? = null
}