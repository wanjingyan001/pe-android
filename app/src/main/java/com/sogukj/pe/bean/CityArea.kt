package com.sogukj.pe.bean

import java.io.Serializable

/**
 * Created by admin on 2017/12/2.
 */
class CityArea : Serializable {
    var id: Int? = null//id
    var name: String? = null//省
    var pid: Int? = null//父id
    var seclected = false
    var city: ArrayList<City>? = null//市相关信息

    inner class City : Serializable  {
        var id: Int? = null//id
        var name: String? = null//省
        var pid: Int? = null//父id
        var seclected = false
    }
}