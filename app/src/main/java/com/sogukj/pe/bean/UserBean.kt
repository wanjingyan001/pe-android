package com.sogukj.pe.bean

import java.io.Serializable

class UserBean() : Serializable {
    var uid: String? = null//用户主键ID
    var name: String? = null
    var phone: String? = null
    var depart_name: String? = null
    var position: String? = null
    var email: String? = null
    var project: String? = null
    var memo: String? = null
    var url: String? = null
}
