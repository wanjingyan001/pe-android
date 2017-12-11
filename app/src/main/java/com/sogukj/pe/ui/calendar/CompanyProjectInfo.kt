package com.sogukj.pe.ui.calendar

import java.io.Serializable

/**
 * Created by admin on 2017/12/9.
 */
class KeyNode : Serializable {
    var end_time: String? = null//结束时间
    var title: String? = null//标题
    var type: Int? = 0//类别
    var data_id: Int? = 0//
}


class MatterDetails : Serializable {
    var year: String = ""
    var data = ArrayList<KeyNode>()
}

