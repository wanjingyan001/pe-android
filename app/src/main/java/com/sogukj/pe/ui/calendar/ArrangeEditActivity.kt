package com.sogukj.pe.ui.calendar

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.databinding.DataBindingUtil.setContentView
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.view.MenuItem
import android.view.View
import com.amap.api.col.fa
import com.framework.base.ToolbarActivity
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.view.RecyclerAdapter
import com.sogukj.pe.view.RecyclerHolder
import kotlinx.android.synthetic.main.activity_arrange_edit.*

class ArrangeEditActivity : ToolbarActivity() {
    override val menuId: Int
        get() = R.menu.arrange_edit_save
    lateinit var editAdapter: RecyclerAdapter<Any>

    companion object {
        fun start(context: Activity) {
            val intent = Intent(context, ArrangeEditActivity::class.java)
            context.startActivityForResult(intent, Extras.REQUESTCODE)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_arrange_edit)
        title = "班子工作安排详情"
        setBack(true)
        editAdapter = RecyclerAdapter(this, { adapter, parent, type ->
            EditHolder(adapter.getView(R.layout.item_arrange_edit, parent))
        })
        arrangeEditList.layoutManager = LinearLayoutManager(this)
        arrangeEditList.adapter = editAdapter
        val inflate = layoutInflater.inflate(R.layout.layout_arrange_weekly_header, arrangeEditList, false)
        arrangeEditList.addHeaderView(inflate)
        for (i in 0..7) {
            editAdapter.dataList.add("")
        }
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.save_edit -> {
                TODO("调用接口保存数据")
            }
        }
        return super.onOptionsItemSelected(item)
    }

    inner class EditHolder(convertView: View) : RecyclerHolder<Any>(convertView) {
        override fun setData(view: View, data: Any, position: Int) {

        }
    }
}
