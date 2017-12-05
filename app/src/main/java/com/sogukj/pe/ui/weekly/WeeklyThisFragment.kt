package com.sogukj.pe.ui.weekly

import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import com.framework.base.BaseFragment

import com.sogukj.pe.R
import kotlinx.android.synthetic.main.fragment_weekly_this.*

/**
 * A simple [Fragment] subclass.
 */
class WeeklyThisFragment : BaseFragment() {

    override val containerViewId: Int
        get() = R.layout.fragment_weekly_this

    lateinit var inflate: LayoutInflater

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        inflate = LayoutInflater.from(context)
        initView()
    }

    fun initView() {
        val item = inflate.inflate(R.layout.weekly_item, null) as LinearLayout

        val ll_event = inflate.inflate(R.layout.weekly_event, null) as LinearLayout
        val ll_leave = inflate.inflate(R.layout.weekly_leave, null) as LinearLayout

        item.addView(ll_event)
        item.addView(ll_leave)

        root.addView(item)

        val item1 = inflate.inflate(R.layout.weekly_item, null) as LinearLayout

        val ll_event1 = inflate.inflate(R.layout.weekly_event, null) as LinearLayout
        val ll_leave1 = inflate.inflate(R.layout.weekly_leave, null) as LinearLayout

        item1.addView(ll_event1)
        item1.addView(ll_leave1)

        root.addView(item1)

        //showToast("${root.childCount}")

        val empty = inflate.inflate(R.layout.buchong_empty, null) as LinearLayout
        val iv_emp = empty.findViewById(R.id.empty) as ImageView
        iv_emp.setOnClickListener {
            WeeklyRecordActivity.startAdd(activity)
        }
        root.addView(empty)

        val full = inflate.inflate(R.layout.buchong_full, null) as LinearLayout
        val iv_edit = full.findViewById(R.id.edit) as ImageView
        iv_edit.setOnClickListener {
            WeeklyRecordActivity.startEdit(activity)
        }
        root.addView(full)

        val send = inflate.inflate(R.layout.send, null) as LinearLayout
        root.addView(send)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 0x001) {//ADD

        } else if (requestCode == 0x002) {//EDIT

        }
    }
}
