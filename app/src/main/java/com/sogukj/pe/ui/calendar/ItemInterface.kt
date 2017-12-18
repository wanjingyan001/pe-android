package com.sogukj.pe.ui.calendar

import android.view.View
import com.ldf.calendar.model.CalendarDate

/**
 * Created by admin on 2017/12/9.
 */
interface MonthSelectListener {
    fun onMonthSelect(date: CalendarDate)
}

interface ScheduleItemClickListener {
    fun onItemClick(view: View, position: Int)
    fun finishCheck( isChecked: Boolean, position: Int)
}

interface CommentListener {
    fun confirmListener(comment: String)
}

 interface AddPersonListener {
    fun addPerson(tag:String)
}