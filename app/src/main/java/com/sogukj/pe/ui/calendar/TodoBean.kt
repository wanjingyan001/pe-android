package com.sogukj.pe.ui.calendar

import java.io.Serializable

/**
 * Created by admin on 2017/12/7.
 */
class TodoYear(val year: String) : Serializable


class TodoDay(val dayTime: String) : Serializable

class TodoInfo(val time: String, val content: String) : Serializable

class CompleteInfo(val info: String, val date: String) : Serializable


/**
 *任务列表item
 */
class TaskItemBean(var id: Int,//日程ID
                   var title: String,//标题
                   var end_time: String,//截止时间
                   var is_finish: Int,//是否完成（1=>完成，0=>未完成）
                   var leader: String?,//董事长安排  非董事长 此字段不传
                   var data_id: Int//任务ID
                    ) : Serializable