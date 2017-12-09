package com.sogukj.pe.bean

import java.io.Serializable
import java.util.*
import kotlin.collections.ArrayList

/**
 * Created by sogubaby on 2017/12/6.
 */
class WeeklyWatchBean {
    var date: String? = null
    var list: ArrayList<BeanObj> = ArrayList()

    class BeanObj : Serializable {
        var icon: Int = 0
        var name: String? = null
        var click: Boolean = false
    }

    fun clone(): WeeklyWatchBean {
        var cloObj = WeeklyWatchBean()
        cloObj.date = this.date
        cloObj.list = ArrayList<BeanObj>(this.list)
        return cloObj
    }
}