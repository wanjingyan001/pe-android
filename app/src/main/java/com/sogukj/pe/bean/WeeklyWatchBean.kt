package com.sogukj.pe.bean

/**
 * Created by sogubaby on 2017/12/6.
 */
class WeeklyWatchBean {
    var date: String? = null
    val list: ArrayList<BeanObj> = ArrayList()

    class BeanObj {
        var icon: Int = 0
        var name: String? = null
        var click: Boolean = false
    }
}