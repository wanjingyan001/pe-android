package com.sogukj.pe.util

import android.graphics.Color
import android.widget.TextView
import com.sogukj.pe.bean.ApprovalBean
import com.sogukj.pe.bean.MessageBean
import org.jetbrains.anko.textColor

/**
 * Created by qinfei on 17/11/7.
 */
object ColorUtil {
    fun setColorStatus(view: TextView, bean: MessageBean) {
//        -1 -> "审批未通过"
//        1 -> "待审批"
//        2 -> "已审批"
//        4 -> "审批通过"
        view.text = bean.status_str
        view.textColor = when (bean.status) {
            4 -> Color.parseColor("#50d59d")
            2 -> Color.parseColor("#a0a4aa")
            1 -> Color.parseColor("#ffa715")
            -1 -> Color.parseColor("#ff5858")
            else -> Color.parseColor("#ffa715")
        }
    }

    fun setColorStatus(view: TextView, bean: ApprovalBean) {
        view.text = bean.status_str
        view.textColor = when (bean.status_str) {
            "完成用印" -> Color.parseColor("#A0A4AA")
            "签发完成" -> Color.parseColor("#A0A4AA")
            "签发中" -> Color.parseColor("#806af2")
            "待用印" -> Color.parseColor("#806af2")
            "审批完成" -> Color.parseColor("#50d59d")
            "审批通过" -> Color.parseColor("#50d59d")
            "审批未通过" -> Color.parseColor("#ff5858")
            "签字中" -> Color.parseColor("#4aaaf4")
            "审批中" -> Color.parseColor("#4aaaf4")
            "待签字" -> Color.parseColor("#ffa715")
            "待审批" -> Color.parseColor("#ffa715")
            else -> Color.parseColor("#ffa715")
        }
    }
}