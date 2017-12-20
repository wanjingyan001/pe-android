package com.sogukj.pe.bean

/**
 * Created by sogubaby on 2017/12/20.
 */
class GradeCheckBean {
    var type: Int? = null//公有
    var finish_grade: ArrayList<ScoreBean>? = null//打分界面
    var ready_grade: ArrayList<ScoreBean>? = null//打分界面
    var data: ArrayList<FengKongItem>? = null//风控填写页面

    class ScoreBean {
        var name: String? = null
        var department: String? = null
        var plan: String? = null
        var date_grade: String? = null
    }

    class FengKongItem {
        var performance_id: Int? = null
        var zhibiao: String? = null
    }
}