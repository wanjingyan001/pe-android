package com.sogukj.pe.bean

import java.io.Serializable

/**
 * Created by qinfei on 17/7/19.
 */
class NewsBean : Serializable, NewsType {
    var title: String? = null
    var time: String? = null
    var source: String? = null
    var tag: String? = null
    var company_id: Int? = null
    var table_id: Int? = null
    var data_id: Int? = null
    var url: String? = null
    var imgUrl: String? = null
    var shareUrl: String = "http://pe.stockalert.cn"
    var shareTitle: String = "新闻舆情"
}
//[{
//    "title":"你好",
//    "time":"2017-07-26 12:01:32",
//    "source":"任命日报",
//    "tag":"娱乐",
//    "company_id":2
//    "table_id":1,
//    "data_id":1,
//}]