package com.sogukj.pe.bean

/**
 * Created by sogubaby on 2017/12/6.
 */
class TimeItem(year: Int, month: Int, day: Int) {
    var year = year
    var month = month
    var day = day

    /**
     * a.compare(b)
     * 返回1表示a大于b，返回-1表示a小于b，返回0表示a等于b
     */
    fun compare(item: TimeItem): Int {
        if (this.year == item.year) {
            if (this.month == item.month) {
                if (this.day == item.day) {
                    return 0
                } else if (this.day > item.day) {
                    return 1
                }
            } else if (this.month > item.month) {
                return 1
            }
        } else if (this.year > item.year) {
            return 1
        }
        return -1
    }
}