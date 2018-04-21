package com.framework.base

import com.sogukj.pe.ui.project.ShareHolderStepActivity
import com.sogukj.pe.ui.project.ShareholderCreditActivity
import java.util.*

/**
 * Created by qinfei on 17/7/18.
 */

object ActivityHelper {
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

    fun removeStep1() {
        for (activity in activities) {
            if (activity is ShareHolderStepActivity) {
                activity.finish()
            }
        }
    }

    fun hasCreditListActivity(): Boolean {
        for (activity in activities) {
            if (activity is ShareholderCreditActivity) {
                return true
            }
        }
        return false
    }

    fun getActivityList(): ArrayList<BaseActivity> {
        return activities
    }
}
