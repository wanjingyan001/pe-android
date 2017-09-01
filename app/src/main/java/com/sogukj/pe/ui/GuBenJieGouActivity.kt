package com.sogukj.pe.ui

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import com.framework.base.ToolbarActivity
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.adapter.ListAdapter
import com.sogukj.pe.adapter.ListHolder
import com.sogukj.pe.bean.ProjectBean
import com.sogukj.pe.bean.TimeGroupedCapitalStructureBean
import com.sogukj.pe.util.Trace
import com.sogukj.service.SoguApi
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_project_gubenjiegou.*

class GuBenJieGouActivity : ToolbarActivity() {

    val type: Int
        get() = 3

    val adapterSelector = ListAdapter {
        object : ListHolder<TimeGroupedCapitalStructureBean> {
            lateinit var text: TextView
            override fun createView(inflater: LayoutInflater): View {
                text = inflater.inflate(R.layout.item_textview_selector, null) as TextView
                return text
            }

            override fun showData(convertView: View, position: Int, data: TimeGroupedCapitalStructureBean?) {
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
        setContentView(R.layout.activity_project_gubenjiegou)

        project = intent.getSerializableExtra(Extras.DATA) as ProjectBean
        setBack(true)
        setTitle("股本结构")

        lv_dropdown.adapter = adapterSelector

        lv_dropdown.setOnItemClickListener { parent, view, position, id ->
            val group = adapterSelector.dataList[position]
            group?.apply { setGroup(position) }
            tv_select.isChecked = false
        }
        tv_select.setOnCheckedChangeListener { buttonView, isChecked ->
            lv_dropdown.visibility = when (isChecked) {
                true -> View.VISIBLE
                else -> View.GONE
            }
        }
        tv_previous.setOnClickListener {
            setGroup(selectedIndex - 1)
        }
        tv_next.setOnClickListener {
            setGroup(selectedIndex + 1)
        }

        handler.postDelayed({
            doRequest()
        }, 100)
    }

    fun setGroup(index: Int = 0) {
        tv_previous.isEnabled = false
        tv_next.isEnabled = false
        val size = adapterSelector.dataList.size
        if (size <= 0) return
        if (index < 0) return
        val group = adapterSelector.dataList[index]
        selectedIndex = index
        if (size > 0 && selectedIndex < size - 1)
            tv_next.isEnabled = true
        if (selectedIndex > 0)
            tv_previous.isEnabled = true
        group.apply {
            tv_select.text = time
            adapterSelector.notifyDataSetChanged()
            data?.firstOrNull()?.apply {
                tv_shareAll.text = shareAll
                tv_ashareAll.text = ashareAll
                tv_noLimitShare.text = noLimitShare
                tv_limitShare.text = limitShare
                tv_changeReason.text = changeReason
            }
            data?.lastOrNull()?.apply {
                tv_ashareAll_h.text = ashareAll
                tv_noLimitShare_h.text = noLimitShare
                tv_limitShare_h.text = limitShare
                tv_changeReason_h.text = changeReason
            }
        }
    }

    fun doRequest() {
        SoguApi.getService(application)
                .gubenjiegou(company_id = project.company_id!!, shareholder_type = type)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ payload ->
                    adapterSelector.dataList.clear()
                    if (payload.isOk) {
                        payload.payload?.apply {
                            adapterSelector.dataList.clear()
                            adapterSelector.dataList.addAll(this.reversed())
                        }
                    } else
                        showToast(payload.message)
                    setGroup(0)
                    adapterSelector.notifyDataSetChanged()
                }, { e ->
                    Trace.e(e)
                })
    }


    companion object {
        fun start(ctx: Activity?, project: ProjectBean) {
            val intent = Intent(ctx, GuBenJieGouActivity::class.java)
            intent.putExtra(Extras.DATA, project)
            ctx?.startActivity(intent)
        }
    }
}
