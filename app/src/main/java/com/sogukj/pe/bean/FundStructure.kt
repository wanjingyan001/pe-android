package com.sogukj.pe.bean

import java.io.Serializable

/**
 * Created by admin on 2017/11/23.
 */
class FundStructure : Serializable {
    var director: String = ""//董事
    var gd: ArrayList<String> = ArrayList()//股东
    var supervisor: String = ""//监事
    var total: String = ""//合计
    var bl: List<FundedRatio> = ArrayList()//各股东出资比例


    inner class FundedRatio {
        var partnerName: String = ""//股东名字
        var contribute: String = ""//实缴出资（万元）
        var investRate: String = ""//出资比例
    }
}