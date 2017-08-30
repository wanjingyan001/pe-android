package com.sogukj.pe.bean

import java.io.File
import java.io.Serializable

open class UserBean() : Serializable {
    var uid: Int? = null//用户主键ID
    var name: String = ""
    var phone: String? = null
    var depart_name: String? = null
    var position: String = ""
    var email: String = ""
    var project: String? = null
    var memo: String? = null
    var url: String? = null
    var depart_id: Int? = null
    var numberOfShares: Int = 0

    fun headImage(): String? {
        if (null == url) return null
        val file = File(url)
        if (file.exists()) return url
        return "${url}"
    }
}
