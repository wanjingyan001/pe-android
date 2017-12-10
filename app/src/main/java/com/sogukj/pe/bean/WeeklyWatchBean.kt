package com.sogukj.pe.bean

import java.io.Serializable
import java.util.*
import kotlin.collections.ArrayList

/**
 * Created by sogubaby on 2017/12/6.
 */
class WeeklyWatchBean {
    var date: String? = null//10.06-10.13
    var s_time: String? = null//2017-10-06
    var e_time: String? = null//2017-10-13
    var data: ArrayList<BeanObj> = ArrayList()

    class BeanObj : Serializable {
        var img_url: String? = null
        var name: String? = null
        var user_id: Int? = null
        var is_read: Int? = null//1=>已读，0=>未读,null=>全部
        var week_id: Int? = null
    }

    fun clone(): WeeklyWatchBean {
        var cloObj = WeeklyWatchBean()
        cloObj.date = this.date
        cloObj.data = ArrayList<BeanObj>(this.data)
        return cloObj
    }
}