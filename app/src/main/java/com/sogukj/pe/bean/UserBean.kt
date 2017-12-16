package com.sogukj.pe.bean

import java.io.File
import java.io.Serializable

open class UserBean() : Serializable {
    var uid: Int? = null//用户主键ID
    var name: String = ""
    var phone: String = ""
    var depart_name: String = ""
    var position: String = ""
    var email: String = ""
    var project: String = ""
    var memo: String = ""
    var url: String? = null
    var depart_id: Int? = null
    var numberOfShares: Int = 0
    var is_admin: Int = 0
    var user_id: Int? = null
    var full: String? = null


    fun headImage(): String? {
        if (null == url) return null
        val file = File(url)
        if (file.exists()) return url
        return "${url}"
    }
}
