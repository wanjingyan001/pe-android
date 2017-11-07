package com.sogukj.pe.util

import android.graphics.Color
import android.widget.TextView
import com.sogukj.pe.bean.MessageBean
import org.jetbrains.anko.textColor

/**
 * Created by qinfei on 17/11/7.
 */
object ColorUtil {
    fun setColorMessage(view: TextView,bean:MessageBean) {
//        -1 -> "审批未通过"
//        1 -> "待审批"
//        2 -> "已审批"
//        4 -> "审批通过"
        view.text = bean.status_str
        view.textColor = when (bean.status) {
            4 -> Color.parseColor("#fa34ca")
            2 -> Color.parseColor("#a0a4aa")
            1 -> Color.parseColor("#ffa715")
            -1 -> Color.parseColor("#ff5858")
            else -> Color.parseColor("#ffa715")
        }

    }
}