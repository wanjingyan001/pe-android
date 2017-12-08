package com.sogukj.pe.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import android.widget.TextView
import com.sogukj.pe.R
import kotlinx.android.synthetic.main.weeklylabel.view.*

/**
 * Created by sogubaby on 2017/12/2.
 */
class WeeklyLabel : LinearLayout {
    constructor(context: Context) : super(context) {
        init(context)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(context)
    }

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle) {
        init(context)
    }

    fun init(context: Context) {
        LayoutInflater.from(context).inflate(R.layout.weeklylabel, this)
    }

    fun setData(seq: String, week: String, date: String) {
        tv_index.text = seq
        tv_week.text = week
        tv_date.text = date
    }
}