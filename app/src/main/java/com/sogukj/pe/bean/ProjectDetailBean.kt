package com.sogukj.pe.bean

import java.io.Serializable

/**
 * Created by qinfei on 17/8/25.
 */
class ProjectDetailBean : Serializable {
    var yuQing: List<NewsBean>? = null
    var fuMian: List<NewsBean>? = null
    var counts: Map<String, Int>? = null
}