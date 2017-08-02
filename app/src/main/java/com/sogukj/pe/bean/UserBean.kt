package com.sogukj.pe.bean

import java.io.File
import java.io.Serializable

class UserBean() : Serializable {
    var uid: Int? = null//用户主键ID
    var name: String? = null
    var phone: String? = null
    var depart_name: String? = null
    var position: String? = null
    var email: String? = null
    var project: String? = null
    var memo: String? = null
    var url: String? = null
    var depart_id: Int? = null

    fun headImage(): String? {
        if (null == url) return null
        val file = File(url)
        if (file.exists()) return url
        return "${url}"
    }
}
