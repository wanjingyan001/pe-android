package com.sogukj.pe.bean

import java.io.Serializable

/**
 * Created by admin on 2017/11/23.
 */
class FundDetail : Serializable {
    var id: Int = 0
    var simpleName: String = ""//基金公司名(简称)
    var regTime: String = ""//成立时间
    var contributeSize: String = ""//认缴规模（万元）
    var actualSize: String = ""//实缴规模（万元
    var duration: String = ""//存续期限
    var partners: String = ""//合伙人人数
    var mode: String = ""//GP模式
    var commission: String = ""//投委会通过方
    var manageFees: String = ""//管理费
    var carry: String = ""//carry分成
    var administrator = ""//管理人
    var list: Collection<NameList>? = null

    inner class NameList {
        var name: String = ""
        var url: String = ""
    }
}