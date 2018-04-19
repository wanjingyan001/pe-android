package com.sogukj.pe.bean

import org.json.JSONArray
import java.io.Serializable

/**
 * Created by admin on 2017/11/30.
 */
class SensitiveInfo : Serializable {
    var bankRisk: BankRisk? = null//银行风险用户
    var dishonest: Dishonest? = null//法院失信名单
    var crime: Crime? = null//危险身份
    var court: Court? = null//涉诉记录


    inner class BankRisk : Serializable {
        //1=>是，0=>否
        var bank_bad = 0//低风险
        var bank_overdue = 0//一般风险
        var bank_lost = 0//高风险
        var bank_fraud = 0//资信不佳
        var bank_refuse = 0//拒绝
    }

    inner class Dishonest : Serializable {
        var num = 0//数量
        var item = ArrayList<Item>()//详情

        inner class Item : Serializable {
            var time = ""//立案日期
            var casenum = ""//案号
            var court = ""//执行法院
            var performance = ""//履行状态
            var base = ""//执行依据号
        }
    }

    inner class Crime : Serializable {
        var checkCode: String? = null//类型 可能同时命中两个  (0=>未比中； 1=>比中在逃； 2=>比中前科； 3=>比中涉毒； 4=>比中吸毒)   返回格式   "1,2"
        var num: String? = null//数量
        var caseSource: String? = null//案件来源
        var caseTime: String? = null//案发时间区间
//        var item = ArrayList<Item>()//案件详情
//
//        inner class Item : Serializable {
//            var caseSource = ""//案件来源
//            var caseTime = ""//案发时间区间
//            var caseType = ""//案件类别
//            var caseLevel = ""//案件级别
//        }
    }

    inner class Court : Serializable {
        var num = 0//数量
        var item = Item()//详情

        inner class Item : Serializable {
            var cpws = 0//裁判文书
            var zxgg = 0//执行公告
            var ktgg = 0//开庭公告
            var fygg = 0//法院公告
        }
    }

}