package com.sogukj.pe.ui.approve

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.framework.base.ToolbarActivity
import com.sogukj.pe.R
import com.sogukj.pe.bean.VacationBean
import com.sogukj.pe.util.ColorUtil
import com.sogukj.pe.util.Trace
import com.sogukj.pe.util.Utils
import com.sogukj.pe.view.RecyclerAdapter
import com.sogukj.pe.view.RecyclerHolder
import com.sogukj.pe.view.SpaceItemDecoration
import com.sogukj.service.SoguApi
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_vacation_record.*

class VacationRecordActivity : ToolbarActivity() {

    lateinit var adapter: RecyclerAdapter<VacationBean>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_vacation_record)
        title = "记录"
        setBack(true)

        //qj_record_item
        adapter = RecyclerAdapter(context, { _adapter, parent, type ->
            val convertView = _adapter.getView(R.layout.item_vacation, parent)
            object : RecyclerHolder<VacationBean>(convertView) {
                val image = convertView.findViewById(R.id.color) as ImageView
                val vac_type = convertView.findViewById(R.id.type) as TextView
                val shengyu = convertView.findViewById(R.id.shengyu) as TextView
                override fun setData(view: View, data: VacationBean, position: Int) {
                    vac_type.text = data.name
                    shengyu.text = "剩余${data.hours}小时"
                    ColorUtil.setColorStatus(image, data)
                }
            }
        })
        recycler_view.layoutManager = LinearLayoutManager(context)
        recycler_view.addItemDecoration(SpaceItemDecoration(Utils.dpToPx(context, 10)))
        recycler_view.adapter = adapter

        SoguApi.getService(application)
                .showVacation()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ payload ->
                    if (payload.isOk) {
                        adapter.dataList.clear()
                        adapter.dataList.addAll(payload.payload!!)
                        adapter.notifyDataSetChanged()
                    } else {
                        showToast(payload.message)
                        empty.visibility = View.VISIBLE
                    }
                }, { e ->
                    Trace.e(e)
                    empty.visibility = View.VISIBLE
                })
    }

    companion object {
        fun start(ctx: Activity?) {
            val intent = Intent(ctx, VacationRecordActivity::class.java)
            ctx?.startActivity(intent)
        }
    }
}
