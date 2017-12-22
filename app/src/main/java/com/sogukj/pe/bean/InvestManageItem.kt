package com.sogukj.pe.bean

/**
 * Created by sogubaby on 2017/12/21.
 */
class InvestManageItem {
    var pName: String? = null
    var data: ArrayList<InvestManageInnerItem>? = null

    class InvestManageInnerItem {
        var id: Int? = null
        var name: String? = null
        var weight: Int? = null//14
        var total_score: Int? = null//140.0
        var target: String? = null
        var type: Int? = null //1=>关键绩效指标评价 2=>岗位胜任力评价 3=>加分项 4=>减分项
        var offset: Int? = null
        var info: String? = null
        var standard: String? = null
    }
}