package com.framework.base

import java.util.*


object ActivityManager {
    private val activities = ArrayList<BaseActivity>()
    var curActivity: BaseActivity? = null

    val rootActivity: BaseActivity
        get() = activities[0]

    val count: Int
        get() = activities.size

    fun add(activity: BaseActivity) {
        activities.add(activity)
        curActivity = activity
    }

    fun remove(activity: BaseActivity) {
        activities.remove(activity)
    }

    //退出整个应用
    fun exit() {
        for (activity in activities) {
            activity.finish()
        }
    }
}
