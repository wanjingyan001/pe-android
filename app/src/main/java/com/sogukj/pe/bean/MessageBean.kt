package com.sogukj.pe.bean

import android.graphics.Color
import android.widget.TextView
import org.jetbrains.anko.textColor
import java.io.Serializable

/**
 * Created by qinfei on 17/10/30.
 */
class MessageBean : Serializable {
    var news_id: Int? = null//	number	消息总数
    var approval_id: Int? = null//	number	审批id
    var time: String? = null//   string    时间
    var title: String? = null//	string	审批标题
    var username: String? = null//	string	申请人
    var type: Int? = null//	number	类别	1=>出勤休假, 2=>用印审批 ,3=>签字审批
    var status: Int? = null//	number	审批状态	1待审批，2 已审批
    var reasons: String? = null//	string	审批说明
    var urgent_count: Int? = null//	number	加急次数
    var message_count: Int? = null//	number	留言数量
    var type_name: String? = null//	string	用印类别

    fun setColorStatus(view: TextView) {
        val status_str = when (status) {
            -1 -> "审批未通过"
            1 -> "待审批"
            0 -> "审批中"
            2 -> "已审批"
            4 -> "审批通过"
            else -> "待审批"
        }
        view.text = status_str
        view.textColor = when (status_str) {
            "完成用印" -> Color.parseColor("#fa34ca")
            "签发完成" -> Color.parseColor("#fa34ca")
            "签发中" -> Color.parseColor("#806af2")
            "待用印" -> Color.parseColor("#806af2")
            "审批完成" -> Color.parseColor("#50d59d")
            "审批未通过" -> Color.parseColor("#ff5858")
            "签字中" -> Color.parseColor("#4aaaf4")
            "审批中" -> Color.parseColor("#4aaaf4")
            "待签字" -> Color.parseColor("#ffa715")
            "待审批" -> Color.parseColor("#ffa715")
            else -> Color.parseColor("#ffa715")
        }
    }
}