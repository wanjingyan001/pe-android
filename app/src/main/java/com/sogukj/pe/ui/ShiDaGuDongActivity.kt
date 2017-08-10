package com.sogukj.pe.ui

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import com.framework.adapter.ListAdapter
import com.framework.adapter.ListHolder
import com.framework.base.ToolbarActivity
import com.framework.util.Trace
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.bean.ProjectBean
import com.sogukj.pe.bean.ShareHolderBean
import com.sogukj.pe.bean.TimeGroupedShareHolderBean
import com.sogukj.service.SoguApi
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_project_share_holder.*

class ShiDaGuDongActivity : ToolbarActivity() {

    val adapter = ListAdapter { LocalHolder() }
    val adapterSelector = ListAdapter {
        object : ListHolder<TimeGroupedShareHolderBean> {
            lateinit var text: TextView
            override fun createView(inflater: LayoutInflater): View {
                text = inflater.inflate(R.layout.item_textview_selector, null) as TextView
                return text
            }

            override fun showData(convertView: View, position: Int, data: TimeGroupedShareHolderBean?) {
                text.text = data?.time
                if (position != selectedIndex)
                    text.setTextColor(R.color.colorBlue)
                else
                    text.setTextColor(R.color.text_2)
            }

        }
    }
    lateinit var project: ProjectBean
    var selectedIndex = -1;
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_project_share_holder)

        project = intent.getSerializableExtra(Extras.DATA) as ProjectBean
        setBack(true)
        setTitle("十大股东")

        lv_dropdown.adapter = adapterSelector
        list_view.adapter = adapter

        lv_dropdown.setOnItemClickListener { parent, view, position, id ->
            val group = adapterSelector.dataList[position]
            group?.apply { setGroup(position, this) }
            lv_dropdown.visibility = View.GONE
        }

        tv_select.setOnClickListener {
            lv_dropdown.visibility = when (lv_dropdown.visibility) {
                View.VISIBLE -> View.GONE
                else -> View.VISIBLE
            }
        }

        handler.postDelayed({
            doRequest()
        }, 100)
    }

    fun setGroup(index: Int = 0, group: TimeGroupedShareHolderBean) {
        selectedIndex = index
        group.apply {
            isSelected = true
            tv_select.text = time
            tv_tenTotal.text = tenTotal
            tv_tenPercent.text = tenPercent
            tv_holdingChange.text = holdingChange
            adapterSelector.notifyDataSetChanged()
            data?.apply {
                adapter.dataList.clear()
                adapter.dataList.addAll(this)
                adapter.notifyDataSetChanged()
            }
        }
    }

    fun doRequest() {
        SoguApi.getService(application)
                .shareHolder(project.company_id!!)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ payload ->
                    adapter.dataList.clear()
                    if (payload.isOk) {
                        payload.payload?.apply {
                            adapterSelector.dataList.clear()
                            adapterSelector.dataList.addAll(this)
                        }
                    } else
                        showToast(payload.message)

                    adapterSelector.dataList.firstOrNull()
                            ?.apply {
                                setGroup(0, this)
                            }
                    adapter.notifyDataSetChanged()
                }, { e ->
                    Trace.e(e)
                })
    }


    class LocalHolder : ListHolder<ShareHolderBean> {

        lateinit var tvName: TextView
        lateinit var tvProportion: TextView
        lateinit var tvHoldingNum: TextView
        lateinit var tvShareType: TextView
        override fun createView(inflater: LayoutInflater): View {
            val view = inflater.inflate(R.layout.item_project_share_holder, null)

            tvName = view.findViewById(R.id.tv_name) as TextView
            tvProportion = view.findViewById(R.id.tv_proportion) as TextView
            tvHoldingNum = view.findViewById(R.id.tv_holdingNum) as TextView
            tvShareType = view.findViewById(R.id.tv_shareType) as TextView
            return view
        }

        override fun showData(convertView: View, position: Int, data: ShareHolderBean?) {
            tvName.text = data?.name
            tvProportion.text = data?.proportion
            tvHoldingNum.text = data?.holdingNum
            tvShareType.text = data?.shareType
        }

    }

    companion object {
        fun start(ctx: Activity?, project: ProjectBean) {
            val intent = Intent(ctx, ShiDaGuDongActivity::class.java)
            intent.putExtra(Extras.DATA, project)
            ctx?.startActivity(intent)
        }
    }
}
