package com.sogukj.pe.bean

/**
 * Created by sogubaby on 2017/12/20.
 */
class TypeBean {
    var type: Int? = null//0=>未开启  1=>管理层，2=>普通员工，3=>普通员工风控部，4=>普通员工投资部
    var is_see: Int? = null//领导是否可以查看	0=>不能查看，1=>可以
    var rule_url: String? = null
}